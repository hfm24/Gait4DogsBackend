package gait4dogs.backend.util;

import gait4dogs.backend.data.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;

public class AnalysisUtil {

    private final float GYROSCOPE_SENSITIVITY = 100;

    public AnalysisUtil() {

    }

    public SessionAnalytics doSessionAnalysis(SessionRawData rawData) {
        List<AccelerometerOutputAnalytics> accelerometerOutputAnalytics = new ArrayList<>();
        for (AccelerometerOutput accelerometerOutput : rawData.getAccelerometerOutputs()) {
            accelerometerOutputAnalytics.add(doAccelerometerAnalytics(accelerometerOutput));
        }

        List<Double> phaseShiftDifs = new ArrayList<>();
        if (accelerometerOutputAnalytics.size() == 2) {
            List<List<double[]>> shiftedMagnitudes = getShiftedMagnitudes(accelerometerOutputAnalytics.get(0), accelerometerOutputAnalytics.get(1));
            phaseShiftDifs = comparePhaseShift(shiftedMagnitudes.get(0), shiftedMagnitudes.get(1));
            accelerometerOutputAnalytics.get(0).setShiftedMagnitudes(shiftedMagnitudes.get(0));
            accelerometerOutputAnalytics.get(1).setShiftedMagnitudes(shiftedMagnitudes.get(1));
            return new SessionAnalytics(accelerometerOutputAnalytics, phaseShiftDifs.get(0), phaseShiftDifs.get(1));
        }
        else {
            return new SessionAnalytics(accelerometerOutputAnalytics);
        }
    }

    public AccelerometerOutputAnalytics doAccelerometerAnalytics(AccelerometerOutput accelerometerOutput) {
        List<float[]> axisData = new ArrayList<>();
        axisData.add(accelerometerOutput.getX());
        axisData.add(accelerometerOutput.getY());
        axisData.add(accelerometerOutput.getZ());

        float[] minimums = getMinimums(axisData);

        float[] maximums = getMaximums(axisData);



        float[] ranges = new float[3];
        ranges[0] = maximums[0] - minimums[0];
        ranges[1] = maximums[1] - minimums[1];
        ranges[2] = maximums[2] - minimums[2];



        //Finding the Magnitude
        float[] magnitudes = getMagnitudes(axisData);
        float minMagnitude = magnitudes[0];
        float maxMagnitude = magnitudes[1];
        float rangeMagnitude = magnitudes[2];

        List<Angle> angles = getAngles(accelerometerOutput);

        List<double[]> smoothedAcc = averageSmooth(accelerometerOutput.getX(), accelerometerOutput.getY(), accelerometerOutput.getZ(), accelerometerOutput.getEpoc());

        List<Long> footStrikeTimes = getFootStrikeTimes(smoothedAcc);

        AccelerometerOutputAnalytics AOA = new AccelerometerOutputAnalytics(minimums, maximums, ranges,
                minMagnitude, maxMagnitude, rangeMagnitude,
                angles, footStrikeTimes, smoothedAcc);
        return AOA;
    }

    private float[] getMinimums(List<float[]> axisData) {
        //Finding the minimums
        float[] minimums = new float[3];
        for (int j = 0; j < 3; j++) {
            float curr = 0;
            float near = axisData.get(j)[0];
            for (int i = 0; i < axisData.get(0).length; i++) {
                curr = axisData.get(j)[i] * axisData.get(j)[i];
                if (curr <= (near * near)) {
                    near = axisData.get(j)[i];
                }
            }
            minimums[j] = near;
        }
        return minimums;
    }

    private float[] getMaximums(List<float[]> axisData) {
        //Finding the Maximum Acceleration Values
        float[] maximums = new float[3];
        float[] x = Arrays.copyOf(axisData.get(0), axisData.get(0).length);
        float[] y = Arrays.copyOf(axisData.get(1), axisData.get(1).length);
        float[] z = Arrays.copyOf(axisData.get(2), axisData.get(2).length);
        Arrays.sort(x);
        Arrays.sort(y);
        Arrays.sort(z);

        if(abs(x[0]) > x[x.length-1]){
            maximums[0] = x[0];
        }
        else
            maximums[0] = x[x.length-1];

        if(abs(y[0]) > y[y.length-1]){
            maximums[1] = y[0];
        }
        else
            maximums[1] = y[y.length-1];


        if(abs(z[0]) > z[z.length-1]){
            maximums[2] = z[0];
        }
        else
            maximums[2] = z[z.length-1];

        return maximums;
    }

    private float[] getMagnitudes(List<float[]> axisData) {
        float maxMagnitude;
        float minMagnitude;
        float rangeMagnitude;

        float[] x = axisData.get(0);
        float[] y = axisData.get(1);
        float[] z = axisData.get(2);
        float[] magnitudes = new float[axisData.get(0).length];
        for(int i = 0; i < x.length ; i++){
            magnitudes[i] = (float) Math.sqrt((x[i] * x[i]) + (y[i] * y[i]) + (z[i] * z[i]));
        }

        maxMagnitude = magnitudes[0];
        minMagnitude = magnitudes[0];

        for (int i = 0; i < magnitudes.length; i++) {
            if (maxMagnitude < magnitudes[i]) {
                maxMagnitude = magnitudes[i];
            }
            if (minMagnitude > magnitudes[i]) {
                minMagnitude = magnitudes[i];
            }
        }

        rangeMagnitude = maxMagnitude - minMagnitude;

        return new float[]{minMagnitude,maxMagnitude,rangeMagnitude};
    }

    public List<Long> getFootStrikeTimes(List<double[]> axisData) {
        // Measuring mean distance between foot strikes
        List<Integer> footStrikes = getFootStrikes(axisData);
        List<Long> footStrikeTimes = new ArrayList<>();

        long sum = 0;
        int firstFootStrikeIdx = footStrikes.get(0);
        long lastEpoc = (long)axisData.get(3)[firstFootStrikeIdx];
        footStrikeTimes.add(lastEpoc);
        long currentEpoc;
        long dt;
        List<Long> dts = new ArrayList<>();
        System.out.println(lastEpoc);
        for (int i = 1; i < footStrikes.size(); i++) {
            int elapsedTIdx = footStrikes.get(i);
            currentEpoc = (long)axisData.get(3)[elapsedTIdx];
            footStrikeTimes.add(currentEpoc);
            System.out.println(currentEpoc);
            dt = currentEpoc - lastEpoc;
            dts.add(dt);
            lastEpoc = currentEpoc;
            sum += dt;
        }
        float avgDt = sum / dts.size();

        sum = 0;
        for (int i = 1; i < dts.size(); i++) {
            float sqrMean = (dts.get(i) - avgDt)*(dts.get(i) - avgDt);
            sum += sqrMean;
        }
        float dtVariance = sum / (dts.size()-1);

        System.out.println("average time between steps: " + avgDt);
        System.out.println("variance of time between steps: " + dtVariance);

        return footStrikeTimes;
    }

    public List<Integer> getFootStrikes(List<double[]> accData) {

        double[] x = accData.get(0);
        double[] y = accData.get(1);
        double[] z = accData.get(2);

        float epsilon = 0.25f;
        List<Integer> peakStridePoints = new ArrayList<>();
        double currentMagnitude;
        double lastMagnitude;
        double nextMagnitude;
        for (int i = 1; i < x.length-1; i++) {
            lastMagnitude = magnitude(x[i-1], y[i-1], z[i-1]);
            currentMagnitude = magnitude(x[i], y[i], z[i]);
            nextMagnitude = magnitude(x[i+1], y[i+1], z[i+1]);
            if (lastMagnitude > currentMagnitude && nextMagnitude-currentMagnitude > epsilon ) {
                peakStridePoints.add(i);
            }
        }
        return peakStridePoints;
    }

    public List<Angle> getAngles(AccelerometerOutput accelerometerOutput) {
        List<Angle> output = new ArrayList<Angle>();


        long[] t = accelerometerOutput.getEpoc();
        float[] x = accelerometerOutput.getX();
        float[] y = accelerometerOutput.getY();
        float[] z = accelerometerOutput.getZ();
        float[] xRot = accelerometerOutput.getxAxis();
        float[] yRot = accelerometerOutput.getyAxis();
        float[] zRot = accelerometerOutput.getzAxis();
        float pitch = 0;
        float roll = 0;
        List<double[]> smoothedAcc = averageSmooth(x, y, z, t);
        List<double[]> smoothedRot = averageSmooth(xRot, yRot, zRot, t);
        smoothedRot = getNoiseAverage(smoothedRot);
        double[] smoothAccX = smoothedAcc.get(0);
        double[] smoothAccY = smoothedAcc.get(1);
        double[] smoothAccZ = smoothedAcc.get(2);

        double[] smoothRotX = smoothedRot.get(0);
        double[] smoothRotY = smoothedRot.get(1);
        double[] smoothRotZ = smoothedRot.get(2);

        for (int i = 0; i < smoothedAcc.get(3).length; i++) {
            //System.out.println(t[i*3] + ", " + smoothedAcc.get(i)[0] + ", " + smoothedAcc.get(i)[1] + ", " + smoothedAcc.get(i)[2]);
            //System.out.println(t[i*3] + ", " + smoothedRot.get(i)[0] + ", " + smoothedRot.get(i)[1] + ", " + smoothedRot.get(i)[2]);
            double[] accX = new double[]{smoothAccX[i],smoothAccY[i], smoothAccZ[i]};
            double[] rotX = new double[]{smoothRotX[i],smoothRotY[i],smoothRotZ[i]};
            Angle thisAngle = complementaryFilter(accX, rotX, pitch, roll, 0.11f);
            pitch = thisAngle.getPitch();
            roll = thisAngle.getRoll();
            output.add(thisAngle);
        }

        return output;
    }

    public Angle complementaryFilter(double[] accData, double[] rotData, float pitch, float roll, double dt) {
        float pitchAcc, rollAcc;

        // Integrate the gyroscope data -> int(angularSpeed) = angle
        pitch += (rotData[0] ) * dt; // Angle around the x-axis
        roll -= (rotData[1] ) * dt; // Angle around the y-axis


        // Compensate for drift with accelerometer data if !bullshit
        // Sensitivity = -2 to 2 G at 16Bit -> 2G = 32768 && 0.5G = 8192
        double forceMagnitudeApprox = (accData[0]) + abs(accData[1]) + abs(accData[2]);
        if (forceMagnitudeApprox > 0.5 && forceMagnitudeApprox < 2) {
            // Turning around the X axis results in a vector on the Y-axis
            pitchAcc = (float) (Math.atan2(accData[1], accData[2]) * 180 / Math.PI);
            pitch = (float) (pitch * 0.98 + pitchAcc * 0.02);

            // Turning around the Y axis results in a vector on the X-axis
            rollAcc = (float) (Math.atan2(accData[0], accData[2]) * 180 / Math.PI);
            roll = (float) (roll * 0.98 + rollAcc * 0.02);
        }

        return new Angle(pitch, roll);
    }

    public List<double[]> averageSmooth(float[] x, float[] y, float[] z, long[] t) {

        List<double[]> smoothedAccelerometerOutput = new ArrayList<>();

        float xSum = 0, ySum = 0, zSum = 0;
        double[] averageX = new double[x.length / 3];
        double[] averageY = new double[y.length / 3];
        double[] averageZ = new double[z.length / 3];
        double[] averageT = new double[t.length / 3];

        for (int i = 0; i < x.length; i++) {
            xSum += x[i];
            ySum += y[i];
            zSum += z[i];

            if (i % 3 == 0) {
                averageX[i/3] = xSum/3;
                averageY[i/3] = ySum/3;
                averageZ[i/3] = zSum/3;
                averageT[i/3] = t[i];

                xSum = 0;
                ySum = 0;
                zSum = 0;

            }
        }
        smoothedAccelerometerOutput.add(averageX);
        smoothedAccelerometerOutput.add(averageY);
        smoothedAccelerometerOutput.add(averageZ);
        smoothedAccelerometerOutput.add(averageT);
        return smoothedAccelerometerOutput;
    }

    //noise
    public List<double[]> getNoiseAverage(List<double[]> gyroData){

        double[] xRotation = gyroData.get(0);
        double[] yRotation = gyroData.get(1);
        double[] zRotation = gyroData.get(2);

        double xMax = getMaxPoint(xRotation);
        double yMax = getMaxPoint(yRotation);
        double zMax = getMaxPoint(zRotation);

        double xNoise = xMax * .05;
        double yNoise = yMax * .05;
        double zNoise = zMax * .05;

        List<double[]> averagedNoise = new ArrayList<>();
        for(int i = 1; i < xRotation.length; i++){
            if(Math.abs(xRotation[i] - (xRotation[i]-1)) < xNoise){
                xRotation[i] = 0;
            }

            if(Math.abs(yRotation[i] - (yRotation[i]-1)) < yNoise){
                yRotation[i] = 0;
            }

            if(Math.abs(zRotation[i] - (zRotation[i]-1)) < zNoise){
                zRotation[i] = 0;
            }
        }



        averagedNoise.add(xRotation);
        averagedNoise.add(yRotation);
        averagedNoise.add(zRotation);

        return averagedNoise;
    }

    public double getMaxPoint(double[] list){
        double max = 0;
        for(int i = 0; i < list.length; i++) {
            if (list[i] > max) {
                max = list[i];
            }
        }
        return max;

    }

    private double magnitude(double x, double y, double z) {
        return (float)Math.sqrt(x*x+y*y+z*z);
    }

    private List<Double> comparePhaseShift(List<double[]> a, List<double[]> b) {
        List<Double> phaseShiftDifs = new ArrayList<>();
        double[] magnitudesA = a.get(0);
        double[] timesA = a.get(1);

        double[] magnitudesB = b.get(0);
        double[] timesB = b.get(0);

        // Get the difference between each datapoint and add all differences to find the area between both curves
        double totalDif = 0;
        for (int i = 0; i < magnitudesA.length; i++) {
            double dif;
            dif = getMagnitudeDifAtClosestTime(magnitudesA[i], timesA[i], magnitudesB, timesB);
            totalDif += dif;
        }
        double avgDif = totalDif / magnitudesA.length;

        phaseShiftDifs.add(totalDif);
        phaseShiftDifs.add(avgDif);

        System.out.println("Total difference in phase-shifted curves: " + totalDif);
        System.out.println("Average difference in phase-shifted curves: " + avgDif);
        // If the area is small, good.
        // If the area is large, bad.

        return phaseShiftDifs;
    }

    private List<List<double[]>> getShiftedMagnitudes(AccelerometerOutputAnalytics a, AccelerometerOutputAnalytics b) {
        // If accel. 1 has a higher range, use it as the control. Else use accel. 2
        AccelerometerOutputAnalytics control;
        AccelerometerOutputAnalytics compare;
        if (a.getRangeMagnitude() > b.getRangeMagnitude()) {
            control = a;
            compare = b;
        } else {
            control = b;
            compare = a;
        }

        // Take 1/2 the period (mean dt between steps) of the control.
        // If the control made the first step, add 1/2 period to each datapoint
        // else subtract 1/2 period from each datapoint
        double[] xA = control.getSmoothedAcc().get(0);
        double[] yA = control.getSmoothedAcc().get(1);
        double[] zA = control.getSmoothedAcc().get(2);
        double[] magnitudesA = new double[xA.length];
        double[] timesA = new double[xA.length];
        double period = footStrikePeriod(control.getFootStrikeTimes());
        if (control.getFootStrikeTimes().get(0) < compare.getFootStrikeTimes().get(0)) {
            for (int i = 0; i < xA.length; i++) {
                double magnitude = Math.sqrt(xA[i]*xA[i] + yA[i]*yA[i] + zA[i]*zA[i]);
                double t = control.getSmoothedAcc().get(3)[i];
                t += period/2;
                magnitudesA[i] = magnitude;
                timesA[i] = t;
            }
        } else {
            for (int i = 0; i < xA.length; i++) {
                double magnitude = Math.sqrt(xA[i]*xA[i] + yA[i]*yA[i] + zA[i]*zA[i]);
                double t = control.getSmoothedAcc().get(3)[i];
                t -= period/2;
                magnitudesA[i] = magnitude;
                timesA[i] = t;
            }
        }

        double[] xB = compare.getSmoothedAcc().get(0);
        double[] yB = compare.getSmoothedAcc().get(1);
        double[] zB = compare.getSmoothedAcc().get(2);
        double[] timesB = compare.getSmoothedAcc().get(3);
        double[] magnitudesB = new double[xB.length];
        for (int i = 0; i < magnitudesB.length; i++) {
            double magnitude = Math.sqrt(xB[i]*xB[i]+yB[i]*yB[i]+zB[i]*zB[i]);
            magnitudesB[i] = magnitude;
        }

        List<double[]> shiftedMagnitudesA = new ArrayList<>();
        List<double[]> shiftedMagnitudesB = new ArrayList<>();

        if (a.getRangeMagnitude() > b.getRangeMagnitude()) {
            shiftedMagnitudesA.add(magnitudesA);
            shiftedMagnitudesA.add(timesA);
            shiftedMagnitudesB.add(magnitudesB);
            shiftedMagnitudesB.add(timesB);
        } else {
            shiftedMagnitudesA.add(magnitudesB);
            shiftedMagnitudesA.add(timesB);
            shiftedMagnitudesB.add(magnitudesA);
            shiftedMagnitudesB.add(timesA);
        }

        List<List<double[]>> shiftedMagnitudes = new ArrayList<>();
        shiftedMagnitudes.add(shiftedMagnitudesA);
        shiftedMagnitudes.add(shiftedMagnitudesB);
        return shiftedMagnitudes;
    }

    private double footStrikePeriod(List<Long> footStrikeTimes) {
        long sum = 0;
        long lastEpoc = (long)footStrikeTimes.get(0);
        footStrikeTimes.add(lastEpoc);
        long currentEpoc;
        long dt;
        List<Long> dts = new ArrayList<>();
        for (int i = 1; i < footStrikeTimes.size(); i++) {
            currentEpoc = footStrikeTimes.get(i);
            dt = currentEpoc - lastEpoc;
            dts.add(dt);
            lastEpoc = currentEpoc;
            sum += dt;
        }
        float avgDt = sum / dts.size();

        return avgDt;
    }

    private double getMagnitudeDifAtClosestTime(double mag, double t, double[] b, double[] tb) {
        double lastTDif = Double.POSITIVE_INFINITY;
        double tDif;
        double magDif = 0;
        for (int i = 0; i < b.length; i++) {
            tDif = tb[i] - t;
            if (tDif > lastTDif)
                break;
            magDif = Math.abs(mag - b[i]);
            lastTDif = tDif;
        }

        return magDif;
    }
}


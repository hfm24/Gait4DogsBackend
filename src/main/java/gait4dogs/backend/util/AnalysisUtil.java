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

        if (accelerometerOutputAnalytics.size() == 2) {
            List<List<double[]>> shiftedMagnitudes = getShiftedMagnitudes(accelerometerOutputAnalytics.get(0), accelerometerOutputAnalytics.get(1));
            List<Double> phaseShiftDifs = comparePhaseShift(shiftedMagnitudes.get(0), shiftedMagnitudes.get(1));
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
        int averagePeriod = 3;

        List<Angle> angles = getAngles(accelerometerOutput);

        List<double[]> smoothedAcc = averageSmooth(accelerometerOutput.getX(), accelerometerOutput.getY(), accelerometerOutput.getZ(), accelerometerOutput.getEpoc(),averagePeriod);

        List<Long> footStrikeTimes = getFootStrikeTimes(smoothedAcc);

        AccelerometerOutputAnalytics AOA = new AccelerometerOutputAnalytics(minimums, maximums, ranges,
                minMagnitude, maxMagnitude, rangeMagnitude,
                angles, footStrikeTimes, smoothedAcc);
        return AOA;
    }

    private float[] getMinimums(List<float[]> axisData) {
        //Finding the minimums
        float[] minimums = new float[axisData.size()];
        for (int j = 0; j < axisData.size(); j++) {
            float curr = 0;
            float near = axisData.get(j)[0];
            for (int i = 0; i < axisData.get(j).length; i++) {
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
        float[] maximums = new float[axisData.size()];
        for (int j = 0; j < axisData.size(); j++) {
            float curr = 0;
            float near = (float)Double.NEGATIVE_INFINITY;
            for (int i = 0; i < axisData.get(j).length; i++) {
                curr = axisData.get(j)[i] * axisData.get(j)[i];
                if (curr > (near * near)) {
                    near = axisData.get(j)[i];
                }
            }
            maximums[j] = near;
        }
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
        int averagePeriod = 3;
        List<double[]> smoothedAcc = averageSmooth(x, y, z, t, averagePeriod);
        List<double[]> smoothedRot = averageSmooth(xRot, yRot, zRot, t, averagePeriod);
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

    public List<double[]> averageSmooth(float[] x, float[] y, float[] z, long[] t, int averagePeriod) {

        List<double[]> smoothedAccelerometerOutput = new ArrayList<>();

        float xSum = 0, ySum = 0, zSum = 0;
        double[] averageX = new double[x.length / averagePeriod];
        double[] averageY = new double[y.length / averagePeriod];
        double[] averageZ = new double[z.length / averagePeriod];
        double[] averageT = new double[t.length / averagePeriod];


        for (int i = 0; i < x.length; i++) {
            xSum += x[i];
            ySum += y[i];
            zSum += z[i];

            if (i % 3 == 0) {
                averageX[i/averagePeriod] = xSum/averagePeriod;
                averageY[i/averagePeriod] = ySum/averagePeriod;
                averageZ[i/averagePeriod] = zSum/averagePeriod;
                averageT[i/averagePeriod] = t[i];

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


        xNoise = 0;
        yNoise = 0;
        zNoise = 0;

        List<double[]> averagedNoise = new ArrayList<>();
        for(int i = 0; i < xRotation.length; i++){
            xRotation[i] = xRotation[i] - xNoise;
            yRotation[i] = yRotation[i] - yNoise;
            zRotation[i] = zRotation[i] - zNoise;
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
        double[] magnitudesControl = a.get(0);
        double[] timesControl = a.get(1);

        double[] magnitudesCompare = b.get(0);
        double[] timesCompare = b.get(0);

        // Get the difference between each datapoint and add all differences to find the area between both curves
        double totalDif = 0;
        for (int i = 0; i < magnitudesControl.length; i++) {
            double dif;
            dif = getMagnitudeDifAtClosestTime(magnitudesControl[i], timesControl[i], magnitudesCompare, timesCompare);
            totalDif += dif;
        }
        double avgDif = totalDif / magnitudesControl.length;

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
        double[] magnitudesControl = new double[xA.length];
        double[] timesControl = new double[xA.length];
        List<Long> shiftedFootStrikesControl = new ArrayList<>();
        double period = footStrikePeriod(control.getFootStrikeTimes());
        if (control.getFootStrikeTimes().get(0) < compare.getFootStrikeTimes().get(0)) {
            for (int i = 0; i < xA.length; i++) {
                double magnitude = Math.sqrt(xA[i]*xA[i] + yA[i]*yA[i] + zA[i]*zA[i]);
                double t = control.getSmoothedAcc().get(3)[i];
                t += period/2;
                magnitudesControl[i] = magnitude;
                timesControl[i] = t;
            }
            for (int i = 0; i < control.getFootStrikeTimes().size(); i++) {
                shiftedFootStrikesControl.add(control.getFootStrikeTimes().get(i) + Math.round(period/2));
            }
        } else {
            for (int i = 0; i < xA.length; i++) {
                double magnitude = Math.sqrt(xA[i]*xA[i] + yA[i]*yA[i] + zA[i]*zA[i]);
                double t = control.getSmoothedAcc().get(3)[i];
                t -= period/2;
                magnitudesControl[i] = magnitude;
                timesControl[i] = t;
            }
            for (int i = 0; i < control.getFootStrikeTimes().size(); i++) {
                shiftedFootStrikesControl.add(control.getFootStrikeTimes().get(i) - Math.round(period/2));
            }
        }

        double[] xB = compare.getSmoothedAcc().get(0);
        double[] yB = compare.getSmoothedAcc().get(1);
        double[] zB = compare.getSmoothedAcc().get(2);
        double[] timesCompare = compare.getSmoothedAcc().get(3);
        double[] magnitudesCompare = new double[xB.length];
        for (int i = 0; i < magnitudesCompare.length; i++) {
            double magnitude = Math.sqrt(xB[i]*xB[i]+yB[i]*yB[i]+zB[i]*zB[i]);
            magnitudesCompare[i] = magnitude;
        }

        List<double[]> shiftedMagnitudesControl = new ArrayList<>();
        List<double[]> shiftedMagnitudesCompare = new ArrayList<>();

        shiftedMagnitudesControl.add(magnitudesControl);
        shiftedMagnitudesControl.add(timesControl);
        shiftedMagnitudesCompare.add(magnitudesCompare);
        shiftedMagnitudesCompare.add(timesCompare);
        control.setShiftedMagnitudes(shiftedMagnitudesControl);
        control.setShiftedFootStrikeTimes(shiftedFootStrikesControl);
        compare.setShiftedMagnitudes(shiftedMagnitudesCompare);
        compare.setShiftedFootStrikeTimes(compare.getFootStrikeTimes());

        List<List<double[]>> shiftedMagnitudes = new ArrayList<>();
        shiftedMagnitudes.add(shiftedMagnitudesControl);
        shiftedMagnitudes.add(shiftedMagnitudesCompare);
        return shiftedMagnitudes;
    }

    private double footStrikePeriod(List<Long> footStrikeTimes) {
        long sum = 0;
        long lastEpoc = (long)footStrikeTimes.get(0);
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


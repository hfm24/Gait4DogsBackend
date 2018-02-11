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
            comparePhaseShift(accelerometerOutputAnalytics.get(0), accelerometerOutputAnalytics.get(1));
        }

        return new SessionAnalytics(accelerometerOutputAnalytics);
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
        long lastEpoc = (long)axisData.get(firstFootStrikeIdx)[3];
        footStrikeTimes.add(lastEpoc);
        long currentEpoc;
        long dt;
        List<Long> dts = new ArrayList<>();
        System.out.println(lastEpoc);
        for (int i = 1; i < footStrikes.size(); i++) {
            int elapsedTIdx = footStrikes.get(i);
            currentEpoc = (long)axisData.get(elapsedTIdx)[3];
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

        for (int i = 0; i < smoothedAcc.size(); i++) {
            //System.out.println(t[i*3] + ", " + smoothedAcc.get(i)[0] + ", " + smoothedAcc.get(i)[1] + ", " + smoothedAcc.get(i)[2]);
            //System.out.println(t[i*3] + ", " + smoothedRot.get(i)[0] + ", " + smoothedRot.get(i)[1] + ", " + smoothedRot.get(i)[2]);
            Angle thisAngle = complementaryFilter(smoothedAcc.get(i), smoothedRot.get(i), pitch, roll, 0.11f);
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

        for (int i = 0; i < x.length; i++) {
            xSum += x[i];
            ySum += y[i];
            zSum += z[i];

            if (i % 3 == 0) {
                double[] averageList = new double[4];
                averageList[0] = xSum/3;
                averageList[1] = ySum/3;
                averageList[2] = zSum/3;
                averageList[3] = t[i];

                xSum = 0;
                ySum = 0;
                zSum = 0;
                smoothedAccelerometerOutput.add(averageList);
            }
        }
        return smoothedAccelerometerOutput;
    }

    public List<Integer> getFootStrikes(List<double[]> accData) {
        float epsilon = 0.25f;
        List<Integer> peakStridePoints = new ArrayList<>();
        double currentMagnitude;
        double lastMagnitude;
        double nextMagnitude;
        for (int i = 1; i < accData.size()-1; i++) {
            double[] lastDataPoint = accData.get(i-1);
            double[] dataPoint = accData.get(i);
            double[] nextDataPoint = accData.get(i+1);
            lastMagnitude = magnitude(lastDataPoint[0], lastDataPoint[1], lastDataPoint[2]);
            currentMagnitude = magnitude(dataPoint[0], dataPoint[1], dataPoint[2]);
            nextMagnitude = magnitude(nextDataPoint[0], nextDataPoint[1], nextDataPoint[2]);
            if (lastMagnitude > currentMagnitude && nextMagnitude-currentMagnitude > epsilon ) {
                peakStridePoints.add(i);
            }
        }
        return peakStridePoints;
    }

    private double magnitude(double x, double y, double z) {
        return (float)Math.sqrt(x*x+y*y+z*z);
    }

    private void comparePhaseShift(AccelerometerOutputAnalytics acc1, AccelerometerOutputAnalytics acc2) {
        // To start, make sure accel. 1 and accel. 2 have the same number of datapoints and start and end at the same time:
        // If accel. 1 started before accel. 2, add a buffer of 0's to the beginning of accel. 2
            // else If accel. 2 started before accel. 1, add a buffer of 0's to the beginning of accel. 1
        // If accel. 1 ended before accel. 2, add a buffer of 0's to the end of accel. 1
            // else If accel. 2 ended before accel. 1, add a buffer of 0's to the end of accel. 2

        // Now for the analysis:
        // If accel. 1 has a higher range, use it as the control. Else use accel. 2
        // Take 1/2 the period (mean dt between steps) of the control.
        // If the control made the first step, add 1/2 period to each datapoint
            // else subtract 1/2 period from each datapoint
        // Compare the new control with the other accel:
            // Get the difference between each datapoint and add all differences to find the area between both curves

        // If the area is small, good.
        // If the area is large, bad.
    }
}


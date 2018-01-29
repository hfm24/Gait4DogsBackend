package gait4dogs.backend.util;

import gait4dogs.backend.data.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;

public class AnalysisUtil {

    private final float ACCELEROMETER_SENSITIVITY = 0.5f;
    private final float GYROSCOPE_SENSITIVITY = 65;

    public AnalysisUtil() {

    }

    public SessionAnalytics doSessionAnalysis(SessionRawData rawData) {


        //Finding the minimums
        List<AccelerometerOutputAnalytics> accelerometerOutputAnalytics = new ArrayList<>();
        for (AccelerometerOutput accelerometerOutput : rawData.getAccelerometerOutputs()) {
            List<float[]> axisData = new ArrayList<float[]>();
            axisData.add(accelerometerOutput.getX());
            axisData.add(accelerometerOutput.getY());
            axisData.add(accelerometerOutput.getZ());

            float[] minimums = new float[3];
            for (int j = 0; j < 3; j++) {
                float curr = 0;
                float near = axisData.get(j)[0];
                for (int i = 0; i < accelerometerOutput.getX().length; i++) {
                    curr = axisData.get(j)[i] * axisData.get(j)[i];
                    if (curr <= (near * near)) {
                        near = axisData.get(j)[i];
                    }
                }
                minimums[j] = near;
            }


            //Finding the Maximum Acceleration Values
            float[] maximums = new float[3];
            Arrays.sort(accelerometerOutput.getX());
            Arrays.sort(accelerometerOutput.getY());
            Arrays.sort(accelerometerOutput.getZ());

            if (abs(accelerometerOutput.getX()[0]) > accelerometerOutput.getX()[accelerometerOutput.getX().length - 1]) {
                maximums[0] = accelerometerOutput.getX()[0];
            } else
                maximums[0] = accelerometerOutput.getX()[accelerometerOutput.getX().length - 1];

            if (abs(accelerometerOutput.getY()[0]) > accelerometerOutput.getY()[accelerometerOutput.getY().length - 1]) {
                maximums[1] = accelerometerOutput.getY()[0];
            } else
                maximums[1] = accelerometerOutput.getY()[accelerometerOutput.getY().length - 1];


            if (abs(accelerometerOutput.getZ()[0]) > accelerometerOutput.getZ()[accelerometerOutput.getZ().length - 1]) {
                maximums[2] = accelerometerOutput.getZ()[0];
            } else
                maximums[2] = accelerometerOutput.getZ()[accelerometerOutput.getZ().length - 1];


            float[] ranges = new float[3];
            ranges[0] = maximums[0] - minimums[0];
            ranges[1] = maximums[1] - minimums[1];
            ranges[2] = maximums[2] - minimums[2];


            //Finding the Magnitude
            float maxMagnitude;
            float minMagnitude;
            float rangeMagnitude;

            float[] magnitudes = new float[accelerometerOutput.getX().length];
            for (int i = 0; i < accelerometerOutput.getX().length; i++) {
                magnitudes[i] = (float) Math.sqrt((accelerometerOutput.getX()[i] * accelerometerOutput.getX()[i]) + (accelerometerOutput.getY()[i] * accelerometerOutput.getY()[i]) + (accelerometerOutput.getZ()[i] * accelerometerOutput.getZ()[i]));
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

            List<Angle> angles = getAngles(accelerometerOutput);
            accelerometerOutputAnalytics.add(new AccelerometerOutputAnalytics(minimums, maximums, ranges, minMagnitude, maxMagnitude, rangeMagnitude, angles));
        }


        return new SessionAnalytics(accelerometerOutputAnalytics);
    }

    public List<Angle> getAngles(AccelerometerOutput accelerometerOutput) {
        List<Angle> output = new ArrayList<Angle>();
        float[] t = accelerometerOutput.getElapsed();
        float[] x = accelerometerOutput.getX();
        float[] y = accelerometerOutput.getY();
        float[] z = accelerometerOutput.getZ();
        float[] xRot = accelerometerOutput.getxAxis();
        float[] yRot = accelerometerOutput.getyAxis();
        float[] zRot = accelerometerOutput.getzAxis();
        float pitch = 0;
        float roll = 0;

        for (int i = 0; i < t.length; i++) {
            float[] accData = new float[]{x[i], y[i], z[i]};
            float[] rotData = new float[]{xRot[i], yRot[i], zRot[i]};
            float dt;
            if (i == 0) {
                dt = 0;
            } else {
                dt = t[i] - t[i - 1];
            }
            output.add(complementaryFilter(accData, rotData, pitch, roll, dt));
        }

        return output;
    }

    public Angle complementaryFilter(float[] accData, float[] rotData, float pitch, float roll, float dt) {
        float pitchAcc, rollAcc;

        // Integrate the gyroscope data -> int(angularSpeed) = angle
        pitch += ((float) rotData[0] / GYROSCOPE_SENSITIVITY) * dt; // Angle around the x-axis
        roll -= ((float) rotData[1] / GYROSCOPE_SENSITIVITY) * dt; // Angle around the y-axis

        // Compensate for drift with accelerometer data if !bullshit
        // Sensitivity = -2 to 2 G at 16Bit -> 2G = 32768 && 0.5G = 8192
        float forceMagnitudeApprox = (accData[0]) + abs(accData[1]) + abs(accData[2]);
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


    public List<float[]> averageSmooth(AccelerometerOutput accelerometerOutput) {

        List<float[]> smoothedAccelerometerOutput = new ArrayList<>();

        float xSum = 0, ySum = 0, zSum = 0;
        float[] x = accelerometerOutput.getX();
        float[] y = accelerometerOutput.getY();
        float[] z = accelerometerOutput.getZ();

        for (int i = 0; i < x.length; i++) {
            xSum += x[i];
            ySum += y[i];
            zSum += z[i];

            if (i % 6 == 0) {
                float[] averageList = new float[3];
                averageList[0] = xSum/6;
                averageList[1] = ySum/6;
                averageList[2] = zSum/6;

                xSum = 0;
                ySum = 0;
                zSum = 0;
                smoothedAccelerometerOutput.add(averageList);
            }
        }
        return smoothedAccelerometerOutput;
    }

}


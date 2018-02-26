package gait4dogs.backend.util;

import gait4dogs.backend.data.Angle;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class FilterUtil {

    public static Angle complementaryFilter(double[] accData, double[] rotData, double pitch, double roll, double dt) {
        double pitchAcc, rollAcc;

        // Integrate the gyroscope data -> int(angularSpeed) = angle
        pitch += (rotData[0] ) * dt; // Angle around the x-axis
        roll -= (rotData[1] ) * dt; // Angle around the y-axis


        // Compensate for drift with accelerometer data if !bullshit
        // Sensitivity = -2 to 2 G at 16Bit -> 2G = 32768 && 0.5G = 8192
        double forceMagnitudeApprox = (accData[0]) + abs(accData[1]) + abs(accData[2]);
        if (forceMagnitudeApprox > 0.5 && forceMagnitudeApprox < 2) {
            // Turning around the X axis results in a vector on the Y-axis
            pitchAcc = (double) (Math.atan2(accData[1], accData[2]) * 180 / Math.PI);
            pitch = (double) (pitch * 0.98 + pitchAcc * 0.02);

            // Turning around the Y axis results in a vector on the X-axis
            rollAcc = (double) (Math.atan2(accData[0], accData[2]) * 180 / Math.PI);
            roll = (double) (roll * 0.98 + rollAcc * 0.02);
        }

        return new Angle(pitch, roll);
    }


    //noise
    public static List<double[]> getNoiseAverage(List<double[]> gyroData){

        double[] xRotation = gyroData.get(0);
        double[] yRotation = gyroData.get(1);
        double[] zRotation = gyroData.get(2);

        double xMax = MathUtil.getMaxPoint(xRotation);
        double yMax = MathUtil.getMaxPoint(yRotation);
        double zMax = MathUtil.getMaxPoint(zRotation);

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
}

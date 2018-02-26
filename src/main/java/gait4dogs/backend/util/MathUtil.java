package gait4dogs.backend.util;

import gait4dogs.backend.data.AccelerometerOutput;
import gait4dogs.backend.data.Angle;

import java.util.ArrayList;
import java.util.List;

public class MathUtil {


    public static float[] getMinimums(List<float[]> axisData) {
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

    public static float[] getMaximums(List<float[]> axisData) {
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

    public static List<Angle> getAngles(AccelerometerOutput accelerometerOutput) {
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


    public static List<double[]> averageSmooth(float[] x, float[] y, float[] z, long[] t, int averagePeriod) {

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

    public static double magnitude(double x, double y, double z) {
        return (float)Math.sqrt(x*x+y*y+z*z);
    }

    public static double getMaxPoint(double[] list){
        double max = 0;
        for(int i = 0; i < list.length; i++) {
            if (list[i] > max) {
                max = list[i];
            }
        }
        return max;

    }

    public static double getMagnitudeDifAtClosestTime(double mag, double t, double[] b, double[] tb) {
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

    private float[] getMagnitudes(List<float[]> axisData) {
        float[] x = axisData.get(0);
        float[] y = axisData.get(1);
        float[] z = axisData.get(2);
        float[] magnitudes = new float[axisData.get(0).length];
        for (int i = 0; i < x.length; i++) {
            magnitudes[i] = (float) magnitude(x[i], y[i], z[i]);
        }

        return magnitudes;
    }

}





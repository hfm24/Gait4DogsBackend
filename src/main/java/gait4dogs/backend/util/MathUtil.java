package gait4dogs.backend.util;

import gait4dogs.backend.data.AccelerometerOutput;
import gait4dogs.backend.data.Angle;

import java.util.ArrayList;
import java.util.List;

public class MathUtil {


    public static double[] getMinimums(List<double[]> axisData) {
        //Finding the minimums
        double[] minimums = new double[axisData.size()];
        for (int j = 0; j < axisData.size(); j++) {
            double min = minimum(axisData.get(j));
            minimums[j] = min;
        }
        return minimums;
    }

    public static double[] getMaximums(List<double[]> axisData) {
        //Finding the Maximum Acceleration Values
        double[] maximums = new double[axisData.size()];
        for (int j = 0; j < axisData.size(); j++) {
            double max = maximum(axisData.get(j));
            maximums[j] = max;
        }
        return maximums;
    }

    public static double maximum(double[] data) {
        double max = data[0];
        for (int i = 0; i < data.length; i++) {
            double curr = data[i] * data[i];
            if (curr > (max * max)) {
                max = data[i];
            }
        }
        return max;
    }

    public static double minimum(double[] data) {
        double min = data[0];
        for (int i = 0; i < data.length; i++) {
            double curr = data[i] * data[i];
            if (curr <= (min * min)) {
                min = data[i];
            }
        }
        return min;
    }

    public static List<Angle> getAngles(AccelerometerOutput accelerometerOutput) {
        List<Angle> output = new ArrayList<Angle>();
        long[] t = accelerometerOutput.getEpoc();
        double[] x = accelerometerOutput.getX();
        double[] y = accelerometerOutput.getY();
        double[] z = accelerometerOutput.getZ();
        double[] xRot = accelerometerOutput.getxAxis();
        double[] yRot = accelerometerOutput.getyAxis();
        double[] zRot = accelerometerOutput.getzAxis();
        double pitch = 0;
        double roll = 0;
        int averagePeriod = 3;
        List<double[]> smoothedAcc = averageSmooth(x, y, z, t, averagePeriod);
        List<double[]> smoothedRot = averageSmooth(xRot, yRot, zRot, t, averagePeriod);
        smoothedRot = FilterUtil.getNoiseAverage(smoothedRot);
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
            Angle thisAngle = FilterUtil.complementaryFilter(accX, rotX, pitch, roll, 0.11f);
            pitch = thisAngle.getPitch();
            roll = thisAngle.getRoll();
            output.add(thisAngle);
        }

        return output;
    }


    public static List<double[]> averageSmooth(double[] x, double[] y, double[] z, long[] t, int averagePeriod) {

        List<double[]> smoothedAccelerometerOutput = new ArrayList<>();

        double xSum = 0, ySum = 0, zSum = 0;
        int pad = 1;
        if (x.length % averagePeriod == 0 ) {
            pad = 0;
        }
        double[] averageX = new double[x.length / averagePeriod + pad];
        double[] averageY = new double[y.length / averagePeriod + pad];
        double[] averageZ = new double[z.length / averagePeriod + pad];
        double[] averageT = new double[t.length / averagePeriod + pad];


        for (int i = 0; i < x.length; i++) {
            xSum += x[i];
            ySum += y[i];
            zSum += z[i];

            if (i % averagePeriod == 0) {
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
        return (double)Math.sqrt(x*x+y*y+z*z);
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

    public static double[] getMagnitudes(List<double[]> axisData) {
        double[] x = axisData.get(0);
        double[] y = axisData.get(1);
        double[] z = axisData.get(2);
        double[] magnitudes = new double[axisData.get(0).length];
        for (int i = 0; i < x.length; i++) {
            magnitudes[i] = magnitude(x[i], y[i], z[i]);
        }

        return magnitudes;
    }

}





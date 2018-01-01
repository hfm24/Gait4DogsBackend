package gait4dogs.backend.util;

import gait4dogs.backend.data.AccelerometerOutput;
import gait4dogs.backend.data.AccelerometerOutputAnalytics;
import gait4dogs.backend.data.SessionAnalytics;
import gait4dogs.backend.data.SessionRawData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalysisUtil {
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
            for(int j = 0; j < 3; j++) {
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

            if(Math.abs(accelerometerOutput.getX()[0]) > accelerometerOutput.getX()[accelerometerOutput.getX().length-1]){
                maximums[0] = accelerometerOutput.getX()[0];
            }
            else
                maximums[0] = accelerometerOutput.getX()[accelerometerOutput.getX().length-1];

            if(Math.abs(accelerometerOutput.getY()[0]) > accelerometerOutput.getY()[accelerometerOutput.getY().length-1]){
                maximums[1] = accelerometerOutput.getY()[0];
            }
            else
                maximums[1] = accelerometerOutput.getY()[accelerometerOutput.getY().length-1];


            if(Math.abs(accelerometerOutput.getZ()[0]) > accelerometerOutput.getZ()[accelerometerOutput.getZ().length-1]){
                maximums[2] = accelerometerOutput.getZ()[0];
            }
            else
                maximums[2] = accelerometerOutput.getZ()[accelerometerOutput.getZ().length-1];


            float[] ranges = new float[3];
            ranges[0] = maximums[0] - minimums[0];
            ranges[1] = maximums[1] - minimums[1];
            ranges[2] = maximums[2] - minimums[2];



            //Finding the Magnitude
            float maxMagnitude;
            float minMagnitude;
            float rangeMagnitude;

            float[] magnitudes = new float[accelerometerOutput.getX().length];
            for(int i = 0; i < accelerometerOutput.getX().length ; i++){
                magnitudes[i] = (float) Math.sqrt((accelerometerOutput.getX()[i] * accelerometerOutput.getX()[i]) + (accelerometerOutput.getY()[i] * accelerometerOutput.getY()[i]) + (accelerometerOutput.getZ()[i] * accelerometerOutput.getZ()[i]));
            }

            maxMagnitude = magnitudes[0];
            minMagnitude = magnitudes[0];

            for(int i = 0; i < magnitudes.length ; i++){
                if(maxMagnitude < magnitudes[i]){
                    maxMagnitude = magnitudes[i];
                }
                if(minMagnitude > magnitudes[i]){
                    minMagnitude = magnitudes[i];
                }
            }

            rangeMagnitude = maxMagnitude - minMagnitude;
            accelerometerOutputAnalytics.add(new AccelerometerOutputAnalytics(minimums, maximums, ranges, minMagnitude, maxMagnitude, rangeMagnitude));
        }


        return new SessionAnalytics(accelerometerOutputAnalytics);
    }
}

package gait4dogs.backend.util;

import gait4dogs.backend.data.SessionAnalytics;
import gait4dogs.backend.data.SessionRawData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnalysisUtil {
    public AnalysisUtil() {

    }

    public SessionAnalytics doSessionAnalysis(SessionRawData rawData, long sessionId) {

        List<float[]> axisData = new ArrayList<float[]>();
        axisData.add(rawData.getX());
        axisData.add(rawData.getY());
        axisData.add(rawData.getZ());

        float[] minimums = new float[3];
        for(int j = 0; j < 3; j++) {
            float curr = 0;
            float near = axisData.get(j)[0];
            for (int i = 0; i < rawData.getX().length; i++) {
                curr = axisData.get(j)[i] * axisData.get(j)[i];
                if (curr <= (near * near)) {
                    near = axisData.get(j)[i];
                }
            }
            minimums[j] = near;
        }


        float[] maximums = new float[3];
        Arrays.sort(rawData.getX());
        Arrays.sort(rawData.getY());
        Arrays.sort(rawData.getZ());

        if(Math.abs(rawData.getX()[0]) > rawData.getX()[rawData.getX().length-1]){
            maximums[0] = rawData.getX()[0];
        }
        else
            maximums[0] = rawData.getX()[rawData.getX().length-1];

        if(Math.abs(rawData.getY()[0]) > rawData.getY()[rawData.getY().length-1]){
            maximums[1] = rawData.getY()[0];
        }
        else
            maximums[1] = rawData.getY()[rawData.getY().length-1];


        if(Math.abs(rawData.getZ()[0]) > rawData.getZ()[rawData.getZ().length-1]){
            maximums[2] = rawData.getZ()[0];
        }
        else
            maximums[2] = rawData.getZ()[rawData.getZ().length-1];


        float[] ranges = new float[3];
        ranges[0] = maximums[0] - minimums[0];
        ranges[1] = maximums[1] - minimums[1];
        ranges[2] = maximums[2] - minimums[2];

        return new SessionAnalytics(sessionId);
    }
}
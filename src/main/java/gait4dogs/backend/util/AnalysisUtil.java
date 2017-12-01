package gait4dogs.backend.util;

import gait4dogs.backend.data.SessionAnalytics;
import gait4dogs.backend.data.SessionRawData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class AnalysisUtil {
    public AnalysisUtil() {

    }

    public SessionAnalytics doSessionAnalysis(SessionRawData rawData, long sessionId, AtomicLong counter) {

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

        return new SessionAnalytics(counter.incrementAndGet(), sessionId, minimums);
    }
}

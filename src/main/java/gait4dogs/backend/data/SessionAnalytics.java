package gait4dogs.backend.data;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class SessionAnalytics {
    private float[] minimums;
    private float[] maximums;
    private float[] ranges;
    private float minMagnitude, maxMagnitude, rangeMagnitude;

    public SessionAnalytics(float[] minimums, float[] maximums, float[] ranges, float minMagnitude, float maxMagnitude, float rangeMagnitude) {
        this.minimums = minimums;
        this.maximums = maximums;
        this.ranges = ranges;
        this.minMagnitude = minMagnitude;
        this.maxMagnitude = maxMagnitude;
        this.rangeMagnitude = rangeMagnitude;
    }

    public float[] getMinimums() {
        return minimums;
    }

    public float[] getMaximums() {
        return maximums;
    }

    public float[] getRanges() {
        return ranges;
    }

    public float getMinMagnitude() {
        return minMagnitude;
    }

    public float getMaxMagnitude(){
        return maxMagnitude;
    }

    public float getRangeMagnitude() {
        return rangeMagnitude;
    }

    public Document toDocument() {
        List<Float> minList = new ArrayList<>();
        List<Float> maxList = new ArrayList<>();
        List<Float> rangeList = new ArrayList<>();
        for (int i = 0; i < minimums.length; i++) {
            minList.add(minimums[i]);
            maxList.add(maximums[i]);
            rangeList.add(ranges[i]);
        }


        Document doc = new Document("minimums", minList)
                .append("maximums", maxList)
                .append("ranges", rangeList)
                .append("minMagnitude", minMagnitude)
                .append("maxMagnitude", maxMagnitude)
                .append("rangeMagnitude", rangeMagnitude);

        return doc;
    }
}

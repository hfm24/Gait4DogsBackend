package gait4dogs.backend.data;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class SessionAnalytics {
    private float[] minimums;
    private float[] maximums;

    public SessionAnalytics(float[] minimums, float[] maximums) {
        this.minimums = minimums;
        this.maximums = maximums;
    }

    public float[] getMinimums() {
        return minimums;
    }

    public float[] getMaximums() {
        return maximums;
    }

    public Document toDocument() {
        List<Float> minList = new ArrayList<>();
        List<Float> maxList = new ArrayList<>();
        for (int i = 0; i < minimums.length; i++) {
            minList.add(minimums[i]);
            maxList.add(maximums[i]);
        }

        Document doc = new Document("minimums", minList)
                .append("maximums", maxList);

        return doc;
    }
}

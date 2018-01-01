package gait4dogs.backend.data;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class SessionAnalytics {
    private List<AccelerometerOutputAnalytics> accelerometerOutputAnalytics;

    public SessionAnalytics(List<AccelerometerOutputAnalytics> accelerometerOutputAnalytics) {
        this.accelerometerOutputAnalytics = accelerometerOutputAnalytics;
    }

    public List<AccelerometerOutputAnalytics> getAccelerometerOutputAnalytics() {
        return accelerometerOutputAnalytics;
    }

    public Document toDocument() {
        List<Document> accelOutAnalyticsDoc = new ArrayList<>();
        for (AccelerometerOutputAnalytics accelOutAnalytics : accelerometerOutputAnalytics) {
            accelOutAnalyticsDoc.add(accelOutAnalytics.toDocument());
        }
        Document doc = new Document("accelerometerOutputAnalytics", accelOutAnalyticsDoc);

        return doc;
    }
}

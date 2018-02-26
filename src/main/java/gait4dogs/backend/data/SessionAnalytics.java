package gait4dogs.backend.data;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class SessionAnalytics {
    private List<AccelerometerOutputAnalytics> accelerometerOutputAnalytics;
    private double phaseShiftTotalDif;
    private double phaseShiftAvgDif;

    public SessionAnalytics(List<AccelerometerOutputAnalytics> accelerometerOutputAnalytics) {
        this.accelerometerOutputAnalytics = accelerometerOutputAnalytics;
    }

    public SessionAnalytics(List<AccelerometerOutputAnalytics> accelerometerOutputAnalytics, double phaseShiftTotalDif, double phaseShiftAvgDif) {
        this.accelerometerOutputAnalytics = accelerometerOutputAnalytics;
        this.phaseShiftTotalDif = phaseShiftTotalDif;
        this.phaseShiftAvgDif = phaseShiftAvgDif;
    }

    public List<AccelerometerOutputAnalytics> getAccelerometerOutputAnalytics() {
        return accelerometerOutputAnalytics;
    }

    public double getPhaseShiftTotalDif() {
        return phaseShiftTotalDif;
    }

    public void setPhaseShiftTotalDif(double phaseShiftTotalDif) {
        this.phaseShiftTotalDif = phaseShiftTotalDif;
    }

    public double getPhaseShiftAvgDif() {
        return phaseShiftAvgDif;
    }

    public void setPhaseShiftAvgDif(double phaseShiftAvgDif) {
        this.phaseShiftAvgDif = phaseShiftAvgDif;
    }

    public Document toDocument() {
        List<Document> accelOutAnalyticsDoc = new ArrayList<>();
        for (AccelerometerOutputAnalytics accelOutAnalytics : accelerometerOutputAnalytics) {
            accelOutAnalyticsDoc.add(accelOutAnalytics.toDocument());
        }
        Document doc = new Document("accelerometerOutputAnalytics", accelOutAnalyticsDoc)
                .append("phaseShiftTotalDif", phaseShiftTotalDif)
                .append("phaseShiftAvgDif", phaseShiftAvgDif);

        return doc;
    }

    public static SessionAnalytics toSessionAnalytics(Document doc) {
        List<AccelerometerOutputAnalytics> accelerometerOutputAnalytics = new ArrayList<>();
        ArrayList<Document> analytics = (ArrayList<Document>)doc.get("accelerometerOutputAnalytics");
        for (Document accelOutputAnalyticsDoc : analytics) {
            accelerometerOutputAnalytics.add(AccelerometerOutputAnalytics.toAccelerometerOuptutAnalytics(accelOutputAnalyticsDoc));
        }
        double phaseShiftTotalDif = doc.getDouble("phaseShiftTotalDif");
        double phaseShiftAvgDif = doc.getDouble("phaseShiftAvgDif");
        return new SessionAnalytics(accelerometerOutputAnalytics, phaseShiftTotalDif, phaseShiftAvgDif);
    }
}

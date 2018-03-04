package gait4dogs.backend.data;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class AccelerometerOutputAnalytics {
    private double[] minimums;
    private double[] maximums;
    private double[] ranges;
    private List<Double> footStrikeTimes;
    private List<Angle> angles; // List of double arrays where each array contains single pitch and single roll
    private List<double[]> smoothedAcc;
    private List<double[]> shiftedMagnitudes;
    private List<Double> shiftedFootStrikeTimes;
    private String label;

    public AccelerometerOutputAnalytics(double[] minimums, double[] maximums, double[] ranges,
                                        List<Angle> angles) {
        this.minimums = minimums;
        this.maximums = maximums;
        this.ranges = ranges;
        this.angles = angles;
        this.footStrikeTimes = new ArrayList<Double>();
    }

    public AccelerometerOutputAnalytics(double[] minimums, double[] maximums, double[] ranges,
                                        List<Angle> angles, List<Double> footStrikeTimes,
                                        List<double[]> smoothedAcc, String label) {
        this.minimums = minimums;
        this.maximums = maximums;
        this.ranges = ranges;
        this.angles = angles;
        this.footStrikeTimes = footStrikeTimes;
        this.smoothedAcc = smoothedAcc;
        this.label = label;
    }

    public AccelerometerOutputAnalytics(double[] minimums, double[] maximums, double[] ranges,
                                        List<Angle> angles, List<Double> footStrikeTimes,
                                        List<double[]> smoothedAcc, List<double[]> shiftedMagnitudes,
                                        List<Double> shiftedFootStrikeTimes, String label) {
        this.minimums = minimums;
        this.maximums = maximums;
        this.ranges = ranges;
        this.angles = angles;
        this.footStrikeTimes = footStrikeTimes;
        this.smoothedAcc = smoothedAcc;
        this.shiftedMagnitudes = shiftedMagnitudes;
        this.shiftedFootStrikeTimes = shiftedFootStrikeTimes;
        this.label = label;
    }

    public double[] getMinimums() {
        return minimums;
    }

    public double[] getMaximums() {
        return maximums;
    }

    public double[] getRanges() {
        return ranges;
    }

    public List<Angle> getAngles() {
        return angles;
    }

    public List<Double> getFootStrikeTimes() {
        return footStrikeTimes;
    }

    public void setFootStrikeTimes(List<Double> footStrikeTimes) {
        this.footStrikeTimes = footStrikeTimes;
    }

    public List<double[]> getSmoothedAcc() {
        return smoothedAcc;
    }

    public void setSmoothedAcc(List<double[]> smoothedAcc) {
        this.smoothedAcc = smoothedAcc;
    }

    public List<double[]> getShiftedMagnitudes() {
        return shiftedMagnitudes;
    }

    public void setShiftedMagnitudes(List<double[]> shiftedMagnitudes) {
        this.shiftedMagnitudes = shiftedMagnitudes;
    }

    public List<Double> getShiftedFootStrikeTimes() {
        return shiftedFootStrikeTimes;
    }

    public void setShiftedFootStrikeTimes(List<Double> shiftedFootStrikeTimes) {
        this.shiftedFootStrikeTimes = shiftedFootStrikeTimes;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Document toDocument() {
        List<Double> minList = new ArrayList<>();
        List<Double> maxList = new ArrayList<>();
        List<Double> rangeList = new ArrayList<>();
        for (int i = 0; i < minimums.length; i++) {
            minList.add(minimums[i]);
            maxList.add(maximums[i]);
            rangeList.add(ranges[i]);
        }
        List<Document> angleDocs = new ArrayList<>();
        for (Angle angle : angles) {
            angleDocs.add(angle.toDocument());
        }


        List<Double> magnitudes = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        for (int i = 0; i < shiftedMagnitudes.get(0).length; i++) {
            magnitudes.add(shiftedMagnitudes.get(0)[i]);
            times.add(shiftedMagnitudes.get(1)[i]);
        }
        Document shiftedMagnitudesDoc = new Document("magnitudes", magnitudes)
                .append("times", times);

        Document doc = new Document("minimums", minList)
                .append("maximums", maxList)
                .append("ranges", rangeList)
                .append("angles", angleDocs)
                .append("footStrikes", footStrikeTimes)
                .append("shiftedMagnitudes", shiftedMagnitudesDoc)
                .append("shiftedFootStrikes", shiftedFootStrikeTimes)
                .append("label", label);

        return doc;
    }

    public static AccelerometerOutputAnalytics toAccelerometerOuptutAnalytics(Document doc) {
        List<Double> minList = (List<Double>)doc.get("minimums");
        List<Double> maxList = (List<Double>)doc.get("maximums");
        List<Double> rangesList = (List<Double>)doc.get("ranges");
        List<Double> footStrikes = (List<Double>)doc.get("footStrikes");
        List<Double> shiftedFootStrikes = (List<Double>)doc.get("shiftedFootStrikes");
        double[] minimums = new double[minList.size()];
        double[] maximums = new double[maxList.size()];
        double[] ranges = new double[rangesList.size()];
        for (int i = 0; i < minimums.length; i++) {
            minimums[i] = minList.get(i).doubleValue();
            maximums[i] = maxList.get(i).doubleValue();
            ranges[i] = rangesList.get(i).doubleValue();
        }
        List<Angle> angles = (List<Angle>)doc.get("angles");

        List<double[]> shiftedMagnitudes = new ArrayList<>();
        Document shiftedMagnitudesDoc = (Document)doc.get("shiftedMagnitudes");
        List<Double> magnitudesList = (List<Double>)shiftedMagnitudesDoc.get("magnitudes");
        List<Double> timesList = (List<Double>) shiftedMagnitudesDoc.get("times");
        double[] magnitudes = new double[magnitudesList.size()];
        double[] times = new double[timesList.size()];
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = magnitudesList.get(i);
            times[i] = timesList.get(i);
        }
        shiftedMagnitudes.add(magnitudes);
        shiftedMagnitudes.add(times);

        String label = doc.getString("label");

        return new AccelerometerOutputAnalytics(minimums, maximums, ranges,
                angles, footStrikes, null, shiftedMagnitudes, shiftedFootStrikes, label);
    }
}

package gait4dogs.backend.data;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class AccelerometerOutputAnalytics {
    private float[] minimums;
    private float[] maximums;
    private float[] ranges;
    private List<Long> footStrikeTimes;
    private List<Angle> angles; // List of float arrays where each array contains single pitch and single roll
    private List<double[]> smoothedAcc;
    private List<double[]> shiftedMagnitudes;
    private List<Long> shiftedFootStrikeTimes;

    public AccelerometerOutputAnalytics(float[] minimums, float[] maximums, float[] ranges,
                                        List<Angle> angles) {
        this.minimums = minimums;
        this.maximums = maximums;
        this.ranges = ranges;
        this.angles = angles;
        this.footStrikeTimes = new ArrayList<Long>();
    }

    public AccelerometerOutputAnalytics(float[] minimums, float[] maximums, float[] ranges,
                                        List<Angle> angles, List<Long> footStrikeTimes) {
        this.minimums = minimums;
        this.maximums = maximums;
        this.ranges = ranges;
        this.angles = angles;
        this.footStrikeTimes = footStrikeTimes;
    }

    public AccelerometerOutputAnalytics(float[] minimums, float[] maximums, float[] ranges,
                                        List<Angle> angles, List<Long> footStrikeTimes,
                                        List<double[]> smoothedAcc) {
        this.minimums = minimums;
        this.maximums = maximums;
        this.ranges = ranges;
        this.angles = angles;
        this.footStrikeTimes = footStrikeTimes;
        this.smoothedAcc = smoothedAcc;
    }

    public AccelerometerOutputAnalytics(float[] minimums, float[] maximums, float[] ranges,
                                        List<Angle> angles, List<Long> footStrikeTimes,
                                        List<double[]> smoothedAcc, List<double[]> shiftedMagnitudes,
                                        List<Long> shiftedFootStrikeTimes) {
        this.minimums = minimums;
        this.maximums = maximums;
        this.ranges = ranges;
        this.angles = angles;
        this.footStrikeTimes = footStrikeTimes;
        this.smoothedAcc = smoothedAcc;
        this.shiftedMagnitudes = shiftedMagnitudes;
        this.shiftedFootStrikeTimes = shiftedFootStrikeTimes;
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

    public List<Angle> getAngles() {
        return angles;
    }

    public List<Long> getFootStrikeTimes() {
        return footStrikeTimes;
    }

    public void setFootStrikeTimes(List<Long> footStrikeTimes) {
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

    public List<Long> getShiftedFootStrikeTimes() {
        return shiftedFootStrikeTimes;
    }

    public void setShiftedFootStrikeTimes(List<Long> shiftedFootStrikeTimes) {
        this.shiftedFootStrikeTimes = shiftedFootStrikeTimes;
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
                .append("shiftedFootStrikes", shiftedFootStrikeTimes);

        return doc;
    }

    public static AccelerometerOutputAnalytics toAccelerometerOuptutAnalytics(Document doc) {
        List<Double> minList = (List<Double>)doc.get("minimums");
        List<Double> maxList = (List<Double>)doc.get("maximums");
        List<Double> rangesList = (List<Double>)doc.get("ranges");
        List<Long> footStrikes = (List<Long>)doc.get("footStrikes");
        List<Long> shiftedFootStrikes = (List<Long>)doc.get("shiftedFootStrikes");
        float[] minimums = new float[minList.size()];
        float[] maximums = new float[maxList.size()];
        float[] ranges = new float[rangesList.size()];
        for (int i = 0; i < minimums.length; i++) {
            minimums[i] = minList.get(i).floatValue();
            maximums[i] = maxList.get(i).floatValue();
            ranges[i] = rangesList.get(i).floatValue();
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

        return new AccelerometerOutputAnalytics(minimums, maximums, ranges,
                angles, footStrikes, null, shiftedMagnitudes, shiftedFootStrikes);
    }
}

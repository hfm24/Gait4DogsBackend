package gait4dogs.backend.data;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class AccelerometerOutput {
    private long[] epoc;
    private String[] timestamp;
    private float[] elapsed;
    private float[] x;
    private float[] y;
    private float[] z;
    private float[] xAxis;
    private float[] yAxis;
    private float[] zAxis;

    public AccelerometerOutput(long[] epoc, String[] timestamp, float[] elapsed, float[] x, float[] y, float[] z, float[] xAxis, float[] yAxis, float[] zAxis) {
        this.epoc = epoc;
        this.timestamp = timestamp;
        this.elapsed = elapsed;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;
    }

    public long[] getEpoc() {
        return epoc;
    }

    public void setEpoc(long[] epoc) {
        this.epoc = epoc;
    }

    public String[] getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String[] timestamp) {
        this.timestamp = timestamp;
    }

    public float[] getElapsed() {
        return elapsed;
    }

    public void setElapsed(float[] elapsed) {
        this.elapsed = elapsed;
    }

    public float[] getX() {
        return x;
    }

    public void setX(float[] x) {
        this.x = x;
    }

    public float[] getY() {
        return y;
    }

    public void setY(float[] y) {
        this.y = y;
    }

    public float[] getZ() {
        return z;
    }

    public void setZ(float[] z) {
        this.z = z;
    }

    public float[] getxAxis() {
        return xAxis;
    }

    public void setxAxis(float[] xAxis) {
        this.xAxis = xAxis;
    }

    public float[] getyAxis() {
        return yAxis;
    }

    public void setyAxis(float[] yAxis) {
        this.yAxis = yAxis;
    }

    public float[] getzAxis() {
        return zAxis;
    }

    public void setzAxis(float[] zAxis) {
        this.zAxis = zAxis;
    }

    public Document toDocument() {
        List<Long> epocList = new ArrayList<>();
        List<String> timestampList = new ArrayList<>();
        List<Float> elapsedList = new ArrayList<>();
        List<Float> xList = new ArrayList<>();
        List<Float> yList = new ArrayList<>();
        List<Float> zList = new ArrayList<>();
        List<Float> xAxisList = new ArrayList<>();
        List<Float> yAxisList = new ArrayList<>();
        List<Float> zAxisList = new ArrayList<>();
        for (int i = 0; i < epoc.length; i++) {
            epocList.add(epoc[i]);
            timestampList.add(timestamp[i]);
            elapsedList.add(elapsed[i]);
            xList.add(x[i]);
            yList.add(y[i]);
            zList.add(z[i]);
            xAxisList.add(xAxis[i]);
            yAxisList.add(yAxis[i]);
            zAxisList.add(zAxis[i]);
        }
        Document doc = new Document("epoc", epocList)
                .append("timestamp", timestampList)
                .append("elapsed", elapsedList)
                .append("x", xList)
                .append("y", yList)
                .append("z", zList)
                .append("xAxis", xAxisList)
                .append("yAxis", yAxisList)
                .append("zAxis", zAxisList);

        return doc;
    }

    public static AccelerometerOutput toAccelerometerOutput(Document doc) {
        List<Long> epocList = (List<Long>)doc.get("epoc");
        List<String> timeStampList = (List<String>)doc.get("timestamp");
        List<Double> elapsedList = (List<Double>)doc.get("elapsed");
        List<Double> xList = (List<Double>)doc.get("x");
        List<Double> yList = (List<Double>)doc.get("y");
        List<Double> zList = (List<Double>)doc.get("z");
        List<Double> xAxisList = (List<Double>)doc.get("xAxis");
        List<Double> yAxisList = (List<Double>)doc.get("yAxis");
        List<Double> zAxisList = (List<Double>)doc.get("zAxis");

        long[] epoc = new long[epocList.size()];
        String[] timestamp = new String[timeStampList.size()];
        float[] elapsed = new float[elapsedList.size()];
        float[] x = new float[xList.size()];
        float[] y = new float[yList.size()];
        float[] z = new float[zList.size()];
        float[] xAxis = new float[xAxisList.size()];
        float[] yAxis = new float[yAxisList.size()];
        float[] zAxis = new float[zAxisList.size()];

        for (int i = 0; i < epoc.length; i++) {
            epoc[i] = epocList.get(i);
            timestamp[i] = timeStampList.get(i);
            elapsed[i] = elapsedList.get(i).floatValue();
            x[i] = xList.get(i).floatValue();
            y[i] = yList.get(i).floatValue();
            z[i] = zList.get(i).floatValue();
            xAxis[i] = xAxisList.get(i).floatValue();
            yAxis[i] = yAxisList.get(i).floatValue();
            zAxis[i] = zAxisList.get(i).floatValue();
        }
        return new AccelerometerOutput(epoc, timestamp, elapsed, x, y, z, xAxis, yAxis, zAxis);
    }
}

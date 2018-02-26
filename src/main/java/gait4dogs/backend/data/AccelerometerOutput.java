package gait4dogs.backend.data;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class AccelerometerOutput {
    private long[] epoc;
    private String[] timestamp;
    private double[] elapsed;
    private double[] x;
    private double[] y;
    private double[] z;
    private double[] xAxis;
    private double[] yAxis;
    private double[] zAxis;

    public AccelerometerOutput(long[] epoc, String[] timestamp, double[] elapsed, double[] x, double[] y, double[] z, double[] xAxis, double[] yAxis, double[] zAxis) {
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

    public double[] getElapsed() {
        return elapsed;
    }

    public void setElapsed(double[] elapsed) {
        this.elapsed = elapsed;
    }

    public double[] getX() {
        return x;
    }

    public void setX(double[] x) {
        this.x = x;
    }

    public double[] getY() {
        return y;
    }

    public void setY(double[] y) {
        this.y = y;
    }

    public double[] getZ() {
        return z;
    }

    public void setZ(double[] z) {
        this.z = z;
    }

    public double[] getxAxis() {
        return xAxis;
    }

    public void setxAxis(double[] xAxis) {
        this.xAxis = xAxis;
    }

    public double[] getyAxis() {
        return yAxis;
    }

    public void setyAxis(double[] yAxis) {
        this.yAxis = yAxis;
    }

    public double[] getzAxis() {
        return zAxis;
    }

    public void setzAxis(double[] zAxis) {
        this.zAxis = zAxis;
    }

    public Document toDocument() {
        List<Long> epocList = new ArrayList<>();
        List<String> timestampList = new ArrayList<>();
        List<Double> elapsedList = new ArrayList<>();
        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();
        List<Double> zList = new ArrayList<>();
        List<Double> xAxisList = new ArrayList<>();
        List<Double> yAxisList = new ArrayList<>();
        List<Double> zAxisList = new ArrayList<>();
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
        double[] elapsed = new double[elapsedList.size()];
        double[] x = new double[xList.size()];
        double[] y = new double[yList.size()];
        double[] z = new double[zList.size()];
        double[] xAxis = new double[xAxisList.size()];
        double[] yAxis = new double[yAxisList.size()];
        double[] zAxis = new double[zAxisList.size()];

        for (int i = 0; i < epoc.length; i++) {
            epoc[i] = epocList.get(i);
            timestamp[i] = timeStampList.get(i);
            elapsed[i] = elapsedList.get(i).doubleValue();
            x[i] = xList.get(i).doubleValue();
            y[i] = yList.get(i).doubleValue();
            z[i] = zList.get(i).doubleValue();
            xAxis[i] = xAxisList.get(i).doubleValue();
            yAxis[i] = yAxisList.get(i).doubleValue();
            zAxis[i] = zAxisList.get(i).doubleValue();
        }
        return new AccelerometerOutput(epoc, timestamp, elapsed, x, y, z, xAxis, yAxis, zAxis);
    }
}

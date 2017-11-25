package gait4dogs.backend.data;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SessionRawData {
    private long[] epoc;
    private String[] timestamp;
    private float[] elapsed;
    private float[] x;
    private float[] y;
    private float[] z;

    public SessionRawData(long[] epoc, String[] timestamp, float[] elapsed, float[] x, float[] y, float[] z) {
        this.epoc = epoc;
        this.timestamp = timestamp;
        this.elapsed = elapsed;
        this.x = x;
        this.y = y;
        this.z = z;
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

    public Document toDocument() {
        List<Long> epocList = new ArrayList<>();
        List<String> timestampList = new ArrayList<>();
        List<Float> elapsedList = new ArrayList<>();
        List<Float> xList = new ArrayList<>();
        List<Float> yList = new ArrayList<>();
        List<Float> zList = new ArrayList<>();
        for (int i = 0; i < epoc.length; i++) {
            epocList.add(epoc[i]);
            timestampList.add(timestamp[i]);
            elapsedList.add(elapsed[i]);
            xList.add(x[i]);
            yList.add(y[i]);
            zList.add(z[i]);
        }
        Document doc = new Document("epoc", epocList)
                .append("timestamp", timestampList)
                .append("elapsed", elapsedList)
                .append("x", xList)
                .append("y", yList)
                .append("z", zList);

        return doc;
    }
}

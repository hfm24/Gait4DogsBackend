package gait4dogs.backend.data;

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
}

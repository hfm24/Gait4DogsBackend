package gait4dogs.backend.data;

public class SessionAnalytics {
    private long sessionId;
    private long id;
    private float[] minimums;

    public SessionAnalytics(long id, long sessionId, float[] minimums) {
        this.id = id;
        this.sessionId = sessionId;
        this.minimums = minimums;
    }

    public long getSessionId() {
        return sessionId;
    }

    public long getId() {
        return id;
    }

    public float[] getMinimums() {
        return minimums;
    }
}

package gait4dogs.backend.data;

public class SessionAnalytics {
    private long sessionId;
    private long id;

    public SessionAnalytics(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public long getId() {
        return id;
    }
}

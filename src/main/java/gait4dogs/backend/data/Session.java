package gait4dogs.backend.data;

import org.bson.Document;

public class Session {
    private long id;
    private long dogId;
    private SessionRawData rawData;
    private SessionAnalytics sessionAnalytics;
    private String notes;

    public Session(long id, long dogId, SessionRawData rawData, SessionAnalytics sessionAnalytics, String notes) {
        this.id = id;
        this.dogId = dogId;
        this.rawData = rawData;
        this.sessionAnalytics = sessionAnalytics;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public long getDogId() {
        return dogId;
    }

    public SessionRawData getRawData() {
        return rawData;
    }

    public String getNotes() {
        return notes;
    }

    public SessionAnalytics getSessionAnalytics() {
         return sessionAnalytics;
    }

    public Document toDocument() {
        Document doc = new Document("id", id)
                .append("dogId", dogId)
                .append("rawData", rawData.toDocument())
                .append("sessionAnalytics", sessionAnalytics.toDocument())
                .append("notes", notes);
        return doc;
    }
}

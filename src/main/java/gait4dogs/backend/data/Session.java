package gait4dogs.backend.data;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Session {
    private String id;
    private String dogId;
    private SessionRawData rawData;
    private SessionAnalytics sessionAnalytics;
    private String notes;

    public Session(String id, String dogId, SessionRawData rawData, SessionAnalytics sessionAnalytics, String notes) {
        this.id = id;
        this.dogId = dogId;
        this.rawData = rawData;
        this.sessionAnalytics = sessionAnalytics;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String getDogId() {
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

    public static Session toSession(Document doc) {
        String id = doc.getString("id");
        String dogId = doc.getString("dogId");
        String notes = doc.getString("notes");

        Document data = (Document)doc.get("rawData");
        SessionRawData rawData = SessionRawData.toSessionRawData(data);

        Document sesssionAnalytics = (Document)doc.get("sessionAnalytics");
        SessionAnalytics sessionAnalytics = SessionAnalytics.toSessionAnalytics(sesssionAnalytics);

        return new Session(id, dogId, rawData, sessionAnalytics, notes);
    }
}

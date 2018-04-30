package gait4dogs.backend.data;

import org.bson.Document;

public class AngleSession extends Session {
    private String id;
    private String dogId;
    private SessionRawData rawData;
    private SessionAnalytics sessionAnalytics;
    private String notes;
    private String date;
    private String gaitType;

    public AngleSession(String id, String dogId, SessionRawData rawData, SessionAnalytics sessionAnalytics, String notes,
                        String date, String gaitType) {
        this.id = id;
        this.dogId = dogId;
        this.rawData = rawData;
        this.sessionAnalytics = sessionAnalytics;
        this.notes = notes;
        this.date = date;
        this.gaitType = gaitType;
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

    public String getDate() {
        return date;
    }

    public String getGaitType() {
        return gaitType;
    }


    public Document toDocument() {
        Document doc = new Document("id", id)
                .append("dogId", dogId)
                .append("rawData", rawData.toDocument())
                .append("sessionAnalytics", sessionAnalytics.toDocument())
                .append("notes", notes)
                .append("date", date)
                .append("gaitType", gaitType);
        return doc;
    }

    public static AngleSession toAngleSession(Document doc) {
        String id = doc.getString("id");
        String dogId = doc.getString("dogId");
        String notes = doc.getString("notes");
        String date = doc.getString("date");
        String gaitType = doc.getString("gaitType");

        Document data = (Document)doc.get("rawData");
        SessionRawData rawData = SessionRawData.toSessionRawData(data);

        Document sesssionAnalytics = (Document)doc.get("sessionAnalytics");
        SessionAnalytics sessionAnalytics = SessionAnalytics.toSessionAnalytics(sesssionAnalytics);

        return new AngleSession(id, dogId, rawData, sessionAnalytics, notes, date, gaitType);
    }
}

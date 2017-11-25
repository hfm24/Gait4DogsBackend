package gait4dogs.backend.data;

import org.bson.Document;

public class Session {
    private long id;
    private long dogId;
    private SessionRawData rawData;
    private String notes;

    public Session(long id, long dogId, SessionRawData rawData, String notes) {
        this.id = id;
        this.dogId = dogId;
        this.rawData = rawData;
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

    public Document toDocument() {
        Document doc = new Document("id", id)
                .append("dogId", dogId)
                .append("rawData", rawData.toDocument())
                .append("notes", notes);
        return doc;
    }
}

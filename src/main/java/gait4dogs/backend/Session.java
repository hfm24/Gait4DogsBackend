package gait4dogs.backend;

public class Session {
    private long id;
    private long dogId;
    private String rawData;
    private String notes;

    public Session(long id, long dogId, String rawData, String notes) {
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

    public String getRawData() {
        return rawData;
    }

    public String getNotes() {
        return notes;
    }
}

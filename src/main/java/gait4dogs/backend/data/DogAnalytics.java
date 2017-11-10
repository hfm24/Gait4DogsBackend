package gait4dogs.backend.data;

public class DogAnalytics {
    private long dogId;
    private long id;

    public DogAnalytics(long dogId) {
        this.dogId = dogId;
    }

    public long getDogId() {
        return dogId;
    }

    public long getId() {
        return id;
    }
}

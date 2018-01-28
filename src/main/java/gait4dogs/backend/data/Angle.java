package gait4dogs.backend.data;

import org.bson.Document;

public class Angle {
    private float pitch;
    private float roll;
    public Angle(float pitch, float roll) {
        this.pitch = pitch;
        this.roll = roll;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public Document toDocument() {
        return new Document("pitch", pitch)
                .append("roll", roll);
    }
}

package gait4dogs.backend.data;

import org.bson.Document;

public class Angle {
    private double pitch;
    private double roll;
    public Angle(double pitch, double roll) {
        this.pitch = pitch;
        this.roll = roll;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public Document toDocument() {
        return new Document("pitch", pitch)
                .append("roll", roll);
    }
}

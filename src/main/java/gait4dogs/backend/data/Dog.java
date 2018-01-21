package gait4dogs.backend.data;

import org.bson.Document;

import java.util.Date;

public class Dog {
    private String id;
    private String name;
    private float height;
    private float weight;
    private String breed;
    private String birthDate;


    public Dog(String id, String name, float height, float weight, String breed, String birthDate) {
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.breed = breed;
        this.birthDate = birthDate;

        this.id = id;
    }

    public String getName() {
        return name;
    }

    public float getHeight() {
        return height;
    }

    public float getWeight() {
        return weight;
    }

    public String getBreed() {
        return breed;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public Document toDocument() {
        Document doc = new Document("id", id)
                .append("name", name)
                .append("height", height)
                .append("weight", weight)
                .append("breed", breed)
                .append("birthDate", birthDate);
        return doc;
    }
}

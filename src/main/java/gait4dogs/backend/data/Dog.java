package gait4dogs.backend.data;

import java.util.Date;

public class Dog {
    private String name;
    private float height;
    private float weight;
    private String breed;
    private String birthDate;
    private long id;

    public Dog() {

    }

    public Dog(String name, float height, float weight, String breed, String birthDate) {
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.breed = breed;
        this.birthDate = birthDate;

        this.id = 0;
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

    public long getId() {
        return id;
    }
}

package com.example.mypet;

public class Pet {
    private String petId;
    private String name;
    private String gender;
    private String age;
    private String breed;
    private String imageUrl;

    public Pet() {
        // Default constructor required for Firebase
    }

    public Pet(String petId, String name, String gender, String age, String breed, String imageUrl) {
        this.petId = petId;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.breed = breed;
        this.imageUrl = imageUrl;
    }

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

package com.example.doggysitter;

import androidx.annotation.NonNull;

public class TestFire {
    private String name;
    private int age;
    private String breed;
    private String ownerId;
    private Object created_at;
    public TestFire(){

    }
    public TestFire(String ownerId,String name){
        this.name=name;
        this.ownerId=ownerId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Object getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Object created_at) {
        this.created_at = created_at;
    }

    @NonNull
    @Override
    public String toString() {
        return "TestFire{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", breed='" + breed + '\'' +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }
}

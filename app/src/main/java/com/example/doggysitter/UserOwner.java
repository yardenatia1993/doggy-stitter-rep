package com.example.doggysitter;

public class UserOwner extends User {

    private Dog dog;

    public UserOwner() {

    }
    public UserOwner(String id, String email) {
        super(id, email, "owner");
    }



    public Dog getDog() {
        return dog;
    }

    public void setDog(Dog dog) {
        this.dog = dog;
    }

}

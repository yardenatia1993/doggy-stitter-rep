package com.example.doggysitter;

public class UserWalker extends User {

    private boolean isAvailable;
    private int rating, counter, sum;
    private int price;


    public UserWalker() {
    }
    public UserWalker(String id, String email) {
        super(id, email, "walker");

    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }


    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {

        counter++;
        sum += rating;
        this.rating = sum / counter;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}

package com.theironyard;

/**
 * Created by stevenburris on 10/4/16.
 */
public class Hurricane {

    int id;
    String name;
    String location;
    String image;
    int category;
    User user;

    public Hurricane(int id, String name, String location, String image, int category) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.image = image;
        this.category = category;
    }

    @Override
    public String toString() {
        return id + ". Hurricane: " + name + ", located near " + location + ". It is a category: " + category + " storm." + image;
    }
}

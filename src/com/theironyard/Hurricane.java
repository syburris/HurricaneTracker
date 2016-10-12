package com.theironyard;

/**
 * Created by stevenburris on 10/4/16.
 */
public class Hurricane {


    String name;
    String location;
    String image;
    int category;
    User user;

    public Hurricane(String name, String location, String image, int category, User user) {
        this.name = name;
        this.location = location;
        this.image = image;
        this.category = category;
        this.user = user;
    }
}

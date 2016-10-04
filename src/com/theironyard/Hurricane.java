package com.theironyard;

/**
 * Created by stevenburris on 10/4/16.
 */
public class Hurricane {
    enum Category {
        ONE, TWO, THREE, FOUR, FIVE
    }

    String name;
    String location;
    String image;
    Category category;

    public Hurricane(String name, String location, String image, Category category) {
        this.name = name;
        this.location = location;
        this.image = image;
        this.category = category;
    }
}

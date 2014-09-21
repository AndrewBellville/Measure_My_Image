package com.example.andrew.measuremyimage.DataBase;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by Andrew on 9/21/2014.
 */
public class ImageSchema {

    // Log cat tag
    private static final String LOG = "ImageSchema";

    int id;
    Bitmap Image;
    String UserName;
    String CreatedOn;

    // constructors
    public ImageSchema() {
    }

    public ImageSchema(Bitmap aImage, String aUserName) {

        Log.e(LOG, "Creating New Image Schema for UserName[" + aUserName + "]");
        this.Image = aImage;
        this.UserName = aUserName;
    }

    // setters
    public void setId(int aId) {
        this.id = aId;
    }

    public void setUserName(String aUserName) {
        this.UserName = aUserName;
    }

    public void setImage(Bitmap aImage) {
        this.Image = aImage;
    }

    public void setCreatedOn(String aCreatedOn){
        this.CreatedOn = aCreatedOn;
    }

    // getters
    public int getId() {
        return this.id;
    }

    public String getUserName() {
        return this.UserName;
    }

    public Bitmap getImage() {
        return this.Image;
    }

    public String getCreatedOn() {
        return this.CreatedOn;
    }


}

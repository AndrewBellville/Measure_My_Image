package com.example.andrew.measuremyimage.DataBase;

import android.util.Log;

/**
 * Created by Andrew on 11/23/2014.
 */
public class ReferenceObjectSchema {

    // Log cat tag
    private static final String LOG = "ReferenceObjectSchema";

    int id;
    String objectName;
    int height;
    int width;
    String  unitOfMeasure;
    String CreatedOn;
    String userName;

    // constructors
    public ReferenceObjectSchema() {
    }

    public ReferenceObjectSchema(String aObjectName, int aHeight,int aWidth,String aUnitOfMeasure, String aUserName) {

        Log.e(LOG, "Creating New Reference Object Schema ObjectName[" + aObjectName + "], UserName[" + aUserName + "]");
        this.setObjectName(aObjectName);
        this.setHeight(aHeight);
        this.setWidth(aWidth);
        this.setUnitOfMeasure(aUnitOfMeasure);
        this.setUserName(aUserName);
    }

    public String getCreatedOn() {
        return CreatedOn;
    }

    public void setCreatedOn(String createdOn) {
        CreatedOn = createdOn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

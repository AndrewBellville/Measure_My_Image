package com.example.andrew.measuremyimage.DataBase;

/**
 * Created by GaryandMichelleandki on 12/5/2014.
 */

import android.util.Log;

public class PreferenceSchema {

    // Log cat tag
    private static final String LOG = "PreferenceSchema";

    String UserName;
    double CameraHeight;
    String UnitOfMeasure;
    String CreatedOn;

    // constructors
    public PreferenceSchema() {
    }

    public PreferenceSchema(String aUserName, double aCameraHeight, String aUnitOfMeasure) {

        Log.e(LOG, "Creating New Preference Schema UserName[" + aUserName + "], Camera Height[" + aCameraHeight +
                "], Unit Of Measure[" + aUnitOfMeasure + "]");
        this.UserName = aUserName;
        this.CameraHeight = aCameraHeight;
        this.UnitOfMeasure = aUnitOfMeasure;
    }

    // setters
    public void setUserName(String aUserName) { this.UserName = aUserName; }
    public void setCameraHeight(double aCameraHeight){ this.CameraHeight = aCameraHeight; }
    public void setUnitOfMeasure (String aUnitOfMeasure){ this.UnitOfMeasure  = aUnitOfMeasure; }
    public void setCreatedOn(String aCreatedOn){
        this.CreatedOn = aCreatedOn;
    }

    // getters
    public String getUserName() {
        return this.UserName;
    }
    public double getCameraHeight() { return CameraHeight; }
    public String getUnitOfMeasure() { return UnitOfMeasure; }
    public String getCreatedOn() { return CreatedOn; }
}

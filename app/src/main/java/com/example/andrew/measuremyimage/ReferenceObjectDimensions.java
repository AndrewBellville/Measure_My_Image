package com.example.andrew.measuremyimage;

import android.graphics.Point;
import android.util.Log;

import com.example.andrew.measuremyimage.DataBase.ReferenceObjectSchema;

/**
 * Created by Andrew on 12/4/2014.
 */
public class ReferenceObjectDimensions {

    public class FloatPoint
    {
        float[] point;

        public FloatPoint(float x, float y)
        {
            point=new float[]{x,y};
        }
        public FloatPoint (float[] aFloat)
        {
            point=aFloat;
        }

        public float getX()
        {
            return point[0];
        }

        public float getY()
        {
            return point[1];
        }
    }


    // Log cat tag
    private static final String LOG = "ReferenceObjectDimensions";

    FloatPoint rightEdge;
    FloatPoint leftEdge;
    FloatPoint topEdge;
    FloatPoint bottomEdge;
    boolean isLeftToRight = true;


    public ReferenceObjectDimensions()
    {

    }

    public float FindDistance(float[] aTp1,float[] aTp2,ReferenceObjectSchema aRefereceObject )
    {
        Log.e(LOG, "Entering: FindDistance");
        float diff = FindPointDifference(aTp1,aTp2);
        float pixelsPerMeasure = 0;

        int measure = FindMeasurementToUse(aRefereceObject);

        if(isLeftToRight){
            Log.e(LOG, "Entering: isLeftToRight");
            pixelsPerMeasure = getLeftToRightPixelsPerMeasure(measure);
        }
        else {
            pixelsPerMeasure = getUpAndDownPixelsPerMeasure(measure);
        }
        Log.e(LOG, Float.toString(diff));
        Log.e(LOG, Float.toString(pixelsPerMeasure));
        diff = diff/pixelsPerMeasure;

        return diff;
    }

    private int FindMeasurementToUse(ReferenceObjectSchema aRefereceObject)
    {
        int height = aRefereceObject.getHeight();
        int width = aRefereceObject.getWidth();
        int measure = 0;
        boolean  isHeightGreater = false;
        boolean isLeftToRightGreater = false;

        float diff = FindFloatDifference(leftEdge.getX(),rightEdge.getX());
        float diff1 = FindFloatDifference(topEdge.getY(),bottomEdge.getY());

        if(height > width) isHeightGreater = true;
        if(diff > diff1) isLeftToRightGreater = true;

        if(isLeftToRight){
            if(isLeftToRightGreater && isHeightGreater) {
                measure = height;
            }
            else if(isLeftToRightGreater && !isHeightGreater) {
                measure = width;
            }
            else if(!isLeftToRightGreater && isHeightGreater)
            {
                measure = width;
            }
            else if(!isLeftToRightGreater && !isHeightGreater)
            {
                measure = height;
            }
        }
        else
        {
            if(isLeftToRightGreater && isHeightGreater) {
                measure = width;
            }
            else if(isLeftToRightGreater && !isHeightGreater) {
                measure = height;
            }
            else if(!isLeftToRightGreater && isHeightGreater)
            {
                measure = height;
            }
            else if(!isLeftToRightGreater && !isHeightGreater)
            {
                measure = width;
            }
        }
        Log.e(LOG, Integer.toString(measure));
        return measure;
    }

    public float getLeftToRightPixelsPerMeasure(int aMeasure)
    {
        return FindFloatDifference(rightEdge.getX(),leftEdge.getX())/aMeasure;
    }

    public float getUpAndDownPixelsPerMeasure(int aMeasure)
    {
        return FindFloatDifference(topEdge.getY(), bottomEdge.getY()) / aMeasure;
    }

    private float FindPointDifference(float[] aTp1,float[] aTp2)
    {
        float xComp=FindFloatDifference(aTp1[0],aTp2[0]);
        float yComp=FindFloatDifference(aTp1[1],aTp2[1]);

        if (xComp > yComp){
            isLeftToRight = true;
            return xComp;
        }
        else{
            isLeftToRight = false;
            return yComp;
        }
    }

    private float FindFloatDifference(float aFloat1, float aFloat2)
    {
        float diff=Math.abs(aFloat1-aFloat2);
        return diff;
    }

    public void setRightEdge(float aX,float aY) {
        this.rightEdge = new FloatPoint(aX,aY);
    }
    public void setRightEdge(float[] aFloat) {
        this.rightEdge = new FloatPoint(aFloat);
    }

    public void setLeftEdge(float aX,float aY) {
        this.leftEdge = new FloatPoint(aX,aY);
    }
    public void setLeftEdge(float[] aFloat) {
        this.leftEdge = new FloatPoint(aFloat);
    }

    public void setTopEdge(float aX,float aY) {
        this.topEdge = new FloatPoint(aX,aY);
    }
    public void setTopEdge(float[] aFloat) {
        this.topEdge = new FloatPoint(aFloat);
    }

    public void setBottomEdge(float aX,float aY) {
        this.bottomEdge = new FloatPoint(aX,aY);
    }
    public void setBottomEdge(float[] aFloat) {
        this.bottomEdge = new FloatPoint(aFloat);
    }

}

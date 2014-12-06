package com.example.andrew.measuremyimage;

import android.view.SurfaceView;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Color;
/**
 * Created by GaryandMichelleandki on 11/30/2014.
 */
public class CameraPreviewView extends SurfaceView {

    protected final Paint rectanglePaint = new Paint();
    // Log cat tag
    private static final String LOG = "CameraPreviewView";
    public CameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rectanglePaint.setARGB(255, 200, 0, 0);
        rectanglePaint.setStyle(Paint.Style.FILL);
        rectanglePaint.setStrokeWidth(2);
    }

    @Override
    protected void onDraw(Canvas canvas){
        int height = canvas.getHeight();
        int width = canvas.getWidth();

        Paint p = new Paint();
        p.setColor(Color.GREEN);
        p.setStrokeWidth(2);

        canvas.drawLine(0, height/2,width, height/2, p);
        canvas.drawLine(width/2, 0, width/2, height, p);
        invalidate();

        Log.w(LOG, "On Draw Called");
    }
}
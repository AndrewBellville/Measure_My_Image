package com.example.andrew.measuremyimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.andrew.measuremyimage.DataBase.DataBaseManager;
import com.example.andrew.measuremyimage.DataBase.ImageSchema;


public class ImageDisplay extends ActionBarActivity {

    // Log cat tag
    private static final String LOG = "ImageDisplay";

    private DataBaseManager dbManager;
    Integer imageRowId;
    ImageProcessing imageProcessor;

    float lastEventX = 0;
    float lastEventY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);
        Log.e(LOG, "Entering: onCreate");

        //TODO pass the ImageSchemaView not just row id so we do not need to look at the db
        //get image to display
        Intent intent = getIntent();
        imageRowId = intent.getIntExtra(UserImages.EXTRA_MESSAGE, 0);
        dbManager = DataBaseManager.getInstance(getApplicationContext());

        Log.e(LOG, imageRowId.toString());

        loadImage();
    }

    private void loadImage()
    {
        Log.e(LOG, "Entering: loadImage");

        ImageSchema image = dbManager.getImageById(imageRowId);
        ImageView view = (ImageView)findViewById(R.id.imageView);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;

        Log.e(LOG, Integer.toString(height) + "  " + Integer.toString(width));

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image.getImage(),width, height, true);
        view.setImageBitmap(scaledBitmap);
        view.setOnTouchListener(imgSourceOnTouchListener);
        view.invalidate();
    }


    View.OnTouchListener imgSourceOnTouchListener
            = new View.OnTouchListener(){

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        Log.e(LOG, "Entering: onTouch");

        float eventX = event.getX();
        float eventY = event.getY();

        if(eventX == lastEventX || eventY == lastEventY) return true;
        lastEventX = eventX;
        lastEventY = eventY;

        float[] eventXY = new float[] {eventX, eventY};

        Matrix invertMatrix = new Matrix();
        ((ImageView)view).getImageMatrix().invert(invertMatrix);

        invertMatrix.mapPoints(eventXY);
        int x = Integer.valueOf((int)eventXY[0]);
        int y = Integer.valueOf((int)eventXY[1]);

        Log.e(LOG,
                "touched position: "
                        + String.valueOf(eventX) + " / "
                        + String.valueOf(eventY));
        Log.e(LOG,
                "touched position: "
                        + String.valueOf(x) + " / "
                        + String.valueOf(y));

        Drawable imgDrawable = ((ImageView)view).getDrawable();
        Bitmap bitmap = ((BitmapDrawable)imgDrawable).getBitmap();
        imageProcessor = new ImageProcessing(bitmap);

        //Limit x, y range within bitmap
        if(x < 0){
            x = 0;
        }else if(x > bitmap.getWidth()-1){
            x = bitmap.getWidth()-1;
        }

        if(y < 0){
            y = 0;
        }else if(y > bitmap.getHeight()-1){
            y = bitmap.getHeight()-1;
        }

        //bitmap.setPixels(imageProcessor.ProcessPixels(x, y), 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        bitmap.setPixels(imageProcessor.GenericEdgeDetection(bitmap), 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        view.invalidate();

        //Log.e(LOG,"touched color: " + "#" + Integer.toHexString(touchedRGB));
        return true;
    }};



}

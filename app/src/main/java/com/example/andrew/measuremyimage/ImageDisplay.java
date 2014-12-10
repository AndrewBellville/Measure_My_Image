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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andrew.measuremyimage.DataBase.DataBaseManager;
import com.example.andrew.measuremyimage.DataBase.ImageSchema;
import com.example.andrew.measuremyimage.DataBase.ReferenceObjectSchema;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ImageDisplay extends ActionBarActivity {

    // Log cat tag
    private static final String LOG = "ImageDisplay";

    private DataBaseManager dbManager;
    Integer imageRowId;
    ImageProcessing imageProcessor;
    List<ReferenceObjectSchema> ReferenceObjectArray;
    private int spinnerPos = -1;
    private Spinner spinner;
    private String userName;
    private UserLoggedIn userLoggedIn;
    TextView distance;
    TextView objectFound;
    TextView firstPoint;
    TextView secondPoint;
    boolean isRefOb = false;
    ReferenceObjectDimensions objectDimensions;

    float[] tp1;
    float[] tp2;

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
        userLoggedIn = UserLoggedIn.getInstance();
        userName = userLoggedIn.getUser().getUserName();
        distance = (TextView)findViewById(R.id.Distance);
        objectFound = (TextView)findViewById(R.id.ObjectTextView);
        firstPoint = (TextView)findViewById(R.id.FPointTextView);
        secondPoint = (TextView)findViewById(R.id.SPointTextView);
        spinner = (Spinner)findViewById(R.id.ReferenceObjectSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                spinnerPos = pos-1;//sub one to account of 'None'
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SetReferenceObjectArray());
        // Specify the layout to use when the list of choices appears
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerArrayAdapter);

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
        Log.e(LOG, image.getImage().getHeight() + "  " + image.getImage().getWidth());

        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(image.getImage(),image.getImage().getWidth(), image.getImage().getHeight(), true);
        view.setImageBitmap(image.getImage());
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
        imageProcessor = new ImageProcessing(bitmap);
        SaveTouchPoints(x,y);

        return true;
    }};

    private void FindReferenceObject(View aView, float aEventX, float aEventY)
    {
        Log.e(LOG, "Entering: FindReferenceObject");

       // imageProcessor = new ImageProcessing(bitmap);
       // bitmap.setPixels(imageProcessor.ProcessPixels(x, y), 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        //bitmap.setPixels(imageProcessor.GenericEdgeDetection(bitmap), 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
       // aView.invalidate();
        //Log.e(LOG,"touched color: " + "#" + Integer.toHexString(touchedRGB));
    }

    private void SaveTouchPoints(int aX,int aY)
    {
        Log.e(LOG, "Entering: SaveTouchPoints");
        if(!isRefOb) {
            objectDimensions = imageProcessor.FindObjectDimensions(aX,aY);
            isRefOb = true;
        }
        else if(tp1 == null){
            tp1 = new float[]{aX,aY};
            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    "X: "+tp1[0]+" Y: "+tp1[1],
                    Toast.LENGTH_SHORT);
            toast.show();

        }
        else if(tp2 == null){
            tp2 = new float[]{aX,aY};
            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    "X: "+tp2[0]+" Y: "+tp2[1],
                    Toast.LENGTH_SHORT);
            toast.show();
        }

        UpdateLabels();
    }

    private void UpdateLabels()
    {
        Log.e(LOG, "Entering: UpdateLabels");

        String red = "#ffff1800";
        String green = "#00ff00";

        if(isRefOb) objectFound.setTextColor(Color.parseColor(green));
        else objectFound.setTextColor(Color.parseColor(red));

        if(!(tp1 == null)) firstPoint.setTextColor(Color.parseColor(green));
        else firstPoint.setTextColor(Color.parseColor(red));

        if(!(tp2 == null)) secondPoint.setTextColor(Color.parseColor(green));
        else secondPoint.setTextColor(Color.parseColor(red));
    }

    private ArrayList<String> SetReferenceObjectArray()
    {
        Log.e(LOG, "Entering: SetReferenceObjectArray");
        ReferenceObjectArray = dbManager.getAllReferenceObjectsForUser(userName);

        ArrayList<String> ObjectNameArray = new ArrayList<String>();

        //Add default None and all user object names to list
        ObjectNameArray.add("None");
        for (ReferenceObjectSchema r : ReferenceObjectArray)
        {
            ObjectNameArray.add(r.getObjectName());
        }

        return ObjectNameArray;
    }

    public void onFindClick(View aView)
    {
        Log.e(LOG, "Entering: onFindClick");

        if(spinnerPos ==  -1){
            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    "No Object Attached",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        else if(!isRefOb){
            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    "Object Attached Not found",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        else if(tp1 == null){
            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    "First Touch Point Not Found",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        else if (tp2 == null){
            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    "Second Touch Point Not Found",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        float diff2  = objectDimensions.FindDistance(tp1, tp2, ReferenceObjectArray.get(spinnerPos));
        distance.setText(Float.toString(diff2) + " " + ReferenceObjectArray.get(spinnerPos).getUnitOfMeasure());
    }

    public void onResetClick(View aView)
    {
        Log.e(LOG, "Entering: onResetClick");
        isRefOb = false;
        tp1 = null;
        tp2 = null;

        UpdateLabels();
    }
}

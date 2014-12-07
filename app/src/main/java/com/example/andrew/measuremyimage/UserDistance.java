package com.example.andrew.measuremyimage;

import java.util.List;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.widget.Toast;

import com.example.andrew.measuremyimage.DataBase.DataBaseManager;
import com.example.andrew.measuremyimage.DataBase.PreferenceSchema;

public class UserDistance extends Activity implements SensorEventListener
{
    private SensorManager sensorManager;
    private Sensor mOrientation;
    private Sensor mAccelerometer;
    private DataBaseManager dbManager;
    private UserLoggedIn userLoggedIn;
    private String userName;
    private PreferenceSchema userPreference;

    private View myRelativeLayout;
    private TextView textMessage;
    private TextView textCameraHeightPreference;
    private Spinner spinnerCameraHeightUOM;
    private int spinnerPos = 0;
    private Button buttonStartDistanceCamera;
    private Button buttonChangeUserPreference;

    // Log cat tag
    private static final String LOG = "UserDistance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_distance);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        userLoggedIn = UserLoggedIn.getInstance();
        userName = userLoggedIn.getUser().getUserName();
        if (dbManager==null)
            dbManager = DataBaseManager.getInstance(getApplicationContext());

        if ( dbManager.DoesPreferenceExist(userName) )
        {
            userPreference = dbManager.getPreference(userName);
        }
        else
        {
            userPreference = new PreferenceSchema(userName, 64, "in.");
            spinnerPos=1;
            if (! dbManager.CreateAPreference(userPreference))
            {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        "Error: Insert of default Camera Height Preference did not save to Database!",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.LEFT,0,0);
                toast.show();
            }
        }

// Find views
        myRelativeLayout = findViewById(R.id.my_id);
        textMessage = (TextView) findViewById(R.id.list_sensors);
        textCameraHeightPreference = (TextView) findViewById(R.id.text_Height);
        spinnerCameraHeightUOM = (Spinner)findViewById(R.id.spinner_UOM);
        buttonStartDistanceCamera = (Button) findViewById(R.id.button_start_distance_camera );
        buttonChangeUserPreference = (Button) findViewById(R.id.button_ChangeHeight );

// Get sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

// Test if orientation sensor exist on the device
        boolean orientok = sensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_UI);
        if (!orientok){
            textMessage.setText("No orientation sensor\n");
            buttonStartDistanceCamera.setEnabled(false);
        }
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));

// Test if accelerometer sensor exist on the device
        boolean accelerometerok = sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (!accelerometerok){
            textMessage.append("No accelerometer sensor\n");
            buttonStartDistanceCamera.setEnabled(false);
        }
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

        if (orientok && accelerometerok)
        {
            buttonStartDistanceCamera.setOnClickListener(new Button.OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    startDistanceCamera(arg0);
                }});

            buttonChangeUserPreference.setOnClickListener(new Button.OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    modifyUserPreference(arg0);
                }
            });

            spinnerCameraHeightUOM.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                    spinnerPos = pos;
                    ((TextView) adapterView.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.Units_of_Measure, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinnerCameraHeightUOM.setAdapter(adapter);

            textCameraHeightPreference.setText(Double.toString(userPreference.getCameraHeight()));
            if (!userPreference.getUnitOfMeasure().equals(null)) {
                spinnerPos = adapter.getPosition(userPreference.getUnitOfMeasure());
                spinnerCameraHeightUOM.setSelection(spinnerPos);
                spinnerPos = 0;
            }
            String messagePreferenceInfo = String.format("CURRENT USER PREFERENCE :\n\tHeight[%.1f] \n\tUnit of Measure[%s]"
                    ,userPreference.getCameraHeight(), userPreference.getUnitOfMeasure());
            textMessage.setText(messagePreferenceInfo);

        }
        else
        {
            textCameraHeightPreference.setEnabled(false);
            spinnerCameraHeightUOM.setEnabled(false);
            buttonStartDistanceCamera.setEnabled(false);
            buttonChangeUserPreference.setEnabled(false);
        }

        myRelativeLayout.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbManager == null)
            dbManager = DataBaseManager.getInstance(getApplicationContext());

        if ( dbManager.DoesPreferenceExist(userName) )
        {
            userPreference = dbManager.getPreference(userName);
        }
        else
        {
            if (userPreference == null)
                userPreference = new PreferenceSchema(userName, 64, "in.");

            if (! dbManager.CreateAPreference(userPreference))
            {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        "Error: Insert of default Camera Height Preference did not save to Database!",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.LEFT,0,0);
                toast.show();
            }
        }
        textCameraHeightPreference.setText(Double.toString(userPreference.getCameraHeight()));
        if (!userPreference.getUnitOfMeasure().equals(null)) {
            ArrayAdapter<CharSequence> myAdapter = (ArrayAdapter<CharSequence>)spinnerCameraHeightUOM.getAdapter();
            spinnerPos = myAdapter.getPosition(userPreference.getUnitOfMeasure());
            spinnerCameraHeightUOM.setSelection(spinnerPos);
            spinnerPos = 0;
        }
        String messagePreferenceInfo = String.format("CURRENT USER PREFERENCE :\n\tHeight[%.1f] \n\tUnit of Measure[%s]"
                ,userPreference.getCameraHeight(), userPreference.getUnitOfMeasure());
        textMessage.setText(messagePreferenceInfo);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
    }
     @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
    }

   public void onAccuracyChanged(Sensor sensor, int accuracy) {
   }

    // Called when sensor changes
    public void onSensorChanged(SensorEvent event) {
    }

    /** Called when the user clicks the Start button */
    public void startDistanceCamera(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, DistanceCamera.class);
        startActivity(intent);
    }
    /** Called when the user clicks the Change Height and Unit of Measure button */
    public void modifyUserPreference(View view) {
        if ( Double.parseDouble(textCameraHeightPreference.getText().toString())> 0)
            userPreference.setCameraHeight(Double.parseDouble(textCameraHeightPreference.getText().toString()));

        if ( spinnerPos > 0)
            userPreference.setUnitOfMeasure(spinnerCameraHeightUOM.getItemAtPosition(spinnerPos).toString());

        if ( dbManager.DoesPreferenceExist(userName) )
        {
            if (! dbManager.modifyPreference(userPreference))
            {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        "Error: Update to Camera Height Preference did not save to Database!",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.LEFT,0,0);
                toast.show();
            }
        }
        else
        {
            if (! dbManager.CreateAPreference(userPreference))
            {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        "Error: Insert of Camera Height Preference did not save to Database!",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.LEFT,0,0);
                toast.show();
            }
        }

        String messagePreferenceInfo = String.format("CURRENT USER PREFERENCE :\n\tHeight[%.1f] \n\tUnit of Measure[%s]"
                ,userPreference.getCameraHeight(), userPreference.getUnitOfMeasure());
        textMessage.setText(messagePreferenceInfo);
    }
}
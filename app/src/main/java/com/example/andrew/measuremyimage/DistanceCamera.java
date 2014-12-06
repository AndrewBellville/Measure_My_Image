package com.example.andrew.measuremyimage;

/**
 * Created by GaryandMichelleandki on 11/20/2014.
 */
        import java.io.IOException;
        import java.util.List;

        import android.app.Activity;
        import android.graphics.Color;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.content.pm.ActivityInfo;
        import android.graphics.PixelFormat;
        import android.hardware.Camera;
        import android.hardware.Camera.AutoFocusCallback;
        import android.hardware.Camera.PictureCallback;
        import android.hardware.Camera.ShutterCallback;
        import android.os.Bundle;
        import android.view.Gravity;
        import android.view.LayoutInflater;
        import android.view.SurfaceHolder;
        import android.view.View;
        import android.view.ViewGroup.LayoutParams;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.LinearLayout;
        import android.widget.Spinner;
        import android.widget.Toast;
        import android.widget.TextView;
        import android.content.Context;
        import com.example.andrew.measuremyimage.DataBase.DataBaseManager;
        import com.example.andrew.measuremyimage.DataBase.PreferenceSchema;

public class DistanceCamera extends Activity implements SensorEventListener, SurfaceHolder.Callback
{
    TextView textOrientation;
    SensorManager orientationManager;
    Sensor orientation;
    float[] lastVectorOrientation = new float[3];
    float[] firstVectorOrientation = new float[3];
    float[] secondVectorOrientation = new float[3];

    double firstVectorDistanceY=0.0;
    double secondVectorDistanceY=0.0;
    double lastDistanceCalculated=0.0;
    double lastAzimuthDeviation=0.0;
    double firstVectorHeight=0.0;
    double secondVectorHeight=0.0;
    String distanceStatus="Get First Point";
    private static final int AZIMUTH = 0;
    private static final int PITCH = 1;
    private static final int ROLL = 2;
    // Log cat tag
    private static final String LOG = "DistanceCamera";

    Camera camera;
    CameraPreviewView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater distanceCameraLayoutInflater = null;
    Button buttonGetPoint;
    Button buttonChangeHeight;
    TextView textHeight;
    private DataBaseManager dbManager;
    private UserLoggedIn userLoggedIn;
    private String userName;
    private Spinner spinner;
    private int spinnerPos = 0;
    private PreferenceSchema userPreference;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        userLoggedIn = UserLoggedIn.getInstance();
        userName = userLoggedIn.getUser().getUserName();
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

        distanceStatus="Get First Point";
        firstVectorOrientation[AZIMUTH] = 0;
        firstVectorOrientation[PITCH] = 0;
        firstVectorOrientation[ROLL] = 0;
        firstVectorDistanceY = 0;
        secondVectorOrientation[AZIMUTH] = 0;
        secondVectorOrientation[PITCH] = 0;
        secondVectorOrientation[ROLL] = 0;
        secondVectorDistanceY=0;
        lastVectorOrientation[AZIMUTH] = 0;
        lastVectorOrientation[PITCH] = 0;
        lastVectorOrientation[ROLL] = 0;
        lastDistanceCalculated =0;
        lastAzimuthDeviation= 0;

        orientationManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        orientation = orientationManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (CameraPreviewView)findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        distanceCameraLayoutInflater = LayoutInflater.from(getBaseContext());
        View viewDistanceCameraControl = distanceCameraLayoutInflater.inflate(R.layout.activity_distance_camera_control, null);
        LayoutParams layoutParamsControl
                = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);
        this.addContentView(viewDistanceCameraControl, layoutParamsControl);
        textHeight = (TextView)findViewById(R.id.editHeight);
        textHeight.setTextColor(Color.GREEN);
        spinner = (Spinner)findViewById(R.id.UnitOfMeasureSpinner);
        buttonChangeHeight = (Button)findViewById(R.id.changeheight);
        buttonChangeHeight.setText("View Height");
        buttonChangeHeight.setTextColor(Color.GREEN);
        buttonGetPoint = (Button)findViewById(R.id.getpoint);
        buttonGetPoint.setText(distanceStatus);
        buttonGetPoint.setTextColor(Color.GREEN);
        buttonGetPoint.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                getDistance(arg0);
            }});

        buttonChangeHeight.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                if (buttonChangeHeight.getText().toString().equalsIgnoreCase("Change Height") )
                {
                    if ( Double.parseDouble(textHeight.getText().toString())> 0)
                        userPreference.setCameraHeight(Double.parseDouble(textHeight.getText().toString()));

                    if ( spinnerPos > 0)
                        userPreference.setUnitOfMeasure(spinner.getItemAtPosition(spinnerPos).toString());

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

                    buttonChangeHeight.setText("View Height");
                    textHeight.setEnabled(false);
                    textHeight.setVisibility(View.INVISIBLE );
                    spinner.setEnabled(false);
                    spinner.setVisibility(View.INVISIBLE );
                }
                else
                {
                    buttonChangeHeight.setText("Change Height");
                    textHeight.setEnabled(true);
                    textHeight.setText(String.valueOf(Double.toString(userPreference.getCameraHeight())));
                    textHeight.setVisibility(View.VISIBLE );
                    spinner.setEnabled(true);
                    spinner.setSelection(spinnerPos);
                    spinner.setVisibility(View.VISIBLE );
                }
            }
        });

        textOrientation = (TextView)findViewById(R.id.textListOrientation);
        textOrientation.setTextColor(Color.GREEN);

        LinearLayout layoutBackground = (LinearLayout)findViewById(R.id.background);
        layoutBackground.setOnClickListener(new LinearLayout.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                buttonGetPoint.setEnabled(false);
                buttonChangeHeight.setEnabled(false);
                camera.autoFocus(myAutoFocusCallback);
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                spinnerPos = pos;
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
        spinner.setAdapter(adapter);

    }

    /** Called when the user clicks the Send button */
    public void getDistance(View view) {
        if (distanceStatus.equalsIgnoreCase("Get First Point"))
        {
            firstVectorOrientation[AZIMUTH] = lastVectorOrientation[AZIMUTH];
            firstVectorOrientation[PITCH] = lastVectorOrientation[PITCH];
            firstVectorOrientation[ROLL] = lastVectorOrientation[ROLL];
            firstVectorHeight = userPreference.getCameraHeight();
            firstVectorDistanceY = (Math.tan(Math.toRadians(Math.abs(firstVectorOrientation[PITCH]))) * firstVectorHeight );
            secondVectorOrientation[AZIMUTH] = 0;
            secondVectorOrientation[PITCH] = 0;
            secondVectorOrientation[ROLL] = 0;
            secondVectorDistanceY=0;
            secondVectorHeight=0;

            String toastMsg = String.format("First Point: \n\t Height = %.2f inches,"
                    + "\n\t Azimuth Angle = %.2f degrees,"
                    + "\n\t Pitch Angle = %.2f degrees,"
                    + "\n\t Roll Angle = %.2f degrees,"
                    + "\n\t Distance = %.2f inches",
                     firstVectorHeight ,firstVectorOrientation[AZIMUTH],
                    Math.abs(firstVectorOrientation[PITCH]),
                    firstVectorOrientation[ROLL],firstVectorDistanceY);

            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    toastMsg,
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.LEFT,0,0);
            toast.show();

            distanceStatus = "Get Second Point";
            buttonGetPoint.setText(distanceStatus);
            buttonChangeHeight.setEnabled(false);
        }
        else if (distanceStatus.equalsIgnoreCase("Get Second Point"))
        {
            secondVectorOrientation[AZIMUTH] = lastVectorOrientation[AZIMUTH];
            secondVectorOrientation[PITCH] = lastVectorOrientation[PITCH];
            secondVectorOrientation[ROLL] = lastVectorOrientation[ROLL];
            secondVectorHeight = userPreference.getCameraHeight();
            secondVectorDistanceY = (Math.tan(Math.toRadians(Math.abs(secondVectorOrientation[PITCH]))) * secondVectorHeight);

            // Law of Cosines
            // c2 = a2 + b2 -2ab cos(C)

            // Azimuth values are between 0 and 359.99

            // Azimuth Deviation = 0
            float azimuthDeviation = Math.abs(firstVectorOrientation[AZIMUTH] - secondVectorOrientation[AZIMUTH]);
            if ( azimuthDeviation <= 0.5 )
            {
                lastDistanceCalculated = Math.abs(firstVectorDistanceY - secondVectorDistanceY);
                lastAzimuthDeviation = azimuthDeviation;
            }
            // First Azimuth - Second Azimuth = 180
            // Therefore the distance is the sum of the first and second distance
            else if ( azimuthDeviation >= 179.5 && azimuthDeviation <= 180.5)
            {
                lastDistanceCalculated = firstVectorDistanceY + secondVectorDistanceY;
                lastAzimuthDeviation = azimuthDeviation;
            }
            // Azimuth Deviation < 180
            else if ( azimuthDeviation < 179.5)
            {
                double firstSquared = firstVectorDistanceY * firstVectorDistanceY;
                double secondSquared = secondVectorDistanceY * secondVectorDistanceY;
                double CosC = Math.cos(Math.toRadians(azimuthDeviation));
                double ab2 = 2 *firstVectorDistanceY * secondVectorDistanceY;
                double ab2CosC = ab2 * CosC;
                double distanceSquared =   firstSquared + secondSquared - ab2CosC;
                lastAzimuthDeviation = azimuthDeviation;
                lastDistanceCalculated = Math.sqrt(distanceSquared);
            }
            // Azimuth Deviation > 180
            else
            {
                if ( firstVectorOrientation[AZIMUTH] > secondVectorOrientation[AZIMUTH] )
                    azimuthDeviation = Math.abs((360 - firstVectorOrientation[AZIMUTH] ) + secondVectorOrientation[AZIMUTH]);
                else
                    azimuthDeviation = Math.abs((360 - secondVectorOrientation[AZIMUTH] ) + firstVectorOrientation[AZIMUTH]);

                double firstSquared = firstVectorDistanceY * firstVectorDistanceY;
                double secondSquared = secondVectorDistanceY * secondVectorDistanceY;
                double CosC = Math.cos(Math.toRadians(azimuthDeviation));
                double ab2 = 2 *firstVectorDistanceY * secondVectorDistanceY;
                double ab2CosC = ab2 * CosC;
                double distanceSquared =   firstSquared + secondSquared - ab2CosC;
                lastAzimuthDeviation = azimuthDeviation;
                lastDistanceCalculated = Math.sqrt(distanceSquared);

                lastAzimuthDeviation = azimuthDeviation;
                lastDistanceCalculated = Math.sqrt(firstSquared + secondSquared - ab2CosC);
            }

            String toastMsg = String.format("Second Point: \n\t Height = %.2f " + userPreference.getUnitOfMeasure() + ","
                            + "\n\t Azimuth Angle = %.2f degrees,"
                            + "\n\t Pitch Angle = %.2f degrees,"
                            + "\n\t Roll Angle = %.2f degrees,"
                            + "\n\t Distance = %.2f  " + userPreference.getUnitOfMeasure()
                            + "\n\nCalculated Distance: \n\t Azimuth Angle = %.2f degrees,"
                            + "\n\t Distance = %.2f " + userPreference.getUnitOfMeasure(),
                    secondVectorHeight ,secondVectorOrientation[AZIMUTH],
                    Math.abs(secondVectorOrientation[PITCH]),
                    secondVectorOrientation[ROLL],secondVectorDistanceY,
                    lastAzimuthDeviation, lastDistanceCalculated);

            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    toastMsg,
                    Toast.LENGTH_SHORT);

            toast.setGravity(Gravity.LEFT, 0, 0);
            toast.show();
            distanceStatus = "Get First Point";
            buttonGetPoint.setText(distanceStatus);
            buttonChangeHeight.setEnabled(true);
        }
        else
        {
            firstVectorOrientation[AZIMUTH] = 0;
            firstVectorOrientation[PITCH] = 0;
            firstVectorOrientation[ROLL] = 0;
            firstVectorDistanceY = 0;
            firstVectorHeight=0;
            secondVectorOrientation[AZIMUTH] = 0;
            secondVectorOrientation[PITCH] = 0;
            secondVectorOrientation[ROLL] = 0;
            secondVectorDistanceY=0;
            secondVectorHeight=0;
            distanceStatus = "Get First Point";
            buttonGetPoint.setText(distanceStatus);
            buttonChangeHeight.setEnabled(true);
        }
    }

    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stub
            String toastMsg = String.format(
                    "First Point: \n\t Height = %.2f " + userPreference.getUnitOfMeasure() + ","
                            + "\n\t Azimuth Angle = %.2f degrees,"
                            + "\n\t Pitch Angle = %.2f degrees,"
                            + "\n\t Roll Angle = %.2f degrees,"
                            + "\n\t Distance = %.2f " + userPreference.getUnitOfMeasure()
                            + "\n\nSecond Point: \n\t Height = %.2f " + userPreference.getUnitOfMeasure() + ","
                            + "\n\t Azimuth Angle = %.2f degrees,"
                            + "\n\t Pitch Angle = %.2f degrees,"
                            + "\n\t Roll Angle = %.2f degrees,"
                            + "\n\t Distance = %.2f " + userPreference.getUnitOfMeasure()
                            + "\n\nCalculated Distance: \n\t Azimuth Angle = %.2f degrees,"
                            + "\n\t Distance = %.2f " + userPreference.getUnitOfMeasure(),
                    firstVectorHeight ,firstVectorOrientation[AZIMUTH],
                    Math.abs(firstVectorOrientation[PITCH]),
                    firstVectorOrientation[ROLL],firstVectorDistanceY,
                    secondVectorHeight ,secondVectorOrientation[AZIMUTH],
                    Math.abs(secondVectorOrientation[PITCH]),
                    secondVectorOrientation[ROLL],secondVectorDistanceY,
                    lastAzimuthDeviation, lastDistanceCalculated);

            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    toastMsg,
                    Toast.LENGTH_SHORT);

            toast.setGravity(Gravity.LEFT,0,0);
            toast.show();

            buttonGetPoint.setEnabled(true);
            if (distanceStatus.equalsIgnoreCase("Get Second Point")) {
                buttonChangeHeight.setEnabled(false);
            }
            else {
                buttonChangeHeight.setEnabled(true);
            }
        }};

    ShutterCallback myShutterCallback = new ShutterCallback(){

        @Override
        public void onShutter() {
            // TODO Auto-generated method stub

        }};

    PictureCallback myPictureCallback_RAW = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub

        }};

/*
    PictureCallback myPictureCallback_JPG = new PictureCallback(){


        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub
   //Bitmap bitmapPicture
   // = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);

            Uri uriTarget = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());

            OutputStream imageFileOS;
            try {
                imageFileOS = getContentResolver().openOutputStream(uriTarget);
                imageFileOS.write(arg0);
                imageFileOS.flush();
                imageFileOS.close();

                Toast.makeText(AndroidCamera.this,
                        "Image saved: " + uriTarget.toString(),
                        Toast.LENGTH_LONG).show();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            camera.startPreview();
        }};
*/
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        if(previewing){
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null){
            try {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size bestSize = null;

                List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
                bestSize = sizeList.get(0);

                for(int i = 1; i < sizeList.size(); i++){
                    if((sizeList.get(i).width * sizeList.get(i).height) >
                            (bestSize.width * bestSize.height)){
                        bestSize = sizeList.get(i);
                    }
                }

                parameters.setPreviewSize(bestSize.width, bestSize.height);
                camera.setParameters(parameters);

                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }
        parameters.setPreviewSize(bestSize.width, bestSize.height);
        camera.setParameters(parameters);
        surfaceView.setWillNotDraw(false);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopPreview();   
        camera.release();
        camera = null;
        previewing = false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        orientationManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        orientationManager.unregisterListener(this, orientationManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
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
        textOrientation.setText("");
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {

            int i = 0;
            for (float orientation_fl : event.values) {

                if (i == AZIMUTH) {
                    lastVectorOrientation[AZIMUTH] = orientation_fl;//z-azimuth
                } else if (i == PITCH) {
                    lastVectorOrientation[PITCH] = orientation_fl;//x-pitch
                } else if (i == ROLL) {
                    lastVectorOrientation[ROLL] = orientation_fl;//y-roll
                } else {
                }
                i = i + 1;
            }
            double currentDistance = (Math.tan(Math.toRadians(Math.abs(lastVectorOrientation[PITCH]))) * userPreference.getCameraHeight() );
            String outfloat = String.format("Current:\nHeight[%.1f] " + userPreference.getUnitOfMeasure() +   "\nAzimuth[%.2f]deg\nPitch[%.2f]deg\nRoll[%.2f]deg\nDistance[%.2f]" + userPreference.getUnitOfMeasure()
                    , userPreference.getCameraHeight(), lastVectorOrientation[AZIMUTH],Math.abs(lastVectorOrientation[PITCH]), lastVectorOrientation[ROLL],currentDistance);
            textOrientation.setText(outfloat);
        }
    }
}

package com.example.andrew.measuremyimage;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.andrew.measuremyimage.DataBase.DataBaseManager;
import com.example.andrew.measuremyimage.DataBase.ReferenceObjectSchema;


public class ReferenceObject extends ActionBarActivity {

    // Log cat tag
    private static final String LOG = "ReferenceObject";

    //intent message
    public final static String EXTRA_MESSAGE = "com.example.measuremyimage.MESSAGE";

    private TextView objectName;
    private TextView height;
    private TextView width;
    private TextView errorText;
    private Spinner spinner;
    private String userName;
    private int spinnerPos = 0;
    private DataBaseManager dbManager;
    private UserLoggedIn userLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference_object);
        Log.e(LOG, "Entering: onCreate");
        //getActionBar().hide();
        userLoggedIn = UserLoggedIn.getInstance();
        userName = userLoggedIn.getUser().getUserName();
        dbManager = DataBaseManager.getInstance(getApplicationContext());

        objectName = (TextView)findViewById(R.id.ObjectNameEditText);
        height = (TextView)findViewById(R.id.HeightEditText);
        width = (TextView)findViewById(R.id.WidthEditText);
        spinner = (Spinner)findViewById(R.id.UnitOfMeasureSpinner);
        errorText = (TextView)findViewById(R.id.ErrorTextView);
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


    public void onSubmitClick(View aView)
    {
        Log.e(LOG, "Entering: onSubmitClick");

        String lObjectName = objectName.getText().toString();
        String tHeight = height.getText().toString();
        String tWidth = width.getText().toString();

        if(lObjectName == "" || tHeight == "" || tWidth == "" || spinnerPos == 0)
        {
            errorText.setText("Blank Object Data");
            return;
        }

        int lHeight = Integer.parseInt(tHeight);
        int lWidth = Integer.parseInt(tWidth);

        //Create reference object
        ReferenceObjectSchema referenceObjectSchema =
                new ReferenceObjectSchema(lObjectName,lHeight,lWidth,spinner.getItemAtPosition(spinnerPos).toString(),userName);

        if(!dbManager.CreateAReferenceObject(referenceObjectSchema))
        {
            errorText.setText("Duplicate Object Name");
            return;
        }

        //Reset all fields
        objectName.setText("");
        height.setText("");
        width.setText("");
        spinner.setSelection(0);

        this.finish();
    }
}

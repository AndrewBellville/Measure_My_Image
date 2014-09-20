package com.example.andrew.measuremyimage;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.andrew.measuremyimage.DataBase.DataBaseManager;
import com.example.andrew.measuremyimage.DataBase.UserSchema;


public class UserLogin extends ActionBarActivity {

    // Log cat tag
    private static final String LOG = "UserLogin";

    private TextView userName;
    private TextView password;
    private TextView message;
    private DataBaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        dbManager = new DataBaseManager(getApplicationContext());
        Log.e(LOG, "Entering: onCreate");

        //Link to XML onCreate of this class
        userName = (TextView)findViewById(R.id.UserNameTextBox);
        password = (TextView)findViewById(R.id.PasswordTextBox);
        message = (TextView)findViewById(R.id.MessageText);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validateUserLogin()
    {
        Log.e(LOG, "Entering: validateUserLogin");

        //TODO better verification
        //verify user record exists
        return dbManager.DoesUserExist(userName.getText().toString(),password.getText().toString());
    }

    private boolean validateUserCreate()
    {
        Log.e(LOG, "Entering: validateUserCreate");

        //TODO better verification
        //create user schema from XML field data and create user in DB
        UserSchema user = new UserSchema(userName.getText().toString(),password.getText().toString());
        return dbManager.CreateAUser(user);
    }

    public void onLoginClick(View aView)
    {
        Log.e(LOG, "Entering: onLoginClick");

        //TODO actually do something
        if (validateUserLogin())
        {
            message.setText("Successful Login");
        }
        else
        {
            message.setText("Failed Login");
        }
        message.invalidate();
    }

    public void onCreateClick(View aView)
    {
        Log.e(LOG, "Entering: onCreateClick");

        //TODO actually do something
        if (validateUserCreate())
        {
            message.setText("Successful Create");
        }
        else {
            message.setText("Failed Create");
        }
        message.invalidate();
    }
}

package com.example.andrew.measuremyimage;



import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

//TODO Create more  tabs for user profile
public class UserProfile extends TabActivity {
    // Log cat tag
    private static final String LOG = "UserProfile";

    //intent message
    public final static String EXTRA_MESSAGE = "com.example.measuremyimage.MESSAGE";

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Log.e(LOG, "Entering: onCreate");

        //Get intent message should be user name from Login
        Intent intent = getIntent();
        userName = intent.getStringExtra(UserLogin.EXTRA_MESSAGE);
        // display in the action bar
        getActionBar().setTitle(userName);
        Log.e(LOG, "User profile for ["+ userName +"] ");

        TabHost tabHost = getTabHost();

        //Add image tab
        intent = new Intent(this, UserImages.class);
        intent.putExtra(EXTRA_MESSAGE, userName);
        tabHost.addTab(tabHost.newTabSpec("images").setIndicator("Images").setContent(intent));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_profile, menu);
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
}

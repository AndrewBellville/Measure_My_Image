package com.example.andrew.measuremyimage;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.example.andrew.measuremyimage.DataBase.DataBaseManager;
import com.example.andrew.measuremyimage.DataBase.ImageSchema;
import com.example.andrew.measuremyimage.DataBase.ReferenceObjectSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UserReferenceObjects extends Activity {

    // Log cat tag
    private static final String LOG = "UserReferenceObjects";

    private String userName;
    private ListView ObjectListView;
    private DataBaseManager dbManager;
    private UserLoggedIn userLoggedIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reference_objects);
        Log.e(LOG, "Entering: onCreate");
        userLoggedIn = UserLoggedIn.getInstance();
        userName = userLoggedIn.getUser().getUserName();
        ObjectListView = (ListView)findViewById(R.id.ReferenceObjectList);
        dbManager = DataBaseManager.getInstance(getApplicationContext());

        LoadReferenceObjects();
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e(LOG, "Entering: onResume");
        LoadReferenceObjects();
    }

    private void LoadReferenceObjects() {
        Log.e(LOG, "Entering: LoadReferenceObjects");

        List<ReferenceObjectSchema> objectList = dbManager.getAllReferenceObjectsForUser(userName);
        if (objectList.size() > 0) {
            ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

            for(ReferenceObjectSchema obj : objectList)
            {
                HashMap<String,String> temp = new HashMap<String,String>();
                temp.put("Name",obj.getObjectName());
                temp.put("Dim", "H: " + Integer.toString(obj.getHeight()) + " , W: " +  Integer.toString(obj.getWidth()));
                list.add(temp);
            }

            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    list,
                    R.layout.custom_row_view,
                    new String[] {"Name","Dim"},
                    new int[] {R.id.text1,R.id.text2}

            );

            ObjectListView.setAdapter(adapter);
        }
    }


    public void onCreateClick(View aView)
    {
        Log.e(LOG, "Entering: onCreateClick");

        Intent intent = new Intent(this, ReferenceObject.class);
        //start next activity
        startActivity(intent);
    }


}

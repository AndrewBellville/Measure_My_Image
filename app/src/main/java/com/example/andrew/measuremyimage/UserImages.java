package com.example.andrew.measuremyimage;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.andrew.measuremyimage.DataBase.DataBaseManager;
import com.example.andrew.measuremyimage.DataBase.ImageSchema;

import java.util.Iterator;
import java.util.List;

public class UserImages extends Activity {

    // Log cat tag
    private static final String LOG = "UserImages";

    private static int RESULT_LOAD_IMAGE = 1;
    private DataBaseManager dbManager;

    //TODO need a better way of knowing he is logged it without sending user name to each activity
    private String userName;
    private TableLayout imageTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_images);
        Log.e(LOG, "Entering: onCreate");
        Intent intent = getIntent();
        userName = intent.getStringExtra(UserLogin.EXTRA_MESSAGE);
        dbManager = DataBaseManager.getInstance(getApplicationContext());
        imageTable = (TableLayout)findViewById(R.id.ImageTable);

        LoadUserImages();

    }

    //TODO Optimize
    //TODO Load only new images based on creation date
    private void LoadUserImages()
    {
        Log.e(LOG, "Entering: LoadUserImages");

        //set all sizing
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();  // deprecated
        int imageSize = 300;
        int imagePadding = 5;
        int totalImagesPerRow = width / (imageSize+imagePadding);
        int imagesInRowCount = 0;

        List<ImageSchema> imageList = dbManager.getAllImagesForUser(userName);
        imageTable.removeAllViews();
        // just for testing
        if(imageList.size() >  0)
        {
            ToggleNoImageDisp(false);
            TableRow row = new TableRow(this);
            for(Iterator<ImageSchema> i = imageList.iterator(); i.hasNext(); ) {
                //create image view
                View v = new ImageView(getBaseContext());
                ImageView image = new ImageView(v.getContext());
                image.setPadding(imagePadding,imagePadding,imagePadding,imagePadding);

                //get the next image schema
                ImageSchema imageSchema = i.next();
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageSchema.getImage(), imageSize, imageSize, true);

                //add image schema to image
                image.setImageBitmap(scaledBitmap);

                row.addView(image);
                imagesInRowCount++;

                // if count is total that can fit or is the last element
                if (totalImagesPerRow == imagesInRowCount || !i.hasNext())
                {
                    imageTable.addView(row);
                    row = new TableRow(this);
                    imagesInRowCount = 0;
                }
            }
        }
        else
        {
            ToggleNoImageDisp(true);
        }

        imageTable.invalidate();
    }

    // handles the button click and opens Image gallery
    public void ImportImageClick(View aView)
    {
        Log.e(LOG, "Entering: ImportImage");

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent,RESULT_LOAD_IMAGE);

    }

    // handles the selection of a single image from image gallery and stores to db
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.e(LOG, "Entering: onActivityResult requestCode ["+requestCode+"] resultCode["+resultCode+"]");
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case 1: //RESULT_LOAD_IMAGE
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    //TODO should save this image but need to optimize
                    Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(yourSelectedImage,200,200,true);


                    ImageSchema imageSchema = new ImageSchema(scaledBitmap,userName);
                    dbManager.CreateAImage(imageSchema);

                    //call to reload images
                    LoadUserImages();
                }
        }

    }

    private void ToggleNoImageDisp(boolean aHasNoImage)
    {
        if(aHasNoImage)
        {
            // show no images text
            TextView textView = new TextView(this);
            textView.setText("No Images");
            imageTable.addView(textView);
        }
    }
}

package com.example.andrew.measuremyimage;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.andrew.measuremyimage.DataBase.DataBaseManager;
import com.example.andrew.measuremyimage.DataBase.ImageSchema;

import java.util.Iterator;
import java.util.List;

public class UserImages extends Activity {

    // Log cat tag
    private static final String LOG = "UserImages";

    private static int RESULT_LOAD_IMAGE = 1;
    private ImageView image;
    private DataBaseManager dbManager;

    //TODO need a better way of knowing he is logged it without sending user name to each activity
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_images);
        Log.e(LOG, "Entering: onCreate");
        Intent intent = getIntent();
        userName = intent.getStringExtra(UserLogin.EXTRA_MESSAGE);
        dbManager = DataBaseManager.getInstance(getApplicationContext());
        image = (ImageView)findViewById(R.id.imageView);

        LoadUserImages();

    }

    //TODO dynamically load user images
    //TODO Load only new images based on creation date
    private void LoadUserImages()
    {
        Log.e(LOG, "Entering: LoadUserImages");

        List<ImageSchema> imageList = dbManager.getAllImagesForUser(userName);
        for(Iterator<ImageSchema> i = imageList.iterator(); i.hasNext(); ) {
            ImageSchema image = i.next();
            //TODO dynamic loading here
        }

        // just for testing
        if(imageList.size() >  0)
        {
            ImageSchema testImage = imageList.get(0);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(testImage.getImage(), 200, 200, true);
            image.setImageBitmap(scaledBitmap);
            image.invalidate();
        }
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
}

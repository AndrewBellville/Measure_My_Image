package com.example.andrew.measuremyimage;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
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

    //intent message
    public final static String EXTRA_MESSAGE = "com.example.measuremyimage.MESSAGE";

    private static int RESULT_LOAD_IMAGE = 1;
    private static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2;
    private DataBaseManager dbManager;
    private UserLoggedIn userLoggedIn;

    private String userName;
    private TableLayout imageTable;
    private boolean isDelete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_images);
        Log.e(LOG, "Entering: onCreate");
        userLoggedIn = UserLoggedIn.getInstance();
        userName = userLoggedIn.getUser().getUserName();
        dbManager = DataBaseManager.getInstance(getApplicationContext());
        imageTable = (TableLayout) findViewById(R.id.ImageTable);

        LoadUserImages();

    }

    //TODO Optimize
    //TODO Load only new images based on creation date
    private void LoadUserImages() {
        Log.e(LOG, "Entering: LoadUserImages");

        //set all sizing
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();  // deprecated
        int imageSize = 300;
        int imagePadding = 5;
        int totalImagesPerRow = width / (imageSize + imagePadding);
        int imagesInRowCount = 0;

        List<ImageSchema> imageList = dbManager.getAllImagesForUser(userName);
        imageTable.removeAllViews();
        // just for testing
        if (imageList.size() > 0) {
            ToggleNoImageDisp(false);
            TableRow row = new TableRow(this);
            for (Iterator<ImageSchema> i = imageList.iterator(); i.hasNext(); ) {

                //get the next image schema
                ImageSchema imageSchema = i.next();

                //create image view
                View v = new ImageView(getBaseContext());
                ImageSchemaView image = new ImageSchemaView(v.getContext(), imageSchema);
                image.setClickable(true);
                image.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View aView) {
                        ImageClickHandler(aView);
                    }
                });

                image.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);


                Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageSchema.getImage(), imageSize, imageSize, true);

                //add image schema to image
                image.setImageBitmap(scaledBitmap);

                row.addView(image);
                imagesInRowCount++;

                // if count is total that can fit or is the last element
                if (totalImagesPerRow == imagesInRowCount || !i.hasNext()) {
                    imageTable.addView(row);
                    row = new TableRow(this);
                    imagesInRowCount = 0;
                }
            }
        } else {
            ToggleNoImageDisp(true);
        }

        imageTable.invalidate();
    }

    // handles the button click and opens Image gallery
    public void ImportImageClick(View aView) {
        Log.e(LOG, "Entering: ImportImageClick");

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, RESULT_LOAD_IMAGE);

    }

    // handles the button click and intent for camera
    public void TakePictureClick(View aView) {
        Log.e(LOG, "Entering: TakePictureClick");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // handles the image click
    public void ImageClickHandler(View aView) {
        Log.e(LOG, "Entering: ImageClickHandler");
        //get image that was clicked
        ImageSchemaView temp = (ImageSchemaView) aView;
        if (isDelete) {
            dbManager.DeleteAnImage(temp.getSchema().getId());
            //call to reload images
            LoadUserImages();

        } else {
            //Bitmap bitmap = ((BitmapDrawable)temp.getDrawable()).getBitmap();
            Integer id = temp.getSchema().getId();
            Log.e(LOG, id.toString());

            //Create and intent which will open ImageDisplay for image clicked
            Intent intent = new Intent(this, ImageDisplay.class);
            intent.putExtra(EXTRA_MESSAGE, id);
            //start next activity
            startActivity(intent);
        }
    }

    // handles the selection of a single image from image gallery and stores to db
    protected void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        Log.e(LOG, "Entering: onActivityResult requestCode [" + aRequestCode + "] resultCode[" + aResultCode + "]");
        super.onActivityResult(aRequestCode, aResultCode, aData);

            if(aRequestCode == RESULT_LOAD_IMAGE)
            {
                if (aResultCode == RESULT_OK) {

                    ImageSchema imageSchema = new ImageSchema(GetImageFromData(aData), userName);
                    dbManager.CreateAnImage(imageSchema);
                }
            }
            else if(aRequestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                if (aResultCode == RESULT_OK) {

                    // Image captured and saved
                    //Bundle extras = data.getExtras();
                    //Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ImageSchema imageSchema = new ImageSchema(GetImageFromData(aData), userName);
                    dbManager.CreateAnImage(imageSchema);
                } else if (aResultCode == RESULT_CANCELED) {
                    // User cancelled the image capture
                } else {
                    // Image capture failed, advise user
                }
            }

        //call to reload images
        LoadUserImages();
    }

    private Bitmap GetImageFromData(Intent aData) {
        Log.e(LOG, "Entering: GetImageFromData");
        Uri selectedImage = aData.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        //TODO should save this image but need to optimize
        Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
        Bitmap scaledBitmap;

        Log.e(LOG, Integer.toString(yourSelectedImage.getWidth())+"  "+Integer.toString(yourSelectedImage.getHeight()));
        if(yourSelectedImage.getWidth() > 1000 || yourSelectedImage.getHeight() > 1500)
        {
            yourSelectedImage = Bitmap.createScaledBitmap(yourSelectedImage, yourSelectedImage.getWidth()/2, yourSelectedImage.getHeight()/2, true);
        }

        return yourSelectedImage;
    }

    private void ToggleNoImageDisp(boolean aHasNoImage) {
        if (aHasNoImage) {
            // show no images text
            TextView textView = new TextView(this);
            textView.setText("No Images");
            imageTable.addView(textView);
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        if(isDelete) ((RadioButton) view).setChecked(false);
        isDelete = ((RadioButton) view).isChecked();
    }

}
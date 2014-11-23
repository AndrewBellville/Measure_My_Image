package com.example.andrew.measuremyimage;

import android.content.Context;
import android.widget.ImageView;
import com.example.andrew.measuremyimage.DataBase.ImageSchema;
import java.io.Serializable;

/**
 * Created by Andrew on 11/2/2014.
 */
public class ImageSchemaView extends ImageView
    implements Serializable {

    ImageSchema schema;

    public ImageSchemaView(Context context, ImageSchema aSchema) {
        super(context);
        schema = aSchema;
    }

    public ImageSchema getSchema() {
        return schema;
    }
}

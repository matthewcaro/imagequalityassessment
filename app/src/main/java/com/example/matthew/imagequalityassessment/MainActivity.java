package com.example.matthew.imagequalityassessment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Example code for picking images in android
 */
public class MainActivity extends AppCompatActivity {

    // this is the action code we use in our intent,
    // this way we know we're looking at the response from our own action
    private static final int SELECT_SINGLE_PICTURE = 101;

    private static final int SELECT_MULTIPLE_PICTURE = 201;

    public static final String IMAGE_TYPE = "image/*";

    /* Get the reference to the text label */
    TextView label = null;


    private ImageView selectedImagePreview;


    void showImage(Bitmap image)
    {

        //label.setText("Loading...");

		/* Get the starting time */
        long startTime = 0;

        startTime = System.nanoTime();

		/* Get the image statistics */
        ImageStats imageStats = new ImageStats(image);

        System.out.println("Execution time: " + (((double)(System.nanoTime() - startTime))/1000000000.0) + " seconds!");

		 /* Get the image statistics */
        double[] stats = imageStats.getStats();

        /*  Decide whether or not the image is of good quality */
        String results = imageStats.result;

		 /* Create the labels */
        String[] labels = new String[]{"Standard Luminosity: ", "Contrast: ", "Face orientation: ", "Sharpness: "};

		 /* The string of statistics */
        StringBuffer strStatsBuff = new StringBuffer();

		 /* Go through all the statistics */
        for(int statIndex = 0; statIndex < stats.length; ++statIndex)
        {
	    	  /* Add the statistics */
            strStatsBuff.append(labels[statIndex]);
            strStatsBuff.append(String.valueOf(stats[statIndex]));
            strStatsBuff.append("\n");
        }

	     /* Add the file name */
        strStatsBuff.append("\n");
        strStatsBuff.append(results);


	      /* Set the label and show the image */
        label.setText(strStatsBuff.toString());



    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // no need to cast to button view here since we can add a listener to any view, this
        // is the single image selection
        label = (TextView)findViewById(R.id.label);
        findViewById(R.id.buttonGallery).setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                // in onCreate or any event where your want the user to
                // select a file
                Intent intent = new Intent();
                intent.setType(IMAGE_TYPE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select image"), SELECT_SINGLE_PICTURE);
            }
        });

        // multiple image selection


        selectedImagePreview = (ImageView)findViewById(R.id.imageView1);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_SINGLE_PICTURE) {

                Uri selectedImageUri = data.getData();
                String selectedImagePath = getPath(selectedImageUri);
                //Bitmap bmp=BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));

                //showImage(bitmap)
                ;
                try {
                    selectedImagePreview.setImageBitmap(new UserPicture(selectedImageUri, getContentResolver()).getBitmap());
                    Bitmap bitmap=BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                    showImage(bitmap);
                } catch (IOException e) {
                    Log.e(MainActivity.class.getSimpleName(), "Failed to load image", e);
                    e.printStackTrace();
                }

                // original code
//                String selectedImagePath = getPath(selectedImageUri);
//                selectedImagePreview.setImageURI(selectedImageUri);
            }
            else if (requestCode == SELECT_MULTIPLE_PICTURE) {
                //And in the Result handling check for that parameter:
                if (Intent.ACTION_SEND_MULTIPLE.equals(data.getAction())
                        && data.hasExtra(Intent.EXTRA_STREAM)) {
                    // retrieve a collection of selected images
                    ArrayList<Parcelable> list = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    // iterate over these images
                    if( list != null ) {
                        for (Parcelable parcel : list) {
                            Uri uri = (Uri) parcel;
                            // handle the images one by one here
                        }
                    }

                    // for now just show the last picture
                    if( !list.isEmpty() ) {
                        Uri imageUri = (Uri) list.get(list.size() - 1);

                        try {
                            selectedImagePreview.setImageBitmap(new UserPicture(imageUri, getContentResolver()).getBitmap());
                        } catch (IOException e) {
                            Log.e(MainActivity.class.getSimpleName(), "Failed to load image", e);
                        }
                        // original code
//                        String selectedImagePath = getPath(imageUri);
//                        selectedImagePreview.setImageURI(imageUri);
//                        displayPicture(selectedImagePath, selectedImagePreview);
                    }
                }
            }
        } else {
            // report failure
            Toast.makeText(getApplicationContext(),"failed to get intent data", Toast.LENGTH_LONG).show();
            Log.d(MainActivity.class.getSimpleName(), "Failed to get intent data, result code is " + resultCode);
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {

        // just some safety built in
        if( uri == null ) {
            // perform some logging or show user feedback
            Toast.makeText(getApplicationContext(), "failed to get picture", Toast.LENGTH_LONG).show();
            Log.d(MainActivity.class.getSimpleName(), "Failed to parse image path from image URI " + uri);
            return null;
        }

        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here, thanks to the answer from @mad indicating this is needed for
        // working code based on images selected using other file managers
        return uri.getPath();
    }


//    /**
//     * helper to scale down image before display to prevent render errors:
//     * "Bitmap too large to be uploaded into a texture"
//     */
    private void displayPicture(String imagePath, ImageView imageView) {

        // from http://stackoverflow.com/questions/22633638/prevent-bitmap-too-large-to-be-uploaded-into-a-texture-android

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        int height = bitmap.getHeight(), width = bitmap.getWidth();

        if (height > 1280 && width > 960){
            Bitmap imgbitmap = BitmapFactory.decodeFile(imagePath, options);
            imageView.setImageBitmap(imgbitmap);
        } else {
            imageView.setImageBitmap(bitmap);
        }
        showImage(bitmap);
    }


}


package com.example.matthew.imagequalityassessment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Example code for picking images in android
 */
public class MainActivity extends AppCompatActivity {

    // this is the action code we use in our intent,
    // this way we know we're looking at the response from our own action
    private static final int SELECT_SINGLE_PICTURE = 101;

    private static final int CAMERA_REQUEST = 102;

    public static final String IMAGE_TYPE = "image/*";

    private final int RequestCode = 20;

    /* Get the reference to the text label */
    TextView label = null;

    ImageView selectedImagePreview;
    ImageView imageViewFace;



    void showImage(Bitmap image)
    {
        FaceCropper mFaceCropper = new FaceCropper();
        mFaceCropper.setMaxFaces(1);
        mFaceCropper.setEyeDistanceFactorMargin(0);
        image = mFaceCropper.getCroppedImage(image);

        String faceFound;
        if (mFaceCropper.faceDetected==true){
            faceFound = "Face Detected!";
        }else faceFound ="No face detected!";

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
       strStatsBuff.append(faceFound);
//        strStatsBuff.append("\n");

	      /* Set the label and show the cropped image */
        label.setText(strStatsBuff.toString());


        if (mFaceCropper.faceDetected == true)
        {
            imageViewFace.setImageBitmap(image);
        }

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

        findViewById(R.id.buttonCamera).setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0 ){
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        selectedImagePreview = (ImageView)findViewById(R.id.imageView1);
        imageViewFace = (ImageView)findViewById(R.id.imageViewFace);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_SINGLE_PICTURE) {

                Uri selectedImageUri = data.getData();

                try {
                    Bitmap bitmap;
                    selectedImagePreview.setImageBitmap(new UserPicture(selectedImageUri, getContentResolver()).getBitmap());
                    bitmap=BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                    selectedImagePreview.setImageBitmap(bitmap);
//                    FaceCropper mfacecropper = new FaceCropper();
//                    bitmap = mfacecropper.getCroppedImage(bitmap);
                    showImage(bitmap);

                } catch (IOException e) {
                    Log.e(MainActivity.class.getSimpleName(), "Failed to load image", e);
                    e.printStackTrace();
                }

            }

        }

        else if (requestCode==CAMERA_REQUEST && resultCode == RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            selectedImagePreview.setImageBitmap(bitmap);
            showImage(bitmap);

        }


        else {
            // report failure
            Toast.makeText(getApplicationContext(),"failed to get intent data", Toast.LENGTH_LONG).show();
            Log.d(MainActivity.class.getSimpleName(), "Failed to get intent data, result code is " + resultCode);
        }
    }


}


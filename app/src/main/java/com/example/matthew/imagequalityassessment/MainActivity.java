package com.example.matthew.imagequalityassessment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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


public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 101;

    private static final int CAMERA_REQUEST = 102;

    public static final String IMAGE_TYPE = "image/*";

    private final int RequestCode = 20;

    Uri mImageCaptureUri1;

    /* Get the reference to the text label */
    TextView label = null;

    //Initiate imageviews
    ImageView selectedImagePreview;
    ImageView imageViewFace;



    void showImage(Bitmap image)
    {
        imageViewFace.setImageBitmap(null);
        FaceCropper mFaceCropper = new FaceCropper();
        mFaceCropper.setMaxFaces(1);
        mFaceCropper.setEyeDistanceFactorMargin(0);


        Bitmap image1 = image;
        //Choose between the following lines to either display green box around face or not

        //selectedImagePreview.setImageBitmap(mFaceCropper.getFullDebugImage(image1));
        selectedImagePreview.setImageBitmap(image1);

        //sets image to the cropped face image for statistics
        image = mFaceCropper.getCroppedImage(image);

        int height = image.getHeight();
        int width = image.getWidth();

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
        //String results = imageStats.result;

		 /* Create the labels */
        String[] labels = new String[]{"Standard Luminosity: ", "Contrast: ", "Face orientation offset: ", "Sharpness: "};

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


        if (mFaceCropper.faceDetected == true)
        {
            /*Display the height and width of cropped image:*/
            strStatsBuff.append("Pixels: "+ height +"x" + width + " (" + height * width + " total)");
            strStatsBuff.append("\n");
        }

        //If displaying accept/reject add following line:
        //strStatsBuff.append(results);

        //Comments that there is a face detected in the image.
        strStatsBuff.append(faceFound);

	      /* Set the label and show the cropped image */
        label.setText(strStatsBuff.toString());


        //Only displays secondary cropped imageview if a face is detected
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
                        "Select image"), SELECT_PICTURE);
            }
        });

        findViewById(R.id.buttonCamera).setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0 ){
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                mImageCaptureUri1 = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                        "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));

                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri1);
                cameraIntent.putExtra("return-data", true);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        selectedImagePreview = (ImageView)findViewById(R.id.imageView1);
        imageViewFace = (ImageView)findViewById(R.id.imageViewFace);
    }

    // Results that happen after gallery option or camera options selected
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {

                //Get data location for image from gallery
                Uri selectedImageUri = data.getData();

                try {
                    Bitmap bitmap;
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                    //class to perform image cropping and displays
                    showImage(bitmap);

                } catch (IOException e) {
                    Log.e(MainActivity.class.getSimpleName(), "Failed to load image", e);
                    e.printStackTrace();
                }

            }
            else if (requestCode==CAMERA_REQUEST){

                try {
                    Bitmap bitmap;
                    //selectedImagePreview.setImageBitmap(new UserPicture(mImageCaptureUri1, getContentResolver()).getBitmap());
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImageCaptureUri1));
                    //selectedImagePreview.setImageBitmap(bitmap);

                    //class to perform image cropping and displays
                    showImage(bitmap);

                } catch (IOException e) {
                    Log.e(MainActivity.class.getSimpleName(), "Failed to load image", e);
                    e.printStackTrace();
                }
            }
        }

        else {
            // report failure
            Toast.makeText(getApplicationContext(),"failed to get intent data", Toast.LENGTH_LONG).show();
            Log.d(MainActivity.class.getSimpleName(), "Failed to get intent data, result code is " + resultCode);
        }
    }
}


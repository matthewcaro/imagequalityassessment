package com.example.matthew.imagequalityassessment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


import android.content.res.AssetManager;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.view.View.OnClickListener;
import android.graphics.*;
import android.media.FaceDetector;

import java.util.Vector;



public class MainActivity extends Activity {




    public static final String IMAGE_TYPE = "image/*";
    private static final int SELECT_SINGLE_PICTURE = 101;


    int count = 0;

    /* The file index */
    int imgIdx = 0;

    /* The list of files */
    Vector<String> files = new Vector<String>();


    /* Get the reference to the image object */
    ImageView imageView = null;

    /* Get the reference to the text label */
    TextView label = null;

    /* By how much to scale down the original image.
     * During tests, I sometimes had to scale down
     * the image size, as some old phones ran out
     * of memory.
     */
    final int IMAGE_SCALE_DOWN_FACTOR = 2;

    /**
     * Displays the next image
     * @param image - the image to show
     * @param fileName - the name of the image file
     */





    void showImage(Bitmap image, String fileName)
    {

        label.setText("Loading...");

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
        String[] labels = new String[]{"Standard Luminosity: ","Perceived Luminosity: ", "Contrast: ", "Face orientation: ", "Sharpness: "};

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
        strStatsBuff.append("File name: ");
        strStatsBuff.append(fileName);
        strStatsBuff.append("\n");
        strStatsBuff.append(results);


	      /* Set the label and show the image */
        label.setText(strStatsBuff.toString());
        imageView.setImageBitmap(image);

    }


    /**
     * Reads the bitmap image from the assets library
     * @param fileName - the file name
     * @return - The loaded image
     */
    Bitmap loadBitmapFromAssets(String fileName)
    {

		/* The bitmap */
        Bitmap bitmap = null;

		/* Load the image */
        try
        {
            System.out.println("Trying to load file <" + fileName + "> from assets");

			/* Load the image */
            InputStream input = getAssets().open(fileName);
            bitmap = BitmapFactory.decodeStream(input);
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/IMAGE_SCALE_DOWN_FACTOR, bitmap.getHeight()/IMAGE_SCALE_DOWN_FACTOR, false);
        }
        catch(Exception e)
        {
            System.err.println("Something went wrong...");
            e.printStackTrace();
        }

        return bitmap;
    }


    /**
     * This is where the program execution begins
     * @param savedInstanceState - the instance of the application.
     */
    private final int REQUEST_CODE_EXTERNAL_IMAGE = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		/* Call the constructor of the super-class */
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button buttonGallery = (Button) findViewById(R.id.buttonGallery);


        buttonGallery.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // choose picture from gallery
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent,
                        REQUEST_CODE_EXTERNAL_IMAGE);

            }
        });






		/* Get the asset manager */
        AssetManager am = getAssets();


		/* Populate the file list */
        try {

            for(String s: am.list(""))
            {
                if(s.endsWith(".jpg") || s.endsWith(".png"))
                {
                    files.add(s);

                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

	  	/* Get the reference to the image object */
        imageView= (ImageView)findViewById(R.id.imageView1);

	  	/* Get the reference to the text label */
        label = (TextView)findViewById(R.id.label);


	      /* Get the reference to the "next" button */
        Button buttonNext = (Button)findViewById(R.id.buttonNext);

	      /* Shows the next image when the "next" button is clicked */
        buttonNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0)
            {
					/* No files to show */
                if(files.size() == 0) return;

					/* Increment the index */
                ++imgIdx;

					/* Is this the end of the list? Go back to the beginning */
                if(imgIdx == files.size())
                    imgIdx = 0;

					/* Load the bitmap */
                Bitmap bitmap = loadBitmapFromAssets(files.get(imgIdx));

                System.out.println("Image width: "+ bitmap.getWidth());

					/* Show the image */
                showImage(bitmap, files.get(imgIdx));

            }
        });



	      /* Get the reference to the "Previous" button */
        Button buttonPrev = (Button)findViewById(R.id.buttonPrev);

	      /* Shows the next image when the "next" button is clicked */
        buttonPrev.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0)
            {
						/* No files to show */
                if(files.size() == 0) return;

						/* Is this the end of the list? Go back to the beginning */
                if(imgIdx == 0)
                    imgIdx = files.size() - 1;
						/* Otherwise, increment the index */
                else
							/* Decrement the index */
                    --imgIdx;

						/* Load the bitmap */
                Bitmap bitmap = loadBitmapFromAssets(files.get(imgIdx));

						/* Show the image */
                showImage(bitmap, files.get(imgIdx));


            }

        });



	      /* Load the first image */
        if(files.size() != 0)
        {
	    	 /* Load the first image */
            showImage(loadBitmapFromAssets(files.get(0)), files.get(0));
        }
    }





}

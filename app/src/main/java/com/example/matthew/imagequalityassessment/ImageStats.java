package com.example.matthew.imagequalityassessment;

/**
 * Created by Matthew Caro on 4/9/2016.
 * Graduate Project California State University Fullerton
 * Code created with much help from Dr. Gofman
 * Facial Image Quality Assessment Android Application created by Dr. Gofman
 * used as a template to create this software, necessary modifications and
 * improvements made to create application.
 */

import android.graphics.Bitmap;
class ImageStats
{
    /*INITIALIZE VARIABLES*/


    /*luminosity*/
    double avgStandardLum = 0.0;
    /*PERCEIVED luminosity*/
    double avgPerceivedLum = 0.0;

    /*contrast*/
    double avgContrast = 0.0;
    /*Initialize center of X axis coordinate*/
    int centerOfMassX=0;
    /*Initialize center of Y axis coordinate*/
    int centerOfMassY=0;
    /*geo center of x*/
    int geoCenterX=0;
    /*geo center of y*/
    int geoCenterY=0;
    /*Initialize what will be the difference of mass xy center and geo xy center*/
    double faceOrientationIndex = 0.0;
    /*Initialize average sharpness to be computed*/
    double avgSharp=0;
    /*validity of class*/
    boolean valid=false;
    /*red in grey*/
    private final static double RedWt=0.299;
    /*green in grey*/
    private final static double GreenWt=0.587;
    /*blue in grey*/
    private final static double BlueWt=0.114;
    /*box size*/
    int BoxSz=3;
    /*square of box*/
    int SquBoxSz=BoxSz*BoxSz;
    /*pixels in image sharpness*/
    int numPixSharp=0;
    /*pixel array*/
    int[] pixels = null;
    /*sum of difference rgb to low pass filtered counterparts*/
    double lowPassRed=0;
    double lowPassGreen=0;
    double lowPassBlue=0;
    /*Initialize Red,Green,Blue values in low pass filter*/
    int winRed=0;
    int winGreen=0;
    int winBlue=0;
    /*original pixels in filtering*/
    int pixelFiltRed=0;
    int pixelFiltGreen=0;
    int pixelFiltBlue=0;
    /*image width*/
    int imageWidth;
    /*image height*/
    int imageHeight;
    String result;
    double GoodLum = 0.2;
    double GoodCont = 0.4;
    double GoodSharp = 2700;
    double GoodOrient = 100;

    /*testing including perceived lum*/
    public enum Stats
    {
        STD_LUM(0), Per_Lum(1), CONTRAST(2), FACE_ORIENTATION_INDEX(3), SHARPNESS(4);
        int value;
        private Stats(int value)
        {
            this.value = value;
        }
        public int getValue() {return this.value;}
    };

    /*Array of statistics for image*/
    double[] getStats()
    {
        if(!this.valid) throw new IllegalStateException();
        return new double[]{avgStandardLum, avgPerceivedLum, avgContrast,faceOrientationIndex,avgSharp};

    }

    /*Set number values to statistics to be computed*/
    //public enum Stats
    //{
    //    STD_LUM(0), CONTRAST(1), FACE_ORIENTATION_INDEX(2), SHARPNESS(3);
    //    int value;
    //    private Stats(int value)
    //    {
    //        this.value = value;
    //    }
    //    public int getValue() {return this.value;}
    //};

    /*Array of statistics for image*/
    //double[] getStats()
    //{
    //    if(!this.valid) throw new IllegalStateException();
    //    return new double[]{avgStandardLum, avgContrast,faceOrientationIndex,avgSharp};
//
//    }

    /*Compute the next pixel to the right*/
    void moveWindowRight(int windowX, int windowY)
    {
        int pixel =0;
        int windowLUE=(imageWidth*windowY)+windowX;
        int windowRUE= windowLUE+BoxSz;
        for (int i=0; i<BoxSz; ++i)
        {
            pixel = pixels[(i*imageWidth)+windowLUE];
            winRed-=((pixel>>16)&0xff);
            winGreen-=((pixel>>8)&0xff);
            winBlue-=((pixel)&0xff);

            /*first pixel in new row*/
            pixel = pixels[(i*imageWidth)+windowRUE];
            winRed+=((pixel>>16)&0xff);
            winGreen+=((pixel>>8)&0xff);
            winBlue+=((pixel)&0xff);

        }
    }

    /*Compute the next row of pixels*/
    void moveWindowDown(int windowX, int windowY)
    {
        /*image pixel*/
        int pixel=0;
        int initWindowCoord=(windowY*imageWidth)+windowX;
        int newRowOffset = ((windowY+BoxSz)*imageWidth)+ windowX;

        /*go through window*/
        for(int i=0;i<BoxSz;++i)
        {
            /*old row pixel*/
            pixel=pixels[initWindowCoord+i];
            winRed-=((pixel>>16)& 0xff);
            winGreen-=((pixel>>8)& 0xff);
            winBlue-=((pixel)& 0xff);
            pixel = pixels[newRowOffset+i];
            winRed+=((pixel>>16)& 0xff);
            winGreen+=((pixel>>8)& 0xff);
            winBlue+=((pixel)& 0xff);
        }

    }


    void dynamicProgLowPassFilter(Bitmap image)
    {
        int pixel = 0;
        int prevRed=0;
        int prevGreen=0;
        int prevBlue=0;
        winGreen=0;
        winRed=0;
        winBlue=0;

        for (int xc=0;xc<BoxSz; ++xc)
            for(int yc=0;yc<BoxSz;++yc)
            {
                pixel=pixels[(yc*imageWidth)+xc];
                winRed+=((pixel>>16)&0xff);
                winGreen+=((pixel>>8)&0xff);
                winBlue+=((pixel)&0xff);
            }
        this.lowPassRed+=Math.abs(((pixels[0]>>16)&0xff)-(winRed/SquBoxSz));
        this.lowPassGreen+=Math.abs(((pixels[0]>>8)&0xff)-(winGreen/SquBoxSz));
        this.lowPassBlue+=Math.abs(((pixels[0]&0xff)-(winBlue/SquBoxSz)));

        ++numPixSharp;

        prevBlue=winBlue;
        prevGreen=winGreen;
        prevRed=winRed;

        int xc =0;
        for (int yc=0; yc<imageHeight; ++yc)
        {
            for(;xc<imageWidth; ++xc)
            {
                if(xc+BoxSz < imageWidth)
                {
                    moveWindowRight(xc, yc);
                    pixel = pixels[(yc*imageWidth)+xc+1];

                    lowPassRed += Math.abs(((double)((pixel   >> 16) & 0xff) - (double)(winRed / SquBoxSz)) );
                    lowPassGreen += Math.abs(((double)((pixel   >> 8) & 0xff) - (double)(winGreen / SquBoxSz)) );
                    lowPassBlue += Math.abs(((double)((pixel & 0xff) - (double)(winBlue / SquBoxSz))));

                    ++numPixSharp;
                }
                else
                {
                    break;
                }

            }

            if (yc+BoxSz>=imageHeight)
                break;
            xc=0;
            winRed= prevRed;
            winGreen=prevGreen;
            winBlue=prevBlue;

            moveWindowDown(0, yc);
            pixel=pixels[((yc+1)*imageWidth)];

            lowPassRed+=Math.abs((double)((pixel>>16)*0xff)-(double)(winRed/SquBoxSz));
            lowPassGreen+=Math.abs((double)((pixel>>8)*0xff)-(double)(winGreen/SquBoxSz));
            lowPassBlue+=Math.abs((double)((pixel & 0xff)-(double)(winBlue/SquBoxSz)));
            ++numPixSharp;

            prevBlue=winBlue;
            prevGreen = winBlue;
            prevRed = winRed;

        }
        /*Pass pixel through filters and update the sharpness value*/
        avgSharp+=(lowPassBlue/(double)(numPixSharp));
        avgSharp+=(lowPassGreen/(double)(numPixSharp));
        avgSharp+=(lowPassRed/(double)(numPixSharp));

        /*Divide based on rgb*/
        avgSharp/=3.0;
    }
    public ImageStats(Bitmap image)
    {
        System.out.println("Number of pixels in the image: "+(image.getWidth()*image.getHeight()));
        computeStats(image);
        this.valid=true;
    }

    /*Where */
    void computeStats(Bitmap image)
    {
        /* The average red, green, and blue values */
        double red = 0, green = 0, blue = 0;

		/* The variance */
        double variance = 0.0;

		/* Normalized average luminosity */
        double normalizedAvgLum = 0;


		/* Save the width of the image */
        this.imageWidth= image.getWidth();

		/* Save the height of the image */
        this.imageHeight = image.getHeight();

		/* The square sum */
        double squareSum = 0.0;

		/* The intensity value */
        double intensity = 0.0;

        /*The perceived intensity value*/
        double pintensity = 0.0;

		/* The pixel array */
        pixels = new int[imageWidth * imageHeight];

		/* Get the pixels of the image */
        image.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);


		/* Run the image through a low-pass filter */
        dynamicProgLowPassFilter(image);

        /**
         FileOutputStream out;
         try {
         out = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/out" + i + ".png");

         image.compress(Bitmap.CompressFormat.PNG, 90, out);
         } catch (Exception e) {
         e.printStackTrace();
         } finally {
         try{
         throw new Exception();
         } catch(Throwable ignore) {}
         }
         **/




		/* The X and y coordinates in the bitmap */
        int xCoord = 0, yCoord = 0;

		/* The pixel number */
        int pixNum = 0;

		/* The RGB value of a pixel inside the low-pass filter box */
        int rgbPixelInBoxIndex = 0;

		/* The RGB pixel in the box */
        int pixelInBox = 0;

		/* The RGB values inside the low-pass filter box */
        double redInBox = 0, greenInBox = 0, blueInBox = 0;

		/* Go through the pixels */
        for(int pixelRGB : pixels)
        {
				/* Compute the X and Y coordinates of the pixel */
            yCoord = pixNum / imageWidth;
            xCoord = pixNum - (yCoord * imageWidth);

				/* Get the red component. */
            red = (pixelRGB >> 16) & 0xff;

			    /* Get the green component. */
            green = (pixelRGB >> 8) & 0xff;

			    /* Get the blue component */
            blue = (pixelRGB) & 0xff;

				/* In BW, would this pixel be black and white? */
            if(((int) (RedWt * red + GreenWt * green + BlueWt * blue)) < 128)
            {
					/* Add the x coordinate to Y coordinate */
                this.centerOfMassX += xCoord;
                this.centerOfMassY += yCoord;

                //image.setPixel(xCoord, yCoord, Color.BLACK);

            }
            //else image.setPixel(xCoord, yCoord, Color.WHITE);

				/* The intensity matrix. Note: we divide by 255 in order to normalize the intensity
				 * values so they fall within the [0..1] range.
				 */
            intensity =  ((0.2126 * red) + (0.7152 * green) + (0.0722 * blue)) / 255;

			    /* Compute and aggregate the standard luminosity of this pixel */
            this.avgStandardLum += 	intensity;


		        /* Update the square sum */
            squareSum += (intensity * intensity);



		        /* Count the pixel */
            ++pixNum;
        }
        for(int pixelRGB : pixels)
        {
				/* Compute the X and Y coordinates of the pixel */
            yCoord = pixNum / imageWidth;
            xCoord = pixNum - (yCoord * imageWidth);

				/* Get the red component. */
            red = (pixelRGB >> 16) & 0xff;

			    /* Get the green component. */
            green = (pixelRGB >> 8) & 0xff;

			    /* Get the blue component */
            blue = (pixelRGB) & 0xff;

				/* In BW, would this pixel be black and white? */
            if(((int) (RedWt * red + GreenWt * green + BlueWt * blue)) < 128)
            {
					/* Add the x coordinate to Y coordinate */
                this.centerOfMassX += xCoord;
                this.centerOfMassY += yCoord;

                //image.setPixel(xCoord, yCoord, Color.BLACK);

            }
            //else image.setPixel(xCoord, yCoord, Color.WHITE);

				/* The intensity matrix. Note: we divide by 255 in order to normalize the intensity
				 * values so they fall within the [0..1] range.
				 */
            pintensity = ((0.299*red) + (0.587*green) + (0.114*blue))/255;

			    /* Compute and aggregate the standard luminosity of this pixel */
            this.avgPerceivedLum += pintensity;

		        /* Update the square sum */
            squareSum += (intensity * intensity);



		        /* Count the pixel */
            ++pixNum;
        }

		/* Compute the average perceived luminosity */
        this.avgPerceivedLum = this.avgPerceivedLum / (imageHeight * imageWidth);

		/* Compute the average standard luminosity */
        this.avgStandardLum = this.avgStandardLum / (imageHeight * imageWidth);

		/* Compute the average contrast */
        this.avgContrast = Math.sqrt((squareSum / (imageHeight * imageWidth)) - (this.avgStandardLum * this.avgStandardLum));

		/* Compute the X coordinate of the center of mass */
        this.centerOfMassX = this.centerOfMassX / pixNum;

		/* Compute the Y coordinate of the center of mass */
        this.centerOfMassY = this.centerOfMassY / pixNum;

		/* Compute the geometric center X and y coordinates */
        this.geoCenterX = (imageHeight - 1) / 2;
        this.geoCenterY = (imageWidth - 1) / 2;


		/* Compute the Euclidean distance between the center of mass and and the geometric center */
        this.faceOrientationIndex = Math.sqrt(Math.pow(this.centerOfMassX - this.geoCenterX, 2.0) +
                Math.pow(this.centerOfMassY - this.geoCenterX, 2.0));


		/* The state of the class is valid */
        this.valid = true;

        /*Compare actual values to acceptable values to decide if image should be accepted*/
        if ((avgContrast <= GoodCont) && (avgSharp<= GoodSharp) && (avgStandardLum >= GoodLum) && (faceOrientationIndex >= GoodOrient))
        {

            result = "Acceptable image";
        }
        else result = "Unacceptable image";
    }

}

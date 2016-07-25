package com.example.matthew.imagequalityassessment;

/**
 * Created by Matthew on 4/25/2016.
 */
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by ignasi on 28/03/14.
 */
public class BitmapUtils {
    public static Bitmap forceEvenBitmapSize(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width % 2 == 1) {
            width++;
        }
        if (height % 2 == 1) {
            height++;
        }

        Bitmap fixedBitmap = original;
        if (width != original.getWidth() || height != original.getHeight()) {
            fixedBitmap = Bitmap.createScaledBitmap(original, width, height, false);
        }

//        if (fixedBitmap != original) {
//            original.recycle();
//        }

        return fixedBitmap;
    }

    public static Bitmap forceConfig565(Bitmap original) {
        Bitmap convertedBitmap = original;
        if (original.getConfig() != Bitmap.Config.RGB_565) {
            convertedBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(convertedBitmap);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            canvas.drawBitmap(original, 0, 0, paint);

//            if (convertedBitmap != original) {
//                original.recycle();
//            }
        }

        return convertedBitmap;
    }
}
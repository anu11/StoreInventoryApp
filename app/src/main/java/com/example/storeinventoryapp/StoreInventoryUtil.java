package com.example.storeinventoryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class StoreInventoryUtil {
    public static final String TAG = StoreInventoryUtil.class.getSimpleName();

    /**
     * Loading Large Bitmaps Efficiently
     * https://developer.android.com/topic/performance/graphics/load-bitmap.html
     * @param context
     * @param uri - image path
     * @param targetW
     * @param targetH
     * @return Bitmap
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri, int targetW, int targetH) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        InputStream input = null;
        try {
            input = context.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            int imageWidth = bmOptions.outWidth;
            int imageHeight = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(imageWidth / targetW, imageHeight / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(TAG, "Failed to load image", fne);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Failed to load image", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
}

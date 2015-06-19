package com.tesscordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.graphics.Matrix;

import java.io.IOException;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TessCordova extends CordovaPlugin {
    static final String IMAGE_PATH = "file:///android_asset/receipt.jpg";
    static final String LOG_TAG = "com.tesscordova";

    public TessCordova() {
        super();
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if ("coolMethod".equals(action)) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if ("tessOCR".equals(action)) {
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inSampleSize = 2;
             Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_PATH, options);

        try {
            ExifInterface exif = new ExifInterface(IMAGE_PATH);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.v(LOG_TAG, "Orient: " + exifOrientation);
            int rotate = 0;
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }
            Log.v(LOG_TAG, "Rotation: " + rotate);
            if (rotate != 0) {
                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);
                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                // tesseract req. ARGB_8888
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Rotate or coversion failed: " + e.toString());
            callbackContext.error("Rotate or coversion failed: " + e.toString());
        }

            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.init("file:///android_asset/tessdata", "eng");

            return true;
        }

        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        Log.d("tesscordova","hello from coolMethod");
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}

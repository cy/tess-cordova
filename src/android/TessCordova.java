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
import android.os.Environment;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TessCordova extends CordovaPlugin {
    static final String LOG_TAG = "com.tesscordova";
    public static final String LANG = "eng";

    public String image_path;
    public String data_path;

    public TessCordova() {
        super();
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("coolMethod".equals(action)) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if ("tessOCR".equals(action)) {
             image_path = webView.getContext().getApplicationContext().getExternalFilesDir(null).toString() + "/TessCordova/receipt.jpg";
             data_path = webView.getContext().getApplicationContext().getExternalFilesDir(null).toString() + "/TessCordova/";
             copyTrainedData();
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inSampleSize = 2;
             Bitmap bitmap = BitmapFactory.decodeFile(image_path, options);

            try {
                ExifInterface exif = new ExifInterface(image_path);
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
                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                Matrix mtx = new Matrix();
                if (rotate != 0) {
                    // Setting pre rotate
                    mtx.preRotate(rotate);
                    // Rotating Bitmap if needed
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                }
                // scale up 10x
                bitmap = Bitmap.createScaledBitmap(bitmap, w * 7, h * 7, false);
                // tesseract req. ARGB_8888
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Rotate or coversion failed: " + e.toString());
                callbackContext.error("Rotate or coversion failed: " + e.toString());
            }
            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.setDebug(true);
            baseApi.init(data_path, LANG, TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED);
            baseApi.setImage(bitmap);
            String recognizedText = baseApi.getUTF8Text();
            baseApi.end();
            callbackContext.success(recognizedText);
            return true;
        }

        return false;
    }

    private void copyTrainedData() {
        String[] paths = new String[] { data_path, data_path + "tessdata/" };
        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(LOG_TAG, "ERROR: Creation of directory " + path + "failed");
                    return;
                } else {
                    Log.v(LOG_TAG, "Created directory " + path);
                }
            }

        }
        AssetManager assetManager = this.webView.getContext().getApplicationContext().getAssets();
        try {
            String[] assets = assetManager.list("tessdata");
            for(int i = 0; i < assets.length; i++) {
                if (!(new File(data_path + "tessdata/" + assets[i])).exists()) {
                    copyFile(assetManager, "tessdata/" + assets[i], data_path + "tessdata/" + assets[i]);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error:" + e.toString());
        }
        //also copy test image
        copyFile(assetManager, "receipt.jpg", image_path);
    }

    private void copyFile(AssetManager assetManager, String assetPath, String destPath) {
        try {
            InputStream in = assetManager.open(assetPath);
            OutputStream out = new FileOutputStream(destPath);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            Log.v(LOG_TAG, "unpacked file to: " + destPath);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Was unable to copy " + e.toString());
        }
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


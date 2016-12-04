package com.rahul.merchant.merchantdemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by root on 11/20/16.
 */

public class Utility {

    private static boolean isValid = true;
    private static final int MANDATORY = 1;

    static boolean checkValidation(ViewGroup view) {
        isValid = true;
        return iterateOverAllView(view);
    }

    private static boolean iterateOverAllView(ViewGroup view) {
        for(int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);
            if(child instanceof ViewGroup) {
                iterateOverAllView((ViewGroup)child);
            }
            else if (isValid && child != null && child instanceof EditText && child.getTag() != null) {
                if ((int) child.getTag() == MANDATORY && TextUtils.isEmpty(getTextFromView(child))) {
                    child.requestFocus();
                    ((EditText) child).setError("Field cannot be empty");
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    public static boolean checkForGPSEnable(Context context) {
        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Toast.makeText(context, "Please enable location", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
        }
        return enabled;
    }

    public static String getTextFromView(View view) {
        if (view instanceof TextView)
            return ((TextView)view).getText().toString();
        else if (view instanceof EditText) {
            return ((EditText) view).getText().toString();
        }
        return "";
    }

    public static void log(String tag, String message) {
        Log.i(tag, message);
    }

    public static void log(String message) {
        Log.i("TAG", message);
    }

    public static Bitmap rotateBitMapImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static boolean checkForImageFileValidation(String path) {
        String extension = "";
        int i = path.lastIndexOf('.');
        if (i > 0)
            extension = path.substring(i+1);
        switch(extension.toLowerCase())
        {
            case "png": return true;
            case "gif": return true;
            case "tiff": return true;
            case "jpg": return true;
            case "jpeg": return true;
        }
        return false;
    }
}

package com.example.eventapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageStorageManager {
    private static final String TAG = "ImageStorageManager";
    private static final String IMAGE_DIR = "event_images";

    public static String saveImage(Context context, Uri imageUri) throws IOException {
        File directory = new File(context.getFilesDir(), IMAGE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "event_" + System.currentTimeMillis() + ".jpg";
        File file = new File(directory, fileName);

        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
             FileOutputStream outputStream = new FileOutputStream(file)) {

            if (inputStream == null) {
                throw new IOException("Failed to open input stream");
            }

            // Copy the file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
            throw e;
        }
    }

    public static Bitmap loadImage(String path) {
        return BitmapFactory.decodeFile(path);
    }

    public static void deleteImage(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
} 
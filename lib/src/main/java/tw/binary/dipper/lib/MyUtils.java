package tw.binary.dipper.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;

import com.google.api.client.util.IOUtils;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.UUID;

// Created by eason on 2015/1/22.

public class MyUtils {
    /*
     * @param bitmap
     * @return converting bitmap and return a string
     */
    public static String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    /*
     * @param encodedString
     * @return bitmap (from given string)
     */
    public static Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    // Json to Object
    public static Object jsonToObj(String s) {
        return new Gson().fromJson(s, Object.class);
    }

    //Object to Json
    public static String objToJson(Object obj) {
        return new Gson().toJson(obj);
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public static String saveImageFile(Context pContext, Bitmap finalBitmap) {
        File myDir = new File(pContext.getFilesDir().getPath());
        myDir.mkdirs();
        Random generator = new Random();
        int n = 99999999;
        n = generator.nextInt(n);
        String filename = "img" + n + ".jpg";
        File file = new File(myDir, filename);
        if (file.exists()) file.delete();
        try {
            FileOutputStream fo = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fo);
            fo.flush();
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            filename = null;

        }
        return filename;
    }

    public static Bitmap readImageFile(Context pContext, String fname) {
        File myDir = new File(pContext.getFilesDir().getAbsolutePath());
        File file = new File(myDir, fname);
        if (!file.exists()) return null; //No file exist
        try {
            FileInputStream fi = new FileInputStream(file);
            Bitmap bmp = BitmapFactory.decodeStream(fi);
            fi.close();
            return bmp;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File stream2file(InputStream in) throws IOException {
        final String PREFIX = "stream2file";
        final String SUFFIX = ".tmp";

        final File tempFile = File.createTempFile(PREFIX, SUFFIX);
        tempFile.deleteOnExit();

        FileOutputStream out = new FileOutputStream(tempFile);
        IOUtils.copy(in, out);
        return tempFile;
    }

    public static boolean string2File(Context pContext, String data, String filename) {
        FileOutputStream outputStream;
        String path = pContext.getFilesDir().getPath();
        try {
            outputStream = pContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean obj2File(Context pContext, Object obj, String filename) {

        try {
            FileOutputStream fileOutputStream = pContext.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.close();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean removeFile(Context pContext, String filename) {
        File file = new File(pContext.getFilesDir().getPath(), filename);
        if (file.exists()) {
            if (!file.delete())
                return false;
        }
        return true;
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String currentTimeStamp() {
        java.util.Date date = new java.util.Date();
        return new String(String.valueOf(date.getTime()));
    }

    public static void toastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}

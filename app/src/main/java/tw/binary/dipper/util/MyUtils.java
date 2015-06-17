package tw.binary.dipper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.DisplayMetrics;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;

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
        Gson gson = new Gson();

        return gson.fromJson(s, Object.class);
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

    public static boolean saveImageFile(Context pContext, Bitmap finalBitmap, String filename) {
        File myDir = new File(pContext.getFilesDir().getPath());
        myDir.mkdirs();

        File file = new File(myDir, filename);
        if (file.exists()) file.delete();
        try {
            FileOutputStream fo = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fo);
            fo.flush();
            fo.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    public static String appDir(Context context) {
        return "file:" + context.getFilesDir().getAbsolutePath() + "/";
    }

    public static File appFileDir(Context context, String localFilename) {
        File file = new File(context.getFilesDir().getAbsolutePath() + "/" + localFilename);
        return file;
    }

    public static String stream2string(InputStream pInputStream) throws IOException {
        return IOUtils.toString(pInputStream, "UTF-8");
    }

    public static int dpToPx(Context pContext, int dp) {
        DisplayMetrics displayMetrics = pContext.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int pxToDp(Context pContext, int px) {
        DisplayMetrics displayMetrics = pContext.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    //攝氏轉華氏
    public static int temperatueC2F(int c) {
        return (int) ((float) c * (9.0 / 5.0)) + 32;
    }

    //華氏轉攝氏
    public static int tempertureF2C(int f) {
        return (int) ((5.0 / 9.0) * (float) (f - 32));
    }

    public static boolean isFileExist(Context pContext, String fname) {
        File myDir = new File(pContext.getFilesDir().getAbsolutePath());
        File file = new File(myDir, fname);
        return file.exists() && file.length() > 0;
    }

}

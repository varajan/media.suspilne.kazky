package media.suspilne.kazky;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class SettingsHelper {
    public static String application = "Kazka";

    public static String getString(Context context, String setting){
        return getString(context, setting, "");
    }

    public static String getString(Context context, String setting, String defaultValue){
        return context.getSharedPreferences(application,0).getString(setting, defaultValue);
    }

    public static void setString(Context context, String setting, String value){
        SharedPreferences.Editor editor = context.getSharedPreferences(application, 0).edit();
        editor.putString(setting, value);
        editor.commit();
    }

    public static ArrayList<Integer> getSavedTaleIds(Context context){
        ArrayList<Integer> readers = new ArrayList<>();
        ArrayList<Integer> titles  = new ArrayList<>();
        ArrayList<Integer> result;

        for (String reader:getAllSettings(context, "reader-")){
            readers.add(Integer.parseInt(reader.split("-")[1]));
        }

        for (String title:getAllSettings(context, "title-")){
            titles.add(Integer.parseInt(title.split("-")[1]));
        }

        result = ListHelper.intersect(readers, titles);
        Collections.sort(result);

        return result;
    }

    public static ArrayList<Integer> getTaleReaderIds(Context context){
        ArrayList<Integer> readers = new ArrayList<>();

        for (String reader:getAllSettings(context, "readerName-")){
            readers.add(Integer.parseInt(reader.split("-")[1]));
        }
        Collections.sort(readers);

        return readers;
    }

    public static ArrayList<String> getAllSettings(Context context, String setting){
        ArrayList<String> result = new ArrayList<>();

        Map<String, ?> allEntries = context.getSharedPreferences(application, 0).getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains(setting)) {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    public static boolean getBoolean(Context context, String setting){
        return getString(context, setting).toLowerCase().equals("true");
    }

    public static void setBoolean(Context context, String setting, boolean value){
        setString(context, setting, String.valueOf(value));
    }

    public static int getInt(Context context, String setting, int defaultValue){
        return Integer.parseInt(getString(context, setting, String.valueOf(defaultValue)));
    }

    public static int getInt(Context context, String setting){
        return Integer.parseInt(getString(context, setting, "0"));
    }

    public static void setInt(Context context, String setting, int value){
        setString(context, setting, String.valueOf(value));
    }

    public static long getLong(Context context, String setting){
        return Long.parseLong(getString(context, setting, "0"));
    }

    public static void setLong(Context context, String setting, long value){
        setString(context, setting, String.valueOf(value));
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void deleteFile(Context context, String name){
        context.deleteFile(name);
    }

    public static String[] getFileNames(Context context){
        return context.fileList();
    }

    public static Boolean fileExists(Context context, String name){
        return context.getFileStreamPath(name).exists();
    }

    public static Boolean taleExists(Context context, int id){
        return fileExists(context, String.format("%02d.mp3", id));
    }

    public static void saveImage(Context context, String name, Drawable drawable){
        try {
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();

            saveFile(context, name, bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(Context context, String name, byte[] bytes){
        try {
            FileOutputStream outputStream;
            outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Drawable getImage(Context context, String name){
        try {
            FileInputStream stream = context.openFileInput(name);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();

            return new BitmapDrawable(bitmap);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static long freeSpace(){
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable;

        if (Build.VERSION.SDK_INT < 18) {
            bytesAvailable = stat.getAvailableBlocks() * stat.getBlockSize();
        } else {
            bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        }

        return bytesAvailable / (1024 * 1024);
    }

    public static String getVersionName(Context context){
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }
}
package media.suspilne.kazky;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class HSettings {
    public static String application = "Kazka";

    public static String getString(String setting){
        return getString(setting, "");
    }

    public static String getString(String setting, String defaultValue){
        return ActivityBase.getActivity().getSharedPreferences(application,0).getString(setting, defaultValue);
    }

    public static void setString(String setting, String value){
        SharedPreferences.Editor editor = ActivityBase.getActivity().getSharedPreferences(application, 0).edit();
        editor.putString(setting, value);
        editor.commit();
    }

    public static ArrayList<Integer> getSavedTaleIds(){
        ArrayList<Integer> readers = new ArrayList<>();
        ArrayList<Integer> titles  = new ArrayList<>();
        ArrayList<Integer> result;

        for (String reader:getAllSettings("reader-")){
            readers.add(Integer.parseInt(reader.split("-")[1]));
        }

        for (String title:getAllSettings("title-")){
            titles.add(Integer.parseInt(title.split("-")[1]));
        }

        result = HList.intersect(readers, titles);
        Collections.sort(result);

        return result;
    }

    public static ArrayList<Integer> getTaleReaderIds(Context context){
        ArrayList<Integer> readers = new ArrayList<>();

        for (String reader:getAllSettings("readerName-")){
            readers.add(Integer.parseInt(reader.split("-")[1]));
        }
        Collections.sort(readers);

        return readers;
    }

    public static ArrayList<String> getAllSettings(String setting){
        ArrayList<String> result = new ArrayList<>();

        Map<String, ?> allEntries = ActivityBase.getActivity().getSharedPreferences(application, 0).getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains(setting)) {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    public static boolean getBoolean(String setting){
        return getString(setting).toLowerCase().equals("true");
    }

    public static void setBoolean(String setting, boolean value){
        setString(setting, String.valueOf(value));
    }

    public static int getInt(String setting, int defaultValue){
        return Integer.parseInt(getString(setting, String.valueOf(defaultValue)));
    }

    public static int getInt(String setting){
        return Integer.parseInt(getString(setting, "0"));
    }

    public static void setInt(String setting, int value){
        setString(setting, String.valueOf(value));
    }

    public static long getLong(String setting){
        return Long.parseLong(getString(setting, "0"));
    }

    public static void setLong(String setting, long value){
        setString(setting, String.valueOf(value));
    }

    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = ActivityBase.getActivity().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(int px) {
        DisplayMetrics displayMetrics = ActivityBase.getActivity().getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void deleteFile(String name){
        ActivityBase.getActivity().deleteFile(name);
    }

    public static String[] getFileNames(Context context){
        return context.fileList();
    }

    public static Boolean fileExists(String name){
        return ActivityBase.getActivity().getFileStreamPath(name).exists();
    }

    public static Boolean taleExists(int id){
        return fileExists(String.format("%02d.mp3", id));
    }

    public static void saveImage(String name, Drawable drawable){
        try {
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();

            saveFile(name, bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(String name, byte[] bytes){
        try {
            FileOutputStream outputStream;
            outputStream = ActivityBase.getActivity().openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Drawable getImage(String name){
        try {
            FileInputStream stream = ActivityBase.getActivity().openFileInput(name);
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

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) ActivityBase.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
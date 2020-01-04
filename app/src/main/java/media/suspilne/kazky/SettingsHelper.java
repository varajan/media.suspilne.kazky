package media.suspilne.kazky;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class SettingsHelper {
    static String application = "media.suspilne.kazky";

    public static void setColor(int color) { setInt("tales.text.color", color); }

    public static int getColor() { return getColor(false); }
    public static int getCustomColor() { return getColor(true); }

    private static int getColor(boolean custom) {
        return getBoolean("use.font.color") || custom
                ? getInt("tales.text.color", ContextCompat.getColor(ActivityMain.getActivity(), R.color.white))
                : ContextCompat.getColor(ActivityMain.getActivity(), R.color.white);
    }

    static String getString(String setting){
        return getString(setting, "");
    }

    static String getString(String setting, String defaultValue){
        return getString(ActivityMain.getActivity(), setting, defaultValue);
    }

    static String getString(Context context, String setting, String defaultValue){
        return context.getSharedPreferences(application,0).getString(setting, defaultValue);
    }

    static void setString(String setting, String value){
        try{
            SharedPreferences.Editor editor = ActivityMain.getActivity().getSharedPreferences(application, 0).edit();
            editor.putString(setting, value);
            editor.apply();
        }
        catch (Exception e){
            /*nothing*/
        }
    }

    public static ArrayList<String> getAllSettings(String setting){
        ArrayList<String> result = new ArrayList<>();

        Map<String, ?> allEntries = ActivityMain.getActivity().getSharedPreferences(application, 0).getAll();
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
        DisplayMetrics displayMetrics = ActivityMain.getActivity().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(int px) {
        DisplayMetrics displayMetrics = ActivityMain.getActivity().getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static Boolean fileExists(String name){
        return ActivityMain.getActivity().getFileStreamPath(name).exists();
    }

    public static void saveImage(String name, Drawable drawable){
        try {
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();

            saveFile( name, bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(String name, byte[] bytes){
        try {
            FileOutputStream outputStream;
            outputStream = ActivityMain.getActivity().openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Drawable getImage(String name){
        try {
            FileInputStream stream = ActivityMain.getActivity().openFileInput(name);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();

            return new BitmapDrawable(bitmap);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public static String formattedSize(long size){
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static long usedSpace() {
        return folderSize(ActivityMain.getActivity().getFilesDir());
    }

    public static long freeSpace(){
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());

        return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
    }

    public static String getVersionName(){
        try {
            return ActivityMain.getActivity().getPackageManager()
                    .getPackageInfo(ActivityMain.getActivity().getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }
}
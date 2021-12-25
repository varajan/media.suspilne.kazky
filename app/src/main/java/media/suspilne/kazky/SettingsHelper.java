package media.suspilne.kazky;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.List;

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

    public static boolean getBoolean(String setting){
        try{
            return getString(setting).equalsIgnoreCase("true");
        }
        catch (Exception e){
            return false;
        }
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

    public static long folderSize(File directory) {
        File[] files = directory.listFiles();

        if (files == null) return 0;

        long length = 0;
        for (File file : files) {
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

    public static String getFacebookPageURL(String url) {
        PackageManager packageManager = ActivityMain.getActivity().getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            String fb = (versionCode >= 3002850) ? "fb://facewebmodal/f?href=" : "fb://page/";
            return fb + url;
        } catch (PackageManager.NameNotFoundException e) {
            return url;
        }
    }

    public static boolean isIntentAvailable(Intent intent) {
        final PackageManager packageManager = ActivityMain.getActivity().getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
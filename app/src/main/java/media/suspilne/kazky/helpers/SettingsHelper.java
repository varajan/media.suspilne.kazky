package media.suspilne.kazky.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.R;
import media.suspilne.kazky.activities.MainActivity;

public class SettingsHelper {
    static public int timeout = 10_000;

    public static void setColor(int color) { setInt(Kazky.Constants.talesTextColor, color); }

    public static int getColor() { return getColor(false); }
    public static int getCustomColor() { return getColor(true); }

    private static int getColor(boolean custom) {
        return getBoolean(Kazky.Constants.useFontColor) || custom
                ? getInt(Kazky.Constants.talesTextColor, ContextCompat.getColor(MainActivity.getActivity(), R.color.white))
                : ContextCompat.getColor(MainActivity.getActivity(), R.color.white);
    }

    public static String getString(String setting) {
        return getString(setting, "");
    }

    public static String getString(String setting, String defaultValue) {
        return getString(MainActivity.getActivity(), setting, defaultValue);
    }

    public static String getString(Context context, String setting, String defaultValue) {
        return context.getSharedPreferences(Kazky.Constants.application,0).getString(setting, defaultValue);
    }

    public static void setString(String setting, String value) {
        try {
            SharedPreferences.Editor editor = MainActivity.getActivity().getSharedPreferences(Kazky.Constants.application, 0).edit();
            editor.putString(setting, value);
            editor.apply();
        }
        catch (Exception e) {
            /*nothing*/
        }
    }

    public static boolean getBoolean(String setting) {
        return getBoolean(setting, false);
    }

    public static boolean getBoolean(String setting, boolean defaultValue) {
        try {
            return getString(setting, String.valueOf(defaultValue)).equalsIgnoreCase("true");
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public static void setBoolean(String setting, boolean value) {
        setString(setting, String.valueOf(value));
    }

    public static int getInt(String setting, int defaultValue) {
        return Integer.parseInt(getString(setting, String.valueOf(defaultValue)));
    }

    public static int getInt(String setting) {
        return Integer.parseInt(getString(setting, "0"));
    }

    public static void setInt(String setting, int value) {
        setString(setting, String.valueOf(value));
    }

    public static long getLong(String setting) {
        return Long.parseLong(getString(setting, "0"));
    }

    public static void setLong(String setting, long value) {
        setString(setting, String.valueOf(value));
    }

    public static void saveFile(String name, byte[] bytes) {
        try {
            FileOutputStream outputStream;
            outputStream = MainActivity.getActivity().openFileOutput(name, Context.MODE_PRIVATE);
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

    public static long usedSpace() {
        return folderSize(MainActivity.getActivity().getFilesDir());
    }

    public static long freeSpace() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());

        return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
    }

    public static String getVersionName() {
        try {
            return MainActivity.getActivity().getPackageManager()
                    .getPackageInfo(MainActivity.getActivity().getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }
}
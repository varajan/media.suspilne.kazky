package media.suspilne.kazky;

import android.app.Activity;
import android.content.SharedPreferences;

public class SettingsHelper {
    private static String application = "Kazka";

    public static String getString(Activity activity, String setting){
        return getString(activity, setting, "");
    }

    public static String getString(Activity activity, String setting, String defaultValue){
        return activity.getSharedPreferences(application,0).getString(setting,defaultValue);
    }

    public static void setString(Activity activity, String setting, String value){
        SharedPreferences.Editor editor = activity.getSharedPreferences(application, 0).edit();
        editor.putString(setting, value);
        editor.commit();
    }

    public static boolean getBoolean(Activity activity, String setting){
        return getString(activity, setting).toLowerCase() == "true";
    }

    public static void setBoolean(Activity activity, String setting, boolean value){
        setString(activity, setting, String.valueOf(value));
    }

    public static int getInt(Activity activity, String setting){
        return Integer.parseInt(getString(activity, setting, "0"));
    }

    public static void setInt(Activity activity, String setting, int value){
        setString(activity, setting, String.valueOf(value));
    }
}

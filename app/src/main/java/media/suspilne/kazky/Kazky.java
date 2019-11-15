package media.suspilne.kazky;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Kazky extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = getSharedPreferences(SettingsHelper.application, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // upgrade from old version, previous setting name was 'talesDownload'
        boolean talesDownload = sharedPreferences.getString("talesDownload", "false").toLowerCase().equals("true");
        if (talesDownload){
            editor.putString("talesDownload", String.valueOf(false));
            editor.putString("downloadAllTales", String.valueOf(true));
            editor.putString("downloadFavoriteTales", String.valueOf(true));
        }

        editor.putString("tales.paused", String.valueOf(false));
        editor.putString("tales.lastPlaying", String.valueOf(-1));
        editor.putString("tales.nowPlaying", String.valueOf(-1));
        editor.putString("talesFilter", "");
        editor.putString("errorMessage", "");
        editor.apply();
    }

    public static void logError(String message, boolean logStackTrace){
        if (logStackTrace){
            logStackTrace(message);
        } else {
            Log.e(SettingsHelper.application, message);
        }
    }

    public static void logError(String message){
        logError(message, true);
    }

    private static void logStackTrace(String message){
        StringBuilder stackTrace = new StringBuilder(message + "\r\n");

        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            stackTrace.append(ste).append("\r\n");
        }

        Log.e(SettingsHelper.application, stackTrace.toString());
    }
}

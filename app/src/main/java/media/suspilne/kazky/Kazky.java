package media.suspilne.kazky;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class Kazky extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = getSharedPreferences(SettingsHelper.application, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("checkForUpdates", String.valueOf(true));
        editor.putString("readSettingsFromGit", String.valueOf(true));
        editor.putString("tales.count.updated", String.valueOf(false));
        editor.putString("tales.paused", String.valueOf(false));
        editor.putString("tales.lastPlaying", String.valueOf(-1));
        editor.putString("tales.nowPlaying", String.valueOf(-1));
        editor.putString("stopPlaybackOnTimeout", String.valueOf(false));
        editor.putString("showKidsTales", sharedPreferences.getString("showKidsTales", "true"));
        editor.putString("showBabiesTales", sharedPreferences.getString("showBabiesTales", "true"));
        editor.putString("showLullabies", sharedPreferences.getString("showLullabies", "true"));
        editor.putString("sortAsc", sharedPreferences.getString("sortAsc", "true"));
        editor.putString("skipIntro", sharedPreferences.getString("skipIntro", "true"));
        editor.putString("talesFilter", "");
        editor.putString("talesList", "");
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

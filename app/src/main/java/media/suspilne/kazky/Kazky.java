package media.suspilne.kazky;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import media.suspilne.kazky.helpers.SettingsHelper;

public class Kazky extends Application {
    public static class Constants {
        public static String checkForNotifications = "checkForNotifications";
        public static String checkForUpdates = "checkForUpdates";
        public static String readSettingsFromGit = "readSettingsFromGit";

        public static String talesPaused = "tales.paused";
        public static String talesLastPlaying = "tales.lastPlaying";
        public static String talesNowPlaying = "tales.nowPlaying";
        public static String talesList = "talesList";
        public static String stopPlaybackOnTimeout = "stopPlaybackOnTimeout";

        public static String errorMessage = "errorMessage";
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = getSharedPreferences(SettingsHelper.application, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(Constants.checkForNotifications, String.valueOf(true));
        editor.putString(Constants.checkForUpdates, String.valueOf(true));
        editor.putString(Constants.readSettingsFromGit, String.valueOf(true));
        editor.putString(Constants.talesPaused, String.valueOf(false));
        editor.putString(Constants.talesLastPlaying, String.valueOf(-1));
        editor.putString(Constants.talesNowPlaying, String.valueOf(-1));
        editor.putString(Constants.stopPlaybackOnTimeout, String.valueOf(false));
        editor.putString(Constants.talesList, "");
        editor.putString(Constants.errorMessage, "");

        editor.apply();
    }

    public static void logError(String message, boolean logStackTrace) {
        if (logStackTrace) {
            logStackTrace(message);
        } else {
            Log.e(SettingsHelper.application, message);
        }
    }

    public static void logError(String message) {
        logError(message, true);
    }

    private static void logStackTrace(String message) {
        StringBuilder stackTrace = new StringBuilder(message + "\r\n");

        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            stackTrace.append(ste).append("\r\n");
        }

        Log.e(SettingsHelper.application, stackTrace.toString());
    }
}

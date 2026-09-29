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
        public static String latestVersion = "LatestVersion";
        public static String version = "version";
        public static String whatsNew = "whatsNew";

        public static String downloadFavoriteTales = "downloadFavoriteTales";
        public static String downloadAllTales = "downloadAllTales";
        public static String suggestToDownloadFavoriteTales = " suggestToDownloadFavoriteTales";

        public static String streamType = "StreamType";
        public static String talesPaused = "tales.paused";
        public static String talesLastPlaying = "tales.lastPlaying";
        public static String talesNowPlaying = "tales.nowPlaying";
        public static String talesList = "talesList";
        public static String taleIdExtra = "tale.id";
        public static String typeExtra = "type";
        public static String stopPlaybackOnTimeout = "stopPlaybackOnTimeout";
        public static String filteredTalesList = "filteredTalesList";
        public static String showBigImages = "showBigImages";

        public static String errorMessage = "errorMessage";

        public static String autoQuit = "autoQuit";
        public static String timeout = "timeout";
        public static String volumeControl = "volumeControl";
        public static String volumeMinutes = "volumeMinutes";

        public static final String codeStopPlay = "StopPlay";
        public static final String codeSetPlayBtnIcon = "SetPlayBtnIcon";
        public static final String codeSourceIsNotAccessible = "SourceIsNotAccessible";

        public static String returnToReaders = "returnToReaders";
    }

    // Kazky.Constants.showBigImages
    // Kazky.Constants.typeExtra

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

package media.suspilne.kazky;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NotificationManager notificationManager;
    private Timer quitTimer;
    private Timer volumeReduceTimer;

    protected NavigationView navigation;
    protected TextView activityTitle;
    protected int currentView;

    private static Activity activity;
    public static Activity getActivity(){ return activity; }

    private void stopVolumeReduceTimer(){
        if (volumeReduceTimer != null) {
            volumeReduceTimer.cancel();
            volumeReduceTimer = null;
        }
    }

    private void stopQuitTimer(){
        if (quitTimer != null) {
            quitTimer.cancel();
            quitTimer = null;
        }

        SettingsHelper.setBoolean("stopPlaybackOnTimeout", false);
    }

    protected void resetVolumeReduceTimer(){
        stopVolumeReduceTimer();
        if (!SettingsHelper.getBoolean("volumeControl")) return;
        if (!isTalePlaying() && !isRadioPlaying()) return;

        int timeout = SettingsHelper.getInt("volumeMinutes");
        timeout = timeout == 0 ? 5 : timeout;

        volumeReduceTimer = new Timer();
        volumeReduceTimer.schedule(new reduceVolume(), timeout * 60 * 1000);
    }

    protected void resetQuitTimeout(){
        if (SettingsHelper.getBoolean("autoQuit")) {
            stopQuitTimer();

            int timeout = SettingsHelper.getInt("timeout");
            timeout = timeout==0 ? 5 : timeout;

            quitTimer = new Timer();
            quitTimer.schedule(new stopPlaybackOnTimeout(), timeout * 60 * 1000);
        } else {
            stopQuitTimer();
        }
    }

    class stopPlaybackOnTimeout extends TimerTask {
        @Override
        public void run() {
            stopVolumeReduceTimer();

            if (isRadioPlaying()){
                Intent intent = new Intent();
                intent.setAction(SettingsHelper.application);
                intent.putExtra("code", "StopPlay");
                sendBroadcast(intent);
            }
            else{
                SettingsHelper.setBoolean("stopPlaybackOnTimeout", true);
            }
        }
    }

    class reduceVolume extends TimerTask {
        @Override
        public void run(){
            MediaVolume media = new MediaVolume();

            if (media.getLevel() > 1) {
                media.setLevel(media.getLevel() - 1);
                resetVolumeReduceTimer();
            }else{
                media.setLevel(media.getMaxLevel() / 2);
                stopVolumeReduceTimer();
                stopPlayerService();

                Tales.setNowPlaying(-1);
                Intent intent = new Intent();
                intent.setAction(SettingsHelper.application);
                intent.putExtra("code", "SetPlayBtnIcon");
                sendBroadcast(intent);
            }
        }
    }

    protected boolean isTalePlaying(){
        return isServiceRunning(PlayerService.class)
                && SettingsHelper.getString("StreamType").equals(getString(R.string.tales))
                && !Tales.isPaused();
    }

    protected boolean isRadioPlaying(){
        return isServiceRunning(PlayerService.class)
                && SettingsHelper.getString("StreamType").equals(getString(R.string.radio))
                && !Tales.isPaused();
    }

    protected boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void setIsRadioAvailable(){
        if (!SettingsHelper.getBoolean("checkForRadio")) return;

        new Thread(() -> {
            ArrayList<String> settings = new ArrayList<>();

            try {
                String url = "https://raw.githubusercontent.com/varajan/media.suspilne.kazky/master/app/src/main/res/settings";
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(15000);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String str;
                while ((str = in.readLine()) != null) {
                    settings.add(str);
                }
                in.close();
            } catch (Exception e) {
                Log.d("MyTag",e.toString());
            }

            SettingsHelper.setBoolean("radioIsAvailable", !settings.contains("radioIsAvailable:false"));
            SettingsHelper.setBoolean("checkForRadio", false);
        }).start();
    }

    protected boolean isRadioAvailable(){ return SettingsHelper.getBoolean("radioIsAvailable"); }

    protected boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected() && isNetworkSpeedOk();
    }

    private boolean isNetworkSpeedOk() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager != null
                ? connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork())
                : null;

        int downSpeed = networkCapabilities != null ? networkCapabilities.getLinkDownstreamBandwidthKbps() : 0;
        int upSpeed   = networkCapabilities != null ? networkCapabilities.getLinkUpstreamBandwidthKbps() : 0;

        return downSpeed > 20_000 && upSpeed > 10_000;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityMain.activity = this;

        switch (currentView){
            case R.id.tales_menu:
            case R.id.coloring_menu:
                setContentView(R.layout.activity_tales);
                break;

            case R.id.radio_menu:
                setContentView(R.layout.activity_radio);
                break;

            case R.id.readers_menu:
                setContentView(R.layout.activity_readers);
                break;

            case R.id.settings_menu:
                setContentView(R.layout.activity_settings);
                break;

            case R.id.info_menu:
                setContentView(R.layout.activity_info);
                break;
        }

        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activityTitle = findViewById(R.id.title);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigation = findViewById(R.id.nav_view);
        navigation.setNavigationItemSelectedListener(this);
        navigation.setCheckedItem(currentView);

        setTitle();
        resetQuitTimeout();
        showErrorMessage();
        updateTalesCountPerReader();
        checkForUpdates();
    }

    @Override
    protected void onResume(){
        super.onResume();
        showErrorMessage();

        ActivityMain.activity = this;
    }

    private void showErrorMessage(){
        String errorMessage = SettingsHelper.getString("errorMessage");

        if (!errorMessage.isEmpty()){
            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

            showAlert(getString(R.string.an_error_occurred), errorMessage);
            notificationManager.cancel(DownloadTask.WITH_ERROR);
            SettingsHelper.setString("errorMessage", "");
        }
    }

    private void exit(){
        moveTaskToBack(true);
        stopVolumeReduceTimer();
        stopPlayerService();
        System.exit(1);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            showQuitDialog();
        }
    }

    private void showQuitDialog(){
        new AlertDialog.Builder(this)
            .setIcon(R.mipmap.logo)
            .setTitle(R.string.confirm_exit)
            .setPositiveButton(R.string.yes, (dialog, which) -> exit())
            .setNegativeButton(R.string.no, null)
            .show();
    }

    protected void setTitle() {
        String title = navigation.getMenu().findItem(currentView).getTitle().toString();
        activityTitle.setText(title);
    }

    protected void stopPlayerService(){
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        stopService(new Intent(this, PlayerService.class));
        try {
            notificationManager.cancelAll();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void openActivity(Class view){
        Intent intent = new Intent(this, view);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.radio_menu:
                if (currentView != R.id.radio_menu) {
                    openActivity(ActivityRadio.class);
                }
                break;

            case R.id.tales_menu:
                if (currentView != R.id.tales_menu) {
                    openActivity(ActivityTales.class);
                }
                break;

            case R.id.readers_menu:
                if (currentView != R.id.readers_menu) {
                    openActivity(ActivityReaders.class);
                }
                break;

            case R.id.settings_menu:
                if (currentView != R.id.settings_menu) {
                    openActivity(ActivitySettings.class);
                }
                break;

            case R.id.coloring_menu:
                if (currentView != R.id.coloring_menu) {
                    openActivity(ActivityColorings.class);
                }
                break;

            case R.id.info_menu:
                if (currentView != R.id.info_menu) {
                    openActivity(ActivityInfo.class);
                }
                break;

            case R.id.rate_menu:
                rateApp();
                break;

            case R.id.exit_menu:
                showQuitDialog();
                break;
        }

        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return false;
    }

    private void rateApp(){
        try {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            startActivity(goToMarket);
        } catch (ActivityNotFoundException anfe) {
            try{
                anfe.printStackTrace();

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    void download(){
        if (!this.isNetworkAvailable()){
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        } else {
            boolean onlyFavorite = SettingsHelper.getBoolean("downloadFavoriteTales") && !SettingsHelper.getBoolean("downloadAllTales");
            Tale[] download = new Tales().getTalesList(onlyFavorite).toArray(new Tale[0]);

            SettingsHelper.setBoolean("checkForUpdates", false);
            new DownloadTask().execute(download);
        }
    }

    public void showAlert(String title, String message){
        new AlertDialog.Builder(this)
            .setIcon(R.mipmap.logo)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton(R.string.ok, null)
            .show();
    }

    protected void continueDownloadTales(){
        if (!SettingsHelper.getBoolean("downloadAllTales") && !SettingsHelper.getBoolean("downloadFavoriteTales")) return;
        if (SettingsHelper.freeSpace() < 150 || !isNetworkAvailable()) return;

        boolean allAreDownloaded = true;
        boolean onlyFavorite = SettingsHelper.getBoolean("downloadFavoriteTales") && !SettingsHelper.getBoolean("downloadAllTales");

        for (Tale tale : new Tales().getTalesList()){
            if ((!onlyFavorite || tale.isFavorite) && !tale.isDownloaded){
                allAreDownloaded = false;
                break;
            }
        }

        if (!allAreDownloaded) download();
    }

    protected void suggestToDownloadFavoriteTales(){
        if (SettingsHelper.getBoolean("suggestToDownloadFavoriteTales")) return;
        if (SettingsHelper.getBoolean("downloadAllTales") || SettingsHelper.getBoolean("downloadFavoriteTales")) return;
        if (SettingsHelper.freeSpace() < 150 || !isNetworkAvailable()) return;

        int favorites = new Tales().getTalesList(true).size();
        if (favorites < 5) return;

        SettingsHelper.setBoolean("suggestToDownloadFavoriteTales", true);

        new AlertDialog.Builder(ActivityMain.this)
            .setIcon(R.mipmap.logo)
            .setTitle(R.string.download)
            .setMessage(getString(R.string.suggestToDownloadFavorite, favorites))
            .setPositiveButton(R.string.download, (dialog, which) -> {SettingsHelper.setBoolean("downloadFavoriteTales", true); download();})
            .setNegativeButton(R.string.no, null)
            .show();
    }

    private void updateTalesCountPerReader(){
        if (Tales.getTalesCountUpdated()) return;

        boolean showBabyTales = Tales.getShowForBabies();
        boolean showKidsTales = Tales.getShowForKids();
        boolean showFavorite = Tales.getShowOnlyFavorite();

        for (Reader reader: new Readers().Readers) {
            int count = 0;

            for (Tale tale : new Tales().getTalesList()) {
                if (!tale.getReader().equals(reader.getName()))         continue;
                if (showFavorite && !tale.isFavorite)                   continue;
                if (!showBabyTales && tale.age == TaleAge.FOR_BABIES)   continue;
                if (!showKidsTales && tale.age == TaleAge.FOR_KIDS)     continue;

                count++;
            }

            SettingsHelper.setInt(reader.getName(), count);
        }

        Tales.setTalesCountUpdated(true);
    }

    private void update(){
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/apps")));
        } catch (ActivityNotFoundException e) {
            rateApp();
        }
    }

    private void checkForUpdates(){
        if (!SettingsHelper.getBoolean("checkForUpdates")) return;
        if (!this.isNetworkAvailable()) return;

        try {
            SettingsHelper.setBoolean("checkForUpdates", false);

            String latestVersion = new VersionChecker().execute().get();
            String whatsNew = new WhatsNewChecker().execute().get();
            String currentVersion = SettingsHelper.getVersionName();
            String loggedVersion = SettingsHelper.getString("LatestVersion", currentVersion);

            if (!latestVersion.equals(currentVersion) && !latestVersion.equals(loggedVersion) ){
                    SettingsHelper.setString("LatestVersion", latestVersion);

                new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.logo)
                        .setTitle(getString(R.string.newVersion, latestVersion))
                        .setMessage(whatsNew)
                        .setPositiveButton(R.string.update, (dialog, which) -> update())
                        .setNegativeButton(R.string.cancel, null)
                        .setOnDismissListener(null)
                        .show();
            }
        } catch (Exception e) {
            SettingsHelper.setBoolean("checkForUpdates", false);
            e.printStackTrace();
        }
    }

    public class VersionChecker extends AsyncTask<String, String, String> {
        String newVersion;

        @Override
        protected String doInBackground(String... params) {

            try {
                newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "&hl=en")
                        .timeout(15000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                        .first()
                        .ownText();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return newVersion;
        }
    }

    public class WhatsNewChecker extends AsyncTask<String, String, String> {
        String whatsNew;

        @Override
        protected String doInBackground(String... params) {

            try {
                whatsNew = Jsoup.connect("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "&hl=en")
                        .timeout(15000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div[itemprop='description'] span")
                        .last()
                        .text()
                        .replace(". ", ".\n• ")
                        .trim();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "• " + whatsNew;
        }
    }
}
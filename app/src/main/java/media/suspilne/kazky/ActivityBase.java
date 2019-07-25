package media.suspilne.kazky;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("Registered")
public class ActivityBase extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Timer quitTimer;
    protected NavigationView navigation;
    protected int currentView;

    private static Activity activity;
    public static Activity getActivity(){ return activity; }

    private NotificationManager getNotificationManager(){
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    protected void setQuiteTimeout(){
        if (HSettings.getBoolean("autoQuit")) {
            if (quitTimer != null) quitTimer.cancel();
            int timeout = HSettings.getInt("timeout");

            quitTimer = new Timer();
            quitTimer.schedule(new stopRadioOnTimeout(), timeout * 60 * 1000);
        } else {
            if (quitTimer != null) quitTimer.cancel();
        }
    }

    class stopRadioOnTimeout extends TimerTask {
        @Override
        public void run() {
            exit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityBase.activity = this;

        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigation = findViewById(R.id.nav_view);
        navigation.setNavigationItemSelectedListener(this);
        navigation.setCheckedItem(currentView);

        setTitle();
        setQuiteTimeout();
        showErrorMessage();

        if (HSettings.isNetworkAvailable()) new DownloadTalesData().execute("https://kazky.suspilne.media/list", DownloadTalesData.CACHE_IMAGES);
        if (HSettings.isNetworkAvailable()) new GetTaleReaders().execute("https://kazky.suspilne.media/list");
    }

    @Override
    protected void onResume(){
        super.onResume();
        showErrorMessage();
    }

    private void showErrorMessage(){
        String errorMessage = HSettings.getString("errorMessage");

        if (!errorMessage.isEmpty()){
            showAlert("Сталась помлка", errorMessage);
            getNotificationManager().cancel(DownloadAll.notificationId);
            HSettings.setString("errorMessage", "");
        }
    }

    protected void showAlert(String title, String message){
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.logo)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton("OK", null)
                .show();
    }

    protected void continueDownloadTales(){
        if (!HSettings.getBoolean("talesDownload")) return;

        boolean allTalesAreDownloaded = true;
        for (int id : HSettings.getSavedTaleIds()){
            if (!HSettings.taleExists(id)){
                allTalesAreDownloaded = false;
            }
        }

        if (allTalesAreDownloaded || !HSettings.isNetworkAvailable()) return;

        new DownloadTalesData().execute("https://kazky.suspilne.media/list", DownloadTalesData.DOWNLOAD_ALL);
    }

    private void exit(){
        Intent intent = new Intent();
        intent.setAction(HSettings.application);
        intent.putExtra("code", "StopPlay");
        sendBroadcast(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            showQuitDialog();
        }
    }

    private void showQuitDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    exit();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //'No' button clicked
                    break;
            }
            }
        };

        new AlertDialog.Builder(this)
            .setIcon(R.mipmap.logo)
            .setTitle("Вийти з Казок?")
            .setPositiveButton("Так", dialogClickListener)
            .setNegativeButton("Ні", dialogClickListener)
            .show();
    }

    private void setTitle() {
        String title = navigation.getMenu().findItem(currentView).getTitle().toString();
        getSupportActionBar().setTitle(title);
    }

    private void openActivity(Class view){
//        if (player != null) player.releasePlayer();

        Intent intent = new Intent(this, view);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
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

    protected void stopPlayerService(){
        stopService(new Intent(this, PlayerService.class));
        try {
            getNotificationManager().cancelAll();
        } catch (Exception ex) {
            Log.e(HSettings.application, ex.getMessage());
            ex.printStackTrace();
        }
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

            case R.id.settings_menu:
                if (currentView != R.id.settings_menu) {
                    openActivity(ActivitySettings.class);
                }
                break;

            case R.id.info_menu:
                if (currentView != R.id.info_menu) {
                    openActivity(ActivityInfo.class);
                }
                break;

            case R.id.rate_app_menu:
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
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    void dropDownloads(String extension){
        for (String file: HSettings.getFileNames(this)) {
            if (file.toLowerCase().contains(extension.toLowerCase())){
                HSettings.deleteFile(file);
            }
        }
    }
}
package media.suspilne.kazky;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Toast;
import com.google.android.gms.common.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("Registered")
public class ActivityBase extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Timer quitTimer;
    protected NavigationView navigation;
    protected int currentView;

    ProgressDialog progress;

    private static Activity activity;
    public static Activity getActivity(){ return activity; }

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

        GetTaleIds cache = new GetTaleIds();
        if (HSettings.isNetworkAvailable()) cache.execute("https://kazky.suspilne.media/list", cache.CACHE_IMAGES);
        if (HSettings.isNetworkAvailable()) new GetTaleReaders().execute("https://kazky.suspilne.media/list");
    }

    protected void askToContinueDownloadTales(){
        if (!HSettings.getBoolean("talesDownload")) return;

        boolean allTalesAreDownloaded = true;
        for (int id : HSettings.getSavedTaleIds()){
            if (!HSettings.taleExists(id)){
                allTalesAreDownloaded = false;
            }
        }

        if (allTalesAreDownloaded || !HSettings.isNetworkAvailable()) return;

        new AlertDialog.Builder(ActivityBase.this)
                .setIcon(R.mipmap.logo)
                .setTitle("Продовжити закачку казок?")
                .setMessage("Не всі казки скачані на пристрій. Докачати?")
                .setPositiveButton("Докачати", (dialog, which) -> {
                    GetTaleIds download = new GetTaleIds();
                    download.execute("https://kazky.suspilne.media/list", download.DOWNLOAD_ALL);})
                .setNegativeButton("Ні", null)
                .show();
    }

    private void exit(){
        moveTaskToBack(true);
        stopPlayerService();
        System.exit(1);
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
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
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

    private void getMp3File(int id) throws Exception {
        if (HSettings.taleExists(id)) return;

        String track = String.format("%02d.mp3", id);
        URL url = new URL("https://kazky.suspilne.media/inc/audio/" + track);
        InputStream is = (InputStream) url.getContent();
        HSettings.saveFile(track, IOUtils.toByteArray(is));
    }

    private void getJpgFile(String url, String name, int width, int height) throws Exception {
        if (HSettings.fileExists(name)) return;

        InputStream is = (InputStream) new URL(url).getContent();
        Drawable drawable = HImages.resize(Drawable.createFromStream(is, "src name"), width, height);
        HSettings.saveImage(name, drawable);
    }

    private void getTitleAndReader(int id) throws Exception {
        String title = HSettings.getString("title-" + id);
        String reader = HSettings.getString("reader-" + id);

        if (title.equals("") || reader.equals("")){
            Document document = Jsoup.connect("https://kazky.suspilne.media/list").get();
            title = document.select("div.tales-list a[href*='/" + id + "?'] div[class$='caption']").text().trim();
            reader = document.select("div.tales-list a[href*='/" + id + "?'] div[class$='tale-time']").text().trim();

            HSettings.setString("title-" + id, title);
            HSettings.setString("reader-" + id, reader);
        }
    }

    class GetTaleIds extends AsyncTask<String, Void, Integer[]> {
        String action = "null";
        final String CACHE_IMAGES = "Cache images";
        final String DOWNLOAD_ALL = "Download all";

        private ArrayList<Integer> getTaleIds(String url) throws Exception {
            ArrayList<Integer> result = new ArrayList<>();
            Document document = Jsoup.connect(url).get();
            Elements tales = document.select("div.tales-list a");

            for (Element tale : tales) {
                String href = tale.attr("href");
                String id = href.split("\\?")[0].split("/")[2];
                result.add(Integer.valueOf(id));
            }

            return result;
        }

        @Override
        protected Integer[] doInBackground(String... arg) {
            ArrayList<Integer> result = new ArrayList<>();

            try {
                action = arg[1];
                ArrayList<Integer> cachedIds =  HSettings.getSavedTaleIds();
                ArrayList<Integer> realIds = new ArrayList<>();

                if (cachedIds.size() < 10){
                    realIds = getTaleIds(arg[0]);
                }

                result = HList.union(cachedIds, realIds);
                Collections.sort(result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result.toArray(new Integer[0]);
        }

        @Override
        protected void onPostExecute(final Integer[] ids) {
            super.onPostExecute(ids);

            if (ids.length == 0){
                Toast.makeText(ActivityBase.this, "Сталася помилка, спробуйте пізніше!", Toast.LENGTH_LONG).show();

                if (action == DOWNLOAD_ALL){
                    HSettings.setBoolean("talesDownload", false);
                }
                return;
            }

            switch (action){
                case CACHE_IMAGES:
                    new CacheImages().execute(ids);
                    break;

                case DOWNLOAD_ALL:
                    new DownloadAll().execute(ids);
                    break;
            }
        }
    }

    class CacheImages extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... integers) {
            try {
                for (int id:integers) {
                    String name = String.format("%02d.jpg", id);
                    String url = String.format("https://kazky.suspilne.media/inc/img/songs_img/%02d.jpg", id);

                    getJpgFile(url, name, 300, 226);
                    getTitleAndReader(id);
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }

    class DownloadAll extends AsyncTask<Integer, Integer, String> {
        protected void onPreExecute() {
            progress = new ProgressDialog(ActivityBase.this);
            progress.setIcon(R.mipmap.logo);
            progress.setTitle("Завантаження казок");
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progress.incrementProgressBy(1);
        }

        @Override
        protected void onPostExecute(final String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }

            if (result.isEmpty()){
                Toast.makeText(ActivityBase.this, "Готово!", Toast.LENGTH_LONG).show();
            }else{
                new AlertDialog.Builder(ActivityBase.this)
                    .setIcon(R.mipmap.logo)
                    .setTitle("Сталась помлка")
                    .setMessage(result)
                    .setNeutralButton("OK", null)
                    .show();
            }
        }

        @Override
        protected String doInBackground(Integer... integers) {
            try {
                progress.setMax(integers.length);

                for (int id:integers) {
                    long freeSpace = HSettings.freeSpace();

                    if (freeSpace < 50){
                        throw new Exception(String.format("Лишилось лише %dМБ вільного місця!", freeSpace));
                    }

                    String name = String.format("%02d.jpg", id);
                    String url = String.format("https://kazky.suspilne.media/inc/img/songs_img/%02d.jpg", id);

                    getMp3File(id);
                    getJpgFile(url, name, 300, 226);
                    getTitleAndReader(id);

                    publishProgress(id);
                }
            }catch (Exception e){
                e.printStackTrace();
                return e.getMessage();
            }

            return "";
        }
    }

    class GetTaleReaders extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... arg) {
            try {
                Document document = Jsoup.connect(arg[0]).get();
                Elements readers = document.select("div.information__main div.reader-line");

                for (Element reader : readers) {
                    final String src = reader.select("img").attr("src");
                    final String id = src.split("readers/")[1].split("\\.")[0];
                    final String fullName = reader.select("div.reader").text().trim().split("\\.")[0];
                    String readerName = String.format("readerName-%s", id);
                    String name = String.format("%s.jpg", readerName);
                    String url = String.format("https://kazky.suspilne.media/inc/img/readers/%s.jpg", id);

                    HSettings.setString(readerName, fullName);
                    getJpgFile(url, name, 100, 100);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
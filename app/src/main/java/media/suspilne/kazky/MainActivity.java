package media.suspilne.kazky;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("Registered")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Timer quitTimer;
    protected Player player;
    protected NavigationView navigation;
    protected int currentView;

    ProgressDialog progressDialog;

    protected void setQuiteTimeout(){
        if (SettingsHelper.getBoolean(this, "autoQuit")) {
            if (quitTimer != null) quitTimer.cancel();
            int timeout = SettingsHelper.getInt(this, "timeout");

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

    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        player = new Player(this);
        player.UpdateSslProvider();
        startService(new Intent(this, Player.class));
    }

    private void exit(){
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
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
        if (player != null) player.releasePlayer();

        Intent intent = new Intent(this, view);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.radio_menu:
                if (currentView != R.id.radio_menu) {
                    openActivity(Radio.class);
                }
                break;

            case R.id.tales_menu:
                if (currentView != R.id.tales_menu) {
                    openActivity(Tales.class);
                }
                break;

            case R.id.settings_menu:
                if (currentView != R.id.settings_menu) {
                    openActivity(Settings.class);
                }
                break;

            case R.id.exit_menu:
                showQuitDialog();
        }

        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return false;
    }

    void dropDownloads(String extension){
        for (String file:SettingsHelper.getFileNames(this)) {
            if (file.toLowerCase().contains(extension.toLowerCase())){
                SettingsHelper.deleteFile(this, file);
            }
        }
    }

    private void getMp3File(int id) throws IOException {
        String track = String.format("%02d.mp3", id);
        URL url = new URL("https://kazky.suspilne.media/inc/audio/" + track);
        int length = url.openConnection().getContentLength();

        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(MainActivity.this.getFilesDir() + "/" + track);
        fos.getChannel().transferFrom(rbc, 0, length);
    }

    private void getJpgFile(int id) throws IOException {
        String image = String.format("%02d", id) + ".jpg";
        InputStream is = (InputStream) new URL("https://kazky.suspilne.media/inc/img/songs_img/" + image).getContent();
        Drawable drawable = ImageHelper.resize(Drawable.createFromStream(is, "src name"), 300, 226);
        SettingsHelper.saveImage(MainActivity.this, image, drawable);
    }

    private void getTitleAndReader(int id) throws IOException {
        String title = SettingsHelper.getString(MainActivity.this, "title-" + id);
        String reader = SettingsHelper.getString(MainActivity.this, "reader-" + id);

        if (title.equals("") || reader.equals("")){
            Document document = Jsoup.connect("https://kazky.suspilne.media/list").get();
            title = document.select("div.tales-list a[href*='/" + id + "?'] div[class$='caption']").text().trim();
            reader = document.select("div.tales-list a[href*='/" + id + "?'] div[class$='tale-time']").text().trim();

            SettingsHelper.setString(MainActivity.this, "title-" + id, title);
            SettingsHelper.setString(MainActivity.this, "reader-" + id, reader);
        }
    }

    class GetTaleIds extends AsyncTask<String, Void, Integer[]> {
        String action = "null";
        final String CACHE_IMAGES = "Cache images";
        final String DOWNLOAD_ALL = "Download all";

        private ArrayList<Integer> getTaleIds(String url) throws IOException {
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
                ArrayList<Integer> cachedIds =  SettingsHelper.getSavedTaleIds(MainActivity.this);
                ArrayList<Integer> realIds = new ArrayList<>();

                if (cachedIds.size() < 10){
                    realIds = getTaleIds(arg[0]);
                }

                result = ListHelper.union(cachedIds, realIds);
                Collections.sort(result);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toArray(new Integer[0]);
        }

        @Override
        protected void onPostExecute(final Integer[] ids) {
            super.onPostExecute(ids);

            if (ids.length == 0){
                Toast.makeText(MainActivity.this, "Сталася помилка, спробуйте пізніше!", Toast.LENGTH_LONG).show();
                return;
            }

            switch (action){
                case CACHE_IMAGES:
                    if (ids.length < 10) new CacheImages().execute(ids);
                    break;

                case DOWNLOAD_ALL:
                    new DownloadAll().execute(ids);
                    break;
            }
        }
    }

    class CacheImages extends AsyncTask<Integer, Integer, Boolean> {
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setIcon(R.mipmap.logo);
            progressDialog.setTitle("Завантаження казок");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            progressDialog.setMax(integers.length);

            try {
                for (int id:integers) {
                    getJpgFile(id);
                    getTitleAndReader(id);
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }

    class DownloadAll extends AsyncTask<Integer, Integer, Boolean> {
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setIcon(R.mipmap.logo);
            progressDialog.setTitle("Завантаження казок");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.incrementProgressBy(1);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (success){
                Toast.makeText(MainActivity.this, "Готово!", Toast.LENGTH_LONG).show();
                SettingsHelper.setBoolean(MainActivity.this, "talesDownload", true);
//                setColorsAndState(); TODO
            }
            else{
                dropDownloads(".mp3");
                Toast.makeText(MainActivity.this, "Сталась помлка, можливо мало місця!", Toast.LENGTH_LONG).show();
                SettingsHelper.setBoolean(MainActivity.this, "talesDownload", false);
//                setColorsAndState(); TODO
            }
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            progressDialog.setMax(integers.length);

            try {
                for (int id:integers) {
                    getMp3File(id);
                    getJpgFile(id);
                    getTitleAndReader(id);

                    publishProgress();
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }
}
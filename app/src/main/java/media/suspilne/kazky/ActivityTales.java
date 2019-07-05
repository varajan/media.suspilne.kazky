package media.suspilne.kazky;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;

public class ActivityTales extends ActivityBase {
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        unregisterReceiver();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        registerReceiver();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_tales);
        currentView = R.id.tales_menu;
        super.onCreate(savedInstanceState);

        showTales();
        askToDownloadTales();
        askToContinueDownloadTales();
        registerReceiver();
    }

    private void askToDownloadTales(){
        if (SettingsHelper.getBoolean("askToDownloadTales")
         || SettingsHelper.getBoolean("talesDownload")) return;

        new AlertDialog.Builder(ActivityTales.this)
                .setIcon(R.mipmap.logo)
                .setTitle("Скачат казки на пристрій?")
                .setMessage("Це займе приблизно 130MB. Але потім казки можна слухати без Інтернета.")
                .setPositiveButton("Скачати", (dialog, which) -> {
                    GetTaleIds download = new GetTaleIds();
                    download.execute("https://kazky.suspilne.media/list", download.DOWNLOAD_ALL);})
                .setNegativeButton("Ні", null)
                .show();

        SettingsHelper.setBoolean("askToDownloadTales", true);
    }

    private View.OnClickListener onPlayBtnClick = v -> {
        try{
            ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds();
            ImageView playBtn = (ImageView) v;
            int id = (int) playBtn.getTag();

            if (isServiceRunning(PlayerService.class) && id == getNowPlaying()){
                stopPlayerService();
                playBtn.setImageResource(R.mipmap.tale_play);
            }else{
                playTale(ids, id);
                setQuiteTimeout();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    };

    public int getNext(){
        ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds();
        boolean online = SettingsHelper.isNetworkAvailable();
        int nowPlaying = getNowPlaying();

        for(int nextId:ids) {
            if (nextId > nowPlaying && (online || SettingsHelper.taleExists(nextId))) {
                return nextId;
            }
        }

        for(int prevId:ids){
            if (prevId < nowPlaying && (online || SettingsHelper.taleExists(prevId))) {
                return prevId;
                }
            }

        return -1;
    }

    public int getPrevious(){
        ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds();
        boolean online = SettingsHelper.isNetworkAvailable();
        int nowPlaying = getNowPlaying();
        Collections.reverse(ids);

        for(int prevId:ids){
            if (prevId < nowPlaying && (online || SettingsHelper.taleExists(prevId))) {
                return prevId;
            }
        }

        for(int nextId:ids) {
            if (nextId > nowPlaying && (online || SettingsHelper.taleExists(nextId))) {
                return nextId;
            }
        }

        return -1;
    }

    private void playTale(ArrayList<Integer> ids, int id){
        super.stopPlayerService();

        if (id > 0){
            Intent intent = new Intent(this, PlayerService.class);
            intent.putExtra("id", id);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            }
            else {
                startService(intent);
            }
        }
    }

    private void setPlayBtnIcon(ArrayList<Integer> ids, int id){
        long start = System.currentTimeMillis();
        LinearLayout list = null;

        while (list == null && System.currentTimeMillis() - start < 1000){
            list = findViewById(R.id.list);
        }

        if (list == null) return;

        for (int x:ids){
            ImageView btn = list.findViewWithTag(x).findViewById(R.id.play);
            btn.setImageResource(x == id ? R.mipmap.tale_pause : R.mipmap.tale_play);
        }
    }

    private void setTaleDetails(View item, int id){
        Drawable image = SettingsHelper.getImage(String.format("%02d.jpg", id));
        String title = SettingsHelper.getString("title-" + id);
        String reader = SettingsHelper.getString("reader-" + id);

        ((TextView) item.findViewById(R.id.title)).setText(title);
        ((TextView) item.findViewById(R.id.reader)).setText(reader);
        if (image != null) ((ImageView) item.findViewById(R.id.preview)).setImageDrawable(image);
    }

    void showTales() {
        final LinearLayout list = findViewById(R.id.list);
        ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds();

        if (ids.size() == 0){
            new AlertDialog.Builder(this)
                .setIcon(R.mipmap.logo)
                .setTitle("Відсутній Інтернет!")
                .setMessage("Щоб схухати казки потрібне підключення до Інтернета.")
                .setPositiveButton("OK", (dialog, which) -> SettingsHelper.setBoolean("askToDownloadTales", false))
                .show();
        }

        for (final int id:ids) {
            View item = LayoutInflater.from(this).inflate(R.layout.tale_item, list, false);
            final ImageView playBtn = item.findViewById(R.id.play);

            item.setTag(id);
            list.addView(item);
            setTaleDetails(item, id);

            playBtn.setOnClickListener(onPlayBtnClick);
            playBtn.setTag(id);

            if (getNowPlaying() == id){
                playBtn.setImageResource(R.mipmap.tale_pause);
                ActivityTales.this.setQuiteTimeout();
            }
        }
    }

    private void registerReceiver(){
        try{
            IntentFilter filter = new IntentFilter();
            filter.addAction(SettingsHelper.application);
            this.registerReceiver(receiver, filter);
        }catch (Exception e){
        }
    }

    private void unregisterReceiver(){
        try{
            this.unregisterReceiver(receiver);
        }catch (Exception e){ /*nothing*/ }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds();
            switch (intent.getStringExtra("code")){
                case "SourceIsNotAccessible":
                    setPlayBtnIcon(ids, -1);
                    Toast.makeText(ActivityTales.this,"Сталась помилка!", Toast.LENGTH_LONG).show();
                    break;

                case "SetPlayBtnIcon":
                    setPlayBtnIcon(ids, intent.getIntExtra("id", -1));
                    break;
            }
        }
    };

    public static int getNowPlaying() { return SettingsHelper.getInt("nowPlaying"); }
    public static void setNowPlaying(int value) { SettingsHelper.setInt("nowPlaying", value); }

    public static int getLastPlaying() { return SettingsHelper.getInt("lastPlaying"); }
    public static void setLastPlaying(int value) { SettingsHelper.setInt("lastPlaying", value); }

    public static long getPosition() { return SettingsHelper.getLong("position"); }
    public static void setPosition(long value) { SettingsHelper.setLong("position", value); }
}
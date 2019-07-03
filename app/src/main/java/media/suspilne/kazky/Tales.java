package media.suspilne.kazky;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class Tales extends MainActivity {
    int nowPlaying;
    int lastPlaying;
    long position;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player.isPlaying())
            player.releasePlayer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("nowPlaying", player.isPlaying() ? nowPlaying : -1);
        outState.putInt("lastPlaying", lastPlaying);
        outState.putLong("position", player.isPlaying() ? player.position() : position);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        nowPlaying = savedInstanceState.getInt("nowPlaying");
        lastPlaying = savedInstanceState.getInt("lastPlaying");
        position = savedInstanceState.getLong("position");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_tales);
        currentView = R.id.tales_menu;
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            nowPlaying = savedInstanceState.getInt("nowPlaying");
            lastPlaying = savedInstanceState.getInt("lastPlaying");
            position = savedInstanceState.getLong("position");
        }

        showTales();
        askToDownloadTales();
        askToContinueDownloadTales();
    }

    private void askToDownloadTales(){
        if (SettingsHelper.getBoolean(this, "askToDownloadTales")
         || SettingsHelper.getBoolean(this, "talesDownload")) return;

        new AlertDialog.Builder(Tales.this)
                .setIcon(R.mipmap.logo)
                .setTitle("Скачат казки на пристрій?")
                .setMessage("Це займе приблизно 130MB. Але потім казки можна слухати без Інтернета.")
                .setPositiveButton("Скачати", (dialog, which) -> {
                    GetTaleIds download = new GetTaleIds();
                    download.execute("https://kazky.suspilne.media/list", download.DOWNLOAD_ALL);})
                .setNegativeButton("Ні", null)
                .show();

        SettingsHelper.setBoolean(this, "askToDownloadTales", true);
    }

    private View.OnClickListener onPlayBtnClick = new View.OnClickListener() {
        public void onClick(View v) {
            try{
                ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds(Tales.this);
                ImageView playBtn = (ImageView) v;
                int id = (int) playBtn.getTag();

                if (player.isPlaying() && id == nowPlaying){
                    position = player.position();
                    lastPlaying = id;

                    player.releasePlayer();
                    playBtn.setImageResource(R.mipmap.tale_play);
                }else{
                    playTale(ids, id);
                    Tales.this.setQuiteTimeout();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private int getNextTale(ArrayList<Integer> ids){
        boolean online = isNetworkAvailable();

        if (SettingsHelper.getBoolean(Tales.this, "talesPlayNext")){
            for(int nextId:ids) {
                if (nextId > nowPlaying && (online || SettingsHelper.taleExists(this, nextId))) {
                    return nextId;
                }
            }

            for(int prevId:ids){
                if (prevId < nowPlaying && (online || SettingsHelper.taleExists(this, prevId))) {
                    return prevId;
                    }
                }
            }

        return -1;
    }

    private PlayerService.MediaIsEndedListener onPlaybackEnded = new PlayerService.MediaIsEndedListener(){
        @Override
        public void mediaIsEnded(){
            ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds(Tales.this);
            int next = getNextTale(ids);

            if (next > 0){
                playTale(ids, next);
            }
            else{
                nowPlaying = -1;
                setPlayBtnIcon(ids, -1);
            }
        }
    };

    private PlayerService.SourceIsNotAccessibleListener onSourceNotAccessible = new PlayerService.SourceIsNotAccessibleListener(){
        @Override
        public void sourceIsNotAccessible(){
            ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds(Tales.this);
            nowPlaying = -1;
            setPlayBtnIcon(ids, -1);
            player.releasePlayer();

            Toast.makeText(Tales.this, "Сталась помилка!", Toast.LENGTH_LONG).show();
        }
    };

    private void playTale(ArrayList<Integer> ids, int playId){
        String name = String.format("%02d.mp3", playId);
        String url = "https://kazky.suspilne.media/inc/audio/" + name;
        String stream = SettingsHelper.taleExists(Tales.this, playId) ? Tales.this.getFilesDir() + "/" + name : url;

        player.releasePlayer();
        player.initializePlayer(stream);
        if (playId == lastPlaying){
            player.setPosition(position);
        }

        setPlayBtnIcon(ids, playId);
        nowPlaying = playId;
        lastPlaying = playId;
    }

    private void setPlayBtnIcon(ArrayList<Integer> ids, int id){
        LinearLayout list = findViewById(R.id.list);

        for (int x:ids){
            ImageView btn = list.findViewWithTag(x).findViewById(R.id.play);
            btn.setImageResource(x == id ? R.mipmap.tale_pause : R.mipmap.tale_play);
        }
    }

    private void setTaleDetails(View item, int id){
        Drawable image = SettingsHelper.getImage(Tales.this, String.format("%02d.jpg", id));
        String title = SettingsHelper.getString(Tales.this, "title-" + id);
        String reader = SettingsHelper.getString(Tales.this, "reader-" + id);

        ((TextView) item.findViewById(R.id.title)).setText(title);
        ((TextView) item.findViewById(R.id.reader)).setText(reader);
        if (image != null) ((ImageView) item.findViewById(R.id.preview)).setImageDrawable(image);
    }

    void showTales() {
        final LinearLayout list = findViewById(R.id.list);
        ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds(Tales.this);

        if (ids.size() == 0){
            new AlertDialog.Builder(Tales.this)
                .setIcon(R.mipmap.logo)
                .setTitle("Відсутній Інтернет!")
                .setMessage("Щоб схухати казки треба підключення до Інтернета.")
                .setPositiveButton("OK", (dialog, which) -> SettingsHelper.setBoolean(Tales.this, "askToDownloadTales", false))
                .show();
        }

        for (final int id:ids) {
            View item = LayoutInflater.from(Tales.this).inflate(R.layout.tale_item, list, false);
            final ImageView playBtn = item.findViewById(R.id.play);

            item.setTag(id);
            list.addView(item);
            setTaleDetails(item, id);

            playBtn.setOnClickListener(onPlayBtnClick);
            playBtn.setTag(id);

            if (nowPlaying == id){
                String name = String.format("%02d.mp3", nowPlaying);
                String url = "https://kazky.suspilne.media/inc/audio/" + name;

                playBtn.setImageResource(R.mipmap.tale_pause);
                player.initializePlayer(SettingsHelper.taleExists(Tales.this, nowPlaying) ? Tales.this.getFilesDir() + "/" + name : url);
                player.setPosition(position);
                Tales.this.setQuiteTimeout();
            }
        }

        player.addListener(onPlaybackEnded);
        player.addListener(onSourceNotAccessible);
    }
}
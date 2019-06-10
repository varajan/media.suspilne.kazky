package media.suspilne.kazky;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import java.util.concurrent.ExecutionException;

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

        askToDownloadTales();

        try {
            GetTaleIds cache = new GetTaleIds();
            cache.execute("https://kazky.suspilne.media/list", cache.CACHE_IMAGES).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new ShowTales().execute();
    }

    private void askToDownloadTales(){
        if (SettingsHelper.getBoolean(this, "askToDownloadTales")
         || SettingsHelper.getBoolean(this, "talesDownload")) return;

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    GetTaleIds download = new GetTaleIds();
                    try {
                        download.execute("https://kazky.suspilne.media/list", download.DOWNLOAD_ALL).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    new ShowTales().execute();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //'No' button clicked
                    break;
            }
        };

        new AlertDialog.Builder(Tales.this)
                .setIcon(R.mipmap.logo)
                .setTitle("Скачат казки на пристрій?")
                .setMessage("Це займе приблизно 130MB. Але потім казки можна слухати без Інтернета.")
                .setPositiveButton("Скачати", dialogClickListener)
                .setNegativeButton("Ні", dialogClickListener)
                .show();

        SettingsHelper.setBoolean(this, "askToDownloadTales", true);
    }

    class ShowTales extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg) { return null; }

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

        private Player.MediaIsEndedListener onPlaybackEnded = new Player.MediaIsEndedListener(){
            @Override
            public void mediaIsEnded(){
                ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds(Tales.this);
                if (SettingsHelper.getBoolean(Tales.this, "talesPlayNext")){
                    int next = ids.get(0);

                    for(int i:ids){
                        if (i > nowPlaying) {
                            next = i;
                            break;
                        }
                    }

                    playTale(ids, next);
                }else{
                    nowPlaying = -1;
                    setPlayBtnIcon(ids, -1);
                }
            }
        };

        private Player.SourceIsNotAccessibleListener onSourceNotAccessible = new Player.SourceIsNotAccessibleListener(){
            @Override
            public void sourceIsNotAccessible(){
                ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds(Tales.this);
                nowPlaying = -1;
                setPlayBtnIcon(ids, -1);
                player.releasePlayer();

                Toast.makeText(Tales.this, "Відсутній Інтернет!", Toast.LENGTH_LONG).show();
            }
        };

        private void playTale(ArrayList<Integer> ids, int playId){
            String name = String.format("%02d.mp3", playId);
            String url = "https://kazky.suspilne.media/inc/audio/" + name;

            player.releasePlayer();
            player.initializePlayer(SettingsHelper.fileExists(Tales.this, name) ? Tales.this.getFilesDir() + "/" + name : url);
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

        @Override
        protected void onPostExecute(Void result) {
            final LinearLayout list = findViewById(R.id.list);
            ArrayList<Integer> ids = SettingsHelper.getSavedTaleIds(Tales.this);
            Collections.sort(ids);

            for (final int id:ids) {
                View item = LayoutInflater.from(Tales.this).inflate(R.layout.tale_item, list, false);
                final ImageView playBtn = item.findViewById(R.id.play);

                item.setTag(id);
                list.addView(item);
                setTaleDetails(item, id);

                playBtn.setOnClickListener(onPlayBtnClick);
                playBtn.setTag(id);

                if (nowPlaying == id){
                    playBtn.setImageResource(R.mipmap.tale_pause);
                    player.initializePlayer("https://kazky.suspilne.media/inc/audio/" + String.format("%02d", nowPlaying) + ".mp3");
                    player.setPosition(position);
                    Tales.this.setQuiteTimeout();
                }
            }

            player.addListener(onPlaybackEnded);
            player.addListener(onSourceNotAccessible);
        }
    }
}
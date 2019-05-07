package media.suspilne.kazky;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class Tales extends BaseActivity {
    int nowPlaying;
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

        outState.putBoolean("isPlaying", player.isPlaying());
        outState.putInt("nowPlaying", player.isPlaying() ? nowPlaying : -1);
        outState.putLong("position", player.position());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        nowPlaying = savedInstanceState.getInt("nowPlaying");
        position = savedInstanceState.getLong("position");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tales);

        this.findViewById(R.id.menuBtn).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) { openSettingsView(); }
            }
        );

        this.findViewById(R.id.radioBtn).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    player.releasePlayer();
                    startActivity(new Intent(Tales.this, Radio.class));
                }
            }
        );

        new GetTales().execute("https://kazky.suspilne.media/list");
    }

    class GetTales extends AsyncTask<String, Void, ArrayList<Integer>> {
        private ArrayList<Integer> getTalesList(String url) {
            try {
                ArrayList<Integer> result = new ArrayList<>();
                Document document = Jsoup.connect(url).get();
                Elements tales = document.select("div.tales-list a");
                for (Element tale : tales) {
                    String href = tale.attr("href");
                    String id = href.split("\\?")[0].split("/")[2];
                    result.add(Integer.valueOf(id));
                }

                Collections.sort(result);

                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected ArrayList<Integer> doInBackground(String... arg) {
            return getTalesList(arg[0]);
        }

        @Override
        protected void onPostExecute(final ArrayList<Integer> ids) {
            super.onPostExecute(ids);
            final LinearLayout list = findViewById(R.id.list);

            for (final int id:ids) {
                View item = LayoutInflater.from(Tales.this).inflate(R.layout.tale_item, list, false);
                item.setTag(id);
                list.addView(item);

                new SetTaleTitle().execute(id);
                new SetTaleImage().execute(id);

                final ImageView playBtn = item.findViewById(R.id.play);

                playBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (player.isPlaying() && playBtn.getTag().equals(R.mipmap.tale_pause)){
                            player.releasePlayer();
                            playBtn.setImageResource(R.mipmap.tale_play);
                            playBtn.setTag(R.mipmap.tale_play);
                        }else{
                            playTale(ids, id);
                            Tales.this.setQuiteTimeout();
                        }
                    }
                });

                if (nowPlaying == id){
                    playBtn.setImageResource(R.mipmap.tale_pause);
                    playBtn.setTag(R.mipmap.tale_pause);

                    player.initializePlayer("https://kazky.suspilne.media/inc/audio/" + String.format("%02d", nowPlaying) + ".mp3");
                    player.setPosition(position);

                    Tales.this.setQuiteTimeout();
                }
            }

            player.addListener(new Player.MediaIsEndedListener(){
                @Override
                public void mediaIsEnded(){
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
            });
        }

        private void playTale(ArrayList<Integer> ids, int playId){
            player.releasePlayer();
            player.initializePlayer("https://kazky.suspilne.media/inc/audio/" + String.format("%02d", playId) + ".mp3");
            setPlayBtnIcon(ids, playId);
            nowPlaying = playId;
        }

        private void setPlayBtnIcon(ArrayList<Integer> ids, int id){
            LinearLayout list = findViewById(R.id.list);

            for (int x:ids){
                ImageView btn = list.findViewWithTag(x).findViewById(R.id.play);
                btn.setImageResource(x == id ? R.mipmap.tale_pause : R.mipmap.tale_play);
                btn.setTag(x == id ? R.mipmap.tale_pause : R.mipmap.tale_play);
            }
        }
    }

    class SetTaleImage extends AsyncTask<Integer, Void, Drawable> {
        private int id;

        private Drawable resize(Drawable image) {
            Bitmap b = ((BitmapDrawable)image).getBitmap();
            Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 150, 113, false);
            return new BitmapDrawable(getResources(), bitmapResized);
        }

        @Override
        protected Drawable doInBackground(Integer... arg) {
            try {
                id = arg[0];
                InputStream is = (InputStream) new URL("https://kazky.suspilne.media/inc/img/songs_img/" + String.format("%02d", id) + ".jpg").getContent();
                return resize(Drawable.createFromStream(is, "src name"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable preview) {
            super.onPostExecute(preview);

            if (preview != null)
            {
                View item = findViewById(R.id.list).findViewWithTag(id);
                ((ImageView)item.findViewById(R.id.preview)).setImageDrawable(preview);
            }
        }
    }

    class SetTaleTitle extends AsyncTask<Integer, Void, String[]> {
        private int id;

        @Override
        protected String[] doInBackground(Integer... arg) {
            try {
                id = arg[0];

                Document document = Jsoup.connect("https://kazky.suspilne.media/list").get();
                Elements title = document.select("div.tales-list a[href*='/" + id + "?'] div[class$='caption']");
                Elements reader = document.select("div.tales-list a[href*='/" + id + "?'] div[class$='tale-time']");

                return new String[] {title.text().trim(), reader.text().trim()};
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] titles) {
            super.onPostExecute(titles);

            View item = findViewById(R.id.list).findViewWithTag(id);
            TextView title = item.findViewById(R.id.title);
            TextView reader = item.findViewById(R.id.reader);
            ImageView preview = item.findViewById(R.id.preview);
            int margin = ((ConstraintLayout.LayoutParams)preview.getLayoutParams()).leftMargin;
            int imageWidth = preview.getWidth();
            int maxWidth =  item.getWidth() - imageWidth - 2 * margin;

            title.setText(titles[0]);
            reader.setText(titles[1]);

            title.setWidth(maxWidth);
            reader.setWidth(maxWidth);
        }
    }
}

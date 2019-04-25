package media.suspilne.kazky;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
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

public class Tales extends AppCompatActivity {
    private String url = "https://kazky.suspilne.media/list";

    private void openSettingsView(){
        startActivityForResult(new Intent(Tales.this, Settings.class), 0);
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
                openSettingsView();
                return true;

            case KeyEvent.KEYCODE_BACK:
                System.exit(1);
                return true;
        }

        return super.onKeyDown(keycode, e);
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

        new GetTales().execute(url);
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
        protected void onPostExecute(ArrayList<Integer> ids) {
            super.onPostExecute(ids);

            // https://kazky.suspilne.media/inc/audio/17.mp3
            LinearLayout list = findViewById(R.id.list);

            for (int id:ids) {
                View item = LayoutInflater.from(Tales.this).inflate(R.layout.tale_item, list, false);
                item.setTag(id);
                list.addView(item);

                new SetTaleImage().execute(id);
                new SetTaleTitle().execute(id);
            }
        }
    }

    class SetTaleImage extends AsyncTask<Integer, Void, Drawable> {
        private int id;

        @Override
        protected Drawable doInBackground(Integer... arg) {
            try {
                id = arg[0];
                InputStream is = (InputStream) new URL("https://kazky.suspilne.media/inc/img/songs_img/" + String.format("%02d", id) + ".jpg").getContent();
                return Drawable.createFromStream(is, "src name");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable preview) {
            super.onPostExecute(preview);
            View item = findViewById(R.id.list).findViewWithTag(id);
            ((ImageView)item.findViewById(R.id.preview)).setImageDrawable(preview);
        }
    }

    class SetTaleTitle extends AsyncTask<Integer, Void, String[]> {
        private int id;

        @Override
        protected String[] doInBackground(Integer... arg) {
            try {
                id = arg[0];

                Document document = Jsoup.connect(url).get();
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
            ((TextView)item.findViewById(R.id.title)).setText(titles[0]);
            ((TextView)item.findViewById(R.id.reader)).setText(titles[1]);
        }
    }
}

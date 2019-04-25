package media.suspilne.kazky;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
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

    class GetTales extends AsyncTask<String, Integer, String> {
        private String getTalesList(String url) {
            try {
                String result = "";
                Document document = Jsoup.connect(url).get();
                Elements tales = document.select("div.tales-list a");
                for (Element tale : tales) {
                    String href = tale.attr("href");
                    String id = href.split("\\?")[0].split("/")[2];
                    result += id + ":";
                }

                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected String doInBackground(String... arg) {
            return getTalesList(arg[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            LinearLayout list = findViewById(R.id.list);

            String[] ids = result.split(":");

            for (String id:ids) {
//                ImageView pic = new ImageView(Tales.this);
//                pic.layout(0, 0,0,0);
//                pic.setImageResource(R.mipmap.back);
//                list.addView(pic);

                Drawable preview = loadImageFromWebOperations(id);
                View item = LayoutInflater.from(Tales.this).inflate(R.layout.tale_item, list, false);

//                ((ImageView)item.findViewById(R.id.preview)).setImageDrawable(preview);
                ((TextView)item.findViewById(R.id.title)).setText("Tale title #" + id);
                ((TextView)item.findViewById(R.id.reader)).setText("Tale reading: " + id);

                list.addView(item);
            }
        }

        private Drawable loadImageFromWebOperations(String id) {
            try {
                InputStream is = (InputStream) new URL(url + "/inc/img/songs_img/" + id + ".jpg").getContent();
                return Drawable.createFromStream(is, "src name");
            } catch (Exception e) {
                return null;
            }
        }
    }
}

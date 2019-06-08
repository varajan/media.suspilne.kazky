package media.suspilne.kazky;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
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
import java.util.Arrays;
import java.util.Collections;

public class Settings extends MainActivity {
    private Switch batteryOptimization;
    private Switch talesDownload;
    private Switch talesPlayNext;
    private Switch autoQuit;
    private SeekBar timeout;
    private TextView timeoutText;
    private int step = 5;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);
        currentView = R.id.settings_menu;
        super.onCreate(savedInstanceState);

        batteryOptimization = this.findViewById(R.id.batteryOptimization);
        talesDownload = this.findViewById(R.id.talesDownload);
        talesPlayNext = this.findViewById(R.id.talesPlayNext);
        autoQuit = this.findViewById(R.id.autoQuit);
        timeout = this.findViewById(R.id.timeout);
        timeoutText = this.findViewById(R.id.timeoutText);

        setColorsAndState();

        talesPlayNext.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsHelper.setBoolean(Settings.this, "talesPlayNext", isChecked);
                setColorsAndState();
            }
        });

        autoQuit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsHelper.setBoolean(Settings.this, "autoQuit", isChecked);
                setColorsAndState();
            }
        });

        timeout.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SettingsHelper.setInt(Settings.this,"timeout", seekBar.getProgress() * step);
                timeoutText.setText(SettingsHelper.getString(Settings.this, "timeout", "0") + " хвилин");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        new GetTaleReaders().execute("https://kazky.suspilne.media/list");
    }

    private CompoundButton.OnCheckedChangeListener onIgnoreBatteryChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            requestIgnoreBatteryOptimization();
            setColorsAndState();
        }
    };

    private void download(){
        if (!this.isNetworkAvailable()){
            Toast.makeText(this, "Відсутній Інтернет!", Toast.LENGTH_LONG).show();
        } else {
            new GetTaleIds().execute("https://kazky.suspilne.media/list");
        }
    }

    private void dropDownloads(){
        for (String file:SettingsHelper.getFileNames(this)) {
            if (file.contains(".mp3")){
                SettingsHelper.deleteFile(this, file);
            }
        }
    }

    private CompoundButton.OnCheckedChangeListener onDownloadTalesListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                download();
                                setColorsAndState();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //'No' button clicked
                                setColorsAndState();
                                break;
                        }
                    }
                };

                new AlertDialog.Builder(Settings.this)
                    .setIcon(R.mipmap.logo)
                    .setTitle("Скачат казки на пристрій?")
                    .setMessage("Це займе деякий час в залежності від швидкості Інтерета.")
                    .setPositiveButton("Скачати", dialogClickListener)
                    .setNegativeButton("Ні", dialogClickListener)
                    .show();
            }else{
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                dropDownloads();
                                SettingsHelper.setBoolean(Settings.this, "talesDownload", false);
                                setColorsAndState();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //'No' button clicked
                                setColorsAndState();
                                break;
                        }
                    }
                };

                new AlertDialog.Builder(Settings.this)
                    .setIcon(R.mipmap.logo)
                    .setTitle("Видалити казки з пристрою?")
                    .setMessage("Ви не зможете слухати казки без Інтерета.")
                    .setPositiveButton("Видалити", dialogClickListener)
                    .setNegativeButton("Ні", dialogClickListener)
                    .show();
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations(){
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        return pm.isIgnoringBatteryOptimizations(this.getPackageName());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestIgnoreBatteryOptimization(){
        if (isIgnoringBatteryOptimizations()){
            startActivityForResult(new Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), 0);
        }else{
            Uri packageUri = Uri.parse("package:" + this.getPackageName());
            startActivityForResult(new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setColorsAndState();
    }

    private void setColorsAndState() {
        boolean isTalesPlayNext = SettingsHelper.getBoolean(this, "talesPlayNext");
        boolean isAutoQuit = SettingsHelper.getBoolean(this, "autoQuit");
        boolean isTalesDownload = SettingsHelper.getBoolean(this, "talesDownload");
        int accent = ContextCompat.getColor(this, R.color.colorAccent);

        timeoutText.setText(SettingsHelper.getString(this, "timeout", "5") + " хвилин");
        timeout.setProgress(SettingsHelper.getInt(this, "timeout", 1) / step);

        talesPlayNext.setChecked(isTalesPlayNext);
        autoQuit.setChecked(isAutoQuit);
        timeout.setEnabled(isAutoQuit);
        timeout.setEnabled(isAutoQuit);

        talesDownload.setOnCheckedChangeListener(null);
        talesDownload.setTextColor(isTalesDownload ? accent : Color.GRAY);
        talesDownload.setChecked(isTalesDownload);
        talesDownload.setOnCheckedChangeListener(onDownloadTalesListener);

        if (Build.VERSION.SDK_INT > 23){
            batteryOptimization.setOnCheckedChangeListener(null);
            batteryOptimization.setTextColor(isIgnoringBatteryOptimizations() ? accent : Color.GRAY);
            batteryOptimization.setChecked(isIgnoringBatteryOptimizations());
            batteryOptimization.setOnCheckedChangeListener(onIgnoreBatteryChangeListener);
        }else{
            batteryOptimization.setVisibility(View.GONE);
        }

        talesPlayNext.setTextColor(isTalesPlayNext ? accent : Color.GRAY);
        autoQuit.setTextColor(isAutoQuit ? accent : Color.GRAY);
        timeoutText.setTextColor(isAutoQuit ? accent : Color.GRAY);
    }

    class GetTaleReaders extends AsyncTask<String, Void, ArrayList<ArrayList<String>>> {
        @Override
        protected ArrayList<ArrayList<String>> doInBackground(String... arg) {
            try {
                ArrayList<ArrayList<String>> result = new ArrayList<>();
                Document document = Jsoup.connect(arg[0]).get();
                Elements readers = document.select("div.information__main div.reader-line");
                for (Element reader : readers) {
                    final String src = reader.select("img").attr("src");
                    final String id = src.split("readers/")[1].split("\\.")[0];
                    final String name = reader.select("div.reader").text().trim().split("\\.")[0];

                    result.add(new ArrayList<>(Arrays.asList(id, name)));
                }

                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<ArrayList<String>> readers) {
            super.onPostExecute(readers);
            final LinearLayout list = findViewById(R.id.list);
            TextView sectionTitle = findViewById(R.id.sectionTitle);

            if (readers == null){
                sectionTitle.setText("Відсутній Інтернет!");
                Toast.makeText(Settings.this, "Відсутній Інтернет!", Toast.LENGTH_LONG).show();
                return;
            }

            for (ArrayList<String> reader:readers) {
                View item = LayoutInflater.from(Settings.this).inflate(R.layout.reader, list, false);
                list.addView(item);
                item.setTag(reader.get(0));

                ((TextView)item.findViewById(R.id.taleReader)).setText(reader.get(1));

                new SetPhotos().execute(reader.get(0));
            }
        }
    }

    class SetPhotos extends AsyncTask<String, Void, Drawable> {
        private String id;

        @Override
        protected Drawable doInBackground(String... arg) {
            try {
                id = arg[0];
                String file = "r" + id + ".jpg";
                Drawable drawable = SettingsHelper.getImage(Settings.this, file);

                if (drawable == null){
                    InputStream is = (InputStream) new URL("https://kazky.suspilne.media/inc/img/readers/" + id + ".jpg").getContent();
                    drawable = Drawable.createFromStream(is, "src name");
                    SettingsHelper.saveImage(Settings.this, file, drawable);
                }

                return drawable;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable readerPhoto) {
            super.onPostExecute(readerPhoto);

            View item = findViewById(R.id.list).findViewWithTag(id);
            TextView taleReader = item.findViewById(R.id.taleReader);
            ImageView photo = item.findViewById(R.id.photo);

            if (readerPhoto != null)
            {
                photo.setImageDrawable(ImageHelper.getCircularDrawable(readerPhoto));
            }

            int margin = ((ConstraintLayout.LayoutParams)photo.getLayoutParams()).leftMargin;
            int maxWidth =  item.getWidth() - photo.getWidth() - 3 * margin;

            taleReader.setWidth(maxWidth);
        }
    }

    class GetTaleIds extends AsyncTask<String, Void, Integer[]> {
        @Override
        protected Integer[] doInBackground(String... arg) {
            ArrayList<Integer> result = new ArrayList<>();

            try {
                Document document = Jsoup.connect(arg[0]).get();
                Elements tales = document.select("div.tales-list a");
                for (Element tale : tales) {
                    String href = tale.attr("href");
                    String id = href.split("\\?")[0].split("/")[2];
                    result.add(Integer.valueOf(id));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            result = ListHelper.union(result, SettingsHelper.getSavedTaleIds(Settings.this));
            Collections.sort(result);

            return result.toArray(new Integer[0]);
        }

        @Override
        protected void onPostExecute(final Integer[] ids) {
            super.onPostExecute(ids);

            if (ids.length == 0){
                Toast.makeText(Settings.this, "Сталася помилка, спробуйте пізніше!", Toast.LENGTH_LONG).show();
            }
            else{
                new Settings.DownloadTales().execute(ids);
            }
        }
    }

    class DownloadTales extends AsyncTask<Integer, Integer, Boolean> {
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(Settings.this);
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
                Toast.makeText(Settings.this, "Готово!", Toast.LENGTH_LONG).show();
                SettingsHelper.setBoolean(Settings.this, "talesDownload", true);
                setColorsAndState();
            }
            else{
                dropDownloads();
                Toast.makeText(Settings.this, "Сталась помлка, можливо мало місця!", Toast.LENGTH_LONG).show();
                SettingsHelper.setBoolean(Settings.this, "talesDownload", false);
                setColorsAndState();
            }
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            progressDialog.setMax(integers.length * 2);

            try {
                for (int id:integers) {
                    // get mp3 file
                    String track = String.format("%02d.mp3", id);
                    URL url = new URL("https://kazky.suspilne.media/inc/audio/" + track);
                    int length = url.openConnection().getContentLength();

                    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                    FileOutputStream fos = new FileOutputStream(Settings.this.getFilesDir() + "/" + track);
                    fos.getChannel().transferFrom(rbc, 0, length);
                    publishProgress();

                    // get jpg file
                    String image = String.format("%02d", id) + ".jpg";
                    InputStream is = (InputStream) new URL("https://kazky.suspilne.media/inc/img/songs_img/" + image).getContent();
                    Drawable drawable = ImageHelper.resize(Drawable.createFromStream(is, "src name"), 300, 226);
                    SettingsHelper.saveImage(Settings.this, image, drawable);
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
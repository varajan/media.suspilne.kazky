package media.suspilne.kazky;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Tasks {
    public static void getMp3File(int id) throws Exception {
        if (HSettings.taleExists(id)) return;

        String track = String.format("%02d.mp3", id);
        URL url = new URL("https://kazky.suspilne.media/inc/audio/" + track);
        InputStream is = (InputStream) url.getContent();
        HSettings.saveFile(track, IOUtils.toByteArray(is));
    }

    public static void getJpgFile(String url, String name, int width, int height) throws Exception {
        if (HSettings.fileExists(name)) return;

        InputStream is = (InputStream) new URL(url).getContent();
        Drawable drawable = HImages.resize(Drawable.createFromStream(is, "src name"), width, height);
        HSettings.saveImage(name, drawable);
    }

    public static void getTitleAndReader(int id) throws Exception {
        String title = HSettings.getString("title-" + id);
        String reader = HSettings.getString("reader-" + id);

        if (title.equals("") || reader.equals("")) {
            Document document = Jsoup.connect("https://kazky.suspilne.media/list").get();
            title = document.select("div.tales-list a[href*='/" + id + "?'] div[class$='caption']").text().trim();
            reader = document.select("div.tales-list a[href*='/" + id + "?'] div[class$='tale-time']").text().trim();

            HSettings.setString("title-" + id, title);
            HSettings.setString("reader-" + id, reader);
        }
    }
}

    class DownloadTalesData extends AsyncTask<String, Void, Integer[]> {
        String action = "null";
        final static String CACHE_IMAGES = "Cache images";
        final static String DOWNLOAD_ALL = "Download all";

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
                if (action.equals(DOWNLOAD_ALL)){
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

                    Tasks.getJpgFile(url, name, 300, 226);
                    Tasks.getTitleAndReader(id);
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }

    class DownloadAll extends AsyncTask<Integer, Void, String> {
        private NotificationManager notificationManager;
        private PendingIntent openApplication;

        private Drawable image = ContextCompat.getDrawable(ActivityBase.getActivity(), R.mipmap.logo);
        private int count;
        private int current;
        static int notificationId = 2;

        private void showProgressNotification(String text){
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ActivityBase.getActivity(), HSettings.application)
//                .setSmallIcon(R.drawable.ic_cloud_download)
                .setContentTitle("Завантаження казок")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .setLargeIcon(HImages.getBitmap(image))
                .setProgress(count, current, false)
                .setContentIntent(openApplication)
                .setSound(null);

            notificationManager.notify(notificationId, notificationBuilder.build());
        }

        private void showCompletedNotification(){
            if (count == 0){
                notificationManager.cancel(notificationId);
            } else {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ActivityBase.getActivity(), HSettings.application)
                    .setSmallIcon(R.drawable.ic_cloud_done)
                    .setContentTitle("Завантаження завершено")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setLargeIcon(HImages.getBitmap(image))
                    .setContentIntent(openApplication)
                    .setSound(null);

                notificationManager.notify(notificationId, notificationBuilder.build());
            }
        }

        private void showFailedNotification(String errorMessage){
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ActivityBase.getActivity(), HSettings.application)
                .setSmallIcon(R.drawable.ic_error)
                .setContentTitle("Сталась помлка")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(HImages.getBitmap(image))
                .setContentIntent(openApplication)
                .setSound(null);

            notificationManager.notify(notificationId, notificationBuilder.build());
            HSettings.setString("errorMessage", errorMessage);
        }

        protected void onPreExecute() {
            notificationManager = (NotificationManager) ActivityBase.getActivity().getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(HSettings.application, HSettings.application, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setSound(null, null);
                notificationChannel.setShowBadge(false);

                notificationManager.createNotificationChannel(notificationChannel);
            }

            Intent notificationIntent = new Intent(ActivityBase.getActivity(), ActivityTales.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            openApplication = PendingIntent.getActivity(ActivityBase.getActivity(), 0, notificationIntent, 0);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            showProgressNotification(String.format("%d з %d", current, count));
        }

        @Override
        protected void onPostExecute(final String result) {
            notificationManager.cancel(notificationId);

            if (result.isEmpty()){
                showCompletedNotification();
            }else{
                showFailedNotification(result);
            }
        }

        @Override
        protected String doInBackground(Integer... integers) {
            try {
                this.count = integers.length;
                this.current = 0;

                for (int id:integers) {
                    this.current++;
                    long freeSpace = HSettings.freeSpace();

                    if (freeSpace < 50){
                        throw new Exception(String.format("Лишилось лише %dМБ вільного місця!", freeSpace));
                    }

                    String name = String.format("%02d.jpg", id);
                    String url = String.format("https://kazky.suspilne.media/inc/img/songs_img/%02d.jpg", id);

                    Tasks.getMp3File(id);
                    Tasks.getJpgFile(url, name, 300, 226);
                    Tasks.getTitleAndReader(id);

                    publishProgress();
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
                    Tasks.getJpgFile(url, name, 100, 100);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }


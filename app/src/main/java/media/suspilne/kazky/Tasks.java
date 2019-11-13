package media.suspilne.kazky;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.google.android.gms.common.util.IOUtils;

import java.io.InputStream;
import java.net.URL;

public class Tasks {
//    public static void getMp3File(int id) throws Exception {
//        if (HSettings.taleExists(id)) return;
//
//        String track = String.format("%02d.mp3", id);
//        URL url = new URL("https://kazky.suspilne.media/tales/songs/" + track);
//        InputStream is = (InputStream) url.getContent();
//        HSettings.saveFile(track, IOUtils.toByteArray(is));
//    }

    public static void getJpgFile(String url, String name, int width, int height) throws Exception {
        if (SettingsHelper.fileExists(name)) return;

        InputStream is = (InputStream) new URL(url).getContent();
        Drawable drawable = ImageHelper.resize(Drawable.createFromStream(is, "src name"), width, height);
        SettingsHelper.saveImage(name, drawable);
    }

//    public static void getTitleAndReader(int id) {
//        String url = HSettings.getResourceString(R.string.index_json);
//        String title = HSettings.getString("title-" + id);
//        String reader = HSettings.getString("reader-" + id);
//
//        if (title.equals("") || reader.equals("")) {
//            String json = HSettings.getPageContent(url);
//            String[] sections = json.split("\\},");
//            String taleSection = "";
//
//            for (String section: sections) {
//                if (section.contains("\"" + id + "\"")){
//                    taleSection = section;
//                    break;
//                }
//            }
//
//            String[] parts = taleSection.split("\"");
//            title = parts[5].trim();
//            reader = parts[9].split(",")[0].trim();
//
//            HSettings.setString("title-" + id, title);
//            HSettings.setString("reader-" + id, reader);
//        }
//    }
}

class CacheImages extends AsyncTask<Integer, Integer, Boolean> {
    @Override
    protected Boolean doInBackground(Integer... integers) {
        try {
            for (int id:integers) {
                String name = String.format("%02d.jpg", id);
                String imgUrl = ActivityMain.getActivity().getResources().getString(R.string.talePicture);
                String url = String.format(imgUrl, id);

                Tasks.getJpgFile(url, name, 300, 226);
//                    Tasks.getTitleAndReader(id);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }
}

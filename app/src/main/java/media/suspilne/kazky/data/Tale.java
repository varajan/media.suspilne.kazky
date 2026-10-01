package media.suspilne.kazky.data;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.google.android.gms.common.util.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import media.suspilne.kazky.R;
import media.suspilne.kazky.activities.views.TaleView;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.activities.ActivityTales;
import media.suspilne.kazky.helpers.StringHelper;

public class Tale {
    public int id;
    public int introTime;
    public int coloring;
    public int titleId;
    public int readerId;
    public int image;
    public boolean isFavorite;
    public boolean isDownloaded;
    public String stream;
    public String fileName;
    public String duration;

    Tale() { id = -1; }

    Tale(int id, String duration, int intro, int coloring, int title, int name, int img) {
        this.id = id;
        this.introTime = intro;
        this.coloring = coloring;
        this.duration = "⏱ " + duration;
        this.titleId = title;
        this.readerId = name;
        this.image = img;
        this.isFavorite = SettingsHelper.getBoolean("isFavorite_" + id);
        this.isDownloaded = id > 0 && isDownloaded(this.id);
        this.stream = id > 0 ? stream(id) : null;
        this.fileName = id > 0 ? fileName(id) : null;
    }

    int getReaderId() {
        return readerId;
    }

    public String getReader() {
        return ActivityTales.getActivity().getString(readerId);
    }

    public String getTitle() {
        return ActivityTales.getActivity().getString(titleId);
    }

    public void resetFavorite() {
        boolean downloadAll = SettingsHelper.getBoolean("downloadAllTales");
        boolean downloadFavorite = SettingsHelper.getBoolean("downloadFavoriteTales");

        isFavorite = !isFavorite;
        SettingsHelper.setBoolean("isFavorite_" + id, isFavorite);

        if ( isFavorite && downloadFavorite && !downloadAll) this.download();
        if (!isFavorite && downloadFavorite && !downloadAll) this.deleteFile();
    }

    public boolean shouldBeShown(boolean showOnlyFavorite, List<Integer> categories, String filter) {
        return matchesFilter(filter)
                && (!showOnlyFavorite || isFavorite)
                && shouldBeShown(categories);
    }

    boolean shouldBeShown(List<Integer> categories) {
        List<Integer> categoryTaleIds = Categories
                .Items
                .stream()
                .filter(category -> categories.contains(category.title))
                .flatMap(category -> category.taleIds.stream())
                .toList();

        return categoryTaleIds.contains(this.id);
    }

    boolean matchesFilter(String filter) {
        filter = filter.toLowerCase();
        String reader = StringHelper.substringTo(filter, ",").trim();
        String title = StringHelper.substringFrom(filter, ",").trim();

        if (reader != "")
            return getTitle().toLowerCase().contains(title) && getReader().toLowerCase().contains(reader);

        return getTitle().toLowerCase().contains(filter) || getReader().toLowerCase().contains(filter);
    }

    @SuppressLint("DefaultLocale")
    private String fileName(int tale) {
        return String.format("%d.mp3", tale);
    }

    public boolean isDownloaded(int tale) {
        try{
            return MainActivity.getActivity().getFileStreamPath(fileName(tale)).exists();
        } catch (Exception ex) {
            return false;
        }
    }

    private String stream(int tale) {
        return isDownloaded(tale)
            ? MainActivity.getActivity().getFilesDir() + "/" + fileName(tale)
            : ActivityTales.getActivity().getString(R.string.gitTaleUrl, tale);
    }

    public void download() {
        new DownloadTrack().execute(this);
    }

    public void deleteFile() {
        MainActivity.getActivity().deleteFile(fileName);
        TaleView taleView = new TaleView(this);
        taleView.setDownloadedIcon();
    }

    static class DownloadTrack extends AsyncTask<Tale, Void, Void> {
        private Tale tale;

        @Override
        protected void onPostExecute(Void result) {
            TaleView taleView = new TaleView(tale);
            taleView.setDownloadedIcon();
        }

        @Override
        protected Void doInBackground(Tale... tales) {
            try {
                tale = tales[0];
                if (!tale.isDownloaded)
                {
                    InputStream is = (InputStream) new URL(tale.stream).getContent();
                    SettingsHelper.saveFile(tale.fileName, IOUtils.toByteArray(is));
                }
            }catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
package media.suspilne.kazky.data;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.util.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.R;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.activities.ActivityTales;
import media.suspilne.kazky.helpers.ImageHelper;
import media.suspilne.kazky.helpers.StringHelper;

public class Tale{
    public int id;
    public int introTime;
    public int coloring;
    private int titleId;
    private int readerId;
    public int image;
    public boolean isFavorite;
    public boolean isDownloaded;
    public String stream;
    public String fileName;
    String duration;

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

    private View getTaleView() {
        try{
            return ActivityTales.getActivity().findViewById(R.id.itemsList).findViewWithTag(id);
        }
        catch (Exception e) {
            return null;
        }
    }

    public void resetFavorite(String filterPrefix) {
        boolean downloadAll = SettingsHelper.getBoolean("downloadAllTales");
        boolean downloadFavorite = SettingsHelper.getBoolean("downloadFavoriteTales");

        isFavorite = !isFavorite;
        SettingsHelper.setBoolean("isFavorite_" + id, isFavorite);

        ((ImageView)getTaleView().findViewById(R.id.favorite)).setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);

        if ( isFavorite && downloadFavorite && !downloadAll) this.download();
        if (!isFavorite && downloadFavorite && !downloadAll) this.deleteFile();
        if (!isFavorite && Tales.getShowOnlyFavorite(filterPrefix)) Tales.setTalesCountUpdated(false);
    }

    private void setDownloadedIcon() {
        View taleView = getTaleView();

        if (taleView != null) {
            isDownloaded = isDownloaded(id);
            getTaleView().findViewById(R.id.downloaded).setVisibility(isDownloaded ? View.VISIBLE : View.GONE);
            getTaleView().findViewById(R.id.downloaded_shadow).setVisibility(isDownloaded ? View.VISIBLE : View.GONE);
        }
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

    public void scrollIntoView() {
        try
        {
            ScrollView scrollView = ActivityTales.getActivity().findViewById(R.id.scrollView);
            if (scrollView == null) return;

            View tale = getTaleView();

            if (tale == null) return;

            int x = 0;
            int y = (int)getTaleView().getY();

            scrollView.postDelayed(() -> scrollView.scrollTo(x, y), 300);
        }
        catch (Exception e) {
            Kazky.logError(e.getMessage());
        }
    }

    public void hide() {
        View tale = getTaleView();
        if (tale != null) tale.setVisibility(View.GONE);
    }

    public void show() {
        View tale = getTaleView();
        if (tale != null) tale.setVisibility(View.VISIBLE);
    }

    public void setViewDetails() {
        try
        {
            Bitmap preview = null;
            View taleView = getTaleView();

            try {
                preview = ImageHelper.getBitmapFromResource(MainActivity.getActivity().getResources(), image);
            }
            catch(OutOfMemoryError outOfMemoryError) {
                Kazky.logError("Failed to load tale #" + id + " preview image", false);
                Kazky.logError(outOfMemoryError.getMessage());
            }
            TextView title = taleView.findViewById(R.id.title);
            TextView reader = taleView.findViewById(R.id.reader);
            TextView duration = taleView.findViewById(R.id.duration);

            int color = SettingsHelper.getColor();

            ((ImageView)taleView.findViewById(R.id.favorite)).setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);
            if (preview != null) ((ImageView)taleView.findViewById(R.id.preview)).setImageBitmap(preview);

            title.setText(titleId);
            title.setTextColor(color);

            reader.setText(readerId);
            reader.setTextColor(color);

            duration.setText(this.duration);
            duration.setTextColor(color);

            setDownloadedIcon();
        }catch (Exception e) {
            Kazky.logError("Failed to load tale #" + id, false);
            Kazky.logError(e.getMessage());

            SettingsHelper.setBoolean("showBigImages", false);
        }
    }

    public void setColoringDetails(boolean showBigImages) {
        try
        {
            Bitmap preview = null;
            View taleView = getTaleView();

            try {
                preview = ImageHelper.getBitmapFromResource(MainActivity.getActivity().getResources(), image);
            }
            catch(OutOfMemoryError outOfMemoryError) {
                Kazky.logError("Failed to load tale #" + id + " preview image", false);
                Kazky.logError(outOfMemoryError.getMessage());
            }
            TextView title = taleView.findViewById(R.id.title);
            TextView reader = taleView.findViewById(R.id.reader);
            TextView duration = taleView.findViewById(R.id.duration);

            int color = SettingsHelper.getColor();

            if (preview != null) ((ImageView)taleView.findViewById(R.id.preview)).setImageBitmap(preview);

            title.setText(titleId);
            title.setTextColor(color);

            reader.setText(readerId);
            reader.setTextColor(color);

            duration.setText("");

            if (showBigImages) ((ImageView)taleView.findViewById(R.id.favoriteShadow)).setVisibility(View.INVISIBLE);
            ((ImageView)taleView.findViewById(R.id.favorite)).setVisibility(View.INVISIBLE);
            ((ImageView)taleView.findViewById(R.id.play)).setImageResource(R.mipmap.download);
        }catch (Exception e) {
            Kazky.logError("Failed to load tale #" + id, false);
            Kazky.logError(e.getMessage());

            SettingsHelper.setBoolean("showBigImages", false);
        }
    }

    @SuppressLint("DefaultLocale")
    private String fileName(int tale) {
        return String.format("%d.mp3", tale);
    }

    private boolean isDownloaded(int tale) {
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
        setDownloadedIcon();
    }

    static class DownloadTrack extends AsyncTask<Tale, Void, Void> {
        private Tale tale;

        @Override
        protected void onPostExecute(Void result) {
            tale.isDownloaded = true;
            tale.setDownloadedIcon();
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
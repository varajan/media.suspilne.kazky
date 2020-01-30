package media.suspilne.kazky;

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

public class Tale{
    public int id;
    private int titleId;
    private int readerId;
    public int image;
    boolean isFavorite;
    boolean isDownloaded;
    String stream;
    String fileName;
    String duration;

    Tale(){ id = -1; }

    Tale(int id, String duration, int title, int name, int img){
        this.id = id;
        this.duration = duration;
        this.titleId = title;
        this.readerId = name;
        this.image = img;
        this.isFavorite = SettingsHelper.getBoolean("isFavorite_" + id);
        this.isDownloaded = isDownloaded(this.id);
        this.stream = stream(id);
        this.fileName = fileName(id);
    }

    String getReader(){
        return ActivityTales.getActivity().getResources().getString(readerId);
    }

    String getTitle(){
        return ActivityTales.getActivity().getResources().getString(titleId);
    }

    private View getTaleView(){
        try{
            return ActivityTales.getActivity().findViewById(R.id.talesList).findViewWithTag(id);
        }
        catch (Exception e) {
            return null;
        }
    }

    void resetFavorite(){
        boolean downloadAll = SettingsHelper.getBoolean("downloadAllTales");
        boolean downloadFavorite = SettingsHelper.getBoolean("downloadFavoriteTales");

        isFavorite = !isFavorite;
        SettingsHelper.setBoolean("isFavorite_" + id, isFavorite);

        ((ImageView)getTaleView().findViewById(R.id.favorite)).setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);

        if ( isFavorite && downloadFavorite && !downloadAll) this.download();
        if (!isFavorite && downloadFavorite && !downloadAll) this.deleteFile();
    }

    private void setDownloadedIcon(){
        View taleView = getTaleView();

        if (taleView != null){
            isDownloaded = isDownloaded(id);
            getTaleView().findViewById(R.id.downloaded).setVisibility(isDownloaded ? View.VISIBLE : View.GONE);
            getTaleView().findViewById(R.id.downloaded_shadow).setVisibility(isDownloaded ? View.VISIBLE : View.GONE);
        }
    }

    boolean shouldBeShown(boolean showOnlyFavorite, String filter){
        return (!showOnlyFavorite || isFavorite) && matchesFilter(filter);
    }

    boolean matchesFilter(String filter){
        filter = filter.toLowerCase();
        return getTitle().toLowerCase().contains(filter) || getReader().toLowerCase().contains(filter);
    }

    void scrollIntoView(){
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
        catch (Exception e){
            Kazky.logError(e.getMessage());
        }
    }

    void hide(){
        View tale = getTaleView();
        if (tale != null) tale.setVisibility(View.GONE);
    }

    void show(){
        View tale = getTaleView();
        if (tale != null) tale.setVisibility(View.VISIBLE);
    }

    void setViewDetails(){
        try
        {
            Bitmap preview = null;
            View taleView = getTaleView();

            try {
                preview = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), image);
            }
            catch(OutOfMemoryError outOfMemoryError){
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
        }catch (Exception e){
            Kazky.logError("Failed to load tale #" + id, false);
            Kazky.logError(e.getMessage());
        }
    }

    @SuppressLint("DefaultLocale")
    private String fileName(int tale){
        return String.format("%d.mp3", tale);
    }

    private boolean isDownloaded(int tale){
        return ActivityMain.getActivity().getFileStreamPath(fileName(tale)).exists();
    }

    private String stream(int tale){
        return isDownloaded(tale)
            ? ActivityMain.getActivity().getFilesDir() + "/" + fileName(tale)
            : ActivityTales.getActivity().getResources().getString(R.string.taleUrl, tale);
    }

    public void download(){
        new DownloadTrack().execute(this);
    }

    public void deleteFile(){
        ActivityMain.getActivity().deleteFile(fileName);
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
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }
}
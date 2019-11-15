package media.suspilne.kazky;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
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
    boolean isFavorite;
    boolean isDownloaded;
    String stream;
    String fileName;

    Tale(){ id = -1; }

    Tale(int id, int title, int name){
        this.id = id;
        this.titleId = title;
        this.readerId = name;
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
            scrollView.postDelayed(() -> scrollView.scrollTo(0, (int)getTaleView().getY()), 300);
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
            String file = String.format("%02d.jpg", id);
            Drawable image = SettingsHelper.fileExists(file) ? SettingsHelper.getImage(file) : ContextCompat.getDrawable(ActivityMain.getActivity(), R.mipmap.logo);
            Bitmap preview = ImageHelper.getBitmap(image);
            View taleView = getTaleView();

            ((ImageView)taleView.findViewById(R.id.favorite)).setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);
            ((ImageView)taleView.findViewById(R.id.photo)).setImageBitmap(preview);
            ((TextView) taleView.findViewById(R.id.title)).setText(titleId);
            ((TextView) taleView.findViewById(R.id.reader)).setText(readerId);
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
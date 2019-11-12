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
    private int authorNameId;
    boolean isFavorite;
    boolean isDownloaded;
    String stream;
    String fileName;

    Tale(){ id = -1; }

    Tale(int id, int title, int name){
        this.id = id;
        this.titleId = title;
        this.authorNameId = name;
        this.isFavorite = SettingsHelper.getBoolean("isFavorite_" + id);
        this.isDownloaded = isDownloaded(this.id);
        this.stream = stream(id);
        this.fileName = fileName(id);
    }

    int getAuthorId(){
        return authorNameId;
    }

    String getAuthor(){
        return ActivityTales.getActivity().getResources().getString(authorNameId);
    }

    String getTitle(){
        return ActivityTales.getActivity().getResources().getString(titleId);
    }

    private View getTrackView(){
        try{
            return ActivityTales.getActivity().findViewById(R.id.talesList).findViewWithTag(id);
        }
        catch (Exception e) {
            return null;
        }
    }

    void resetFavorite(){
        boolean downloadAll = SettingsHelper.getBoolean("downloadAlltales");
        boolean downloadFavorite = SettingsHelper.getBoolean("downloadFavoritetales");

        isFavorite = !isFavorite;
        SettingsHelper.setBoolean("isFavorite_" + id, isFavorite);

        ((ImageView)getTrackView().findViewById(R.id.favorite)).setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);

        if ( isFavorite && downloadFavorite && !downloadAll) this.download();
        if (!isFavorite && downloadFavorite && !downloadAll) this.deleteFile();
    }

    private void setDownloadedIcon(){
        View trackView = getTrackView();

        if (trackView != null){
            isDownloaded = isDownloaded(id);
            getTrackView().findViewById(R.id.downloaded).setVisibility(isDownloaded ? View.VISIBLE : View.GONE);
        }
    }

    boolean shouldBeShown(boolean showOnlyFavorite, String filter){
        return (!showOnlyFavorite || isFavorite) && matchesFilter(filter);
    }

    boolean matchesFilter(String filter){
        filter = filter.toLowerCase();
        return getTitle().toLowerCase().contains(filter) || getAuthor().toLowerCase().contains(filter);
    }

    void scrollIntoView(){
        try
        {
            ScrollView scrollView = ActivityTales.getActivity().findViewById(R.id.scrollView);
            if (scrollView == null) return;

            View track = getTrackView();

            if (track == null) return;
            scrollView.postDelayed(() -> scrollView.scrollTo(0, (int)getTrackView().getY()), 300);
        }
        catch (Exception e){
            Kazky.logError(e.getMessage());
        }
    }

    void hide(){
        View track = getTrackView();
        if (track != null) track.setVisibility(View.GONE);
    }

    void show(){
        View track = getTrackView();
        if (track != null) track.setVisibility(View.VISIBLE);
    }

    void setViewDetails(){
        try
        {
            Bitmap author = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), new Reader(authorNameId).photo, 100, 100);
            author = ImageHelper.getCircularDrawable(author);
            View trackView = getTrackView();

            ((ImageView)trackView.findViewById(R.id.favorite)).setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);
            ((ImageView)trackView.findViewById(R.id.photo)).setImageBitmap(author);
            ((TextView) trackView.findViewById(R.id.title)).setText(titleId);
            ((TextView) trackView.findViewById(R.id.reader)).setText(authorNameId);
            setDownloadedIcon();
        }catch (Exception e){
            Kazky.logError("Failed to load track #" + id, false);
            Kazky.logError(e.getMessage());
        }
    }

    @SuppressLint("DefaultLocale")
    private String fileName(int track){
        return String.format("%d.mp3", track);
    }

    private boolean isDownloaded(int track){
        return ActivityMain.getActivity().getFileStreamPath(fileName(track)).exists();
    }

    private String stream(int track){
        return isDownloaded(track)
            ? ActivityMain.getActivity().getFilesDir() + "/" + fileName(track)
            : ActivityTales.getActivity().getResources().getString(R.string.trackUrl, track);
    }

    public void download(){
        new DownloadTrack().execute(this);
    }

    public void deleteFile(){
        ActivityMain.getActivity().deleteFile(fileName);
        setDownloadedIcon();
    }

    static class DownloadTrack extends AsyncTask<Tale, Void, Void> {
        private Tale track;

        @Override
        protected void onPostExecute(Void result) {
            track.isDownloaded = true;
            track.setDownloadedIcon();
        }

        @Override
        protected Void doInBackground(Tale... tales) {
            try {
                track = tales[0];
                if (!track.isDownloaded)
                {
                    InputStream is = (InputStream) new URL(track.stream).getContent();
                    SettingsHelper.saveFile(track.fileName, IOUtils.toByteArray(is));
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }
}
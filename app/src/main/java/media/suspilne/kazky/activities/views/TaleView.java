package media.suspilne.kazky.activities.views;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.R;
import media.suspilne.kazky.activities.ActivityTales;
import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.data.Tale;
import media.suspilne.kazky.helpers.ImageHelper;
import media.suspilne.kazky.helpers.SettingsHelper;

public class TaleView {
    Tale taleData;

    public TaleView(Tale taleData) {
        this.taleData = taleData;
    }

    public void hide() {
        View tale = getView();
        if (tale != null) tale.setVisibility(View.GONE);
    }

    public void show() {
        View tale = getView();
        if (tale != null) tale.setVisibility(View.VISIBLE);
    }

    public void setFavoriteIcon() {
        int icon = taleData.isFavorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite;
        ImageView iconView = getView().findViewById(R.id.favorite);
        iconView.setImageResource(icon);
    }

    public void setDownloadedIcon() {
        View taleView = getView();

        if (taleView != null) {
            boolean isDownloaded = taleData.isDownloaded(taleData.id);
            taleView.findViewById(R.id.downloaded).setVisibility(isDownloaded ? View.VISIBLE : View.GONE);
            taleView.findViewById(R.id.downloaded_shadow).setVisibility(isDownloaded ? View.VISIBLE : View.GONE);
        }
    }

    public void setViewDetails() {
        try
        {
            Bitmap preview = null;
            View taleView = getView();

            try {
                preview = ImageHelper.getBitmapFromResource(MainActivity.getActivity().getResources(), taleData.image);
            }
            catch(OutOfMemoryError outOfMemoryError) {
                Kazky.logError("Failed to load tale #" + taleData.id + " preview image", false);
                Kazky.logError(outOfMemoryError.getMessage());
            }
            TextView title = taleView.findViewById(R.id.title);
            TextView reader = taleView.findViewById(R.id.reader);
            TextView duration = taleView.findViewById(R.id.duration);

            int color = SettingsHelper.getColor();

            ((ImageView)taleView.findViewById(R.id.favorite)).setImageResource(taleData.isFavorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);
            if (preview != null) ((ImageView)taleView.findViewById(R.id.preview)).setImageBitmap(preview);

            title.setText(taleData.titleId);
            title.setTextColor(color);

            reader.setText(taleData.readerId);
            reader.setTextColor(color);

            duration.setText(taleData.duration);
            duration.setTextColor(color);

            setDownloadedIcon();
        }catch (Exception e) {
            Kazky.logError("Failed to load tale #" + taleData.id, false);
            Kazky.logError(e.getMessage());

            SettingsHelper.setBoolean("showBigImages", false);
        }
    }

    public void setColoringDetails(boolean showBigImages) {
        try
        {
            Bitmap preview = null;
            View taleView = getView();

            try {
                preview = ImageHelper.getBitmapFromResource(MainActivity.getActivity().getResources(), taleData.image);
            }
            catch(OutOfMemoryError outOfMemoryError) {
                Kazky.logError("Failed to load tale #" + taleData.id + " preview image", false);
                Kazky.logError(outOfMemoryError.getMessage());
            }
            TextView title = taleView.findViewById(R.id.title);
            TextView reader = taleView.findViewById(R.id.reader);
            TextView duration = taleView.findViewById(R.id.duration);

            int color = SettingsHelper.getColor();

            if (preview != null) ((ImageView)taleView.findViewById(R.id.preview)).setImageBitmap(preview);

            title.setText(taleData.titleId);
            title.setTextColor(color);

            reader.setText(taleData.readerId);
            reader.setTextColor(color);

            duration.setText("");

            if (showBigImages) ((ImageView)taleView.findViewById(R.id.favoriteShadow)).setVisibility(View.INVISIBLE);
            ((ImageView)taleView.findViewById(R.id.favorite)).setVisibility(View.INVISIBLE);
            ((ImageView)taleView.findViewById(R.id.play)).setImageResource(R.mipmap.download);
        }catch (Exception e) {
            Kazky.logError("Failed to load tale #" + taleData.id, false);
            Kazky.logError(e.getMessage());

            SettingsHelper.setBoolean("showBigImages", false);
        }
    }

    public void scrollIntoView() {
        try
        {
            ScrollView scrollView = ActivityTales.getActivity().findViewById(R.id.scrollView);
            if (scrollView == null) return;

            View tale = getView();

            if (tale == null) return;

            int x = 0;
            int y = (int)getView().getY();

            scrollView.postDelayed(() -> scrollView.scrollTo(x, y), 300);
        }
        catch (Exception e) {
            Kazky.logError(e.getMessage());
        }
    }

    private View getView() {
        try{
            return ActivityTales.getActivity().findViewById(R.id.itemsList).findViewWithTag(taleData.id);
        }
        catch (Exception e) {
            return null;
        }
    }
}

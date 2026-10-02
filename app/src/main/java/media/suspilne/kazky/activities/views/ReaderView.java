package media.suspilne.kazky.activities.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.R;
import media.suspilne.kazky.activities.ActivityReaders;
import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.data.Reader;
import media.suspilne.kazky.helpers.ImageHelper;
import media.suspilne.kazky.helpers.SettingsHelper;

public class ReaderView {
    Reader readerData;

    public ReaderView(Reader readerData) {
        this.readerData = readerData;
    }

    public void hide() { getView().setVisibility(View.GONE); }

    public void show(Context context, Integer talesCount) {

        getView().setVisibility(View.VISIBLE);
        View readerView = getReaderView();
        TextView description = readerView.findViewById(R.id.description);

        description.setText(context.getString(R.string.reader_description, readerData.getDescription(), talesCount));
    }

    public void setViewDetails() {
        try
        {
            Bitmap photo = ImageHelper.getBitmapFromResource(MainActivity.getActivity().getResources(), readerData.photo, 100, 100);
            photo = readerData.photo.equals(R.mipmap.logo) ? photo : ImageHelper.getCircularDrawable(photo);
            int color = SettingsHelper.getColor();

            View readerView = getReaderView();
            TextView reader = readerView.findViewById(R.id.reader);
            TextView description = readerView.findViewById(R.id.description);

            ((ImageView)readerView.findViewById(R.id.photo)).setImageBitmap(photo);

            reader.setText(readerData.name);
            reader.setTextColor(color);
            description.setTextColor(color);
        } catch (Exception e) {
            Log.e(Kazky.Constants.application, e.getMessage());
            Log.e(Kazky.Constants.application, e.getStackTrace().toString());
            e.printStackTrace();
        }
    }

    private View getView() {
        return ActivityReaders.getActivity().findViewById(R.id.itemsList).findViewWithTag(readerData.getName());
    }

    private View getReaderView() {
        return MainActivity.getActivity().findViewById(R.id.itemsList).findViewWithTag(readerData.getName());
    }
}

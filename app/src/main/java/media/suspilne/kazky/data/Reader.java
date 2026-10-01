package media.suspilne.kazky.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.R;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.activities.ActivityReaders;
import media.suspilne.kazky.helpers.ImageHelper;

public class Reader {
    public Integer name;
    public Integer description;
    public Integer photo;
    public Integer talesCount = 0;

    public Reader(int name, int description) {
        this.name = name;
        this.description = description;
        this.photo = getPhoto();
    }

    public Integer getMatchedTales(String filter, boolean showOnlyFavorite, List<Integer> categories) {
        List<Integer> categoryTales = Categories.Items.stream()
                .filter(c -> categories.contains(c.title))
                .flatMap(c -> c.taleIds.stream())
                .collect(Collectors.toList());

        List<Tale> allReaderTales = new Tales().items.stream()
                .filter(t -> Objects.equals(t.readerName, this.getName()))
                .filter(t -> !showOnlyFavorite || t.isFavorite)
                .filter(t -> categoryTales.contains(t.id))
                .collect(Collectors.toList());

        String finalFilter = filter.toLowerCase().trim();
        boolean matchName = getName().toLowerCase().contains(finalFilter) || getDescription().toLowerCase().contains(finalFilter);
        List<Tale> talesMatchFilter = allReaderTales.stream().filter(t -> t.title.toLowerCase().contains(finalFilter))
                .collect(Collectors.toList());

        return matchName
                ? allReaderTales.size()
                : talesMatchFilter.size();
    }

    private View getView() {
        return ActivityReaders.getActivity().findViewById(R.id.itemsList).findViewWithTag(getName());
    }

    public void hide() { getView().setVisibility(View.GONE); }

    public void show(Context context, Integer talesCount) {

        getView().setVisibility(View.VISIBLE);
        View readerView = getReaderView();
        TextView description = readerView.findViewById(R.id.description);

        description.setText(context.getString(R.string.reader_description, getDescription(), talesCount));
    }

    public String getName() {
        return MainActivity.getActivity().getString(name);
    }
    public String getDescription() {
        return MainActivity.getActivity().getString(description);
    }

    private View getReaderView() {
        return MainActivity.getActivity().findViewById(R.id.itemsList).findViewWithTag(getName());
    }

    public void setViewDetails() {
        try
        {
            Bitmap photo = ImageHelper.getBitmapFromResource(MainActivity.getActivity().getResources(), this.photo, 100, 100);
            photo = this.photo.equals(R.mipmap.logo) ? photo : ImageHelper.getCircularDrawable(photo);
            int color = SettingsHelper.getColor();

            View readerView = getReaderView();
            TextView reader = readerView.findViewById(R.id.reader);
            TextView description = readerView.findViewById(R.id.description);

            ((ImageView)readerView.findViewById(R.id.photo)).setImageBitmap(photo);

            reader.setText(name);
            reader.setTextColor(color);
            description.setTextColor(color);
        } catch (Exception e) {
            Log.e(Kazky.Constants.application, e.getMessage());
            Log.e(Kazky.Constants.application, e.getStackTrace().toString());
            e.printStackTrace();
        }
    }

    @SuppressLint("DiscouragedApi")
    private int getPhoto() {
        Activity activity = MainActivity.getActivity();
        String resourceId = activity.getResources().getResourceEntryName(name);

        return activity.getResources().getIdentifier(
                resourceId,
                "mipmap",
                activity.getPackageName());
    }
}

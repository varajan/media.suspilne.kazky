package media.suspilne.kazky.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.R;
import media.suspilne.kazky.activities.views.TaleView;
import media.suspilne.kazky.helpers.AssetUtils;
import media.suspilne.kazky.helpers.JsonUtils;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.activities.ActivityTales;
import media.suspilne.kazky.helpers.StringHelper;
import media.suspilne.kazky.tasks.DownloadTaleTask;

public class Tale {
    public int id;
    public int introTime;
    public boolean hasColoring;
    public int readerId;
    public int image;
    public boolean isFavorite;
    public boolean isDownloaded;
    public String stream;
    public String fileName;
    public String duration;
    private final String isFavoriteKey = "isFavorite_";

    private static final List<TaleInfo> staticInfo;

    static {
        String jsonString = AssetUtils.loadJSON(MainActivity.getActivity(), "talesInfo.json");
        Type listType = new TypeToken<List<TaleInfo>>() {}.getType();
        staticInfo = JsonUtils.fromJson(jsonString, listType);
    }

    Tale() { id = -1; }

    Tale(int id, int intro, int readerName) {
        TaleInfo taleInfo = staticInfo.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);

        this.id = id;
        this.introTime = intro;
        this.hasColoring = taleInfo.isHasColoring();
        this.duration = "⏱ " + taleInfo.getDuration();
        this.readerId = readerName;
        this.image = this.getTaleImage();
        this.isFavorite = SettingsHelper.getBoolean(isFavoriteKey + id);
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
        Context context = ActivityTales.getActivity();
        String resourceName = String.format(Locale.US, "tale_%03d", this.id);

        @SuppressLint("DiscouragedApi")
        int resId = context.getResources().getIdentifier(resourceName, "string", context.getPackageName());

        return context.getString(resId);
    }

    public void resetFavorite() {
        boolean downloadAll = SettingsHelper.getBoolean(Kazky.Constants.downloadAllTales);
        boolean downloadFavorite = SettingsHelper.getBoolean(Kazky.Constants.downloadFavoriteTales);

        isFavorite = !isFavorite;
        SettingsHelper.setBoolean(isFavoriteKey + id, isFavorite);

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
                .collect(Collectors.toList());

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
        try {
            return MainActivity.getActivity().getFileStreamPath(fileName(tale)).exists();
        } catch (Exception ex) {
            return false;
        }
    }

    @SuppressLint("DiscouragedApi")
    private int getTaleImage() {
        Activity activity = MainActivity.getActivity();
        boolean showBigImages = SettingsHelper.getBoolean(Kazky.Constants.showBigImages);
        String imageName = String.format(Locale.US, "t%03d%s", id, showBigImages ? "" : "_min");

        return activity.getResources().getIdentifier(
                imageName,
                "drawable",
                activity.getPackageName()
        );
    }

    private String stream(int tale) {
        return isDownloaded(tale)
            ? MainActivity.getActivity().getFilesDir() + "/" + fileName(tale)
            : ActivityTales.getActivity().getString(R.string.gitTaleUrl, tale);
    }

    public void download() {
        new DownloadTaleTask().execute(this);
    }

    public void deleteFile() {
        MainActivity.getActivity().deleteFile(fileName);
        TaleView taleView = new TaleView(this);
        taleView.setDownloadedIcon();
    }
}
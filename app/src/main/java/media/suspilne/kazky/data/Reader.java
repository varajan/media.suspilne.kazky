package media.suspilne.kazky.data;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import media.suspilne.kazky.activities.MainActivity;

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

    public String getName() {
        return MainActivity.getActivity().getString(name);
    }
    public String getDescription() {
        return MainActivity.getActivity().getString(description);
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

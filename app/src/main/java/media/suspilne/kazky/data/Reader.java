package media.suspilne.kazky.data;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.data.dto.ReaderDto;

public class Reader {
    public String name;
    public String description;
    public Integer photo;
    public Integer talesCount = 0;

    public Reader(ReaderDto reader) {
        this.name = reader.name();
        this.description = reader.description();
        this.photo = getPhoto(reader.id());
    }

    public Integer getMatchedTales(String filter, boolean showOnlyFavorite, List<Integer> categories) {
        List<Integer> categoryTales = Categories.Items.stream()
                .filter(c -> categories.contains(c.title))
                .flatMap(c -> c.taleIds.stream())
                .collect(Collectors.toList());

        List<Tale> allReaderTales = new Tales().items.stream()
                .filter(t -> Objects.equals(t.readerName, this.name))
                .filter(t -> !showOnlyFavorite || t.isFavorite)
                .filter(t -> categoryTales.contains(t.id))
                .collect(Collectors.toList());

        String finalFilter = filter.toLowerCase().trim();
        boolean matchName = name.toLowerCase().contains(finalFilter) || description.toLowerCase().contains(finalFilter);
        List<Tale> talesMatchFilter = allReaderTales.stream().filter(t -> t.title.toLowerCase().contains(finalFilter))
                .collect(Collectors.toList());

        return matchName
                ? allReaderTales.size()
                : talesMatchFilter.size();
    }

    @SuppressLint("DiscouragedApi")
    private int getPhoto(String resourceId) {
        Activity activity = MainActivity.getActivity();

        return activity.getResources().getIdentifier(
                resourceId,
                "mipmap",
                activity.getPackageName());
    }
}

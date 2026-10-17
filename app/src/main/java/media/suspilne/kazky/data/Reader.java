package media.suspilne.kazky.data;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public Integer getMatchedTales(String filter, boolean showOnlyFavorite, List<String> categories) {
        if (filter == null) {
            filter = "";
        }
        String finalFilter = filter.toLowerCase().trim();

        Set<String> categorySet = new HashSet<>(categories);

        Set<Integer> categoryTalesSet = Categories.Items.stream()
                .filter(c -> categorySet.contains(c.title()))
                .flatMap(c -> c.taleIds().stream())
                .collect(Collectors.toSet());

        boolean matchName = name.toLowerCase().contains(finalFilter) || description.toLowerCase().contains(finalFilter);

        Stream<Tale> readerTalesStream = Tales.items.stream()
                .filter(t -> Objects.equals(t.readerName, this.name))
                .filter(t -> !showOnlyFavorite || t.isFavorite)
                .filter(t -> categoryTalesSet.contains(t.id));

        if (matchName) {
            return (int) readerTalesStream.count();
        }

        long count = readerTalesStream
                .filter(t -> t.title != null && t.title.toLowerCase().contains(finalFilter))
                .count();

        return (int) count;
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

package media.suspilne.kazky.data;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.data.dto.ReaderDto;
import media.suspilne.kazky.helpers.AssetUtils;
import media.suspilne.kazky.helpers.JsonUtils;
import media.suspilne.kazky.helpers.SettingsHelper;

public class Readers {
    private static final List<ReaderDto> staticReadersData;

    static {
        String jsonString = AssetUtils.loadJSON(MainActivity.getActivity(), "readers.json");
        Type listType = new TypeToken<List<ReaderDto>>() {}.getType();
        staticReadersData = JsonUtils.fromJson(jsonString, listType);
    }

    public List<Reader> Readers;

    public Readers() {
        List<Reader> items = staticReadersData.stream()
                .map(Reader::new)
                .collect(Collectors.toList());

        if (isAscSorted()) {
            items.sort(Comparator.comparing(c -> c.name));
        } else {
            items.sort((c1, c2) -> c2.talesCount.compareTo(c1.talesCount));
        }

        Readers = items;
    }

    public static void setAscSorting(boolean value) {
        SettingsHelper.setBoolean(Kazky.Constants.isAscSorted, value);
    }

    public static boolean isAscSorted() {
        return SettingsHelper.getBoolean(Kazky.Constants.isAscSorted);
    }
}

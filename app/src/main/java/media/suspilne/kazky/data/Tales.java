package media.suspilne.kazky.data;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.helpers.AssetUtils;
import media.suspilne.kazky.helpers.JsonUtils;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.helpers.ListHelper;

public class Tales {
    private static final List<TaleInfo> staticTalesInfo;

    static {
        String jsonString = AssetUtils.loadJSON(MainActivity.getActivity(), "tales.json");
        Type listType = new TypeToken<List<TaleInfo>>() {}.getType();
        staticTalesInfo = JsonUtils.fromJson(jsonString, listType);
    }

    private static final String playerPositionKey = "PlayerPosition";
    private static final String onlyFavoriteKey = "_showOnlyFavorite";

    public static boolean getShowCategory(String filterPrefix, String category) { return SettingsHelper.getBoolean(filterPrefix + "_show_" + category); }
    public static void setShowCategory(String filterPrefix, String category, boolean value) {
        SettingsHelper.setBoolean(filterPrefix + "_show_" + category, value);
    }

    public static boolean getShowOnlyFavorite(String filterPrefix) { return SettingsHelper.getBoolean(filterPrefix + onlyFavoriteKey); }
    public static void setShowOnlyFavorite(String filterPrefix, boolean value) {
        SettingsHelper.setBoolean(filterPrefix + onlyFavoriteKey, value);
    }

    public static String getFilter(String filterPrefix) { return SettingsHelper.getString(filterPrefix + "_talesFilter"); }
    public static void setFilter(String filterPrefix, String filter) { SettingsHelper.setString(filterPrefix + "_talesFilter", filter); }

    public static void setLastPosition(long value) {
        SettingsHelper.setLong(playerPositionKey, value);
    }

    public static long getLastPosition() {
        return SettingsHelper.getLong(playerPositionKey);
    }

    public static void setLastPlaying(int value) {
        SettingsHelper.setInt(Kazky.Constants.talesLastPlaying, value);
    }

    public static int getLastPlaying() {
        return SettingsHelper.getInt(Kazky.Constants.talesLastPlaying);
    }

    public static void setNowPlaying(int value) {
        SettingsHelper.setInt(Kazky.Constants.talesNowPlaying, value);
    }

    public static int getNowPlaying() {
        return SettingsHelper.getInt(Kazky.Constants.talesNowPlaying);
    }

    public static void setPause(boolean value) {
        SettingsHelper.setBoolean(Kazky.Constants.talesPaused, value);
    }

    public static boolean isPaused() {
        return SettingsHelper.getBoolean(Kazky.Constants.talesPaused);
    }

    public Tale getPrevious() {
        boolean skip = true;
        int nowPlaying = getNowPlaying();
        List<String> ids = Arrays.asList( SettingsHelper.getString(Kazky.Constants.filteredTalesList).split(";") );
        Collections.reverse(ids);

        for(String id:ids) {
            int taleId = Integer.parseInt(id);

            if (taleId != nowPlaying && skip) continue;
            if (taleId == nowPlaying) {skip = false; continue;}

            return getById(taleId);
        }

        return ids.isEmpty() ? new Tale() : getById(ids.get(0));
    }

    public Tale getNext() {
        boolean skip = true;
        int nowPlaying = getNowPlaying();
        List<String> ids = ListHelper.removeBlank(SettingsHelper.getString(Kazky.Constants.filteredTalesList).split(";"));

        for(String id:ids) {
            int taleId = Integer.parseInt(id);

            if (taleId != nowPlaying && skip) continue;
            if (taleId == nowPlaying) {skip = false; continue;}

            return getById(taleId);
        }

        return ids.isEmpty() ? new Tale() : getById(ids.get(0));
    }

    Tale getById(String id) {
        return getById(Integer.parseInt(id));
    }

    public Tale getById(int id) {
        for (Tale tale:items) {
            if (tale.id == id) return tale;
        }

        return null;
    }

    int compare(String arg1, String arg2) {
        Collator collator = Collator.getInstance(new Locale("uk", "UA"));
        collator.setStrength(Collator.PRIMARY);

        return collator.compare(arg1, arg2);
    }

    public void setTalesList() {
        String sorting = SettingsHelper.getString(Kazky.Constants.sorting, Kazky.Constants.shuffle);
        StringBuilder list = new StringBuilder();
        List<Tale> result = new ArrayList<>(items);

        switch (sorting) {
            case Kazky.Constants.shuffle:
                Collections.shuffle(result);
                break;

            case Kazky.Constants.sortAsc:
                Collections.shuffle(result);
                if (SettingsHelper.getBoolean(Kazky.Constants.groupByReader)) {
                    result.sort((tale1, tale2)
                            -> tale1.readerName.equals(tale2.readerName)
                            ? compare(tale1.title, tale2.title)
                            : compare(tale1.readerName, tale2.readerName));
                } else {
                    result.sort((tale1, tale2) -> compare(tale1.title, tale2.title));
                }
                break;

            case Kazky.Constants.sort19:
                result.sort((tale1, tale2) -> compare(tale1.duration, tale2.duration));
                break;

            case Kazky.Constants.sort91:
                result.sort((tale1, tale2) -> compare(tale1.duration, tale2.duration));
                Collections.reverse(result);
                break;
        }

        for (Tale tale:result) { list.append(tale.id).append(";"); }

        SettingsHelper.setString(Kazky.Constants.talesList, list.toString());
    }

    public List<Tale> getTalesList(boolean favoriteOnly) {
        List<Tale> result = new ArrayList<>();

        for (Tale tale: getTalesList()) {
            if (!favoriteOnly || tale.isFavorite) result.add(tale);
        }

        return result;
    }

    public List<Tale> getTalesList() {
        List<Tale> result = new ArrayList<>();

        if (SettingsHelper.getString(Kazky.Constants.talesList, "").isEmpty()) setTalesList();

        for(String id:SettingsHelper.getString(Kazky.Constants.talesList).split(";")) {
            result.add(getById(id));
        }

        return result;
    }

    public int getFavoriteCount() {
        int result = 0;

        for(Tale tale:items) if (tale.isFavorite) result++;

        return result;
    }

    public final List<Tale> items = staticTalesInfo.stream()
            .map(Tale::new)
            .collect(Collectors.toList());
}
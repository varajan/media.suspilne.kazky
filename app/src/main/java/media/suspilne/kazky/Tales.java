package media.suspilne.kazky;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Tales {
    String filter = SettingsHelper.getString("talesFilter");
    boolean showOnlyFavorite = SettingsHelper.getBoolean("showOnlyFavorite");

    public static void setLastPosition(long value){
        SettingsHelper.setLong("PlayerPosition", value);
    }

    public static long getLastPosition(){
        return SettingsHelper.getLong("PlayerPosition");
    }

    public static void setLastPlaying(int value) {
        SettingsHelper.setInt("tales.lastPlaying", value);
    }

    public static int getLastPlaying(){
        return SettingsHelper.getInt("tales.lastPlaying");
    }

    public static void setNowPlaying(int value){
        SettingsHelper.setInt("tales.nowPlaying", value);
    }

    public static int getNowPlaying(){
        return SettingsHelper.getInt("tales.nowPlaying");
    }

    public static void setPause(boolean value){
        SettingsHelper.setBoolean("tales.paused", value);
    }

    public static boolean isPaused(){
        return SettingsHelper.getBoolean("tales.paused");
    }

    Tale getPrevious(){
        int nowPlaying = getNowPlaying();
        List<Tale> tales = getTales(showOnlyFavorite, filter);
        boolean skip = (nowPlaying > 0 && getById(nowPlaying).shouldBeShown(showOnlyFavorite, filter));

        for (int i = tales.size() - 1; i >= 0; i--){
            Tale tale = tales.get(i);

            if (tale.id != nowPlaying && skip) { continue; }
            if (tale.id == nowPlaying) { skip = false; continue; }

            return tale;
        }

        return tales.size() == 0 ? new Tale() : tales.get(tales.size() - 1);
    }

    Tale getNext(){
        int nowPlaying = getNowPlaying();
        List<Tale> tales = getTales(showOnlyFavorite, filter);
        boolean skip = (nowPlaying > 0 && getById(nowPlaying).shouldBeShown(showOnlyFavorite, filter));

        for (int i = 0; i < tales.size(); i++){
            Tale tale = tales.get(i);

            if (tale.id != nowPlaying && skip) { continue; }
            if (tale.id == nowPlaying) { skip = false; continue; }

            return tale;
        }

        return tales.size() == 0 ? new Tale() : tales.get(0);
    }

    Tale getById(int id){
        for (Tale tale:getTales()) {
            if (tale.id == id) return tale;
        }

        return null;
    }

    List<Tale> getTales(boolean onlyFavorite, String filter){
        List<Tale> result = new ArrayList<>();

        for (Tale tale:getTales()) {
            if (tale.shouldBeShown(onlyFavorite, filter)){
                result.add(tale);
            }
        }

        return result;
    }

    List<Tale> getTales(boolean onlyFavorite){
        List<Tale> result = new ArrayList<>();

        for (Tale tale:items) {
            if (!onlyFavorite || tale.isFavorite) result.add(tale);
        }

        Collections.sort(result, (tale1, tale2)
                -> tale1.getReader().equals(tale2.getReader())
                ?  tale1.getTitle().compareTo(tale2.getTitle())
                :  tale1.getReader().compareTo(tale2.getReader()));

        return result;
    }

    List<Tale> getTales(){
        return getTales(false);
    }

    private List<Tale> items = new ArrayList<>(Arrays.asList(
            new Tale(1, R.string.tale_001, R.string.andrii_hlyvniuk),
            new Tale(2, R.string.tale_002, R.string.andrii_hlyvniuk),
            new Tale(3, R.string.tale_003, R.string.andrii_hlyvniuk),
            new Tale(4, R.string.tale_004, R.string.marko_galanevych),
            new Tale(5, R.string.tale_005, R.string.marko_galanevych)
    ));
}
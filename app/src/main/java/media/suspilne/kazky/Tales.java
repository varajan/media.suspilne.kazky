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
            Tale track = tales.get(i);

            if (track.id != nowPlaying && skip) { continue; }
            if (track.id == nowPlaying) { skip = false; continue; }

            return track;
        }

        return tales.size() == 0 ? new Tale() : tales.get(tales.size() - 1);
    }

    Tale getNext(){
        int nowPlaying = getNowPlaying();
        List<Tale> tales = getTales(showOnlyFavorite, filter);
        boolean skip = (nowPlaying > 0 && getById(nowPlaying).shouldBeShown(showOnlyFavorite, filter));

        for (int i = 0; i < tales.size(); i++){
            Tale track = tales.get(i);

            if (track.id != nowPlaying && skip) { continue; }
            if (track.id == nowPlaying) { skip = false; continue; }

            return track;
        }

        return tales.size() == 0 ? new Tale() : tales.get(0);
    }

    Tale getById(int id){
        for (Tale track:getTales()) {
            if (track.id == id) return track;
        }

        return null;
    }

    List<Tale> getTales(boolean onlyFavorite, String filter){
        List<Tale> result = new ArrayList<>();

        for (Tale track:getTales()) {
            if (track.shouldBeShown(onlyFavorite, filter)){
                result.add(track);
            }
        }

        return result;
    }

    List<Tale> getTales(boolean onlyFavorite){
        List<Tale> result = new ArrayList<>();

        for (Tale track:items) {
            if (!onlyFavorite || track.isFavorite) result.add(track);
        }

        Collections.sort(result, (track1, track2)
                -> track1.getAuthor().equals(track2.getAuthor())
                ?  track1.getTitle().compareTo(track2.getTitle())
                :  track1.getAuthor().compareTo(track2.getAuthor()));

        return result;
    }

    List<Tale> getTales(){
        return getTales(false);
    }

    private List<Tale> items = new ArrayList<>(Arrays.asList(
//            new Tale(124, R.string.track_124, R.string.elgar),
//            new Tale(125, R.string.track_125, R.string.chaikovsky)
    ));
}
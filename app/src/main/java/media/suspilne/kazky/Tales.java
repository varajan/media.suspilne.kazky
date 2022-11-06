package media.suspilne.kazky;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

class Tales {
    public static boolean getTalesCountUpdated(){ return SettingsHelper.getBoolean("tales.count.updated"); }
    public static void setTalesCountUpdated(boolean value){SettingsHelper.setBoolean("tales.count.updated", value);}

    public static boolean getShowLullabies() { return SettingsHelper.getBoolean("showLullabies"); }
    public static void setShowLullabies(boolean value) {
        SettingsHelper.setBoolean("showLullabies", value);
        setTalesCountUpdated(false);
    }

    public static boolean getShowForBabies() { return SettingsHelper.getBoolean("showBabiesTales"); }
    public static void setShowForBabies(boolean value) {
        SettingsHelper.setBoolean("showBabiesTales", value);
        setTalesCountUpdated(false);
    }

    public static boolean getShowForKids() { return SettingsHelper.getBoolean("showKidsTales"); }
    public static void setShowForKids(boolean value) {
        SettingsHelper.setBoolean("showKidsTales", value);
        setTalesCountUpdated(false);
    }

    public static boolean getShowOnlyFavorite() { return SettingsHelper.getBoolean("showOnlyFavorite"); }
    public static void setShowOnlyFavorite(boolean value) {
        SettingsHelper.setBoolean("showOnlyFavorite", value);
        setTalesCountUpdated(false);
    }

    public static String getFilter(){ return SettingsHelper.getString("talesFilter"); }
    public static void setFilter(String filter){ SettingsHelper.setString("talesFilter", filter); }

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

    public static boolean playTalesFromGit(){ return SettingsHelper.getBoolean("playTalesFromGit"); }

    Tale getPrevious(){
        boolean skip = true;
        int nowPlaying = getNowPlaying();
        List<String> ids = Arrays.asList( SettingsHelper.getString("filteredTalesList").split(";") );
        Collections.reverse(ids);

        for(String id:ids){
            int taleId = Integer.parseInt(id);

            if (taleId != nowPlaying && skip) continue;
            if (taleId == nowPlaying) {skip = false; continue;}

            return getById(taleId);
        }

        return ids.size() == 0 ? new Tale() : getById(ids.get(0));
    }

    Tale getNext(){
        boolean skip = true;
        int nowPlaying = getNowPlaying();
        List<String> ids = ListHelper.removeBlank(SettingsHelper.getString("filteredTalesList").split(";"));

        for(String id:ids){
            int taleId = Integer.parseInt(id);

            if (taleId != nowPlaying && skip) continue;
            if (taleId == nowPlaying) {skip = false; continue;}

            return getById(taleId);
        }

        return ids.size() == 0 ? new Tale() : getById(ids.get(0));
    }

    Tale getById(String id) {
        return getById(Integer.parseInt(id));
    }

    Tale getById(int id){
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

    public void setTalesList(){
        String sorting = SettingsHelper.getString("sorting", "shuffle");
        StringBuilder list = new StringBuilder();
        List<Tale> result = new ArrayList<>(items);

        switch (sorting){
            case "shuffle":
                Collections.shuffle(result);
                break;

            case "sortAsc":
                Collections.shuffle(result);
                if (SettingsHelper.getBoolean("groupByReader")) {
                    Collections.sort(result, (tale1, tale2)
                            -> tale1.getReader().equals(tale2.getReader())
                            ?  compare(tale1.getTitle(), tale2.getTitle())
                            :  compare(tale1.getReader(), tale2.getReader()));
                } else {
                    Collections.sort(result, (tale1, tale2) -> compare(tale1.getTitle(), tale2.getTitle()));
                }
                break;

            case "sort19":
                Collections.sort(result, (tale1, tale2) -> compare(tale1.duration, tale2.duration));
                break;

            case "sort91":
                Collections.sort(result, (tale1, tale2) -> compare(tale1.duration, tale2.duration));
                Collections.reverse(result);
                break;
        }

        for (Tale tale:result) { list.append(tale.id).append(";"); }

        SettingsHelper.setString("talesList", list.toString());
    }

    public List<Tale> getTalesList(boolean favoriteOnly){
        List<Tale> result = new ArrayList<>();

        for (Tale tale: getTalesList()) {
            if (!favoriteOnly || tale.isFavorite) result.add(tale);
        }

        return result;
    }

    public List<Tale> getTalesList(){
        List<Tale> result = new ArrayList<>();

        if (SettingsHelper.getString("talesList", "").length() == 0) setTalesList();

        for(String id:SettingsHelper.getString("talesList").split(";")){
            result.add(getById(id));
        }

        return result;
    }

    int getFavoriteCount(){
        int result = 0;

        for(Tale tale:items) if (tale.isFavorite) result++;

        return result;
    }

    public List<Tale> items = new ArrayList<>(Arrays.asList(
            new Tale(1,  TaleAge.FOR_BABIES,	    "02:04", 7200, 1, R.string.tale_001, R.string.andrii_hlyvniuk,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t001 : R.drawable.t001_min),
            new Tale(2,  TaleAge.FOR_BOTH, 		"02:28", 7200, 1, R.string.tale_002, R.string.andrii_hlyvniuk,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t002 : R.drawable.t002_min),
            new Tale(3,  TaleAge.FOR_KIDS, 		"02:10", 8200, 1, R.string.tale_003, R.string.andrii_hlyvniuk,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t003 : R.drawable.t003_min),
            new Tale(4,  TaleAge.FOR_BABIES, 	"01:11", 7500, 1, R.string.tale_004, R.string.marko_galanevych,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t004 : R.drawable.t004_min),
            new Tale(5,  TaleAge.FOR_BOTH, 		"01:50", 7000, 1, R.string.tale_005, R.string.marko_galanevych,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t005 : R.drawable.t005_min),
            new Tale(6,  TaleAge.FOR_BABIES, 	"02:31", 6500, 1, R.string.tale_006, R.string.alina_pash,           SettingsHelper.getBoolean("showBigImages") ? R.drawable.t006 : R.drawable.t006_min),
            new Tale(7,  TaleAge.FOR_KIDS, 		"02:34", 7000, 1, R.string.tale_007, R.string.sasha_koltsova,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t007 : R.drawable.t007_min),
            new Tale(8,  TaleAge.FOR_BABIES, 	"00:48", 7000, 1, R.string.tale_008, R.string.sasha_koltsova,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t008 : R.drawable.t008_min),
            new Tale(9,  TaleAge.FOR_BABIES, 	"01:44", 7000, 1, R.string.tale_009, R.string.sasha_koltsova,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t009 : R.drawable.t009_min),
            new Tale(10, TaleAge.FOR_BABIES, 	"02:15", 7000, 1, R.string.tale_010, R.string.evgen_maluha,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t010 : R.drawable.t010_min),

            new Tale(11, TaleAge.FOR_BOTH, 		"02:15", 8200, 1, R.string.tale_011, R.string.evgen_maluha,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t011 : R.drawable.t011_min),
            new Tale(12, TaleAge.FOR_BABIES, 	"03:01", 6500, 1, R.string.tale_012, R.string.evgen_maluha,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t012 : R.drawable.t012_min),
            new Tale(13, TaleAge.FOR_KIDS, 		"03:20", 6800, 1, R.string.tale_013, R.string.sergii_zhadan,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t013 : R.drawable.t013_min),
            new Tale(14, TaleAge.FOR_KIDS, 		"03:53", 6700, 1, R.string.tale_014, R.string.sergii_zhadan,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t014 : R.drawable.t014_min),
            new Tale(15, TaleAge.FOR_KIDS, 		"05:01", 6900, 1, R.string.tale_015, R.string.sergii_zhadan,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t015 : R.drawable.t015_min),
            new Tale(16, TaleAge.FOR_KIDS, 		"09:11", 7700, 1, R.string.tale_016, R.string.hrystyna_soloviy,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t016 : R.drawable.t016_min),
            new Tale(17, TaleAge.FOR_KIDS, 		"12:33", 6500, 1, R.string.tale_017, R.string.hrystyna_soloviy,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t017 : R.drawable.t017_min),
            new Tale(18, TaleAge.FOR_KIDS, 		"07:36", 7200, 1, R.string.tale_018, R.string.hrystyna_soloviy,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t018 : R.drawable.t018_min),
            new Tale(19, TaleAge.FOR_BABIES, 	"04:03", 6700, 1, R.string.tale_019, R.string.oleksiy_dorychevsky,  SettingsHelper.getBoolean("showBigImages") ? R.drawable.t019 : R.drawable.t019_min),
            new Tale(20, TaleAge.FOR_BABIES, 	"02:59", 6500, 1, R.string.tale_020, R.string.oleksiy_dorychevsky,  SettingsHelper.getBoolean("showBigImages") ? R.drawable.t020 : R.drawable.t020_min),

            new Tale(21, TaleAge.FOR_BABIES, 	"05:05", 6700, 1, R.string.tale_021, R.string.oleksiy_dorychevsky,  SettingsHelper.getBoolean("showBigImages") ? R.drawable.t021 : R.drawable.t021_min),
            new Tale(22, TaleAge.FOR_BABIES, 	"04:42", 6200, 1, R.string.tale_022, R.string.alina_pash,           SettingsHelper.getBoolean("showBigImages") ? R.drawable.t022 : R.drawable.t022_min),
            new Tale(23, TaleAge.FOR_BABIES, 	"09:56", 7500, 1, R.string.tale_023, R.string.julia_jurina,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t023 : R.drawable.t023_min),
            new Tale(24, TaleAge.FOR_BABIES, 	"01:03", 6300, 1, R.string.tale_024, R.string.julia_jurina,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t024 : R.drawable.t024_min),
            new Tale(25, TaleAge.FOR_BABIES, 	"04:27", 6500, 1, R.string.tale_025, R.string.marta_liubchyk,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t025 : R.drawable.t025_min),
            new Tale(26, TaleAge.FOR_BABIES, 	"04:51", 6300, 1, R.string.tale_026, R.string.alyona_alyona,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t026 : R.drawable.t026_min),
            new Tale(27, TaleAge.FOR_BABIES, 	"02:51", 6200, 1, R.string.tale_027, R.string.alina_pash,           SettingsHelper.getBoolean("showBigImages") ? R.drawable.t027 : R.drawable.t027_min),
            new Tale(28, TaleAge.FOR_KIDS, 		"03:03", 6500, 1, R.string.tale_028, R.string.mariana_golovko,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t028 : R.drawable.t028_min),
            new Tale(29, TaleAge.FOR_KIDS, 		"04:14", 6200, 1, R.string.tale_029, R.string.dmytro_schebetiuk,    SettingsHelper.getBoolean("showBigImages") ? R.drawable.t029 : R.drawable.t029_min),
            new Tale(30, TaleAge.FOR_BABIES, 	"02:51", 6000, 1, R.string.tale_030, R.string.dmytro_schebetiuk,    SettingsHelper.getBoolean("showBigImages") ? R.drawable.t030 : R.drawable.t030_min),

            new Tale(31, TaleAge.FOR_KIDS, 		"06:30", 6000, 1, R.string.tale_031, R.string.jaroslav_lodygin,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t031 : R.drawable.t031_min),
            new Tale(32, TaleAge.FOR_BOTH, 		"06:31", 6500, 1, R.string.tale_032, R.string.julia_jurina,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t032 : R.drawable.t032_min),
            new Tale(33, TaleAge.FOR_BABIES, 	"02:01", 6500, 1, R.string.tale_033, R.string.mariana_golovko,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t033 : R.drawable.t033_min),
            new Tale(34, TaleAge.FOR_KIDS, 		"01:58", 6500, 1, R.string.tale_034, R.string.michel_schur,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t034 : R.drawable.t034_min),
            new Tale(35, TaleAge.FOR_KIDS, 		"05:27", 6550, 1, R.string.tale_035, R.string.jaroslav_lodygin,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t035 : R.drawable.t035_min),
            new Tale(36, TaleAge.FOR_KIDS, 		"04:36", 6500, 1, R.string.tale_036, R.string.jaroslav_lodygin,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t036 : R.drawable.t036_min),
            new Tale(37, TaleAge.FOR_BABIES, 	"03:19", 6300, 1, R.string.tale_037, R.string.alyona_alyona,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t037 : R.drawable.t037_min),
            new Tale(38, TaleAge.FOR_KIDS, 		"04:58", 7000, 1, R.string.tale_038, R.string.mariana_golovko,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t038 : R.drawable.t038_min),
            new Tale(39, TaleAge.FOR_BABIES, 	"02:39", 7000, 1, R.string.tale_039, R.string.alyona_alyona,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t039 : R.drawable.t039_min),
            new Tale(40, TaleAge.FOR_KIDS, 		"02:39", 6600, 1, R.string.tale_040, R.string.dmytro_schebetiuk,    SettingsHelper.getBoolean("showBigImages") ? R.drawable.t040 : R.drawable.t040_min),

            new Tale(41, TaleAge.FOR_KIDS, 		"05:09", 6500, 1, R.string.tale_041, R.string.marta_liubchyk,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t041 : R.drawable.t041_min),
            new Tale(42, TaleAge.FOR_KIDS, 		"05:11", 6500, 1, R.string.tale_042, R.string.marta_liubchyk,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t042 : R.drawable.t042_min),
            new Tale(43, TaleAge.FOR_KIDS, 		"04:13", 6200, 1, R.string.tale_043, R.string.michel_schur,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t043 : R.drawable.t043_min),
            new Tale(44, TaleAge.FOR_BABIES, 	"02:51", 6150, 1, R.string.tale_044, R.string.ruslana_khazipova,    SettingsHelper.getBoolean("showBigImages") ? R.drawable.t044 : R.drawable.t044_min),
            new Tale(45, TaleAge.FOR_KIDS, 		"07:04", 5700, 1, R.string.tale_045, R.string.ruslana_khazipova,    SettingsHelper.getBoolean("showBigImages") ? R.drawable.t045 : R.drawable.t045_min),
            new Tale(46, TaleAge.FOR_BABIES, 	"02:31", 6700, 1, R.string.tale_046, R.string.ruslana_khazipova,    SettingsHelper.getBoolean("showBigImages") ? R.drawable.t046 : R.drawable.t046_min),
            new Tale(47, TaleAge.FOR_BABIES, 	"01:32", 7000, 1, R.string.tale_047, R.string.vova_zi_lvova,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t047 : R.drawable.t047_min),
            new Tale(48, TaleAge.FOR_BOTH, 	    "01:43", 6850, 1, R.string.tale_048, R.string.vova_zi_lvova,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t048 : R.drawable.t048_min),
            new Tale(49, TaleAge.FOR_KIDS, 		"04:55", 7200, 1, R.string.tale_049, R.string.evgen_klopotenko,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t049 : R.drawable.t049_min),
            new Tale(50, TaleAge.FOR_KIDS, 		"04:25", 6300, 1, R.string.tale_050, R.string.marusia_ionova,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t050 : R.drawable.t050_min),

            new Tale(51, TaleAge.FOR_KIDS, 		"03:42", 7300, 1, R.string.tale_051, R.string.evgen_klopotenko,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t051 : R.drawable.t051_min),
            new Tale(52, TaleAge.FOR_KIDS, 		"06:20", 6850, 1, R.string.tale_052, R.string.evgen_klopotenko,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t052 : R.drawable.t052_min),
            new Tale(53, TaleAge.FOR_KIDS, 		"08:52", 5700, 1, R.string.tale_053, R.string.marusia_ionova,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t053 : R.drawable.t053_min),
            new Tale(54, TaleAge.FOR_KIDS, 		"06:48", 6800, 1, R.string.tale_054, R.string.marusia_ionova,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t054 : R.drawable.t054_min),
            new Tale(55, TaleAge.FOR_KIDS, 		"02:50", 6000, 1, R.string.tale_055, R.string.solomia_melnyk,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t055 : R.drawable.t055_min),
            new Tale(56, TaleAge.FOR_KIDS, 		"04:47", 6600, 1, R.string.tale_056, R.string.solomia_melnyk,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t056 : R.drawable.t056_min),
            new Tale(57, TaleAge.FOR_BABIES, 	"02:36", 6700, 1, R.string.tale_057, R.string.solomia_melnyk,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t057 : R.drawable.t057_min),
            new Tale(58, TaleAge.FOR_BOTH, 		"01:53", 6600, 1, R.string.tale_058, R.string.solomia_melnyk,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t058 : R.drawable.t058_min),
            new Tale(59, TaleAge.FOR_BOTH, 		"02:28", 6500, 1, R.string.tale_059, R.string.anna_nikitina,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t059 : R.drawable.t059_min),
            new Tale(60, TaleAge.FOR_BABIES, 	"02:16", 6250, 1, R.string.tale_060, R.string.anna_nikitina,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t060 : R.drawable.t060_min),

            new Tale(61, TaleAge.FOR_KIDS, 	    "03:27", 6500, 1, R.string.tale_061, R.string.anna_nikitina,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t061 : R.drawable.t061_min),
            new Tale(62, TaleAge.FOR_BOTH, 	    "04:05", 6600, 1, R.string.tale_062, R.string.vova_zi_lvova,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t062 : R.drawable.t062_min),
            new Tale(63, TaleAge.FOR_BABIES, 	"01:52", 6350, 1, R.string.tale_063, R.string.roman_yasynovsky,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t063 : R.drawable.t063_min),
            new Tale(64, TaleAge.FOR_KIDS, 		"07:20", 7200, 1, R.string.tale_064, R.string.roman_yasynovsky,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t064 : R.drawable.t064_min),
            new Tale(65, TaleAge.FOR_BABIES, 	"02:10", 6500, 1, R.string.tale_065, R.string.roman_yasynovsky,     SettingsHelper.getBoolean("showBigImages") ? R.drawable.t065 : R.drawable.t065_min),
            new Tale(66, TaleAge.FOR_KIDS, 		"03:01", 6500, 1, R.string.tale_066, R.string.vlad_fisun,           SettingsHelper.getBoolean("showBigImages") ? R.drawable.t066 : R.drawable.t066_min),
            new Tale(67, TaleAge.FOR_BABIES, 	"01:24", 6300, 1, R.string.tale_067, R.string.vlad_fisun,           SettingsHelper.getBoolean("showBigImages") ? R.drawable.t067 : R.drawable.t067_min),
            new Tale(68, TaleAge.FOR_KIDS, 		"02:34", 6500, 1, R.string.tale_068, R.string.vlad_fisun,           SettingsHelper.getBoolean("showBigImages") ? R.drawable.t068 : R.drawable.t068_min),
            new Tale(69, TaleAge.FOR_KIDS, 	    "03:27", 6700, 1, R.string.tale_069, R.string.timur_miroshnychenko, SettingsHelper.getBoolean("showBigImages") ? R.drawable.t069 : R.drawable.t069_min),
            new Tale(70, TaleAge.FOR_KIDS, 		"06:10", 6300, 1, R.string.tale_070, R.string.timur_miroshnychenko, SettingsHelper.getBoolean("showBigImages") ? R.drawable.t070 : R.drawable.t070_min),

            new Tale(71, TaleAge.FOR_BOTH, 		"04:14", 6300, 1, R.string.tale_071, R.string.timur_miroshnychenko, SettingsHelper.getBoolean("showBigImages") ? R.drawable.t071 : R.drawable.t071_min),
            new Tale(72, TaleAge.FOR_BABIES, 	"01:43", 6250, 1, R.string.tale_072, R.string.pavlo_varenitsa,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t072 : R.drawable.t072_min),
            new Tale(73, TaleAge.FOR_BOTH, 		"01:54", 6700, 1, R.string.tale_073, R.string.pavlo_varenitsa,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t073 : R.drawable.t073_min),
            new Tale(74, TaleAge.FOR_BABIES, 	"01:30", 6550, 1, R.string.tale_074, R.string.pavlo_varenitsa,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t074 : R.drawable.t074_min),
            new Tale(75, TaleAge.FOR_BABIES, 	"02:10", 6500, 1, R.string.tale_075, R.string.sergii_kolos,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t075 : R.drawable.t075_min),
            new Tale(76, TaleAge.FOR_BABIES, 	"02:00", 6350, 1, R.string.tale_076, R.string.sergii_kolos,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t076 : R.drawable.t076_min),
            new Tale(77, TaleAge.FOR_KIDS, 		"08:37", 5500, 1, R.string.tale_077, R.string.sergii_kolos,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t077 : R.drawable.t077_min),
            new Tale(78, TaleAge.FOR_KIDS, 		"03:54", 6000, 1, R.string.tale_078, R.string.stas_koroliov,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t078 : R.drawable.t078_min),
            new Tale(79, TaleAge.FOR_BOTH, 		"04:55", 7000, 1, R.string.tale_079, R.string.stas_koroliov,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t079 : R.drawable.t079_min),
            new Tale(80, TaleAge.FOR_BABIES, 	"03:31", 6000, 1, R.string.tale_080, R.string.stas_koroliov,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t080 : R.drawable.t080_min),

            new Tale(81, TaleAge.FOR_BOTH, 	    "06:58", 5700, 1, R.string.tale_081, R.string.katia_rogova,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t081 : R.drawable.t081_min),
            new Tale(82, TaleAge.FOR_KIDS, 		"05:39", 6000, 1, R.string.tale_082, R.string.katia_rogova,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t082 : R.drawable.t082_min),
            new Tale(83, TaleAge.FOR_KIDS, 		"09:23", 6000, 1, R.string.tale_083, R.string.katia_rogova,         SettingsHelper.getBoolean("showBigImages") ? R.drawable.t083 : R.drawable.t083_min),
            new Tale(84, TaleAge.FOR_KIDS, 		"09:02", 5500, 1, R.string.tale_084, R.string.ivan_marunych,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t084 : R.drawable.t084_min),
            new Tale(85, TaleAge.FOR_KIDS, 		"04:45", 6800, 1, R.string.tale_085, R.string.ivan_marunych,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t085 : R.drawable.t085_min),
            new Tale(86, TaleAge.FOR_KIDS, 		"04:48", 6500, 1, R.string.tale_086, R.string.ivan_marunych,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t086 : R.drawable.t086_min),
            new Tale(87, TaleAge.FOR_BABIES, 	"04:53", 6500, 1, R.string.tale_087, R.string.nata_smirnova,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t087 : R.drawable.t087_min),
            new Tale(88, TaleAge.FOR_KIDS, 		"09:14", 5500, 1, R.string.tale_088, R.string.nata_smirnova,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t088 : R.drawable.t088_min),
            new Tale(89, TaleAge.FOR_KIDS, 		"11:32", 5600, 1, R.string.tale_089, R.string.nata_smirnova,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t089 : R.drawable.t089_min),
            new Tale(90, TaleAge.FOR_KIDS, 		"12:18", 5750, 1, R.string.tale_090, R.string.oleg_moskalenko,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t090 : R.drawable.t090_min),

            new Tale(91, TaleAge.FOR_KIDS, 		"05:54", 5400, 1, R.string.tale_091, R.string.oleg_moskalenko,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t091 : R.drawable.t091_min),
            new Tale(92, TaleAge.FOR_KIDS, 		"06:44", 5800, 1, R.string.tale_092, R.string.oleg_moskalenko,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t092 : R.drawable.t092_min),
            new Tale(93, TaleAge.FOR_KIDS, 		"07:39", 7200, 1, R.string.tale_093, R.string.rosava,               SettingsHelper.getBoolean("showBigImages") ? R.drawable.t093 : R.drawable.t093_min),
            new Tale(94, TaleAge.FOR_BABIES, 	"02:45", 6800, 1, R.string.tale_094, R.string.rosava,               SettingsHelper.getBoolean("showBigImages") ? R.drawable.t094 : R.drawable.t094_min),
            new Tale(95, TaleAge.FOR_KIDS, 		"08:00", 7000, 1, R.string.tale_095, R.string.rosava,               SettingsHelper.getBoolean("showBigImages") ? R.drawable.t095 : R.drawable.t095_min),
            new Tale(96, TaleAge.FOR_BABIES, 	"02:37", 6600, 1, R.string.tale_096, R.string.jamala,               SettingsHelper.getBoolean("showBigImages") ? R.drawable.t096 : R.drawable.t096_min),
            new Tale(97, TaleAge.FOR_KIDS, 		"01:59", 6500, 1, R.string.tale_097, R.string.jamala,               SettingsHelper.getBoolean("showBigImages") ? R.drawable.t097 : R.drawable.t097_min),
            new Tale(98, TaleAge.FOR_KIDS, 		"06:30", 6300, 1, R.string.tale_098, R.string.jamala,               SettingsHelper.getBoolean("showBigImages") ? R.drawable.t098 : R.drawable.t098_min),
            new Tale(99, TaleAge.FOR_KIDS, 		"01:56", 6700, 1, R.string.tale_099, R.string.sergii_tanchynets,    SettingsHelper.getBoolean("showBigImages") ? R.drawable.t099 : R.drawable.t099_min),
            new Tale(100,TaleAge.FOR_KIDS, 		"02:09", 6500, 1, R.string.tale_100, R.string.sergii_tanchynets,    SettingsHelper.getBoolean("showBigImages") ? R.drawable.t100 : R.drawable.t100_min),

            new Tale(101, TaleAge.FOR_KIDS, 	"04:42", 6700, 1, R.string.tale_101, R.string.sergii_tanchynets,   SettingsHelper.getBoolean("showBigImages") ? R.drawable.t101 : R.drawable.t101_min),
            new Tale(102, TaleAge.FOR_KIDS, 	"07:54", 7300, 1, R.string.tale_102, R.string.inna_grebeniuk,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t102 : R.drawable.t102_min),
            new Tale(103, TaleAge.FOR_KIDS, 	"04:27", 6500, 1, R.string.tale_103, R.string.inna_grebeniuk,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t103 : R.drawable.t103_min),
            new Tale(104, TaleAge.FOR_KIDS, 	"05:39", 6000, 1, R.string.tale_104, R.string.inna_grebeniuk,      SettingsHelper.getBoolean("showBigImages") ? R.drawable.t104 : R.drawable.t104_min),
            new Tale(105, TaleAge.FOR_KIDS, 	"04:57", 6800, 1, R.string.tale_105, R.string.olga_shurova,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t105 : R.drawable.t105_min),
            new Tale(106, TaleAge.FOR_BOTH, 	"01:46", 6200, 1, R.string.tale_106, R.string.olga_shurova,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t106 : R.drawable.t106_min),
            new Tale(107, TaleAge.FOR_KIDS, 	"03:50", 6300, 1, R.string.tale_107, R.string.olga_shurova,        SettingsHelper.getBoolean("showBigImages") ? R.drawable.t107 : R.drawable.t107_min),
            new Tale(108, TaleAge.FOR_KIDS, 	"01:43", 6500, 1, R.string.tale_108, R.string.anastasiia_gudyma,   SettingsHelper.getBoolean("showBigImages") ? R.drawable.t108 : R.drawable.t108_min),
            new Tale(109, TaleAge.FOR_KIDS, 	"05:10", 6500, 1, R.string.tale_109, R.string.anastasiia_gudyma,   SettingsHelper.getBoolean("showBigImages") ? R.drawable.t109 : R.drawable.t109_min),
            new Tale(110, TaleAge.FOR_BOTH, 	"02:37", 6850, 1, R.string.tale_110, R.string.anastasiia_gudyma,   SettingsHelper.getBoolean("showBigImages") ? R.drawable.t110 : R.drawable.t110_min),

            new Tale(111, TaleAge.FOR_KIDS, 	"10:00", 7500, 1, R.string.tale_111, R.string.dmytro_horkin,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t111 : R.drawable.t111_min),
            new Tale(112, TaleAge.FOR_KIDS, 	"10:26", 5500, 1, R.string.tale_112, R.string.dmytro_horkin,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t112 : R.drawable.t112_min),
            new Tale(113, TaleAge.FOR_KIDS, 	"11:33", 5500, 1, R.string.tale_113, R.string.dmytro_horkin,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t113 : R.drawable.t113_min),
            new Tale(114, TaleAge.FOR_KIDS, 	"06:01", 6500, 1, R.string.tale_114, R.string.dmytro_horkin,       SettingsHelper.getBoolean("showBigImages") ? R.drawable.t114 : R.drawable.t114_min),
            new Tale(115, TaleAge.FOR_BOTH, 	"04:55", 7800, 1, R.string.tale_115, R.string.kateryna_ofliyan,    SettingsHelper.getBoolean("showBigImages") ? R.drawable.t115 : R.drawable.t115_min),
            new Tale(116, TaleAge.FOR_KIDS, 	"05:52", 6000, 1, R.string.tale_116, R.string.kateryna_ofliyan,    SettingsHelper.getBoolean("showBigImages") ? R.drawable.t116 : R.drawable.t116_min),
            new Tale(117, TaleAge.FOR_BOTH, 	"03:16", 7400, 1, R.string.tale_117, R.string.kateryna_ofliyan,    SettingsHelper.getBoolean("showBigImages") ? R.drawable.t117 : R.drawable.t117_min),
            new Tale(118, TaleAge.FOR_KIDS, 	"17:56", 0, 1, R.string.tale_118, R.string.ira_bova, SettingsHelper.getBoolean("showBigImages") ? R.drawable.t118 : R.drawable.t118_min),
            new Tale(119, TaleAge.LULLABIES, 	"03:13", 0, 0, R.string.tale_119, R.string.nina_matvienko, SettingsHelper.getBoolean("showBigImages") ? R.drawable.t119 : R.drawable.t119_min),
            new Tale(120, TaleAge.LULLABIES, 	"02:22", 0, 0, R.string.tale_120, R.string.olga_tokar, SettingsHelper.getBoolean("showBigImages") ? R.drawable.t120 : R.drawable.t120_min),

            new Tale(121, TaleAge.LULLABIES, "04:14", 0, 0, R.string.tale_121, R.string.olga_tokar, SettingsHelper.getBoolean("showBigImages") ? R.drawable.t121 : R.drawable.t121_min),
            new Tale(122, TaleAge.LULLABIES, "02:35", 0, 0, R.string.tale_122, R.string.vitaliy_bilonozhko, SettingsHelper.getBoolean("showBigImages") ? R.drawable.t122 : R.drawable.t122_min)
//            new Tale(3, TaleAge.FOR_BABIES, "00:00", 5000, 0, R.string.tale_3, R.string., SettingsHelper.getBoolean("showBigImages") ? R.drawable.t1 : R.drawable.t3_min),
//            new Tale(4, TaleAge.FOR_BABIES, "00:00", 5000, 0, R.string.tale_4, R.string., SettingsHelper.getBoolean("showBigImages") ? R.drawable.t1 : R.drawable.t4_min),
//            new Tale(5, TaleAge.FOR_BABIES, "00:00", 5000, 0, R.string.tale_5, R.string., SettingsHelper.getBoolean("showBigImages") ? R.drawable.t1 : R.drawable.t5_min),
//            new Tale(6, TaleAge.FOR_BABIES, "00:00", 5000, 0, R.string.tale_6, R.string., SettingsHelper.getBoolean("showBigImages") ? R.drawable.t1 : R.drawable.t6_min),
//            new Tale(7, TaleAge.FOR_BABIES, "00:00", 5000, 0, R.string.tale_7, R.string., SettingsHelper.getBoolean("showBigImages") ? R.drawable.t1 : R.drawable.t7_min),
//            new Tale(8, TaleAge.FOR_BABIES, "00:00", 5000, 0, R.string.tale_8, R.string., SettingsHelper.getBoolean("showBigImages") ? R.drawable.t1 : R.drawable.t8_min),
//            new Tale(9, TaleAge.FOR_BABIES, "00:00", 5000, 0, R.string.tale_9, R.string., SettingsHelper.getBoolean("showBigImages") ? R.drawable.t1 : R.drawable.t9_min),
//            new Tale(0, TaleAge.FOR_BABIES, "00:00", 5000, 0, R.string.tale_0, R.string., SettingsHelper.getBoolean("showBigImages") ? R.drawable.t1 : R.drawable.t0_min),
    ));
}
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
            new Tale(1, R.string.tale_001, R.string.andrii_hlyvniuk, R.drawable.t001_min),
            new Tale(2, R.string.tale_002, R.string.andrii_hlyvniuk, R.drawable.t002_min),
            new Tale(3, R.string.tale_003, R.string.andrii_hlyvniuk, R.drawable.t003_min),
            new Tale(4, R.string.tale_004, R.string.marko_galanevych, R.drawable.t004_min),
            new Tale(5, R.string.tale_005, R.string.marko_galanevych, R.drawable.t005_min),
            new Tale(6, R.string.tale_006, R.string.alina_pash, R.drawable.t006_min),
            new Tale(7, R.string.tale_007, R.string.sasha_koltsova, R.drawable.t007_min),
            new Tale(8, R.string.tale_008, R.string.sasha_koltsova, R.drawable.t008_min),
            new Tale(9, R.string.tale_009, R.string.sasha_koltsova, R.drawable.t009_min),
            new Tale(10, R.string.tale_010, R.string.evgen_maluha, R.drawable.t010_min),

            new Tale(10, R.string.tale_010, R.string.evgen_maluha, R.drawable.t010_min),
            new Tale(11, R.string.tale_011, R.string.evgen_maluha, R.drawable.t011_min),
            new Tale(12, R.string.tale_012, R.string.evgen_maluha, R.drawable.t012_min),
            new Tale(13, R.string.tale_013, R.string.sergii_zhadan, R.drawable.t013_min),
            new Tale(14, R.string.tale_014, R.string.sergii_zhadan, R.drawable.t014_min),
            new Tale(15, R.string.tale_015, R.string.sergii_zhadan, R.drawable.t015_min),
            new Tale(16, R.string.tale_016, R.string.hrystyna_soloviy, R.drawable.t016_min),
            new Tale(17, R.string.tale_017, R.string.hrystyna_soloviy, R.drawable.t017_min),
            new Tale(18, R.string.tale_018, R.string.hrystyna_soloviy, R.drawable.t018_min),
            new Tale(19, R.string.tale_019, R.string.oleksiy_dorychevsky, R.drawable.t019_min),
            new Tale(20, R.string.tale_020, R.string.oleksiy_dorychevsky, R.drawable.t020_min),

            new Tale(21, R.string.tale_021, R.string.oleksiy_dorychevsky, R.drawable.t021_min),
            new Tale(22, R.string.tale_022, R.string.alina_pash, R.drawable.t022_min),
            new Tale(23, R.string.tale_023, R.string.julia_jurina, R.drawable.t023_min),
            new Tale(24, R.string.tale_024, R.string.julia_jurina, R.drawable.t024_min),
            new Tale(25, R.string.tale_025, R.string.marta_liubchyk, R.drawable.t025_min),
            new Tale(26, R.string.tale_026, R.string.alyona_alyona, R.drawable.t026_min),
            new Tale(27, R.string.tale_027, R.string.alina_pash, R.drawable.t027_min),
            new Tale(28, R.string.tale_028, R.string.mariana_golovko, R.drawable.t028_min),
            new Tale(29, R.string.tale_029, R.string.dmytro_schebetiuk, R.drawable.t029_min),
            new Tale(30, R.string.tale_030, R.string.dmytro_schebetiuk, R.drawable.t030_min),

            new Tale(31, R.string.tale_031, R.string.jaroslav_ljudgin, R.drawable.t031_min),
            new Tale(32, R.string.tale_032, R.string.julia_jurina, R.drawable.t032_min),
            new Tale(33, R.string.tale_033, R.string.mariana_golovko, R.drawable.t033_min),
            new Tale(34, R.string.tale_034, R.string.michel_schur, R.drawable.t034_min),
            new Tale(35, R.string.tale_035, R.string.jaroslav_ljudgin, R.drawable.t035_min),
            new Tale(36, R.string.tale_036, R.string.jaroslav_ljudgin, R.drawable.t036_min),
            new Tale(37, R.string.tale_037, R.string.alyona_alyona, R.drawable.t037_min),
            new Tale(38, R.string.tale_038, R.string.mariana_golovko, R.drawable.t038_min),
            new Tale(39, R.string.tale_039, R.string.alyona_alyona, R.drawable.t039_min),
            new Tale(40, R.string.tale_040, R.string.dmytro_schebetiuk, R.drawable.t040_min),

            new Tale(41, R.string.tale_041, R.string.marta_liubchyk, R.drawable.t041_min),
            new Tale(42, R.string.tale_042, R.string.marta_liubchyk, R.drawable.t042_min),
            new Tale(43, R.string.tale_043, R.string.michel_schur, R.drawable.t043_min),
            new Tale(44, R.string.tale_044, R.string.ruslana_khazipova, R.drawable.t044_min),
            new Tale(45, R.string.tale_045, R.string.ruslana_khazipova, R.drawable.t045_min),
            new Tale(46, R.string.tale_046, R.string.ruslana_khazipova, R.drawable.t046_min),
            new Tale(47, R.string.tale_047, R.string.vova_zi_lvova, R.drawable.t047_min),
            new Tale(48, R.string.tale_048, R.string.vova_zi_lvova, R.drawable.t048_min),
            new Tale(49, R.string.tale_049, R.string.evgen_klopotenko, R.drawable.t049_min),
            new Tale(50, R.string.tale_050, R.string.marusia_ionova, R.drawable.t050_min),

            new Tale(51, R.string.tale_051, R.string.evgen_klopotenko, R.drawable.t051_min),
            new Tale(52, R.string.tale_052, R.string.evgen_klopotenko, R.drawable.t052_min),
            new Tale(53, R.string.tale_053, R.string.marusia_ionova, R.drawable.t053_min),
            new Tale(54, R.string.tale_054, R.string.marusia_ionova, R.drawable.t054_min),
            new Tale(55, R.string.tale_055, R.string.solomia_melnyk, R.drawable.t055_min),
            new Tale(56, R.string.tale_056, R.string.solomia_melnyk, R.drawable.t056_min),
            new Tale(57, R.string.tale_057, R.string.solomia_melnyk, R.drawable.t057_min),
            new Tale(58, R.string.tale_058, R.string.solomia_melnyk, R.drawable.t058_min),
            new Tale(59, R.string.tale_059, R.string.anna_nikitina, R.drawable.t059_min),
            new Tale(60, R.string.tale_060, R.string.anna_nikitina, R.drawable.t060_min),

            new Tale(61, R.string.tale_061, R.string.anna_nikitina, R.drawable.t061_min),
            new Tale(62, R.string.tale_062, R.string.vova_zi_lvova, R.drawable.t062_min),
            new Tale(63, R.string.tale_063, R.string.roman_yasynovsky, R.drawable.t063_min),
            new Tale(64, R.string.tale_064, R.string.roman_yasynovsky, R.drawable.t064_min),
            new Tale(65, R.string.tale_065, R.string.roman_yasynovsky, R.drawable.t065_min),
            new Tale(66, R.string.tale_066, R.string.vlad_fisun, R.drawable.t066_min),
            new Tale(67, R.string.tale_067, R.string.vlad_fisun, R.drawable.t067_min),
            new Tale(68, R.string.tale_068, R.string.vlad_fisun, R.drawable.t068_min),
            new Tale(69, R.string.tale_069, R.string.timur_miroshnychenko, R.drawable.t069_min),
            new Tale(70, R.string.tale_070, R.string.timur_miroshnychenko, R.drawable.t070_min),

            new Tale(71, R.string.tale_071, R.string.timur_miroshnychenko, R.drawable.t071_min),
            new Tale(72, R.string.tale_072, R.string.pavlo_varenitsa, R.drawable.t072_min),
            new Tale(73, R.string.tale_073, R.string.pavlo_varenitsa, R.drawable.t073_min),
            new Tale(74, R.string.tale_074, R.string.pavlo_varenitsa, R.drawable.t074_min),
            new Tale(75, R.string.tale_075, R.string.sergii_kolos, R.drawable.t075_min),
            new Tale(76, R.string.tale_076, R.string.sergii_kolos, R.drawable.t076_min),
            new Tale(77, R.string.tale_077, R.string.sergii_kolos, R.drawable.t077_min),
            new Tale(78, R.string.tale_078, R.string.stas_koroliov, R.drawable.t078_min),
            new Tale(79, R.string.tale_079, R.string.stas_koroliov, R.drawable.t079_min),
            new Tale(80, R.string.tale_080, R.string.stas_koroliov, R.drawable.t080_min),

            new Tale(81, R.string.tale_081, R.string.katia_rogova, R.drawable.t081_min),
            new Tale(82, R.string.tale_082, R.string.katia_rogova, R.drawable.t082_min),
            new Tale(83, R.string.tale_083, R.string.katia_rogova, R.drawable.t083_min)
//            new Tale(84, R.string.tale_084, R.string., R.drawable.t084_min),
//            new Tale(85, R.string.tale_085, R.string., R.drawable.t085_min),
//            new Tale(86, R.string.tale_086, R.string., R.drawable.t086_min),
//            new Tale(87, R.string.tale_087, R.string., R.drawable.t087_min),
//            new Tale(88, R.string.tale_088, R.string., R.drawable.t088_min),
//            new Tale(89, R.string.tale_089, R.string., R.drawable.t089_min),
//            new Tale(90, R.string.tale_090, R.string., R.drawable.t090_min),

//            new Tale(1, R.string.tale_01, R.string., R.drawable.t01_min),
//            new Tale(2, R.string.tale_02, R.string., R.drawable.t02_min),
//            new Tale(3, R.string.tale_03, R.string., R.drawable.t03_min),
//            new Tale(4, R.string.tale_04, R.string., R.drawable.t04_min),
//            new Tale(5, R.string.tale_05, R.string., R.drawable.t05_min),
//            new Tale(6, R.string.tale_06, R.string., R.drawable.t06_min),
//            new Tale(7, R.string.tale_07, R.string., R.drawable.t07_min),
//            new Tale(8, R.string.tale_08, R.string., R.drawable.t08_min),
//            new Tale(9, R.string.tale_09, R.string., R.drawable.t09_min),
//            new Tale(0, R.string.tale_00, R.string., R.drawable.t00_min),
            ));
}
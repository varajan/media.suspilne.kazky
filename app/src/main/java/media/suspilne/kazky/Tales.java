package media.suspilne.kazky;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Tales {
    boolean showOnlyFavorite = SettingsHelper.getBoolean("showOnlyFavorite");

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

    Tale getPrevious(){
        int nowPlaying = getNowPlaying();
        List<Tale> tales = getTales(showOnlyFavorite, getFilter());
        boolean skip = (nowPlaying > 0 && getById(nowPlaying).shouldBeShown(showOnlyFavorite, getFilter()));

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
        List<Tale> tales = getTales(showOnlyFavorite, getFilter());
        boolean skip = (nowPlaying > 0 && getById(nowPlaying).shouldBeShown(showOnlyFavorite, getFilter()));

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
            new Tale(1, R.string.tale_001, R.string.andrii_hlyvniuk, R.drawable.t001),
            new Tale(2, R.string.tale_002, R.string.andrii_hlyvniuk, R.drawable.t002),
            new Tale(3, R.string.tale_003, R.string.andrii_hlyvniuk, R.drawable.t003),
            new Tale(4, R.string.tale_004, R.string.marko_galanevych, R.drawable.t004),
            new Tale(5, R.string.tale_005, R.string.marko_galanevych, R.drawable.t005),
            new Tale(6, R.string.tale_006, R.string.alina_pash, R.drawable.t006),
            new Tale(7, R.string.tale_007, R.string.sasha_koltsova, R.drawable.t007),
            new Tale(8, R.string.tale_008, R.string.sasha_koltsova, R.drawable.t008),
            new Tale(9, R.string.tale_009, R.string.sasha_koltsova, R.drawable.t009),
            new Tale(10, R.string.tale_010, R.string.evgen_maluha, R.drawable.t010),

            new Tale(11, R.string.tale_011, R.string.evgen_maluha, R.drawable.t011),
            new Tale(12, R.string.tale_012, R.string.evgen_maluha, R.drawable.t012),
            new Tale(13, R.string.tale_013, R.string.sergii_zhadan, R.drawable.t013),
            new Tale(14, R.string.tale_014, R.string.sergii_zhadan, R.drawable.t014),
            new Tale(15, R.string.tale_015, R.string.sergii_zhadan, R.drawable.t015),
            new Tale(16, R.string.tale_016, R.string.hrystyna_soloviy, R.drawable.t016),
            new Tale(17, R.string.tale_017, R.string.hrystyna_soloviy, R.drawable.t017),
            new Tale(18, R.string.tale_018, R.string.hrystyna_soloviy, R.drawable.t018),
            new Tale(19, R.string.tale_019, R.string.oleksiy_dorychevsky, R.drawable.t019),
            new Tale(20, R.string.tale_020, R.string.oleksiy_dorychevsky, R.drawable.t020),

            new Tale(21, R.string.tale_021, R.string.oleksiy_dorychevsky, R.drawable.t021),
            new Tale(22, R.string.tale_022, R.string.alina_pash, R.drawable.t022),
            new Tale(23, R.string.tale_023, R.string.julia_jurina, R.drawable.t023),
            new Tale(24, R.string.tale_024, R.string.julia_jurina, R.drawable.t024),
            new Tale(25, R.string.tale_025, R.string.marta_liubchyk, R.drawable.t025),
            new Tale(26, R.string.tale_026, R.string.alyona_alyona, R.drawable.t026),
            new Tale(27, R.string.tale_027, R.string.alina_pash, R.drawable.t027),
            new Tale(28, R.string.tale_028, R.string.mariana_golovko, R.drawable.t028),
            new Tale(29, R.string.tale_029, R.string.dmytro_schebetiuk, R.drawable.t029),
            new Tale(30, R.string.tale_030, R.string.dmytro_schebetiuk, R.drawable.t030),

            new Tale(31, R.string.tale_031, R.string.jaroslav_ljudgin, R.drawable.t031),
            new Tale(32, R.string.tale_032, R.string.julia_jurina, R.drawable.t032),
            new Tale(33, R.string.tale_033, R.string.mariana_golovko, R.drawable.t033),
            new Tale(34, R.string.tale_034, R.string.michel_schur, R.drawable.t034),
            new Tale(35, R.string.tale_035, R.string.jaroslav_ljudgin, R.drawable.t035),
            new Tale(36, R.string.tale_036, R.string.jaroslav_ljudgin, R.drawable.t036),
            new Tale(37, R.string.tale_037, R.string.alyona_alyona, R.drawable.t037),
            new Tale(38, R.string.tale_038, R.string.mariana_golovko, R.drawable.t038),
            new Tale(39, R.string.tale_039, R.string.alyona_alyona, R.drawable.t039),
            new Tale(40, R.string.tale_040, R.string.dmytro_schebetiuk, R.drawable.t040),

            new Tale(41, R.string.tale_041, R.string.marta_liubchyk, R.drawable.t041),
            new Tale(42, R.string.tale_042, R.string.marta_liubchyk, R.drawable.t042),
            new Tale(43, R.string.tale_043, R.string.michel_schur, R.drawable.t043),
            new Tale(44, R.string.tale_044, R.string.ruslana_khazipova, R.drawable.t044),
            new Tale(45, R.string.tale_045, R.string.ruslana_khazipova, R.drawable.t045),
            new Tale(46, R.string.tale_046, R.string.ruslana_khazipova, R.drawable.t046),
            new Tale(47, R.string.tale_047, R.string.vova_zi_lvova, R.drawable.t047),
            new Tale(48, R.string.tale_048, R.string.vova_zi_lvova, R.drawable.t048),
            new Tale(49, R.string.tale_049, R.string.evgen_klopotenko, R.drawable.t049),
            new Tale(50, R.string.tale_050, R.string.marusia_ionova, R.drawable.t050),

            new Tale(51, R.string.tale_051, R.string.evgen_klopotenko, R.drawable.t051),
            new Tale(52, R.string.tale_052, R.string.evgen_klopotenko, R.drawable.t052),
            new Tale(53, R.string.tale_053, R.string.marusia_ionova, R.drawable.t053),
            new Tale(54, R.string.tale_054, R.string.marusia_ionova, R.drawable.t054),
            new Tale(55, R.string.tale_055, R.string.solomia_melnyk, R.drawable.t055),
            new Tale(56, R.string.tale_056, R.string.solomia_melnyk, R.drawable.t056),
            new Tale(57, R.string.tale_057, R.string.solomia_melnyk, R.drawable.t057),
            new Tale(58, R.string.tale_058, R.string.solomia_melnyk, R.drawable.t058),
            new Tale(59, R.string.tale_059, R.string.anna_nikitina, R.drawable.t059),
            new Tale(60, R.string.tale_060, R.string.anna_nikitina, R.drawable.t060),

            new Tale(61, R.string.tale_061, R.string.anna_nikitina, R.drawable.t061),
            new Tale(62, R.string.tale_062, R.string.vova_zi_lvova, R.drawable.t062),
            new Tale(63, R.string.tale_063, R.string.roman_yasynovsky, R.drawable.t063),
            new Tale(64, R.string.tale_064, R.string.roman_yasynovsky, R.drawable.t064),
            new Tale(65, R.string.tale_065, R.string.roman_yasynovsky, R.drawable.t065),
            new Tale(66, R.string.tale_066, R.string.vlad_fisun, R.drawable.t066),
            new Tale(67, R.string.tale_067, R.string.vlad_fisun, R.drawable.t067),
            new Tale(68, R.string.tale_068, R.string.vlad_fisun, R.drawable.t068),
            new Tale(69, R.string.tale_069, R.string.timur_miroshnychenko, R.drawable.t069),
            new Tale(70, R.string.tale_070, R.string.timur_miroshnychenko, R.drawable.t070),

            new Tale(71, R.string.tale_071, R.string.timur_miroshnychenko, R.drawable.t071),
            new Tale(72, R.string.tale_072, R.string.pavlo_varenitsa, R.drawable.t072),
            new Tale(73, R.string.tale_073, R.string.pavlo_varenitsa, R.drawable.t073),
            new Tale(74, R.string.tale_074, R.string.pavlo_varenitsa, R.drawable.t074),
            new Tale(75, R.string.tale_075, R.string.sergii_kolos, R.drawable.t075),
            new Tale(76, R.string.tale_076, R.string.sergii_kolos, R.drawable.t076),
            new Tale(77, R.string.tale_077, R.string.sergii_kolos, R.drawable.t077),
            new Tale(78, R.string.tale_078, R.string.stas_koroliov, R.drawable.t078),
            new Tale(79, R.string.tale_079, R.string.stas_koroliov, R.drawable.t079),
            new Tale(80, R.string.tale_080, R.string.stas_koroliov, R.drawable.t080),

            new Tale(81, R.string.tale_081, R.string.katia_rogova, R.drawable.t081),
            new Tale(82, R.string.tale_082, R.string.katia_rogova, R.drawable.t082),
            new Tale(83, R.string.tale_083, R.string.katia_rogova, R.drawable.t083)
//            new Tale(84, R.string.tale_084, R.string., R.drawable.t084),
//            new Tale(85, R.string.tale_085, R.string., R.drawable.t085),
//            new Tale(86, R.string.tale_086, R.string., R.drawable.t086),
//            new Tale(87, R.string.tale_087, R.string., R.drawable.t087),
//            new Tale(88, R.string.tale_088, R.string., R.drawable.t088),
//            new Tale(89, R.string.tale_089, R.string., R.drawable.t089),
//            new Tale(90, R.string.tale_090, R.string., R.drawable.t090),

//            new Tale(1, R.string.tale_01, R.string., R.drawable.t01),
//            new Tale(2, R.string.tale_02, R.string., R.drawable.t02),
//            new Tale(3, R.string.tale_03, R.string., R.drawable.t03),
//            new Tale(4, R.string.tale_04, R.string., R.drawable.t04),
//            new Tale(5, R.string.tale_05, R.string., R.drawable.t05),
//            new Tale(6, R.string.tale_06, R.string., R.drawable.t06),
//            new Tale(7, R.string.tale_07, R.string., R.drawable.t07),
//            new Tale(8, R.string.tale_08, R.string., R.drawable.t08),
//            new Tale(9, R.string.tale_09, R.string., R.drawable.t09),
//            new Tale(0, R.string.tale_00, R.string., R.drawable.t00),
            ));
}
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
            new Tale(5, R.string.tale_005, R.string.marko_galanevych),
            new Tale(6, R.string.tale_006, R.string.alina_pash),
            new Tale(7, R.string.tale_007, R.string.sasha_koltsova),
            new Tale(8, R.string.tale_008, R.string.sasha_koltsova),
            new Tale(9, R.string.tale_009, R.string.sasha_koltsova),
            new Tale(10, R.string.tale_010, R.string.evgen_maluha),

            new Tale(11, R.string.tale_011, R.string.evgen_maluha),
            new Tale(12, R.string.tale_012, R.string.evgen_maluha),
            new Tale(13, R.string.tale_013, R.string.sergii_zhadan),
            new Tale(14, R.string.tale_014, R.string.sergii_zhadan),
            new Tale(15, R.string.tale_015, R.string.sergii_zhadan),
            new Tale(16, R.string.tale_016, R.string.hrystyna_soloviy),
            new Tale(17, R.string.tale_017, R.string.hrystyna_soloviy),
            new Tale(18, R.string.tale_018, R.string.hrystyna_soloviy),
            new Tale(19, R.string.tale_019, R.string.oleksiy_dorychevsky),
            new Tale(20, R.string.tale_020, R.string.oleksiy_dorychevsky),

            new Tale(21, R.string.tale_021, R.string.oleksiy_dorychevsky),
            new Tale(22, R.string.tale_022, R.string.alina_pash),
            new Tale(23, R.string.tale_023, R.string.julia_jurina),
            new Tale(24, R.string.tale_024, R.string.julia_jurina),
            new Tale(25, R.string.tale_025, R.string.marta_liubchyk),
            new Tale(26, R.string.tale_026, R.string.alyona_alyona),
            new Tale(27, R.string.tale_027, R.string.alina_pash),
            new Tale(28, R.string.tale_028, R.string.mariana_golovko),
            new Tale(29, R.string.tale_029, R.string.dmytro_schebetiuk),
            new Tale(30, R.string.tale_030, R.string.dmytro_schebetiuk),

            new Tale(31, R.string.tale_031, R.string.jaroslav_ljudgin),
            new Tale(32, R.string.tale_032, R.string.julia_jurina),
            new Tale(33, R.string.tale_033, R.string.mariana_golovko),
            new Tale(34, R.string.tale_034, R.string.michel_schur),
            new Tale(35, R.string.tale_035, R.string.jaroslav_ljudgin),
            new Tale(36, R.string.tale_036, R.string.jaroslav_ljudgin),
            new Tale(37, R.string.tale_037, R.string.alyona_alyona),
            new Tale(38, R.string.tale_038, R.string.mariana_golovko),
            new Tale(39, R.string.tale_039, R.string.alyona_alyona),
            new Tale(40, R.string.tale_040, R.string.dmytro_schebetiuk),

            new Tale(41, R.string.tale_041, R.string.marta_liubchyk),
            new Tale(42, R.string.tale_042, R.string.marta_liubchyk),
            new Tale(43, R.string.tale_043, R.string.michel_schur),
            new Tale(44, R.string.tale_044, R.string.ruslana_khazipova),
            new Tale(45, R.string.tale_045, R.string.ruslana_khazipova),
            new Tale(46, R.string.tale_046, R.string.ruslana_khazipova),
            new Tale(47, R.string.tale_047, R.string.vova_zi_lvova),
            new Tale(48, R.string.tale_048, R.string.vova_zi_lvova),
            new Tale(49, R.string.tale_049, R.string.evgen_klopotenko),
            new Tale(50, R.string.tale_050, R.string.marusia_ionova),

            new Tale(51, R.string.tale_051, R.string.evgen_klopotenko),
            new Tale(52, R.string.tale_052, R.string.evgen_klopotenko),
            new Tale(53, R.string.tale_053, R.string.marusia_ionova),
            new Tale(54, R.string.tale_054, R.string.marusia_ionova),
            new Tale(55, R.string.tale_055, R.string.solomia_melnyk),
            new Tale(56, R.string.tale_056, R.string.solomia_melnyk),
            new Tale(57, R.string.tale_057, R.string.solomia_melnyk),
            new Tale(58, R.string.tale_058, R.string.solomia_melnyk),
            new Tale(59, R.string.tale_059, R.string.anna_nikitina),
            new Tale(60, R.string.tale_060, R.string.anna_nikitina),

            new Tale(61, R.string.tale_061, R.string.anna_nikitina),
            new Tale(62, R.string.tale_062, R.string.vova_zi_lvova),
            new Tale(63, R.string.tale_063, R.string.roman_yasynovsky),
            new Tale(64, R.string.tale_064, R.string.roman_yasynovsky),
            new Tale(65, R.string.tale_065, R.string.roman_yasynovsky),
            new Tale(66, R.string.tale_066, R.string.vlad_fisun),
            new Tale(67, R.string.tale_067, R.string.vlad_fisun),
            new Tale(68, R.string.tale_068, R.string.vlad_fisun),
            new Tale(69, R.string.tale_069, R.string.timur_miroshnychenko),
            new Tale(70, R.string.tale_070, R.string.timur_miroshnychenko),

            new Tale(71, R.string.tale_071, R.string.timur_miroshnychenko),
            new Tale(72, R.string.tale_072, R.string.pavlo_varenitsa),
            new Tale(73, R.string.tale_073, R.string.pavlo_varenitsa),
            new Tale(74, R.string.tale_074, R.string.pavlo_varenitsa),
            new Tale(75, R.string.tale_075, R.string.sergii_kolos),
            new Tale(76, R.string.tale_076, R.string.sergii_kolos),
            new Tale(77, R.string.tale_077, R.string.sergii_kolos),
            new Tale(78, R.string.tale_078, R.string.stas_koroliov),
            new Tale(79, R.string.tale_079, R.string.stas_koroliov),
            new Tale(80, R.string.tale_080, R.string.stas_koroliov),

            new Tale(81, R.string.tale_081, R.string.katia_rogova),
            new Tale(82, R.string.tale_082, R.string.katia_rogova),
            new Tale(83, R.string.tale_083, R.string.katia_rogova)
//            new Tale(84, R.string.tale_084, R.string.),
//            new Tale(85, R.string.tale_085, R.string.),
//            new Tale(86, R.string.tale_086, R.string.),
//            new Tale(87, R.string.tale_087, R.string.),
//            new Tale(88, R.string.tale_088, R.string.),
//            new Tale(89, R.string.tale_089, R.string.),
//            new Tale(90, R.string.tale_090, R.string.),

//            new Tale(1, R.string.tale_01, R.string.),
//            new Tale(2, R.string.tale_02, R.string.),
//            new Tale(3, R.string.tale_03, R.string.),
//            new Tale(4, R.string.tale_04, R.string.),
//            new Tale(5, R.string.tale_05, R.string.),
//            new Tale(6, R.string.tale_06, R.string.),
//            new Tale(7, R.string.tale_07, R.string.),
//            new Tale(8, R.string.tale_08, R.string.),
//            new Tale(9, R.string.tale_09, R.string.),
//            new Tale(0, R.string.tale_00, R.string.),
            ));
}
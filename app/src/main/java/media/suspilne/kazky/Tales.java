package media.suspilne.kazky;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Tales {
    public static boolean getShowOnlyFavorite() { return SettingsHelper.getBoolean("showOnlyFavorite"); }
    public static void setShowOnlyFavorite(boolean value) { SettingsHelper.setBoolean("showOnlyFavorite", value); }

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
        List<Tale> tales = getTales(getShowOnlyFavorite(), getFilter());
        boolean skip = (nowPlaying > 0 && getById(nowPlaying).shouldBeShown(getShowOnlyFavorite(), getFilter()));

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
        List<Tale> tales = getTales(getShowOnlyFavorite(), getFilter());
        boolean skip = (nowPlaying > 0 && getById(nowPlaying).shouldBeShown(getShowOnlyFavorite(), getFilter()));

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
            new Tale(1, "02:04", R.string.tale_001, R.string.andrii_hlyvniuk, R.drawable.t001),
            new Tale(2, "02:28", R.string.tale_002, R.string.andrii_hlyvniuk, R.drawable.t002),
            new Tale(3, "02:10", R.string.tale_003, R.string.andrii_hlyvniuk, R.drawable.t003),
            new Tale(4, "01:11", R.string.tale_004, R.string.marko_galanevych, R.drawable.t004),
            new Tale(5, "01:50", R.string.tale_005, R.string.marko_galanevych, R.drawable.t005),
            new Tale(6, "02:31", R.string.tale_006, R.string.alina_pash, R.drawable.t006),
            new Tale(7, "02:34", R.string.tale_007, R.string.sasha_koltsova, R.drawable.t007),
            new Tale(8, "00:48", R.string.tale_008, R.string.sasha_koltsova, R.drawable.t008),
            new Tale(9, "01:44", R.string.tale_009, R.string.sasha_koltsova, R.drawable.t009),
            new Tale(10, "02:15", R.string.tale_010, R.string.evgen_maluha, R.drawable.t010),

            new Tale(11, "02:15", R.string.tale_011, R.string.evgen_maluha, R.drawable.t011),
            new Tale(12, "03:01", R.string.tale_012, R.string.evgen_maluha, R.drawable.t012),
            new Tale(13, "03:20", R.string.tale_013, R.string.sergii_zhadan, R.drawable.t013),
            new Tale(14, "03:53", R.string.tale_014, R.string.sergii_zhadan, R.drawable.t014),
            new Tale(15, "05:01", R.string.tale_015, R.string.sergii_zhadan, R.drawable.t015),
            new Tale(16, "09:11", R.string.tale_016, R.string.hrystyna_soloviy, R.drawable.t016),
            new Tale(17, "12:33", R.string.tale_017, R.string.hrystyna_soloviy, R.drawable.t017),
            new Tale(18, "07:36", R.string.tale_018, R.string.hrystyna_soloviy, R.drawable.t018),
            new Tale(19, "04:03", R.string.tale_019, R.string.oleksiy_dorychevsky, R.drawable.t019),
            new Tale(20, "02:59", R.string.tale_020, R.string.oleksiy_dorychevsky, R.drawable.t020),

            new Tale(21, "05:05", R.string.tale_021, R.string.oleksiy_dorychevsky, R.drawable.t021),
            new Tale(22, "04:42", R.string.tale_022, R.string.alina_pash, R.drawable.t022),
            new Tale(23, "09:56", R.string.tale_023, R.string.julia_jurina, R.drawable.t023),
            new Tale(24, "01:03", R.string.tale_024, R.string.julia_jurina, R.drawable.t024),
            new Tale(25, "04:27", R.string.tale_025, R.string.marta_liubchyk, R.drawable.t025),
            new Tale(26, "04:51", R.string.tale_026, R.string.alyona_alyona, R.drawable.t026),
            new Tale(27, "02:51", R.string.tale_027, R.string.alina_pash, R.drawable.t027),
            new Tale(28, "03:03", R.string.tale_028, R.string.mariana_golovko, R.drawable.t028),
            new Tale(29, "04:14", R.string.tale_029, R.string.dmytro_schebetiuk, R.drawable.t029),
            new Tale(30, "02:51", R.string.tale_030, R.string.dmytro_schebetiuk, R.drawable.t030),

            new Tale(31, "06:30", R.string.tale_031, R.string.jaroslav_ljudgin, R.drawable.t031),
            new Tale(32, "06:31", R.string.tale_032, R.string.julia_jurina, R.drawable.t032),
            new Tale(33, "02:01", R.string.tale_033, R.string.mariana_golovko, R.drawable.t033),
            new Tale(34, "01:58", R.string.tale_034, R.string.michel_schur, R.drawable.t034),
            new Tale(35, "05:27", R.string.tale_035, R.string.jaroslav_ljudgin, R.drawable.t035),
            new Tale(36, "04:36", R.string.tale_036, R.string.jaroslav_ljudgin, R.drawable.t036),
            new Tale(37, "03:19", R.string.tale_037, R.string.alyona_alyona, R.drawable.t037),
            new Tale(38, "04:58", R.string.tale_038, R.string.mariana_golovko, R.drawable.t038),
            new Tale(39, "02:39", R.string.tale_039, R.string.alyona_alyona, R.drawable.t039),
            new Tale(40, "02:39", R.string.tale_040, R.string.dmytro_schebetiuk, R.drawable.t040),

            new Tale(41, "05:09", R.string.tale_041, R.string.marta_liubchyk, R.drawable.t041),
            new Tale(42, "05:11", R.string.tale_042, R.string.marta_liubchyk, R.drawable.t042),
            new Tale(43, "04:13", R.string.tale_043, R.string.michel_schur, R.drawable.t043),
            new Tale(44, "02:51", R.string.tale_044, R.string.ruslana_khazipova, R.drawable.t044),
            new Tale(45, "07:04", R.string.tale_045, R.string.ruslana_khazipova, R.drawable.t045),
            new Tale(46, "02:31", R.string.tale_046, R.string.ruslana_khazipova, R.drawable.t046),
            new Tale(47, "01:32", R.string.tale_047, R.string.vova_zi_lvova, R.drawable.t047),
            new Tale(48, "01:43", R.string.tale_048, R.string.vova_zi_lvova, R.drawable.t048),
            new Tale(49, "04:55", R.string.tale_049, R.string.evgen_klopotenko, R.drawable.t049),
            new Tale(50, "04:25", R.string.tale_050, R.string.marusia_ionova, R.drawable.t050),

            new Tale(51, "03:42", R.string.tale_051, R.string.evgen_klopotenko, R.drawable.t051),
            new Tale(52, "06:20", R.string.tale_052, R.string.evgen_klopotenko, R.drawable.t052),
            new Tale(53, "08:52", R.string.tale_053, R.string.marusia_ionova, R.drawable.t053),
            new Tale(54, "06:48", R.string.tale_054, R.string.marusia_ionova, R.drawable.t054),
            new Tale(55, "02:50", R.string.tale_055, R.string.solomia_melnyk, R.drawable.t055),
            new Tale(56, "04:47", R.string.tale_056, R.string.solomia_melnyk, R.drawable.t056),
            new Tale(57, "02:36", R.string.tale_057, R.string.solomia_melnyk, R.drawable.t057),
            new Tale(58, "01:53", R.string.tale_058, R.string.solomia_melnyk, R.drawable.t058),
            new Tale(59, "02:28", R.string.tale_059, R.string.anna_nikitina, R.drawable.t059),
            new Tale(60, "02:16", R.string.tale_060, R.string.anna_nikitina, R.drawable.t060),

            new Tale(61, "03:27", R.string.tale_061, R.string.anna_nikitina, R.drawable.t061),
            new Tale(62, "04:05", R.string.tale_062, R.string.vova_zi_lvova, R.drawable.t062),
            new Tale(63, "01:52", R.string.tale_063, R.string.roman_yasynovsky, R.drawable.t063),
            new Tale(64, "07:20", R.string.tale_064, R.string.roman_yasynovsky, R.drawable.t064),
            new Tale(65, "02:10", R.string.tale_065, R.string.roman_yasynovsky, R.drawable.t065),
            new Tale(66, "03:01", R.string.tale_066, R.string.vlad_fisun, R.drawable.t066),
            new Tale(67, "01:24", R.string.tale_067, R.string.vlad_fisun, R.drawable.t067),
            new Tale(68, "02:34", R.string.tale_068, R.string.vlad_fisun, R.drawable.t068),
            new Tale(69, "03:27", R.string.tale_069, R.string.timur_miroshnychenko, R.drawable.t069),
            new Tale(70, "06:10", R.string.tale_070, R.string.timur_miroshnychenko, R.drawable.t070),

            new Tale(71, "04:14", R.string.tale_071, R.string.timur_miroshnychenko, R.drawable.t071),
            new Tale(72, "01:43", R.string.tale_072, R.string.pavlo_varenitsa, R.drawable.t072),
            new Tale(73, "01:54", R.string.tale_073, R.string.pavlo_varenitsa, R.drawable.t073),
            new Tale(74, "01:30", R.string.tale_074, R.string.pavlo_varenitsa, R.drawable.t074),
            new Tale(75, "02:10", R.string.tale_075, R.string.sergii_kolos, R.drawable.t075),
            new Tale(76, "02:00", R.string.tale_076, R.string.sergii_kolos, R.drawable.t076),
            new Tale(77, "08:37", R.string.tale_077, R.string.sergii_kolos, R.drawable.t077),
            new Tale(78, "03:54", R.string.tale_078, R.string.stas_koroliov, R.drawable.t078),
            new Tale(79, "04:55", R.string.tale_079, R.string.stas_koroliov, R.drawable.t079),
            new Tale(80, "03:31", R.string.tale_080, R.string.stas_koroliov, R.drawable.t080),

            new Tale(81, "06:58", R.string.tale_081, R.string.katia_rogova, R.drawable.t081),
            new Tale(82, "05:39", R.string.tale_082, R.string.katia_rogova, R.drawable.t082),
            new Tale(83, "09:23", R.string.tale_083, R.string.katia_rogova, R.drawable.t083),
            new Tale(84, "09:02", R.string.tale_084, R.string.ivan_marunych, R.drawable.t084),
            new Tale(85, "04:45", R.string.tale_085, R.string.ivan_marunych, R.drawable.t085),
            new Tale(86, "04:48", R.string.tale_086, R.string.ivan_marunych, R.drawable.t086),
            new Tale(87, "04:53", R.string.tale_087, R.string.nata_smirnova, R.drawable.t087),
            new Tale(88, "09:14", R.string.tale_088, R.string.nata_smirnova, R.drawable.t088),
            new Tale(89, "11:32", R.string.tale_089, R.string.nata_smirnova, R.drawable.t089),
            new Tale(90, "12:18", R.string.tale_090, R.string.oleg_moskalenko, R.drawable.t090),

            new Tale(91, "05:54", R.string.tale_091, R.string.oleg_moskalenko, R.drawable.t091),
            new Tale(92, "06:44", R.string.tale_092, R.string.oleg_moskalenko, R.drawable.t092),
            new Tale(93, "07:39", R.string.tale_093, R.string.rosava, R.drawable.t093),
            new Tale(94, "02:45", R.string.tale_094, R.string.rosava, R.drawable.t094),
            new Tale(95, "08:00", R.string.tale_095, R.string.rosava, R.drawable.t095),
            new Tale(96, "02:37", R.string.tale_096, R.string.jamala, R.drawable.t096),
            new Tale(97, "01:59", R.string.tale_097, R.string.jamala, R.drawable.t097),
            new Tale(98, "06:30", R.string.tale_098, R.string.jamala, R.drawable.t098),
            new Tale(99, "01:56", R.string.tale_099, R.string.sergii_tanchynets, R.drawable.t099),
            new Tale(100, "02:09", R.string.tale_100, R.string.sergii_tanchynets, R.drawable.t100),

            new Tale(101, "04:42", R.string.tale_101, R.string.sergii_tanchynets, R.drawable.t101),
            new Tale(102, "07:54", R.string.tale_102, R.string.inna_grebeniuk, R.drawable.t102),
            new Tale(103, "04:27", R.string.tale_103, R.string.inna_grebeniuk, R.drawable.t103),
            new Tale(104, "05:39", R.string.tale_104, R.string.inna_grebeniuk, R.drawable.t104),
            new Tale(105, "04:57", R.string.tale_105, R.string.olga_shurova, R.drawable.t105),
            new Tale(106, "01:46", R.string.tale_106, R.string.olga_shurova, R.drawable.t106),
            new Tale(107, "03:50", R.string.tale_107, R.string.olga_shurova, R.drawable.t107),
            new Tale(108, "01:43", R.string.tale_108, R.string.anastasiia_gudyma, R.drawable.t108),
            new Tale(109, "05:10", R.string.tale_109, R.string.anastasiia_gudyma, R.drawable.t109),
            new Tale(110, "02:37", R.string.tale_110, R.string.anastasiia_gudyma, R.drawable.t110),

            new Tale(111, "10:00", R.string.tale_111, R.string.dmytro_horkin, R.drawable.t111),
            new Tale(112, "10:26", R.string.tale_112, R.string.dmytro_horkin, R.drawable.t112),
            new Tale(113, "11:33", R.string.tale_113, R.string.dmytro_horkin, R.drawable.t113),
            new Tale(114, "06:01", R.string.tale_114, R.string.dmytro_horkin, R.drawable.t114),
            new Tale(115, "04:55", R.string.tale_115, R.string.kateryna_ofliyan, R.drawable.t115),
            new Tale(116, "05:52", R.string.tale_116, R.string.kateryna_ofliyan, R.drawable.t116),
            new Tale(117, "03:16", R.string.tale_117, R.string.kateryna_ofliyan, R.drawable.t117)
//            new Tale(8, "00:00", R.string.tale_118, R.string., R.drawable.t118),
//            new Tale(9, "00:00", R.string.tale_119, R.string., R.drawable.t119),
//            new Tale(0, "00:00", R.string.tale_120, R.string., R.drawable.t120),

//            new Tale(1, "00:00", R.string.tale_1, R.string., R.drawable.t1),
//            new Tale(2, "00:00", R.string.tale_2, R.string., R.drawable.t2),
//            new Tale(3, "00:00", R.string.tale_3, R.string., R.drawable.t3),
//            new Tale(4, "00:00", R.string.tale_4, R.string., R.drawable.t4),
//            new Tale(5, "00:00", R.string.tale_5, R.string., R.drawable.t5),
//            new Tale(6, "00:00", R.string.tale_6, R.string., R.drawable.t6),
//            new Tale(7, "00:00", R.string.tale_7, R.string., R.drawable.t7),
//            new Tale(8, "00:00", R.string.tale_8, R.string., R.drawable.t8),
//            new Tale(9, "00:00", R.string.tale_9, R.string., R.drawable.t9),
//            new Tale(0, "00:00", R.string.tale_0, R.string., R.drawable.t0),
            ));
}
package media.suspilne.kazky.data;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.R;
import media.suspilne.kazky.activities.MainActivity;
import media.suspilne.kazky.helpers.SettingsHelper;
import media.suspilne.kazky.helpers.ListHelper;

public class Tales {
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
                            -> tale1.getReader().equals(tale2.getReader())
                            ? compare(tale1.getTitle(), tale2.getTitle())
                            : compare(tale1.getReader(), tale2.getReader()));
                } else {
                    result.sort((tale1, tale2) -> compare(tale1.getTitle(), tale2.getTitle()));
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

    public List<Tale> items = Arrays.asList(
            new Tale(1, "02:04", 7200, 1, R.string.tale_001, R.string.andrii_hlyvniuk),
            new Tale(2, "02:28", 7200, 1, R.string.tale_002, R.string.andrii_hlyvniuk),
            new Tale(3, "02:10", 8200, 1, R.string.tale_003, R.string.andrii_hlyvniuk),
            new Tale(4, "01:11", 7500, 1, R.string.tale_004, R.string.marko_galanevych),
            new Tale(5, "01:50", 7000, 1, R.string.tale_005, R.string.marko_galanevych),
            new Tale(6, "02:31", 6500, 1, R.string.tale_006, R.string.alina_pash),
            new Tale(7, "02:34", 7000, 1, R.string.tale_007, R.string.sasha_koltsova),
            new Tale(8, "00:48", 7000, 1, R.string.tale_008, R.string.sasha_koltsova),
            new Tale(9, "01:44", 7000, 1, R.string.tale_009, R.string.sasha_koltsova),
            new Tale(10, "02:15", 7000, 1, R.string.tale_010, R.string.evgen_maluha),

            new Tale(11, "02:15", 8200, 1, R.string.tale_011, R.string.evgen_maluha),
            new Tale(12, "03:01", 6500, 1, R.string.tale_012, R.string.evgen_maluha),
            new Tale(13, "03:20", 6800, 1, R.string.tale_013, R.string.sergii_zhadan),
            new Tale(14, "03:53", 6700, 1, R.string.tale_014, R.string.sergii_zhadan),
            new Tale(15, "05:01", 6900, 1, R.string.tale_015, R.string.sergii_zhadan),
            new Tale(16, "09:11", 7700, 1, R.string.tale_016, R.string.hrystyna_soloviy),
            new Tale(17, "12:33", 6500, 1, R.string.tale_017, R.string.hrystyna_soloviy),
            new Tale(18, "07:36", 7200, 1, R.string.tale_018, R.string.hrystyna_soloviy),
            new Tale(19, "04:03", 6700, 1, R.string.tale_019, R.string.oleksiy_dorychevsky),
            new Tale(20, "02:59", 6500, 1, R.string.tale_020, R.string.oleksiy_dorychevsky),

            new Tale(21, "05:05", 6700, 1, R.string.tale_021, R.string.oleksiy_dorychevsky),
            new Tale(22, "04:42", 6200, 1, R.string.tale_022, R.string.alina_pash),
            new Tale(23, "09:56", 7500, 1, R.string.tale_023, R.string.julia_jurina),
            new Tale(24, "01:03", 6300, 1, R.string.tale_024, R.string.julia_jurina),
            new Tale(25, "04:27", 6500, 1, R.string.tale_025, R.string.marta_liubchyk),
            new Tale(26, "04:51", 6300, 1, R.string.tale_026, R.string.alyona_alyona),
            new Tale(27, "02:51", 6200, 1, R.string.tale_027, R.string.alina_pash),
            new Tale(28, "03:03", 6500, 1, R.string.tale_028, R.string.mariana_golovko),
            new Tale(29, "04:14", 6200, 1, R.string.tale_029, R.string.dmytro_schebetiuk),
            new Tale(30, "02:51", 6000, 1, R.string.tale_030, R.string.dmytro_schebetiuk),

            new Tale(31, "06:30", 6000, 1, R.string.tale_031, R.string.jaroslav_lodygin),
            new Tale(32, "06:31", 6500, 1, R.string.tale_032, R.string.julia_jurina),
            new Tale(33, "02:01", 6500, 1, R.string.tale_033, R.string.mariana_golovko),
            new Tale(34, "01:58", 6500, 1, R.string.tale_034, R.string.michel_schur),
            new Tale(35, "05:27", 6550, 1, R.string.tale_035, R.string.jaroslav_lodygin),
            new Tale(36, "04:36", 6500, 1, R.string.tale_036, R.string.jaroslav_lodygin),
            new Tale(37, "03:19", 6300, 1, R.string.tale_037, R.string.alyona_alyona),
            new Tale(38, "04:58", 7000, 1, R.string.tale_038, R.string.mariana_golovko),
            new Tale(39, "02:39", 7000, 1, R.string.tale_039, R.string.alyona_alyona),
            new Tale(40, "02:39", 6600, 1, R.string.tale_040, R.string.dmytro_schebetiuk),

            new Tale(41, "05:09", 6500, 1, R.string.tale_041, R.string.marta_liubchyk),
            new Tale(42, "05:11", 6500, 1, R.string.tale_042, R.string.marta_liubchyk),
            new Tale(43, "04:13", 6200, 1, R.string.tale_043, R.string.michel_schur),
            new Tale(44, "02:51", 6150, 1, R.string.tale_044, R.string.ruslana_khazipova),
            new Tale(45, "07:04", 5700, 1, R.string.tale_045, R.string.ruslana_khazipova),
            new Tale(46, "02:31", 6700, 1, R.string.tale_046, R.string.ruslana_khazipova),
            new Tale(47, "01:32", 7000, 1, R.string.tale_047, R.string.vova_zi_lvova),
            new Tale(48, "01:43", 6850, 1, R.string.tale_048, R.string.vova_zi_lvova),
            new Tale(49, "04:55", 7200, 1, R.string.tale_049, R.string.evgen_klopotenko),
            new Tale(50, "04:25", 6300, 1, R.string.tale_050, R.string.marusia_ionova),

            new Tale(51, "03:42", 7300, 1, R.string.tale_051, R.string.evgen_klopotenko),
            new Tale(52, "06:20", 6850, 1, R.string.tale_052, R.string.evgen_klopotenko),
            new Tale(53, "08:52", 5700, 1, R.string.tale_053, R.string.marusia_ionova),
            new Tale(54, "06:48", 6800, 1, R.string.tale_054, R.string.marusia_ionova),
            new Tale(55, "02:50", 6000, 1, R.string.tale_055, R.string.solomia_melnyk),
            new Tale(56, "04:47", 6600, 1, R.string.tale_056, R.string.solomia_melnyk),
            new Tale(57, "02:36", 6700, 1, R.string.tale_057, R.string.solomia_melnyk),
            new Tale(58, "01:53", 6600, 1, R.string.tale_058, R.string.solomia_melnyk),
            new Tale(59, "02:28", 6500, 1, R.string.tale_059, R.string.anna_nikitina),
            new Tale(60, "02:16", 6250, 1, R.string.tale_060, R.string.anna_nikitina),

            new Tale(61, "03:27", 6500, 1, R.string.tale_061, R.string.anna_nikitina),
            new Tale(62, "04:05", 6600, 1, R.string.tale_062, R.string.vova_zi_lvova),
            new Tale(63, "01:52", 6350, 1, R.string.tale_063, R.string.roman_yasynovsky),
            new Tale(64, "07:20", 7200, 1, R.string.tale_064, R.string.roman_yasynovsky),
            new Tale(65, "02:10", 6500, 1, R.string.tale_065, R.string.roman_yasynovsky),
            new Tale(66, "03:01", 6500, 1, R.string.tale_066, R.string.vlad_fisun),
            new Tale(67, "01:24", 6300, 1, R.string.tale_067, R.string.vlad_fisun),
            new Tale(68, "02:34", 6500, 1, R.string.tale_068, R.string.vlad_fisun),
            new Tale(69, "03:27", 6700, 1, R.string.tale_069, R.string.timur_miroshnychenko),
            new Tale(70, "06:10", 6300, 1, R.string.tale_070, R.string.timur_miroshnychenko),

            new Tale(71, "04:14", 6300, 1, R.string.tale_071, R.string.timur_miroshnychenko),
            new Tale(72, "01:43", 6250, 1, R.string.tale_072, R.string.pavlo_varenitsa),
            new Tale(73, "01:54", 6700, 1, R.string.tale_073, R.string.pavlo_varenitsa),
            new Tale(74, "01:30", 6550, 1, R.string.tale_074, R.string.pavlo_varenitsa),
            new Tale(75, "02:10", 6500, 1, R.string.tale_075, R.string.sergii_kolos),
            new Tale(76, "02:00", 6350, 1, R.string.tale_076, R.string.sergii_kolos),
            new Tale(77, "08:37", 5500, 1, R.string.tale_077, R.string.sergii_kolos),
            new Tale(78, "03:54", 6000, 1, R.string.tale_078, R.string.stas_koroliov),
            new Tale(79, "04:55", 7000, 1, R.string.tale_079, R.string.stas_koroliov),
            new Tale(80, "03:31", 6000, 1, R.string.tale_080, R.string.stas_koroliov),

            new Tale(81, "06:58", 5700, 1, R.string.tale_081, R.string.katia_rogova),
            new Tale(82, "05:39", 6000, 1, R.string.tale_082, R.string.katia_rogova),
            new Tale(83, "09:23", 6000, 1, R.string.tale_083, R.string.katia_rogova),
            new Tale(84, "09:02", 5500, 1, R.string.tale_084, R.string.ivan_marunych),
            new Tale(85, "04:45", 6800, 1, R.string.tale_085, R.string.ivan_marunych),
            new Tale(86, "04:48", 6500, 1, R.string.tale_086, R.string.ivan_marunych),
            new Tale(87, "04:53", 6500, 1, R.string.tale_087, R.string.nata_smirnova),
            new Tale(88, "09:14", 5500, 1, R.string.tale_088, R.string.nata_smirnova),
            new Tale(89, "11:32", 5600, 1, R.string.tale_089, R.string.nata_smirnova),
            new Tale(90, "12:18", 5750, 1, R.string.tale_090, R.string.oleg_moskalenko),

            new Tale(91, "05:54", 5400, 1, R.string.tale_091, R.string.oleg_moskalenko),
            new Tale(92, "06:44", 5800, 1, R.string.tale_092, R.string.oleg_moskalenko),
            new Tale(93, "07:39", 7200, 1, R.string.tale_093, R.string.rosava),
            new Tale(94, "02:45", 6800, 1, R.string.tale_094, R.string.rosava),
            new Tale(95, "08:00", 7000, 1, R.string.tale_095, R.string.rosava),
            new Tale(96, "02:37", 6600, 1, R.string.tale_096, R.string.jamala),
            new Tale(97, "01:59", 6500, 1, R.string.tale_097, R.string.jamala),
            new Tale(98, "06:30", 6300, 1, R.string.tale_098, R.string.jamala),
            new Tale(99, "01:56", 6700, 1, R.string.tale_099, R.string.sergii_tanchynets),
            new Tale(100, "02:09", 6500, 1, R.string.tale_100, R.string.sergii_tanchynets),

            new Tale(101, "04:42", 6700, 1, R.string.tale_101, R.string.sergii_tanchynets),
            new Tale(102, "07:54", 7300, 1, R.string.tale_102, R.string.inna_grebeniuk),
            new Tale(103, "04:27", 6500, 1, R.string.tale_103, R.string.inna_grebeniuk),
            new Tale(104, "05:39", 6000, 1, R.string.tale_104, R.string.inna_grebeniuk),
            new Tale(105, "04:57", 6800, 1, R.string.tale_105, R.string.olga_shurova),
            new Tale(106, "01:46", 6200, 1, R.string.tale_106, R.string.olga_shurova),
            new Tale(107, "03:50", 6300, 1, R.string.tale_107, R.string.olga_shurova),
            new Tale(108, "01:43", 6500, 1, R.string.tale_108, R.string.anastasiia_gudyma),
            new Tale(109, "05:10", 6500, 1, R.string.tale_109, R.string.anastasiia_gudyma),
            new Tale(110, "02:37", 6850, 1, R.string.tale_110, R.string.anastasiia_gudyma),

            new Tale(111, "10:00", 7500, 1, R.string.tale_111, R.string.dmytro_horkin),
            new Tale(112, "10:26", 5500, 1, R.string.tale_112, R.string.dmytro_horkin),
            new Tale(113, "11:33", 5500, 1, R.string.tale_113, R.string.dmytro_horkin),
            new Tale(114, "06:01", 6500, 1, R.string.tale_114, R.string.dmytro_horkin),
            new Tale(115, "04:55", 7800, 1, R.string.tale_115, R.string.kateryna_ofliyan),
            new Tale(116, "05:52", 6000, 1, R.string.tale_116, R.string.kateryna_ofliyan),
            new Tale(117, "03:16", 7400, 1, R.string.tale_117, R.string.kateryna_ofliyan),
            new Tale(118, "17:56", 0, 1, R.string.tale_118, R.string.ira_bova),
            new Tale(119, "03:05", 0, 0, R.string.tale_119, R.string.nina_matvienko),
            new Tale(120, "02:12", 0, 0, R.string.tale_120, R.string.olga_tokar),

            new Tale(121, "04:10", 0, 0, R.string.tale_121, R.string.olga_tokar),
            new Tale(122, "02:34", 0, 0, R.string.tale_122, R.string.vitaliy_bilonozhko),
            new Tale(123, "16:35", 7000, 0, R.string.tale_123, R.string.nataliya_vasko),
            new Tale(124, "11:24", 2000, 0, R.string.tale_124, R.string.ruhanko_man),
            new Tale(125, "07:10", 2000, 0, R.string.tale_125, R.string.roxolana),
            new Tale(126, "06:53", 4000, 0, R.string.tale_126, R.string.ivan_kaluzhny),
            new Tale(127, "08:28", 500, 0, R.string.tale_127, R.string.antonina_hyzhniak),
            new Tale(128, "06:42", 3000, 0, R.string.tale_128, R.string.kola),
            new Tale(129, "08:33", 500, 0, R.string.tale_129, R.string.kler),
            new Tale(130, "08:52", 6000, 0, R.string.tale_130, R.string.julia_sanina),

            new Tale(131, "06:10", 4000, 0, R.string.tale_131, R.string.busha),
            new Tale(132, "11:33", 12000, 0, R.string.tale_132, R.string.pavlo_vyshebaba),
            new Tale(133, "13:57", 12000, 0, R.string.tale_133, R.string.irma_vitovska),
            new Tale(134, "11:52", 11000, 0, R.string.tale_134, R.string.olena_topolia),
            new Tale(135, "11:38", 3000, 0, R.string.tale_135, R.string.antonina_hyzhniak),
            new Tale(136, "11:43", 3000, 0, R.string.tale_136, R.string.timur_miroshnychenko),
            new Tale(137, "09:02", 2500, 0, R.string.tale_137, R.string.serhii_prytula),
            new Tale(138, "06:33", 4000, 0, R.string.tale_138, R.string.roxolana),
            new Tale(139, "09:42", 11000, 0, R.string.tale_139, R.string.natalka_denysenko),
            new Tale(140, "07:46", 4000, 0, R.string.tale_140, R.string.zlata_ognevich),

            new Tale(141, "12:13", 3500, 0, R.string.tale_141, R.string.taras_kompanichenko),
            new Tale(142, "06:27", 3000, 0, R.string.tale_142, R.string.vlad_rudnitsky)

//            new Tale(3, "00:00", 5000, 0, R.string.tale_3, R.string.),
    );
}
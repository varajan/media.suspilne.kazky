package media.suspilne.kazky.data;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import media.suspilne.kazky.Kazky;
import media.suspilne.kazky.R;
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
            new Tale(1, 7200, R.string.andrii_hlyvniuk),
            new Tale(2, 7200, R.string.andrii_hlyvniuk),
            new Tale(3, 8200, R.string.andrii_hlyvniuk),
            new Tale(4, 7500, R.string.marko_galanevych),
            new Tale(5, 7000, R.string.marko_galanevych),
            new Tale(6, 6500, R.string.alina_pash),
            new Tale(7, 7000, R.string.sasha_koltsova),
            new Tale(8, 7000, R.string.sasha_koltsova),
            new Tale(9, 7000, R.string.sasha_koltsova),
            new Tale(10, 7000, R.string.evgen_maluha),

            new Tale(11, 8200, R.string.evgen_maluha),
            new Tale(12, 6500, R.string.evgen_maluha),
            new Tale(13, 6800, R.string.sergii_zhadan),
            new Tale(14, 6700, R.string.sergii_zhadan),
            new Tale(15, 6900, R.string.sergii_zhadan),
            new Tale(16, 7700, R.string.hrystyna_soloviy),
            new Tale(17, 6500, R.string.hrystyna_soloviy),
            new Tale(18, 7200, R.string.hrystyna_soloviy),
            new Tale(19, 6700, R.string.oleksiy_dorychevsky),
            new Tale(20, 6500, R.string.oleksiy_dorychevsky),

            new Tale(21, 6700, R.string.oleksiy_dorychevsky),
            new Tale(22, 6200, R.string.alina_pash),
            new Tale(23, 7500, R.string.julia_jurina),
            new Tale(24, 6300, R.string.julia_jurina),
            new Tale(25, 6500, R.string.marta_liubchyk),
            new Tale(26, 6300, R.string.alyona_alyona),
            new Tale(27, 6200, R.string.alina_pash),
            new Tale(28, 6500, R.string.mariana_golovko),
            new Tale(29, 6200, R.string.dmytro_schebetiuk),
            new Tale(30, 6000, R.string.dmytro_schebetiuk),

            new Tale(31, 6000, R.string.jaroslav_lodygin),
            new Tale(32, 6500, R.string.julia_jurina),
            new Tale(33, 6500, R.string.mariana_golovko),
            new Tale(34, 6500, R.string.michel_schur),
            new Tale(35, 6550, R.string.jaroslav_lodygin),
            new Tale(36, 6500, R.string.jaroslav_lodygin),
            new Tale(37, 6300, R.string.alyona_alyona),
            new Tale(38, 7000, R.string.mariana_golovko),
            new Tale(39, 7000, R.string.alyona_alyona),
            new Tale(40, 6600, R.string.dmytro_schebetiuk),

            new Tale(41, 6500, R.string.marta_liubchyk),
            new Tale(42, 6500, R.string.marta_liubchyk),
            new Tale(43, 6200, R.string.michel_schur),
            new Tale(44, 6150, R.string.ruslana_khazipova),
            new Tale(45, 5700, R.string.ruslana_khazipova),
            new Tale(46, 6700, R.string.ruslana_khazipova),
            new Tale(47, 7000, R.string.vova_zi_lvova),
            new Tale(48, 6850, R.string.vova_zi_lvova),
            new Tale(49, 7200, R.string.evgen_klopotenko),
            new Tale(50, 6300, R.string.marusia_ionova),

            new Tale(51, 7300, R.string.evgen_klopotenko),
            new Tale(52, 6850, R.string.evgen_klopotenko),
            new Tale(53, 5700, R.string.marusia_ionova),
            new Tale(54, 6800, R.string.marusia_ionova),
            new Tale(55, 6000, R.string.solomia_melnyk),
            new Tale(56, 6600, R.string.solomia_melnyk),
            new Tale(57, 6700, R.string.solomia_melnyk),
            new Tale(58, 6600, R.string.solomia_melnyk),
            new Tale(59, 6500, R.string.anna_nikitina),
            new Tale(60, 6250, R.string.anna_nikitina),

            new Tale(61, 6500, R.string.anna_nikitina),
            new Tale(62, 6600, R.string.vova_zi_lvova),
            new Tale(63, 6350, R.string.roman_yasynovsky),
            new Tale(64, 7200, R.string.roman_yasynovsky),
            new Tale(65, 6500, R.string.roman_yasynovsky),
            new Tale(66, 6500, R.string.vlad_fisun),
            new Tale(67, 6300, R.string.vlad_fisun),
            new Tale(68, 6500, R.string.vlad_fisun),
            new Tale(69, 6700, R.string.timur_miroshnychenko),
            new Tale(70, 6300, R.string.timur_miroshnychenko),

            new Tale(71, 6300, R.string.timur_miroshnychenko),
            new Tale(72, 6250, R.string.pavlo_varenitsa),
            new Tale(73, 6700, R.string.pavlo_varenitsa),
            new Tale(74, 6550, R.string.pavlo_varenitsa),
            new Tale(75, 6500, R.string.sergii_kolos),
            new Tale(76, 6350, R.string.sergii_kolos),
            new Tale(77, 5500, R.string.sergii_kolos),
            new Tale(78, 6000, R.string.stas_koroliov),
            new Tale(79, 7000, R.string.stas_koroliov),
            new Tale(80, 6000, R.string.stas_koroliov),

            new Tale(81, 5700, R.string.katia_rogova),
            new Tale(82, 6000, R.string.katia_rogova),
            new Tale(83, 6000, R.string.katia_rogova),
            new Tale(84, 5500, R.string.ivan_marunych),
            new Tale(85, 6800, R.string.ivan_marunych),
            new Tale(86, 6500, R.string.ivan_marunych),
            new Tale(87, 6500, R.string.nata_smirnova),
            new Tale(88, 5500, R.string.nata_smirnova),
            new Tale(89, 5600, R.string.nata_smirnova),
            new Tale(90, 5750, R.string.oleg_moskalenko),

            new Tale(91, 5400, R.string.oleg_moskalenko),
            new Tale(92, 5800, R.string.oleg_moskalenko),
            new Tale(93, 7200, R.string.rosava),
            new Tale(94, 6800, R.string.rosava),
            new Tale(95, 7000, R.string.rosava),
            new Tale(96, 6600, R.string.jamala),
            new Tale(97, 6500, R.string.jamala),
            new Tale(98, 6300, R.string.jamala),
            new Tale(99, 6700, R.string.sergii_tanchynets),
            new Tale(100, 6500, R.string.sergii_tanchynets),

            new Tale(101, 6700, R.string.sergii_tanchynets),
            new Tale(102, 7300, R.string.inna_grebeniuk),
            new Tale(103, 6500, R.string.inna_grebeniuk),
            new Tale(104, 6000, R.string.inna_grebeniuk),
            new Tale(105, 6800, R.string.olga_shurova),
            new Tale(106, 6200, R.string.olga_shurova),
            new Tale(107, 6300, R.string.olga_shurova),
            new Tale(108, 6500, R.string.anastasiia_gudyma),
            new Tale(109, 6500, R.string.anastasiia_gudyma),
            new Tale(110, 6850, R.string.anastasiia_gudyma),

            new Tale(111, 7500, R.string.dmytro_horkin),
            new Tale(112, 5500, R.string.dmytro_horkin),
            new Tale(113, 5500, R.string.dmytro_horkin),
            new Tale(114, 6500, R.string.dmytro_horkin),
            new Tale(115, 7800, R.string.kateryna_ofliyan),
            new Tale(116, 6000, R.string.kateryna_ofliyan),
            new Tale(117, 7400, R.string.kateryna_ofliyan),
            new Tale(118, 0, R.string.ira_bova),
            new Tale(119, 0, R.string.nina_matvienko),
            new Tale(120, 0, R.string.olga_tokar),

            new Tale(121, 0, R.string.olga_tokar),
            new Tale(122, 0, R.string.vitaliy_bilonozhko),
            new Tale(123, 7000, R.string.nataliya_vasko),
            new Tale(124, 2000, R.string.ruhanko_man),
            new Tale(125, 2000, R.string.roxolana),
            new Tale(126, 4000, R.string.ivan_kaluzhny),
            new Tale(127, 500, R.string.antonina_hyzhniak),
            new Tale(128, 3000, R.string.kola),
            new Tale(129, 500, R.string.kler),
            new Tale(130, 6000, R.string.julia_sanina),

            new Tale(131, 4000, R.string.busha),
            new Tale(132, 12000, R.string.pavlo_vyshebaba),
            new Tale(133, 12000, R.string.irma_vitovska),
            new Tale(134, 11000, R.string.olena_topolia),
            new Tale(135, 3000, R.string.antonina_hyzhniak),
            new Tale(136, 3000, R.string.timur_miroshnychenko),
            new Tale(137, 2500, R.string.serhii_prytula),
            new Tale(138, 4000, R.string.roxolana),
            new Tale(139, 11000, R.string.natalka_denysenko),
            new Tale(140, 4000, R.string.zlata_ognevich),

            new Tale(141, 3500, R.string.taras_kompanichenko),
            new Tale(142, 3000, R.string.vlad_rudnitsky)

//            new Tale(3, 5000, R.string.),
    );
}
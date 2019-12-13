package media.suspilne.kazky;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Readers {
    public ArrayList<Reader> Readers;

    public Readers(){
        if (isAscSorted()){
            Collections.sort(items, (c1, c2) -> c1.getName().compareTo(c2.getName()));
        }else{
            Collections.sort(items, (c1, c2) -> c2.talesCount.compareTo(c1.talesCount));
        }

        Readers = items;
    }

    public static boolean isAscSorted(){
        return SettingsHelper.getBoolean("isAscSorted");
    }

    private ArrayList<Reader> items = new ArrayList<>(Arrays.asList(
            new Reader(R.string.andrii_hlyvniuk, R.string.andrii_hlyvniuk_description),
            new Reader(R.string.marko_galanevych, R.string.marko_galanevych_description),
            new Reader(R.string.alina_pash, R.string.alina_pash_description),
            new Reader(R.string.alyona_alyona, R.string.alyona_alyona_description),
            new Reader(R.string.vova_zi_lvova, R.string.vova_zi_lvova_description),
            new Reader(R.string.evgen_klopotenko, R.string.evgen_klopotenko_description),
            new Reader(R.string.evgen_maluha, R.string.evgen_maluha_description),
            new Reader(R.string.anna_nikitina, R.string.anna_nikitina_description),
            new Reader(R.string.vlad_fisun, R.string.vlad_fisun_description),
            new Reader(R.string.dmytro_schebetiuk, R.string.dmytro_schebetiuk_description),
            new Reader(R.string.katia_rogova, R.string.katia_rogova_description),
            new Reader(R.string.michel_schur, R.string.michel_schur_description),
            new Reader(R.string.mariana_golovko, R.string.mariana_golovko_description),
            new Reader(R.string.marta_liubchyk, R.string.marta_liubchyk_description),
            new Reader(R.string.marusia_ionova, R.string.marusia_ionova_description),
            new Reader(R.string.oleksiy_dorychevsky, R.string.oleksiy_dorychevsky_description),
            new Reader(R.string.pavlo_varenitsa, R.string.pavlo_varenitsa_description),
            new Reader(R.string.roman_yasynovsky, R.string.roman_yasynovsky_description),
            new Reader(R.string.ruslana_khazipova, R.string.ruslana_khazipova_description),
            new Reader(R.string.sasha_koltsova, R.string.sasha_koltsova_description),
            new Reader(R.string.sergii_zhadan, R.string.sergii_zhadan_description),
            new Reader(R.string.sergii_kolos, R.string.sergii_kolos_description),
            new Reader(R.string.solomia_melnyk, R.string.solomia_melnyk_description),
            new Reader(R.string.stas_koroliov, R.string.stas_koroliov_description),
            new Reader(R.string.timur_miroshnychenko, R.string.timur_miroshnychenko_description),
            new Reader(R.string.hrystyna_soloviy, R.string.hrystyna_soloviy_description),
            new Reader(R.string.julia_jurina, R.string.julia_jurina_description),
            new Reader(R.string.jaroslav_ljudgin, R.string.jaroslav_ljudgin_description),
            new Reader(R.string.ivan_marunych, R.string.ivan_marunych_description),
            new Reader(R.string.nata_smirnova, R.string.nata_smirnova_description),
            new Reader(R.string.oleg_moskalenko, R.string.oleg_moskalenko_description),
            new Reader(R.string.rosava, R.string.rosava_description),
            new Reader(R.string.jamala, R.string.jamala_description)
    ));
}
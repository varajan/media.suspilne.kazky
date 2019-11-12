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
            new Reader(R.string.beethoven),
            new Reader(R.string.rachmaninov),
            new Reader(R.string.chaikovsky),
            new Reader(R.string.mendelson),
            new Reader(R.string.bach),
            new Reader(R.string.musorgsky),
            new Reader(R.string.elgar),
            new Reader(R.string.leontovych),
            new Reader(R.string.bilash),
            new Reader(R.string.bellini),
            new Reader(R.string.lysenko),
            new Reader(R.string.khachaturian),
            new Reader(R.string.shostakovich),
            new Reader(R.string.chopin),
            new Reader(R.string.haydn),
            new Reader(R.string.list),
            new Reader(R.string.debussy),
            new Reader(R.string.orff),
            new Reader(R.string.ravel),
            new Reader(R.string.borodin),
            new Reader(R.string.rossini),
            new Reader(R.string.saint_saens),
            new Reader(R.string.wagner),
            new Reader(R.string.mozart),
            new Reader(R.string.strauss_i),
            new Reader(R.string.strauss_ii),
            new Reader(R.string.strauss_eduard),
            new Reader(R.string.vivaldi),
            new Reader(R.string.piazzolla),
            new Reader(R.string.bizet),
            new Reader(R.string.grieg),
            new Reader(R.string.offenbach),
            new Reader(R.string.boccherini),
            new Reader(R.string.ponchielli),
            new Reader(R.string.dukas),
            new Reader(R.string.barber),
            new Reader(R.string.rimsky_korsakov),
            new Reader(R.string.verdi),
            new Reader(R.string.brahms),
            new Reader(R.string.handel),
            new Reader(R.string.prokofiev),
            new Reader(R.string.puccini),
            new Reader(R.string.donizetti),
            new Reader(R.string.gounod)
    ));
}
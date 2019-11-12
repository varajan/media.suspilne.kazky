package media.suspilne.kazky;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Readers {
    public static boolean isAscSorted(){
        return HSettings.getBoolean("isAscSorted");
    }

    public ArrayList<Reader> readers;

    public Readers(){
        if (isAscSorted()){
            Collections.sort(items, (c1, c2) -> c1.getName().compareTo(c2.getName()));
        }else{
            Collections.sort(items, (c1, c2) -> c2.talesCount.compareTo(c1.talesCount));
        }

        readers = items;
    }

    private ArrayList<Reader> items = new ArrayList<>(Arrays.asList(
            new Reader(1, "Євген Малуха", "Актор театру, кіно та дубляжу")
    ));
}

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
            new Reader(R.string.marko_galanevych, R.string.marko_galanevych_description)
    ));
}
package media.suspilne.kazky;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListHelper {
    public static List<String> removeBlank(String[] array){
        return removeBlank(Arrays.asList(array));
    }

    public static List<String> removeBlank(List<String> list){
        List<String> result = new ArrayList<String>();

        for (String item: list) {
            if (item != null && !item.equals(""))
                result.add(item);
        }

        return result;
    }
}

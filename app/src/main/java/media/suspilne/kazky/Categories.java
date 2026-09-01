package media.suspilne.kazky;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Categories {
    public static List<Category> Items = new ArrayList<>(Arrays.asList(
            new Category(R.string.lullabies, Arrays.asList(119, 120, 121, 122)),
            new Category(R.string.brave, Arrays.asList(130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141))
    ));

    public static List<Integer> NameIds = Items.stream().map(category -> category.title).toList();
}

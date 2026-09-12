package media.suspilne.kazky.data;

import java.util.Arrays;
import java.util.List;

import media.suspilne.kazky.R;

public class Categories {
    private static final Range[] ukrainianTales = {
            new Range(1, 2),
            new Range(6, 6),
            new Range(12, 12),
            new Range(17, 29),
            new Range(31, 77),
            new Range(104, 104),
    };

    private static final Range[] internationalTales = {
            new Range(3, 3),
            new Range(7, 7),
            new Range(13, 16),
            new Range(78, 103),
            new Range(105, 117),
    };

    private static final Range[] bigWorldAnimalsTales = {
            new Range(1, 6),
            new Range(8, 13),
            new Range(20, 23),
            new Range(26, 27),
            new Range(30, 30),
            new Range(33, 35),
            new Range(37, 37),
            new Range(39, 40),
            new Range(44, 44),
            new Range(47, 47),
            new Range(58, 59),
            new Range(61, 61),
            new Range(63, 63),
            new Range(65, 65),
            new Range(67, 67),
            new Range(72, 72),
            new Range(74, 76),
            new Range(78, 78),
            new Range(80, 82),
            new Range(94, 94),
            new Range(96, 96),
            new Range(99, 100),
            new Range(105, 105),
            new Range(115, 115),
            new Range(117, 117),
            new Range(124, 129),
            new Range(142, 142),
    };

    private static final Range[] modernAuthorTales = {
            new Range(118, 118),
            new Range(123, 123),
            new Range(125, 125),
        };

    public static List<Category> Items = Arrays.asList(
            new Category(R.string.ukrainianFolkTales, ukrainianTales),
            new Category(R.string.internationalTales, internationalTales),
            new Category(R.string.bigWorldAnimals, bigWorldAnimalsTales),
            new Category(R.string.brave, new Range(130, 141)),
            new Category(R.string.modernAuthorTales, modernAuthorTales),
            new Category(R.string.lullabies, new Range(119, 122))
        );

    public static List<Integer> NameIds = Items.stream().map(category -> category.title).toList();
}

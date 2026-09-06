package media.suspilne.kazky;

import java.util.Arrays;
import java.util.List;

public class Categories {
    private static final Range[] ukrainianTales = {
            new Range(1, 2),
            new Range(6, 6),
            new Range(9, 10),
            new Range(17, 18),
            new Range(19, 21),
            new Range(23, 29),
            new Range(31, 39),
            new Range(41, 43),
            new Range(45, 49),
            new Range(51, 53),
            new Range(55, 58),
            new Range(60, 63),
            new Range(66, 71),
            new Range(73, 77),
            new Range(81, 83),
            new Range(86, 89),
            new Range(91, 91),
            new Range(95, 98),
            new Range(101, 107),
            new Range(109, 110),
            new Range(113, 117)
        };

    private static final Range[] internationalTales = {
            new Range(3, 5),
            new Range(7, 8),
            new Range(11, 16),
            new Range(22, 22),
            new Range(30, 30),
            new Range(40, 40),
            new Range(44, 44),
            new Range(50, 50),
            new Range(54, 54),
            new Range(59, 59),
            new Range(64, 65),
            new Range(72, 72),
            new Range(78, 80),
            new Range(84, 85),
            new Range(90, 90),
            new Range(92, 94),
            new Range(99, 100),
            new Range(108, 108),
            new Range(111, 112),
            new Range(123, 123)
        };

    private static final Range[] bigWorldAnimalsTales = {
            new Range(124, 129),
            new Range(142, 142)
        };

    private static final Range[] modernAuthorTales = {
            new Range(118, 118),
            new Range(142, 142)
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

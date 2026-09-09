package media.suspilne.kazky;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Category {
    public Integer title;
    public List<Integer> taleIds;

    public Category(Integer title, Range... ranges) {
        this.title = title;
        this.taleIds = Arrays.stream(ranges)
                .flatMap(range -> IntStream.rangeClosed(range.from, range.to).boxed())
                .toList();
    }
}

package media.suspilne.kazky;

import java.util.List;

public class Category {
    public Integer title;
    public List<Integer> taleIds;

    public Category(Integer title, List<Integer> taleIds) {
        this.title = title;
        this.taleIds = taleIds;
    }
}

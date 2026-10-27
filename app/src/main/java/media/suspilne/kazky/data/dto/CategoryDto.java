package media.suspilne.kazky.data.dto;

import androidx.annotation.NonNull;

import java.util.List;

public class CategoryDto {
    private String title;
    private List<Integer> taleIds;

    public CategoryDto() {
    }

    public CategoryDto(String name, List<Integer> taleIds) {
        this.taleIds = taleIds;
        this.title = name;
    }

    public List<Integer> taleIds() {
        return taleIds;
    }

    public String title() {
        return title;
    }

    @NonNull
    @Override
    public String toString() {
        return "CategoryDto{" +
                "taleIds=" + taleIds +
                ", title='" + title + '\'' +
                '}';
    }
}

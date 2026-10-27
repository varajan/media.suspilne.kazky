package media.suspilne.kazky.data.dto;

import androidx.annotation.NonNull;

public class ReaderDto {
    private String id;
    private String name;
    private String description;

    public ReaderDto() {
    }

    public ReaderDto(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    @NonNull
    @Override
    public String toString() {
        return "ReaderDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
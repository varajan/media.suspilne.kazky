package media.suspilne.kazky.data.dto;

public class TaleDto {
    private int id;
    private String title;
    private String duration;
    private boolean hasColoring;
    private int intro;
    private String reader;

    public TaleDto() {
    }

    public TaleDto(int id, String title, String duration, boolean hasColoring, int intro, String reader) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.hasColoring = hasColoring;
        this.intro = intro;
        this.reader = reader;
    }

    public int id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String duration() {
        return duration;
    }

    public boolean hasColoring() {
        return hasColoring;
    }

    public int intro() {
        return intro;
    }

    public String reader() {
        return reader;
    }

    @Override
    public String toString() {
        return "TaleInfo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", duration='" + duration + '\'' +
                ", hasColoring=" + hasColoring +
                ", intro=" + intro +
                ", reader='" + reader + '\'' +
                '}';
    }
}
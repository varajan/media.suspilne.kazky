package media.suspilne.kazky.data;

public class TaleInfo {
    private int id;
    private String title;
    private String duration;
    private boolean hasColoring;
    private int intro;
    private String reader;

    public TaleInfo() {
    }

    public TaleInfo(int id, String title, String duration, boolean hasColoring, int intro, String reader) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.hasColoring = hasColoring;
        this.intro = intro;
        this.reader = reader;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public boolean hasColoring() {
        return hasColoring;
    }

    public int getIntro() {
        return intro;
    }

    public String getReader() {
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
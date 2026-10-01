package media.suspilne.kazky.data;

public class TaleInfo {
    private int id;
    private String duration;
    private boolean hasColoring;

    public TaleInfo() {
    }

    public TaleInfo(int id, String duration, boolean hasColoring) {
        this.id = id;
        this.duration = duration;
        this.hasColoring = hasColoring;
    }

    public int getId() {
        return id;
    }

    public String getDuration() {
        return duration;
    }

    public boolean isHasColoring() {
        return hasColoring;
    }

    @Override
    public String toString() {
        return "TaleInfo{" +
                "id=" + id +
                ", duration='" + duration + '\'' +
                ", hasColoring=" + hasColoring +
                '}';
    }
}
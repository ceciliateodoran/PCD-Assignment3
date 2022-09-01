package distributed;

public class City {

    private int width;
    private int height;

    private int gridRows;
    private int gridColumns;

    private int sensors;

    public City(final int w, final int h, final int gridRows, final int gridColumns, final int numOfSensors) {
        this.width = w;
        this.height = h;
        this.gridRows = gridRows;
        this.gridColumns = gridColumns;
        this.sensors = numOfSensors;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getGridRows() {
        return gridRows;
    }

    public int getGridColumns() {
        return gridColumns;
    }

    public int getSensors() {
        return sensors;
    }

    @Override
    public String toString() {
        return "City{" +
                "width=" + width +
                ", height=" + height +
                ", gridRows=" + gridRows +
                ", gridColumns=" + gridColumns +
                ", sensors=" + sensors +
                '}';
    }
}

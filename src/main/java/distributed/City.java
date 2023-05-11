package distributed;

public class City {

    private int width;
    private int height;

    private int gridRows;
    private int gridColumns;

    private int sensors;
    private final double limit;

    public City(final int w, final int h, final int gridRows, final int gridColumns, final int numOfSensors, final double limit) {
        this.width = w<gridColumns ? gridColumns : w;
        this.height = h<gridRows ? gridRows : h;
        this.gridRows = gridRows<1 ? 1 : gridRows;
        this.gridColumns = gridColumns<1 ? 1 : gridColumns;
        this.sensors = numOfSensors;
        this.limit = limit;
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

    public double getLimit() { return limit; }

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

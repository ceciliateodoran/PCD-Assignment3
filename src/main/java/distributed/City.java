package distributed;

/**
 * Represents the data structure of a City
 */
public class City {
    private int width;
    private int height;
    private int gridRows;
    private int gridColumns;
    private int sensors;
    private final double limit;

    /**
     * Construct a new instance of a city
     *
     * @param w The width of the city
     * @param h The height of the city
     * @param gridRows The number of rows
     * @param gridColumns The number of columns
     * @param numOfSensors The number of sensors
     * @param limit The maximum water level to consider
     */
    public City(final int w, final int h, final int gridRows, final int gridColumns, final int numOfSensors, final double limit) {
        this.width = w<gridColumns ? gridColumns : w;
        this.height = h<gridRows ? gridRows : h;
        this.gridRows = gridRows<1 ? 1 : gridRows;
        this.gridColumns = gridColumns<1 ? 1 : gridColumns;
        this.sensors = numOfSensors;
        this.limit = limit;
    }

    /**
     * @return the width of the city
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height of the city
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the number of rows
     */
    public int getGridRows() {
        return gridRows;
    }

    /**
     * @return the number of columns
     */
    public int getGridColumns() {
        return gridColumns;
    }

    /**
     * @return the number of sensors
     */
    public int getSensors() {
        return sensors;
    }

    /**
     * @return the value of the maximum water level considered
     */
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

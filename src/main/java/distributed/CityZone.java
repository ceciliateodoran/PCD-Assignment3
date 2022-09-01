package distributed;

public class CityZone {

    private int index;
    private int x;
    private int y;
    private int xOffset;
    private int yOffset;

    public CityZone(final int x, final int y, final int xOffset, final int yOffset, final int index) {
        this.x = x;
        this.y = y;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getxOffset() {
        return xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    @Override
    public String toString() {
        return "CityZone{" +
                "index=" + index +
                ", x=" + x +
                ", y=" + y +
                ", xOffset=" + xOffset +
                ", yOffset=" + yOffset +
                '}';
    }
}

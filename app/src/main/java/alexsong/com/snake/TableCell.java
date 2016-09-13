package alexsong.com.snake;

public class TableCell {
    private int x;
    private int y;

    public TableCell(int row, int col) {
        x = row;
        y = col;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int xVal) {
        x = xVal;
    }

    public void setY(int yVal) {
        y = yVal;
    }
}
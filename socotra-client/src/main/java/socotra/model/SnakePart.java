package socotra.model;

import socotra.util.Direction;

/**
 * This class is about the Snake moving part in Snake Game.
 */

public class SnakePart{
    private int x;
    private int y;

    public SnakePart(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    private synchronized void movePart(int deltaX, int deltaY){
        setX(getX() + deltaX);
        setY(getY() + deltaY);
    }

    public void move(Direction direction) {
        movePart(direction.getDeltaX(), direction.getDeltaY());
    }
}

package socotra.SnakeGame;

import java.util.Random;

public class Point {
    int x;
    int y;

    public Point() {
        this.setRandomPoint();
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Random coordinate generation
    public void setRandomPoint() {
        Random random = new Random();
        this.x = 20 * random.nextInt(460 / 20 - 1);
        this.y = 20 * random.nextInt(380 / 20 - 1);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "[(" + this.x + "," + this.y + ")]";
    }
}

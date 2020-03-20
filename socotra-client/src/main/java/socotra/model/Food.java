package socotra.model;

import socotra.util.Colors;

import java.util.Random;

/**
 * This class is about the food in Snake Game.
 */

public class Food{
    private int x;
    private int y;
    private Colors color;

    public Food(int x, int y) {
        this.x = x;
        this.y = y;
        nextColor();
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

    public Colors getColor() {
        return color;
    }

    public void setColor(Colors color) {
        this.color = color;
    }

    public void nextColor(){
        Random random = new Random();
        Colors newColor;
        int color;
        do {
            color = random.nextInt(Colors.values().length);
            newColor = Colors.values()[color];
        }while (getColor() == newColor);

        setColor(newColor);
    }

    public void nextPosition(int boardWidth, int boardHeight) {
        Random random = new Random();
        int newX = random.nextInt(boardWidth);
        int newY = random.nextInt(boardHeight);

        setX(newX);
        setY(newY);
    }
}

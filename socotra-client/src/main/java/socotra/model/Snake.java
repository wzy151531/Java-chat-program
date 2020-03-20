package socotra.model;

import socotra.util.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class is about the Snake in Snake Game.
 */

public class Snake{
    private List<SnakePart> snake = new ArrayList<>();
    private int speed;
    private Direction direction;

    public Snake(int width, int height) {
        this.direction = getRandomDirection();
        addPart(width/2 + 2 * direction.getDeltaX(),height/2 + 2 * direction.getDeltaY());
        addPart(width/2 + direction.getDeltaX(),height/2 + direction.getDeltaY());
        addPart(width/2,height/2);
        this.speed = 1;
    }

    private Direction getRandomDirection() {
        Random random = new Random();
        int dir = random.nextInt(4);
        return Direction.values()[dir];
    }

    public List<SnakePart> getSnake() {
        return snake;
    }

    public SnakePart getPart(int i){
        return snake.get(i);
    }

    public void addPart(int x, int y){
        snake.add(new SnakePart(x, y));
    }

    public int getSpeed() {
        return speed;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public SnakePart getHead() {
        return getPart(0);
    }

    public synchronized boolean move() {
        for(int i = snake.size() - 1; i > 0; i--){
            snake.get(i).setX(snake.get(i-1).getX());
            snake.get(i).setY(snake.get(i-1).getY());
        }

        getHead().move(getDirection());

        return !isSelfColided();
    }

    public boolean canEat(Food food) {
        for(SnakePart snakePart : snake){
            if(snakePart.getX() == food.getX() && snakePart.getY() == food.getY()) return true;
        }
        
        return false;
    }

    public void eat(Food food) {
        addPart(food.getX(), food.getY());
    }

    private boolean isSelfColided() {
        for(int i = 1; i < snake.size(); i++){
            if(snake.get(0).getX() == snake.get(i).getX() && snake.get(0).getY() == snake.get(i).getY()){
                return true;
            }
        }

        return false;
    }

    public void increaseSpeed() {
        if(speed <= 20){
            speed++;
        }
    }
}

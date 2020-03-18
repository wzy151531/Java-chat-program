package socotra.game;



import socotra.model.Food;
import socotra.model.Snake;
import socotra.model.SnakePart;

import java.util.Random;

public class Game {
    private Snake snake;
    private Food food;

    private final int boardWidth;
    private final int boardHeight;

    private long score;
    private boolean gameOver;

    public Game(int width, int height) {
        this.boardWidth = width;
        this.boardHeight = height;
        this.score = 0;
        this.gameOver = false;

        int randX = (new Random()).nextInt(boardHeight);
        int randY = (new Random()).nextInt(boardWidth);

        this.snake = new Snake(boardWidth, boardHeight);
        this.food = new Food(randX, randY);

    }

    public Snake getSnake() {
        return snake;
    }

    public Food getFood() {
        return food;
    }

    public long getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isPaused(){
        return isGameOver();
    }

    public boolean isSnakeHitEdge() {
        for(SnakePart snakePart : snake.getSnake()){
            if(snakePart.getX() == -1 || snakePart.getX() == boardWidth || snakePart.getY() == -1 || snakePart.getY() == boardHeight){
                return true;
            }
        }

        return false;
    }

    public void nextFood() {
        food.nextColor();
        do{
            food.nextPosition(boardWidth, boardHeight);
        }while (foodInCollisionWithSnake());
    }

    private boolean foodInCollisionWithSnake() {
        for(int i = 0; i < getSnake().getSnake().size(); i++){
            if(getFood().getX() == getSnake().getPart(i).getX() && getFood().getY() == getSnake().getPart(i).getY()){
                return true;
            }
        }

        return false;
    }

    public void increaseScore() {
        score += 10;
        snake.increaseSpeed();
    }
}

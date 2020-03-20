package socotra.controller;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import socotra.game.Game;
import socotra.model.Snake;
import socotra.model.SnakePart;
import socotra.util.Direction;

import java.net.URL;
import java.util.ResourceBundle;

//import java.awt.*;
//import java.net.URL;
//import java.util.ResourceBundle;

/**
 * This class is about controller of Snake Game page.
 */

public class BoardController implements Initializable, Runnable {
    public Canvas canvas;
    public BorderPane pane;
    /**
     * Pause button.
     */
    public Button pauseButton;
    /**
     * Restart game button.
     */
    public Button restartButton;
    /**
     * End game button.
     */
    public Button endGameButton;
    /**
     * The score of the game
     */
    public Label score;

    private GraphicsContext graphicsContext;
    private Game game;

    private static final int WIDTH = 40;
    private static final int HEIGHT = 40;
    private static final int PIXEL = 10;
    private static final int TICKER_INTERVAL = 300;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        game = new Game(WIDTH, HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();

        drawBoard(graphicsContext);
        bindInputHandlers();

        canvas.requestFocus();
        new Thread(this).start();
    }

    private void drawBoard(GraphicsContext graphicsContext) {
        graphicsContext.setFill(javafx.scene.paint.Color.BLACK);
        graphicsContext.fillRect(0,0, WIDTH * PIXEL, HEIGHT * PIXEL);

        Snake snake = game.getSnake();
        for(SnakePart part : snake.getSnake()){
            graphicsContext.setFill(javafx.scene.paint.Color.BLACK); //border
            graphicsContext.fillRect(game.getFood().getX() * PIXEL, game.getFood().getY() * PIXEL, PIXEL, PIXEL);
            graphicsContext.setFill(javafx.scene.paint.Color.WHITE); //inside
            graphicsContext.fillRect(part.getX() * PIXEL + 0.5, part.getY() * PIXEL + 0.5, PIXEL - 1, PIXEL - 1);
        }

        //food
        graphicsContext.setFill(game.getFood().getColor().getColor());
        graphicsContext.fillRect(game.getFood().getX() * PIXEL, game.getFood().getY() * PIXEL, PIXEL, PIXEL);

        Platform.runLater(() -> score.setText(String.valueOf(game.getScore())));
    }

    private void bindInputHandlers() {
        canvas.setOnKeyTyped(this::handleKeyInput);
        pauseButton.setOnAction((actionEvent) -> pauseOrResume());
        restartButton.setOnAction((actionEvent) -> restart());
        endGameButton.setOnAction((actionEvent) -> endGame());
    }

    private void handleKeyInput(KeyEvent event) {
        switch (event.getCharacter()){
            case "w":{
                if(game.getSnake().getDirection() != Direction.DOWN) {
                    game.getSnake().setDirection(Direction.UP);
                }
                break;
            }
            case "a":{
                if(game.getSnake().getDirection() != Direction.RIGHT) {
                    game.getSnake().setDirection(Direction.LEFT);
                }
                break;
            }
            case "s":{
                if(game.getSnake().getDirection() != Direction.UP) {
                    game.getSnake().setDirection(Direction.DOWN);
                }
                break;
            }
            case "d":{
                if(game.getSnake().getDirection() != Direction.LEFT) {
                    game.getSnake().setDirection(Direction.RIGHT);
                }
                break;
            }
        }
    }

    private synchronized void pauseOrResume() {
        if(game.isPaused()){
            pauseButton.setText("pause");
            game.setGameOver(false);
        }else{
            pauseButton.setText("resume");
            game.setGameOver(true);
        }

        notifyAll();
    }

    private synchronized void restart() {
        pauseButton.setDisable(false);
        endGameButton.setDisable(false);
        game = new Game(WIDTH, HEIGHT);
        pauseButton.setText("pause");
        notifyAll();
    }

    private synchronized void endGame() {
        pauseButton.setDisable(true);
        endGameButton.setDisable(true);
        game.setGameOver(true);
        showGameOverMessage();
        pauseButton.setText("pause");
        notifyAll();
    }

    @Override
    public void run() {
        while(!Thread.interrupted()){
            Platform.runLater(() -> canvas.requestFocus());
            play();
            try {
                Thread.sleep(TICKER_INTERVAL - game.getSnake().getSpeed() * 10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized ( this){
                while (game.isGameOver()){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void play() {
        if(!game.getSnake().move() || game.isSnakeHitEdge()){
            game.setGameOver(true);
            showGameOverMessage();
            pauseButton.setDisable(true);
            endGameButton.setDisable(true);
            return;
        }

        if(game.getSnake().canEat(game.getFood())){
            game.getSnake().eat(game.getFood());
            game.increaseScore();
            game.nextFood();
        }


        drawBoard(graphicsContext);
    }

    private void showGameOverMessage() {
        String message = "Oh no, Game Over!";
        graphicsContext.setFill(javafx.scene.paint.Color.BLACK);
        graphicsContext.fillRect(0,0, WIDTH * PIXEL, HEIGHT * PIXEL);
        graphicsContext.setFill(javafx.scene.paint.Color.WHITE);
        graphicsContext.setTextAlign(TextAlignment.CENTER);
        graphicsContext.setTextBaseline(VPos.CENTER);
        graphicsContext.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        graphicsContext.fillText(message, (WIDTH * PIXEL) / 2., (HEIGHT * PIXEL) / 2.);
    }
}

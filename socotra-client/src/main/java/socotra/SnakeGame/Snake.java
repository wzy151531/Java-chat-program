package socotra.SnakeGame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

enum Direction {
    R, L, U, D
}

public class Snake extends JPanel implements KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // The width and height of Gaming Panel
    private final int PANEL_WIDTH = 482;
    private final int PANEL_HEIGHT = 402;
    // Snake node size
    private final int NODE_SIZE = 20;
    // Set color
    private final Color BODY_COLOR = new Color(30, 144, 255);
    private final Color FOOD_COLOR = new Color(50, 205, 50);
    // The max level
    private final int MAX_LEVEL = 5;

    private Point head = new Point(40, 200); // Define snake head
    private ArrayList<Point> body = new ArrayList<Point>(); // Define snake body
    private Point food; // Define food

    private int score = 0; // Define score records
    private int level = 1; // Define the level, add 1 level for every 5 foods eaten, the higher the level, the faster the speed
    private int count = 0; // Define a counter
    // Set the number of milliseconds for automatic refresh
    private int mills = 50;

    //	private boolean isEaten;
    private Direction direcion = Direction.R; // User-triggered change in motion direction, default to right
    private Direction current_Direcion = Direction.R; // Current movement direction, default to the right
    private boolean isGameOver;
    private boolean isPause; // Whether the game is paused

    private Thread run;

    public Snake() {
        InitGame();
        setSize(PANEL_WIDTH, PANEL_HEIGHT);
        setBackground(new Color(220, 220, 220));
        setLocation(20, 100);
        this.setFocusable(true);
        this.addKeyListener(this);
    }

    // Game initialization
    public void InitGame() {
        // Initialize some variables
//		isEaten = false;
        isGameOver = false;
        isPause = false;
        current_Direcion = Direction.R;
        direcion = Direction.R;
        score = 0;
        level = 1;
        count = 0;
        mills = 500;

        // Initialize the snake's head and body
        head.x = 40;
        head.y = 200;
        body.clear();
        body.add(new Point(20, 200));

        // Initialize the food
        food = new Point();
//		System.out.println(food.toString());
    }

    @Override
    protected void paintComponent(Graphics g) {
        // TODO Auto-generated method stub
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw border
        g2d.setColor(Color.black);
        g2d.drawLine(0, 0, 0, PANEL_HEIGHT - 1);
        g2d.drawLine(0, PANEL_HEIGHT - 1, PANEL_WIDTH - 1, PANEL_HEIGHT - 1);
        g2d.drawLine(PANEL_WIDTH - 1, PANEL_HEIGHT - 1, PANEL_WIDTH - 1, 0);
        g2d.drawLine(PANEL_WIDTH - 1, 0, 0, 0);

        // Draw snake head
        g2d.setColor(Color.red);
//		g2d.setColor(new Color(30, 144, 255));
        g2d.fillOval(head.x, head.y, NODE_SIZE, NODE_SIZE);

        // Draw snake body
        g2d.setColor(BODY_COLOR);
        for (Point point : body) {
            g2d.fillOval(point.x, point.y, NODE_SIZE, NODE_SIZE);
        }

        // Draw snake food
        g2d.setColor(FOOD_COLOR);
        g2d.fillOval(food.x, food.y, NODE_SIZE, NODE_SIZE);

        // Drawing length information
        g2d.setColor(Color.black);
        g2d.drawString("Length " + (body.size() + 1), 0, 10);
        // Draw score information
        g2d.drawString("Score " + score, 200, 10);
        // Draw level information
        g2d.drawString("Level " + level, 380, 10);

        // Draw other information
        g2d.drawString("Control keys = (w, s, a, d), Pause/Continue = (Space)", 0, 400);
    }

    // move
    public void move(Direction d) {
        // Create intermediate variables
        Point temp = new Point(0, 0);
        Point temp1 = new Point(0, 0);
        temp.x = head.x;
        temp.y = head.y;

        // Head movement
        if (d == Direction.L) {
            head.x -= 20;
        }
        if (d == Direction.R) {
            head.x += 20;
        }
        if (d == Direction.U) {
            head.y -= 20;
        }
        if (d == Direction.D) {
            head.y += 20;
        }
        current_Direcion = d;
        // body movement
        for (int i = 0; i < body.size(); i++) {
            temp1.x = body.get(i).x;
            temp1.y = body.get(i).y;
            body.get(i).x = temp.x;
            body.get(i).y = temp.y;
            temp.x = temp1.x;
            temp.y = temp1.y;
        }
        if (eatFood()) {
            body.add(temp);
            // Record score, plus 5 points for each meal
            score += 5;
            count++;
            // Regenerate food coordinates
            food.setRandomPoint();
//			isEaten = false;
        }
        repaint();

        hitWall();
        hitSelf();
        changeLevel();
        gameOver();
    }

    // Determine if eating food
    public boolean eatFood() {
        if (head.x == food.x && head.y == food.y) {
//			isEaten = true;
            return true;
        } else {
            return false;
        }
    }

    // Determine if hitting the wall
    public void hitWall() {
        if (!((head.x >= 0 && head.x <= 460) && (head.y >= 0 && head.y <= 380))) {
            isGameOver = true;
        }
    }

    // Determine if hitting own body
    public void hitSelf() {
        for (Point point : body) {
            if (head.x == point.x && head.y == point.y) {
                isGameOver = true;
                break;
            }
        }
    }

    // change the level
    public void changeLevel() {
        if (count == 5) {
            if (level != MAX_LEVEL) {
                level++;
                mills -= 100;
                count = 0;
            }
        }
    }

    // Game Over
    public void gameOver() {
        if (isGameOver) {
            int n = JOptionPane.showConfirmDialog(null, "Game Over! Play again?", "Game over!", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (n == 1) {
//				String name = JOptionPane.showInputDialog(null, "Hero, please leave your name！", "Please enter your name",
//						JOptionPane.INFORMATION_MESSAGE);
//				DBUtil.insert(name, score);
                System.exit(0);
            } else {
                InitGame();
            }
        }
    }

    // Move automatically
    public void autoMove() {
        run = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(mills);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (!isPause) {
                        move(direcion);
                    }
                }
            }
        };
        run.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            if (current_Direcion != Direction.D) {
                direcion = Direction.U;
//				move(direcion);
            }
        }
        if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            if (current_Direcion != Direction.U) {
                direcion = Direction.D;
//				move(direcion);
            }
        }
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            if (current_Direcion != Direction.R) {
                direcion = Direction.L;
//				move(direcion);
            }
        }
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            if (current_Direcion != Direction.L) {
                direcion = Direction.R;
//				move(direcion);
            }
        }
        // Press the space bar to pause and run
        if (keyCode == KeyEvent.VK_SPACE) {
            if (isPause) {
                isPause = false;
            } else {
                isPause = true;
            }
        }

//		repaint();
//		System.out.println(head.toString());
//		for (Point point : body) {
//			System.out.println(point.toString());
//		}
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}

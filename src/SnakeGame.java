// created by :- Gaurav Rajurkar

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }  

    int boardWidth;
    int boardHeight;
    int tileSize = 25;
    int borderSize = 25;
    
    //snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    //food
    Tile food;
    Random random;

    //game logic
    int velocityX;
    int velocityY;
    Timer gameLoop;

    boolean gameOver = false;
    boolean isPaused = false; // Pause state flag

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth + borderSize * 2, this.boardHeight + borderSize * 2));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<Tile>();

        food = new Tile(10, 10);
        random = new Random();
        placeFood();

        velocityX = 1;
        velocityY = 0;
        
        //game timer
        gameLoop = new Timer(200, this); // Changed delay to 200 milliseconds
        gameLoop.start();
    }   
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw the blue border around the game board
        g.setColor(Color.blue);
        g.fillRect(0, 0, boardWidth + borderSize * 2, borderSize); // Top border
        g.fillRect(0, 0, borderSize, boardHeight + borderSize * 2); // Left border
        g.fillRect(0, boardHeight + borderSize, boardWidth + borderSize * 2, borderSize); // Bottom border
        g.fillRect(boardWidth + borderSize, 0, borderSize, boardHeight + borderSize * 2); // Right border

        //Grid Lines
        g.setColor(Color.gray);
        for(int i = 0; i < boardWidth/tileSize; i++) {
            //(x1, y1, x2, y2)
            g.drawLine(i*tileSize + borderSize, borderSize, i*tileSize + borderSize, boardHeight + borderSize);
            g.drawLine(borderSize, i*tileSize + borderSize, boardWidth + borderSize, i*tileSize + borderSize); 
        }

        //Food
        g.setColor(Color.red);
        g.fillOval(food.x * tileSize + borderSize, food.y * tileSize + borderSize, tileSize, tileSize);

        //Snake Head
        g.setColor(Color.green);
        g.fillOval(snakeHead.x * tileSize + borderSize, snakeHead.y * tileSize + borderSize, tileSize, tileSize);
        
        //Snake Body
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);
            g.fillOval(snakePart.x * tileSize + borderSize, snakePart.y * tileSize + borderSize, tileSize, tileSize);
        }

        //Score
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (gameOver) {
            g.setColor(Color.red);
            g.drawString("Game Over: " + String.valueOf(snakeBody.size()), tileSize - 16 + borderSize, tileSize + borderSize);
        } else if (isPaused) {
            g.setColor(Color.yellow);
            g.drawString("Paused", tileSize - 16 + borderSize, tileSize + borderSize);
        } else {
            g.setColor(Color.white);
            g.drawString("Score: " + String.valueOf(snakeBody.size()), tileSize - 16 + borderSize, tileSize + borderSize);
        }
    }

    public void placeFood(){
        food.x = random.nextInt(boardWidth/tileSize);
        food.y = random.nextInt(boardHeight/tileSize);
    }

    public void move() {
        if (isPaused || gameOver) {
            return;
        }

        //eat food
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            placeFood();
        }

        //move snake body
        for (int i = snakeBody.size() - 1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) { //right before the head
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            } else {
                Tile prevSnakePart = snakeBody.get(i - 1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }
        //move snake head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        //game over conditions
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);

            //collide with snake head
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
            }
        }

        if (snakeHead.x * tileSize < 0 || snakeHead.x * tileSize >= boardWidth || //passed left border or right border
            snakeHead.y * tileSize < 0 || snakeHead.y * tileSize >= boardHeight ) { //passed top border or bottom border
            gameOver = true;
        }
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) { //called every x milliseconds by gameLoop timer
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }  

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) { // Pause/Resume the game with the space bar
            if (isPaused) {
                resumeGame();
            } else {
                pauseGame();
            }
        }
    }

    public void pauseGame() {
        isPaused = true;
        gameLoop.stop();
    }

    public void resumeGame() {
        isPaused = false;
        gameLoop.start();
    }

    //not needed
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}

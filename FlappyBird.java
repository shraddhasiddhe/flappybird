import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener, MouseListener {
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final int PIPE_WIDTH = 50;
    private final int PIPE_GAP = 150;

    private Bird bird;
    private ArrayList<Rectangle> pipes;
    private Random random;
    private Timer timer;
    private int score;

    private enum GameState { START, PLAYING, GAME_OVER }
    private GameState gameState;

    private Rectangle startButton;

    public FlappyBird() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.cyan);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        bird = new Bird(WIDTH / 4, HEIGHT / 2);
        pipes = new ArrayList<>();
        random = new Random();
        timer = new Timer(20, this);
        score = 0;

        gameState = GameState.START;
        startButton = new Rectangle(WIDTH / 2 - 50, HEIGHT / 2 + 50, 100, 50);

        timer.start();
    }

    private void addPipe() {
        int space = random.nextInt(HEIGHT - PIPE_GAP - 200) + 100;
        pipes.add(new Rectangle(WIDTH, 0, PIPE_WIDTH, space));
        pipes.add(new Rectangle(WIDTH, space + PIPE_GAP, PIPE_WIDTH, HEIGHT - space - PIPE_GAP));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            bird.update();

            // Move and remove pipes
            for (int i = 0; i < pipes.size(); i += 2) {
                Rectangle topPipe = pipes.get(i);
                Rectangle bottomPipe = pipes.get(i + 1);

                topPipe.x -= 5;
                bottomPipe.x -= 5;

                if (topPipe.x + PIPE_WIDTH < 0) {
                    pipes.remove(i);
                    pipes.remove(i);
                    i -= 2;
                    score++;
                }
            }

            // Add new pipes
            if (pipes.isEmpty() || pipes.get(pipes.size() - 1).x < WIDTH - 300) {
                addPipe();
            }

            // Check for collisions
            if (bird.y < 0 || bird.y > HEIGHT) {
                gameState = GameState.GAME_OVER;
            }

            for (Rectangle pipe : pipes) {
                if (pipe.intersects(bird.x, bird.y, bird.SIZE, bird.SIZE)) {
                    gameState = GameState.GAME_OVER;
                    break;
                }
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        switch (gameState) {
            case START:
                drawStartScreen(g);
                break;
            case PLAYING:
                drawGame(g);
                break;
            case GAME_OVER:
                drawGame(g);
                drawGameOverScreen(g);
                break;
        }
    }

    private void drawStartScreen(Graphics g) {
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Flappy Bird", WIDTH / 2 - 100, HEIGHT / 2 - 50);

        g.setColor(Color.green);
        g.fillRect(startButton.x, startButton.y, startButton.width, startButton.height);
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Start", startButton.x + 25, startButton.y + 30);

        drawInstructions(g);
    }

    private void drawGame(Graphics g) {
        // Draw bird
        g.setColor(Color.yellow);
        g.fillRect(bird.x, bird.y, bird.SIZE, bird.SIZE);

        // Draw pipes
        g.setColor(Color.green);
        for (Rectangle pipe : pipes) {
            g.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);
        }

        // Draw score
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 30);
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.red);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Game Over!", WIDTH / 2 - 100, HEIGHT / 2 - 50);

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Final Score: " + score, WIDTH / 2 - 80, HEIGHT / 2);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Press SPACE to restart", WIDTH / 2 - 100, HEIGHT / 2 + 40);
    }

    private void drawInstructions(Graphics g) {
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        String[] instructions = {
            "How to play:",
            "- Press SPACE to make the bird jump",
            "- Navigate through the pipes",
            "- Don't hit the pipes or the ground",
            "- Score points by passing through pipes"
        };
        for (int i = 0; i < instructions.length; i++) {
            g.drawString(instructions[i], 50, HEIGHT - 150 + i * 20);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            switch (gameState) {
                case PLAYING:
                    bird.jump();
                    break;
                case GAME_OVER:
                    resetGame();
                    gameState = GameState.PLAYING;
                    break;
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (gameState == GameState.START && startButton.contains(e.getPoint())) {
            gameState = GameState.PLAYING;
        }
    }

    private void resetGame() {
        bird = new Bird(WIDTH / 4, HEIGHT / 2);
        pipes.clear();
        addPipe();
        score = 0;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Bird {
    public int x, y;
    public final int SIZE = 20;
    private int velocity = 0;
    private static final int GRAVITY = 1;
    private static final int JUMP_STRENGTH = -15;

    public Bird(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        velocity += GRAVITY;
        y += velocity;
    }

    public void jump() {
        velocity = JUMP_STRENGTH;
    }
}

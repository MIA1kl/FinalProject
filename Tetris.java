package sample;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;


class Tetris extends JFrame {

    final String mainTitle = "Tetris Game";
    final int sizeOfBlock = 25;
    final int panelWidth = 15;
    final int panelHeight = 20;
    final int locate = 10;
    final int FIELD_DX = 20;
    final int FIELD_DY = 40;
    final int left = 37;
    final int up = 38;
    final int right = 39;
    final int down = 40;
    final int time = 450;
    final int[][][] SHAPES = {
            {{0, 0, 0, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}, {4, 0x00f0f0}}, // I
            {{0, 0, 0, 0}, {0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {4, 0xf0f000}}, // O
            {{1, 0, 0, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0x0000f0}}, // J
            {{0, 0, 1, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xf0a000}}, // L
            {{0, 1, 1, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0x00f000}}, // S
            {{1, 1, 1, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xa000f0}}, // T
            {{1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xf00000}}, // Z

    };
    int gameScore = 0;
    int lines = 0;
    int[][] mine = new int[panelHeight + 1][panelWidth];
    JFrame frame;
    Canvas canvas = new Canvas();
    Random random = new Random();
    Figure figure = new Figure();
    boolean gameOver = false;


    public static void main(String[] args) {

        new Tetris().go();

    }

    Tetris() {

        //ImageView image = new ImageView("https://cdn0.iconfinder.com/data/icons/toys/256/teddy_bear_toy_6.png");
        setTitle(mainTitle);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(locate, locate, (panelWidth) * sizeOfBlock + FIELD_DX, panelHeight * sizeOfBlock + FIELD_DY);
        setResizable(false);

       canvas.setBackground(Color.gray);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!gameOver) {
                    if (e.getKeyCode() == down) figure.drop();
                    if (e.getKeyCode() == up) figure.rotate();
                    if (e.getKeyCode() == left || e.getKeyCode() == right) figure.move(e.getKeyCode());
                }
                canvas.repaint();
            }
        });
        add(BorderLayout.CENTER, canvas);
        setVisible(true);
        Arrays.fill(mine[panelHeight], 1);
    }


    void go() {
        while (!gameOver) {
            try {
                Thread.sleep(time);
            } catch (Exception e) {
                e.printStackTrace();
            }
            canvas.repaint();
            checkFilling();
            if (figure.isTouchGround()) {
                figure.leaveOnTheGround();
                figure = new Figure();
                gameOver = figure.isCrossGround();
            } else
                figure.stepDown();
        }

    }

    void checkFilling() { // check filling rows
        int row = panelHeight - 1;
        int countFillRows = 0;
        while (row > 0) {
            int filled = 1;
            for (int col = 0; col < panelWidth - 5; col++)
                filled *= Integer.signum(mine[row][col]);
            if (filled > 0) {
                countFillRows++;
                for (int i = row; i > 0; i--) System.arraycopy(mine[i - 1], 0, mine[i], 0, panelWidth - 5);
            } else
                row--;
        }
        if (countFillRows > 0) {
            gameScore += 10;
            lines++;
        }
    }


    class Figure {
        private ArrayList<Block> figure = new ArrayList<Block>();
        private int[][] shape = new int[4][4];
        private int type, size, color;
        private int x = 3, y = 0; // starting left up corner

        Figure() {
            type = random.nextInt(SHAPES.length);
            size = SHAPES[type][4][0];
            color = SHAPES[type][4][1];
            if (size == 4) y = -1;
            for (int i = 0; i < size; i++)
                System.arraycopy(SHAPES[type][i], 0, shape[i], 0, SHAPES[type][i].length);
            createFromShape();
        }

        void createFromShape() {
            for (int x = 0; x < size; x++)
                for (int y = 0; y < size; y++)
                    if (shape[y][x] == 1) figure.add(new Block(x + this.x, y + this.y));

        }

        boolean isTouchGround() {
            for (Block block : figure) if (mine[block.getY() + 1][block.getX()] > 0) return true;
            return false;
        }

        boolean isCrossGround() {
            for (Block block : figure) if (mine[block.getY()][block.getX()] > 0) return true;
            return false;
        }

        void leaveOnTheGround() {
            gameScore++;
            for (Block block : figure) mine[block.getY()][block.getX()] = color;
        }

        boolean isTouchWall(int direction) {
            for (Block block : figure) {
                if (direction == left && (block.getX() == 0 || mine[block.getY()][block.getX() - 1] > 0)) return true;
                if (direction == right && (block.getX() == panelWidth - 6 || mine[block.getY()][block.getX() + 1] > 0))
                    return true;
            }
            return false;
        }

        void move(int direction) {
            if (!isTouchWall(direction)) {
                int dx = direction - 38; // LEFT = -1, RIGHT = 1
                for (Block block : figure) block.setX(block.getX() + dx);
                x += dx;
            }
        }

        void stepDown() {
            for (Block block : figure) block.setY(block.getY() + 1);
            y++;
        }

        void drop() {
            while (!isTouchGround()) stepDown();
        }

        boolean isWrongPosition() {
            for (int x = 0; x < size; x++)
                for (int y = 0; y < size; y++)
                    if (shape[y][x] == 1) {
                        if (y + this.y < 0) return true;
                        if (x + this.x < 0 || x + this.x > panelWidth - 6) return true;
                        if (mine[y + this.y][x + this.x] > 0) return true;
                    }
            return false;
        }

        void rotateShape(int direction) {
            for (int i = 0; i < size / 2; i++)
                for (int j = i; j < size - 1 - i; j++)
                    if (direction == right) { // clockwise
                        int tmp = shape[size - 1 - j][i];
                        shape[size - 1 - j][i] = shape[size - 1 - i][size - 1 - j];
                        shape[size - 1 - i][size - 1 - j] = shape[j][size - 1 - i];
                        shape[j][size - 1 - i] = shape[i][j];
                        shape[i][j] = tmp;
                    } else { // counterclockwise
                        int tmp = shape[i][j];
                        shape[i][j] = shape[j][size - 1 - i];
                        shape[j][size - 1 - i] = shape[size - 1 - i][size - 1 - j];
                        shape[size - 1 - i][size - 1 - j] = shape[size - 1 - j][i];
                        shape[size - 1 - j][i] = tmp;
                    }
        }

        void rotate() {
            rotateShape(right);
            if (!isWrongPosition()) {
                figure.clear();
                createFromShape();
            } else
                rotateShape(left);
        }

        void paint(Graphics g) {
            for (Block block : figure) block.paint(g, color);
        }
    }

    class Block { // building element for Figure
        private int x, y;

        public Block(int x, int y) {
            setX(x);
            setY(y);
        }

        void setX(int x) {
            this.x = x;
        }

        void setY(int y) {
            this.y = y;
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        void paint(Graphics g, int color) {
            g.setColor(new Color(color));
            g.fill3DRect(x * sizeOfBlock + 1, y * sizeOfBlock + 1, sizeOfBlock - 1, sizeOfBlock - 1, true);
            // g.drawRoundRect(x*BLOCK_SIZE+1, y*BLOCK_SIZE+1, BLOCK_SIZE-2, BLOCK_SIZE-2, ARC_RADIUS, ARC_RADIUS);
        }
    }

    class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);

            for (int x = 0; x < panelWidth - 5; x++) {
                for (int y = 0; y < panelHeight; y++) {
                    if (x < panelWidth - 6 && y < panelHeight - 1) {
                        g.setColor(Color.lightGray);
                        g.drawLine((x + 1) * sizeOfBlock - 2, (y + 1) * sizeOfBlock, (x + 1) * sizeOfBlock + 2, (y + 1) * sizeOfBlock);
                        g.drawLine((x + 1) * sizeOfBlock, (y + 1) * sizeOfBlock - 2, (x + 1) * sizeOfBlock, (y + 1) * sizeOfBlock + 2);
                    }
                    if (mine[y][x] > 0) {
                        g.setColor(new Color(mine[y][x]));
                        g.fill3DRect(x * sizeOfBlock + 1, y * sizeOfBlock + 1, sizeOfBlock - 1, sizeOfBlock - 1, true);
                    }
                }
            }
            g.setColor(Color.black);
            g.drawLine(250, 0, 250, 500);
            Font font = new Font("Verdana", Font.BOLD, 20);
            g.setFont(font);
            g.drawString("Score:", 270, 100);
            g.drawString(String.valueOf(gameScore), 290, 120);
            g.drawString("Lines:", 270, 150);
            g.drawString(String.valueOf(lines), 290, 170);
            if (gameOver) {
                Font font1 = new Font("Verdana", Font.BOLD, 60);
                g.setFont(font1);
                g.setColor(Color.black);
                g.drawString("Game", 40, 150);
                g.drawString("Over", 45, 200);


                JFrame frame=new JFrame("Again");
                frame.setBounds(400, 120, 100, 100);
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(480,400);
                frame.setResizable(false);
                JPanel panel=new JPanel();
                frame.add(panel);        try {
                    frame.setContentPane(new JLabel(new ImageIcon(ImageIO.read(new File("src/sample/depositphotos_71176567-stock-illustration-good-job-grunge-retro-red.jpg")))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                frame.pack();
                //frame.setVisible(true);


            } else {
                figure.paint(g);
//                gameScore += 1;
            }
        }

    }


    static class Stopwatch {
        int secondPassed=0;
        Timer timer1 = new Timer();
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                secondPassed++;
            }
        };
        public void start(){
            timer1.scheduleAtFixedRate(task,1000,1000);
        }

    }
}
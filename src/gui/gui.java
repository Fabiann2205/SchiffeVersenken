package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class gui extends JFrame {

    private final int GRID_SIZE = 10;
    private final int[] SHIP_SIZES = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
    private final JButton[][] playerGrid;
    private final JButton[][] computerGrid;
    private final int[][] playerShips;
    private final int[][] computerShips;
    private boolean settingShips = true;
    private int currentShipIndex = 0;
    private boolean horizontalPlacement = true;
    private int playerShipsRemaining = 0;
    private int computerShipsRemaining = 0;

    public gui() {
        setTitle("Schiffe Versenken");
        setSize(850, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        playerGrid = new JButton[GRID_SIZE][GRID_SIZE];
        computerGrid = new JButton[GRID_SIZE][GRID_SIZE];
        playerShips = new int[GRID_SIZE][GRID_SIZE];
        computerShips = new int[GRID_SIZE][GRID_SIZE];

        JPanel playerPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        JPanel computerPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));

        initializeGrid(playerGrid, playerPanel, true);
        initializeGrid(computerGrid, computerPanel, false);

        JPanel gridPanel = new JPanel(new GridLayout(1, 3));
        gridPanel.add(playerPanel);
        gridPanel.add(new SeparatorPanel());
        gridPanel.add(computerPanel);

        add(gridPanel, BorderLayout.CENTER);

        placeComputerShips();

        JPanel controlPanel = new JPanel();
        JToggleButton orientationToggle = new JToggleButton("Horizontal");
        orientationToggle.addItemListener(e -> {
            horizontalPlacement = e.getStateChange() != ItemEvent.SELECTED;
            orientationToggle.setText(horizontalPlacement ? "Horizontal" : "Vertical");
        });
        controlPanel.add(orientationToggle);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void initializeGrid(JButton[][] grid, JPanel panel, boolean isPlayer) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                grid[row][col] = new JButton();
                grid[row][col].setBackground(Color.BLUE);
                grid[row][col].setOpaque(true);
                grid[row][col].setPreferredSize(new Dimension(30, 30));
                int finalRow = row;
                int finalCol = col;
                if (isPlayer) {
                    grid[row][col].addActionListener(e -> placePlayerShip(finalRow, finalCol));
                } else {
                    grid[row][col].addActionListener(e -> playerAttack(finalRow, finalCol));
                }
                panel.add(grid[row][col]);
            }
        }
    }

    private void placePlayerShip(int row, int col) {
        if (settingShips && currentShipIndex < SHIP_SIZES.length) {
            int shipSize = SHIP_SIZES[currentShipIndex];
            if (canPlaceShip(playerShips, row, col, shipSize, horizontalPlacement)) {
                for (int i = 0; i < shipSize; i++) {
                    if (horizontalPlacement) {
                        playerGrid[row][col + i].setBackground(Color.GRAY);
                        playerShips[row][col + i] = currentShipIndex + 1; // Mark the ship index
                    } else {
                        playerGrid[row + i][col].setBackground(Color.GRAY);
                        playerShips[row + i][col] = currentShipIndex + 1; // Mark the ship index
                    }
                }
                playerShipsRemaining += shipSize;
                currentShipIndex++;
                if (currentShipIndex >= SHIP_SIZES.length) {
                    settingShips = false;
                }
            }
        }
    }

    private void placeComputerShips() {
        Random random = new Random();
        for (int shipSize : SHIP_SIZES) {
            boolean placed = false;
            while (!placed) {
                int row = random.nextInt(GRID_SIZE);
                int col = random.nextInt(GRID_SIZE);
                boolean horizontal = random.nextBoolean();
                if (canPlaceShip(computerShips, row, col, shipSize, horizontal)) {
                    for (int i = 0; i < shipSize; i++) {
                        if (horizontal) {
                            computerShips[row][col + i] = currentShipIndex + 1; // Mark the ship index
                        } else {
                            computerShips[row + i][col] = currentShipIndex + 1; // Mark the ship index
                        }
                    }
                    computerShipsRemaining += shipSize;
                    currentShipIndex++;
                    placed = true;
                }
            }
        }
        currentShipIndex = 0; // Reset for player ship placement
    }

    private boolean canPlaceShip(int[][] ships, int row, int col, int shipSize, boolean horizontal) {
        if (horizontal) {
            if (col + shipSize > GRID_SIZE) return false;
            for (int i = 0; i < shipSize; i++) {
                if (ships[row][col + i] != 0) return false;
            }
        } else {
            if (row + shipSize > GRID_SIZE) return false;
            for (int i = 0; i < shipSize; i++) {
                if (ships[row + i][col] != 0) return false;
            }
        }
        return true;
    }

    private void playerAttack(int row, int col) {
        if (computerShips[row][col] > 0) {
            computerGrid[row][col].setBackground(Color.RED);
            computerShips[row][col] = -computerShips[row][col];
            computerShipsRemaining--;
            if (isShipSunk(computerShips, row, col)) {
                markShipAsSunk(computerGrid, computerShips, row, col);
            }
            checkWin();
        } else if (computerShips[row][col] == 0) {
            computerGrid[row][col].setBackground(Color.WHITE);
            computerShips[row][col] = -1;
            computerAttack();
        }
    }

    private void computerAttack() {
        Random random = new Random();
        boolean validAttack = false;
        while (!validAttack) {
            int row = random.nextInt(GRID_SIZE);
            int col = random.nextInt(GRID_SIZE);
            if (playerShips[row][col] > 0) {
                playerGrid[row][col].setBackground(Color.RED);
                playerShips[row][col] = -playerShips[row][col];
                playerShipsRemaining--;
                if (isShipSunk(playerShips, row, col)) {
                    markShipAsSunk(playerGrid, playerShips, row, col);
                }
                validAttack = true;
                checkWin();
            } else if (playerShips[row][col] == 0) {
                playerGrid[row][col].setBackground(Color.WHITE);
                playerShips[row][col] = -1;
                validAttack = true;
            }
        }
    }

    private boolean isShipSunk(int[][] ships, int row, int col) {
        int shipIndex = -ships[row][col];
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (ships[r][c] == shipIndex) {
                    return false;
                }
            }
        }
        return true;
    }

    private void markShipAsSunk(JButton[][] grid, int[][] ships, int row, int col) {
        int shipIndex = -ships[row][col];
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (ships[r][c] == shipIndex*-1) {
                    grid[r][c].setBackground(Color.BLACK);
                }
            }
        }
    }

    private void checkWin() {
        if (playerShipsRemaining == 0) {
            JOptionPane.showMessageDialog(this, "Computer hat gewonnen!");
            System.exit(0);
        } else if (computerShipsRemaining == 0) {
            JOptionPane.showMessageDialog(this, "Spieler hat gewonnen!");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new gui();
    }
}

class SeparatorPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.RED);
        g.fillRect(getWidth() / 2 - 1, 0, 2, getHeight());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(5, 0);
    }
}

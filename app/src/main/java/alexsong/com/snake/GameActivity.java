package alexsong.com.snake;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    public enum DIRECTION { LEFT, RIGHT, UP, DOWN }
    public Context thisContext = this;
    public TableLayout gameTable;
    public SnakeNode snakeHead;
    public Random random = new Random();
    public int foodX = 0;
    public int foodY = 0;
    public List<int[]> goldList = new ArrayList<>();
    public List<int[]> forbiddenList = new ArrayList<>();
    public DIRECTION currDirection;
    private Handler handler = new Handler();
    private Runnable handlerTask;
    private static final int TABLE_WIDTH = 11;
    private static final int TABLE_HEIGHT = 11;
    private static final int SNAKE_START_X = 5;
    private static final int SNAKE_START_Y = 5;
    private static final int GOLD_RANGE = 7;
    private static final int GOLD_BONUS = 3;
    private static final int SNAKE_IMAGE = R.drawable.snake_1;
    private static final int FOOD_IMAGE = R.drawable.food_1;
    private static final int GOLD_IMAGE = R.drawable.gold_1;
    private static final int BACKGROUND_TILE = R.drawable.background_tile_1;


    public TextView scoreView;
    public int score = 0;
    public Map<String, Integer> scoreMap = new HashMap<>();
    private static final String scoreKey = "Score";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        forbiddenList.add(new int[]{SNAKE_START_X, SNAKE_START_Y});
        generateFoodLocation();
        createGameTable();
        scoreMap.put(scoreKey, 0);
        currDirection = DIRECTION.RIGHT;
        startSnake();
        setSwipeListener();
    }

    /**
     * Create the table where the game will be played.
     */
    private void createGameTable() {
        gameTable = (TableLayout) findViewById(R.id.gameTable);
        for(int i = 0; i < TABLE_HEIGHT; i++) {
            TableRow row = new TableRow(this);
            row.setId(i);
            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(tableParams);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(10,10,10,10);

            for(int j = 0; j < TABLE_WIDTH; j++) {
                ImageView cellView = new ImageView(this);
                cellView.setLayoutParams(rowParams);
                cellView.setTag(new TableCell(i, j));
                // Add snake to starting cell
                if(i == SNAKE_START_X && j == SNAKE_START_Y) {
                    cellView.setImageResource(SNAKE_IMAGE);
                    snakeHead = new SnakeNode(cellView);
                }
                else {
                    // Add Food to a cell
                    if (i == foodX && j == foodY) {
                        cellView.setImageResource(FOOD_IMAGE);
                    }
                    // Add 0's to all other cells
                    else {
                        cellView.setImageResource(BACKGROUND_TILE);
                    }
                }
                row.addView(cellView);
            }
            gameTable.addView(row);
        }
    }

    /**
     * Start moving the snake by repeatedly calling moveSnake
     */
    private void startSnake() {
        handlerTask = new Runnable() {
            @Override
            public void run() {
                if(moveSnake()) {
                    handler.postDelayed(handlerTask, 200);
                } else {
                    Toast.makeText(thisContext, "GAME OVER", Toast.LENGTH_SHORT).show();
                }
            }
        };
        handlerTask.run();
    }

    /**
     * Move the snake to the next cell. The direction is indicated by currDirection.
     * It returns whether or not the snake is still in a valid state after moving
     * @return boolean
     */
    public boolean moveSnake() {
        TableCell currCell = (TableCell) snakeHead.getView().getTag();
        ViewGroup row = (ViewGroup) snakeHead.getView().getParent();
        int currX = currCell.getX();
        int currY = currCell.getY();
        int[] xyDirection = setDirection();
        int xdelta = xyDirection[0];
        int ydelta = xyDirection[1];

        currX += xdelta;
        currY += ydelta;
        // return false if the snake head goes out of bounds or hits its body
        if(!inBounds(currX, currY) || hasHitBody(currX, currY)) {
            return false;
        }

        row = (ViewGroup) gameTable.getChildAt(row.getId()+xdelta);
        ImageView newCellView = (ImageView) row.getChildAt(currY);
        newCellView.setImageResource(SNAKE_IMAGE);
        forbiddenList.add(new int[]{currX, currY});

        SnakeNode newSnakeHead = new SnakeNode(newCellView);
        newSnakeHead.setNext(snakeHead);
        SnakeNode prev = newSnakeHead;
        SnakeNode curr = snakeHead;
        while(curr.getNext() != null) {
            prev = curr;
            curr = curr.getNext();
        }

        boolean foundFood = (currX == foodX && currY == foodY);
        // The next cell has the food - generate a new food location and update the score
        if(foundFood) {
            generateFoodLocation();
            updateFoodLocation();
            updateScore();
            if(score > 0 && score%GOLD_RANGE == 0) {
                generateNewGold();
            }
        }
        // No food was found, - remove snake tail and remove its location from forbiddenList
        else {
            curr.getView().setImageResource(BACKGROUND_TILE);
            int tailX = ((TableCell) curr.getView().getTag()).getX();
            int tailY = ((TableCell) curr.getView().getTag()).getY();
            removeFromForbiddenList(tailX, tailY);
            prev.setNext(null);
        }

        // Found gold
        for(int[] gCell : goldList) {
            if(currX == gCell[0] && currY == gCell[1]) {
                goldList.remove(gCell);
                reduceSnakeSize();
                break;
            }
        }

        snakeHead = newSnakeHead;
        return true;
    }

    /**
     * Set the x and y direction that the snake should move, based on currentDirection.
     * @return an array with xdelta at index 0 and ydelta at index 1.
     */
    private int[] setDirection() {
        int ydelta = 0;
        int xdelta = 0;
        switch (currDirection) {
            case LEFT:
                ydelta = -1;
                break;
            case RIGHT:
                ydelta = 1;
                break;
            case UP:
                xdelta = -1;
                break;
            case DOWN:
                xdelta = 1;
                break;
        }
        return new int[] {xdelta, ydelta};
    }

    /**
     * Check if Snake is inside the gameTable
     * @param x x coordinate of the snake
     * @param y y coordinate of the snake
     * @return boolean
     */
    private boolean inBounds(int x, int y) {
        return (x >= 0 && x < TABLE_HEIGHT && y >= 0 && y < TABLE_WIDTH);
    }

    /**
     * Check if Snake has hit its own body
     * @param x x coordinate of the snake
     * @param y y coordinate of the snake
     * @return boolean whether or not the snake head has hit the body
     */
    private boolean hasHitBody(int x, int y) {
        for(int[] cell : forbiddenList) {
            if(cell[0] == x && cell[1] == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the location indicated by the x and y coordinates from the forbidden list
     * @param x x coordinate of cell to remove
     * @param y y coordinate of cell to remove
     */
    private void removeFromForbiddenList(int x, int y) {
        for(int[] cell : forbiddenList) {
            if(cell[0] == x && cell[1] == y) {
                forbiddenList.remove(cell);
                break;
            }
        }
    }

    /**
     * Reduce the size of the snake when it eats a gold
     */
    private void reduceSnakeSize() {
        SnakeNode curr = snakeHead;
        SnakeNode prev = curr;
        for(int i = 0; i < forbiddenList.size()-(1+GOLD_BONUS); i++) {
            prev = curr;
            curr = curr.getNext();
        }
        prev.setNext(null);
        while(curr != null) {
            ImageView view = curr.getView();
            TableCell cell = (TableCell) view.getTag();
            view.setImageResource(R.drawable.background_tile_1);
            removeFromForbiddenList(cell.getX(), cell.getY());
            curr = curr.getNext();
        }
    }

    /**
     * Generate random x and y coordinates for the food.
     */
    private void generateFoodLocation() {
        int lastX = foodX;
        int lastY = foodY;
        while(true) {
            foodX = random.nextInt(TABLE_HEIGHT);
            foodY = random.nextInt(TABLE_WIDTH);
            // check if food location is same as before
            boolean isSame = (foodX == lastX && foodY == lastY);
            // check if food location is same as a gold
            boolean hitGold = false;
            for(int[] gCell : goldList) {
                if(gCell[0] == foodX && gCell[1] == foodY) {
                    hitGold = true;
                    break;
                }
            }
            // check if food location hits the snake
            boolean hitSnake = false;
            for(int[] cell : forbiddenList) {
                if((cell[0] == foodX && cell[1] == foodY)) {
                    hitSnake = true;
                    break;
                }
            }
            // break out of loop if the new position does not conflict with any of the cases
            if(!isSame && !hitGold && !hitSnake) {
                break;
            }
        }
    }

    /**
     * Add the food to a new location on the game table
     */
    private void updateFoodLocation() {
        ViewGroup newFoodRow = (ViewGroup) gameTable.getChildAt(foodX);
        ImageView newFoodCell = (ImageView) newFoodRow.getChildAt(foodY);
        newFoodCell.setImageResource(FOOD_IMAGE);
    }

    /**
     * Generate random x and y coordinates for the gold and add it to the goldList and game map.
     */
    private void generateNewGold() {
        int goldX;
        int goldY;
        while(true) {
            goldX = random.nextInt(TABLE_HEIGHT);
            goldY = random.nextInt(TABLE_WIDTH);
            // check if gold location is same as the food
            boolean hitFood = false;
            if (goldX == foodX && goldY == foodY) {
                hitFood = true;
            }
            // check if gold location is part of the snake's body
            boolean hitSnake = false;
            for (int[] cell : forbiddenList) {
                if (goldX == cell[0] && goldY == cell[1]) {
                    hitSnake = true;
                    break;
                }
            }
            // check if gold location is already in goldList
            boolean alreadyExists = false;
            for (int[] gCell : goldList) {
                if (goldX == gCell[0] && goldY == gCell[1]) {
                    alreadyExists = true;
                    break;
                }
            }
            // break out of loop if the new position does not conflict with any of the cases
            if (!hitFood && !hitSnake && !alreadyExists) {
                break;
            }
        }
        ViewGroup newGoldRow = (ViewGroup) gameTable.getChildAt(goldX);
        ImageView newFoodCell = (ImageView) newGoldRow.getChildAt(goldY);
        newFoodCell.setImageResource(GOLD_IMAGE);
        goldList.add(new int[]{goldX, goldY});
    }

    /**
     * Update the current score
     */
    private void updateScore() {
        score = scoreMap.get(scoreKey)+1;
        scoreMap.put(scoreKey, score);
        scoreView = (TextView) findViewById(R.id.score);
        scoreView.setText(getString(R.string.scoreText, scoreKey, score));
    }

    private void setSwipeListener() {
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeLeft() {
                currDirection = DIRECTION.LEFT;
            }
            public void onSwipeRight() {
                currDirection = DIRECTION.RIGHT;
            }
            public void onSwipeTop() {
                currDirection = DIRECTION.UP;
            }
            public void onSwipeBottom() {
                currDirection = DIRECTION.DOWN;
            }
        });
    }
}


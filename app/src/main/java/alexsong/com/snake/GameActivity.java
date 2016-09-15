package alexsong.com.snake;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
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
    public SnakeNode snakeHead, snakeTail;
    public Random random = new Random();
    public int foodX = 0;
    public int foodY = 0;
    public List<int[]> forbiddenList = new ArrayList<>();
    public DIRECTION currDirection;
    private Handler handler = new Handler();
    private Runnable handlerTask;
    private static final int TABLE_WIDTH = 13;
    private static final int TABLE_HEIGHT = 9;
    private static final int SNAKE_START_X = 4;
    private static final int SNAKE_START_Y = 6;

    public Map<String, Integer> scoreMap = new HashMap<>();
    private static final String scoreKey = "Score";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        forbiddenList.add(new int[]{SNAKE_START_X, SNAKE_START_Y});
        generateFoodLocation(forbiddenList);
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
                TextView cellView = new TextView(this);
                cellView.setLayoutParams(rowParams);
                cellView.setTag(new TableCell(i, j));
                // Add snake to starting cell
                if(i == SNAKE_START_X && j == SNAKE_START_Y) {
                    cellView.setText("X");
                    cellView.setTypeface(null, Typeface.BOLD);
                    snakeHead = new SnakeNode(cellView);
                    snakeTail = snakeHead;
                }
                else {
                    // Add Food to a cell
                    if (i == foodX && j == foodY) {
                        cellView.setText("F");
                        cellView.setTypeface(null, Typeface.BOLD);
                    }
                    // Add 0's to all other cells
                    else {
                        cellView.setText("0");
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
                    handler.postDelayed(handlerTask, 500);
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
        TableCell currCell = (TableCell) snakeHead.getTextView().getTag();
        ViewGroup row = (ViewGroup) snakeHead.getTextView().getParent();
        int currX = currCell.getX();
        int currY = currCell.getY();
        int[] xyArray = setDirection();
        int xdelta = xyArray[0];
        int ydelta = xyArray[1];

        if(!inBounds(currX + xdelta, currY + ydelta)) {
            return false;
        }
        int x = currX + xdelta;
        int y = currY + ydelta;

        if(hasHitBody(currX + xdelta, currY + ydelta)) {
            return false;
        }

        currX += xdelta;
        currY += ydelta;
        row = (ViewGroup) gameTable.getChildAt(row.getId()+xdelta);
        TextView newTextView = (TextView) row.getChildAt(currY);
        newTextView.setText("X");
        newTextView.setTypeface(null, Typeface.BOLD);
        forbiddenList.add(new int[]{currX, currY});

        SnakeNode newSnakeHead = new SnakeNode(newTextView);
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
            generateFoodLocation(forbiddenList);
            updateFoodLocation();
            for(int[] cell : forbiddenList) {
                if(cell[0] == foodX && cell[1] == foodY) {
                    forbiddenList.remove(cell);
                    break;
                }
            }
            updateScore();
        }
        // No food was found, - remove snake tail and remove its location from forbiddenList
        else {
            curr.getTextView().setText("0");
            curr.getTextView().setTypeface(null, Typeface.NORMAL);
            int tailX = ((TableCell) curr.getTextView().getTag()).getX();
            int tailY = ((TableCell) curr.getTextView().getTag()).getY();
            for(int[] cell : forbiddenList) {
                if(cell[0] == tailX && cell[1] == tailY) {
                    forbiddenList.remove(cell);
                    break;
                }
            }
            prev.setNext(null);
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
     * @return
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
     * Generate random x and y coordinates for the food.
     */
    private void generateFoodLocation(List<int[]> forbiddenList) {
        int lastX = foodX;
        int lastY = foodY;
        foodX = random.nextInt(TABLE_HEIGHT);
        foodY = random.nextInt(TABLE_WIDTH);
        for(int[] cell : forbiddenList) {
            // regenerate if new location hits the snake, or is the same as before
            if((cell[0] == foodX && cell[1] == foodY) || (cell[0] == lastX && cell[1] == lastY)) {
                generateFoodLocation(forbiddenList);
            }
        }
    }

    /**
     * Add the food to a new location on the game table
     */
    private void updateFoodLocation() {
        ViewGroup newFoodRow = (ViewGroup) gameTable.getChildAt(foodX);
        TextView newFoodCell = (TextView) newFoodRow.getChildAt(foodY);
        newFoodCell.setText("F");
        newFoodCell.setTypeface(null, Typeface.BOLD);
    }

    /**
     * Update the current score
     */
    private void updateScore() {
        scoreMap.put(scoreKey, scoreMap.get(scoreKey)+1);
        TextView score = (TextView) findViewById(R.id.score);
        score.setText(getString(R.string.scoreText, scoreKey, scoreMap.get(scoreKey)));
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


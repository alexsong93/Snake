package alexsong.com.snake;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    public enum DIRECTION { LEFT, RIGHT, UP, DOWN }
    public Context thisContext = this;
    public TableLayout gameTable;
    public TextView snake;
    public int currX, currY;
    public int foodX, foodY;
    public DIRECTION currDirection;
    private Handler handler = new Handler();
    private Runnable handlerTask;
    private static final int TABLE_WIDTH = 13;
    private static final int TABLE_HEIGHT = 9;
    private static final int SNAKE_START_X = 4;
    private static final int SNAKE_START_Y = 6;

    public Map<String, Integer> scoreMap = new HashMap<String, Integer>();
    private static final String scoreKey = "Score";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        snake = (TextView) findViewById(R.id.snake);
        foodX = SNAKE_START_X;
        foodY = SNAKE_START_Y;
        generateFoodLocation();
        createGameTable();
        scoreMap.put(scoreKey, 0);
        currDirection = DIRECTION.RIGHT;
        startSnake();

        Button leftBtn = (Button) findViewById(R.id.leftBtn);
        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        Button upBtn = (Button) findViewById(R.id.upBtn);
        Button downBtn = (Button) findViewById(R.id.downBtn);

        addListenerOnButton(leftBtn, snake);
        addListenerOnButton(rightBtn, snake);
        addListenerOnButton(upBtn, snake);
        addListenerOnButton(downBtn, snake);
    }

    /**
     * Generate random x and y coordinates for the food.
     */
    private void generateFoodLocation() {
        Random random = new Random();
        int currFoodX = foodX;
        while(foodX == currFoodX) {
            foodX = random.nextInt(TABLE_HEIGHT);
        }
        int currFoodY = foodY;
        while(foodY == currFoodY) {
            foodY = random.nextInt(TABLE_WIDTH);
        }
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
                // Add snake to starting cell
                if(i == SNAKE_START_X && j == SNAKE_START_Y) {
                    ViewGroup parent = (ViewGroup) snake.getParent();
                    parent.removeView(snake);
                    snake.setLayoutParams(rowParams);
                    snake.setTag(new TableCell(i, j));
                    currX = i;
                    currY = j;
                    row.addView(snake);

                }
                else {
                    TextView cell = new TextView(this);
                    cell.setLayoutParams(rowParams);
                    cell.setTag(new TableCell(i, j));
                    // Add Food to a cell
                    if (i == foodX && j == foodY) {
                        cell.setText("F");
                        cell.setTypeface(null, Typeface.BOLD);
                    }
                    // Add 0's to all other cells
                    else {
                        cell.setText("0");
                    }
                    row.addView(cell);
                }
            }
            gameTable.addView(row);
        }
    }

    /**
     * Start the game by start moving the snake.
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
     * Check if Snake is inside the gameTable
     * @param x
     * @param y
     * @return boolean
     */
    private boolean inBounds(int x, int y) {
        return (x >= 0 && x < TABLE_HEIGHT && y >= 0 && y < TABLE_WIDTH);
    }

    /**
     * Move the snake to the next cell. The direction is indicated by currDirection.
     * It returns true if the next cell is within the boundaries and false if it's not.
     * @return boolean
     */
    public boolean moveSnake() {
        TableCell currCell = (TableCell) snake.getTag();
        ViewGroup row = (ViewGroup) snake.getParent();
        currX = currCell.getX();
        currY = currCell.getY();
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
        if(!inBounds(currX + xdelta, currY + ydelta)) {
            return false;
        }
        currX += xdelta;
        currY += ydelta;
        row = (ViewGroup) gameTable.getChildAt(row.getId()+xdelta);
        TextView next = (TextView) row.getChildAt(currY);

        // The Snake has found the food
        if(currX == foodX && currY == foodY) {
            generateFoodLocation();
            ViewGroup newFoodRow = (ViewGroup) gameTable.getChildAt(foodX);
            TextView newFoodCell = (TextView) newFoodRow.getChildAt(foodY);
            newFoodCell.setText("F");
            newFoodCell.setTypeface(null, Typeface.BOLD);
            scoreMap.put(scoreKey, scoreMap.get(scoreKey)+1);

            TextView score = (TextView) findViewById(R.id.score);
            score.setText(getString(R.string.scoreText, scoreKey, scoreMap.get(scoreKey)));
        }
        // Snake moves on to next cell
        next.setText(snake.getText());
        next.setTypeface(null, Typeface.BOLD);
        snake.setText("0");
        snake.setTypeface(null, Typeface.NORMAL);
        snake = next;
        snake.setTag(new TableCell(currX, currY));
        return true;
    }


    protected void addListenerOnButton(Button b, TextView s) {
        final Button btn = b;

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableCell currCell = (TableCell) snake.getTag();
                ViewGroup row = (ViewGroup) snake.getParent();
                currX = currCell.getX();
                currY = currCell.getY();
                if(btn.getId() == R.id.leftBtn) {
                    currDirection = DIRECTION.LEFT;
                } else if(btn.getId() == R.id.rightBtn) {
                    currDirection = DIRECTION.RIGHT;
                } else if(btn.getId() == R.id.upBtn) {
                    currDirection = DIRECTION.UP;
                } else if (btn.getId() == R.id.downBtn) {
                    currDirection = DIRECTION.DOWN;
                }
            }
        });
    }
}


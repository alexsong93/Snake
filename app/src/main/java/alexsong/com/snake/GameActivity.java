package alexsong.com.snake;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jp.wasabeef.blurry.Blurry;

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
    private Runnable gameStartTask;
    private Runnable countdownTask;
    private TextView countdownView;
    private int countdownNum = 4;
    private static final int TABLE_WIDTH = 21;
    private static final int TABLE_HEIGHT = 25;
    private static final int SNAKE_START_X = TABLE_HEIGHT/2;
    private static final int SNAKE_START_Y = TABLE_WIDTH/2;
    private static final int GOLD_RANGE = 7;
    private static final int GOLD_BONUS = 2;

    private static final int FOOD_IMAGE = R.drawable.food_3;
    private static final int GOLD_IMAGE = R.drawable.gold_2;
    private static final int BACKGROUND_TILE = R.drawable.background_tile_2;

    public static int SNAKE_IMAGE = R.drawable.snake_1;
    public static int speed = SettingsActivity.NORMAL;
    public static String speedText = "Normal";
    public TextView scoreView;
    public int score = 0;

    private boolean gameOver = false;
    private boolean snakeStopped = false;
    private boolean startup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Set custom font
        scoreView = (TextView) findViewById(R.id.score);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Fipps-Regular.otf");
        scoreView.setTypeface(font);

        // Set speed, if already selected
        SharedPreferences sharedPref = getSharedPreferences("Settings", MODE_PRIVATE);
        int value = sharedPref.getInt("speedDropdown", -1);
        if(value != -1) {
            if (value == 0) {
                speed = SettingsActivity.SLOW;
                speedText = "Slow";
            } else if (value == 1) {
                speed = SettingsActivity.NORMAL;
                speedText = "Normal";
            } else if (value == 2) {
                speed = SettingsActivity.FAST;
                speedText = "Fast";
            }
        } else {
            speed = SettingsActivity.NORMAL;
        }
        // Set snake color, if already selected
        boolean greenBtnClicked = sharedPref.getBoolean("greenSnake", true);
        boolean pinkBtnClicked = sharedPref.getBoolean("pinkSnake", true);
        if(greenBtnClicked) {
            SNAKE_IMAGE = R.drawable.snake_1;
        } else if(pinkBtnClicked) {
            SNAKE_IMAGE = R.drawable.snake_2;
        }

        forbiddenList.add(new int[]{SNAKE_START_X, SNAKE_START_Y});
        generateFoodLocation();
        createGameTable();
        int index = random.nextInt(4);
        currDirection = DIRECTION.values()[index];
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            ViewGroup viewgroup = (ViewGroup) findViewById(android.R.id.content);
            Blurry.with(this).radius(8).sampling(3).onto(viewgroup);
            countdownView = new TextView(this);
            countdownView.setText(String.valueOf(countdownNum));
            countdownView.setTextSize(80);
            countdownView.setTextColor(Color.WHITE);
            Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Fipps-Regular.otf");
            countdownView.setTypeface(font);
            countdownView.setGravity(Gravity.CENTER);
            viewgroup.addView(countdownView);
            startup = true;
            countDown();
        }
    }

    @Override
    public void onBackPressed() {
        if(startup) {
            finishGame();
        } else if(!gameOver) {
            pauseGame();
        } else {
            finishGame();
        }
    }

    /**
     * Create the table where the game will be played.
     */
    private void createGameTable() {
        gameTable = (TableLayout) findViewById(R.id.gameTable);
        gameTable.setStretchAllColumns(true);
        for(int i = 0; i < TABLE_HEIGHT; i++) {
            TableRow row = new TableRow(this);
            row.setId(i);
            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(tableParams);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            //rowParams.setMargins(10,10,10,10);

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
                    // Add background tile to all other cells
                    else {
                        cellView.setImageResource(BACKGROUND_TILE);
                    }
                }
                cellView.setScaleType(ImageView.ScaleType.FIT_XY);
                row.addView(cellView);
            }
            gameTable.addView(row);
        }
    }

    /**
     * Countdown before starting game
     */
    private void countDown() {
        countdownTask = new Runnable() {
            @Override
            public void run() {
                if(!gameOver) {
                    countdownNum--;
                    if(countdownNum > 0) {
                        countdownView.setText(String.valueOf(countdownNum));
                        handler.postDelayed(countdownTask, 800);
                    } else {
                        countdownView.setVisibility(View.GONE);
                        startup = false;
                        snakeStopped = false;
                        Blurry.delete((ViewGroup) findViewById(android.R.id.content));
                        startSnake();
                        setSwipeListener();
                    }
                }
            }
        };
        countdownTask.run();
    }

    /**
     * Start moving the snake by repeatedly calling moveSnake
     */
    private void startSnake() {
        gameOver = false;
        snakeStopped = false;
        gameStartTask = new Runnable() {
            @Override
            public void run() {
                if(!snakeStopped) {
                    boolean validStatus = moveSnake();
                    if(validStatus) {
                        handler.postDelayed(gameStartTask, speed);
                    } else {
                        gameOver = true;

                        // Get/set high score
                        SharedPreferences prefs = thisContext.getSharedPreferences("highscore", Context.MODE_PRIVATE);
                        int highscore = prefs.getInt(String.valueOf(speed), 0);
                        if(score > highscore) {
                            highscore = score;
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt(String.valueOf(speed), score);
                            editor.apply();
                        }

                        TextView restartTextView = new TextView(thisContext);
                        restartTextView.setText(String.format(getResources().getString(R.string.bestScore), speedText, highscore));
                        restartTextView.setTextSize(20);
                        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Fipps-Regular.otf");
                        restartTextView.setTypeface(font);
                        restartTextView.setGravity(Gravity.CENTER);
                        restartTextView.setTextColor(Color.BLACK);

                        AlertDialog.Builder builder = new AlertDialog.Builder(thisContext, R.style.CustomDialog);
                        builder.setView(restartTextView);
                        builder.setCancelable(false);
                        builder.setNegativeButton(
                                "Restart",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        recreate();
                                        dialog.cancel();
                                    }
                                });

                        builder.setPositiveButton(
                                "Quit",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finishGame();
                                        dialog.cancel();
                                    }
                                });
                        TextView dialogTitle = new TextView(thisContext);
                        dialogTitle.setText(String.format(getResources().getString(R.string.endScore), score));
                        dialogTitle.setTextSize(30);
                        dialogTitle.setTypeface(font);
                        dialogTitle.setGravity(Gravity.CENTER);
                        dialogTitle.setTextColor(Color.BLACK);

                        builder.setCustomTitle(dialogTitle);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        ViewGroup parent = (ViewGroup) dialogTitle.getParent();
                        parent.setPadding(100, 0, 100, 50);
                    }
                }
            }
        };
        gameStartTask.run();
    }

    /**
     * Pause the game
     */
    private void pauseGame() {
        snakeStopped = true;
        ViewGroup viewgroup = (ViewGroup) findViewById(android.R.id.content);
        Blurry.with(this).radius(8).sampling(3).onto(viewgroup);

        TextView pausedTextView = new TextView(this);
        pausedTextView.setText(R.string.quitPrompt);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Fipps-Regular.otf");
        pausedTextView.setTypeface(font);
        pausedTextView.setTextSize(22);
        pausedTextView.setTextColor(Color.BLACK);
        pausedTextView.setGravity(Gravity.CENTER);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setCancelable(false);
        builder.setView(pausedTextView);
        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        gameOver = true;
                        snakeStopped = true;
                        GameActivity.this.finish();
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Blurry.delete((ViewGroup) findViewById(android.R.id.content));
                        gameStartTask.run();
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        ViewGroup parent = (ViewGroup) pausedTextView.getParent();
        parent.setPadding(100, 0, 100, 0);
    }


    /**
     * Finish game and go back to landing page
     */
    public void finishGame() {
        gameOver = true;
        snakeStopped = true;
        countdownNum = 4;
        this.finish();
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
            updateScore(1);
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

        for(int[] gCell : goldList) {
            // Found gold
            if(currX == gCell[0] && currY == gCell[1]) {
                // Vibrate phone if snake hits gold
                Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
                updateScore(2);
                // remove gold from goldList and reduce snake size
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
            view.setImageResource(BACKGROUND_TILE);
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
     * @param i amount to increase score by
     */
    private void updateScore(int i) {
        score += i;
        scoreView.setText(String.valueOf(score));
    }

    /**
     * Set listener to screen for swipe gestures
     */
    private void setSwipeListener() {
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeLeft() {
                int forbiddenListSize = forbiddenList.size();
                if(forbiddenListSize == 1 || (forbiddenListSize > 1 && currDirection != DIRECTION.RIGHT)) {
                    currDirection = DIRECTION.LEFT;
                }
            }
            public void onSwipeRight() {
                int forbiddenListSize = forbiddenList.size();
                if(forbiddenListSize == 1 || (forbiddenListSize > 1 && currDirection != DIRECTION.LEFT)) {
                    currDirection = DIRECTION.RIGHT;
                }
            }
            public void onSwipeTop() {
                int forbiddenListSize = forbiddenList.size();
                if(forbiddenListSize == 1 || (forbiddenListSize > 1 && currDirection != DIRECTION.DOWN)) {
                    currDirection = DIRECTION.UP;
                }
            }
            public void onSwipeBottom() {
                int forbiddenListSize = forbiddenList.size();
                if(forbiddenListSize == 1 || (forbiddenListSize > 1 && currDirection != DIRECTION.UP)) {
                    currDirection = DIRECTION.DOWN;
                }
            }
        });
    }
}


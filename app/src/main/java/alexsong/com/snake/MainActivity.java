package alexsong.com.snake;

import android.content.Context;
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

public class MainActivity extends AppCompatActivity {

    public static class TableCell {
        public int x;
        public int y;

        public TableCell(int row, int col) {
            x = row;
            y = col;
        }
    }

    public enum DIRECTION { LEFT, RIGHT, UP, DOWN }
    public Context thisContext = this;
    public TableLayout gameTable;
    public TextView snake;
    public int currX, currY;
    public DIRECTION currDirection;
    private Handler handler = new Handler();
    Runnable handlerTask;
    private static final int TABLE_WIDTH = 13;
    private static final int TABLE_HEIGHT = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        snake = (TextView) findViewById(R.id.snake);
        createGameTable();
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
                // Add snake to first cell
                if(i == 4 && j == 6) {
                    ViewGroup parent = (ViewGroup) snake.getParent();
                    parent.removeView(snake);
                    snake.setLayoutParams(rowParams);
                    snake.setTag(new TableCell(i, j));
                    currX = i;
                    currY = j;
                    row.addView(snake);
                }
                // Add 0's to all other cells
                else {
                    TextView cell = new TextView(this);
                    cell.setId(j);
                    cell.setLayoutParams(rowParams);
                    cell.setTag(new TableCell(i, j));
                    cell.setText("0");
                    row.addView(cell);
                }
            }
            gameTable.addView(row);
        }
    }

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
    /*
    * Check if Snake is inside the gameTable
    */
    private boolean inBounds(int x, int y) {
        return (x >= 0 && x < TABLE_HEIGHT && y >= 0 && y < TABLE_WIDTH);
    }

    public boolean moveSnake() {
        TableCell currCell = (TableCell) snake.getTag();
        ViewGroup row = (ViewGroup) snake.getParent();
        currX = currCell.x;
        currY = currCell.y;
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
        next.setText(snake.getText());
        snake.setText("0");
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
                currX = currCell.x;
                currY = currCell.y;
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

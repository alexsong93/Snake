package alexsong.com.snake;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public Spinner speedDropdown;
    private static final String[] speeds = {"Slow", "Normal", "Fast"};
    private static final int SLOW = 400;
    private static final int NORMAL = 200;
    private static final int FAST = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        speedDropdown = (Spinner) findViewById(R.id.speedSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.speed_spinner_item, speeds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedDropdown.setAdapter(adapter);
        speedDropdown.setOnItemSelectedListener(this);

        // Select previously selected speed
        SharedPreferences sharedPref = this.getSharedPreferences("Settings", MODE_PRIVATE);
        int value = sharedPref.getInt("speedDropdown", -1);
        if(value != -1) {
            speedDropdown.setSelection(value);
        }

        // Select previously selected snake color
        RadioButton greenBtn = (RadioButton) findViewById(R.id.greenSnakeBtn);
        RadioButton pinkBtn = (RadioButton) findViewById(R.id.pinkSnakeBtn);
        boolean greenBtnClicked = sharedPref.getBoolean("greenSnake", true);
        if(greenBtnClicked) {
            GameActivity.SNAKE_IMAGE = R.drawable.snake_1;
            greenBtn.setButtonDrawable(R.drawable.snake_1_selected);
            pinkBtn.setButtonDrawable(R.drawable.snake_2_btn);
        } else{
            GameActivity.SNAKE_IMAGE = R.drawable.snake_2;
            pinkBtn.setButtonDrawable(R.drawable.snake_2_selected);
            greenBtn.setButtonDrawable(R.drawable.snake_1_btn);
        }
    }

    public void onSnakeColorButtonClicked(View view) {
        RadioButton greenBtn = (RadioButton) findViewById(R.id.greenSnakeBtn);
        RadioButton pinkBtn = (RadioButton) findViewById(R.id.pinkSnakeBtn);
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.greenSnakeBtn:
                if (checked) {
                    GameActivity.SNAKE_IMAGE = R.drawable.snake_1;
                    greenBtn.setButtonDrawable(R.drawable.snake_1_selected);
                    pinkBtn.setButtonDrawable(R.drawable.snake_2_btn);
                    break;
                }
            case R.id.pinkSnakeBtn:
                if (checked) {
                    GameActivity.SNAKE_IMAGE = R.drawable.snake_2;
                    pinkBtn.setButtonDrawable(R.drawable.snake_2_selected);
                    greenBtn.setButtonDrawable(R.drawable.snake_1_btn);
                    break;
                }
        }
        SharedPreferences sharedPref = this.getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putBoolean("greenSnake", greenBtn.isChecked());
        prefEditor.putBoolean("pinkSnake", pinkBtn.isChecked());
        prefEditor.apply();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        switch (position) {
            case 0:
                GameActivity.speed = SLOW;
                break;
            case 1:
                GameActivity.speed = NORMAL;
                break;
            case 2:
                GameActivity.speed = FAST;
                break;
        }
        int choice = speedDropdown.getSelectedItemPosition();
        SharedPreferences sharedPref = this.getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putInt("speedDropdown", choice);
        prefEditor.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        GameActivity.speed = NORMAL;
    }
}

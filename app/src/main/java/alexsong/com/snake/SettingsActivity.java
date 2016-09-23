package alexsong.com.snake;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

        SharedPreferences sharedPref = this.getSharedPreferences("Settings", MODE_PRIVATE);
        int value = sharedPref.getInt("speedDropdown", -1);
        if(value != -1) {
            speedDropdown.setSelection(value);
        }
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

package alexsong.com.snake;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        TextView title = (TextView) findViewById(R.id.title);
        TextView startBtn = (TextView) findViewById(R.id.startBtn);
        TextView settingsBtn = (TextView) findViewById(R.id.settingsBtn);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Fipps-Regular.otf");
        title.setTypeface(font);
        startBtn.setTypeface(font);
        settingsBtn.setTypeface(font);
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}

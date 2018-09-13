package kr.ac.pusan.chalkak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class IntroActivity extends AppCompatActivity {
    public SharedPreferences prefs;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            prefs = getSharedPreferences("Pref", MODE_PRIVATE);

            boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

            if (isFirstRun) {
                Intent intent = new Intent(getApplicationContext(), CardWizardOverlap.class);
                startActivity(intent);
                finish();
                prefs.edit().putBoolean("isFirstRun", false).apply();
            } else {
                Intent intent = new Intent(getApplicationContext(), GridSectioned.class);
                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 4000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}

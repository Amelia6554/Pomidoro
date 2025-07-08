package com.example.pomidoro;

import android.content.pm.PackageManager;
import android.Manifest;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.logic.Timer;
import com.example.logic.TimerListener;


public class MainActivity extends AppCompatActivity {

    EditText study_minutes_et;
    EditText break_minutes_et;
    TextView timer_tv;
    Button start_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
                }
            }



            //// inicjalizacja przycisków

            study_minutes_et = findViewById(R.id.study_minutes_et);
            break_minutes_et = findViewById(R.id.break_minutes_et);
            timer_tv = findViewById(R.id.timer_tv);
            start_btn = findViewById(R.id.start_btn);

            //// timer
            timer = new Timer();


            return insets;
        });
    }


    boolean isStudying = true;
    Timer timer;

    private int minutes;
    private int seconds;

    private void update(int progress) {
        minutes = progress / 60;
        seconds = progress % 60;
        String secondsFinal = "";

        if (seconds <= 9) {
            secondsFinal = "0" + seconds;
        } else {
            secondsFinal = "" + seconds;
        }
        timer_tv.setText("" + minutes + ":" + secondsFinal);
    }


    public void start_timer(View view) {

        String studyInput = study_minutes_et.getText().toString().trim();
        String breakInput = break_minutes_et.getText().toString().trim();


        // Sprawdzenie czy odpowiednie pole nie jest puste
        if ((isStudying && studyInput.isEmpty()) || (!isStudying && breakInput.isEmpty())) {
            Toast.makeText(MainActivity.this, "Wprowadź poprawną liczbę minut", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!timer.isCounterIsActive()) {

            timer.setListener(new TimerListener() {
                @Override
                public void onTick(int secondsLeft) {
                    update(secondsLeft);
//                    textView.setText("Pozostało: " + secondsLeft + " sek.");
                }

                @Override
                public void onFinish() {
                    Toast.makeText(MainActivity.this, "Czas minął!", Toast.LENGTH_SHORT).show();
                }
            });


            study_minutes_et.setEnabled(false);
            start_btn.setText("STOP");

            int time;
            if (isStudying) {
                time = Integer.parseInt(studyInput);
                timer_tv.setTextColor(Color.RED);
            } else {
                time = Integer.parseInt(breakInput);
                timer_tv.setTextColor(Color.GREEN);
            }
            isStudying = !isStudying;

            timer.start_timer(time * 60);
            
        } else {
            reset();
        }
    }

    private void reset() {
        timer_tv.setText("0:00");
        //timer_tv.setTextColor(Color.RED);
        timer.reset();
        start_btn.setText("Start");
        study_minutes_et.setEnabled(true);
    }

    public void launchSettings(View v) {
        ForegroundService.startService(this, 1);
        Toast.makeText(MainActivity.this, "Ustawienia", Toast.LENGTH_SHORT).show();


    }


}

package com.example.pomidoro;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class MainActivity extends AppCompatActivity {

    EditText study_minutes_et;
    EditText break_minutes_et;
    TextView timer_tv;
    Button start_btn;
    boolean isTimerRunning;

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


            // inicjalizacja przycisków
            initViews();
            setupListeners();

            // broadcast
            IntentFilter filter = new IntentFilter();
            filter.addAction("TIMER_TICK");
            filter.addAction("TIMER_FINISHED");
            LocalBroadcastManager.getInstance(this).registerReceiver(timerReceiver, filter);

            return insets;
        });
    }

    private void initViews() {
        study_minutes_et = findViewById(R.id.study_minutes_et);
        break_minutes_et = findViewById(R.id.break_minutes_et);
        timer_tv = findViewById(R.id.timer_tv);
        start_btn = findViewById(R.id.start_btn);

        study_minutes_et.setText("25");
        break_minutes_et.setText("5");
        timer_tv.setText("25:00");
    }

    private void setupListeners() {
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_timer();
            }
        });
    }

    //BOROADCAST
    private BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("TIMER_TICK".equals(action)) {
                int secondsLeft = intent.getIntExtra("seconds_left", 0);
                updateTimerDisplay(secondsLeft);
            } else if ("TIMER_FINISHED".equals(action)) {
                onTimerFinished();
            }
        }
    };




    boolean isStudying = true;

    private void updateTimerDisplay(int secondsLeft) {
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        timer_tv.setText(timeString);
    }

    private void onTimerFinished() {
        isTimerRunning = false;
        start_btn.setEnabled(true);
        study_minutes_et.setEnabled(true);
        break_minutes_et.setEnabled(true);
        start_btn.setText("START");

        // Przywróć domyślny czas
        String defaultMinutes = study_minutes_et.getText().toString();
        if (!defaultMinutes.isEmpty()) {
            try {
                int minutes = Integer.parseInt(defaultMinutes);
                updateTimerDisplay(minutes * 60);
            } catch (NumberFormatException e) {
                timer_tv.setText("25:00");
            }
        } else {
            timer_tv.setText("25:00");
        }
    }


    public void start_timer() {

        String studyInput = study_minutes_et.getText().toString().trim();
        String breakInput = break_minutes_et.getText().toString().trim();

        // Sprawdzenie czy odpowiednie pole nie jest puste
        if ((isStudying && studyInput.isEmpty()) || (!isStudying && breakInput.isEmpty())) {
            Toast.makeText(MainActivity.this, "Wprowadź poprawną liczbę minut", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isTimerRunning) {


            start_btn.setText("STOP");

            int minutes;
            if (isStudying) {
                minutes = Integer.parseInt(studyInput);
                timer_tv.setTextColor(Color.RED);
                study_minutes_et.setEnabled(false);
            } else {
                minutes = Integer.parseInt(breakInput);
                timer_tv.setTextColor(Color.GREEN);
                break_minutes_et.setEnabled(false);
            }
            isStudying = !isStudying;
            ForegroundService.startService(this, minutes);
            // Zaktualizuj interfejs
            isTimerRunning = true;
            //start_btn.setEnabled(false);

            // Ustaw początkowy czas na wyświetlaczu
            updateTimerDisplay(minutes * 60);

            Toast.makeText(this, "Pomodoro rozpoczęte!", Toast.LENGTH_SHORT).show();

        } else {
            stopTimer();
            start_btn.setText("STOP");
        }
    }

    private void stopTimer() {
        ForegroundService.stopService(this);
        onTimerFinished();
        Toast.makeText(this, "Pomodoro zatrzymane", Toast.LENGTH_SHORT).show();
    }

    public void launchSettings(View v) {
        Toast.makeText(MainActivity.this, "Ustawienia", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, SettingsActivity.class);
            //String message = ((EditText)findViewById(R.id.source)).getText().toString();
            i.putExtra("Package", "Ustawienia");
            startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ForegroundService.stopService(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Możesz tutaj sprawdzić czy timer jest aktywny i zaktualizować interfejs
    }


}

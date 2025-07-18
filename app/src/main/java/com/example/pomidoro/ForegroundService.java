package com.example.pomidoro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.logic.Timer;
import com.example.logic.TimerListener;

public class ForegroundService extends Service {
    private static final int ONGOING_NOTIFICATION_ID = 101;
    private static final String CHANNEL_ALERT = "CHANNEL_ALERT";
    private static final String CHANNEL_SILENT = "CHANNEL_SILENT";
    private static final String EXTRA_SECONDS = "EXTRA_SECONDS";

    private NotificationManager notificationManager;
    private Timer timer;
    private LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        broadcastManager = LocalBroadcastManager.getInstance(this);
        createChannels();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int minutes = intent.getIntExtra(EXTRA_SECONDS, 25);
        startForeground(ONGOING_NOTIFICATION_ID, buildSilentNotification("Pomodoro start: " + minutes + " minut"));
        startPomodoroTimer(minutes);
        return START_STICKY;
    }

    private void startPomodoroTimer(int secondsLeft) {
        timer = new Timer();
        timer.setListener(new TimerListener() {
            @Override
            public void onTick(int secondsLeft) {
                // Aktualizuj powiadomienie
                updateNotification("Pozostało: " + formatTime(secondsLeft));

                // Wyślij broadcast do MainActivity
                Intent intent = new Intent("TIMER_TICK");
                intent.putExtra("seconds_left", secondsLeft);
                broadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                // Pokaż powiadomienie o zakończeniu
                notificationManager.notify(314, buildAlertNotification("Pomodoro zakończone!"));

                // Wyślij broadcast o zakończeniu
                Intent intent = new Intent("TIMER_FINISHED");
                broadcastManager.sendBroadcast(intent);

                // Zatrzymaj serwis
                stopSelf();
            }
        });

        updateNotification("Pozostało: " + formatTime(secondsLeft));
        timer.start_timer(secondsLeft);
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private void updateNotification(String contentText) {
        Notification notification = buildSilentNotification(contentText);
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
    }

    private Notification buildSilentNotification(String contentText) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_SILENT)
                .setContentTitle("Pomodoro")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private Notification buildAlertNotification(String contentText) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_ALERT)
                .setContentTitle("Pomodoro")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_baseline_alarm_on_24)
                .setContentIntent(pendingIntent)
                .setOngoing(false)
                .build();
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);

            // Kanał cichy
            NotificationChannel silentChannel = new NotificationChannel(
                    CHANNEL_SILENT,
                    "Ciche powiadomienia",
                    NotificationManager.IMPORTANCE_LOW
            );
            silentChannel.setSound(null, null);
            silentChannel.enableVibration(false);

            // Kanał głośny
            NotificationChannel alertChannel = new NotificationChannel(
                    CHANNEL_ALERT,
                    "Ważne powiadomienia",
                    NotificationManager.IMPORTANCE_HIGH
            );

            nm.createNotificationChannel(silentChannel);
            nm.createNotificationChannel(alertChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.reset(); // Jeśli masz taką metodę w klasie Timer
        }
    }

    // Static methods to start/stop service
    public static void startService(Context context, int secondsLeft) {
        Intent intent = new Intent(context, ForegroundService.class);
        intent.putExtra(EXTRA_SECONDS, secondsLeft);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            context.startService(intent);
        } else {
            context.startForegroundService(intent);
        }
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, ForegroundService.class);
        context.stopService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

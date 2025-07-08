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

import com.example.logic.Timer;
import com.example.logic.TimerListener;

public class ForegroundService extends Service {
    private static final int ONGOING_NOTIFICATION_ID = 101;
    private static final String CHANNEL_ID = "1001";
    private static final String EXTRA_MINUTES = "EXTRA_MINUTES";

    private NotificationManager notificationManager;
    private Timer timer;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createServiceNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int minutes = intent.getIntExtra(EXTRA_MINUTES, 25); // domyślnie 25
        startForeground(ONGOING_NOTIFICATION_ID, buildNotification("Pomodoro start: " + minutes + " minut"));
        startPomodoroTimer(minutes);
        return START_STICKY;
    }

    private void startPomodoroTimer(int minutes) {
        timer = new Timer();
        timer.setListener(new TimerListener() {
            @Override
            public void onTick(int secondsLeft) {
                updateNotification("Pozostało: " + secondsLeft + " sek.");
            }

            @Override
            public void onFinish() {
                updateNotification("Pomodoro zakończone!");
                stopSelf(); // opcjonalnie zatrzymaj serwis po zakończeniu
            }
        });
        timer.start_timer(minutes * 60); // np. 25 minut
    }

    private void updateNotification(String contentText) {
        Notification notification = buildNotification(contentText);
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
    }

    private Notification buildNotification(String contentText) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Pomodoro")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pomodoro channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Static methods to start/stop service
    public static void startService(Context context, int minutes) {
        Intent intent = new Intent(context, ForegroundService.class);
        intent.putExtra(EXTRA_MINUTES, minutes);
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

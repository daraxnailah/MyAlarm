package com.example.myalarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private TimePicker timePicker;
    private Button btnTimer;
    private int jam, menit;

    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = findViewById(R.id.timePicker);
        btnTimer = findViewById(R.id.btnTimer);

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            jam = hourOfDay;
            menit = minute;
        });

        btnTimer.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Set Alarm " + jam + " : " + menit, Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
                } else {
                    setTimer();
                    createNotificationChannel();
                }
            } else {
                setTimer();
                createNotificationChannel();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Reminders";
            String description = "Bangun & Minum Obat";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel("Notify", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setTimer() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Date date = new Date();

        Calendar cal_alarm = Calendar.getInstance();
        Calendar cal_now = Calendar.getInstance();

        cal_now.setTime(date);
        cal_alarm.setTime(date);

        cal_alarm.set(Calendar.HOUR_OF_DAY, jam);
        cal_alarm.set(Calendar.MINUTE, menit);
        cal_alarm.set(Calendar.SECOND, 0);

        if (cal_alarm.before(cal_now)) {
            cal_alarm.add(Calendar.DATE, 1);
        }

        Intent i = new Intent(MainActivity.this, MyBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pendingIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setTimer();
                createNotificationChannel();
            } else {
                Toast.makeText(this, "Permission ditolak, notifikasi tidak akan muncul", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

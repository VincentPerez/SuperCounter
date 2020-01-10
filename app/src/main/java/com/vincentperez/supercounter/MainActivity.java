package com.vincentperez.supercounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {


    private static final String DECREASE_ACTION = "DECREASE_ACTION";
    private static final String KEY_NOTIFICATION_ID = "KEY_NOTIFICATION_ID";
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static final String CURRENT_COUNTER = "CURRENT_COUNTER";
    public static final String ALARM_TRIGGER = "ALARM_TRIGGER";
    private static final int COUNTER_MAX = 100;

    public static final int DECREMENT = 5;
    public static int notificationId = 1500;
    public static DbManager dbManager;

    private TextView counter;
    private Button reset, decrease;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbManager = new DbManager(this);
        dbManager.open();

        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(alarmIntent());
        startAlert();

        setContentView(R.layout.activity_main);
        counter = findViewById(R.id.counter);
        reset = findViewById(R.id.reset_button);
        decrease = findViewById(R.id.decrease_button);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCurrentCounter();
                refreshUI();
            }
        });

        decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseCurrentCounter();
                refreshUI();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
        createNotificationChannel();
        showNotification(getCurrentCounter());
    }

    public void showNotification(int counter) {
        CharSequence decreaseLabel = "Décrémenter";

        NotificationCompat.Action decreaseAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_launcher_foreground, decreaseLabel, decreasePendingIntent())
                .build();

        if (counter > 0) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("N'oubliez pas votre compteur !")
                    .setContentText(String.valueOf(counter))
                    .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND)
                    .setVibrate(new long[]{0L})
                    .setShowWhen(true)
                    .setOngoing(true)
                    .addAction(decreaseAction);

            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
            mNotificationManager.notify(notificationId, mBuilder.build());
        }

        if (counter == 0) {
            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
            mNotificationManager.cancel(notificationId);
        }
    }

    private PendingIntent decreasePendingIntent() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(this, MyBroadcastReceiver.class);
            intent.setAction(DECREASE_ACTION);
            intent.putExtra(KEY_NOTIFICATION_ID, notificationId);
            return PendingIntent.getBroadcast(getApplicationContext(), 100, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            // start your activity for Android M and below
            intent = new Intent(this, MainActivity.class);
            intent.setAction(DECREASE_ACTION);
            intent.putExtra(KEY_NOTIFICATION_ID, notificationId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return PendingIntent.getActivity(this, 100, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private PendingIntent alarmIntent() {
        Intent intent = new Intent(this, MyBroadcastReceiver.class);
        intent.setAction(ALARM_TRIGGER);
        return PendingIntent.getBroadcast(this.getApplicationContext(), 234324243, intent, 0);
    }

    private void createNotificationChannel() {
        // Créer le NotificationChannel, seulement pour API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification channel name";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setVibrationPattern(new long[]{ 0 });
            channel.enableVibration(true);
            channel.setDescription("Notification channel description");
            // Enregister le canal sur le système : attention de ne plus rien modifier après
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }

    public void startAlert() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8); // For 1 PM or 2 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),  AlarmManager.INTERVAL_DAY, alarmIntent());
    }

    private void refreshUI() {
        int count = getCurrentCounter();
        counter.setText(String.valueOf(count));
        showNotification(count);
    }

    public static String formatDateSimpleDay(Date timeToFormat) {

        SimpleDateFormat simpleDayFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());

        return simpleDayFormat.format(timeToFormat);
    }

    public static boolean checkIfDayExists(Date date) {
        String day = formatDateSimpleDay(date);
        Cursor cursor = dbManager.fetchDate(day);
        if (cursor.getCount() != 0) {
            return true;
        } else {
            return dbManager.insert(COUNTER_MAX, day);
        }
    }

    public static int getCounter(Date date) {
        if (checkIfDayExists(date)){
            Cursor cursor = dbManager.fetchDate(formatDateSimpleDay(date));
            return cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COUNTER));
        } else {
            return 0;
        }
    }

    public static int getCurrentCounter() {
        return getCounter(new Date());
    }

    public static void updateCounter(Date date, int counter) {
        if (checkIfDayExists(date)){
            dbManager.updateOnDate(counter, formatDateSimpleDay(date));
        }
    }

    public static void updateCurrentCounter(int counter) {
        if (checkIfDayExists(new Date())){
            dbManager.updateOnDate(counter, formatDateSimpleDay(new Date()));
        }
    }

    public static void resetCurrentCounter() {
        resetCounter(new Date());
    }

    public static void resetCounter(Date date) {
        if (checkIfDayExists(new Date())){
            dbManager.updateOnDate(COUNTER_MAX, formatDateSimpleDay(date));
        }
    }

    public static void decreaseCounter(Date date) {
        if (checkIfDayExists(new Date())){
            int current = getCounter(date);
            if (current > 0 && current - DECREMENT <= 0) {
                dbManager.updateOnDate(0, formatDateSimpleDay(date));
            } else if (current > 0) {
                dbManager.updateOnDate(current - DECREMENT, formatDateSimpleDay(date));
            }
        }
    }

    public static void decreaseCurrentCounter() {
        decreaseCounter(new Date());
    }
}

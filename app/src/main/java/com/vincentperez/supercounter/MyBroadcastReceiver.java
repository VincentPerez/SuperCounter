package com.vincentperez.supercounter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String KEY_NOTIFICATION_ID = "KEY_NOTIFICATION_ID";
    private static final String KEY_REPLY = "KEY_REPLY";
    private static final String REPLY_ACTION = "REPLY_ACTION";
    private static final String DECREASE_ACTION = "DECREASE_ACTION";
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static final String CURRENT_COUNTER = "CURRENT_COUNTER";

    private int notificationId = MainActivity.notificationId;
    private int counter;
    private DbManager dbManager;

    public MyBroadcastReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {

        dbManager = new DbManager(context);
        dbManager.open();

        counter = MainActivity.getCurrentCounter();

        CharSequence decreaseLabel = "Décrémenter";

        NotificationCompat.Action decreaseAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_launcher_foreground, decreaseLabel, decreasePendingIntent(context))
                .build();

        if (DECREASE_ACTION.equals(intent.getAction())){
            notificationId = intent.getIntExtra(KEY_NOTIFICATION_ID, 0);

            counter = MainActivity.getCurrentCounter();

            if ((counter > 0) && ((counter - MainActivity.DECREMENT) > 0)) {
                counter = counter - MainActivity.DECREMENT;
                MainActivity.updateCurrentCounter(counter);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("N'oubliez pas votre compteur !")
                        .addAction(decreaseAction)
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND)
                        .setVibrate(new long[]{0L})
                        .setOngoing(true)
                        .setContentText(String.valueOf(counter));

                notificationManager.notify(MainActivity.notificationId, builder.build());
            } else if (counter > 0){
                counter = 0;
                MainActivity.updateCurrentCounter(counter);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Objectif atteint. Bravo !")
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND)
                        .setVibrate(new long[]{0L});

                notificationManager.notify(MainActivity.notificationId, builder.build());
            }
        }

        if (MainActivity.ALARM_TRIGGER.equals(intent.getAction())){
            notificationId = MainActivity.notificationId;

            counter = MainActivity.getCurrentCounter();

            if (counter > 0) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("N'oubliez pas votre compteur !")
                        .addAction(decreaseAction)
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND)
                        .setVibrate(new long[]{0L})
                        .setOngoing(true)
                        .setContentText(String.valueOf(counter));

                notificationManager.notify(MainActivity.notificationId, builder.build());
            }
        }

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
            alarmManager.cancel(alarmIntent(context));
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 8); // For 1 PM or 2 PM
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),  AlarmManager.INTERVAL_DAY, alarmIntent(context));
        }

    }

    private PendingIntent decreasePendingIntent(Context context) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(context, MyBroadcastReceiver.class);
            intent.setAction(DECREASE_ACTION);
            intent.putExtra(KEY_NOTIFICATION_ID, notificationId);
            intent.putExtra(CURRENT_COUNTER, counter);
            return PendingIntent.getBroadcast(context, 100, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            // start your activity for Android M and below
            intent = new Intent(context, MainActivity.class);
            intent.setAction(DECREASE_ACTION);
            intent.putExtra(KEY_NOTIFICATION_ID, notificationId);
            intent.putExtra(CURRENT_COUNTER, counter);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return PendingIntent.getActivity(context, 100, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private PendingIntent alarmIntent(Context context) {
        Intent intent = new Intent(context, MyBroadcastReceiver.class);
        intent.setAction(MainActivity.ALARM_TRIGGER);
        return PendingIntent.getBroadcast(context, 234324243, intent, 0);
    }


}
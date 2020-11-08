package com.nbut.memorandum;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import java.util.Calendar;

public class AlarmService extends Service {
    private int alarmId;
    private String alarm;
    int BIG_NUM_FOR_ALARM = 100;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("不支持");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("AlarmService","executed at"+new Date().toString());
//            }
//        });

        //安排闹钟广播
        alarmId = intent.getIntExtra("alarmId", 0);
        alarm = intent.getStringExtra("alarm");
        int alarm_hour = 0;
        int alarm_minute = 0;
        int alarm_year = 0;
        int alarm_month = 0;
        int alarm_day = 0;
        int i = 0, k = 0;
        while (i < alarm.length() && alarm.charAt(i) != '-') i++;
        alarm_year = Integer.parseInt(alarm.substring(k, i));
        k = i + 1;
        i++;
        while (i < alarm.length() && alarm.charAt(i) != '-') i++;
        alarm_month = Integer.parseInt(alarm.substring(k, i));
        k = i + 1;
        i++;
        while (i < alarm.length() && alarm.charAt(i) != ' ') i++;
        alarm_day = Integer.parseInt(alarm.substring(k, i));
        k = i + 1;
        i++;
        while (i < alarm.length() && alarm.charAt(i) != ':') i++;
        alarm_hour = Integer.parseInt(alarm.substring(k, i));
        k = i + 1;
        i++;
        alarm_minute = Integer.parseInt(alarm.substring(k));

        // 设置闹钟具体时间
        Calendar alarm_time = Calendar.getInstance();
        alarm_time.set(alarm_year, alarm_month - 1, alarm_day, alarm_hour, alarm_minute);

        Intent it = new Intent(this, AlarmReceiver.class);
        it.putExtra("alarmId", alarmId + BIG_NUM_FOR_ALARM);
        PendingIntent sender = PendingIntent.getBroadcast(this, alarmId + BIG_NUM_FOR_ALARM, it, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, alarm_time.getTimeInMillis(), sender);

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

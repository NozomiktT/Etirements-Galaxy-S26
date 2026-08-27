package com.cedric.etirements;

import android.app.*;
import android.content.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.*;

public class PhaseReceiver extends BroadcastReceiver {
    static final int REQ = 48291;

    static PendingIntent pi(Context c) {
        Intent i = new Intent(c, PhaseReceiver.class);
        return PendingIntent.getBroadcast(c, REQ, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void schedule(Context c, long at) {
        AlarmManager a = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent p = pi(c);
        if (a == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (a.canScheduleExactAlarms()) {
                a.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, p);
            } else {
                a.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, p);
            }
        } else {
            a.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, p);
        }
    }

    static void cancel(Context c) {
        AlarmManager a = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (a != null) a.cancel(pi(c));
    }

    @Override public void onReceive(Context c, Intent i) {
        SharedPreferences s = c.getSharedPreferences("state", 0);
        if (!s.getBoolean("running", false)) return;

        long now = SystemClock.elapsedRealtime();
        long scheduledEnd = s.getLong("end", now);
        long delay = Math.max(0, now - scheduledEnd);

        boolean workPhase = s.getBoolean("workPhase", true);
        boolean nextWork = !workPhase;
        int cycle = s.getInt("cycle", 0);
        if (nextWork) cycle++;

        int totalSeconds = nextWork ? StretchWidget.work(c) : StretchWidget.rest(c);
        
        long durationMs = Math.max(1000L, (totalSeconds * 1000L) - delay);
        long end = now + durationMs;

        s.edit().putBoolean("workPhase", nextWork)
               .putInt("cycle", cycle)
               .putInt("remaining", (int) (durationMs / 1000L))
               .putLong("end", end)
               .apply();

        // 1. Signal Vibreur
        Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(200);
            }
        }

        // 2. Signal Sonore (Bip court système)
        try {
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150); // Bip sonore de 150ms
        } catch (Exception e) {
            e.printStackTrace();
        }

        schedule(c, end);
        StretchWidget.render(c);
    }
}

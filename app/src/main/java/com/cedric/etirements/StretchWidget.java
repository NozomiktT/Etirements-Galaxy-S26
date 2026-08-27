package com.cedric.etirements;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.widget.RemoteViews;

public class StretchWidget extends AppWidgetProvider {
    static final String START="com.cedric.etirements.START";
    static final String RESET="com.cedric.etirements.RESET";
    static final String PREF="state";

    static PendingIntent pending(Context c, String action) {
        Intent i = new Intent(c, StretchWidget.class).setAction(action);
        return PendingIntent.getBroadcast(c, action.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static SharedPreferences p(Context c) { return c.getSharedPreferences(PREF, 0); }
    static int work(Context c) { return c.getSharedPreferences("settings", 0).getInt("work", 30); }
    static int rest(Context c) { return c.getSharedPreferences("settings", 0).getInt("rest", 5); }

    static void render(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, StretchWidget.class));
        for (int id : ids) render(c, m, id);
    }

    static void render(Context c, AppWidgetManager m, int id) {
        SharedPreferences s = p(c);
        boolean running = s.getBoolean("running", false);
        boolean phase = s.getBoolean("workPhase", true);
        int cycle = s.getInt("cycle", 0);
        int remaining = s.getInt("remaining", work(c));
        long end = s.getLong("end", 0);

        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_layout);
        v.setTextViewText(R.id.phase, running ? (phase ? "ÉTIREMENT" : "REPOS") : (cycle == 0 ? "PRÊT ?" : (phase ? "ÉTIREMENT" : "REPOS")));
        v.setTextViewText(R.id.counter, "Cycle " + cycle);

        if (running) {
            v.setChronometerCountDown(R.id.timer, true);
            // Si le temps est dépassé, on bloque l'affichage à zéro au lieu de laisser décompter en négatif
            long now = SystemClock.elapsedRealtime();
            long base = Math.max(now, end);
            v.setChronometer(R.id.timer, base, null, true);
            v.setTextViewText(R.id.action, "⏸ Pause");
        } else {
            v.setChronometerCountDown(R.id.timer, false);
            v.setChronometer(R.id.timer, SystemClock.elapsedRealtime() + remaining * 1000L, null, false);
            v.setTextViewText(R.id.action, cycle == 0 ? "▶ Lancer" : "▶ Reprendre");
        }

        v.setOnClickPendingIntent(R.id.action, pending(c, START));
        v.setOnClickPendingIntent(R.id.reset, pending(c, RESET));
        m.updateAppWidget(id, v);
    }

    public static void reset(Context c) {
        PhaseReceiver.cancel(c);
        p(c).edit().clear().putBoolean("workPhase", true).putInt("cycle", 0).putInt("remaining", work(c)).apply();
        render(c);
    }

    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) { for (int id : ids) render(c, m, id); }

    @Override public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        if (START.equals(i.getAction())) {
            SharedPreferences s = p(c);
            boolean running = s.getBoolean("running", false);
            if (running) {
                long left = Math.max(0, s.getLong("end", 0) - SystemClock.elapsedRealtime());
                int sec = (int) Math.ceil(left / 1000.0);
                PhaseReceiver.cancel(c);
                s.edit().putBoolean("running", false).putInt("remaining", Math.max(1, sec)).apply();
            } else {
                int sec = s.getInt("remaining", work(c));
                if (sec <= 0) sec = work(c);
                long end = SystemClock.elapsedRealtime() + sec * 1000L;
                if (s.getInt("cycle", 0) == 0) s.edit().putInt("cycle", 1).apply();
                s.edit().putBoolean("running", true).putLong("end", end).apply();
                PhaseReceiver.schedule(c, end);
            }
            render(c);
        } else if (RESET.equals(i.getAction())) {
            reset(c);
        }
    }
}

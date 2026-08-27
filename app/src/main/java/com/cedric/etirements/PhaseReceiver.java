package com.cedric.etirements;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.Toast;

public class PhaseReceiver extends BroadcastReceiver {
  static final int REQ=48291;
  static PendingIntent pi(Context c){
    Intent i=new Intent(c,PhaseReceiver.class);
    return PendingIntent.getBroadcast(c,REQ,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  }
  static void schedule(Context c,long at){
    AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    PendingIntent p=pi(c);
    try { a.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,at,p); }
    catch(SecurityException e){ a.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,at,p); }
  }
  static void cancel(Context c){
    AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    a.cancel(pi(c));
  }
  @Override public void onReceive(Context c,Intent i){
    android.content.SharedPreferences s=c.getSharedPreferences("state",0);
    if(!s.getBoolean("running",false)) return;
    boolean workPhase=s.getBoolean("workPhase",true);
    boolean nextWork=!workPhase;
    int cycle=s.getInt("cycle",0);
    if(nextWork) cycle++;
    int seconds=nextWork?StretchWidget.work(c):StretchWidget.rest(c);
    long end=SystemClock.elapsedRealtime()+seconds*1000L;
    s.edit().putBoolean("workPhase",nextWork).putInt("cycle",cycle)
      .putInt("remaining",seconds).putLong("end",end).apply();
    ((android.os.Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(
      android.os.VibrationEffect.createOneShot(180,android.os.VibrationEffect.DEFAULT_AMPLITUDE));
    schedule(c,end);
    StretchWidget.render(c);
  }
}
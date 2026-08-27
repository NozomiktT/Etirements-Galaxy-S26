package com.cedric.etirements;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Intent;
import android.provider.Settings;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
  EditText work, rest;
  @Override public void onCreate(Bundle b) {
    super.onCreate(b);
    setContentView(R.layout.activity_main);
    work=findViewById(R.id.work); rest=findViewById(R.id.rest);
    SharedPreferences p=getSharedPreferences("settings",0);
    work.setText(String.valueOf(p.getInt("work",30)));
    rest.setText(String.valueOf(p.getInt("rest",5)));
    findViewById(R.id.save).setOnClickListener(v -> {
      int w=parse(work.getText().toString(),30), r=parse(rest.getText().toString(),5);
      p.edit().putInt("work",Math.max(1,w)).putInt("rest",Math.max(1,r)).apply();
      StretchWidget.reset(this);
      Toast.makeText(this,"Réglages enregistrés",Toast.LENGTH_SHORT).show();
    });
  }
  int parse(String s,int d){ try{return Integer.parseInt(s);}catch(Exception e){return d;} }
}
package com.typicalgeek.madeni;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Splash extends AppCompatActivity {
    Intent i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("darkTheme", false)){
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final SharedPreferences pref = getSharedPreferences("preferences", 0);
        final boolean bSetup = pref.getBoolean("firstTimeSetup", true);
        String strPasscode = SP.getString("passcode", "1234").trim();
        final String finalStrPasscode = strPasscode.trim();
        boolean bAppLock = SP.getBoolean("appLock",false);
        if (finalStrPasscode.isEmpty()){
            Toast.makeText(this, "There is no passcode set. App lock will be skipped.", Toast.LENGTH_SHORT).show();
            bAppLock = false;
        }
        final boolean finalBoolAppLock = bAppLock;
        final int FADE_DELAY = 500;
        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(FADE_DELAY);
        final Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(FADE_DELAY);
        final ProgressBar pbLoader = findViewById(R.id.pbLoader);
        final ImageView ivSplash = findViewById(R.id.ivSplash);
        ivSplash.startAnimation(in);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ivSplash.startAnimation(out);
            }
        }, FADE_DELAY*3);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ivSplash.setVisibility(View.INVISIBLE);
                pbLoader.setVisibility(View.VISIBLE);
                pbLoader.startAnimation(in);
            }
        }, FADE_DELAY*4);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pbLoader.startAnimation(out);
            }
        }, FADE_DELAY*7);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    pbLoader.setVisibility(View.GONE);
                    pref.edit().putBoolean("firstTimeSetup", false).apply();
                    i = new Intent(Splash.this, bSetup ? Setup.class : finalBoolAppLock ? PasscodeActivity.class : MainActivity.class);
                    Splash.this.startActivity(i);
                    Splash.this.finish();
            }
        }, FADE_DELAY*8);
    }
}
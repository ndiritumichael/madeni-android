package com.typicalgeek.madeni;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.typicalgeek.madeni.views.RecoveryActivity;

public class PasscodeActivity extends AppCompatActivity {
    Button btnHint, btnForgot;
    CardView cvForgot;
    EditText etPass;
    TextView tvHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("darkTheme", false)){
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
        btnHint = findViewById(R.id.btnHint);
        btnForgot = findViewById(R.id.btnForgot);
        cvForgot = findViewById(R.id.card_forgot);
        etPass = findViewById(R.id.etPasscode);
        tvHint = findViewById(R.id.tvHint);
        FloatingActionButton fab = findViewById(R.id.fab);
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final String strPasscode = SP.getString("passcode", "1234").trim();
        String strDefHint = "No hint set. You're on your own, buddy.";
        String strHint = SP.getString("passcodeHint", strDefHint).trim();
        strHint = "HINT: " + strHint;
        final String finalStrHint = strHint;
        String strSecurityQuestion = SP.getString("securityQ", "").trim();
        final String finalStrSecurityQuestion = strSecurityQuestion.trim();
        String strSecurityAnswer = SP.getString("securityA", "").trim();
        final String finalStrSecurityAnswer = strSecurityAnswer.trim();
        Toast.makeText(this, "App is locked.", Toast.LENGTH_SHORT).show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strPass = etPass.getText().toString().trim();
                etPass.setText("");

                if (strPass.isEmpty()){
                    Toast.makeText(PasscodeActivity.this, "Please provide a passcode", Toast.LENGTH_SHORT).show();
                } else if (strPass.equals(strPasscode)){
                    Toast.makeText(PasscodeActivity.this, "Access Granted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PasscodeActivity.this,MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(PasscodeActivity.this, "Access Denied! Passcode is incorrect.", Toast.LENGTH_SHORT).show();
                    cvForgot.setVisibility(View.VISIBLE);
                }

            }
        });
        tvHint.setText(finalStrHint);
        btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (finalStrSecurityQuestion.equals("")){
                    Toast.makeText(PasscodeActivity.this, "No security question set.", Toast.LENGTH_SHORT).show();
                }else if (finalStrSecurityAnswer.equals("")) {
                    Toast.makeText(PasscodeActivity.this, "Security question has no answer.", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(PasscodeActivity.this, RecoveryActivity.class));
                    finish();
                }
            }
        });
        btnHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cvForgot.setVisibility(View.VISIBLE);
                btnHint.setVisibility(View.GONE);
            }
        });
    }
}
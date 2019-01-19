package com.typicalgeek.madeni;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RecoveryActivity extends AppCompatActivity {
    EditText etRecovery, etNewPass;
    CardView cvRecovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("darkTheme", false)){
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);
        etNewPass = findViewById(R.id.etNewPass);
        etRecovery = findViewById(R.id.etRecovery);
        cvRecovery = findViewById(R.id.card_recovery);
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String strSecurityQuestion = SP.getString("securityQ", "").trim();
        if(!strSecurityQuestion.endsWith("?")) strSecurityQuestion+="?";
        final String finalStrSecurityQuestion = strSecurityQuestion.trim();
        String strSecurityAnswer = SP.getString("securityA", "").trim();
        final String finalStrSecurityAnswer = strSecurityAnswer.toLowerCase().trim();
        etRecovery.setHint(finalStrSecurityQuestion);
        final FloatingActionButton fab = findViewById(R.id.fab);
        etNewPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length()>0) fab.setImageDrawable(getDrawable(R.drawable.ic_done));
                else fab.setImageDrawable(getDrawable(R.drawable.ic_back));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (cvRecovery.getVisibility() != View.VISIBLE) {
                        String strVerify = etRecovery.getText().toString().toLowerCase().trim();
                        if (strVerify.equals(finalStrSecurityAnswer)) {
                            fab.setImageDrawable(getDrawable(R.drawable.ic_back));
                            Toast.makeText(RecoveryActivity.this, "Success! Create a new passcode.", Toast.LENGTH_SHORT).show();
                            cvRecovery.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(RecoveryActivity.this, "Input does not match stored answer.", Toast.LENGTH_SHORT).show();
                        }
                    } else{
                        if (etNewPass.getText().toString().trim().length()>0){
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RecoveryActivity.this);
                            SharedPreferences.Editor buddy = preferences.edit();
                            buddy.putString("passcode", etNewPass.getText().toString().trim());
                            if (buddy.commit()) {
                                Toast.makeText(RecoveryActivity.this, "Passcode changed successfully", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            } else
                                Toast.makeText(RecoveryActivity.this, "Error while changing passcode", Toast.LENGTH_SHORT).show();
                        } else onBackPressed();
                    }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RecoveryActivity.this, PasscodeActivity.class));
        finish();
    }
}
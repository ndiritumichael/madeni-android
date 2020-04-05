package com.typicalgeek.madeni;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class Setup extends AppCompatActivity {
    FloatingActionButton fab;
    private TextView tvTitle, tvStage;
    private Switch appLock;
    private EditText etUsername, etPasscode, etPasscodehint, etSecQ, etSecA;
    private LinearLayout stageZero, stageOne, stageTwo, stageThree;
    private String username;
    Button btnGrant;
    private final static int pages = 3;
    int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        tvTitle = findViewById(R.id.tvStageTitle);
        tvStage = findViewById(R.id.tvStage);
        appLock = findViewById(R.id.swApplock);
        etUsername = findViewById(R.id.etUsername);
        etPasscode = findViewById(R.id.etUsercode);
        etPasscodehint = findViewById(R.id.etUserhint);
        etSecQ = findViewById(R.id.etUsersecQ);
        etSecA = findViewById(R.id.etUsersecA);
        stageZero = findViewById(R.id.stageZero);
        stageOne = findViewById(R.id.stageOne);
        stageTwo = findViewById(R.id.stageTwo);
        stageThree = findViewById(R.id.stageThree);
        btnGrant = findViewById(R.id.btnPermissions);
        fab = findViewById(R.id.fab);
        pageSetup();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parsePage(++page);
            }
        });
        appLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableSecurity(isChecked);
            }
        });
        btnGrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOkay();
            }
        });
    }

    private void getOkay() {
        final int PERMISSIONS_REQUEST = 100;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST);
    }

    private void pageSetup() {
        // TODO: 27/08/2018 stageZero
        parsePage(page);
    }

    private void enableSecurity(boolean isChecked) {
        etPasscode.setEnabled(isChecked);
        etPasscode.setFocusable(isChecked);
        etPasscode.setFocusableInTouchMode(isChecked);
        etPasscodehint.setEnabled(isChecked);
        etPasscodehint.setFocusable(isChecked);
        etPasscodehint.setFocusableInTouchMode(isChecked);
        etSecQ.setEnabled(isChecked);
        etSecQ.setFocusable(isChecked);
        etSecQ.setFocusableInTouchMode(isChecked);
        etSecA.setEnabled(isChecked);
        etSecA.setFocusable(isChecked);
        etSecA.setFocusableInTouchMode(isChecked);
    }

    private void parsePage(int p) {
        switch (p){
            case -1:
                ++page;
                tvStage.setVisibility(View.GONE);
                new AlertDialog.Builder(this)
                        .setTitle("Skip?")
                        .setMessage("Would you like to skip this setup wizard?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Setup.this, MainActivity.class));
                                Setup.this.finish();
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        parsePage(page);
                    }
                }).create().show();

            case 0:
                stageZero.setVisibility(View.VISIBLE);
                stageOne.setVisibility(View.GONE);
                stageTwo.setVisibility(View.GONE);
                stageThree.setVisibility(View.GONE);
                tvStage.setVisibility(View.GONE);
                tvTitle.setText(R.string.hello);
                fab.setImageDrawable(getDrawable(R.drawable.ic_next));
                break;
            case 1:
                stageZero.setVisibility(View.GONE);
                stageOne.setVisibility(View.VISIBLE);
                stageTwo.setVisibility(View.GONE);
                stageThree.setVisibility(View.GONE);
                tvStage.setVisibility(View.VISIBLE);
                tvTitle.setText(R.string.protect_the_app);
                fab.setImageDrawable(getDrawable(R.drawable.ic_next));
                break;
            case 2:
                stageZero.setVisibility(View.GONE);
                stageOne.setVisibility(View.GONE);
                stageTwo.setVisibility(View.VISIBLE);
                stageThree.setVisibility(View.GONE);
                tvStage.setVisibility(View.VISIBLE);
                tvTitle.setText("Restore backups");
                fab.setImageDrawable(getDrawable(R.drawable.ic_next));
                break;
            case 3:
                stageZero.setVisibility(View.GONE);
                stageOne.setVisibility(View.GONE);
                stageTwo.setVisibility(View.GONE);
                stageThree.setVisibility(View.VISIBLE);
                tvStage.setVisibility(View.VISIBLE);
                tvTitle.setText(R.string.one_more_thing);
                fab.setImageDrawable(getDrawable(R.drawable.ic_done));
                break;
            case 4:
                --page;
                if (putPreferences()) {
                    startActivity(new Intent(Setup.this, MainActivity.class));
                    Toast.makeText(this, "Welcome to peace of mind, " + username, Toast.LENGTH_SHORT).show();
                    Setup.this.finish();
                }
                break;
            default:
                throw new UnknownError("Something went wrong :(");
        }
        tvStage.setText(String.format(Locale.getDefault(),"%1$d/%2$d", page, pages));
    }

    private boolean putPreferences() {
        boolean locked = appLock.isChecked();
        username = etUsername.getText().toString().trim();
        username = username.isEmpty()?"User":username;
        String passcode = etPasscode.getText().toString().trim();
        String passcodeHint = etPasscodehint.getText().toString().trim();
        String secQ = etSecQ.getText().toString().trim();
        String secA = etSecA.getText().toString().trim();
        if (locked) {
            page = 2;
            if (passcode.isEmpty()) {
                Toast.makeText(this, "Field \'Passcode\' is required.", Toast.LENGTH_LONG).show();
                parsePage(page);
                etPasscode.requestFocus();
                return false;
            }
            if (!secQ.isEmpty() && secA.isEmpty()) {
                Toast.makeText(this, "Field \'Security Answer\' is required with \'Security Question\'.", Toast.LENGTH_LONG).show();
                parsePage(page);
                etSecA.requestFocus();
                return false;
            }
            if (secQ.isEmpty() && !secA.isEmpty()) {
                Toast.makeText(this, "Field \'Security Question\' is required with \'Security Answer\'.", Toast.LENGTH_LONG).show();
                parsePage(page);
                etSecQ.requestFocus();
                return false;
            }
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor buddy = preferences.edit();
        buddy.putString("username", username);
        if(locked) {
            buddy.putBoolean("appLock", true);
            buddy.putString("passcode", passcode);
            if(!passcodeHint.isEmpty()) buddy.putString("passcodeHint", passcodeHint);
            if(!secQ.isEmpty()){
                buddy.putString("securityQ", secQ);
                buddy.putString("securityA", secA);
            }
        }
        buddy.apply();
        return true;
    }

    @Override
    public void onBackPressed() {
        parsePage(--page);
    }
}

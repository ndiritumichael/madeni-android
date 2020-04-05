package com.typicalgeek.madeni;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Objects;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("darkTheme", false)){
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.nunito_light));
        toolbarLayout.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.nunito_extra_light));
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse("mailto:"));
                String [] s = {"plenjo15@gmail.com"};
                i.putExtra(Intent.EXTRA_EMAIL, s);
                i.putExtra(Intent.EXTRA_SUBJECT, "Madeni App Feedback");
                i.putExtra(Intent.EXTRA_TEXT, "Hello Typical Geek. I have tried your app and this is what I think. \n");
                Intent chooser = Intent.createChooser(i, "Send Feedback via:");
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
            }
        });
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_preferences:
                startActivity(new Intent(HelpActivity.this, PreferencesActivity.class).putExtra("home", getClass()));
                finish();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        startActivity(new Intent(HelpActivity.this, MainActivity.class));
        finish();
    }
}
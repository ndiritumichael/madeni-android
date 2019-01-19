package com.typicalgeek.madeni;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    boolean doubleBackToExitPressedOnce = false;
    DatabaseHelper databaseHelper = new DatabaseHelper(this);
    Cursor res;
    TextView tvWelcome, tvDebts, tvSummary;
    RelativeLayout rlDebts, rlSummary;
    SwipeRefreshLayout swipeMain;
    Button btnHelp, btnDebts, btnSummary;
    ImageButton btnHideDebts, btnHideSummary;
    SharedPreferences SP;
    int countOwe, countOwed;
    float sumOwe, sumOwed, totalPayment;
    String currency;
    final int REFRESH_LENGTH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("darkTheme", false)){
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.nunito_light));
        toolbarLayout.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.nunito_extra_light));
        FloatingActionButton fab = findViewById(R.id.fab);
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        rlDebts = findViewById(R.id.rlDebts);
        rlSummary = findViewById(R.id.rlSummary);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvDebts = findViewById(R.id.tvDebts);
        tvSummary = findViewById(R.id.tvSummary);
        btnHelp = findViewById(R.id.btnMainHelp);
        btnDebts = findViewById(R.id.btnMainDebts);
        btnSummary = findViewById(R.id.btnMainSummary);
        btnHideDebts = findViewById(R.id.ibDebts);
        btnHideSummary = findViewById(R.id.ibSummary);
        swipeMain = findViewById(R.id.swipeMain);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swipeMain.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeMain.setRefreshing(false);
                    }
                }, REFRESH_LENGTH);
                refreshWelcome();
                refreshDebts();
                refreshSummary();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        refreshWelcome();
        refreshDebts();
        refreshSummary();
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
                finish();
            }
        });
        btnDebts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DebtsActivity.class));
                finish();
            }
        });
        btnSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SummaryActivity.class));
                finish();
            }
        });

        btnHideDebts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rlDebts.getVisibility()==View.VISIBLE){
                    rlDebts.setVisibility(View.GONE);
                    btnHideDebts.setImageDrawable(getDrawable(R.drawable.ic_unhide));
                } else {
                    rlDebts.setVisibility(View.VISIBLE);
                    btnHideDebts.setImageDrawable(getDrawable(R.drawable.ic_hide));
                }
            }
        });
        btnHideSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rlSummary.getVisibility()==View.VISIBLE){
                    rlSummary.setVisibility(View.GONE);
                    btnHideSummary.setImageDrawable(getDrawable(R.drawable.ic_unhide));
                } else {
                    rlSummary.setVisibility(View.VISIBLE);
                    btnHideSummary.setImageDrawable(getDrawable(R.drawable.ic_hide));
                }
            }
        });

        swipeMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWelcome();
                refreshDebts();
                refreshSummary();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeMain.setRefreshing(false);
                    }
                }, REFRESH_LENGTH);
            }
        });
    }

    private void refreshWelcome(){
        tvWelcome.setText(String.format(getString(R.string.welcome_card_info), SP.getString("username", "User")));
    }

    private void refreshDebts() {
        currency = SP.getString("currency", "KSH");
        countOwe = databaseHelper.countFiltered(DatabaseHelper.DEBTS_TABLE_NAME, DatabaseHelper.DEBTS_COL_5, DatabaseHelper.DEBT_OWE);
        countOwed = databaseHelper.countFiltered(DatabaseHelper.DEBTS_TABLE_NAME, DatabaseHelper.DEBTS_COL_5, DatabaseHelper.DEBT_OWED);
        sumOwe = 0; sumOwed = 0;
        totalPayment = 0;
        res = databaseHelper.getFilteredData(DatabaseHelper.DEBTS_TABLE_NAME, DatabaseHelper.DEBTS_COL_5, DatabaseHelper.DEBT_OWE);
        if (res != null && res.getCount() > 0) {
            while (res.moveToNext()) {
                sumOwe += res.getFloat(3);
                Cursor cursor = databaseHelper.getFilteredData(DatabaseHelper.PAYMENTS_TABLE_NAME, DatabaseHelper.PAYMENTS_COL_1, res.getInt(0));
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        totalPayment += cursor.getFloat(2);
                    }
                }
            }
        }
        sumOwe -= totalPayment>sumOwe?sumOwe:totalPayment;
        totalPayment = 0;
        res = databaseHelper.getFilteredData(DatabaseHelper.DEBTS_TABLE_NAME, DatabaseHelper.DEBTS_COL_5, DatabaseHelper.DEBT_OWED);
        if (res != null && res.getCount() > 0) {
            while (res.moveToNext()) {
                sumOwed += res.getFloat(3);
                Cursor cursor = databaseHelper.getFilteredData(DatabaseHelper.PAYMENTS_TABLE_NAME, DatabaseHelper.PAYMENTS_COL_1, res.getInt(0));
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        totalPayment += cursor.getFloat(2);
                    }
                }
            }
        }
        sumOwed -= totalPayment>sumOwed?sumOwed:totalPayment;
        tvDebts.setText(String.format(getString(R.string.debts_card_info),
                countOwe, currency, sumOwe, countOwed, sumOwed));
    }

    private void refreshSummary() {
        currency = SP.getString("currency", "KSH");
        float netDebt = sumOwed - sumOwe;
        int countTotal = databaseHelper.countAll(DatabaseHelper.DEBTS_TABLE_NAME);
        tvSummary.setText(String.format(getString(R.string.summary_card_info),
                currency, netDebt, countTotal));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_preferences) {
            startActivity(new Intent(MainActivity.this, PreferencesActivity.class)
                    .putExtra("home", getClass()));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_new_debt:
                startActivity(new Intent(MainActivity.this, NewDebtActivity.class));
                finish();
                break;
            case R.id.nav_view_debts:
                startActivity(new Intent(MainActivity.this, DebtsActivity.class));
                finish();
                break;
            case R.id.nav_debt_summary:
                startActivity(new Intent(MainActivity.this, SummaryActivity.class));
                finish();
                break;
            case R.id.nav_help:
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
                finish();
                break;
            case R.id.nav_share: {
                Intent i = new Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.str_app_name))
                        .putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.str_app_description),
                                getString(R.string.share_link)));
                if (i.resolveActivity(getPackageManager()) != null) startActivity(Intent.createChooser(i, "Share App Link via:"));
                else Toast.makeText(this, "No apps can perform this action.", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.nav_feedback: {
                String[] s = {getString(R.string.str_support_email)};
                Intent i = new Intent(Intent.ACTION_SENDTO)
                        .setData(Uri.parse("mailto:"))
                        .putExtra(Intent.EXTRA_EMAIL, s)
                        .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.str_feedback_subject))
                        .putExtra(Intent.EXTRA_TEXT, getString(R.string.str_subject_header));
                if (i.resolveActivity(getPackageManager()) != null) startActivity(Intent.createChooser(i, "Send Feedback via:"));
                else Toast.makeText(this, "No apps can perform this action.", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
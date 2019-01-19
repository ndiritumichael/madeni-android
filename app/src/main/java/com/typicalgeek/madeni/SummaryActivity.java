package com.typicalgeek.madeni;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SummaryActivity extends AppCompatActivity {
    SwipeRefreshLayout swipeSummary;
    final int REFRESH_LENGTH = 3000;
    ProgressBar pbOwe, pbOwed;
    TextView perOwe, perOwed, oweCount, owedCount, oweSum, owedSum, oweDates, owedDates, sumNet;
    int owePerc, owedPerc, countOwe, countOwed;
    float oweCash, owedCash, net, total, payCash;
    String curr, dateOweStart, dateOweEnd, dateOwedStart, dateOwedEnd, dateCurr;
    DatabaseHelper databaseHelper = new DatabaseHelper(this);
    SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    Cursor res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("darkTheme", false)){
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        swipeSummary = findViewById(R.id.swipeSummary);
        pbOwe = findViewById(R.id.pbDebtOwe);
        pbOwed = findViewById(R.id.pbDebtOwed);
        perOwe = findViewById(R.id.percentageOwe);
        perOwed = findViewById(R.id.percentageOwed);
        oweCount = findViewById(R.id.countOwe);
        owedCount = findViewById(R.id.countOwed);
        oweSum = findViewById(R.id.sumOwe);
        owedSum = findViewById(R.id.sumOwed);
        oweDates = findViewById(R.id.datesOwe);
        owedDates = findViewById(R.id.datesOwed);
        sumNet = findViewById(R.id.summaryNet);
        refreshSummary();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshSummary();
                swipeSummary.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeSummary.setRefreshing(false);
                    }
                }, REFRESH_LENGTH);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeSummary.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshSummary();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeSummary.setRefreshing(false);
                    }
                }, REFRESH_LENGTH);
            }
        });
    }

    private void refreshSummary() {
        curr = PreferenceManager.getDefaultSharedPreferences(this).getString("currency", "KSH");
        countOwe = 0; countOwed = 0;
        oweCash = 0; owedCash = 0;
        payCash = 0;
        res = databaseHelper.getFilteredData(DatabaseHelper.DEBTS_TABLE_NAME, DatabaseHelper.DEBTS_COL_5, DatabaseHelper.DEBT_OWE);
        if (res != null && res.getCount() > 0) {
            countOwe = databaseHelper.countFiltered(DatabaseHelper.DEBTS_TABLE_NAME, DatabaseHelper.DEBTS_COL_5, DatabaseHelper.DEBT_OWE);
            while (res.moveToNext()) {
                oweCash += res.getFloat(3);
                Cursor cursor = databaseHelper.getFilteredData(DatabaseHelper.PAYMENTS_TABLE_NAME, DatabaseHelper.PAYMENTS_COL_1, res.getInt(0));
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        payCash += cursor.getFloat(2);
                    }
                }
            }
        }
        oweCash -= payCash>oweCash?oweCash:payCash;
        payCash = 0;
        res = databaseHelper.getFilteredData(DatabaseHelper.DEBTS_TABLE_NAME, DatabaseHelper.DEBTS_COL_5, DatabaseHelper.DEBT_OWED);
        if (res != null && res.getCount() > 0) {
            countOwed = databaseHelper.countFiltered(DatabaseHelper.DEBTS_TABLE_NAME, DatabaseHelper.DEBTS_COL_5, DatabaseHelper.DEBT_OWED);
            while (res.moveToNext()) {
                owedCash += res.getFloat(3);
                Cursor cursor = databaseHelper.getFilteredData(DatabaseHelper.PAYMENTS_TABLE_NAME, DatabaseHelper.PAYMENTS_COL_1, res.getInt(0));
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        payCash += cursor.getFloat(2);
                    }
                }
            }
        }
        owedCash -= payCash>owedCash?owedCash:payCash;
        try {
            res = databaseHelper.getFilteredData(DatabaseHelper.DEBTS_TABLE_NAME, DatabaseHelper.DEBTS_COL_5, DatabaseHelper.DEBT_OWE);
            if (res != null && res.getCount() > 0) {
                while (res.moveToNext()) {
                    dateCurr = res.getString(6);
                    if (res.isFirst()){
                        dateOweStart = dateCurr;
                        dateOweEnd = dateCurr;
                    }
                    if (parser.parse(dateCurr).before(parser.parse(dateOweStart))) {
                        dateOweStart = dateCurr;
                    } else if (parser.parse(dateCurr).after(parser.parse(dateOweEnd))) {
                        dateOweEnd = dateCurr;
                    }

                }
            }
            res = databaseHelper.getFilteredData(DatabaseHelper.DEBTS_TABLE_NAME, DatabaseHelper.DEBTS_COL_5, DatabaseHelper.DEBT_OWED);
            if (res != null && res.getCount() > 0) {
                while (res.moveToNext()) {
                    dateCurr = res.getString(6);
                    if (res.isFirst()){
                        dateOwedStart = dateCurr;
                        dateOwedEnd = dateCurr;
                    }
                    if (parser.parse(dateCurr).before(parser.parse(dateOwedStart))){
                        dateOwedStart = dateCurr;
                    } else if (parser.parse(dateCurr).after(parser.parse(dateOwedEnd))){
                        dateOwedEnd = dateCurr;
                    }
                }
            }
        } catch (Exception ex){
            Toast.makeText(this, "Hey, 😃 I'thisMinute broken, just like the developer;\n"+ex.toString(), Toast.LENGTH_LONG).show();
        }
        net = owedCash-oweCash;
        total = ((oweCash+owedCash)==0)? 1 :(oweCash+owedCash);
        total = oweCash+owedCash;
        owedPerc = (int) (((owedCash / total) * 100) + 0.5);
        owePerc = (int) (((oweCash / total) * 100) + 0.5);
        pbOwe.setProgress(owePerc);
        pbOwed.setProgress(owedPerc);
        perOwe.setText(String.format(Locale.getDefault(), getString(R.string.owed_by_you_per), owePerc));
        perOwed.setText(String.format(Locale.getDefault(), getString(R.string.owed_to_you_per), owedPerc));
        oweCount.setText(String.format(Locale.getDefault(), getString(R.string.summary_count), countOwe));
        owedCount.setText(String.format(Locale.getDefault(), getString(R.string.summary_count), countOwed));
        oweSum.setText(String.format(Locale.getDefault(), getString(R.string.summary_sum), curr, oweCash));
        owedSum.setText(String.format(Locale.getDefault(), getString(R.string.summary_sum), curr, owedCash));
        if(dateOweStart!=null) {
            if (dateOweStart.equals(dateOweEnd)) {
                oweDates.setText(String.format(Locale.getDefault(), getString(R.string.summary_date), dateOweStart));
            } else {
                oweDates.setText(String.format(Locale.getDefault(), getString(R.string.summary_dates), dateOweStart, dateOweEnd));
            }
        }
        if (dateOwedStart != null) {
            if (dateOwedStart.equals(dateOwedEnd)) {
                owedDates.setText(String.format(Locale.getDefault(), getString(R.string.summary_date), dateOwedStart));
            } else {
                owedDates.setText(String.format(Locale.getDefault(), getString(R.string.summary_dates), dateOwedStart, dateOwedEnd));
            }
        }
        sumNet.setText(String.format(Locale.getDefault(), getString(R.string.net_debt_summ), curr, net));
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
                startActivity(new Intent(SummaryActivity.this, PreferencesActivity.class).putExtra("home", getClass()));
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
        startActivity(new Intent(SummaryActivity.this, MainActivity.class));
        finish();
    }
}
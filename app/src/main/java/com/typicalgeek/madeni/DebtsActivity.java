package com.typicalgeek.madeni;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Objects;

public class DebtsActivity extends AppCompatActivity {

    final int REFRESH_LENGTH = 2000;
    SwipeRefreshLayout swipeAllDebts, swipeYouOwe, swipeOwedToYou;
    DatabaseHelper databaseHelper;
    boolean isDark;
    ImageButton btnOverflowAll;
    SharedPreferences SP;
    static String currency;
    static DatabaseHelper myDb;
    static Cursor res;
    static int vector, dialogThemeID  = R.style.AlertDialogCustom;
    FloatingActionButton fab;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        isDark = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("darkTheme", false);
        if (isDark){
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        dialogThemeID = isDark?R.style.AlertDialogCustom_Dark:R.style.AlertDialogCustom;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debts);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.nunito_light));
        toolbarLayout.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.nunito_extra_light));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fab = findViewById(R.id.fab);
        myDb = new DatabaseHelper(this);
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        currency = SP.getString("currency", "KSH");
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("ALL DEBTS"));
        tabLayout.addTab(tabLayout.newTab().setText("YOU OWE"));
        tabLayout.addTab(tabLayout.newTab().setText("OWED TO YOU"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (!fab.isShown()) fab.show();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (viewPager.getCurrentItem()){
                    case 0:
                            ((AllDebtsFragment) Objects.requireNonNull (viewPager.getAdapter())
                                    .instantiateItem(viewPager, 0)).refreshAll(true);
                        break;
                    case 1:
                        ((YouOweFragment) Objects.requireNonNull (viewPager.getAdapter())
                                .instantiateItem(viewPager, 1)).refreshAll(true);
                        break;
                    case 2:
                        ((OwedToYouFragment) Objects.requireNonNull (viewPager.getAdapter())
                                .instantiateItem(viewPager, 2)).refreshAll(true);
                        break;
                    default:
                        Toast.makeText(DebtsActivity.this, "An error occurred while refreshing.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static Debt[] dbGetAllDebts() {
        res = myDb.getAllData(DatabaseHelper.DEBTS_TABLE_NAME);
        Debt[] debt = new Debt[res.getCount()];
        int i = 0;
        if(res!=null && res.getCount()>0){
            while (res.moveToNext()){
                debt[i] = new Debt(res.getInt(0), res.getString(1),
                        res.getLong(2), res.getFloat(3),
                        res.getString(4), res.getString(5),
                        res.getString(6));
                i++;
            }
        }
        return debt;
    }

    public static Payment[] dbGetAllPayments() {
        res = myDb.getAllData(DatabaseHelper.PAYMENTS_TABLE_NAME);
        Payment[] payment = new Payment[res.getCount()];
        int i = 0;
        if(res!=null && res.getCount()>0){
            while (res.moveToNext()){
                payment[i] = new Payment(res.getInt(0), res.getInt(1),
                        res.getFloat(2), res.getString(3));
                i++;
            }
        }
        return payment;
    }

    public static Debt[] dbGetFilteredDebts(String COL_INDEX, String value) {
        res = myDb.getFilteredData(DatabaseHelper.DEBTS_TABLE_NAME, COL_INDEX, value);
        Debt[] debt = new Debt[res.getCount()];
        int i = 0;
        if(res!=null && res.getCount()>0){
            while (res.moveToNext()){
                debt[i] = new Debt(res.getInt(0), res.getString(1),
                        res.getLong(2), res.getFloat(3),
                        res.getString(4), res.getString(5),
                        res.getString(6));
                i++;
            }
        }
        return debt;
    }

    public static Payment[] dbGetFilteredPayments(String COL_INDEX, int value) {
        res = myDb.getFilteredData(DatabaseHelper.PAYMENTS_TABLE_NAME, COL_INDEX, value);
        Payment[] payment = new Payment[res.getCount()];
        int i = 0;
        if(res!=null && res.getCount()>0){
            while (res.moveToNext()){
                payment[i] = new Payment(res.getInt(0), res.getInt(1),
                        res.getFloat(2), res.getString(3));
                i++;
            }
        }
        return payment;
    }

    void addPayment(Context context, Payment payment){
        databaseHelper = new DatabaseHelper(context);
        if (databaseHelper.insertPayment(payment))
            Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show();
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
                startActivity(new Intent(DebtsActivity.this, PreferencesActivity.class).putExtra("home", getClass()));
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
        startActivity(new Intent(DebtsActivity.this, MainActivity.class));
        finish();
    }
}

interface RefreshInterface{
    void refreshAll();
}
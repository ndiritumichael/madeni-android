package com.typicalgeek.madeni;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewDebtActivity extends AppCompatActivity {
    Spinner descSpinner, typeSpinner;
    EditText etPerson, etPhone, etAmount, etDescription;
    CheckBox cbRem;
    DatabaseHelper databaseHelper;
    String person;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    int selection, layout = R.style.AlertDialogCustom, d, M, y, h, m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("darkTheme", false)){
            setTheme(R.style.AppTheme_Dark_NoActionBar);
            layout = R.style.AlertDialogCustom_Dark;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_debt);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        etPerson = findViewById(R.id.etPersonName);
        etPhone = findViewById(R.id.etPersonPhone);
        etAmount = findViewById(R.id.etAmountOwed);
        etDescription = findViewById(R.id.etDebtDescription);
        descSpinner = findViewById(R.id.descriptionSpinner);
        typeSpinner = findViewById(R.id.debtTypeSpinner);
        cbRem = findViewById(R.id.cbReminder);
        databaseHelper = new DatabaseHelper(this);
        final FloatingActionButton fabSubmit = findViewById(R.id.fabSubmit);
        final FloatingActionButton fabContacts = findViewById(R.id.fabContacts);
        cbRem.setTypeface(ResourcesCompat.getFont(this, R.font.nunito_extra_light));
        descSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                findViewById(R.id.tilDescription).setVisibility(position==3?View.VISIBLE:View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        fabSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etPhone.getText().toString().trim().equals("")) {
                    etPhone.setText("0");
                }
                if (etAmount.getText().toString().trim().equals("")) {
                    etAmount.setText("0");
                }
                m = Integer.parseInt(new SimpleDateFormat("mm", Locale.getDefault()).format(new Date()));
                h = Integer.parseInt(new SimpleDateFormat("hh", Locale.getDefault()).format(new Date()));
                d = Integer.parseInt(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()));
                M = Integer.parseInt(new SimpleDateFormat("MM", Locale.getDefault()).format(new Date()));
                y = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date()));
                Debt entry = getDebt();
                if (dataValidate(entry)) {
                    if (cbRem.isChecked()) {
                        LayoutInflater inflater = LayoutInflater.from(view.getContext());
                        final View remView = inflater.inflate(R.layout.layout_reminder,
                                (ViewGroup) view.getParent(), false);
                        final CalendarView calendarView = remView.findViewById(R.id.calendarViewReminder);
                        new androidx.appcompat.app.AlertDialog.Builder(new ContextThemeWrapper(NewDebtActivity.this, layout))
                                .setTitle("Add Reminder")
                                .setView(remView)
                                .setPositiveButton("ADD AND SAVE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Calendar remTime = Calendar.getInstance();
                                        m = Integer.parseInt(new SimpleDateFormat("mm", Locale.getDefault()).format(new Date()));
                                        h = Integer.parseInt(new SimpleDateFormat("hh", Locale.getDefault()).format(new Date()));
                                        remTime.set(y, M, d, h, m);
                                        Intent intent = new Intent(Intent.ACTION_INSERT)
                                                .setData(CalendarContract.Events.CONTENT_URI)
                                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, remTime.getTimeInMillis())
                                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, remTime.getTimeInMillis())
                                                .putExtra(CalendarContract.Events.TITLE, String.format(Locale.getDefault(), "Madeni App: %s\'s debt", person))
                                                .putExtra(CalendarContract.Events.DESCRIPTION,
                                                        String.format(Locale.getDefault(), "Reminder for debt to %s. View in app for more details.", person))
                                                .putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE);
                                        dbInsert(getDebt());
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("CANCEL", null)
                                .create().show();
                        calendarView.setMinDate(new Date().getTime());
                        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                            @Override
                            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                                d = dayOfMonth;
                                M = month;
                                y = year;
                            }
                        });
                    } else {
                        dbInsert(entry);
                    }
                }
            }
        });
        fabContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasPermissions()) {
                    startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 1);
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private Debt getDebt() {
        person = etPerson.getText().toString().trim();
        final long phone = Long.parseLong(etPhone.getText().toString().trim());
        final float amount = Float.parseFloat(etAmount.getText().toString().trim());
        String desc = descSpinner.getSelectedItem().toString();
        String[] descArr = getResources().getStringArray(R.array.descriptionSpinnerArray);
        final String description = (desc.equals(descArr[0])||desc.equals(descArr[3]))?
                etDescription.getText().toString().trim() : descSpinner.getSelectedItem().toString().trim();
        final String spinnerText = typeSpinner.getSelectedItem().toString().trim();
        final String dateEntered = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        return new Debt(person, phone, amount, description, spinnerText, dateEntered);
    }

    private boolean dataValidate(final Debt debt){
        if (debt.getDebtName().isEmpty()) {
            Toast.makeText(this, "ERROR! Person's Name cannot be empty.", Toast.LENGTH_LONG).show();
            etPerson.requestFocus();
            return false;
        } else if (debt.getDebtAmount() == 0) {
            Toast.makeText(this, "ERROR! Amount cannot be empty.", Toast.LENGTH_LONG).show();
            etAmount.requestFocus();
            return false;
        }else if (debt.getDebtType().equals(getResources().getStringArray(R.array.typeSpinnerArray)[0])) {
            Toast.makeText(this, "ERROR! Please pick a debt type.", Toast.LENGTH_SHORT).show();
            typeSpinner.performClick();
            return false;
        }
        return true;
    }

    private void dbInsert(Debt debt){
        try {
            Boolean result = databaseHelper.insertDebt(debt);
            if (result) {
                etPerson.setText("");
                etPhone.setText("");
                etAmount.setText("");
                descSpinner.setSelection(0, true);
                etDescription.setText("");
                typeSpinner.setSelection(0,true);
                dbInsertSuccess();
            } else {
                dbInsertFailed();
            }
        } catch (Exception e){
            String exc = e.toString();
            Toast.makeText(this, exc, Toast.LENGTH_SHORT).show();
        }
    }

    private void dbInsertSuccess() {
        Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, DebtsActivity.class),0);
        String notif = ""+RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifSound = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString("notification",
                notif));
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(NewDebtActivity.this, "Debts")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setBadgeIconType(R.mipmap.ic_launcher)
                        .setContentTitle("Success!")
                        .setContentText("Your debt has been saved. Tap to view.")
                        .setSound(notifSound)
                        .setPriority(0)
                        .setContentIntent(pi);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());
    }

    private void dbInsertFailed() {
        Toast.makeText(this, "Failed. Please get in touch with developer.", Toast.LENGTH_LONG).show();
        drastic();
        String notif = ""+RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifSound = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString("notification",
                notif));
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(NewDebtActivity.this, "Debts")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setBadgeIconType(R.mipmap.ic_launcher)
                        .setContentTitle("Failed!")
                        .setContentText("Your debt has not been saved.")
                        .setSound(notifSound)
                        .setPriority(0);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());
    }

    public void drastic(){
        new androidx.appcompat.app.AlertDialog.Builder(new ContextThemeWrapper(NewDebtActivity.this, layout))
                .setTitle("Operation Failed")
                .setMessage("Your attempt to save a debt failed. Would you like to contact the developer?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] s = {getString(R.string.str_support_email)};
                        Intent i = new Intent(Intent.ACTION_SENDTO)
                                .setData(Uri.parse("mailto:"))
                                .putExtra(Intent.EXTRA_EMAIL, s)
                                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.str_crash_subject))
                                .putExtra(Intent.EXTRA_TEXT, getString(R.string.str_crash_header));
                        if (i.resolveActivity(getPackageManager()) != null) startActivity(Intent.createChooser(i, "Send Feedback via:"));
                        else Toast.makeText(NewDebtActivity.this, "No apps can perform this action.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("NO", null)
                .create().show();
    }

    private boolean hasPermissions() {
        int check = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (check == PackageManager.PERMISSION_GRANTED) return true;
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 1);
            else Toast.makeText(this, "Grant permissions to select a contact.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data){
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            assert uri != null;
            Cursor cursor = this.getContentResolver().query(uri, null,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1",
                    null, null);
            while (cursor != null && cursor.moveToNext()) {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                final String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                        null, null);
                assert phones != null;
                int num = phones.getCount();
                if (num == 1) {
                    phones.moveToNext();
                    etPerson.setText(contactName);
                    etPhone.setText(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ",""));
                } else {
                    final String[] phonesList = new String[num];
                    int i = 0;
                    while (phones.moveToNext()) {
                        phonesList[i++] = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ","");
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            etPerson.setText(contactName);
                            etPhone.setText(phonesList[selection]);

                        }
                    })
                            .setNegativeButton("CANCEL", null)
                            .setTitle("Select number for " + contactName)
                            .setSingleChoiceItems(phonesList, 0, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selection = which;
                                }
                            }).create().show();
                }
                phones.close();
            }
            assert cursor != null;
            cursor.close();
        }
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

                startActivity(new Intent(NewDebtActivity.this, PreferencesActivity.class).putExtra("home", getClass()));
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
        startActivity(new Intent(NewDebtActivity.this, MainActivity.class));
        finish();
    }
}
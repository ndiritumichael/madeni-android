package com.typicalgeek.madeni;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;

public class PreferencesActivity extends PreferenceActivity{
    static int layout;
    private final static int BACKUP_REQUEST = 100, BACKUP_PERMISSION_REQUEST = 101,
            RESTORE_REQUEST = 200, RESTORE_PERMISSION_REQUEST = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isDark = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("darkTheme", false);
        if (isDark) setTheme(R.style.AppTheme_Dark_NoActionBar_BlendStatusBar);
        layout = isDark? R.style.AlertDialogCustom_Dark: R.style.AlertDialogCustom;
        super.onCreate(savedInstanceState);
        // TODO: 08/09/2018 Deprecated
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferencesFragment()).commit();
    }

    public static class PreferencesFragment extends PreferenceFragment {
        Intent i;
        @Override
        public void onCreate(final Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            final SwitchPreference theme = (SwitchPreference) findPreference("darkTheme");
            Preference backup = findPreference("deviceBackupNow");
            Preference restore = findPreference("deviceRestore");
            Preference clear = findPreference("clear");
            final ListPreference nudge = (ListPreference) findPreference("nudge");
            Preference credits = findPreference("credits");

            theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        }
                    }, 50);
                    return true;
                }
            });
            clear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), layout))
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new DatabaseHelper(getActivity()).clearDB();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setTitle("Clear Database")
                            .setMessage("This action cannot be undone!\nPreferences will not be deleted.")
                            .create().show();
                    return true;
                }
            });
            backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // TODO: 18/09/2018 Permissions
                    try {
                        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addCategory(Intent.CATEGORY_DEFAULT);
                        if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                            if (hasPermissions(BACKUP_PERMISSION_REQUEST)) {
                                startActivityForResult(Intent.createChooser(i, "Choose backup directory:"), BACKUP_REQUEST);
                            }
                        } else Toast.makeText(getActivity(), "No apps can perform this action.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });
            restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT).setType("*/*");
                    if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                        if (hasPermissions(RESTORE_PERMISSION_REQUEST)) {
                            startActivityForResult(Intent.createChooser(i, "Choose backup file:"), RESTORE_REQUEST);
                        }
                    } else Toast.makeText(getActivity(), "No apps can perform this action.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            nudge.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    DebtsActivity.vector = Integer.parseInt((String) newValue);
                    return true;
                }
            });
            credits.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), layout))
                            .setNeutralButton("Close", null)
                            .setTitle("App Credits")
                            .setMessage(R.string.str_credits)
                            .create().show();
                    return true;
                }
            });
        }

        private boolean hasPermissions(int REQUEST_CODE) {
            int checkRead = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
            int checkWrite = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkRead == PackageManager.PERMISSION_GRANTED && checkWrite == PackageManager.PERMISSION_GRANTED) {return true;}
            else {
                try {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE);
                    return false;
                } catch (Exception e){
                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            try {
                if (requestCode == BACKUP_REQUEST && resultCode == RESULT_OK) {
                    Uri backupUri = data.getData();
                    if (backupUri != null){
                        // DocumentFile documentFile = DocumentFile.fromTreeUri(getActivity(), backupUri);
                        /*String backupPath = backupUri.getPath();
                        Toast.makeText(getActivity(), backupPath, Toast.LENGTH_SHORT).show();*/
                        final String dbFileName = getActivity().getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath();
                        File dbFile = new File(dbFileName);
                        FileInputStream inputStream = new FileInputStream(dbFile);

                        // TODO: 08/09/2018 User select backup directory
                        File file = new File(Environment.getExternalStorageDirectory()+"/madeni_database_backup.db");
                        final String backupFileName = file.getPath();
                        OutputStream outputStream = new FileOutputStream(backupFileName);

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }

                        outputStream.flush();
                        outputStream.close();
                        inputStream.close();

                        Toast.makeText(getActivity(), "Backup successful", Toast.LENGTH_SHORT).show();
                    }
                } else  if (requestCode == RESTORE_REQUEST && resultCode == RESULT_OK) {
                    // TODO: 08/09/2018 Path from URI
                    // TODO: 08/09/2018 Verify DB structure
                    // TODO: 08/09/2018 Display backup creation date
                    final Uri restoreUri = data.getData();
                    if (restoreUri != null) {
                        final String restorePath = restoreUri.toString();
                        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), layout))
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            File restoreFile = new File(new URI(restorePath));
                                            FileInputStream inputStream = new FileInputStream(restoreFile);

                                            final String dbFileName = getActivity().getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath();
                                            OutputStream outputStream = new FileOutputStream(dbFileName);

                                            byte[] buffer = new byte[1024];
                                            int length;
                                            while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);

                                            outputStream.flush();
                                            outputStream.close();
                                            inputStream.close();

                                            Toast.makeText(getActivity(), "Restore successful", Toast.LENGTH_SHORT).show();
                                        }catch (Exception e){
                                            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .setNegativeButton("No", null)
                                .setTitle("Restore Backup")
                                .setMessage(String.format(Locale.getDefault(),
                                        "You have selected to restore file %s.\nWarning: This will completely overwrite current database. Proceed?",
                                        restorePath))
                                .create().show();
                    }
                }
            } catch (Exception e){
                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

     @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case BACKUP_PERMISSION_REQUEST:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addCategory(Intent.CATEGORY_DEFAULT);
                        startActivityForResult(Intent.createChooser(i, "Choose backup directory:"), BACKUP_REQUEST);
                    } else
                        Toast.makeText(this, "Grant permissions to perform backup.", Toast.LENGTH_LONG).show();

                    break;
                case RESTORE_PERMISSION_REQUEST:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        Intent i = new Intent(Intent.ACTION_GET_CONTENT).setType("*/*");
                        startActivityForResult(Intent.createChooser(i, "Choose backup file:"), RESTORE_REQUEST);
                    } else
                        Toast.makeText(this, "Grant permissions to perform restore.", Toast.LENGTH_LONG).show();

                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    break;
            }
        }

    public void onBackPressed(){
        Class home = (Class) Objects.requireNonNull(getIntent().getExtras()).get("home");
        home = home==null?MainActivity.class:home;
        startActivity(new Intent(PreferencesActivity.this, home));
        finish();
    }
}
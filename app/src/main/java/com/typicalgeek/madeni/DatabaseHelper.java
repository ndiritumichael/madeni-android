package com.typicalgeek.madeni;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
    static final String DATABASE_NAME = "MadeniDB";
    private static final int DATABASE_VERSION = 1;
    public static final String DEBTS_TABLE_NAME = "debts_table";
    public static final String PAYMENTS_TABLE_NAME = "payments_table";

    static final String DEBTS_COL_0 = "ID";
    static final String DEBTS_COL_1 = "NAME";
    static final String DEBTS_COL_2 = "PHONE";
    static final String DEBTS_COL_3 = "AMOUNT";
    static final String DEBTS_COL_4 = "DESCRIPTION";
    public static final String DEBTS_COL_5 = "TYPE";
    static final String DEBTS_COL_6 = "DATE";

    static final String PAYMENTS_COL_0 = "ID";
    public static final String PAYMENTS_COL_1 = "DEBT";
    static final String PAYMENTS_COL_2 = "AMOUNT";
    static final String PAYMENTS_COL_3 = "DATE";

    public static final String DEBT_OWE = "Debt You Owe";
    public static final String DEBT_OWED = "Debt Owed To You";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DEBTS_TABLE_NAME + "(" + DEBTS_COL_0 + " INTEGER PRIMARY KEY, " + DEBTS_COL_1 +
                " TEXT, " + DEBTS_COL_2 + " INTEGER, " + DEBTS_COL_3 + " REAL, " + DEBTS_COL_4 + " TEXT, " + DEBTS_COL_5 +
                " TEXT, " + DEBTS_COL_6 + " TEXT)");
        db.execSQL("CREATE TABLE " + PAYMENTS_TABLE_NAME + "(" + PAYMENTS_COL_0 + " INTEGER PRIMARY KEY, " + PAYMENTS_COL_1 +
                " INTEGER, " + PAYMENTS_COL_2 + " REAL, " + PAYMENTS_COL_3 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int NewVersion) {
        clearDB();
    }

    public boolean insertDebt(Debt debt){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DEBTS_COL_1, debt.getDebtName());
        contentValues.put(DEBTS_COL_2, debt.getDebtPhone());
        contentValues.put(DEBTS_COL_3, debt.getDebtAmount());
        contentValues.put(DEBTS_COL_4, debt.getDebtDescription());
        contentValues.put(DEBTS_COL_5, debt.getDebtType());
        contentValues.put(DEBTS_COL_6, debt.getDebtDate());
        long result = db.insert(DEBTS_TABLE_NAME, null, contentValues);
        db.close();
        return result != -1;
    }

    public boolean insertPayment(Payment payment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PAYMENTS_COL_1, payment.getPaymentDebt());
        contentValues.put(PAYMENTS_COL_2, payment.getPaymentAmount());
        contentValues.put(PAYMENTS_COL_3, payment.getPaymentDate());
        long result = db.insert(PAYMENTS_TABLE_NAME, null, contentValues);
        db.close();
        return result != -1;
    }

    public Cursor getAllData(String TABLE_NAME){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public Cursor getFilteredData(String TABLE_NAME, String COL_INDEX, String value){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_INDEX + " = \'" + value +"\'", null);
    }

    public Cursor getFilteredData(String TABLE_NAME, String COL_INDEX, int value){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_INDEX + " = " + value, null);
    }

    public int countAll(String TABLE_NAME){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null).getCount();
    }

    public int countFiltered(String TABLE_NAME, String COL_INDEX, String value){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_INDEX + " = \'" + value +"\'", null).getCount();
    }

    public void clearDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DEBTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PAYMENTS_TABLE_NAME);
        onCreate(db);
    }

    public void deleteDebt(int ID){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + DEBTS_TABLE_NAME + " WHERE " + DEBTS_COL_0 + " = " + ID);
        db.execSQL("DELETE FROM " + PAYMENTS_TABLE_NAME + " WHERE " + PAYMENTS_COL_1 + " = " + ID);
    }

    public void deletePayment(int ID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + PAYMENTS_TABLE_NAME + " WHERE " + PAYMENTS_COL_0 + " = " + ID);
    }

    public void updateData(Debt debt, int debtID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + DEBTS_TABLE_NAME + " SET " + DEBTS_COL_1 + " = \'" + debt.getDebtName() + "\', "
                + DEBTS_COL_2 + " = " + debt.getDebtPhone() + ", " + DEBTS_COL_3 + " = " + debt.getDebtAmount()
                        + ", " + DEBTS_COL_4 + " = \'" + debt.getDebtDescription() + "\', " + DEBTS_COL_5 + " = \'"
                        + debt.getDebtType() + "\' WHERE " + DEBTS_COL_0 + " = " + debtID);
    }
}
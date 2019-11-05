package com.banking_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;


public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Konten_master.db";
    public static final String TABLE_NAME = "Konten";
    public static final String TABLE_NAME_BUCHUNGEN = "Buchungen";
    public static final String TABLE_NAME_SETTINGS = "Settings";
    public static final String COL1 = "IBAN";
    public static final String COL2 = "NAME";
    public static final String COL3 = "BANK";
    public static final String COL4 = "SALDO";
    public static final String COL5 = "WAEHRUNG";
    public static final String BUCH_COL1 = "IBAN";
    public static final String BUCH_COL2 = "Buchung";
    public static final String BUCH_COL3 = "Valuta";
    public static final String BUCH_COL4 = "Auftraggeber_Empfaenger";
    public static final String BUCH_COL5 = "Buchungstext";
    public static final String BUCH_COL6 = "Verwendungszweck";
    public static final String BUCH_COL7 = "Saldo";
    public static final String BUCH_COL8 = "Waehrung";
    public static final String BUCH_COL9 = "Betrag";
    public static final String Settings_COL1 = "Saving";
    public static final String Settings_COL3 = "IBAN";



    public static Integer check_db = 0;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (check_db == 0) {
            String createTable = "CREATE TABLE " + TABLE_NAME + " (IBAN TEXT Primary Key, Name  TEXT,  Bank TEXT, Saldo TEXT,Waehrung TEXT )";
            check_db = 1;
            db.execSQL(createTable);
            String sql1 = "insert into " + TABLE_NAME + " (" + COL1 + ", " + COL2 + " , " + COL3 + " ," + COL4 + " , " + COL5 + ") " +
                    "values('DE89 3704 0044 0532 0130 87', 'Girokonto', 'INGDIBA', '1000,00', 'Euro' );";
            String sql2 = "insert into " + TABLE_NAME + " (" + COL1 + ", " + COL2 + " , " + COL3 + " ," + COL4 + " , " + COL5 + ") " +
                    "values('DE78 0310 2350 4440 4037 98', 'Festsparen', 'Sparda Bank', '10000,00', 'Euro' );";
            db.execSQL(sql1);
            db.execSQL(sql2);
            String createTable_Buchungen = "CREATE TABLE " + TABLE_NAME_BUCHUNGEN + " (IBAN TEXT, Buchung  TEXT,  Valuta TEXT, Auftraggeber_Empfaenger TEXT,Buchungstext TEXT, Verwendungszweck TEXT, Saldo TEXT, Waehrung TEXT, Betrag TEXT,PRIMARY KEY (IBAN, Buchung,Valuta,Auftraggeber_Empfaenger,Buchungstext,Verwendungszweck,Saldo,Waehrung,Betrag))";
            db.execSQL(createTable_Buchungen);
            String createTable_Settings = "CREATE TABLE " + TABLE_NAME_SETTINGS + " (Saving TEXT, IBAN TEXT)";
            db.execSQL(createTable_Settings);

        } else {
        }
    }
    public boolean defaultSettings(String saving, String iban) {

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * FROM Settings";
        Cursor data = db.rawQuery(query, null);
        if (data.getCount()==0){
            ContentValues contentValues = new ContentValues();
            contentValues.put(Settings_COL1, saving);
            contentValues.put(Settings_COL3, iban);
            long result = db.insert(TABLE_NAME_SETTINGS, null, contentValues);

            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }
        else{
            return  true;
        }

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME_BUCHUNGEN);
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME_SETTINGS);
        onCreate(db);
    }

    public boolean addSettings(String saving, String Iban) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Settings_COL1, saving);
        contentValues.put(Settings_COL3, Iban);

        long result = db.insert(TABLE_NAME_SETTINGS, null, contentValues);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getSettingsIban() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor I = db.rawQuery("SELECT IBAN FROM  Settings", null);
        return I;
    }

    public Cursor showKonten() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor K = db.rawQuery("SELECT * FROM  Konten", null);
        return K;
    }

    public boolean updateData(String saving, String iban) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Settings_COL1, saving);
        contentValues.put(Settings_COL3, iban);
        SQLiteStatement query_saving = db.compileStatement("Update " + TABLE_NAME_SETTINGS + " set " + Settings_COL1 +" = ?");
        query_saving.bindString(1, saving);
        query_saving.execute();
        SQLiteStatement query_Iban = db.compileStatement("Update " + TABLE_NAME_SETTINGS + " set " + Settings_COL3 +" = ?");
        query_Iban.bindString(1, iban);
        query_Iban.execute();
        db.close();
        return true;
    }

    public boolean addUmsaetze(String iban, String buchung, String valuta, String auftraggeberEmpfaenger, String buchungstext, String verwendungszweck, String saldo, String waehrung, String betrag) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BUCH_COL1, iban);
        contentValues.put(BUCH_COL2, buchung);
        contentValues.put(BUCH_COL3, valuta);
        contentValues.put(BUCH_COL4, auftraggeberEmpfaenger);
        contentValues.put(BUCH_COL5, buchungstext);
        contentValues.put(BUCH_COL6, verwendungszweck);
        contentValues.put(BUCH_COL7, saldo);
        contentValues.put(BUCH_COL8, waehrung);
        contentValues.put(BUCH_COL9, betrag);

        long result = db.insert(TABLE_NAME_BUCHUNGEN, null, contentValues);
        if (result == -1) {
            return false;
        }
        return  true;
    }

    public Cursor showSettings() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor B = db.rawQuery("SELECT * FROM  Settings", null);
        return B;
    }

    public Cursor getData(String iban) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * FROM Buchungen Where " + BUCH_COL1 + " = " +  "'"+iban+"'";;
        Cursor data = db.rawQuery(query, null);
        return  data;
    }
    public Cursor getSaldo(String iban) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select Saldo FROM Buchungen Where " + BUCH_COL1 + " = " +  "'"+iban+"'  order by " + BUCH_COL3 + " "+ "desc limit 1";
        Cursor data = db.rawQuery(query, null);
        return  data;
    }

    public Cursor getDauerauftraege(String iban, String valuta, String datum, String dauerauftrag) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * FROM Buchungen Where " + BUCH_COL1 + " = " +  "'"+iban+"' and " + "(" + BUCH_COL3+ " "+  "between " + "'"+valuta+" 00:00:00"+"'" + " and " +  "('" + datum + " 00:00:00"+"')" + ")" + " and " + "(" + BUCH_COL5 + " like " + "'%Dauerauftrag%'" + "or "+ BUCH_COL5 + " = " + "'"+dauerauftrag+"'"+")" ;
        Cursor data = db.rawQuery(query, null);
        return  data;
    }

    public Cursor getItemData(String valuta, String verwendungszweck,String betrag) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * FROM Buchungen Where " + BUCH_COL2 + " = " +  "'"+valuta+"'"+ " and "  + BUCH_COL6+ " = " + "'"+verwendungszweck+"'" + " and " + BUCH_COL9 + " = "+ "'"+betrag+"'";
        Cursor data = db.rawQuery(query, null);
        return  data;
    }
}
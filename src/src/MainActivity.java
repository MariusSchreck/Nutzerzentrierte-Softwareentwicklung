package com.banking_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper kontenDB;

    private static final String TAG = "Settings";

    ArrayList<String> AistData_Konten_IBAN = new ArrayList<>();
    ArrayList<String> AlistData_Konten_NAME = new ArrayList<>();
    ArrayList<String> AlistData_Konten_Bank = new ArrayList<>();
    ArrayList<String> AlistData_Konten_Saldo = new ArrayList<>();
    ArrayList<String> AlistData_Konten_Info = new ArrayList<>();

    ArrayList<String> listData_Datum = new ArrayList<>();
    ArrayList<String> listData_Auftraggeber_Empfaenger = new ArrayList<>();
    ArrayList<String> listData_Verwendungszweck = new ArrayList<>();
    ArrayList<String> listData_Betrag = new ArrayList<>();
    ArrayList<String> listData_Saldo_last = new ArrayList<>();
    ArrayList<String> listData_Dauerauftraege = new ArrayList<>();
    ArrayList<String> listData_DauerauftraegeVerwendungszweck = new ArrayList<>();

    Button btnCsvSync;
    TextView etVerfuegung;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String iban_nr = "";

        kontenDB = new DatabaseHelper(this);
        etVerfuegung = (TextView) findViewById(R.id.etVerfuegung);
        btnCsvSync = (Button) findViewById(R.id.btnInsertcsv);

        SyncCsv();
        SetSettings();
        ShowKonten();
        AddData();

        try {
            Cursor cursorIban =  kontenDB.getSettingsIban();
            String sIban ="n.v";
            while (cursorIban.moveToNext()) {
                sIban = cursorIban.getString(0);
            }
            CalcVerfuegung(sIban);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SyncCsvButton();

    }

    public void SetSettings()  {
        String saving = "100";
        String iban = "DE89 3704 0044 0532 0130 87";
        kontenDB.defaultSettings(saving, iban);
    }
    public void CalcVerfuegung(final String iban) throws ParseException {
        String year="", monthString="", dateString_to="", dateString_from="",day="",totalDaysOfMonth="",saving="n.v";
        Timestamp timestamp = new Timestamp(new Date().getTime());
        String from = timestamp.toString().substring(0,10);
        //Extract year, month and day from Timestamp
        Integer from_year = Integer.parseInt(from.substring(0,4));
        Integer month = Integer.parseInt(from.substring(5,7));
        day = from.substring(8,10);

        switch (month) {
            case 1:
                from_year --;
                totalDaysOfMonth = "31";
                monthString = "12";
                year = from_year.toString();
                break;
            case 2:
                totalDaysOfMonth ="31";
                monthString = "01";
                break;
            case 3:
                from_year --;
                if(monthString.equals("02")) {
                    if (Integer.parseInt(year) % 4 == 0 && (Integer.parseInt(year) % 100 != 0 || Integer.parseInt(year) % 400 == 0)) {
                        totalDaysOfMonth = "29";

                    } else {
                        totalDaysOfMonth = "28";
                    }
                }
                monthString = "02";
                year = from_year.toString();
                break;
            case 4:
                totalDaysOfMonth = "31";
                monthString = "03";
                break;
            case 5:
                totalDaysOfMonth = "30";
                monthString = "04";
                break;
            case 6:
                totalDaysOfMonth = "31";
                monthString = "05";
                break;
            case 7:
                totalDaysOfMonth = "30";
                monthString = "06";
                break;
            case 8:
                totalDaysOfMonth = "31";
                monthString = "07";
                break;
            case 9:
                totalDaysOfMonth = "31";
                monthString = "08";
                break;
            case 10:
                totalDaysOfMonth = "30";
                monthString = "09";

                break;
            case 11:
                totalDaysOfMonth = "31";
                monthString = "10";
                break;
            case 12:
                totalDaysOfMonth = "30";
                monthString = "11";
                break;
            default:
                monthString = "Invalid month";
                break;
        }
        dateString_from = year+"-"+monthString+"-"+day;
        if(monthString == "12"){
            dateString_to =  year+"-"+"01"+"-"+totalDaysOfMonth;
        }
        else {
            dateString_to = year + "-" + monthString + "-" + totalDaysOfMonth;
        }

        Cursor bookings = kontenDB.getData(iban);
        while (bookings.moveToNext()) {

            listData_Datum.add(bookings.getString(1));
            listData_Auftraggeber_Empfaenger.add(bookings.getString(3));
            listData_Verwendungszweck.add(bookings.getString(5));
            listData_Betrag.add(bookings.getString(8));
        }
        Cursor lastSaldo = kontenDB.getSaldo(iban);

        while (lastSaldo.moveToNext()) {
            listData_Saldo_last.add(lastSaldo.getString(0));
        }
        final String aktKonto = listData_Saldo_last.get(0);
        final String dauerauftrag = "Dauerauftrag";
        Cursor dauerauftraege = kontenDB.getDauerauftraege(iban,dateString_from,dateString_to,dauerauftrag);
        while (dauerauftraege.moveToNext()) {
            listData_Dauerauftraege.add(dauerauftraege.getString(8));
            listData_DauerauftraegeVerwendungszweck.add(dauerauftraege.getString(5));
        }
        double sum = 0;
        double haben = 0;
        double soll = 0;

        Cursor settingsSaving = kontenDB.showSettings();
        while (settingsSaving.moveToNext()){
            saving = settingsSaving.getString(0);
        }

        for(Integer i = 0;i<listData_Dauerauftraege.size();i++){
            String s_h = listData_Dauerauftraege.get(i);
            String s_hTemp = s_h.substring(0,1);
            if(s_hTemp.equals("+")){
                s_h = s_h.replace("+","");
                haben = haben + Double.parseDouble(s_h);
            }
            if(s_hTemp.equals("-")){
                s_h = s_h.replace("-", "");
                soll = soll + Double.parseDouble(s_h);
            }

        }
        sum = haben - soll - Double.parseDouble(saving);
        final Double ergebnis = Double.parseDouble(aktKonto) + sum;
        final String finalSaving = saving;
        final String finalDateString_from = dateString_from;
        final String finalDateString_to = dateString_to;

        ImageButton infoButton = (ImageButton) findViewById(R.id.infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StringBuffer buffer = new StringBuffer();

                buffer.append("Aktueller Kontostand:" + "\t" + aktKonto + " €\n\n");
                listData_Dauerauftraege.clear();
                listData_DauerauftraegeVerwendungszweck.clear();
                Cursor dauerauftraege = kontenDB.getDauerauftraege(iban, finalDateString_from, finalDateString_to,dauerauftrag);

                while (dauerauftraege.moveToNext()) {
                    listData_Dauerauftraege.add(dauerauftraege.getString(8));
                    listData_DauerauftraegeVerwendungszweck.add(dauerauftraege.getString(5));
                }

                for (int i = 0; i < listData_DauerauftraegeVerwendungszweck.size(); i++){
                    buffer.append(listData_DauerauftraegeVerwendungszweck.get(i) + "\t\t" + listData_Dauerauftraege.get(i) + " €\n");
                }

                buffer.append("\nReserve" + "\t-" + finalSaving + " €\n");
                DecimalFormat f = new DecimalFormat("0.##");
                String erg = f.format(ergebnis)+" €";
                buffer.append("\nVerfügungsbetrag:" + "\t" + erg + "\n");


                display("Einzelansicht zum Konto:", buffer.toString());
            }
        });

        DecimalFormat f = new DecimalFormat("0.##");
        String erg = f.format(ergebnis)+" €";
        etVerfuegung.setText(erg);
    }

    public void AddData() {
        ImageButton settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
            }
        });

    }

    public void display(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void SyncCsv() {

        InputStream is = getResources().openRawResource(R.raw.umsaetze_giro);
        BufferedReader bf = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String booking, valuta, auftraggeberEmpfaenger, bookingnote, verwendungszweck, saldo, currency, value;
        String line = "";

        try {
            for (int i = 0; i < 3; i++) {
                String line_skip = bf.readLine();
            }
            String iban = bf.readLine();
            String iban_array[] = iban.split(",");
            String _iban = iban_array[1];
            for (int i = 0; i < 9; i++) {
                String line_skip = bf.readLine();
            }

            while ((line = bf.readLine()) != null) {

                String[] values = line.split(",");
                booking = values[0];
                valuta = values[1];
                auftraggeberEmpfaenger = values[2];
                bookingnote = values[3];
                verwendungszweck = values[4];
                saldo = values[5];
                currency = values[6];
                value = values[7];

                kontenDB.addUmsaetze(_iban, booking, valuta, auftraggeberEmpfaenger, bookingnote, verwendungszweck, saldo, currency, value);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream is_s = getResources().openRawResource(R.raw.umsaetze_sparen);
        BufferedReader bf_s = new BufferedReader(new InputStreamReader(is_s, Charset.forName("UTF-8")));

        try {
            for (int i = 0; i < 3; i++) {
                String line_skip = bf_s.readLine();
            }
            String iban = bf_s.readLine();
            String iban_array[] = iban.split(",");
            String _iban = iban_array[1];
            for (int i = 0; i < 9; i++) {
                String line_skip = bf_s.readLine();
            }

            while ((line = bf_s.readLine()) != null) {

                String[] values = line.split(",");
                booking = values[0];
                valuta = values[1];
                String Valuta_Tag = valuta.substring(0,2);
                String Valuta_Monat = valuta.substring(3,6);
                String Valuta_Jahr = valuta.substring(6,10);
                valuta = Valuta_Jahr+"-"+Valuta_Monat+Valuta_Tag;
                valuta = valuta.replace('.','-');
                auftraggeberEmpfaenger = values[2];
                bookingnote = values[3];
                verwendungszweck = values[4];
                saldo = values[5];
                currency = values[6];
                value = values[7];

                kontenDB.addUmsaetze(_iban, booking, valuta, auftraggeberEmpfaenger, bookingnote, verwendungszweck, saldo, currency, value);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void SyncCsvButton() {
        btnCsvSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputStream is = getResources().openRawResource(R.raw.umsaetze_giro_neu);
                BufferedReader bf = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String booking, valuta, auftraggeberEmpfaenger, bookingnote, verwendungszweck, saldo, currency, value;

                String line = "";

                try {
                    for (int i = 0; i < 3; i++) {
                        String line_skip = bf.readLine();
                    }
                    String IBAN = bf.readLine();
                    String iban_array[] = IBAN.split(",");
                    String iban = iban_array[1];
                    for (int i = 0; i < 9; i++) {
                        String line_skip = bf.readLine();
                    }

                    while ((line = bf.readLine()) != null) {

                        String[] values = line.split(",");
                        booking = values[0];
                        valuta = values[1];

                        auftraggeberEmpfaenger = values[2];
                        bookingnote = values[3];
                        verwendungszweck = values[4];
                        saldo = values[5];
                        currency = values[6];
                        value = values[7];

                        kontenDB.addUmsaetze(iban, booking, valuta, auftraggeberEmpfaenger, bookingnote, verwendungszweck, saldo, currency, value);
                    } Toast.makeText(MainActivity.this, "Successfully Updated CSV", Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(MainActivity.this, MainActivity.class);

                startActivity(intent);
            }



        });
    }

    public void ShowKonten() {

        kontenDB = new DatabaseHelper(this);
        Cursor data_Konten = kontenDB.showKonten();

        AistData_Konten_IBAN.add("IBAN");
        AlistData_Konten_NAME.add("Name");
        AlistData_Konten_Bank.add("Bank");
        AlistData_Konten_Saldo.add("Saldo");
        AlistData_Konten_Info.add("Info");

        TableLayout tl = (TableLayout) findViewById(R.id.table_konten);
        int counter = 1;

        while (data_Konten.moveToNext()) {

            // insert new row
            TableRow newRowInKonto = (TableRow) getLayoutInflater().inflate(R.layout.row_layout, null); // Layout einer Zeile in der Tabelle

            // textView price / saldo
            TextView price_textView = newRowInKonto.findViewById(R.id.textView1);
            Cursor saldo = kontenDB.getSaldo(data_Konten.getString(0));
            //String price = data_Konten.getString(3);
            while(saldo.moveToNext()) {
                String price = saldo.getString(0);
                price_textView.setText(price + " €");
            }

            // textView title / Name
            TextView title_textView = newRowInKonto.findViewById(R.id.textView1_1);
            title_textView.setText(data_Konten.getString(1));

            // textView date / IBAN
            TextView date_textView = newRowInKonto.findViewById(R.id.textView1_2);
            date_textView.setText(data_Konten.getString(0));

            // id to identify / ID
            newRowInKonto.setId(counter);
            tl.addView(newRowInKonto);

            newRowInKonto.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String Name = AlistData_Konten_NAME.get(v.getId());
                    String IBAN = AistData_Konten_IBAN.get(v.getId());

                    Intent intent = new Intent(MainActivity.this, ListDataBuchungen.class);

                    Bundle extras = new Bundle();
                    extras.putString("iban",IBAN);
                    extras.putString("name",Name);
                    intent.putExtras(extras);

                    //   intent.putExtra("iban",IBAN);
                    startActivity(intent);
                }
            });

            counter++;

            AistData_Konten_IBAN.add(data_Konten.getString(0));
            AlistData_Konten_NAME.add(data_Konten.getString(1));
            AlistData_Konten_Bank.add(data_Konten.getString(2));
            AlistData_Konten_Saldo.add(data_Konten.getString(3) + " EURO");
            AlistData_Konten_Info.add("Info");

        }
    }

}

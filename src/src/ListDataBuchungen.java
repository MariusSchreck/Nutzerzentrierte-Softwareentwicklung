package com.banking_app;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.jar.Attributes;

public class ListDataBuchungen extends AppCompatActivity {

    private static final String TAG = "ListDataBuchungen";

    DatabaseHelper mDatabaseHelper;

    ArrayList<String> listData_Datum = new ArrayList<>();
    ArrayList<String> listData_Auftraggeber_Empfaenger = new ArrayList<>();
    ArrayList<String> listData_Verwendungszweck = new ArrayList<>();
    ArrayList<String> listData_Betrag = new ArrayList<>();
    ArrayList<String> listData_item = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data_buchungen);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        String iban_nr = extras.getString("iban");
        String name = extras.getString("name");


        mDatabaseHelper = new DatabaseHelper(this);
        populateListView(iban_nr);

        getSupportActionBar().setTitle(name);  // provide compatibility to all the versions

    }

    private void populateListView(String iban_nr) {


        Cursor data = mDatabaseHelper.getData(iban_nr);

        listData_Datum.add("Valuta");
        listData_Auftraggeber_Empfaenger.add("Von");
        listData_Verwendungszweck.add("Betreff");
        listData_Betrag.add("Betrag");
        listData_item.add("Info");




        TableLayout tl = (TableLayout) findViewById(R.id.table);
        int counter = 1;

        while (data.moveToNext()) {

            // insert new row
            TableRow newRowInKonto = (TableRow) getLayoutInflater().inflate(R.layout.row_layout, null); // Layout einer Zeile in der Tabelle

            // textView price / Betrag
            TextView price_textView = newRowInKonto.findViewById(R.id.textView1);
            String price = data.getString(8);

            int number = Integer.parseInt(price); // price = -100
            if(number <= 0) { // Preis bei Negativ rot, bei Positiv grau
                price_textView.setTextColor(getResources().getColor(R.color.colorNegativeNumberRed));
            }
            price_textView.setText(Integer.toString(number) + " €");

            // textView title / Verwendungszweck
            TextView title_textView = newRowInKonto.findViewById(R.id.textView1_1);
            title_textView.setText(data.getString(5));

            // textView date / Datum
            TextView date_textView = newRowInKonto.findViewById(R.id.textView1_2);
            date_textView.setText(data.getString(1));

            // id to identify / ID
            newRowInKonto.setId(counter);

            tl.addView(newRowInKonto);

            newRowInKonto.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String val = listData_Datum.get(v.getId());
                    String ver = listData_Verwendungszweck.get(v.getId());
                    String be = listData_Betrag.get(v.getId());

                    Cursor data_item = mDatabaseHelper.getItemData(val,ver,be);

                    StringBuffer buffer = new StringBuffer();
                    while (data_item.moveToNext()) {
                        buffer.append("IBAN: " + data_item.getString(0) + "\n");
                        buffer.append("Buchung: " + data_item.getString(1) + "\n");
                        buffer.append("Valuta: " + data_item.getString(2) + "\n");
                        buffer.append("Auftraggeber_Empfaenger: " + data_item.getString(3) + "\n");
                        buffer.append("Buchungstext: " + data_item.getString(4) + "\n");
                        buffer.append("Verwendungszweck: " + data_item.getString(5) + "\n");
                        buffer.append("Saldo: " + data_item.getString(6) + "\n");
                        buffer.append("Waehrung: " + data_item.getString(7) + "\n");
                        buffer.append("Betrag: " + data_item.getString(8) + "\n");

                        _display("Einzelansicht zur Buchung:", buffer.toString());
                    }


                }
            });

            counter++;

            listData_Datum.add(data.getString(1));
            listData_Auftraggeber_Empfaenger.add(data.getString(3));
            listData_Verwendungszweck.add(data.getString(5));
            listData_Betrag.add(data.getString(8));
            listData_item.add("Info");
        }
}

    public void _display(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }


}
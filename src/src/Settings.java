package com.banking_app;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;


public class Settings extends AppCompatActivity {

    private static final String TAG = "Settings";

    DatabaseHelper kontenDB;
    Button btnCsvSync;
    EditText etSaving;
    Button btnSave;
    String _tempDate, _tempSaving;

    ArrayList<String> AistData_Konten_IBAN = new ArrayList<>();
    ArrayList<String> AlistData_Konten_NAME = new ArrayList<>();
//    ArrayList<String> AIban_Nr = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle("Einstellungen");  // provide compatibility to all the versions

        kontenDB = new DatabaseHelper(this);
        btnSave = (Button) findViewById(R.id.btnsave);
        etSaving = (EditText) findViewById(R.id.etSaving);
        btnCsvSync = (Button) findViewById(R.id.btnInsertcsv);

        ViewSettings();
        Save();

    }

    public void ViewSettings() {

        Cursor settingsSaving = kontenDB.showSettings();
        String saving = "n.v", date = "n.v";

        while (settingsSaving.moveToNext()) {
            saving = settingsSaving.getString(0);
            date = settingsSaving.getString(1);
        }
        etSaving.setText(saving);
        _tempSaving = saving;
        _tempDate = date;

        Cursor data_Konten = kontenDB.showKonten();
        RadioGroup radioGroup = findViewById(R.id.radiogroup);

        int counter = 0;
        while (data_Konten.moveToNext()) {
            AistData_Konten_IBAN.add(data_Konten.getString(0));
            AlistData_Konten_NAME.add(data_Konten.getString(1));
            //  RadioButton radioButton = findViewById(R.id.konto1_radioButton);
            RadioButton radioButton1 = new RadioButton(this);
            radioButton1.setPadding(0, 0, 0, 10);
            radioButton1.setText(data_Konten.getString(1) + "\n" + data_Konten.getString(0));
            radioButton1.setId(counter);
            radioGroup.addView(radioButton1);
            counter++;
            String txt = "n.v";
            Cursor t = kontenDB.getSettingsIban();
            while (t.moveToNext()) {
                txt = t.getString(0);
            }
            String radio = radioButton1.getText().toString();
            radio = radio.substring(10);
            String s = radio.substring(0, 1);
            if (s.equals("\n")) {
                radio = radio.substring(1);
            }
            if (txt.equals(radio)) {
                radioButton1.setChecked(true);
            }
        }
    }

    public void Save() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String saving = etSaving.getText().toString();
                String iban = "n.v";
                // Funktionierendes Beispielcoding für die IBAN-Nr des ausgewählten Kontos
                RadioGroup radioButtonGroup = findViewById(R.id.radiogroup);
                int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();

                if (radioButtonID != -1) { // etwas ausgewählt
                    View radioButton = radioButtonGroup.findViewById(radioButtonID);
                    int idx = radioButtonGroup.indexOfChild(radioButton);
                    RadioButton r = (RadioButton) radioButtonGroup.getChildAt(idx);
                    String selectedtext[];
                    selectedtext = r.getText().toString().split("\n");
                    iban = selectedtext[1]; // IBAN des ausgewählten Kontos
                } else {
                    // Nichts ausgewählt
                    // verknüpfen mit dem unteren Coding, sodass wenn nichts ausgewählt auch die activity sich nicht ändert
                }

                if (_tempSaving.equals("n.v") && _tempDate.equals("n.v")) {
                    boolean insertSettings = kontenDB.addSettings(saving, iban);
                    if (insertSettings == true) {
                        Toast.makeText(Settings.this, "Daten erfolgreich hinzugefuegt!", Toast.LENGTH_LONG).show();
                        // change Activity / same below
                        Intent intent = new Intent(Settings.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Settings.this, "Ups, Da hat etwas nicht geklappt :(.", Toast.LENGTH_LONG).show();
                    }
                } else {

                    int temp = etSaving.getText().toString().length();
                    if(isNumeric(etSaving.getText().toString())){
                        if (temp > 0) {
                            boolean insertSettings = kontenDB.updateData(saving, iban);
                            if (insertSettings == true) {
                                Toast.makeText(Settings.this, "Daten erfolgreich aktualisiert!", Toast.LENGTH_LONG).show();
                                // change Activity / same above
                                Intent intent = new Intent(Settings.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(Settings.this, "Da hat etwas nicht geklappt :(.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    else {
                        Toast.makeText(Settings.this, "Bitte eine Ganzzahl eingeben :(.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    public static boolean isNumeric(String strNum) {
        try {
            int i = Integer.parseInt(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }
}

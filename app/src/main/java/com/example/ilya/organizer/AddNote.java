package com.example.ilya.organizer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddNote extends AppCompatActivity {
    EditText header;
    EditText text;
    CheckBox check;
    DatePicker datePicker;
    TimePicker timePicker;
    Button submit;
    Button delete;

    private int getUsId;
    private int getId;
    String getDate;

    private boolean edit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        Intent intent = getIntent();
        getUsId = intent.getIntExtra("id", -1);
        getId = intent.getIntExtra("idNote", -1);

        if(getId != -1)
            edit = true;

        header =(EditText) findViewById(R.id.header);
        text =(EditText) findViewById(R.id.noteText);

        datePicker = (DatePicker) findViewById(R.id.datePicker);
        timePicker = (TimePicker) findViewById(R.id.timePicker);

        submit =(Button) findViewById(R.id.addButton);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Add().execute();
            }
        });

        check =(CheckBox) findViewById(R.id.check);
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(check.isChecked()){
                    datePicker.setVisibility(View.VISIBLE);
                    timePicker.setVisibility(View.VISIBLE);
                }
                else {
                    datePicker.setVisibility(View.GONE);
                    timePicker.setVisibility(View.GONE);
                }
            }
        });

        if(edit == true){
            delete = (Button) findViewById(R.id.deleteButton);
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    del();
                }
            });

            submit.setText(R.string.edit);

            getDate = intent.getStringExtra("date");
            if(getDate.length() > 0) {
                check.setChecked(true);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(parseDate());
                datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
                timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
            }

            text.setText(intent.getStringExtra("text"));
            header.setText(intent.getStringExtra("header"));
        }
    }

    private void del(){
        new Del().execute();
    }

    private Date parseDate(){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = null;
        try{
            date = df.parse(getDate);
            return date;
        }
        catch (ParseException ex){
            Snackbar.make(findViewById(R.id.addLayot), ex.toString(), Snackbar.LENGTH_INDEFINITE)
                    .show();
            return date;
        }catch (Exception ex){
            Snackbar.make(findViewById(R.id.addLayot), ex.toString(), Snackbar.LENGTH_INDEFINITE)
                    .show();
            return date;
        }
    }

    private String getData(int index){
        switch (index){
            case 0:
                if(check.isChecked()) {
                    int[] massDt = new int[5];
                    massDt[0] = datePicker.getYear();
                    massDt[1] = datePicker.getMonth() + 1;
                    massDt[2] = datePicker.getDayOfMonth();
                    massDt[3] = timePicker.getCurrentHour();
                    massDt[4] = timePicker.getCurrentMinute();
                    return massDt[0] + "-" + massDt[1] + "-" + massDt[2] + " " + massDt[3] + ":" + massDt[4];
                }
                else return "";
            case 1:
                if(header.getText().length() == 0){
                    header.setError(getString(R.string.error_field_required));
                    return null;
                }
                return header.getText().toString();
            case 2:
                return text.getText().toString();
            default:
                return null;
        }
    }

    class Add extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String urlDate = URLEncoder.encode(getData(0));
                String urlName = URLEncoder.encode(getData(1));
                String urlDesc = URLEncoder.encode(getData(2));
                String request = "http://www.organizer.esy.es/addrem.php?userid=" + getUsId + "&time=" + urlDate + "&name=" + urlName + "&desc=" + urlDesc;
                URL url = new URL(request);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept-Charset", "utf-8");
                InputStream response = connection.getInputStream();

                if(edit == true)
                    del();
                else
                    finish();
            }
            catch (IOException ex) {
                Snackbar.make(findViewById(R.id.addLayot), R.string.error_network, Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
            catch (Exception ex) {
                Snackbar.make(findViewById(R.id.addLayot), ex.toString(), Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
            return null;
        }
    }

    class Del extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String request = "http://www.organizer.esy.es/delrem.php?id=" + getId;
                URL url = new URL(request);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept-Charset", "utf-8");
                InputStream response = connection.getInputStream();
                finish();
            }
            catch (Exception ex) {
                Snackbar.make(findViewById(R.id.addLayot), ex.toString(), Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
            return null;
        }
    }
}

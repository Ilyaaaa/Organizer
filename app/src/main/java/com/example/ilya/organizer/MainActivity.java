package com.example.ilya.organizer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private String[][] massrem;
    private int getUsId;
    private int notifeId = 1;

    View progressView;
    ListView listView;

    NotificationManager nm;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = getIntent();
        getUsId = intent.getIntExtra("id", -1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressView = findViewById(R.id.main_progress);
        listView = (ListView) findViewById(R.id.content_list);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAddNote();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        showProgress();
        new ImportReminders().execute();
        addElements();
    }

    private void showProgress(){progressView.setVisibility(View.VISIBLE);}
    private void hideProgress(){progressView.setVisibility(View.GONE);}

    private void startAddNote(){
        Intent addIntent = new Intent(this, AddNote.class);
        addIntent.putExtra("id", getUsId);
        startActivity(addIntent);
    }

    void sendNotif(String id, String date, String header, String text) {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notif;

        Intent intent = new Intent(this, AddNote.class);
        intent.putExtra("id",getUsId);
        intent.putExtra("idNote", Integer.parseInt(id));
        intent.putExtra("date",date);
        intent.putExtra("header",header);
        intent.putExtra("text",text);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        builder.setCategory(Notification.CATEGORY_REMINDER);
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setContentTitle(header);
        builder.setContentText(text);
        builder.setSmallIcon(R.drawable.ic_announcement_black_24dp);
        builder.setContentIntent(pIntent);
        builder.build();

        notif = builder.getNotification();

        nm.notify(notifeId, notif);
        notifeId++;
    }

    private void addElements() {
        while (1 == 1) {
            if (progressView.getVisibility() == View.GONE) {
                try {
                    String[] massNames = new String[massrem.length];
                    for (int i = 0; i < massrem.length; i++)
                        massNames[i] = massrem[i][2];
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, massNames);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            newIntent(massrem[position][0], massrem[position][1], massrem[position][2], massrem[position][3]);
                        }
                    });
                } catch (Exception ex) {}
                break;
            }
        }
    }

    private void newIntent(String id, String date, String header, String text){
        Intent intent = new Intent(this, AddNote.class);
        intent.putExtra("id",getUsId);
        intent.putExtra("idNote", Integer.parseInt(id));
        intent.putExtra("date",date);
        intent.putExtra("header",header);
        intent.putExtra("text",text);
        startActivity(intent);
    }

    class ImportReminders extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String url = "http://www.organizer.esy.es/reminders.php?userid=" + getUsId;
                URL us = new URL(url);
                URLConnection connect = us.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                String json = in.readLine();

                JSONParser parser = new JSONParser();
                Object obj = parser.parse(json);
                JSONArray jarr;
                int size = 0;
                try {
                    jarr = (JSONArray) obj;
                    size = jarr.size();
                }catch (Exception ex){
                    jarr = null;
                }
                Timer messTim = new Timer();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Calendar calendar = Calendar.getInstance();
                Calendar current = Calendar.getInstance();
                Date dt = null;
                if(size != 0) {
                    massrem = new String[size][4];

                    for (int i = 0; i < size; i++) {
                        String line = jarr.get(i).toString();
                        obj = parser.parse(line);
                        JSONObject jline = (JSONObject) obj;
                        massrem[i][0] = jline.get("id").toString();
                        massrem[i][1] = jline.get("time").toString();
                        massrem[i][2] = jline.get("name").toString();
                        massrem[i][3] = jline.get("description").toString();

                        if (massrem[i][1].length() != 0) {
                            try {
                                dt = df.parse(massrem[i][1]);
                            } catch (ParseException ex) {
                            }

                            calendar.setTime(dt);
                            current.setTime(new Date());
                            final int num = i;
                            if (calendar.getTime().after(current.getTime())) {
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        sendNotif(massrem[num][0], massrem[num][1], massrem[num][2], massrem[num][3]);
                                    }
                                };
                                messTim.schedule(task, calendar.getTime());
                            }
                        }
                    }
                }
                in.close();
                hideProgress();
            } catch (Exception ex) {}
            return null;
        }
    }
}

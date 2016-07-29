package com.example.ilya.organizer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

public class ConfirmActivity extends AppCompatActivity {
    private EditText codeView;
    private View progressView;
    private int getCode;
    private String getMail;
    private String getPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        Intent intent = getIntent();
        getMail = intent.getStringExtra("email");
        getPass = intent.getStringExtra("pass");
        getCode = intent.getIntExtra("code", -1);

        codeView = (EditText) findViewById(R.id.confirmCode);
        progressView = findViewById(R.id.confirm_progress);

        Button confirmBut = (Button) findViewById(R.id.confirmButton);
        confirmBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeCompare();
            }
        });
    }

    private void codeCompare(){
        if(Integer.parseInt(codeView.getText().toString()) == getCode){
            new AddUser().execute();
        }
        else {
            codeView.setError(getString(R.string.error_code));
        }
    }

    class AddUser extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String request = "http://www.organizer.esy.es/adduser.php?mail=" + getMail + "&password=" + getPass;
                URL url = new URL(request);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept-Charset", "utf-8");
                InputStream response = connection.getInputStream();
            }
            catch (Exception ex) {
                Snackbar.make(findViewById(R.id.confirmLayout),R.string.error_network,Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
            return null;
        }
    }
}

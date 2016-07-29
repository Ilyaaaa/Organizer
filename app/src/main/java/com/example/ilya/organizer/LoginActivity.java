package com.example.ilya.organizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class LoginActivity extends AppCompatActivity {
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CheckBox check;

    private String[][] massus = null;
    private int code;

    private SharedPreferences pref;
    private final String keyMail = "mail";
    private final String keyPassword = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        check = (CheckBox) findViewById(R.id.member);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!attemptLogin()){loginCheck();}
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        new ImportUsers().execute();

        if(checkVal()){
            check.setChecked(true);
            loadVal();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new ImportUsers().execute();
    }

    private void showProgress(){mProgressView.setVisibility(View.VISIBLE);}
    private void hideProgress(){mProgressView.setVisibility(View.GONE);}

    private boolean attemptLogin() {
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        if(!email.contains("@")){
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if(!cancel)
            if (password.length() < 4) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }

        return cancel;
    }

    private void loginCheck(){
        try {
            boolean checkMail = false;
            int n = 0;

            for(int i = 0; i < massus.length; i++){
                if(mEmailView.getText().toString().compareToIgnoreCase(massus[i][0]) == 0){
                    n = i;
                    checkMail = true;
                    break;
                }
            }

            if(checkMail == true){
                if(mPasswordView.getText().toString().equals(massus[n][1])){
                    saveVal();

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("id",Integer.parseInt(massus[n][2]));
                    startActivity(intent);
                }
                else{
                    mPasswordView.getText().clear();
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                }
            }
            else {
                Random rand = new Random(System.currentTimeMillis());
                code = rand.nextInt(1099999) - 100000;
                new SendMail().execute();
                Intent inetent = new Intent(this, ConfirmActivity.class);
                inetent.putExtra("email", mEmailView.getText().toString());
                inetent.putExtra("pass", mPasswordView.getText().toString());
                inetent.putExtra("code", code);
                startActivity(inetent);
            }
        }
        catch (Exception ex){}
    }

    private void saveVal(){
        pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(keyMail, mEmailView.getText().toString());
        editor.putString(keyPassword,mPasswordView.getText().toString());
        editor.commit();
    }

    private boolean checkVal(){
        pref = getPreferences(MODE_PRIVATE);
        if(pref.getString(keyMail,"") != "")
            return true;
        else return false;
    }

    private void loadVal(){
        pref = getPreferences(MODE_PRIVATE);
        mEmailView.setText(pref.getString(keyMail,""));
        mPasswordView.setText((pref.getString(keyPassword,"")));
    }

    private void importUsers() {
        try {
            URL us = new URL("http://www.organizer.esy.es/users.php");
            URLConnection connect = us.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            String json = in.readLine();

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONArray jarr = (JSONArray) obj;

            massus = new String[jarr.size()][3];

            for (int i = 0; i < jarr.size(); i++) {
                String line = jarr.get(i).toString();
                obj = parser.parse(line);
                JSONObject jline = (JSONObject) obj;
                massus[i][0] = jline.get("mail").toString();
                massus[i][1] = jline.get("password").toString();
                massus[i][2] = jline.get("id").toString();
            }
            in.close();
        } catch (Exception ex) {
            Snackbar.make(findViewById(R.id.loginLayout), R.string.error_network, Snackbar.LENGTH_INDEFINITE)
                    .show();
        }
    }

    private String getMail(){
        return mEmailView.getText().toString();
    }

    class ImportUsers extends AsyncTask<Void, Void, Void>{
        protected Void doInBackground(Void... params) {
            importUsers();
            return null;
        }
    }

    class SendMail extends AsyncTask<Void, Void, Void>{
        protected Void doInBackground(Void... params){
            String messageText = URLEncoder.encode("Hello, user! \nYou just try register in my service. If you still want to do this, enter this code to field: " + code + "\nGood luck.");
            try {
                String request = "http://www.organizer.esy.es/sendmail.php?email=" + getMail() + "&subj=Registration&message=" + messageText;
                URL url = new URL(request);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept-Charset", "utf-8");
                InputStream response = connection.getInputStream();
            }
            catch (Exception ex) {
                Snackbar.make(findViewById(R.id.confirmLayout), R.string.error_network, Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
            return null;
        }
    }
}


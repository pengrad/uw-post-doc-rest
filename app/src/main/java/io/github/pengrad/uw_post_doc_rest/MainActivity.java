package io.github.pengrad.uw_post_doc_rest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String IS_LOGIN = "isLogin";
    public static final String LOGIN = "login";
    public static final String PASS = "pass";
    private EditText mEditLogin;
    private EditText mEditPass;
    private TextView mTextLoginStatus;
    private TextView mTextStatus;

    private String mLogin;
    private View mLayoutLogin;
    private View mLayoutRes;
    private boolean isLogin;

    private ApiManager.Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = ApiManager.createApi();

        mLayoutLogin = findViewById(R.id.layoutLogin);
        mLayoutRes = findViewById(R.id.layoutResult);

        mEditLogin = (EditText) findViewById(R.id.editLogin);
        mEditPass = (EditText) findViewById(R.id.editPass);

        mTextLoginStatus = (TextView) findViewById(R.id.textLoginStatus);
        mTextStatus = (TextView) findViewById(R.id.textStatus);

        findViewById(R.id.buttonLogin).setOnClickListener(this);
        findViewById(R.id.buttonLogout).setOnClickListener(this);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isLogin = preferences.getBoolean(IS_LOGIN, false);
        mLogin = preferences.getString(LOGIN, "");
        String pass = preferences.getString(PASS, "");

        processLogin(isLogin, mLogin, pass, false);

        handleIntent();
    }

    private void handleIntent() {
        if (!isLogin) {
            Toast.makeText(this, "You should login fist", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        String fileType = "file";
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("application/pdf".equals(type)) {
                fileType = "pdf";
            } else if (type.startsWith("image/")) {
                fileType = "image";
            }

            if (intent.getData() != null && intent.getData().getPath() != null) {
                String path = intent.getData().getPath();
                updateShareStatus("Uploading some " + fileType + " from " + path + " to API");
            } else {
                updateShareStatus("Uploading some " + fileType + " to API");
            }

            api.sendData("file", new Callback<JsonElement>() {
                @Override
                public void success(JsonElement jsonElement, Response response) {
                    updateShareStatus(mTextStatus.getText() + "\nResponse: " + jsonElement.toString());
                }

                @Override
                public void failure(RetrofitError error) {
                    updateShareStatus(mTextStatus.getText() + "\nResponse: error occurred");
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonLogin) {
            String login = mEditLogin.getText().toString();
            String pass = mEditPass.getText().toString();
            if (TextUtils.isEmpty(login) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Please input login and password", Toast.LENGTH_SHORT).show();
            } else {
                processLogin(true, login, pass, true);
            }

        } else if (v.getId() == R.id.buttonLogout) {
            processLogin(false, "", "", true);
        }
    }

    private void processLogin(boolean isLogin, String login, String pass, boolean save) {
        if (save) {
            saveLogin(isLogin, login, pass);
        }
        if (isLogin) {
            mLogin = login;
            mEditPass.setText("");
            mTextLoginStatus.setText("Logged as " + login);
            updateShareStatus("");
            mLayoutLogin.setVisibility(View.GONE);
            mLayoutRes.setVisibility(View.VISIBLE);
        } else {
            mLogin = "";
            mLayoutRes.setVisibility(View.GONE);
            mLayoutLogin.setVisibility(View.VISIBLE);
        }
    }

    private void saveLogin(boolean isLogin, String login, String pass) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit()
                .putBoolean(IS_LOGIN, isLogin)
                .putString(LOGIN, login)
                .putString(PASS, pass)
                .apply();
    }

    private void updateShareStatus(String text) {
        mTextStatus.setText(text);
    }
}

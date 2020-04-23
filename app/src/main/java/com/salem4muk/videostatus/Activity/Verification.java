package com.salem4muk.videostatus.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.salem4muk.videostatus.R;
import com.salem4muk.videostatus.Util.API;
import com.salem4muk.videostatus.Util.Constant_Api;
import com.salem4muk.videostatus.Util.Method;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class Verification extends AppCompatActivity {

    private Method method;
    private PinView pinView;
    private String verification, name, email, password, phoneNo, reference;
    private InputMethodManager imm;
    private ProgressDialog progressDialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Method.forceRTLIfSupported(getWindow(), Verification.this);

        method = new Method(Verification.this);

        progressDialog = new ProgressDialog(Verification.this);

        Intent intent = getIntent();
        if (intent.hasExtra("name")) {
            name = intent.getStringExtra("name");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            phoneNo = intent.getStringExtra("phoneNo");
            reference = intent.getStringExtra("reference");
        } else {
            name = method.pref.getString(method.reg_name, null);
            email = method.pref.getString(method.reg_email, null);
            password = method.pref.getString(method.reg_password, null);
            phoneNo = method.pref.getString(method.reg_phoneNo, null);
            reference = method.pref.getString(method.reg_reference, null);
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        pinView = findViewById(R.id.firstPinView);
        Button button_verification = findViewById(R.id.button_verification);
        Button button_register = findViewById(R.id.button_register_verification);
        TextView textView = findViewById(R.id.resend_verification);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Random generator = new Random();
                int n = generator.nextInt(9999 - 1000) + 1000;

                String stringEmail = method.pref.getString(method.reg_email, null);
                resend_verification(stringEmail, String.valueOf(n));

            }
        });

        button_verification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verification = pinView.getText().toString();
                verification();
            }
        });

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                method.editor.putBoolean(method.is_verification, false);
                method.editor.commit();
                startActivity(new Intent(Verification.this, Register.class));
                finishAffinity();
            }
        });

    }

    public void verification() {

        pinView.clearFocus();
        imm.hideSoftInputFromWindow(pinView.getWindowToken(), 0);

        if (verification == null || verification.equals("") || verification.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_verification_code), Toast.LENGTH_SHORT).show();
        } else {
            if (Method.isNetworkAvailable(Verification.this)) {
                pinView.setText("");
                if (verification.equals(method.pref.getString(method.verification_code, null))) {
                    register(name, email, password, phoneNo, reference);
                } else {
                    method.alertBox(getResources().getString(R.string.verification_message));
                }
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }

        }
    }

    @SuppressLint("HardwareIds")
    public void register(String sendName, String sendEmail, String sendPassword, String sendPhone, String reference) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        String device_id;
        try {
            device_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            device_id = "Not Found";
        }

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Verification.this));
        jsObj.addProperty("method_name", "user_register");
        jsObj.addProperty("type", "normal");
        jsObj.addProperty("name", sendName);
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("password", sendPassword);
        jsObj.addProperty("phone", sendPhone);
        jsObj.addProperty("device_id", device_id);
        jsObj.addProperty("user_refrence_code", reference);
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant_Api.url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                Log.d("Response", new String(responseBody));
                String res = new String(responseBody);

                try {
                    JSONObject jsonObject = new JSONObject(res);

                    if (jsonObject.has(Constant_Api.STATUS)) {

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        method.alertBox(message);

                    } else {

                        JSONArray jsonArray = jsonObject.getJSONArray(Constant_Api.tag);

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject object = jsonArray.getJSONObject(i);
                            String msg = object.getString("msg");
                            String success = object.getString("success");

                            method.editor.putBoolean(method.is_verification, false);
                            method.editor.commit();

                            if (success.equals("1")) {
                                Toast.makeText(Verification.this, msg, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Verification.this, Login.class));
                                finishAffinity();
                            } else {
                                Toast.makeText(Verification.this, msg, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Verification.this, Register.class));
                                finishAffinity();
                            }

                        }

                    }

                    progressDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressDialog.dismiss();
            }
        });
    }

    public void resend_verification(String sendEmail, String otp) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(60000);
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Verification.this));
        jsObj.addProperty("method_name", "user_register_verify_email");
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("otp_code", otp);
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant_Api.url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                Log.d("Response", new String(responseBody));
                String res = new String(responseBody);

                try {
                    JSONObject jsonObject = new JSONObject(res);

                    if (jsonObject.has(Constant_Api.STATUS)) {

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        method.alertBox(message);

                    } else {

                        JSONArray jsonArray = jsonObject.getJSONArray(Constant_Api.tag);

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject object = jsonArray.getJSONObject(i);
                            String msg = object.getString("msg");
                            String success = object.getString("success");

                            if (success.equals("1")) {

                                method.editor.putString(method.verification_code, otp);
                                method.editor.commit();

                                method.alertBox(msg);
                            } else {
                                method.alertBox(msg);
                            }

                        }

                    }

                    progressDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                method.alertBox(getResources().getString(R.string.server_time_out));
                progressDialog.dismiss();
            }
        });
    }
}

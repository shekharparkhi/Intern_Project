/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.sampleapp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.CommonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * This activity acts as the login screen that enables users to log in to the app. Steps to login include:
 * 1. Selecting a suitable server to connect to.
 * 2. Performing an OAuth login (handled in the activity: OAuthLoginActivity.java).
 */
public class LoginScreenActivity extends Activity {

    private static final String LOG = "PayPalHere.LoginScreen";
    private String mUsername;
    private String mPassword;
    private Button mLoginButton;
    private ProgressBar mProgressBar;
    private TextView mEnv;
    private boolean mUseLive;

    /**
     * initialize the various layout elements.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting the layout for this activity.
        setContentView(R.layout.activity_login_screen);

        mEnv = (TextView) findViewById(R.id.env);

        mProgressBar = (ProgressBar) findViewById(R.id.login_progress);
        mProgressBar.setVisibility(View.GONE);

        mLoginButton = (Button) findViewById(R.id.login);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg) {
                // Get the username and password from the input fields.
                mUsername = CommonUtils.getString((EditText) findViewById(R.id.username));
                mPassword = CommonUtils.getString((EditText) findViewById(R.id.password));
                // Validate the username and password for null or empty.
                if (!isValidInput()) {
                    // Sending back a toast message indicating invalid credentials.
                    CommonUtils.createToastMessage(LoginScreenActivity.this,
                            CommonUtils.getStringFromId(LoginScreenActivity.this, R.string.invalid_user_credentials));
                    return;
                }

                // Move on to the OAuth login screen.
                performOAuthLogin(arg);

            }
        });

        //Easter egg : An option to connect to different stages.

        mLoginButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Intent i = new Intent(LoginScreenActivity.this, StageSelectActivity.class);
                startActivity(i);
                return true;
            }
        });

        //In case you want to test in live.
        CheckBox checkBox = (CheckBox) findViewById(R.id.isLive_checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                mUseLive = isChecked;

                updateConnectedEnvUI();
            }
        });

        // Initialize the PayPalHereSDK with the application context and the server env name.
        // This init is NECESSARY as the SDK needs the app context to init a few underlying objects.
        // The 2 options available are "Live" and "Sandbox"

        updateConnectedEnvUI();

        // Set the list of servers within the SDK so that we can choose to work with any one of them.
        PayPalHereSDK.setOptionalServerList(getServerList());

        // Using a default username and password.
        setUserCredentials();

    }

    private void updateConnectedEnvUI() {

        if(mUseLive) {
            PayPalHereSDK.init(getApplicationContext(), PayPalHereSDK.Live);
            PayPalHereSDK.setServerName("Live");
        }
        else {
            PayPalHereSDK.init(getApplicationContext(), PayPalHereSDK.Sandbox);
            PayPalHereSDK.setServerName("Sandbox");
        }

        String connectedTo = PayPalHereSDK.getCurrentServer();
        mEnv.setText(CommonUtils.isNullOrEmpty(connectedTo) ? "" : connectedTo);
    }

    /**
     * Method to move into the OAuth login activity.
     *
     * @param arg
     */
    private void performOAuthLogin(View arg) {
        hideKeyboard(arg);

        // Pass the username and password to the OAuth login activity for OAuth login.
        // Once the login is successful, we automatically check in the merchant in the OAuth activity.
        Intent intent = new Intent(LoginScreenActivity.this, OAuthLoginActivity.class);
        intent.putExtra("username", mUsername);
        intent.putExtra("password", mPassword);
        intent.putExtra("useLive", mUseLive);
        startActivity(intent);

    }

    /**
     * Method to valid the input for null or empty.
     *
     * @return
     */
    private boolean isValidInput() {
        if (CommonUtils.isNullOrEmpty(mUsername) || CommonUtils.isNullOrEmpty(mPassword)) {
            return false;
        }
        return true;
    }

    /**
     * Method to hide the keyboard.
     *
     * @param v
     */
    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

    }

    /**
     * Method to set the username and password to a default value.
     */
    //TODO: need to remove this before shipping the app to public
    private void setUserCredentials() {
        ((EditText) findViewById(R.id.username)).setText("sathya");
        ((EditText) findViewById(R.id.password)).setText("11111111");
    }

    /**
     * This method is needed to make sure nothing is invoked/called when the
     * orientation of the phone is changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    private String getServerList() {
        InputStream is = getResources().openRawResource(R.raw.optional_server_list);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {

        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return writer.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateConnectedEnvUI();
    }
}

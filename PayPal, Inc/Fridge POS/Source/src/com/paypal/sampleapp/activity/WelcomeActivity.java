package com.paypal.sampleapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.paypal.merchant.sdk.MerchantManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.Merchant;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.CommonUtils;


public class WelcomeActivity extends Activity {

    private static MerchantManager sMerchantManager = PayPalHereSDK.getMerchantManager();
    private static final String LOG = "PayPalHere.WelcomeActivity";

    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        mp=MediaPlayer.create(WelcomeActivity.this,R.raw.selfie);
        mp.setLooping(true);
        mp.start();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if(mp!=null && !mp.isPlaying())
        mp.start();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if(mp!=null && !mp.isPlaying())
        mp.start();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(mp!=null && mp.isPlaying())
        mp.stop();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(mp!=null && !mp.isPlaying())
        mp.start();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(mp!=null && mp.isPlaying())
        mp.stop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(mp!=null && mp.isPlaying())
        mp.stop();
    }

    public void grab(View view)
    {
        mp.stop();
        // mp.release(); // Causes Force Close

        Intent intent = new Intent(WelcomeActivity.this, FixedPriceActivity.class);
        startActivity(intent);

        // Finish this activity
        finish();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }





    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean returnValue;

        switch (item.getItemId()) {
            case R.id.checkin:

                sMerchantManager.checkinMerchant("Beer @ Work Day!",
                        new DefaultResponseHandler<Merchant, PPError<MerchantManager.MerchantErrors>>() {

                            @Override
                            public void onSuccess(Merchant merchant) {
                                CommonUtils.createToastMessage(WelcomeActivity.this,
                                        "Merchant checkin successful!");
                                Log.e(LOG, "Checkin successful");

                            }

                            @Override
                            public void onError(PPError<MerchantManager.MerchantErrors> error) {
                                CommonUtils.createToastMessage(WelcomeActivity.this,
                                        "Checkin unsuccessful. " + error.getDetailedMessage());

                                Log.e(LOG, "Checkin unsuccessful. " + error.getDetailedMessage());
                            }
                        }
                );

                returnValue = true;

                break;

            case R.id.checkout:

                sMerchantManager.checkoutMerchant(new DefaultResponseHandler<Merchant,
                        PPError<MerchantManager.MerchantErrors>>() {
                    @Override
                    public void onSuccess(Merchant merchant) {
                        CommonUtils.createToastMessage(WelcomeActivity.this,
                                "Merchant checkout successful!");
                        Log.d(LOG, "Checkout successful");
                    }

                    @Override
                    public void onError(PPError<MerchantManager.MerchantErrors> error) {
                        CommonUtils.createToastMessage(WelcomeActivity.this,
                                "Merchant checkout unsuccessful!");
                        Log.e(LOG, "Checkout unsuccessful. " + error.getDetailedMessage());
                    }
                });

                returnValue = true;

                break;

            default:
                returnValue = super.onOptionsItemSelected(item);

        }
        return returnValue;

    }
}

package com.paypal.sampleapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import com.paypal.sampleapp.R;


public class ThanksActivity extends Activity {

    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thanks);
        mp=MediaPlayer.create(ThanksActivity.this,R.raw.tada);
        mp.start();

        WebView wv = (WebView) findViewById(R.id.webView1);
        wv.loadDataWithBaseURL("file:///android_res/drawable/", "<img align='middle' src='loader.gif' width='100%' height='85%' />", "text/html", "utf-8", null);


        new Handler().postDelayed(new Runnable() {
            public void run() {

                Intent intent = new Intent();
                intent.setClass(ThanksActivity.this, WelcomeActivity.class);

                ThanksActivity.this.startActivity(intent);
                ThanksActivity.this.finish();

                overridePendingTransition(R.anim.fadein, R.anim.fadeout);

            }
        }, 5000);
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

    /*

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
}

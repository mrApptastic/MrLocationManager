package dk.tesj.henrik.mrlocationmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;
import android.webkit.WebView;

public class MrLocationManager extends ActionBarActivity implements Runnable {

    public String s = "Hej, lille tulipan";
    TextView myLocationText;
    private Button start;
    private Button stop;
    private Button quit;
    private Thread t = null;
    WebView webView;
    long lastTime = 0;
    GPSBandit g;
    private Boolean logStarted = false;
    private String postLocation = "http://yourlocation.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mr_location_bandit);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(postLocation);
        myLocationText = (TextView)findViewById(R.id.myLocationText);
        start = (Button)findViewById(R.id.startButton);
        stop = (Button)findViewById(R.id.stopButton);
        quit = (Button)findViewById(R.id.quitButton);
        start.setEnabled(true);
        stop.setEnabled(false);

        start.setOnClickListener(new View.OnClickListener(){
                                     public void onClick(View v) {
                                         start.setEnabled(false);
                                         stop.setEnabled(true);
                                         logStarted = false;
                                         start();
                                     }
                                 }
        );

        stop.setOnClickListener(new View.OnClickListener(){
                                    public void onClick(View v) {
                                        start.setEnabled(true);
                                        stop.setEnabled(false);
                                        logStarted = null;
                                        stop();
                                    }
                                }
        );

        quit.setOnClickListener(new View.OnClickListener(){
                                    public void onClick(View v) {
                                        finish();
                                    }
                                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mr_location_bandit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
        else {
            start.setEnabled(true);
            stop.setEnabled(false);
        }
    }
    public void run () {
        while (t != null) {
            SystemClock.sleep(1000);
            try {
                sendLocation();
            } catch (Exception e) {

            }
        }
    }

    public void stop() {
        try
        {
            t.interrupt();
            t = null;
        }
        catch (Exception e) {

        }
    }

    public void sendLocation() {
        if (logStarted == true) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getNewLocation();
                }
            });
        }
        else if (logStarted == false) {
            startBeep();
            logStarted = true;
        }
    }

    private void startBeep() {
        String URLBandit = postLocation + "?mrRight=false";
        webView.loadUrl(URLBandit);
    }

    private void getNewLocation() {
        g = new GPSBandit(MrLocationManager.this);

        if(g.canGetLocation()) {
            double latitude = g.getLatitude();
            double longitude = g.getLongitude();
            double altitude = g.getAltitude();
            double speed = g.getSpeed();
            long time = g.getTime();
            Date currentTime = new Date(g.getTime());
            String provider = g.getProvider();
            float accuracy = g.getAccuracy();
            float bearing = g.getBearing();

            if (time > lastTime) {
                myLocationText.setText("Your Current Position is:\n" +
                        "Latitude: " + latitude + "\n" + "Longitude: " + longitude + "\nAltitude: " + altitude + "\nSpeed: " + +speed * 3.6 + " km/h\nTime: " + currentTime + "\nProvider: " + provider + "\nAccuracy: " + accuracy + "\nBearing: " + bearing);
                String URLBandit = postLocation + "?mrRight=true&Latitude=" + latitude + "&Longitude=" + longitude + "&Altitude=" + altitude + "&Speed=" + speed + "&Time=" + time + "&Provider=" + provider + "&Accuracy=" + accuracy + "&Bearing=" + bearing;

                webView.loadUrl(URLBandit);
            }

            lastTime = time;
        } else {

            g.showSettingsAlert();
        }
    }

}

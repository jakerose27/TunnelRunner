package net.wearethegreatest.tunnelrunner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.preference.PreferenceManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.Random;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long lastUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lastUpdate = System.currentTimeMillis();
    }

    public void onAccuracyChanged(Sensor sensor, int foo) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    // Based on tutorial from http://vogella.com/tutorials/AndroidSensor/article.html
    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = event.timestamp;
        if (accelationSquareRoot >= 4) //
        {
            // Only do this shit every one second
            if (actualTime - lastUpdate < 1000000000) {
                return;
            }
            System.out.println("Time: " + (actualTime));
            lastUpdate = actualTime;

            System.out.println("Sensor 1: " + event.values[0]);

            Random rand = new Random();
            int r = rand.nextInt(256);
            int g = rand.nextInt(256);
            int b = rand.nextInt(256);
            sendColor(r, g, b);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                mSensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public void sendRed(View view) {
        sendColor(255, 0, 0);
    }

    public void sendGreen(View view) {
        sendColor(0, 255, 0);
    }

    public void sendBlue(View view) {
        sendColor(0, 0, 255);
    }

    public void sendColor(int red, int green, int blue) {

        final Integer RED = red;
        final Integer GREEN = green;
        final Integer BLUE = blue;

        // `this` will refer to thread once inside the thread
        final Context thisActivity = this;

        Thread t = new Thread() {

            public void run() {
                Looper.prepare();
                HttpClient httpClient = new DefaultHttpClient();

                // Get IP address from prefs
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);
                String ip_addr =  prefs.getString("ip_addr", "");

                // Format URL
                String url = "http://" + ip_addr + "/rpi";

                try {
                    HttpPost request = new HttpPost(url);
                    String a = "{\"lights\" : [{\"blue\":";
                    String b = ", \"green\": ";
                    String c = ", \"intensity\": 0.5, \"lightId\": 1, \"red\": ";
                    String d = "}], \"propagate\": true}";
                    String a1 = a.concat(BLUE.toString());
                    String a2 = b.concat(GREEN.toString());
                    String a3 = c.concat(RED.toString());
                    String s = a1 + a2 + a3 + d;


                    StringEntity params = new StringEntity(s);
                    request.addHeader("content-type",
                            "application/json");
                    request.setEntity(params);
                    HttpResponse response = httpClient.execute(request);
                    // handle response here...
                } catch (Exception ex) {
                    // handle exception here
                    System.out.println(ex);
                } finally {
                    httpClient.getConnectionManager().shutdown();
                    Looper.loop();
                }
            }
        };

        t.start();
    }
}

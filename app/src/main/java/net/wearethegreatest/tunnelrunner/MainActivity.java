package net.wearethegreatest.tunnelrunner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long lastUpdate;
    private int position;
    public LightObject objy;
    private String question;
    private String answer;
    private String nextAnswer;
    private int currentPlayer;

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

        objy = new LightObject();
        question = null;
        answer = null;
        randomizeColor();
        randomizeQuestion();
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
            // Only do this shit every half second
            if (actualTime - lastUpdate < 500000000) {
                return;
            }
            lastUpdate = actualTime;

            randomizeQuestion();
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

    public void randomizeView(View view) {
        randomizeQuestion();
    }

    public void randomizeColor() {
        Random rand = new Random();
        objy.red = rand.nextInt(256);
        objy.green = rand.nextInt(256);
        objy.blue = rand.nextInt(256);
        sendColor(objy.red, objy.green, objy.blue);
    }

    public void answerLeft(View view) {
        currentPlayer = 1;
        openAnswer(view);
        openConfirm(view);
    }

    public void answerRight(View view) {
        currentPlayer = -1;
        openAnswer(view);
        openConfirm(view);
    }

    public void move(int num) {
        objy.setPosition(objy.getPosition() + num);
        sendColor(objy.red, objy.green, objy.blue);
    }

    private void openConfirm(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

        alertDialogBuilder.setTitle(this.getTitle() + " decision");
        alertDialogBuilder.setMessage("Say your answer out loud.");
        // set positive button: Yes message
        alertDialogBuilder.setPositiveButton("Okay.",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();
    }

    private void openAnswer(View view) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);


        alertDialogBuilder.setTitle(this.getTitle() + " decision");
        alertDialogBuilder.setMessage("Answer: " + answer + "\nWere you correct?");
        // set positive button: Yes message
        alertDialogBuilder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                updateScore(true);
            }
        });
        // set negative button: No message
        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                updateScore(false);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();
    }

    public void updateScore(boolean correct){
        System.out.println(currentPlayer + " was " + correct);
        int score = correct ? currentPlayer : (-1) * currentPlayer;
        move(score);
        Button but = (Button) findViewById(R.id.question);
        but.setText("New Question");
    };

    public void randomizeQuestion() {

        // `this` will refer to thread once inside the thread
        final Context thisActivity = this;

        Thread t = new Thread() {

            public void run() {
                Looper.prepare();
                HttpClient httpClient = new DefaultHttpClient();

                // Get IP address from prefs
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);

                // Format URL
                String url = "http://jservice.io/api/random/";

                try {
                    HttpGet request = new HttpGet(url);
                    HttpResponse response = httpClient.execute(request);
                    String jsonString = EntityUtils.toString(response.getEntity());
                    System.out.println(jsonString);
                    JSONObject jsobjy = new JSONArray(jsonString).getJSONObject(0);
                    question = "Category: " + jsobjy.getJSONObject("category").getString("title") + "\n\n" + jsobjy.getString("question");
                    answer = nextAnswer;
                    nextAnswer = jsobjy.getString("answer");

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

        if (question != null){
            System.out.println("Q: " + question);
            Button but = (Button) findViewById(R.id.question);
            but.setText(question);
            question = null;
        }
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
                    String intro = "{\"lights\" : [";
                    String item_intro = "{";
                    String no_blue = "\"blue\": " + 0 + ", ";
                    String blue = "\"blue\": " + 255 + ", ";
                    String no_green = "\"green\": " + 0 + ", ";
                    String no_red = "\"red\": " + 0 + ", ";
                    String red = "\"red\": " + 255 + ", ";
                    String id1 = "\"lightId\": " + 1 + ", ";
                    String id = "\"lightId\": " + objy.getPosition() + ", ";
                    String intensity = "\"intensity\": 0.5";
                    String item_conclusion1 = "}, ";
                    String item_conclusion2 = "} ";
                    String conclusion = "], \"propagate\": true}";

                    String item1 = item_intro + red + no_green + no_blue + id1 + intensity + item_conclusion1;
                    String item2 = item_intro + no_red + no_green + blue + id + intensity + item_conclusion2;
//                    String item2 = "";
                    String s = intro + item1 + item2 + conclusion;

//                    String a = "{\"lights\" : [{\"blue\":";
//                    String b = ", \"green\": ";
//                    String c = ", \"intensity\": 0.5, \"lightId\": " + objy.getPosition() + ", \"red\": ";
//                    String d = "}], \"propagate\": false}";
//                    String a1 = a.concat(BLUE.toString());
//                    String a2 = b.concat(GREEN.toString());
//                    String a3 = c.concat(RED.toString());
//                    String s = a1 + a2 + a3 + d;

                    System.out.println("String: " + s);

                    StringEntity params = new StringEntity(s);
                    request.addHeader("content-type",
                            "application/json");
                    request.setEntity(params);
                    HttpResponse response = httpClient.execute(request);
                    System.out.println("Response" + response.toString());
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

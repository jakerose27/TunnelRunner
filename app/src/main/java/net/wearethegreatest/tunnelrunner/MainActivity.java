package net.wearethegreatest.tunnelrunner;

import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;


public class MainActivity extends ActionBarActivity {
    private static String IP_ADDR = "http://192.168.0.111/rpi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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

    public void sendColor(int red, int green, int blue){
        final Integer RED = red;
        final Integer GREEN = green;
        final Integer BLUE = blue;
        Thread t = new Thread() {

            public void run() {
                Looper.prepare();
                HttpClient httpClient = new DefaultHttpClient();
//                String url = "http://".concat(URL);
//                url.concat("/rpi");

                try {
                    HttpPost request = new HttpPost(IP_ADDR);
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
                    System.out.println(response);
                    // handle response here...
                } catch (Exception ex) {
                    // handle exception here
                    System.out.println("Exception");
                    System.out.println(ex);
                } finally {
                    System.out.println("Finally");
                    httpClient.getConnectionManager().shutdown();
                    Looper.loop();
                }
            }
        };
        t.start();
    }
}

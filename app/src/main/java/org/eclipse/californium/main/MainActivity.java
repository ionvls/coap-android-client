package org.eclipse.californium.main;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_main);
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

        if (id == R.id.action_laptop) {
            ((EditText) findViewById(R.id.editUri)).setText(R.string.uri_laptop);
            return true;
        }
//        else if (id == R.id.action_sandbox) {
//            ((EditText)findViewById(R.id.editUri)).setText(R.string.uri_sandbox);
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public void clickGet(View view) {
        String uri = ((EditText)findViewById(R.id.editUri)).getText().toString();
        new CoapGetTask().execute(uri);
    }

    class CoapGetTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            ((TextView)findViewById(R.id.textCodeName)).setText("Loading...");
        }

        protected String doInBackground(String... args) {
            CTClient client = new CTClient(true);
            String protectedMessage = client.coapRequest(args[0], mRequestQueue);
            return protectedMessage;
        }

        protected void onPostExecute(String response) {
            if (response!=null) {
                ((TextView)findViewById(R.id.textCodeName)).setText(response);
            } else {
                ((TextView)findViewById(R.id.textCodeName)).setText("No response");
            }
        }
    }
}

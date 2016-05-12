package com.lockerfish.redditreaderapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedditActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private final boolean D = Log.isLoggable(TAG, Log.DEBUG);
    private ArrayAdapter<String> mRedditAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (D) { Log.d(TAG, "Starting OnCreate"); }

        // (1)
        // create some dummy data
        String[] data = {
                "Traincube Mobile Challenge: We The Best",
                "Google I/O is finally here",
                "Android N is for Nutella",
                "Android Studio on Windows sucks"
        };
        List<String> reddit = new ArrayList<String>(Arrays.asList(data));
        // (2)
        // ArrayAdapter will take data from a source (data) and
        // use it to populate the listView it's attached to
        mRedditAdapter = new ArrayAdapter<String>(
                this, // context
                R.layout.reddit_list_item, // layout to use for each item
                R.id.reddit_list_item_textview, // textview to update with each item
                reddit // data to display
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reddit);

        // (3)
        // attach listview
        ListView listView = (ListView) findViewById(R.id.listview_reddit);
        listView.setAdapter(mRedditAdapter);

        // (4)
        // connect network

        if (D) { Log.d(TAG, "Ending OnCreate"); }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.action_refresh) {
            // start reddit task here
            if (D) { Log.i(TAG, "Starting reddit AsyncTask here"); }

            FetchRedditTask redditTask = new FetchRedditTask(this);
            redditTask.execute();
        }
        return true;
    }


    public class FetchRedditTask extends AsyncTask<Void, Void, String[]> {

        private final String TAG = getClass().getSimpleName();
        private final boolean D = Log.isLoggable(TAG, Log.DEBUG);
        private Context mContext;

        public FetchRedditTask(Context context) {
            mContext = context;
        }

        @Override
        protected String[] doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String redditJson = null;
            try {
                URL url = new URL("http://www.reddit.com/r/androiddev/hot/.json");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ( (line = reader.readLine()) != null) {
                    Log.i(TAG, line + "\n");
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                redditJson = buffer.toString();
                if (D) { Log.i(TAG, redditJson); }

                return getRedditDataFromJson(redditJson);

            } catch (IOException e) {
                Log.e(TAG, "Failed to connect to reddit");
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse JSON data");
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing stream reader.");
                    }
                }
            }

            // if we get here, some error happened
            Log.wtf(TAG, "what da!");
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            Toast toast = Toast.makeText(
                    mContext,
                    String.format("result has %d entries", result.length),
                    Toast.LENGTH_SHORT);
            toast.show();

            mRedditAdapter.clear();
            for (int i = 0; i < result.length; i++) {
                mRedditAdapter.add(result[i]);
            }
            if (D) { Log.d(TAG, "onPostExecute"); }
        }

        private String[] getRedditDataFromJson(String redditJsonStr) throws JSONException {

            // These are the names of the JSON objects that need to be extracted
            final String REDDIT_DATA = "data";
            final String REDDIT_CHILDREN = "children";
            final String REDDIT_TITLE = "title";
            final String REDDIT_URL = "url";

            JSONObject redditJson = new JSONObject(redditJsonStr);
            JSONObject data = redditJson.getJSONObject(REDDIT_DATA);
            JSONArray redditArray = data.getJSONArray(REDDIT_CHILDREN);

            String[] entries = new String[redditArray.length()];

            for (int i = 0; i < redditArray.length(); i++) {
                JSONObject child = redditArray.getJSONObject(i);
                JSONObject item = child.getJSONObject(REDDIT_DATA);
                String title = item.getString(REDDIT_TITLE);
                String url = item.getString(REDDIT_URL);

                entries[i] = title;

                if (D) { Log.d(TAG, String.format("title: %s url: %s", title, url));}
            }

            if (D) { Log.d(TAG, String.format("%d entries parsed.", entries.length));}

            return entries;
        }
    }
}

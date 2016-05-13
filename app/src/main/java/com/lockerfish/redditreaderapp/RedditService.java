package com.lockerfish.redditreaderapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class RedditService extends IntentService {

    private final String TAG = getClass().getSimpleName();
    private final boolean D = Log.isLoggable(TAG, Log.DEBUG);

    private static final String ACTION_FETCH_NEW_ITEMS = "com.lockerfish.redditreaderapp.action.fetch";

    private static Context mContext;

    public static final String REDDIT_RESULTS = "REDDIT_RESULTS";

    public RedditService() {
        super("RedditService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetch(Context context) {
        mContext = context;
        Intent intent = new Intent(context, RedditService.class);
        intent.setAction(ACTION_FETCH_NEW_ITEMS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_NEW_ITEMS.equals(action)) {
                handleActionFetch();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetch() {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String redditJsonStr = null;
        HashMap<String, String> results = new HashMap<String,String>();

        try {
            URL url = new URL("http://www.reddit.com/r/androiddev/hot/.json");

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
//                urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) { // 200 means connection was good
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return ;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ( (line = reader.readLine()) != null) {
                    Log.i(TAG, line + "\n");
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return ;
                }
                redditJsonStr = buffer.toString();
                if (D) { Log.i(TAG, redditJsonStr); }

                // These are the names of the JSON objects that need to be extracted
                final String REDDIT_DATA = "data";
                final String REDDIT_CHILDREN = "children";
                final String REDDIT_TITLE = "title";
                final String REDDIT_URL = "url";

                JSONObject redditJson = new JSONObject(redditJsonStr);
                JSONObject data = redditJson.getJSONObject(REDDIT_DATA);
                JSONArray redditArray = data.getJSONArray(REDDIT_CHILDREN);

//            String[] entries = new String[redditArray.length()];

                for (int i = 0; i < redditArray.length(); i++) {
                    JSONObject child = redditArray.getJSONObject(i);
                    JSONObject item = child.getJSONObject(REDDIT_DATA);
                    String title = item.getString(REDDIT_TITLE);
                    String urlStr = item.getString(REDDIT_URL);

//                entries[i] = title;
                    results.put(title, urlStr);
                    if (D) { Log.d(TAG, String.format("title: %s url: %s", title, url));}
                }

                if (D) { Log.d(TAG, String.format("%d entries parsed.", results.size()));}

                publishResults(results);
            }

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
    }

    private void publishResults(HashMap<String,String> results) {

        Intent intent = new Intent(mContext, RedditReceiver.class);
        intent.setAction(RedditReceiver.ACTION_REFRESH);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(REDDIT_RESULTS, results);
        sendBroadcast(intent);

    }

}

package in.aerl.googleapi;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by npradeesh on 10/14/2016.
 */
public class RestAPICall extends AsyncTask<String, Integer, String> {

    private Context mainContext;
    public RestAPICall(Context context){
        this.mainContext = context;
    }

    @Override
    protected String doInBackground(String... params) {
        Log.i("params", params[0]);

        URL url = null;
        try {
            url = new URL(params[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            InputStream in = urlConnection.getInputStream();
            String result = convertStreamToString(in);

            Intent pathpage = new Intent(mainContext,PathActivity.class);
            pathpage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            pathpage.putExtra("pathString",result);
            mainContext.startActivity(pathpage);

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


}

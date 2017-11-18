package in.aerl.googleapi;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static java.lang.Float.parseFloat;

public class PathActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RouteAdapter routeAdapter;
        Context ctx = getApplicationContext();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ListView lst_Route = (ListView) findViewById(R.id.lst_Path);
        String result = new String(getIntent().getStringExtra("pathString"));
        //Log.i("String",result);
        try {
            JSONObject jsonObj = new JSONObject(result);
            JSONObject routes = jsonObj.getJSONArray("routes").getJSONObject(0);
            JSONObject legs = routes.getJSONArray("legs").getJSONObject(0);
            JSONArray steps = legs.getJSONArray("steps");

            //ArrayList<Routes> items = new ArrayList<Routes>();
            ArrayList<NavLocation> Nav_items = new ArrayList<NavLocation>();

            for(int i=0; i < steps.length() ; i++) {
                JSONObject json_data = steps.getJSONObject(i);
                String distance=json_data.getJSONObject("distance").getString("text");
                Float start_lat =  parseFloat(json_data.getJSONObject("start_location").getString("lat")) ;
                Float start_lng =  parseFloat(json_data.getJSONObject("start_location").getString("lng")) ;
                Float end_lat =  parseFloat(json_data.getJSONObject("end_location").getString("lat")) ;
                Float end_lng =  parseFloat(json_data.getJSONObject("end_location").getString("lng")) ;

                String path=json_data.getString("html_instructions");
                //Log.e("html",path);
                //items.add(new Routes(path,distance));


                //Nav_items.add(new NavLocation(i+1, start_lat,start_lng,end_lat,end_lng,path));
            }
            String nav_json = "nav_json";
            SharedPreferences.Editor editor = getSharedPreferences(nav_json,MODE_PRIVATE).edit();

            editor.remove("json").commit();
            editor.commit();
            editor.putString("json", result);
            editor.commit();




            //routeAdapter = new RouteAdapter(this,items);
            //ListView listView = (ListView) findViewById(R.id.lst_Path);
            //TextView listView = (TextView) findViewById(R.id.frieds_test);
            //listView.setAdapter(routeAdapter);*/
            finish();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}

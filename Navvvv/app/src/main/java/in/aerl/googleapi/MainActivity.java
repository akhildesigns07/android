package in.aerl.googleapi;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static in.aerl.googleapi.R.id.txt_destination;
import static in.aerl.googleapi.R.id.txt_source;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;

public class MainActivity extends AppCompatActivity implements LocationListener,TextToSpeech.OnInitListener {
    private EditText txt_destination;
    ArrayList<NavLocation> Nav_items = new ArrayList<NavLocation>();
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    Integer p = 0;
    volatile boolean stopWorker;
    private TextToSpeech tts;
    public boolean isSpeaking = false;


    private LocationManager locationManager;
    private Button speak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private String provider;
    public Integer counter1 = 0;

    EditText source;
    EditText destination;
    String key = "AIzaSyB-GnlX4VizR9wPVkhc_rPmDLKF3NKz-hY\t";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tts = new TextToSpeech(this, this);
        setSupportActionBar(toolbar);
        txt_destination = (EditText) findViewById(R.id.txt_destination);
        speak = (Button) findViewById(R.id.speak);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter1 = 0;
                promptSpeechInput();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        try
        {
            findBT();
            openBT();
        }
        catch (IOException ex) { }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });




        source = (EditText) findViewById(R.id.txt_source);
        destination = (EditText) findViewById(R.id.txt_destination);

        Button btn_path = (Button) findViewById(R.id.btn_path);
        btn_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findPath(source.getText().toString(), destination.getText().toString());
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txt_destination.setText(result.get(0));
                    Log.e("String in ", result.get(0));
                }
                break;
            }

        }

    }


    private void findPath(String source, String destination) {
        Log.e("hi","path");
        try {
            String nav_json = "nav_json";
            SharedPreferences.Editor editor = getSharedPreferences(nav_json,MODE_PRIVATE).edit();

            editor.remove("json").commit();
            editor.commit();


            //To convert special characters (space, underscore, etc.. ) to url format.String src = URLEncoder.encode(source, "utf-8");

           // Log.e("counter path = ", String.valueOf(counter1));
            String dst = URLEncoder.encode(destination, "utf-8");
            String mode = "walking";
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + source + "&destination=" + dst + "&key=" + key;
            Log.e("url = ",url);
            new RestAPICall(getApplicationContext()).execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.e("onresume","hi");
        RouteAdapterNav routeAdapter;
        String nav_json = "nav_json";
        SharedPreferences prefs = getSharedPreferences(nav_json, MODE_PRIVATE);
        Log.e("Counter1", String.valueOf(counter1));
        counter1 = counter1 + 1;
         if(counter1>1){
        try {

            String sp_json = prefs.getString("json", null);
            Log.e("sp1",sp_json);

            if (sp_json.length() > 1) {
                JSONObject jsonObj = new JSONObject(sp_json);

                JSONObject routes = jsonObj.getJSONArray("routes").getJSONObject(0);
                JSONObject legs = routes.getJSONArray("legs").getJSONObject(0);
                JSONArray steps = legs.getJSONArray("steps");
                for (int i = 0; i < steps.length(); i++) {
                    JSONObject json_data = steps.getJSONObject(i);
                    Double start_lat = parseDouble(json_data.getJSONObject("start_location").getString("lat"));
                    Double start_lng = parseDouble(json_data.getJSONObject("start_location").getString("lng"));
                    Double end_lat = parseDouble(json_data.getJSONObject("end_location").getString("lat"));
                    Double end_lng = parseDouble(json_data.getJSONObject("end_location").getString("lng"));

                    String path = json_data.getString("html_instructions");
                    Log.e("json = ", path);
                    Nav_items.add(new NavLocation(i + 1, start_lat, start_lng, end_lat, end_lng, path));

                }
                routeAdapter = new RouteAdapterNav(this, Nav_items);
                ListView listView1 = (ListView) findViewById(R.id.List_id);
                listView1.setAdapter(routeAdapter);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }}
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
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

    @Override
    public void onLocationChanged(Location location) {
        double lat = (double) (location.getLatitude());
        double lng = (double) (location.getLongitude());
        Log.e("lat", String.valueOf(lng));
        source.setText(String.valueOf(lat)+","+String.valueOf(lng));
        //Log.e("nav start", String.valueOf(Nav_items.get(0).Start_Lat));
            if(counter1>1) {
                Log.e("nav lat", String.valueOf(Nav_items.get(p).Start_Lat));
                double a = Nav_items.get(p).Start_Lat;
                double b = Nav_items.get(p).Start_Lng;
                MathContext mc = new MathContext(12);
                float la = (float)(a - lat);
                DecimalFormat decimalFormat = new DecimalFormat("#.###########");
                //System.out.println(decimalFormat.format(la));
                float ln = (float)(b - lng);
                Log.e("onloaction lat", String.valueOf(lat));
                Log.e("onloaction json ", String.valueOf(a));
                BigDecimal bd = new BigDecimal(lat);
                bd = bd.round(new MathContext(3));
                double rounded = bd.doubleValue();
                BigDecimal bdnav = new BigDecimal(a);
                bd = bd.round(new MathContext(3));
                double roundednavlat = bd.doubleValue();
                                BigDecimal bdlng = new BigDecimal(lng);
                                bd = bd.round(new MathContext(3));
                                double roundedlng = bd.doubleValue();
                                BigDecimal bdnavlng = new BigDecimal(b);
                                bd = bd.round(new MathContext(3));
                                double roundednavlng = bd.doubleValue();
                Log.e("Rounded", String.valueOf(rounded));
                if((Math.abs(la) < 0.000015||Math.abs(ln) < 0.000015 )&&(roundednavlat == rounded)&&(roundednavlng == roundedlng)) {
                    Log.e("onloaction", "speak");

                    speakOut();
                    p++;


                }
            }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            //myLabel.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("HC-05-28"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        //myLabel.setText("Bluetooth Device Found");
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

       // myLabel.setText("Bluetooth Opened");
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        Toast tst = Toast.makeText(getApplicationContext(),"Warning",Toast.LENGTH_LONG);
                        tst.show();
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            //destination.setText(parseFloat(data)+"");
                                            if(parseFloat(data) < 100.0 && parseFloat(data)>5.0){
                                                //alert
                                                Log.e("hi",data);

                                                speakOutblueeooth();

                                            }else{
                                                //myLabel.setText(data);
                                            }
                                            Log.e("data","data: "+data);

                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {

               // speakOutblueeooth();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }


    @Override
    public void onDestroy() {
// Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }



    private void speakOutblueeooth() {
        //String text = editText.getText().toString();
        if(!isSpeaking) {
            isSpeaking = true;
            tts.speak("Stop", TextToSpeech.QUEUE_FLUSH, null);

        }
        isSpeaking = false;
    }

    public void speakOut() {
        //String text = editText.getText().toString();
        if(!isSpeaking) {
            isSpeaking = true;
            String path =  Html.fromHtml(Nav_items.get(p).html_instructions).toString();;
            Log.e("path",path);
            tts.speak(path, TextToSpeech.QUEUE_FLUSH, null);

        }
        isSpeaking = false;
    }



}

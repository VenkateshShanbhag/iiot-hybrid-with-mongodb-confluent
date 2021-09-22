package com.example.trackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.trackerapp.Model.TrackingGeoSpatial;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.trackerapp.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.Sort;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.ClientResetRequiredError;
import io.realm.mongodb.sync.SyncConfiguration;
import io.realm.mongodb.sync.SyncSession;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnPolylineClickListener{
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    String Appid;
    public Realm realm;
    public String[] registration_number;
    String value;
    Button refresh;
    Button show_timeline;
    List<String> latList = new ArrayList<String>();
    List<String> lonList = new ArrayList<String>();
    List<LatLng> latlonList = new ArrayList<LatLng>();
    int vehicleTimeline = 0;
    MyApplication dbConfigs = new MyApplication();
    Realm backgroundThreadRealm;
    Date timerange;
    App app;
    public List<TrackingGeoSpatial> tracking_data = new ArrayList<TrackingGeoSpatial>();
    SupportMapFragment mapFragment;
    String timeline_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        timeline_url = dbConfigs.getTimeline_url();
        Appid = dbConfigs.getAppid();

        super.onCreate(savedInstanceState);
        // Get the value passed from intent in previous activity.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("key");
            Log.v("Intent Value", value);
        }
        registration_number = value.split(" - ", 2);
        SyncSession.ClientResetHandler handler = new SyncSession.ClientResetHandler() {
            @Override
            public void onClientReset(SyncSession session, ClientResetRequiredError error) {
                Log.e("EXAMPLE", "Client Reset required for: " +
                        session.getConfiguration().getServerUrl() + " for error: " +
                        error.toString());
            }
        };

        /* Initialize app configuration and login */
        app = new App(new AppConfiguration.Builder(Appid)
                .defaultClientResetHandler(handler)
                .build());
        app.login(Credentials.anonymous());
        
        // MAP Activity
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshPage();
            }
        });

        show_timeline = findViewById(R.id.timeline);
        try {
            show_timeline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        showVehicleTimeline();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("NETWORK: ", "PLEASE CHEK YOUR NETWORK CONNECTION");
        }
    }


    public void showVehicleTimeline() throws IOException, JSONException {
        vehicleTimeline = 1;
        startDialogActivity();
    }




    private void refreshPage() {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        mapFragment.getMapAsync(this);
        ft.detach(mapFragment);
        ft.attach(mapFragment);




//        Intent intent = getIntent();
//        intent.putExtra("key", value);
//        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {

            // Timeline data
            URL url = new URL(timeline_url+registration_number[1].toUpperCase());
            String readLine = null;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                ArrayList<String> response = new ArrayList<>();

                while ((readLine = in.readLine()) != null) {
                    response.add(readLine);
                }
                in.close();
                // TODO: Parse the result and display in maps
                JSONArray response_json_array = new JSONArray(response.get(0));

                for(int i = 0; i<response_json_array.length(); i++) {
                    String lat = response_json_array.getJSONObject(i).optString("lat");
                    JSONObject lat_json = new JSONObject(lat);
                    latList.add((String) lat_json.get("$numberDouble"));
                    String lon = response_json_array.getJSONObject(i).optString("lon");
                    JSONObject lon_json = new JSONObject(lon);
                    lonList.add((String) lon_json.get("$numberDouble"));
                }

                for(int i = 0; i< latList.size(); i++) {
                    double lat1 = Double.parseDouble(latList.get(i));
                    double lon1 = Double.parseDouble(lonList.get(i));
                    LatLng latlan = new LatLng(lat1,lon1);
                    latlonList.add(latlan);
                }
            }
            PolylineOptions opts = new PolylineOptions();
            for (LatLng location : latlonList) {
                opts.add(location)
                    .color(Color.BLUE)
                    .width(10)
                    .geodesic(true);
            }

            Polyline polyline = googleMap.addPolyline(opts);
            polyline.setClickable(true);
        } catch (Exception e){
            System.out.println("EXCEPTION: "+e);
        }

        mMap = googleMap;
        LatLng custom = new LatLng(Double.parseDouble(latList.get(latList.size()-1)), Double.parseDouble(lonList.get(lonList.size()-1)));
        MarkerOptions marker = new MarkerOptions().position(custom).title(registration_number[1].toUpperCase());
        marker.icon(bitmapDescriptorFromVector(this, R.mipmap.car_icon_03));
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(custom));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18));

        googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(@NonNull Polyline polyline) {
                startDialogActivity();
            }
        });
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {
        System.out.println("Polyline clicked !!!!!!");
    }

    private void startDialogActivity() {
        SyncSession.ClientResetHandler handler = new SyncSession.ClientResetHandler() {
            @Override
            public void onClientReset(SyncSession session, ClientResetRequiredError error) {
                Log.e("EXAMPLE", "Client Reset required for: " +
                        session.getConfiguration().getServerUrl() + " for error: " +
                        error.toString());
            }
        };

        /* Initialize app configuration and login */
        App app = new App(new AppConfiguration.Builder(Appid)
                .defaultClientResetHandler(handler)
                .build());
        app.login(Credentials.anonymous());
        User user = app.currentUser();
        String partitionKey = "security";
        SyncConfiguration config = new SyncConfiguration.Builder(
                user,
                partitionKey).allowWritesOnUiThread(true).allowQueriesOnUiThread(true)
                .build();

        backgroundThreadRealm = Realm.getInstance(config);
        backgroundThreadRealm.executeTransaction(transactionRealm -> {
            TrackingGeoSpatial results = backgroundThreadRealm.where(TrackingGeoSpatial.class).sort("Timestamp", Sort.DESCENDING).equalTo("reg_num", registration_number[1].toUpperCase()).findFirst();
            tracking_data.add(results);
        });

//        for (int i = 0; i < tracking_data.size(); i++) {
//            Double lat = tracking_data.get(i).getLat();
//            Double lon = tracking_data.get(i).getLon();
//        }

        backgroundThreadRealm.close();
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dialog, viewGroup, false);

        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView textView = (TextView) findViewById(R.id.display_msg);

        // TODO: Set the timeline data for the registration number
        textView.setText("");

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
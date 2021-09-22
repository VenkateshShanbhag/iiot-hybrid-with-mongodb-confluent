package com.example.trackerapp;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.trackerapp.Model.TrackingGeoSpatial;
import com.example.trackerapp.Model.TrackingGeoSpatial_location;
import com.example.trackerapp.databinding.ActivityShowAllVehiclesBinding;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
//
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.ClientResetRequiredError;
import io.realm.mongodb.sync.SyncConfiguration;
import io.realm.mongodb.sync.SyncSession;

public class ShowAllVehiclesActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityShowAllVehiclesBinding binding;
    String Appid;
    public List<RealmResults<TrackingGeoSpatial>> tracking_data = new ArrayList<RealmResults<TrackingGeoSpatial>>();
    double lat;
    double lon;
    String reg_num;
    Realm backgroundThreadRealm;
    Button refresh;
    Button add_vehicle;
    Button vehicle_list;
    List<String> latList = new ArrayList<String>();
    List<String> lonList = new ArrayList<String>();
    List<String> regNumList = new ArrayList<String>();
    String partitionKey;
    List<String> timestampList = new ArrayList<String>();
    TrackingGeoSpatial tracking=null;
    boolean inCircle;
    MyApplication dbConfigs;
    String latest_location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dbConfigs = new MyApplication();
        Appid = dbConfigs.getAppid();
        latest_location = dbConfigs.getLatest_location();

        super.onCreate(savedInstanceState);

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

        syncConfigurations(user);
        syncLatestLatLon(user);
        readRealmData(user);

        binding = ActivityShowAllVehiclesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        refresh = findViewById(R.id.refresh2);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backgroundThreadRealm.close();
                refreshPage();
            }
        });

        add_vehicle = findViewById(R.id.add_vehicle);
        add_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backgroundThreadRealm.close();
                openAddVehiclePage();
            }
        });

        vehicle_list = findViewById(R.id.vehicle_list);
        vehicle_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backgroundThreadRealm.close();
                showVehicleList();
            }
        });
    }

    /* Realm Sync init */
    public void syncConfigurations(User user) {
        partitionKey = "security";
        SyncConfiguration config = new SyncConfiguration.Builder(
                user,
                partitionKey).allowWritesOnUiThread(true).allowQueriesOnUiThread(true)
                .build();

        backgroundThreadRealm = Realm.getInstance(config);

    }

    private void syncLatestLatLon(User user){
        try {
            URL url = new URL(latest_location);
            String readLine;
            HttpURLConnection conection = (HttpURLConnection) url.openConnection();
            conection.setRequestMethod("GET");
            int responseCode = conection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conection.getInputStream()));
                ArrayList<String> response = new ArrayList<>();
                while ((readLine = in.readLine()) != null) {
                    response.add(readLine);
                }
                in.close();
                // TODO: Parse the result and display in maps
                JSONArray response_json_array = new JSONArray(response.get(0));
                System.out.println("JSON String Result " + response_json_array.get(0));

                for(int i = 0; i<response_json_array.length(); i++){
                    String lat = response_json_array.getJSONObject(i).optString("lat");
                    JSONObject lat_json = new JSONObject(lat);
                    latList.add((String) lat_json.get("$numberDouble"));

                    String lon = response_json_array.getJSONObject(i).optString("lon");
                    JSONObject lon_json = new JSONObject(lon);
                    lonList.add((String) lon_json.get("$numberDouble"));

                    String reg_num = response_json_array.getJSONObject(i).optString("reg_num");
                    regNumList.add( reg_num);

                    String timestamp = response_json_array.getJSONObject(i).optString("Timestamp");
                    JSONObject timestamp_json = new JSONObject(timestamp);
                    JSONObject timestamp_long = new JSONObject(timestamp_json.get("$date").toString());

                    timestampList.add((String)timestamp_long.get("$numberLong"));
                }
                backgroundThreadRealm.executeTransaction(transactionRealm -> {
                    System.out.println(latList);
                    for(int i=0; i< latList.size(); i++){
                        double lat1 = Double.parseDouble(latList.get(i));
                        double lon1 = Double.parseDouble(lonList.get(i));
                        long timestamp = Long.parseLong(timestampList.get(i));
                        Date date = new Date(timestamp);
                        String reg_num = regNumList.get(i);
                        System.out.println("REG_NUM !!!!!!"+ reg_num);
                        tracking = transactionRealm.where(TrackingGeoSpatial.class).equalTo("reg_num",reg_num).findFirst();
                        if(tracking == null) {
                            tracking = new TrackingGeoSpatial();  // or realm.createObject(Person.class, id);
                            tracking.set_id(new ObjectId());
                        }
                        tracking.setTimestamp(date);
                        tracking.setPartition_key("security");
                        tracking.setReg_num(reg_num);
                        TrackingGeoSpatial_location tracking_location = new TrackingGeoSpatial_location();
                        RealmList<Double> latlonlist = new RealmList<>();
                        latlonlist.add(lat1);
                        latlonlist.add(lon1);
                        tracking_location.setCoordinates(latlonlist);
                        tracking_location.setType("Point");

                        tracking.setLocation(tracking_location);


                        transactionRealm.insertOrUpdate(tracking);
                    }

                    System.out.println("Instered successfully !!!!!!!!!!!!!!!!!!!!");
                });
            }
        } catch (Exception e){
            System.out.println("EXCEPTION: "+e);
        }
    }

    /* Sync data from Atlas to Realm db */
    //TODO: The query should read all the files. as the timeseries data is synced directly into Tracking data in updateLatestLatLon method
    public void readRealmData(User user){
        backgroundThreadRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                // ... do something with the updates (UI, etc.) ...
                RealmChangeListener<Realm> realmListener = new RealmChangeListener<Realm>() {
                    @Override
                    public void onChange(Realm realm) {
                        RealmResults<TrackingGeoSpatial> results =
                                realm.where(TrackingGeoSpatial.class).sort("Timestamp", Sort.DESCENDING).distinct("reg_num").findAll();

                        tracking_data.add(results);
                    }
                };
                realm.addChangeListener(realmListener);
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        for (int i = 0; i < tracking_data.get(0).size(); i++) {
            lat = tracking_data.get(0).get(i).getLocation().getCoordinates().get(0);
            lon = tracking_data.get(0).get(i).getLocation().getCoordinates().get(1);
            reg_num = tracking_data.get(0).get(i).getReg_num();
            // Add a marker in Sydney and move the camera
            LatLng custom = new LatLng(lat, lon);
            MarkerOptions marker = new MarkerOptions().position(custom).title(reg_num + "\n " + lat + " " + lon);
            marker.icon(bitmapDescriptorFromVector(this, R.mipmap.car_icon_03));
            mMap.addMarker(marker).showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(custom));
        }
        // addGeofence(new LatLng(14.24166, 74.448394), 10000f);
        addCircle(new LatLng(dbConfigs.getStatic_lat(),dbConfigs.getStatic_lon()), 10000f);
        filterMarkers(20000f);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
    }


    /* Create fence circle on googlemaps */
    private void addCircle(LatLng latLng, float radius){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(128,0,255,255));
        circleOptions.fillColor(Color.argb(64,0,128,255));
        circleOptions.strokeWidth(3);
        mMap.addCircle(circleOptions);
    }

    private void filterMarkers(double radiusForCircle){
        float[] distance = new float[2];

        for (int i = 0; i < tracking_data.get(0).size(); i++) {
            double lat = tracking_data.get(0).get(i).getLocation().getCoordinates().get(0);
            double lon = tracking_data.get(0).get(i).getLocation().getCoordinates().get(1);
            String reg_num_1 = tracking_data.get(0).get(i).getReg_num();
            Location.distanceBetween(lat,lon, dbConfigs.getStatic_lat(),dbConfigs.getStatic_lon()
                    , distance);

            inCircle = distance[0] <= radiusForCircle;
            System.out.println("IN CIRCLE: "+inCircle+ "   reg_num :"+reg_num_1);
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    /* Button Function */
    private void showVehicleList(){
        Intent intent = new Intent(this, AllVehicleDetails.class);
        Log.v("INFO>>","The Add vehicle activity started");
        startActivity(intent);
    }

    private void openAddVehiclePage() {
        Intent intent = new Intent(this, AddVehicle.class);
        Log.v("INFO>>","The Add vehicle activity started");
        startActivity(intent);
    }

    private void refreshPage() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

}
package com.example.trackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.trackerapp.Model.TrackingGeoSpatial;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.ClientResetRequiredError;
import io.realm.mongodb.sync.SyncConfiguration;
import io.realm.mongodb.sync.SyncSession;


public class AllVehicleDetails extends AppCompatActivity implements AdapterView.OnItemClickListener {
    String Appid;
    private App app;
    public Realm realm;
    public List<String> vehicles=new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApplication dbConfigs = new MyApplication();
        Appid = dbConfigs.getAppid();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details);

        try{
            getDataFromSync();
        } catch (Exception e)
        {
            System.out.println("EXCEPTION >>>>>>>>>>>>>>>>> "+ e);
        }
    }

    public void getDataFromSync() {
        SyncSession.ClientResetHandler handler = new SyncSession.ClientResetHandler() {
            @Override
            public void onClientReset(SyncSession session, ClientResetRequiredError error) {
                Log.e("EXAMPLE", "Client Reset required for: " +
                        session.getConfiguration().getServerUrl() + " for error: " +
                        error.toString());
            }
        };


        App app = new App(new AppConfiguration.Builder(Appid)
                .defaultClientResetHandler(handler)
                .build());


        app.login(Credentials.anonymous());
//        app.loginAsync(Credentials.anonymous(), new App.Callback<User>() {
//            @Override
//            public void onResult(App.Result<User> result) {
//                if(result.isSuccess())
//                {
//                    Log.v("User","Logged In Successfully");
//
//                }
//                else
//                {
//                    Log.v("User","Failed to Login");
//                }
//            }
//        });
        User user = app.currentUser();

        String partitionValue = "security";

        SyncConfiguration config = new SyncConfiguration.Builder(user, partitionValue)
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .build();
        Realm backgroundThreadRealm = Realm.getInstance(config);
        backgroundThreadRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                RealmResults<TrackingGeoSpatial> results = realm.where(TrackingGeoSpatial.class).findAll();
                for (int i = 0; i < results.size(); i++) {
                    vehicles.add(results.get(i).toString());
                }
            }
        });
        renderListActivity(vehicles);

        backgroundThreadRealm.close();
    }

    public void renderListActivity(List<String> vehicles){
        System.out.println("!!!!!!!!!!!!!!!>>>>>>>>>>>>>>>>>>>>"+vehicles);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, vehicles);
        ListView listView = (ListView) findViewById(R.id.lvVehicle);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
        System.out.println(">>>>>>>> INside ADaptor view");
        String vehicle_data = adapterView.getItemAtPosition(pos).toString();
        System.out.println(vehicle_data);
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("key",vehicle_data);
        startActivity(i);
    }
}
package com.example.trackerapp;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import io.realm.Realm;

public class DialogActivity extends AppCompatActivity {

    private String value;
    Realm backgroundThreadRealm;
    public String registration_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            registration_number = extras.getString("key");
            Log.v("Intent Value", value);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        System.out.println("!!!!!!!!!!!!!>>>>>>>>>>>>>>>>>>>>>>>>>"+registration_number);
        TextView textView = (TextView) findViewById(R.id.display_msg);
        textView.setText(value);
    }


//    void showCustomDialog() {
//        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
//        ViewGroup viewGroup = findViewById(android.R.id.content);
//
//        //then we will inflate the custom alert dialog xml that we created
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dialog, viewGroup, false);
//
//        //Now we need an AlertDialog.Builder object
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        //setting the view of the builder to our custom view that we already inflated
//        builder.setView(dialogView);
//
//        //finally creating the alert dialog and displaying it
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//    }
}
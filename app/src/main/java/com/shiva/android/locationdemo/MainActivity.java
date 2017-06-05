package com.shiva.android.locationdemo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    TextView tv1,tv2;

    LocationManager locationManager;

    LocationListener locationListener;

    EditText number;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = (TextView)findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);

        number = (EditText) findViewById(R.id.et1);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                double longitude = location.getLongitude();

                double latitude = location.getLatitude();

                tv1.setText("Longitude : " + longitude);

                tv2.setText("Latitude : " + latitude);

                Log.i("Location",location.toString());
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
        };

        //turn on GPS
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            showGPSDisabledAlertToUser();
        }

        //check if android version is less then 23
        if(Build.VERSION.SDK_INT < 23){

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 10, locationListener);

        } else {

            //check for permission...
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                //request for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 10, locationListener);

            }

            //check for SMS permission
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){

                //ask for SMS permission
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SEND_SMS}, 2);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            showGPSDisabledAlertToUser();
        }

        if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 10, locationListener);

            }else {
                Toast.makeText(this, "Can't display Location, Give the GPS permission", Toast.LENGTH_SHORT);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

    }

    public void sendSMS(View v){

        //check for permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){

            String msg = "Your Location \n " + tv1.getText() + "\n" +
                    tv2.getText();

            SmsManager smsManager = SmsManager.getDefault();

            StringTokenizer stringTokenizer = new StringTokenizer(number.getText().toString(),",");
            while(stringTokenizer.hasMoreElements()){
                String num = stringTokenizer.nextToken();

                if(num.length() == 10) { // send sms if number length is 10

                    smsManager.sendTextMessage(num, null, msg, null, null);

                    Toast.makeText(this, "SMS Send Successfully to : " + num, Toast.LENGTH_SHORT).show();

                }else{

                    Toast.makeText(this, "SMS Delivery failed to : " + num + "\n Not a Valid Number", Toast.LENGTH_SHORT).show();
                }
            }

            //number.setText(""); // To remove the number after sending SMS

        }else{
            Toast.makeText(this, "SMS delivery Failed, Try again", Toast.LENGTH_SHORT).show();

            //ask for request
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 2);
        }

    }

    public void showGPSDisabledAlertToUser(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage("Your GPS is Disabled, Please Turn ON the GPS for your safery")
                .setCancelable(false).setPositiveButton("Turn On GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(callGPSSettingIntent);
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

    }
}

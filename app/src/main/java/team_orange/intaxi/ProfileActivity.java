package team_orange.intaxi;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;

    private TextView textViewUserEmail, latitude, longitude;
    private Button buttonLogout;
    private Switch visibilitySwitch;

    LocationManager locationManager;
    LocationListener locationListener;

    DatabaseReference mRootRef= FirebaseDatabase.getInstance().getReference();
    DatabaseReference mDriverRef=mRootRef.child("drivers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            finish();

            startActivity(new Intent(this, LoginActivity.class));
        } else {
            final FirebaseUser user = firebaseAuth.getCurrentUser();
            buttonLogout = (Button) findViewById(R.id.buttonLogout);
            visibilitySwitch = (Switch) findViewById(R.id.visbilitySwitch);
            this.setTitle(user.getEmail());
            buttonLogout.setOnClickListener(this);

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            latitude = (TextView) findViewById(R.id.latitude);
            longitude = (TextView) findViewById(R.id.longitude);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(!isOnline())
                    {
                        Toast.makeText(getApplicationContext(),"No internet connection!",Toast.LENGTH_LONG).show();
                        visibilitySwitch.setChecked(false);
                        return;
                    }
                    latitude.setText(" " + location.getLatitude());
                    longitude.setText(" " + location.getLongitude());
                    mDriverRef.child(user.getUid()).child("location").child("latitude").setValue(location.getLatitude());
                    mDriverRef.child(user.getUid()).child("location").child("longitude").setValue(location.getLongitude());

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {
                    Intent intern = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intern);
                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.INTERNET
                    }, 10);
                    return;
                } else {
                    configureButton();
                }

                try {
                    getSupportActionBar().setElevation(0);
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }

    }
    @Override
    public void onClick (View view){
        if(!isOnline())
        {
            Toast.makeText(this,"No internet connection!",Toast.LENGTH_LONG).show();
            return;
        }
        if (view == buttonLogout) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    configureButton();
                return;
        }
    }
    private void configureButton() {


        visibilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    buttonView.setText("Visible");
                    if ( locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                        locationManager.requestLocationUpdates("network", 5000, 4, locationListener);
                    }
                    else{
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));

                        TextView text = (TextView) layout.findViewById(R.id.text);
                        text.setText("Please, enable GPS on your phone and try again.");

                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                        buttonView.setChecked(false);
                    }


                }else
                {
                    buttonView.setText("Hidden");
                    longitude.setText(" ");
                    latitude.setText(" ");
                }

            }
        });

    }

    public  boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}

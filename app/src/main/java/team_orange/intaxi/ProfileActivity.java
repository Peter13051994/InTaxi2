package team_orange.intaxi;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;

    private TextView notifTextView, latitude, longitude,visibilityTextView;
    private Button buttonLogout;
    public ArrayList<Request> Requests;
    private ImageButton visibilityButton;

    long count=0;

    LocationManager locationManager;
    LocationListener locationListener;

    DatabaseReference mRootRef= FirebaseDatabase.getInstance().getReference();
    DatabaseReference mDriverRef=mRootRef.child("drivers");
    DatabaseReference mRqestsRef=mRootRef.child("requests");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        firebaseAuth = FirebaseAuth.getInstance();
        Requests=new ArrayList<Request>();
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            final FirebaseUser user = firebaseAuth.getCurrentUser();
            buttonLogout = (Button) findViewById(R.id.buttonLogout);
            visibilityButton=(ImageButton)findViewById(R.id.visibilityButton);
            visibilityTextView=(TextView)findViewById(R.id.visibilityTextView);
            notifTextView=(TextView)findViewById(R.id.requestsTextView);
            this.setTitle(user.getEmail());
            buttonLogout.setOnClickListener(this);
            /*notifTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(getApplicationContext(),RequestsActivity.class);
                    i.putExtra("reqs",Requests);
                    startActivity(i);
                }
            });*/


            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            latitude = (TextView) findViewById(R.id.latitude);
            longitude = (TextView) findViewById(R.id.longitude);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(!isOnline())
                    {
                        Toast.makeText(getApplicationContext(),"No internet connection!",Toast.LENGTH_LONG).show();
                        visibilityTextView.setText("Hidden");
                        visibilityButton.setImageResource(R.drawable.button_hidden);
                        return;
                    }
                    latitude.setText(" " + location.getLatitude());
                    longitude.setText(" " + location.getLongitude());
                    mDriverRef.child(user.getUid()).child("location").child("latitude").setValue(location.getLatitude());
                    mDriverRef.child(user.getUid()).child("location").child("longitude").setValue(location.getLongitude());
                    mDriverRef.child(user.getUid()).child("status").setValue(1);
                    mRqestsRef.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                            final FirebaseUser user = firebaseAuth.getCurrentUser();
                            if(snapshot.child("possibleDrivers").child(user.getUid()).exists()) {
                                int id = Integer.parseInt(snapshot.getKey());
                                double latitude = snapshot.child("CustomerLocation").child("latitude").getValue(Double.class);
                                double longitude = snapshot.child("CustomerLocation").child("latitude").getValue(Double.class);
                                int travCount=Integer.parseInt(snapshot.child("TravellersCount").getValue(String.class));
                                String DestinationLocation=snapshot.child("DestinationLocation").getValue(String.class);
                                String OtherRequests=snapshot.child("OtherRequests").getValue(String.class);
                                Requests.add( new Request(id,latitude,longitude,travCount,DestinationLocation,OtherRequests));

                                notifTextView.setText("Notification:" + Requests.size());
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
    } //End of onCreate method

    @Override
    public void onClick (View view){
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        if(!isOnline())
        {
            Toast.makeText(this,"No internet connection!",Toast.LENGTH_LONG).show();
            return;
        }
        if (view == buttonLogout) {
            mDriverRef.child(user.getUid()).child("status").setValue(0);
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
        visibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(visibilityTextView.getText().toString()=="Hidden") {
                    visibilityTextView.setText("Visible");
                    visibilityButton.setImageResource(R.drawable.button_visible);
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locationManager.requestLocationUpdates("network", 5000, 4, locationListener);


                    }
                    else
                        {
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
                    }
                }
                else{
                        visibilityTextView.setText("Hidden");
                        visibilityButton.setImageResource(R.drawable.button_hidden);
                    latitude.setText("");
                    longitude.setText("");
                    locationManager.removeUpdates(locationListener);
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

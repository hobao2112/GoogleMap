package com.bindungso.map;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.Polyline;

public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private static final int RADIUS = 700;
    private GoogleMap myMap;
    private ProgressDialog progressDialog;
    private LatLng latLng;
    private static final int REQUEST_CODE = 21;
    private Button btnFind;
    private Button btnCafe;
    private Button btnRestaurant;
    private Button btnFuel;
    private Button btnAsk;
    private static final int CHECK_ACTI = 0;
    private static final int CHECK_BTN = 1;
    private Location myLocation;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderApi fusedLocationProviderApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        enableGoogleApiClient();
        checkPers(CHECK_ACTI);
        this.setUI();
        this.setProgressDialog();
        this.drawMap();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void checkPers(int type) {
        if (type == 1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "ASK PERMISSON FIRST!!!", Toast.LENGTH_SHORT).show();
            } else this.showMyLocation();
        } else if (type == 0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleApiClient.connect();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnFind:
                checkPers(CHECK_BTN);
                break;
            case R.id.btnCafe:
                if (isConnected() == true) {
                    run("cafe");
                } else {
                    Toast.makeText(this, "NO INTERNET CONNECTION!!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnRestaurant:
                if (isConnected() == true) {
                    run("restaurant");
                } else {
                    Toast.makeText(this, "NO INTERNET CONNECTION!!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnFuel:
                if (isConnected() == true) {
                    run("gas_station");
                } else {
                    Toast.makeText(this, "NO INTERNET CONNECTION!!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnPermisson:
                askPermission();
                break;
        }
    }

    private void run(String type) {
        myMap.clear();
        if (myLocation == null) {
            Toast.makeText(this, "Can't find your location", Toast.LENGTH_SHORT).show();
            return;
        } else {
            runAsyncTask(type);
        }
    }

    private void showMyLocation() {
        try {
            if (myLocation == null)
                Toast.makeText(MainActivity.this, "Location not found!!!", Toast.LENGTH_SHORT).show();
            else {
                myMap.clear();
                latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));
                setMarkerMyLocation();
            }

        } catch (SecurityException e) {
            e.printStackTrace();
            return;
        }
    }

    private void setMarkerMyLocation() {
        MarkerOptions option = new MarkerOptions();
        option.position(latLng);
        option.title("My location!!");
        Marker currentMarker = myMap.addMarker(option);
        currentMarker.showInfoWindow();
        myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String markerAddress = marker.getSnippet().toString();
                LatLng markerLocation = marker.getPosition();
                Toast.makeText(MainActivity.this, "" + (float) markerLocation.latitude, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                String permisson[] = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(this, permisson, REQUEST_CODE);
                return;
            } else {
                Toast.makeText(MainActivity.this, "You have permisson!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (permissions.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                googleApiClient.connect();
            } else
                Toast.makeText(MainActivity.this, "Permission denied!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
    }

    private void drawMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                myMap = googleMap;
                myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        progressDialog.dismiss();
                    }
                });
                MapStyleOptions mapStyleOptions = new MapStyleOptions(getResources().getString(R.string.style_json));
                boolean s = myMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.style));
                if (s == false)
                    Toast.makeText(MainActivity.this, "Failed to set map style", Toast.LENGTH_SHORT).show();
                myMap.getUiSettings().setZoomControlsEnabled(true);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                myMap.setMyLocationEnabled(true);
                myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Toast.makeText(MainActivity.this, "" + latLng.latitude + "," + latLng.longitude, Toast.LENGTH_SHORT).show();
                    }
                });
                myMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
                    @Override
                    public void onPoiClick(PointOfInterest pointOfInterest) {
                        Toast.makeText(MainActivity.this, pointOfInterest.name.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setUI() {
        btnFind = (Button) findViewById(R.id.btnFind);
        btnCafe = (Button) findViewById(R.id.btnCafe);
        btnRestaurant = (Button) findViewById(R.id.btnRestaurant);
        btnFuel = (Button) findViewById(R.id.btnFuel);
        btnAsk = (Button) findViewById(R.id.btnPermisson);
        btnCafe.setOnClickListener(this);
        btnFind.setOnClickListener(this);
        btnRestaurant.setOnClickListener(this);
        btnFuel.setOnClickListener(this);
        btnAsk.setOnClickListener(this);

    }

    private void setProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("PLEASE WAIT...");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(true);
        progressDialog.show();

    }

    public void setMarker(Address address) {
        MarkerOptions options = new MarkerOptions();
        options.position(address.getLocation());
        Marker marker = myMap.addMarker(options);
        marker.setTitle(address.getName());
        marker.setSnippet(address.getDetail_address());
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker.showInfoWindow();
    }

    private void enableGoogleApiClient() {
        createLocationRequest();
        fusedLocationProviderApi = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=&radius=&type=&key=AIzaSyA_nIe6tt9rnS9BjJPSYVY2yd3I2NJPQPw
    private void runAsyncTask(String type) {
        setMarkerMyLocation();
        StringBuilder urlRequest = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        urlRequest.append("location=" + myLocation.getLatitude() + "," + myLocation.getLongitude());
        urlRequest.append("&radius=" + RADIUS);
        urlRequest.append("&type=" + type);
        urlRequest.append("&key=" + getString(R.string.MY_API_KEY));
        MyAsyncTask myAsyncTask = new MyAsyncTask(this);
        myAsyncTask.execute(urlRequest.toString());
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }


    @Override
    public void onConnected(Bundle bundle) {
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
        fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();


    }
}

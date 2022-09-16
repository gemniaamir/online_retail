package com.codecanyon.onlinestore;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.cocosw.bottomsheet.BottomSheet;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.sdsmdg.tastytoast.TastyToast;

import io.nlopez.smartlocation.SmartLocation;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    ImageView gotoBack;
    TextView gLocation, latitude, longitude;
    AppCompatButton gotoLocation;

    GoogleMap mMap;
    LatLng currentLocation;
    LinearLayout bottomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        gLocation = findViewById(R.id.location);
        gotoLocation = findViewById(R.id.gotoAddress);
        bottomView = findViewById(R.id.latLngView);
        bottomView.setVisibility(View.GONE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        gotoBack = findViewById(R.id.gotoBack);

        gotoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        gotoLocation.setOnClickListener(view -> {
            if (latitude.getText().toString().trim().equals("") ||
                    longitude.getText().toString().trim().equals("") ||
                    gLocation.getText().toString().trim().equals("")) {
                TastyToast.makeText(MapActivity.this, "Address fields can't be empty", TastyToast.LENGTH_LONG, TastyToast.WARNING);
            } else {
                Intent returnIntent = new Intent();

                returnIntent.putExtra("Lat", latitude.getText().toString().trim());
                returnIntent.putExtra("Lon", longitude.getText().toString().trim());
                returnIntent.putExtra("Loc", gLocation.getText().toString().trim());

                setResult(Activity.RESULT_OK, returnIntent);

                finish();
            }
        });

        findViewById(R.id.changeMapType).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Bottom sheet for changing map types
                new BottomSheet.Builder(MapActivity.this).title("Pick Map Type")
                        .sheet(R.menu.bottom_sheet).listener((DialogInterface.OnClickListener) (dialog, which) -> {
                            switch (which) {
                                case R.id.satellite:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                    break;
                                case R.id.hybrid:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                    break;
                                case R.id.terrain:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                    break;
                                case R.id.standard:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                    break;
                            }
                        }).show();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_json));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }

        // Check for GPS and get location.
        getLocation();

        mMap.setOnCameraIdleListener(() -> {
            // Create a location object for reverse geocoding
            Location location2 = new Location("");
            location2.setLatitude(mMap.getCameraPosition().target.latitude);
            location2.setLongitude(mMap.getCameraPosition().target.longitude);

            SmartLocation.
                    with(MapActivity.this)
                    .geocoding()
                    .reverse(location2, (location1, list) -> {
                        // Got Geocoding and Lat lng text values
                        if (list.size() > 0 && location1 != null){
                            latitude.setText(String.valueOf(location1.getLatitude()));
                            longitude.setText(String.valueOf(location1.getLongitude()));
                            gLocation.setText(String.valueOf(list.get(0).getAddressLine(0)));
                            bottomView.setVisibility(View.VISIBLE);
                        }
                    });
        });

    }

    private void getLocation() {
        if (checkGPSStatus()) {
            // Start Location listener
            SmartLocation
                    .with(MapActivity.this)
                    .location()
                    .oneFix()
                    .start(location -> {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));

                        // Create a location object for reverse geocoding
                        Location location2 = new Location("");
                        location2.setLatitude(currentLocation.latitude);
                        location2.setLongitude(currentLocation.longitude);

                        SmartLocation.
                                with(MapActivity.this)
                                .geocoding()
                                .reverse(location2, (location1, list) -> {
                                    // Got Geocoding and Lat lng text values
                                    if (location1 != null && list.size() > 0){
                                        latitude.setText(String.valueOf(location1.getLatitude()));
                                        longitude.setText(String.valueOf(location1.getLongitude()));
                                        gLocation.setText(String.valueOf(list.get(0).getAddressLine(0)));
                                        bottomView.setVisibility(View.VISIBLE);
                                    }
                                });
                    });
        } else {
            showBottomDialog("GPS not enabled", "GPS is necessary for your location.", "Turn on GPS", "Cancel");
        }
    }

    // TODO: Run application on different type of devices for location issue

    public boolean checkGPSStatus() {
        LocationManager locationManager = (LocationManager) MapActivity.this.getSystemService(Context.LOCATION_SERVICE);
        boolean GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return GpsStatus;
    }

    private void showBottomDialog(String title, String message, String positiveText, String negativeText) {
        new BottomDialog.Builder(this)
                .setTitle(title)
                .setContent(message)
                .setCancelable(false)
                .setIcon(R.drawable.grocerycart)
                .setPositiveText(positiveText)
                .onPositive(dialog12 -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 100);
                })
                .show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check for GPS and get location
        getLocation();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
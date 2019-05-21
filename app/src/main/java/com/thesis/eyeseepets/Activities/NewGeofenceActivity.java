package com.thesis.eyeseepets.Activities;

import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ndroid.CoolEditText;
import com.thesis.eyeseepets.Dialogs.CustomDialog;
import com.thesis.eyeseepets.Interfaces.API;
import com.thesis.eyeseepets.Models.GeoPointModel;
import com.thesis.eyeseepets.Models.PetModel;
import com.thesis.eyeseepets.R;
import com.thesis.eyeseepets.Utilities.Globals;
import com.thesis.eyeseepets.Utilities.RetrofitClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NewGeofenceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mvMap;
    private GoogleMap gmap;
    private String MAP_VIEW_BUNDLE_KEY;
    private FloatingActionButton fabToggleMapStyle;
    private int MAP_TOGGLE;
    private CoolEditText etName;
    private CoolEditText etRadius;

    private LatLng currPos;
    private Circle currCircle;
    private Marker currMarker;

    private Button btnConfirm;
    private Button btnback;

    private Retrofit retrofit;

    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_geofence);
        MAP_TOGGLE = 1;

        MAP_VIEW_BUNDLE_KEY = "EYESEEPETS";

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        btnback = findViewById(R.id.btnBack);
        btnConfirm = findViewById(R.id.btnConfirm);

        fabToggleMapStyle = findViewById(R.id.fabToggleMapStyle);
        etName = findViewById(R.id.etName);
        etRadius = findViewById(R.id.etRadius);

        mvMap = findViewById(R.id.mvMap);
        mvMap.onCreate(mapViewBundle);
        mvMap.getMapAsync(this);

        retrofit = RetrofitClient.getClient();
        loadViewActions();

        if (Globals.updateGeoPoint) {
            etName.setText(Globals.selectedGeoPoint.getName());
            etRadius.setText(Globals.selectedGeoPoint.getRadius().toString());
        }
    }

    private void loadViewActions() {
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialog customDialog = new CustomDialog();
                loadingDialog = customDialog.showLoadingDialog(NewGeofenceActivity.this);

                GeoPointModel geoPointModel = new GeoPointModel();
                geoPointModel.setName(etName.getText().toString());
                geoPointModel.setOwner(Globals.currentOwner.getId());
                geoPointModel.setRadius(Integer.parseInt(etRadius.getText().toString()));
                geoPointModel.setLatitude(currPos.latitude);
                geoPointModel.setLongitude(currPos.longitude);

                if (!Globals.updateGeoPoint) {
                    createGeoPoint(geoPointModel);
                }
                else {
                    geoPointModel.setId(Globals.selectedGeoPoint.getId());
                    updateGeoPoint(geoPointModel);
                }
            }
        });
    }

    private void createGeoPoint(final GeoPointModel geoPointModel) {
        API api = retrofit.create(API.class);

        Call<GeoPointModel> createNewGeoPoint = api.createGeoPoint(geoPointModel);

        createNewGeoPoint.enqueue(new Callback<GeoPointModel>() {
            @Override
            public void onResponse(Call<GeoPointModel> call, Response<GeoPointModel> response) {
                if (response != null && response.code() == 201) {
                    Toast.makeText(NewGeofenceActivity.this, "Geofence successfully created.", Toast.LENGTH_SHORT).show();
                    Globals.geoPointModelList.add(response.body());
                    loadingDialog.dismiss();
                    finish();
                }
                else {
                    Toast.makeText(NewGeofenceActivity.this, "Please supply all missing fields.", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<GeoPointModel> call, Throwable t) {
                Toast.makeText(NewGeofenceActivity.this, "Network error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGeoPoint(final GeoPointModel geoPointModel) {
        API api = retrofit.create(API.class);

        Call<GeoPointModel> updateSelectedGeoPoint = api.updateGeoPoint(geoPointModel, geoPointModel.getId());

        updateSelectedGeoPoint.enqueue(new Callback<GeoPointModel>() {
            @Override
            public void onResponse(Call<GeoPointModel> call, Response<GeoPointModel> response) {
                if (response != null && response.code() == 200) {
                    Toast.makeText(NewGeofenceActivity.this, "Geofence successfully updated.", Toast.LENGTH_SHORT).show();
                    Globals.geoPointModelList.get(Globals.selectedGeoPointIndex).setId(geoPointModel.getId());
                    Globals.geoPointModelList.get(Globals.selectedGeoPointIndex).setName(geoPointModel.getName());
                    Globals.geoPointModelList.get(Globals.selectedGeoPointIndex).setRadius(geoPointModel.getRadius());
                    Globals.geoPointModelList.get(Globals.selectedGeoPointIndex).setLatitude(geoPointModel.getLatitude());
                    Globals.geoPointModelList.get(Globals.selectedGeoPointIndex).setLongitude(geoPointModel.getLongitude());

                    loadingDialog.dismiss();
                    finish();
                }
                else {
                    Toast.makeText(NewGeofenceActivity.this, "Please supply all missing fields.", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<GeoPointModel> call, Throwable t) {
                Toast.makeText(NewGeofenceActivity.this, "Network error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadMapActions() {
        gmap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                gmap.clear();
                currMarker = gmap.addMarker(new MarkerOptions().position(latLng).draggable(true));
                currPos = currMarker.getPosition();
                addMapCircle(currPos, etRadius.getText().toString());
            }
        });


        fabToggleMapStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MAP_TOGGLE == 1)
                    MAP_TOGGLE += 1;
                else
                    MAP_TOGGLE = 1;

                switch (MAP_TOGGLE) {
                    case 1:
                        gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 2:
                        gmap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                }
            }
        });

        etRadius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                addMapCircle(currPos, String.valueOf(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void addMapCircle(LatLng latLng, String radius) {
        if (currCircle != null)
            currCircle.remove();

        try {
            currCircle = gmap.addCircle(new CircleOptions()
                    .strokeColor(ContextCompat.getColor(NewGeofenceActivity.this, R.color.colorCircleStroke))
                    .strokeWidth(1f)
                    .fillColor(ContextCompat.getColor(NewGeofenceActivity.this, R.color.colorCircleFill))
                    .center(latLng)
                    .radius(Double.valueOf(String.valueOf(radius))));
        }
        catch (Exception e) {
            currCircle.remove();

            Toast.makeText(this, "Invalid radius input.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mvMap.onSaveInstanceState(mapViewBundle);
    }
    @Override
    public void onResume() {
        super.onResume();
        mvMap.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mvMap.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mvMap.onStop();
    }
    @Override
    public void onPause() {
        mvMap.onPause();
        super.onPause();
    }
    @Override
    public void onDestroy() {
        mvMap.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mvMap.onLowMemory();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        LatLng ph = new LatLng(12.8797, 121.7740);
        gmap.moveCamera(CameraUpdateFactory.newLatLng(ph));
        gmap.animateCamera( CameraUpdateFactory.zoomTo( 5.0f ) );
        loadMapActions();

        if (Globals.updateGeoPoint) {
            currPos = new LatLng(Globals.selectedGeoPoint.getLatitude(), Globals.selectedGeoPoint.getLongitude());
            gmap.animateCamera( CameraUpdateFactory.newLatLngZoom(currPos, 19.0f ) );
            currMarker = gmap.addMarker(new MarkerOptions().position(currPos).draggable(true));
            addMapCircle(currPos, etRadius.getText().toString());

            gmap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    currCircle.remove();
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    currPos = marker.getPosition();
                    addMapCircle(currPos, etRadius.getText().toString());
                }
            });
        }
        else {
            SmartLocation.with(NewGeofenceActivity.this)
                    .location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            currPos = new LatLng(location.getLatitude(), location.getLongitude());
                            gmap.animateCamera( CameraUpdateFactory.newLatLngZoom(currPos, 19.0f ) );
                            currMarker = gmap.addMarker(new MarkerOptions().position(currPos).draggable(true));
                            addMapCircle(currPos, etRadius.getText().toString());

                            gmap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                                @Override
                                public void onMarkerDragStart(Marker marker) {
                                    currCircle.remove();
                                }

                                @Override
                                public void onMarkerDrag(Marker marker) {
                                }

                                @Override
                                public void onMarkerDragEnd(Marker marker) {
                                    currPos = marker.getPosition();
                                    addMapCircle(currPos, etRadius.getText().toString());
                                }
                            });
                        }
                    });
        }
    }
}

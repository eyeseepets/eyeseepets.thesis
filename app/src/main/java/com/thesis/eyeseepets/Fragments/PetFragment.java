package com.thesis.eyeseepets.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thesis.eyeseepets.Activities.LoginActivity;
import com.thesis.eyeseepets.Activities.NewGeofenceActivity;
import com.thesis.eyeseepets.Activities.NewPetActivity;
import com.thesis.eyeseepets.Dialogs.CustomDialog;
import com.thesis.eyeseepets.Interfaces.API;
import com.thesis.eyeseepets.Models.GeoPointModel;
import com.thesis.eyeseepets.Models.LocationModel;
import com.thesis.eyeseepets.Models.PetModel;
import com.thesis.eyeseepets.R;
import com.thesis.eyeseepets.Utilities.Globals;
import com.thesis.eyeseepets.Utilities.MarkerUtility;
import com.thesis.eyeseepets.Utilities.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PetFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public PetFragment() {
    }

    public static PetFragment newInstance(String param1, String param2) {
        PetFragment fragment = new PetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private MapView mvMap;
    private GoogleMap gmap;
    private String MAP_VIEW_BUNDLE_KEY;
    private int MAP_TOGGLE;

    private MenuItem miNewPet;
    private MenuItem miNewGeoPoint;
    private MenuItem miLogout;
    private MenuItem miSendReport;
    private MenuItem miUpdatePet;
    private MenuItem miUpdateGeoPoint;

    private TextView tvName;
    private TextView tvGender;
    private TextView tvPetName;
    private Circle currCircle;
    private FloatingActionButton fabSwitchPet;
    private FloatingActionButton fabTrackPet;
    private FloatingActionButton fabToggleMapStyle;
    private CircularImageView civPet;
    private Integer currentPetIndex;

    private Retrofit retrofit;

    private Marker petMarker;
    private Boolean isTracking;
    private CountDownTimer countDownTimer;
    private Call<LocationModel> getLatestLocation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        MAP_TOGGLE = 1;

        isTracking = true;

        petMarker = null;

        retrofit = RetrofitClient.getClient();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pet, container, false);
        MAP_VIEW_BUNDLE_KEY = "EYESEEPETS";

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mvMap = view.findViewById(R.id.mvMap);
        mvMap.onCreate(mapViewBundle);
        mvMap.getMapAsync(this);

        tvName = view.findViewById(R.id.tvName);
        tvGender = view.findViewById(R.id.tvGender);
        tvPetName = view.findViewById(R.id.tvPetName);
        civPet = view.findViewById(R.id.civPet);

        fabSwitchPet = view.findViewById(R.id.fabSwitchPet);
        fabToggleMapStyle = view.findViewById(R.id.fabToggleMapStyle);
        fabTrackPet = view.findViewById(R.id.fabTrackPet);

        if (Globals.petModelList.size() > 0)
            tvPetName.setText(Globals.petModelList.get(0).getName());

        loadViewActions();

        return view;
    }

    private void getLatestPetLocation() {
        API api = retrofit.create(API.class);

        getLatestLocation = api.getLatestLocation(Globals.currentPet.getId());

        getLatestLocation.enqueue(new Callback<LocationModel>() {
            @Override
            public void onResponse(Call<LocationModel> call, Response<LocationModel> response) {
                if (response != null && response.code() == 200) {
                    if (petMarker != null) {
                        petMarker.remove();
                    }

                    LocationModel latestLocation = response.body();

                    if (latestLocation.getDate() != null && Globals.currentPet != null) {
                        LatLng point = new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude());

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(point);
                        markerOptions.title(Globals.currentPet.getName());

                        if (Globals.currentPet.getImage() != null) {
                            MarkerUtility markerUtility = new MarkerUtility();
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerUtility.createCustomMarker(getActivity(), Globals.currentPet.getImage())));
                        }

                        petMarker = gmap.addMarker(markerOptions);

                        gmap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                CustomDialog customDialog = new CustomDialog();
                                customDialog.showPetDetailsDialog(Globals.currentPet, getActivity());
                                return false;
                            }
                        });

                        gmap.animateCamera( CameraUpdateFactory.newLatLngZoom(point, 19.0f ) );

                        if (isTracking) {
                            countDownTimer = new CountDownTimer(10000, 100) {
                                @Override
                                public void onTick(long l) {

                                }

                                @Override
                                public void onFinish() {
                                    if (Globals.currentPet != null)
                                        getLatestPetLocation();
                                }
                            }.start();
                        }
                    }
                    else {
                        isTracking = false;
                        fabTrackPet.setImageResource(R.drawable.ic_location_search_gray_24dp);
                        if (Globals.geoPointModelList.size() > 0) {
                            LatLng point = new LatLng(Globals.geoPointModelList.get(0).getLatitude(), Globals.geoPointModelList.get(0).getLongitude());
                            gmap.animateCamera( CameraUpdateFactory.newLatLngZoom(point, 19.0f ) );
                        }

                        Toast.makeText(getActivity(), "Your pet doesn't have a location yet.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LocationModel> call, Throwable t) {
            }
        });
    }

    private void loadViewActions() {
        fabSwitchPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (petMarker != null)
                    petMarker.remove();

                List<PetModel> petModelList = Globals.petModelList;
                int petListSize = petModelList.size();

                if (petListSize > 1) {
                    if (currentPetIndex == petListSize - 1)
                        currentPetIndex = 0;
                    else if (currentPetIndex < petListSize)
                        currentPetIndex += 1;
                }

                Globals.currentPet = Globals.petModelList.get(currentPetIndex);

                tvPetName.setText(Globals.currentPet.getName());

                if (Globals.currentPet != null)
                    getLatestPetLocation();

                if (Globals.currentPet.getImage() != null) {
                    byte[] decodedString = Base64.decode(Globals.currentPet.getImage(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    civPet.setImageBitmap(bitmap);
                }
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

        fabTrackPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTracking) {
                    isTracking = true;
                    if (Globals.currentPet != null)
                        getLatestPetLocation();
                    fabTrackPet.setImageResource(R.drawable.ic_location_searching_24dp);
                }
                else {
                    isTracking = false;
                    fabTrackPet.setImageResource(R.drawable.ic_location_search_gray_24dp);
                }
            }
        });
    }
    private void loadMenuFunctions() {
        miNewGeoPoint.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startActivity(new Intent(getActivity(), NewGeofenceActivity.class));
                return false;
            }
        });

        miNewPet.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startActivity(new Intent(getActivity(), NewPetActivity.class));
                return false;
            }
        });

        miLogout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                return false;
            }
        });

        miSendReport.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                CustomDialog customDialog = new CustomDialog();
                customDialog.showReportDialog(getActivity());
                return false;
            }
        });

        miUpdatePet.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Globals.updatePet = true;
                startActivity(new Intent(getActivity(), NewPetActivity.class));
                return false;
            }
        });

        miUpdateGeoPoint.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Globals.updateGeoPoint = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final String[] geofenceNames = new String[Globals.geoPointModelList.size()];
                for (int i = 0; i < Globals.geoPointModelList.size(); i++) {
                    geofenceNames[i] = Globals.geoPointModelList.get(i).getName();
                    Log.e("PET", String.valueOf(Globals.geoPointModelList.get(i).getId()));
                }
                builder.setTitle("Pick a Geofence");
                builder.setItems(geofenceNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Globals.selectedGeoPoint = new GeoPointModel();
                        Globals.selectedGeoPointIndex = which;
                        Globals.selectedGeoPoint.setId(Globals.geoPointModelList.get(which).getId());
                        Globals.selectedGeoPoint.setLongitude(Globals.geoPointModelList.get(which).getLongitude());
                        Globals.selectedGeoPoint.setLatitude(Globals.geoPointModelList.get(which).getLatitude());
                        Globals.selectedGeoPoint.setRadius(Globals.geoPointModelList.get(which).getRadius());
                        Globals.selectedGeoPoint.setName(Globals.geoPointModelList.get(which).getName());
                        startActivity(new Intent(getActivity(), NewGeofenceActivity.class));
                    }
                });
                builder.show();
                return false;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);

        miNewGeoPoint = menu.findItem(R.id.miNewGeoPoint);
        miNewPet = menu.findItem(R.id.miNewPet);
        miLogout = menu.findItem(R.id.miLogout);
        miSendReport = menu.findItem(R.id.miSendReport);
        miUpdatePet = menu.findItem(R.id.miUpdatePet).setVisible(true);
        miUpdateGeoPoint = menu.findItem(R.id.miUpdateGeoPoint).setVisible(true);

        loadMenuFunctions();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        Globals.updatePet = false;
        Globals.updateGeoPoint = false;
        Globals.selectedGeoPoint = null;
        Globals.selectedGeoPointIndex = null;

        List<PetModel> petModelList = Globals.petModelList;
        if (petModelList.size() > 0) {
            if (currentPetIndex == null)
                currentPetIndex = 0;

            Globals.currentPet = petModelList.get(currentPetIndex);
            tvName.setText(Globals.currentPet.getName());
            tvGender.setText(Globals.currentPet.getGender());

            if (Globals.currentPet.getImage() != null) {
                byte[] decodedString = Base64.decode(Globals.currentPet.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                civPet.setImageBitmap(bitmap);
            }
        }

        if (gmap != null)
            updateGeoPointsInMap();

        if (gmap != null && Globals.currentPet != null) {
            isTracking = true;
            getLatestPetLocation();
            tvPetName.setText(Globals.currentPet.getName());
            fabTrackPet.setImageResource(R.drawable.ic_location_searching_24dp);
        }
    }

    private void addMapCircle(LatLng latLng, String radius) {
        try {
            currCircle = gmap.addCircle(new CircleOptions()
                    .strokeColor(ContextCompat.getColor(getActivity(), R.color.colorCircleStroke))
                    .strokeWidth(1f)
                    .fillColor(ContextCompat.getColor(getActivity(), R.color.colorCircleFill))
                    .center(latLng)
                    .radius(Double.valueOf(String.valueOf(radius))));
        }
        catch (Exception e) {
            currCircle.remove();

            Toast.makeText(getActivity(), "INVALID RADIUS", Toast.LENGTH_SHORT).show();
        }
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

        if (getLatestLocation != null)
            getLatestLocation.cancel();

        if (countDownTimer != null)
            countDownTimer.cancel();

        isTracking = false;
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

        updateGeoPointsInMap();

        gmap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                CustomDialog customDialog = new CustomDialog();
                customDialog.showCoordinatesDialog(latLng, getActivity());
            }
        });

        if (Globals.currentPet != null)
            getLatestPetLocation();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void updateGeoPointsInMap() {
        List<GeoPointModel> geoPointModelList = Globals.geoPointModelList;
        gmap.clear();
        if (geoPointModelList.size() > 0) {
            for (GeoPointModel geoPointModel: geoPointModelList) {
                LatLng point = new LatLng(geoPointModel.getLatitude(), geoPointModel.getLongitude());
                addMapCircle(point, geoPointModel.getRadius().toString());
            }
        }
    }
}

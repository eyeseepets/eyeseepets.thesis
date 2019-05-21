package com.thesis.eyeseepets.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.thesis.eyeseepets.Adapters.MainPagerAdapter;
import com.thesis.eyeseepets.Fragments.ChatFragment;
import com.thesis.eyeseepets.Fragments.HomeFragment;
import com.thesis.eyeseepets.Fragments.PetFragment;
import com.thesis.eyeseepets.Fragments.SettingsFragment;
import com.thesis.eyeseepets.R;
import com.thesis.eyeseepets.Utilities.Globals;
import com.thesis.eyeseepets.Utilities.RetrofitClient;

import org.chat21.android.ui.ChatUI;

import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener,
        ChatFragment.OnFragmentInteractionListener, PetFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener {

    private TabLayout tlNavigator;
    private ViewPager vpMain;
    private PagerAdapter pagerAdapter;
    private String TAG = "FIREBASE";
    private FirebaseAuth mAuth;
    private MenuInflater menuInflater;
    private Retrofit retrofit;
    private Boolean ownerFetched;
    private Boolean geoPointsFetched;
    private Boolean dialogOnScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ownerFetched = false;
        geoPointsFetched = false;
        dialogOnScreen = false;
        retrofit = RetrofitClient.getClient();

        setContentView(R.layout.activity_main);
        Globals.lastVisibleFragment = 0;

        checkPermissions();

        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        tlNavigator = findViewById(R.id.tlNavigator);
        tlNavigator.addTab(tlNavigator.newTab().setIcon(R.drawable.home_black_24dp));
        tlNavigator.addTab(tlNavigator.newTab().setIcon(R.drawable.ic_pets_black_24dp));
        tlNavigator.addTab(tlNavigator.newTab().setIcon(R.drawable.ic_chat_black_24dp));
        tlNavigator.setTabGravity(TabLayout.GRAVITY_FILL);

        vpMain = findViewById(R.id.vpMain);
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), tlNavigator.getTabCount());
        vpMain.setAdapter(pagerAdapter);
        vpMain.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tlNavigator));

        // SET TAB BUTTONS COLOR
        tlNavigator.getTabAt(0).getIcon().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorSelectedTab), PorterDuff.Mode.SRC_IN);
        tlNavigator.getTabAt(1).getIcon().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorTabButtons), PorterDuff.Mode.SRC_IN);
        tlNavigator.getTabAt(2).getIcon().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorTabButtons), PorterDuff.Mode.SRC_IN);

        tlNavigator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() != 2) {
                    vpMain.setCurrentItem(tab.getPosition());
                    tab.getIcon().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorSelectedTab), PorterDuff.Mode.SRC_IN);
                    Globals.lastVisibleFragment = tab.getPosition();
                } else {
                    ChatUI.getInstance().openConversationsListActivity();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorTabButtons), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Gson gson = new Gson();
    }

    public void checkPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkGPSAvailability();
                } else {
                    finish();
                }
                break;
        }

    }

    private void checkGPSAvailability() {
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS is not enabled. Redirecting to settings.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        tlNavigator.getTabAt(Globals.lastVisibleFragment).select();

        checkUserDataCompleteness();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void showNoPetDialog() {
        dialogOnScreen = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_no_pet, null);
        Button btnOk = view.findViewById(R.id.btnOk);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogOnScreen = false;
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, NewPetActivity.class));
            }
        });
    }

    private void showNoGeoPointDialog() {
        dialogOnScreen = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_no_geo_point, null);
        Button btnOk = view.findViewById(R.id.btnOk);

        TextView tvHello;
        tvHello = view.findViewById(R.id.tvHello);

        tvHello.setText("Hello, " + Globals.currentUser.getDisplayName() + "!");

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogOnScreen = false;
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, NewGeofenceActivity.class));
            }
        });
    }

    private void checkUserDataCompleteness() {
        if (Globals.geoPointModelList.size() == 0) {
            if (!dialogOnScreen)
                showNoGeoPointDialog();
        }
        else if (Globals.petModelList.size() == 0) {
            if (!dialogOnScreen)
                showNoPetDialog();
        }
    }
}

package com.thesis.eyeseepets.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ndroid.CoolEditText;
import com.thesis.eyeseepets.Dialogs.CustomDialog;
import com.thesis.eyeseepets.Interfaces.API;
import com.thesis.eyeseepets.Models.GeoPointModel;
import com.thesis.eyeseepets.Models.OwnerModel;
import com.thesis.eyeseepets.R;
import com.thesis.eyeseepets.Utilities.Globals;
import com.thesis.eyeseepets.Utilities.RetrofitClient;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin;
    private Button btnSignUp;
    private CoolEditText etEmail;
    private CoolEditText etPassword;
    private String TAG;

    private FirebaseAuth mAuth;

    private CustomDialog customDialog;
    private AlertDialog loadingDialog;

    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TAG = "FIREBASE";
        Globals.currentUser = null;
        retrofit = RetrofitClient.getClient();

        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        Globals.currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (Globals.currentUser != null) {
            customDialog = new CustomDialog();
            loadingDialog = customDialog.showLoadingDialog(LoginActivity.this);
            getCurrentOwner();
        }

        loadViewActions();
    }

    private void loadViewActions() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etEmail.getText().toString().equalsIgnoreCase("") && !etPassword.getText().toString().equalsIgnoreCase("")) {
                    customDialog = new CustomDialog();
                    loadingDialog = customDialog.showLoadingDialog(LoginActivity.this);
                    signInToFirebase(etEmail.getText().toString(), etPassword.getText().toString());
                }
                else {
                    Toast.makeText(LoginActivity.this, "Please enter your email address and password.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
    }

    private void redirectToHome() {
        softInitChatSDK();

        startActivity(new Intent(LoginActivity.this, MainActivity.class));

        if (loadingDialog != null)
            loadingDialog.dismiss();

        finishAffinity();
    }

    private void signInToFirebase(final String email, final String password) {
        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        Globals.currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        getCurrentOwner();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());

                        Toast.makeText(LoginActivity.this, "Invalid email address or password.", Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                }
            });
    }

    private void softInitChatSDK() {
        // it creates the chat configurations
        ChatManager.Configuration mChatConfiguration =
                new ChatManager.Configuration.Builder(getString(R.string.chat_firebase_appId))
                        .firebaseUrl(getString(R.string.chat_firebase_url))
                        .storageBucket(getString(R.string.chat_firebase_storage_bucket))
                        .build();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        IChatUser iChatUser = new ChatUser();
        iChatUser.setId(currentUser.getUid());
        iChatUser.setEmail(currentUser.getEmail());
        iChatUser.setFullName(currentUser.getDisplayName());

        ChatManager.start(this, mChatConfiguration, iChatUser);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getCurrentOwner() {
        API api = retrofit.create(API.class);

        Call<List<OwnerModel>> getOwner = api.getOwner(Globals.currentUser.getEmail());

        getOwner.enqueue(new Callback<List<OwnerModel>>() {
            @Override
            public void onResponse(Call<List<OwnerModel>> call, Response<List<OwnerModel>> response) {
                if (response != null && response.code() == 200) {
                    List<OwnerModel> ownerModels = response.body();
                    Globals.currentOwner = ownerModels.get(0);
                    Globals.petModelList = ownerModels.get(0).getPets();
                    getGeoPoints();
                }
                else {
                    FirebaseAuth.getInstance().signOut();
                    loadingDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OwnerModel>> call, Throwable t) {
                FirebaseAuth.getInstance().signOut();
                loadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Network error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                Log.e("LOGIN", t.toString());
            }
        });
    }

    private void getGeoPoints() {
        API api = retrofit.create(API.class);

        Call<List<GeoPointModel>> getGeoPointList = api.getGeoPoints(Globals.currentOwner.getId());

        getGeoPointList.enqueue(new Callback<List<GeoPointModel>>() {
            @Override
            public void onResponse(Call<List<GeoPointModel>> call, Response<List<GeoPointModel>> response) {
                if (response != null && response.code() == 200) {
                    Globals.geoPointModelList = response.body();

                    redirectToHome();
                }
            }

            @Override
            public void onFailure(Call<List<GeoPointModel>> call, Throwable t) {
            }
        });
    }
}

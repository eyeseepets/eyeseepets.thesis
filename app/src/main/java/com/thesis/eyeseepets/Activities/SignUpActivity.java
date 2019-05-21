package com.thesis.eyeseepets.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.ndroid.CoolEditText;
import com.ndroid.CoolImage;
import com.thesis.eyeseepets.Dialogs.CustomDialog;
import com.thesis.eyeseepets.Interfaces.API;
import com.thesis.eyeseepets.Models.GeoPointModel;
import com.thesis.eyeseepets.Models.OwnerModel;
import com.thesis.eyeseepets.R;
import com.thesis.eyeseepets.Statics.Intents;
import com.thesis.eyeseepets.Utilities.Globals;
import com.thesis.eyeseepets.Utilities.RetrofitClient;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.authentication.ChatAuthentication;
import org.chat21.android.core.contacts.listeners.OnContactCreatedCallback;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignUpActivity extends AppCompatActivity {
    private RelativeLayout rlSignUp;

    private Button btnAdd;
    private Button btnBack;
    private Button btnConfirm;

    private CoolEditText etEmail;
    private CoolEditText etPassword;
    private CoolEditText etFirstName;
    private CoolEditText etLastName;
    private CoolEditText etConfirmPassword;

    private CircularImageView civOwner;

    private String TAG;

    private Retrofit retrofit;
    private Gson gson;

    private CustomDialog customDialog;
    private AlertDialog loadingDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        TAG = "FIREBASE";

        // TOOLBAR VIEWS
        rlSignUp = findViewById(R.id.rlSignUp);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // MAIN VIEWS
        civOwner = findViewById(R.id.civOwner);
        btnAdd = findViewById(R.id.btnAdd);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        mAuth = FirebaseAuth.getInstance();

        gson = new Gson();
        retrofit = RetrofitClient.getClient();

        loadViewActions();
    }

    private void loadViewActions() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currEmail = etEmail.getText().toString();
                Log.e("EMAIL", currEmail);
                String currPassword = etPassword.getText().toString();
                String currConfirmPassword = etConfirmPassword.getText().toString();

                Log.e("PASSWORD", currPassword);
                Log.e("PASSWORD", currConfirmPassword);
                if (currEmail.length() > 14 &&
                        currEmail.length() < 35 && currEmail.contains("@") && currEmail.contains(".com")) {
                    if (currPassword.equalsIgnoreCase(currConfirmPassword)) {
                        if (currPassword.length() > 5) {
                            try {
                                customDialog = new CustomDialog();
                                loadingDialog = customDialog.showLoadingDialog(SignUpActivity.this);

                                registerUserToFirebase(etEmail.getText().toString(),
                                        etPassword.getText().toString(),
                                        etFirstName.getText().toString(),
                                        etLastName.getText().toString());
                            }
                            catch (Exception e) {
                                Log.e("ERROR", e.toString());
                            }
                        }
                        else {
                            Toast.makeText(SignUpActivity.this, "The password should contain at least 6 characters.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(SignUpActivity.this, "Input confirm password does not match with the entered password.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    if (!(currEmail.contains("@") && currEmail.contains(".com")))
                        Toast.makeText(SignUpActivity.this, "Invalid email address.", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(SignUpActivity.this, "Your email address is either too short or too long.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, Intents.SELECT_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case Intents.SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                    civOwner.setImageBitmap(yourSelectedImage);
                }
        }
    }

    private void redirectToLogin() {
        Toast.makeText(SignUpActivity.this, "clicked confirm", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finishAffinity();
    }

    private void redirectToHome() {
        if (loadingDialog != null)
            loadingDialog.dismiss();

        softInitChatSDK();

        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        finishAffinity();
    }

    private void registerUserToFirebase(final String email, final String password, final String firstName, final String lastName) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(firstName + " " + lastName).build();

                        user.updateProfile(profileUpdates);

                        OwnerModel ownerModel = new OwnerModel();
                        ownerModel.setEmail(email);
                        ownerModel.setUsername(email);
                        ownerModel.setPassword(password);
                        ownerModel.setFirebaseId(user.getUid());
                        ownerModel.setFirstName(firstName);
                        ownerModel.setLastName(lastName);

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        Bitmap bitmap = ((BitmapDrawable)civOwner.getDrawable()).getBitmap();
                        Bitmap rescaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                        rescaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream .toByteArray();

                        ownerModel.setImage(Base64.encodeToString(byteArray, Base64.DEFAULT));

                        Log.e("RESP", "CREATING OWNER");

                        createOwner(user, ownerModel, email, password, firstName, lastName);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();

                        loadingDialog.dismiss();
                    }
                }
            });
    }

    private void signInToFirebase(final String email, final String password, final String firstName, final String lastName) {
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
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    }
                });
    }

    private void authWithEmail(String email, String password, final String firstName, final String lastName) {
        ChatManager.startWithEmailAndPassword(this, getString(R.string.chat_firebase_appId),
                email, password, new ChatAuthentication.OnChatLoginCallback() {
                    @Override
                    public void onChatLoginSuccess(IChatUser currentUser) {
                        ChatManager.getInstance().createContactFor(currentUser.getId(), currentUser.getEmail(),
                                firstName, lastName, new OnContactCreatedCallback() {
                                    @Override
                                    public void onContactCreatedSuccess(ChatRuntimeException exception) {
                                        if (exception == null) {
                                            Log.e("SUCCESS", "NEW CONTACT ADDED");
                                        } else {
                                            Log.e("FAIL", "NEW CONTACT NOT ADDED");
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onChatLoginError(Exception e) {
                        Log.e("FIREBASE", "LOGIN ERROR");
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

    private void createOwner(final FirebaseUser user, OwnerModel ownerModel, final String email, final String password, final String firstName, final String lastName) {
        API api = retrofit.create(API.class);

        Call<OwnerModel> createNewOwner = api.createOwner(ownerModel);

        createNewOwner.enqueue(new Callback<OwnerModel>() {
            @Override
            public void onResponse(Call<OwnerModel> call, Response<OwnerModel> response) {
                if (response != null && response.code() == 201) {
                    Globals.currentOwner = response.body();

                    authWithEmail(user.getEmail(), password, firstName, lastName);

                    Globals.currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    getCurrentOwner();
                }
            }

            @Override
            public void onFailure(Call<OwnerModel> call, Throwable t) {
                deleteFirebaseUser(user, password);
            }
        });
    }

    private void deleteFirebaseUser(final FirebaseUser user, String password) {
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), password);

        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                user.delete();
                FirebaseAuth.getInstance().signOut();
            }
        });
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
                    Toast.makeText(SignUpActivity.this, "Oops. Something went wrong! Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OwnerModel>> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "Network error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
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

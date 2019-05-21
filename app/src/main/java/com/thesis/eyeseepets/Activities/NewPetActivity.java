package com.thesis.eyeseepets.Activities;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thesis.eyeseepets.Dialogs.CustomDialog;
import com.thesis.eyeseepets.Interfaces.API;
import com.thesis.eyeseepets.Models.GeoPointModel;
import com.thesis.eyeseepets.Models.PetModel;
import com.thesis.eyeseepets.R;
import com.thesis.eyeseepets.Statics.Genders;
import com.thesis.eyeseepets.Statics.Intents;
import com.thesis.eyeseepets.Utilities.Globals;
import com.thesis.eyeseepets.Utilities.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NewPetActivity extends AppCompatActivity {
    private Button btnAdd;
    private EditText etName;
    private EditText etBirthdate;
    private EditText etGeofence;
    private EditText etBreed;
    private EditText etDescription;
    private EditText etPhoneNumber;
    private EditText etcolor;
    private EditText etSize;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private Integer selectedGeofence;
    private AlertDialog loadingDialog;
    private Integer currentPetIndex;

    private CircularImageView civPet;

    private Button btnBack;
    private Button btnConfirm;

    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pet);

        btnAdd = findViewById(R.id.btnAdd);
        etName = findViewById(R.id.etName);
        etBirthdate = findViewById(R.id.etBirthdate);
        etBreed = findViewById(R.id.etBreed);
        etGeofence = findViewById(R.id.etGeofence);
        etDescription = findViewById(R.id.etDescription);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etcolor = findViewById(R.id.etColor);
        etSize = findViewById(R.id.etSize);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);

        civPet = findViewById(R.id.civPet);

        btnBack = findViewById(R.id.btnBack);
        btnConfirm = findViewById(R.id.btnConfirm);

        retrofit = RetrofitClient.getClient();

        if (Globals.updatePet) {
            PetModel currentPet = Globals.currentPet;

            currentPetIndex = Globals.petModelList.indexOf(currentPet);

            Log.e("INDEX", currentPetIndex.toString());

            etName.setText(currentPet.getName());
            etBirthdate.setText(currentPet.getBirthdate());
            etBreed.setText(currentPet.getBreed());
            etDescription.setText(currentPet.getDescription());
            etPhoneNumber.setText(currentPet.getAssignedSim());
            etcolor.setText(currentPet.getColor());
            etSize.setText(currentPet.getSize());
            if (currentPet.getImage() != null) {
                byte[] decodedString = Base64.decode(Globals.currentPet.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                civPet.setImageBitmap(bitmap);
            }

            if (currentPet.getGender().equalsIgnoreCase(Genders.MALE))
                rbMale.setChecked(true);
            else
                rbFemale.setChecked(true);

            for (int i = 0; i < Globals.geoPointModelList.size(); i++)
                if (Globals.geoPointModelList.get(i).getId() == currentPet.getFence())
                    etGeofence.setText(Globals.geoPointModelList.get(i).getName());
        }

        selectedGeofence = Globals.geoPointModelList.get(0).getId();
        etGeofence.setText(Globals.geoPointModelList.get(0).getName());

        loadViewAction();
    }

    private void loadViewAction() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialog customDialog = new CustomDialog();
                loadingDialog = customDialog.showLoadingDialog(NewPetActivity.this);

                PetModel petModel = new PetModel();
                petModel.setName(etName.getText().toString());
                petModel.setBirthdate(etBirthdate.getText().toString());
                petModel.setOwner(Globals.currentOwner.getId());
                petModel.setBreed(etBreed.getText().toString());
                petModel.setImage("");
                petModel.setDescription(etDescription.getText().toString());
                petModel.setAssignedSim(etPhoneNumber.getText().toString());
                petModel.setFence(selectedGeofence);
                petModel.setColor(etcolor.getText().toString());
                petModel.setSize(etSize.getText().toString());

                if (rbMale.isChecked())
                    petModel.setGender(Genders.MALE);
                else
                    petModel.setGender(Genders.FEMALE);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Bitmap bitmap = ((BitmapDrawable)civPet.getDrawable()).getBitmap();
                Bitmap rescaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
                rescaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();

                petModel.setImage(Base64.encodeToString(byteArray, Base64.DEFAULT));

                Log.e("PET", new Gson().toJson(petModel));

                if (!Globals.updatePet) {
                    createNewPet(petModel);
                }
                else{
                    petModel.setId(Globals.currentPet.getId());
                    updateCurrentPet(petModel);
                }
            }
        });

        etBirthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(NewPetActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        etBirthdate.setText(String.valueOf(i) + "-" +
                                String.format("%02d", i1 + 1) + "-" +
                                String.format("%02d", i2));
                    }
                }, Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        etGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewPetActivity.this);
                final String[] geofenceNames = new String[Globals.geoPointModelList.size()];
                for (int i = 0; i < Globals.geoPointModelList.size(); i++) {
                    geofenceNames[i] = Globals.geoPointModelList.get(i).getName();
                    Log.e("PET", String.valueOf(Globals.geoPointModelList.get(i).getId()));
                }
                builder.setTitle("Pick a Geofence");
                builder.setItems(geofenceNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etGeofence.setText(geofenceNames[which]);
                        selectedGeofence = Globals.geoPointModelList.get(which).getId();
                    }
                });
                builder.show();
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
                    civPet.setImageBitmap(yourSelectedImage);
                }
        }
    }

    private void createNewPet(PetModel petModel) {
        API api = retrofit.create(API.class);

        Call<PetModel> createNewPet = api.createPet(petModel);

        createNewPet.enqueue(new Callback<PetModel>() {
            @Override
            public void onResponse(Call<PetModel> call, Response<PetModel> response) {
                if (response != null && response.code() == 201) {
                    Toast.makeText(NewPetActivity.this, "Pet successfully registered.", Toast.LENGTH_SHORT).show();
                    Globals.petModelList.add(response.body());
                    loadingDialog.dismiss();
                    finish();
                }
                else {
                    loadingDialog.dismiss();
                    Toast.makeText(NewPetActivity.this, "Please supply all missing fields.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PetModel> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(NewPetActivity.this, "Network error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCurrentPet(PetModel petModel) {
        API api = retrofit.create(API.class);

        Call<PetModel> updatePet = api.updatePet(petModel, petModel.getId());

        Log.e("PET", petModel.getId().toString());

        updatePet.enqueue(new Callback<PetModel>() {
            @Override
            public void onResponse(Call<PetModel> call, Response<PetModel> response) {
                if (response != null && response.code() == 200) {
                    Toast.makeText(NewPetActivity.this, "Pet data successfully updated.", Toast.LENGTH_SHORT).show();
                    Globals.petModelList.set(currentPetIndex, response.body());
                    loadingDialog.dismiss();
                    finish();
                }
                else {
                    Toast.makeText(NewPetActivity.this, "Please supply all missing fields.", Toast.LENGTH_SHORT).show();
                    Log.e("PET", response.raw().request().url().toString());
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<PetModel> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(NewPetActivity.this, "Network error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
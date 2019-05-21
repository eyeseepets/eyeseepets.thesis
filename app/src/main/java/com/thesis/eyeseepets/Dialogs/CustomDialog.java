package com.thesis.eyeseepets.Dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.thesis.eyeseepets.Activities.NewGeofenceActivity;
import com.thesis.eyeseepets.Interfaces.API;
import com.thesis.eyeseepets.Models.GeoPointModel;
import com.thesis.eyeseepets.Models.PetModel;
import com.thesis.eyeseepets.Models.ReportModel;
import com.thesis.eyeseepets.R;
import com.thesis.eyeseepets.Utilities.Globals;
import com.thesis.eyeseepets.Utilities.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CustomDialog {
    private Retrofit retrofit;
    private Integer selectedPet = null;

    public CustomDialog() {
        retrofit = RetrofitClient.getClient();
    }

    public AlertDialog showLoadingDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_loading, null);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        return dialog;
    }

    public AlertDialog showReportDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_report, null);
        final Button btnSend = view.findViewById(R.id.btnSend);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        final EditText etSubject = view.findViewById(R.id.etSubject);
        final EditText etDetails = view.findViewById(R.id.etDetails);
        final EditText etPet = view.findViewById(R.id.etPet);
        etPet.setText(Globals.petModelList.get(0).getName());
        selectedPet = Globals.petModelList.get(0).getId();

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Globals.updateGeoPoint = true;

        etPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                final String[] petNames = new String[Globals.petModelList.size()];
                for (int i = 0; i < Globals.petModelList.size(); i++) {
                    petNames[i] = Globals.petModelList.get(i).getName();
                    Log.e("PET", String.valueOf(Globals.petModelList.get(i).getId()));
                }
                builder.setTitle("Select a logo_pet");
                builder.setItems(petNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedPet = Globals.petModelList.get(which).getId();
                        etPet.setText(petNames[which]);
                    }
                });
                builder.show();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog loadingDialog = showLoadingDialog(activity);

                if (!(etSubject.getText().toString().equalsIgnoreCase("") || etDetails.getText().toString().equalsIgnoreCase(""))) {
                    ReportModel reportModel = new ReportModel();
                    reportModel.setSubject(etSubject.getText().toString());
                    reportModel.setMessage(etDetails.getText().toString());
                    reportModel.setOwner(Globals.currentOwner.getId());
                    reportModel.setPet(selectedPet);

                    sendReport(reportModel, activity, dialog, loadingDialog, btnSend);
                    btnSend.setEnabled(false);
                }
                else {
                    Toast.makeText(activity, "Please supply all missing fields.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return dialog;
    }

    private void sendReport(ReportModel reportModel, final Activity activity, final AlertDialog dialog, final AlertDialog loadingDialog,final Button sendButton) {
        API api = retrofit.create(API.class);

        Call<ReportModel> createNewReport = api.createReport(reportModel);

        createNewReport.enqueue(new Callback<ReportModel>() {
            @Override
            public void onResponse(Call<ReportModel> call, Response<ReportModel> response) {
                if (response != null && response.code() == 201) {
                    Toast.makeText(activity, "Report successfully sent.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(activity, "Something went wrong.", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
                loadingDialog.dismiss();
                sendButton.setEnabled(true);
            }

            @Override
            public void onFailure(Call<ReportModel> call, Throwable t) {
                Toast.makeText(activity, "Network error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadingDialog.dismiss();
                sendButton.setEnabled(true);
            }
        });
    }

    public void showPetDetailsDialog(PetModel petModel, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_pet_details, null);
        Button btnOk = view.findViewById(R.id.btnOk);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvDetails = view.findViewById(R.id.tvDetails);

        tvName.setText("Hi there! My name is " + petModel.getName() + "!");
        tvDetails.setText("I am a " + petModel.getGender().toLowerCase() + " " + petModel.getBreed()
                + ".\nDescription: " + petModel.getDescription()
                + "\nColor: " + petModel.getColor()
                + "\nSize: " + petModel.getSize());

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void showCoordinatesDialog(LatLng latLng, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_lat_long, null);
        Button btnOk = view.findViewById(R.id.btnOk);
        TextView tvLatitude = view.findViewById(R.id.tvLatitude);
        TextView tvLongitude = view.findViewById(R.id.tvLongitude);

        tvLatitude.setText(tvLatitude.getText().toString() + String.valueOf(latLng.latitude));
        tvLongitude.setText(tvLongitude.getText().toString() + String.valueOf(latLng.longitude));

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}

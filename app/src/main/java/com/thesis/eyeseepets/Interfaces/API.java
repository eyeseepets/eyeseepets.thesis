package com.thesis.eyeseepets.Interfaces;

import com.thesis.eyeseepets.Models.GeoPointModel;
import com.thesis.eyeseepets.Models.LocationModel;
import com.thesis.eyeseepets.Models.OwnerModel;
import com.thesis.eyeseepets.Models.PetModel;
import com.thesis.eyeseepets.Models.ReportModel;

import java.security.acl.Owner;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface API {
    String BASE_URL = "https://eye-see-pets.herokuapp.com/";

    @POST("users/create/")
    Call<OwnerModel> createOwner(@Body OwnerModel ownerModel);

    @POST("pets/create/")
    Call<PetModel> createPet(@Body PetModel petModel);

    @PATCH("pets/{id}/edit")
    Call<PetModel> updatePet(@Body PetModel petModel, @Path("id") int id);

    @PATCH("pets/geopoints/{id}/edit")
    Call<GeoPointModel> updateGeoPoint(@Body GeoPointModel geoPointModel, @Path("id") int id);

    @POST("pets/geopoints/create/")
    Call<GeoPointModel> createGeoPoint(@Body GeoPointModel geoPointModel);

    @POST("pets/message/create/")
    Call<ReportModel> createReport(@Body ReportModel reportModel);

    @GET("users/?")
    Call<List<OwnerModel>> getOwner(@Query("user") String username);

    @GET("/pets/geopoints/?")
    Call<List<GeoPointModel>> getGeoPoints(@Query("user") Integer id);

    @GET("/pets/location/latest/?")
    Call<LocationModel> getLatestLocation(@Query("id") Integer id);
}

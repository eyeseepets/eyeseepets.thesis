package com.thesis.eyeseepets.Utilities;

import com.google.firebase.auth.FirebaseUser;
import com.thesis.eyeseepets.Fragments.HomeFragment;
import com.thesis.eyeseepets.Fragments.PetFragment;
import com.thesis.eyeseepets.Models.GeoPointModel;
import com.thesis.eyeseepets.Models.OwnerModel;
import com.thesis.eyeseepets.Models.PetModel;

import java.util.List;

public class Globals {
    public static FirebaseUser currentUser = null;
    public static HomeFragment homeFragment = new HomeFragment();
    public static PetFragment petFragment = new PetFragment();
    public static int lastVisibleFragment = 0;

    public static OwnerModel currentOwner = null;

    public static GeoPointModel selectedGeoPoint = null;
    public static Integer selectedGeoPointIndex = null;

    public static List<GeoPointModel> geoPointModelList = null;

    public static List<PetModel> petModelList = null;

    public static PetModel currentPet = null;

    public static Boolean updatePet = null;

    public static Boolean updateGeoPoint = null;
}

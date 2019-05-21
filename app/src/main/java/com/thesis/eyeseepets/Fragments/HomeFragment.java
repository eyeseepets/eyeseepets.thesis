package com.thesis.eyeseepets.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.ndroid.CoolButton;
import com.thesis.eyeseepets.Activities.LoginActivity;
import com.thesis.eyeseepets.Activities.NewGeofenceActivity;
import com.thesis.eyeseepets.Activities.NewPetActivity;
import com.thesis.eyeseepets.Activities.SignUpActivity;
import com.thesis.eyeseepets.Dialogs.CustomDialog;
import com.thesis.eyeseepets.R;
import com.thesis.eyeseepets.Utilities.Globals;

import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private TextView tvWelcome;
    private TextView tvFullName;
    private TextView tvEmail;
    private ConstraintLayout clReplaceable;
    private ConstraintLayout clUnsafe;
    private ConstraintLayout clSafe;
    private TransitionDrawable transition;
    private CircularImageView civOwner;

    private MenuItem miNewPet;
    private MenuItem miNewGeoPoint;
    private MenuItem miLogout;
    private MenuItem miSendReport;
    private MenuItem miUpdateUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        clReplaceable = view.findViewById(R.id.clReplaceable);
        clUnsafe = view.findViewById(R.id.clUnsafe);
        clSafe = view.findViewById(R.id.clSafe);

        tvFullName = view.findViewById(R.id.tvFullName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        civOwner = view.findViewById(R.id.civOwner);

        tvFullName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        tvEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        if (Globals.currentOwner.getImage() != null) {
            byte[] decodedString = Base64.decode(Globals.currentOwner.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            civOwner.setImageBitmap(bitmap);
        }

        tvWelcome.setText("Welcome back, " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        return view;
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);

        miNewGeoPoint = menu.findItem(R.id.miNewGeoPoint).setVisible(false);
        miNewPet = menu.findItem(R.id.miNewPet).setVisible(false);
        miLogout = menu.findItem(R.id.miLogout);
        miSendReport = menu.findItem(R.id.miSendReport);

        loadMenuFunctions();
    }

    private void loadViewActions() {

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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

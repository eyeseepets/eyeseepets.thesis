package com.thesis.eyeseepets.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.thesis.eyeseepets.Fragments.ChatFragment;
import com.thesis.eyeseepets.Fragments.HomeFragment;
import com.thesis.eyeseepets.Fragments.PetFragment;
import com.thesis.eyeseepets.Utilities.Globals;

public class MainPagerAdapter extends FragmentStatePagerAdapter {

    private int tabCount;

    public MainPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Globals.homeFragment = new HomeFragment();
                    return Globals.homeFragment;
                case 1:
                    Globals.petFragment = new PetFragment();
                    return Globals.petFragment;
                case 2:
                    ChatFragment chatFragment = new ChatFragment();
                    return chatFragment;
                default:
                    return null;
            }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}

package com.your.mychat.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.your.mychat.ChatFragment;
import com.your.mychat.FindFriendsFragment;
import com.your.mychat.RequestsFragment;
/**
 * made by Igor Ferbert 3.01.2021
 * Adapter class to work with Fragments
 *
 * */
public class Adapter extends FragmentPagerAdapter {

    private final TabLayout _adapterTabLayout;
    public Adapter(@NonNull FragmentManager fm, int behavior, TabLayout tabLayout) {
        super(fm, behavior);
        this._adapterTabLayout = tabLayout;

    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position)
        {
            case 0:
                return new ChatFragment();
            case 1:
                return new RequestsFragment();
            case 2:
                return new FindFriendsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return _adapterTabLayout.getTabCount();
    }
}

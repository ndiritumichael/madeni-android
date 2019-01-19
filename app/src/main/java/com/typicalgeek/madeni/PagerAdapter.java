package com.typicalgeek.madeni;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs){
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                AllDebtsFragment tab1 = new AllDebtsFragment();
                return tab1;
            case 1:
                YouOweFragment tab2 = new YouOweFragment();
                return tab2;
            case 2:
                OwedToYouFragment tab3 = new OwedToYouFragment();
                return tab3;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
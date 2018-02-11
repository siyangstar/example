package com.cqsynet.swifi.activity.social;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/11/23
 */
public class SocialPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments;

    public SocialPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}

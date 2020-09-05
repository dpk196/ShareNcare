package com.example.sharencare.Fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {
    private  final List<Fragment> mFragmentsList=new ArrayList<>();
    private  final List<String> mFragmentsTitleList=new ArrayList<>();
    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }
    public   void addFragment(Fragment fragment,String title){
        mFragmentsList.add(fragment);
        mFragmentsTitleList.add(title);

    }

    @Override
    public Fragment getItem(int i) {
        return mFragmentsList.get(i);
    }

    @Override
    public int getCount() {
        return mFragmentsList.size();
    }
}

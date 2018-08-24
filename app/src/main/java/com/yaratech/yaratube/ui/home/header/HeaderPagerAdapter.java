package com.yaratech.yaratube.ui.home.header;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yaratech.yaratube.data.model.HeaderItem;

import java.util.List;

public class HeaderPagerAdapter extends FragmentPagerAdapter {


    private List<HeaderItem> mHeaderItems;

    public HeaderPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return HeaderItemsFragment.newInstance(mHeaderItems.get(position));
    }

    @Override
    public int getCount() {
        if (mHeaderItems == null) {
            return 0;
        }
        return mHeaderItems.size();
    }

    public void setHeaderItems(List<HeaderItem> mHeaderItems) {
        this.mHeaderItems = mHeaderItems;
        notifyDataSetChanged();
    }
}
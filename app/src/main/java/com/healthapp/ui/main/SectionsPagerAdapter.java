package com.healthapp.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.healthapp.R;
import com.healthapp.ui.contacts.FragmentContact;
import com.healthapp.ui.meetings.FragmentMeetings;
import com.healthapp.ui.meetings.MeetingsDelegate;

import java.util.ArrayList;
import java.util.List;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_2, R.string.tab_text_1};
    private final Context mContext;

    private final List<Fragment> fragments = new ArrayList<>();

    SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
        fragments.add(FragmentMeetings.newInstance());
        fragments.add(FragmentContact.newInstance());
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    MeetingsDelegate getFragmentMeetings() {
        return (MeetingsDelegate) fragments.get(0);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
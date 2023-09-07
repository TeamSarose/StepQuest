package com.SAROSE.StepQuest.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }
    @NonNull
    @Override
    // If value 0 it will show Signup
    // If value 1 it will show Login
    public Fragment createFragment(int position) {
        if (position == 1){
            return new LoginFragment();
        }
        return new SignUpFragment();
    }
    @Override
    public int getItemCount() {
        return 2;
    }
}

package com.irttechnologies.gui.android.listeners;

import android.app.Activity;
import android.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.irttechnologies.gui.R;
import com.irttechnologies.gui.android.AboutUsFragment;
import com.irttechnologies.gui.android.MainActivity;

public class DrawerItemClickListener implements ListView.OnItemClickListener {

    private final MainActivity activity;
    private final String[] menuTitles;
    private final DrawerLayout mDrawerLayout;
    private final ListView mDrawerList;

    public DrawerItemClickListener(MainActivity activity) {
        this.activity = activity;

        menuTitles = activity.getResources().getStringArray(R.array.menu_array);
        mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) activity.findViewById(R.id.left_drawer);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Create a new fragment and specify the planet to show based on position
        Fragment fragment = new AboutUsFragment();

        // Insert the fragment
        activity.showFragment(1);

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        activity.setTitle(menuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
}

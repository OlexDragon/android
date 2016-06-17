package com.irttechnologies.gui.android.listeners;

import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.irttechnologies.gui.android.MainActivity;

public class DrawerItemClickListener implements ListView.OnItemClickListener {

    private final MainActivity activity;
    private DrawerLayout mDrawer;


    public DrawerItemClickListener(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }
}

package com.irttechnologies.gui.android;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.irttechnologies.gui.R;
import com.irttechnologies.gui.android.listeners.DrawerItemClickListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private Fragment fragment;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
        String[] menuTitles = getResources().getStringArray(R.array.menu_array);
        Log.e("menuTitles, ", Arrays.toString(menuTitles));

        // Set the adapter for the list view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.id.left_drawer, menuTitles);
        mDrawerList.setAdapter(adapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener(this));

        // However, if we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return;
        }

        SharedPreferences preferences = getSharedPreferences("androidIrtGui", 0);
        final int menuId = preferences.getInt("onCreate", R.id.menu_about_us);
        showFragment(menuId);
    }

    @Override  public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_irt, menu);
        return true;
    }

    @Override  public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int itemId = item.getItemId();
        SharedPreferences preferences = getSharedPreferences("androidIrtGui", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("selected_menu", itemId);
        return showFragment(itemId);
    }

    public boolean showFragment(int menuId) {
        switch (menuId) {
            case R.id.menu_about_us:
                showAboutUsFragment();
                return true;
            case R.id.menu_contact_us:
                showContactUsFragment();
                return true;
            case R.id.menu_control:
                showControlFragment();
                return true;
            default:
                return false;//super.onOptionsItemSelected(item);
        }
    }

    private void showControlFragment() {
        if(findViewById(R.id.main_frame) != null){
            boolean createNew = fragment == null;

            // Create a new Fragment to be placed in the activity layout
            fragment = new ControlFragment();
            showFragment(createNew);
        }
    }

    private void showContactUsFragment() {
        if(findViewById(R.id.main_frame) != null){
            boolean createNew = fragment == null;

            // Create a new Fragment to be placed in the activity layout
            fragment = new ContactUsFragment();
            showFragment(createNew);


        }
    }

    private void showAboutUsFragment() {
        if(findViewById(R.id.main_frame) != null){
            boolean createNew = fragment == null;

            // Create a new Fragment to be placed in the activity layout
            fragment = new AboutUsFragment();
            showFragment(createNew);


        }
    }

    private void showFragment(boolean createNew) {
        // Add the fragment to the 'fragment_container' FrameLayout
        FragmentTransaction transaction = getSupportFragmentManager()
                                                        .beginTransaction();
        if(createNew)
            transaction.add(R.id.main_frame, fragment);
        else
            transaction.replace(R.id.main_frame, fragment);

        transaction.commit();
    }
}

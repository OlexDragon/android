package com.irttechnologies.gui.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.irttechnologies.gui.R;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "irt_android_gui";
    public static final String SELECTED_MENU = "selected_menu";
    private Fragment fragment;
    private DrawerLayout mDrawer;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        showFragment(sharedPreferences.getInt(SELECTED_MENU, R.id.menu_contact_us));
    }

    @Override  public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_irt, menu);
        return true;
    }

    @Override  public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int itemId = item.getItemId();

        final SharedPreferences preferences = getSharedPreferences("androidIrtGui", 0);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SELECTED_MENU, itemId);

        return showFragment(itemId);
    }

    public boolean showFragment(int menuId) {
        switch (menuId) {
            case R.id.menu_about_us:
                showFragment(new AboutUsFragment());
                return true;
            case R.id.menu_contact_us:
                showFragment(new ContactUsFragment());
                return true;
            case R.id.menu_control:
                showFragment(new ControlFragment());
                return true;
            default:
                return false;//super.onOptionsItemSelected(item);
        }
    }

    private void showFragment(Fragment fragment) {
        if(findViewById(R.id.main_frame) != null){
            boolean createNew = fragment == null;

            // Create a new Fragment to be placed in the activity layout
             showFragment(createNew, fragment);


        }
    }

    private void showFragment(boolean createNew, Fragment fragment) {
        this.fragment = fragment;

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

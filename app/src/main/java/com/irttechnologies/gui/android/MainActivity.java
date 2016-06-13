package com.irttechnologies.gui.android;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.irttechnologies.gui.R;

public class MainActivity extends AppCompatActivity {

    private Fragment aboutUsFragment;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private boolean showFragment(int menuId) {
        switch (menuId) {
            case R.id.menu_about_us:
                showAboutUsFragment();
                return true;
            case R.id.menu_contact_us:
                showContactUsFragment();
                return true;
            default:
                return false;//super.onOptionsItemSelected(item);
        }
    }

    private void showContactUsFragment() {
    }

    private void showAboutUsFragment() {
        if(findViewById(R.id.main_frame) != null){
            Log.e("onCreate", "Create layout");
            boolean createNew = aboutUsFragment == null;

            // Create a new Fragment to be placed in the activity layout
            aboutUsFragment = new AboutUsFragment();
            showFragment(createNew);


        }
    }

    private void showFragment(boolean createNew) {
        // Add the fragment to the 'fragment_container' FrameLayout
        FragmentTransaction transaction = getSupportFragmentManager()
                                                        .beginTransaction();
        if(createNew)
            transaction.add(R.id.main_frame, aboutUsFragment);
        else
            transaction.replace(R.id.main_frame, aboutUsFragment);

        transaction.commit();
    }
}

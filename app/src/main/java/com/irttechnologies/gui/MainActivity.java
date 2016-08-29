package com.irttechnologies.gui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.irttechnologies.gui.enums.BluetoothProfileState;
import com.irttechnologies.gui.enums.ScanStatus;
import com.irttechnologies.gui.interfaces.ble.ScanStatusListener;
import com.irttechnologies.gui.interfaces.SelectionListener;
import com.irttechnologies.gui.services.ScanStatusReceiver;
import com.irttechnologies.gui.services.SelectionStatusReceiver;
import com.irttechnologies.gui.subfragments.FragmentAlarms;
import com.irttechnologies.gui.subfragments.FragmentInfo;
import com.irttechnologies.gui.subfragments.FragmentMonitor;
import com.irttechnologies.gui.subfragments.FragmentNetwork;
import com.irttechnologies.gui.subfragments.FragmentRedundancy;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "irt_android_gui";
    public static final String SELECTED_MENU = "selected_menu";
    private static final String TAG = MainActivity.class.getSimpleName();

    private static BluetoothProfileState mBluetoothProfileState = BluetoothProfileState.DISCONNECTED;
    private static ScanStatus mScanStatus = ScanStatus.STOP;
    private static boolean mSelected;

    private Fragment fragment;

    private final ScanStatusReceiver    mScanStatusReceiver  = new ScanStatusReceiver();
    private final SelectionStatusReceiver mSelectionReceiver  = new SelectionStatusReceiver();
    private MenuItem menuItemStatus;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            if(menuItemStatus == null)
                return;

            final String action = intent.getAction();
            if(action.equals(BluetoothProfileState.class.getName())){
                switch ((BluetoothProfileState)intent.getSerializableExtra("value")){
                    case CONNECTED:
                        menuItemStatus.setActionView(R.layout.led_green);
                        break;
                    case CONNECTING:
                        menuItemStatus.setActionView(R.layout.actionbar_indeterminate_progress);
                        break;
                    default:
                        menuItemStatus.setActionView(R.layout.led_red);
                }
            }
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

//        getActionBar().setTitle(R.string.title_devices);
        setContentView(R.layout.activity_main);

        if(mSelected)
            showFragment(getPreference());
        else
            showFragment(R.id.menu_bluetooth);

        mScanStatusReceiver.addScanStatusListener(new ScanStatusListener() {
            @Override
            public void onScanStatusChange(ScanStatus scanStatus) {
                mScanStatus = scanStatus;
                invalidateOptionsMenu();
            }
        });
        mSelectionReceiver.addScanStatusListener(new SelectionListener() {
            @Override
            public void onChange(boolean selected) {
                mSelected = selected;

                invalidateOptionsMenu();
            }
        });

        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    @Override
    protected void onDestroy() {
        mScanStatusReceiver.removeAllListeners();
        mSelectionReceiver.removeAllListeners();
        super.onDestroy();
    }

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothProfileState.class.getName());
        return intentFilter;
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mBroadcastReceiver, makeGattUpdateIntentFilter());
        registerReceiver(mScanStatusReceiver, makeGattUpdateIntentFilter(ScanStatusReceiver.ACTION));
        registerReceiver(mSelectionReceiver, makeGattUpdateIntentFilter(SelectionStatusReceiver.ACTION));
    }

    @Override
    protected void onPause() {

        unregisterReceiver(mScanStatusReceiver);
        unregisterReceiver(mSelectionReceiver);
        unregisterReceiver(mBroadcastReceiver);

        super.onPause();
    }

    @Override  public boolean onCreateOptionsMenu(Menu menu) {

        if(mSelected)
            menuIrt(menu);
        else
            menuScan(menu);

        return true;
    }

    @Override  public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Log.e(TAG, R.id.menu_redundancy + " : " + item);
        return showFragment(item.getItemId());
    }

    private void menuIrt(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_irt, menu);
        menuItemStatus = menu.findItem(R.id.menu_status);
    }

    private void menuScan(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);

        switch(mScanStatus) {
            case STOP:
                menu.findItem(R.id.menu_stop).setVisible(false);
                menu.findItem(R.id.menu_scan).setVisible(true);
                menu.findItem(R.id.menu_refresh).setActionView(null);
                break;
            case SCAN:
                menu.findItem(R.id.menu_stop).setVisible(true);
                menu.findItem(R.id.menu_scan).setVisible(false);
                menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
    }

    private void savePreference(int menuId) {

        if(menuId == R.id.menu_bluetooth)
            return;

        final SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SELECTED_MENU, menuId);
        editor.commit();
    }

    public boolean showFragment(int menuId) {

        savePreference(menuId);

        switch (menuId) {
            case R.id.menu_about_us:
                showFragment(new FragmentAboutUs());
                getSupportActionBar().setTitle(getString(R.string.menu_about_us));
                return true;
            case R.id.menu_contact_us:
                showFragment(new FragmentContactUs());
                getSupportActionBar().setTitle(getString(R.string.menu_contact_us));
                return true;
            case R.id.menu_control:
//                showFragment(new FragmentTest());
                showFragment(new FragmentControl());
                getSupportActionBar().setTitle(getString(R.string.menu_control));
                return true;
            case R.id.menu_bluetooth:
                final FragmentBluetooth fragment = new FragmentBluetooth();
                showFragment(fragment);
                getSupportActionBar().setTitle(getString(R.string.available_devices));
                return true;
            case R.id.menu_alarms:
                showFragment(new FragmentAlarms());
                getSupportActionBar().setTitle(getString(R.string.menu_alarms));
                return true;
            case R.id.menu_info:
                showFragment(new FragmentInfo());
                getSupportActionBar().setTitle(getString(R.string.menu_info));
                return true;
            case R.id.menu_monitor:
                showFragment(new FragmentMonitor());
                getSupportActionBar().setTitle(getString(R.string.menu_moonitor));
                return true;
            case R.id.menu_network:
                showFragment(new FragmentNetwork());
                getSupportActionBar().setTitle(getString(R.string.menu_network));
                return true;
            case R.id.menu_redundancy:
                showFragment(new FragmentRedundancy());
                getSupportActionBar().setTitle(getString(R.string.menu_redundancy));
                return true;
            case R.id.menu_scan:
                if(!(this.fragment instanceof  FragmentBluetooth)) {
                    showFragment(new FragmentBluetooth());
                    getSupportActionBar().setTitle(getString(R.string.menu_bluetooth));
                    return true;
                }
                return false;
            default:
                return false;//super.onOptionsItemSelected(item);
        }
    }

    private int getPreference() {
        final SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        return preferences.getInt(SELECTED_MENU, R.id.menu_about_us);
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(createNew)
            transaction.add(R.id.main_frame, fragment);
        else
            transaction.replace(R.id.main_frame, fragment);

        transaction.commit();
    }

    private IntentFilter makeGattUpdateIntentFilter(String action) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        return intentFilter;
    }
}

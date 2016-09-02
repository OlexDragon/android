package com.irttechnologies.gui;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.irttechnologies.gui.enums.BluetoothGattStatus;
import com.irttechnologies.gui.enums.CharacteristicProperties;
import com.irttechnologies.gui.enums.GattAttributes;
import com.irttechnologies.gui.interfaces.ble.BluetoothValue;
import com.irttechnologies.gui.pojo.BleValue;
import com.irttechnologies.gui.services.BleService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentTest extends Fragment implements View.OnClickListener {

    private static final String TAG = FragmentTest.class.getSimpleName();
    private BleService bleService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BleService.LocalBinder) service).getService();

            if(bleService !=null){
                bleService.initialize();
                bleService.discoverServices();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {

            if(bleService == null)
                return;

            final String action = intent.getAction();
            if(action.equals(BluetoothGattStatus.class.getName())){

                if(intent.getSerializableExtra("value")==BluetoothGattStatus.GATT_SUCCESS){

                    final List<BluetoothGattService> services = bleService.getServices();
                    adapter.updateAdapter(services);
                }
            }
        }
    };
    private AppCompatActivity activity;
    private Button button;
    private EditText editText;
    private EditText editTextPeriod;
    private ListView listView;
    private AdapterGattServicesList adapter;
    private List uuIds = new ArrayList();
    private BluetoothGattCharacteristic selectedCharacteristic;
    private BluetoothGattService selectedBluetoothGattService;
    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View inflate = inflater.inflate(R.layout.fragment_test, container, false);
        button = (Button)inflate.findViewById(R.id.CONTROL_BUTTON);
        button.setOnClickListener(this);
        editText = (EditText) inflate.findViewById(R.id.CONTROL_EDIT_TEXT);
        editTextPeriod = (EditText) inflate.findViewById(R.id.CONTROL_EDIT_TEXT_PERIOD);
        listView = (ListView) inflate.findViewById(R.id.CONTROL_LIST_VIEW);
        final TextView textView = (TextView) inflate.findViewById(R.id.CONTRO_TEXT_VIEW);

        activity = (AppCompatActivity) getActivity();

        adapter = new AdapterGattServicesList(activity);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public int index;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stop();

                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                selectedBluetoothGattService = (BluetoothGattService) adapter.getItem(position);
                final List<BluetoothGattCharacteristic> characteristics = selectedBluetoothGattService.getCharacteristics();

                if(index>=characteristics.size())
                    index = 0;

                bleService.setCharacteristicNotification(selectedCharacteristic, false);
                selectedCharacteristic = characteristics.get(index);
                bleService.setCharacteristicNotification(selectedCharacteristic, true);

                index++;
                textView.setText(index + "/" + characteristics.size() + " : " + GattAttributes.lookup(selectedCharacteristic.getUuid()));

                final int properties = selectedCharacteristic.getProperties();
                final boolean enable = CharacteristicProperties.WRITE.isIn(properties) | CharacteristicProperties.WRITE_NO_RESPONSE.isIn(properties);
                button.setEnabled(enable);
                editText.setEnabled(enable);
                final List<CharacteristicProperties> characteristicProperties = CharacteristicProperties.valuesOf(properties);
                editText.setText(characteristicProperties.toString());

                hideKeybord(view);
            }
        });

        Intent gattServiceIntent = new Intent(activity, BleService.class);
        activity.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        activity.registerReceiver(mBroadcastReceiver, makeGattUpdateIntentFilter());

        return inflate;
    }

    private void hideKeybord(View view) {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    @Override
    public void onDestroyView() {
        activity.unregisterReceiver(mBroadcastReceiver);
        activity.unbindService(mServiceConnection);
        stop();
        super.onDestroyView();
    }

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothGattStatus.class.getName());
        return intentFilter;
    }

    private boolean send;
    private long period;
    @Override
    public void onClick(View v) {

        byte[] value = getValue();
        String periodText = editTextPeriod.getText().toString();
        if(periodText.length()==0)
            period = 1000;
        else
            period = Long.parseLong(periodText);

        final String text = button.getText().toString();
        final String textStop = getString(R.string.stop);
        if(text.equals(textStop)){
            stop();
            return;
        }

        if(selectedCharacteristic == null){
            Toast.makeText(activity, "Characteristic in not selected", Toast.LENGTH_LONG).show();
            return;
        }

        button.setText(textStop);
        send = true;

        handler.post(new Runnable() {
            @Override
            public void run() {

                if(!send) return;

                BluetoothValue bluetoothValue = new BleValue(selectedBluetoothGattService.getUuid(), selectedCharacteristic.getUuid(), getValue());
                final boolean sent = bleService.writeCharacteristic(bluetoothValue);

                handler.postDelayed(this, period);
            }

        });
    }

    private void stop() {
        button.setText(getString(R.string.send));
        send = false;
    }

    @NonNull
    private byte[] getValue() {
        byte[] value = editText.getText().toString().getBytes();
        if(value.length>20){
            value = Arrays.copyOf(value, 20);
            editText.setText(new String(value));
        }
        return value;
    }
}

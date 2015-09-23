package com.example.ppamatoserver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class PassBTInformActivity extends Activity {
	TextView btDevice;
	Button btnServiceStart;
	Button btnServiceEnd;
	Button btnMeasureNew;
	Button btnMeasureStart;
	Button btnMeasureEnd;
	Button btnSetDevice;
	
	String deviceName="";	
	String ppamCommand="";
	
	Intent serviceIntent;
	
	String messageActionFilter = "ppam.test.bcm";
	String buttonActionFilter = "ppam.test.bnt";
	String BroadcastKey = "BroadcastMessage";
	String CommandKey = "CommandMessage";
	
    private static final String TAG = "PassBTInform";
    private static final boolean D = true;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;	
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    
    private ListView mConversationView;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up ScriptListView 
        if(D) Log.e(TAG, "++ ON CREATE ++");
        setContentView(R.layout.activity_passbtinform);
        
        // Set up Button and TextView
        btDevice=(TextView)findViewById(R.id.device_name);
        btnSetDevice=(Button)findViewById(R.id.btn_set_bt_device);
        btnSetDevice.setOnClickListener(mClickListener);
        btnServiceStart=(Button)findViewById(R.id.btn_service_start_bts);
        btnServiceStart.setOnClickListener(mClickListener);
        btnServiceEnd=(Button)findViewById(R.id.btn_service_end_bts);
        btnServiceEnd.setOnClickListener(mClickListener);
        btnMeasureNew=(Button)findViewById(R.id.btn_measurement_newfile_bts);
        btnMeasureNew.setOnClickListener(mClickListener);
        btnMeasureStart=(Button)findViewById(R.id.btn_measurement_start_bts);
        btnMeasureStart.setOnClickListener(mClickListener);
        btnMeasureEnd=(Button)findViewById(R.id.btn_measurement_end_bts);
        btnMeasureEnd.setOnClickListener(mClickListener);

        serviceIntent = new Intent(getBaseContext(),CommandSenderService.class);
    }
	
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
		stopService(serviceIntent);
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        
        // When DeviceListActivity returns with a device to connect
        if (resultCode == Activity.RESULT_OK) {
            // Get the device MAC address
            deviceName = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            btDevice.setText(deviceName);
        }
    }
    
    Button.OnClickListener mClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent tmpIntent = new Intent();
			switch (v.getId()) {
			case R.id.btn_service_start_bts :
	            // 새로운 서비스 실행
				serviceIntent.putExtra("ADDRESS", deviceName);
				serviceIntent.putExtra("PPAM_COMMAND", ppamCommand);
	            startService(serviceIntent);
				break;
			case R.id.btn_service_end_bts :
				stopService(serviceIntent);
				break;
			case R.id.btn_measurement_newfile_bts :
				//intent.setComponent(component);
				tmpIntent.setAction(buttonActionFilter);
				tmpIntent.putExtra(CommandKey, "NEW");
				sendBroadcast(tmpIntent);
				break;
			case R.id.btn_measurement_start_bts :
				//intent.setComponent(component);
				tmpIntent.setAction(buttonActionFilter);
				tmpIntent.putExtra(CommandKey, "START");
				sendBroadcast(tmpIntent);
				break;
			case R.id.btn_measurement_end_bts :
				//intent.setComponent(component);
				tmpIntent.setAction(buttonActionFilter);
				tmpIntent.putExtra(CommandKey, "END");
				sendBroadcast(tmpIntent);
				break;
			case R.id.btn_set_bt_device :
	            Intent serverIntent = new Intent(PassBTInformActivity.this, DeviceListActivity.class);
	            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				break;
			}
		}
	};
}

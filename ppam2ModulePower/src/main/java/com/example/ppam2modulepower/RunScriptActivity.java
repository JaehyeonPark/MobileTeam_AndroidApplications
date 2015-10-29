package com.example.ppam2modulepower;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.PPAMStates;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class RunScriptActivity extends Activity {
	ArrayList<TestScriptElement> listElements;
	ListView scriptView;
	TextView tester;
    TextView wifiStateTexture;
	TextView tvLocTitle;
	Button btnRun;
	
	// acc sensor
	private SensorManager mSensorManager;
	private Sensor mAcc;
	private mAccListener mAccL;
	
	// loc sensor
	private LocationManager mLocMan;
	private LocationListener mNetworkListener;
	private LocationListener mGpsListener;
//	TextView EditResult;
//	TextView mStatus;
//	TextView mResult;
	String mProvider;
	int mCount;
	private int preGpsState;
	
	
	int order = 0;
	int extend_order=0;
    int timerCounter = 0;
    private static int partNumber = 1;
    private int partPower[] = new int[100];
    
    private TextView mTitle;
	
    private static final String TAG = "ppam2runner";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;	
    
    private static final int SUB_LIMIT = 500;
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    private ListView mConversationView;
    
    //FILE 
//	private String dirPath;
//	private File file;
//	private File savefile;
//	private FileOutputStream fos;

    //broadcast for wifi state
    String wifiActionFilter = "android.net.wifi.WIFI_STATE_CHANGED";
    String wifiStateKey = "wifi_state";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Set up ScriptListView 
        listElements = (ArrayList<TestScriptElement>) getIntent().getSerializableExtra("listElements");
        setContentView(R.layout.activity_runscript);
        scriptView = (ListView)findViewById(R.id.script_viewer_run);
        
        ArrayAdapter<TestScriptElement> adapter;
        adapter = new ArrayAdapter<TestScriptElement>(this, android.R.layout.simple_list_item_1, listElements);
        scriptView.setAdapter(adapter);
        
        // Set up Button and TextView
        tvLocTitle=(TextView)findViewById(R.id.tvLocTitle);
        tester=(TextView)findViewById(R.id.text_test);
        wifiStateTexture=(TextView)findViewById(R.id.wifi_state);
        btnRun=(Button)findViewById(R.id.btn_start_script);
        btnRun.setOnClickListener(mClickListener);
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_right_text);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
		mLocMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocMan.addGpsStatusListener(mGpsStatusListener);
		mGpsListener = new mGpsLocationListener();
		mNetworkListener = new mNetworkLocationListener();

//		dirPath = getFilesDir().getAbsolutePath();
////		dirPath = "/Test/";
//		file = new File(dirPath);
//		
//		if(!file.exists()){
//			file.mkdirs();
//		}
    }
	
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        setupAcc();
        setupLoc();

        IntentFilter filter = new IntentFilter();
        filter.addAction(wifiActionFilter);
        registerReceiver(MessageChecker, filter);
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }

        if(listElements.size()==0){
            Toast.makeText(getApplicationContext(), "Script is empty\nReturn to main page", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
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
        // Stop the Bluetooth chat services
        if (mChatService != null){ 
        	mChatService.stop();
        }
        if(D) Log.e(TAG, "--- ON DESTROY ---");
		
		mSensorManager.unregisterListener(mAccL);
    	mLocMan.removeUpdates(mGpsListener);
    	mLocMan.removeUpdates(mNetworkListener);
        unregisterReceiver(MessageChecker);
    	
//		try {
//			fos.close();
//		} catch (IOException e) {}
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
            
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }
    
	public void RunTestElement(TestScriptElement testElement) {
		if(testElement instanceof StrElement){
			
			tester.setText(testElement.toString());
			
			String message = "$START@";
			sendMessage(message);
			SystemClock.sleep(20);
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBSTR"+String.format("%02d",partNumber)+"#START"+"@";
				sendMessage(message);
			}

		}
		else if(testElement instanceof EndElement){

            String message= "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
        	Log.d("RunScriptActivity", "EndElement **");  
			message = "$STOP@";
			sendMessage(message);
			
			mSensorManager.unregisterListener(mAccL);
	    	mLocMan.removeUpdates(mGpsListener);
	    	mLocMan.removeUpdates(mNetworkListener);

			tester.setText(testElement.toString());
			
		}
		else if(testElement instanceof AccElement){

        	Log.d("RunScriptActivity", "AccElement ** partNum ++");  
            String message;    
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#PART"+(partNumber+1)+"@";  
	        	partNumber ++;	    
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
			
			mSensorManager.unregisterListener(mAccL);
			mSensorManager.registerListener(mAccL, mAcc, ((AccElement) testElement).getMode());
			
			tester.setText(testElement.toString());
			
		}
		else if(testElement instanceof AccEndElement){

        	Log.d("RunScriptActivity", "AccEndElement ** partNum ++");  
            String message;    
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#PART"+(partNumber+1)+"@"; 
	        	partNumber ++;	     
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);

			mSensorManager.unregisterListener(mAccL);

			tester.setText(testElement.toString());
			
		}
		else if(testElement instanceof GpsElement){
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew
        	mLocMan.removeUpdates(mGpsListener);
	    	mCount = 0;
	    	mLocMan.requestLocationUpdates(	mLocMan.GPS_PROVIDER, 
					((GpsElement) testElement).getMinTime(), 
					((GpsElement) testElement).getMinDistance(), 
					mGpsListener);
//	    	mStatus.setText("�쁽�옱 �긽�깭 : �꽌鍮꾩뒪 �떆�옉("+mProvider+")");		

	    	tester.setText(testElement.toString());

        	Log.d("RunScriptActivity", "GpsElement ** partNum ++");  
            String message;    
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#PART"+(partNumber+1)+"@"; 
	        	partNumber ++;	     
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
		}
		else if(testElement instanceof GpsEndElement){
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew

        	Log.d("RunScriptActivity", "GpsEndElement ** partNum ++");   
            String message;    
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#PART"+(partNumber+1)+"@";    
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
        	partNumber ++;	  
			sendMessage(message);
        	mLocMan.removeUpdates(mGpsListener);
	    	tester.setText(testElement.toString());
		}
		else if(testElement instanceof NetworkElement){
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew
        	mLocMan.removeUpdates(mNetworkListener);
	    	mCount = 0;
	    	mLocMan.requestLocationUpdates(	mLocMan.NETWORK_PROVIDER, 
					((NetworkElement) testElement).getMinTime(), 
					((NetworkElement) testElement).getMinDistance(), 
					mNetworkListener);
//	    	mStatus.setText("�쁽�옱 �긽�깭 : �꽌鍮꾩뒪 �떆�옉("+mProvider+")");		

	    	tester.setText(testElement.toString());

        	Log.d("RunScriptActivity", "NetworkElement ** partNum ++");  
            String message;    
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#PART"+(partNumber+1)+"@"; 
	        	partNumber ++;	     
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
		}
		else if(testElement instanceof NetworkEndElement){
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew

        	Log.d("RunScriptActivity", "NetworkEndElement ** partNum ++");   
            String message;    
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#PART"+(partNumber+1)+"@";    
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
        	partNumber ++;	  
			sendMessage(message);
        	mLocMan.removeUpdates(mNetworkListener);
	    	tester.setText(testElement.toString());
		}
		else if(testElement instanceof NewElement){

	    	tester.setText(testElement.toString());
			
			Calendar c = Calendar.getInstance();
			int cur_time = c.get(Calendar.DATE)*1000000 +c.get(Calendar.HOUR_OF_DAY)*10000 + c.get(Calendar.MINUTE)*100 + c.get(Calendar.SECOND);
            String message = "$SETTIME#" + String.format("%08d",cur_time) + "@";
			sendMessage(message);
			SystemClock.sleep(20);
			message = "$NEWFILE@";
            sendMessage(message);
            
//            savefile = new File(dirPath+"/"+ String.format("%08d",cur_time) +".csv");
//            try{
//            	fos = new FileOutputStream(savefile);
//            }
//            catch(IOException e){}
		}
		else{
            Log.d("RunScriptActivity", "Default ** partNum ++");   
            String message;    
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#PART"+(partNumber+1)+"@";    
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
        	partNumber ++;	  
			sendMessage(message);
	    	tester.setText(testElement.toString());
		}
	}

    private void ensureDiscoverable() {    	
        if(D) Log.d(TAG, "ensure discoverable");
        //?�뮞��? 筌뤴뫀諭� ?苑�?�젟. 
        
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
    private void setupAcc() {

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAccL = new mAccListener();
    }
    
    private void setupLoc(){

		// �쐞移� 愿�由ъ옄 援ы븿
		mLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mLocMan.addGpsStatusListener(mGpsStatusListener);
		
    }
    
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
    }
	
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
    	if(mChatService == null)
    		return;
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            
            case MESSAGE_STATE_CHANGE:
            	
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    mConversationArrayAdapter.clear();
                    break;
                    
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                    
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                    
                }
                break;
                
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
                
            case MESSAGE_READ:
                // construct a string from the valid bytes in the buffer
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                if(!readMessage.contains("@")){
                	mConversationArrayAdapter.add(mConnectedDeviceName+": " + readMessage );
                }
                else{
	                String tempString= readMessage.substring(readMessage.indexOf("@")+3,readMessage.length()-1);
//	                mConversationArrayAdapter.add(mConnectedDeviceName+": " + tempPow + "order: " + (order-2));
	                String tempOrder = tempString.substring(0, 2);
	                String tempPower = tempString.substring(2);
	                String tempToFile="";
	                Calendar c = Calendar.getInstance();
	    			String time = c.get(Calendar.DATE) + ", " + c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+"\n";
	                
	            	tempToFile=time +"POWER,"+ Integer.parseInt(tempPower)+"\n";
//	            	try {
//		            	fos.write(tempToFile.getBytes());
//					} catch (IOException e) {}
	            	
//	                mConversationArrayAdapter.add("Me:  " + tempOrder + tempPower);
                }
                break;
                
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                
                break;
                
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    
    //���씠癒몃�� 泥섎━�븯湲� �쐞�빐 �빖�뱾�윭 媛앹껜 �깮�꽦
    private Handler delayhandler = new Handler() {
    	public void handleMessage(Message msg) {
    		int temp = 1;
    		
    		if(order<listElements.size()){
    			RunTestElement(listElements.get(order));	
    			temp = listElements.get(order).getDueTime();
	        	order++;
    		}
    		else{   
    			delayhandler.removeMessages(0);
    	        btnRun.setEnabled(true);
    		}
			
    		delayhandler.sendEmptyMessageDelayed(0, temp*1000);
    		
//    		tester.setText("Value = " + timerCounter++);
    	}
    };
	
	private class mAccListener implements SensorEventListener{

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			
		}

		@Override
		public void onSensorChanged(SensorEvent arg0) {
			// 媛��냽�룄 �꽱�꽌�쓽 �꽱�꽌媛믪쓽 寃쎌슦,
			// arg0[0], [1], [2] 媛� 媛곴컖,
			// x, y, z 異뺤쓽 痢≪젙媛믪씠硫�, 紐⑤뱺 媛믩뱾�� 以묐젰 �슂�냼媛� 吏��썙�졇�엳�떎.
			// �꽱�꽌 媛� 蹂��룞 �떆,
//			Log.d("MainActivity", "ACC / onSensorChanged ***");
//			tvAccVal_X.setText("Acc X : " + Double.toString(arg0.values[0]));
//			tvAccVal_Y.setText("Acc Y : " + Double.toString(arg0.values[1]));
//			tvAccVal_Z.setText("Acc Z : " + Double.toString(arg0.values[2]));
		}
	}	

	private class mGpsLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location arg0) {
			Log.d("RunScriptActivity", "GpsLocationChanged **");
			Log.d("RunScriptActivity", "GpsLocation : "+arg0.getLatitude()+"  ,  "+arg0.getLongitude());
			
			String message="";
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew
        	Log.d("RunScriptActivity", "GpsLocationChanged ** partNum ++");         
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#GpsLocCha"+"@";
            	partNumber ++;	  
            	extend_order++;   
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
		}

		@Override
		public void onProviderDisabled(String arg0) {
			Log.d("RunScriptActivity", "GPS / onProviderDisabled **");
			
			String message="";
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew
        	Log.d("RunScriptActivity", "GpsProviderDisabled ** partNum ++");         
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#GpsProvDis"+"@";
            	partNumber ++;	  
            	extend_order++;   
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
		}

		@Override
		public void onProviderEnabled(String arg0) {
			Log.d("RunScriptActivity", "GPS / onProviderEnabled **");
			
			String message="";
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew
        	Log.d("RunScriptActivity", "GpsProviderEnabled ** partNum ++");         
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#GpsProvEn"+"@";
            	partNumber ++;	  
            	extend_order++;   
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			Log.d("RunScriptActivity", "GPS / onStatusChanged **");
			
			String message="";
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew
        	Log.d("RunScriptActivity", "GpsStatusChanged"+arg1+" **++ partNum ++");         
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#GpsStaCha"+arg1+"@";
            	partNumber ++;	  
            	extend_order++;   
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
		}
	}
	
	private class mNetworkLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location arg0) {
			Log.d("RunScriptActivity", "NetworkLocationChanged **");
			Log.d("RunScriptActivity", "NetworkLocation : "+arg0.getLatitude()+"  ,  "+arg0.getLongitude());
			
			String message="";
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew
			
        	Log.d("RunScriptActivity", "NetworkLocationChanged ** partNum ++");         
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#NetLocCha"+"@";
            	partNumber ++;	  
            	extend_order++;   
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
		}

		@Override
		public void onProviderDisabled(String arg0) {
			Log.d("RunScriptActivity", "Network / NetProviderDisabled **");
			
			String message="";
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew
			
        	Log.d("RunScriptActivity", "NetProviderDisabled ** partNum ++");         
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#NetProvDis"+"@";
            	partNumber ++;	  
            	extend_order++;   
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
		}

		@Override
		public void onProviderEnabled(String arg0) {
			Log.d("RunScriptActivity", "Network / onProviderEnabled **");
			
			String message="";
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew
			
        	Log.d("RunScriptActivity", "NetProviderEnabled ** partNum ++");         
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#NetProvEn"+"@";
            	partNumber ++;	  
            	extend_order++;   
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			Log.d("RunScriptActivity", "Network / onStatusChanged **");
			
			String message="";
			
			// 湲곗〈 �긽�깭�뿉�꽌 testElement renew
			
        	Log.d("RunScriptActivity", "NetStatusChanged"+arg1+" ** partNum ++");         
			if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
				message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#NetStaCha"+arg1+"@";
            	partNumber ++;	  
            	extend_order++;   
			}
			else
				message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
			sendMessage(message);
		}
	}
	
	private final GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener()
	{

		@Override
		public void onGpsStatusChanged(int event) {
			String message="";
			switch(event) {
			case GpsStatus.GPS_EVENT_FIRST_FIX :
				Log.d("RunScriptActivity", "GPS / GPS_EVENT_FIRST_FIX **");
				tvLocTitle.setText("GPS_EVENT_FIRST_FIX **");
		
		            preGpsState=GpsStatus.GPS_EVENT_FIRST_FIX;
		            
	            	Log.d("RunScriptActivity", "GPS / GPS_EVENT_FIRST_FIX ** partNum ++");         
					if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
						message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#GPS_1ST"+"@";
		            	partNumber ++;	  
		            	extend_order++;   
					}
					else
						message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
					sendMessage(message);
				
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS :
				Log.d("RunScriptActivity", "GPS / GPS_EVENT_SATELLITE_STATUS **");
				tvLocTitle.setText("GPS_EVENT_SATELLITE_STATUS **");
				if(preGpsState!=GpsStatus.GPS_EVENT_SATELLITE_STATUS){
			        			
		            preGpsState=GpsStatus.GPS_EVENT_SATELLITE_STATUS;
		            
	            	Log.d("RunScriptActivity", "GPS / GPS_EVENT_SATELLITE_STATUS ** partNum ++");      
					if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
						message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#GPS_SAT"+"@";
		            	partNumber ++;	  
		            	extend_order++;  
					}
					else
						message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
					sendMessage(message);
				}
				break;
			case GpsStatus.GPS_EVENT_STARTED :
				Log.d("RunScriptActivity", "GPS / GPS_EVENT_STARTED **");
				tvLocTitle.setText("GPS_EVENT_STARTED **");
				
		            preGpsState=GpsStatus.GPS_EVENT_STARTED;
		            
	            	Log.d("RunScriptActivity", "GPS / GPS_EVENT_STARTED ** partNum ++");
					if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
						message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#GPS_START"+"@";
		            	partNumber ++;	  
		            	extend_order++;   
					}
					else
						message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
					sendMessage(message);
				
				break;
			case GpsStatus.GPS_EVENT_STOPPED :
				Log.d("RunScriptActivity", "GPS / GPS_EVENT_STOPPED **");
				tvLocTitle.setText("GPS_EVENT_STOPPED **");
				
		            preGpsState=GpsStatus.GPS_EVENT_STOPPED;
		            
	            	Log.d("RunScriptActivity", "GPS / GPS_EVENT_STOPPED ** partNum ++");
					if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
						message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#GPS_STOP"+"@";
		            	partNumber ++;	  
		            	extend_order++;   
					}
					else
						message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
					sendMessage(message);
				
				break;
			}
		}
	};

    
    Button.OnClickListener mClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_start_script :
		        delayhandler.sendEmptyMessage(0);
		        btnRun.setEnabled(false);
	            partNumber = 1;
	            order=0;
	        	extend_order=0;
	            timerCounter =0;
	            
				break;
			}
		}
	};
    public BroadcastReceiver MessageChecker = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals(wifiActionFilter)){
                Bundle bundle = intent.getExtras();
                int temp = bundle.getInt(wifiStateKey, -1);
                String tempString = "";
                String message = "";
                if(temp== WifiManager.WIFI_STATE_DISABLING)
                    tempString = "WIFI_DISING";
                if(temp== WifiManager.WIFI_STATE_DISABLED)
                    tempString = "WIFI_DISED";
                if(temp== WifiManager.WIFI_STATE_ENABLING)
                    tempString = "WIFI_ENING";
                if(temp== WifiManager.WIFI_STATE_ENABLED)
                    tempString = "WIFI_ENED";
                if(temp== WifiManager.WIFI_STATE_UNKNOWN)
                    tempString = "WIFI_UNKNOWN";

                Log.d("RunScriptActivity", "WIFI / STATECHANED **" + tempString);
                tvLocTitle.setText("WIFI_STATE_CHANGE **" + tempString);
                tester.setText(tempString);
                if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
                    message = "$SUBCHANGE"+String.format("%02d",partNumber)+"#"+ tempString +"@";
                    partNumber ++;
                    extend_order++;
                }
                else
                    message = "$SUBEND"+String.format("%02d",partNumber)+"#PART"+partNumber+"@$WRRT@";
                sendMessage(message);
            }
        }
    };
}
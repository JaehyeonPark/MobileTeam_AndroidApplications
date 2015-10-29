package com.example.ppamattester;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
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
	TextView tvLocTitle;
	Button btnRun;
	
	// acc sensor
	private SensorManager mSensorManager;
	private Sensor mAcc;
	private mAccListener mAccL;
	private Sensor mLight;
	private mLightListener mLightL;
	
	// loc sensor
	private LocationManager mLocMan;
	private LocationListener mNetworkListener;
	private LocationListener mGpsListener;
//	TextView EditResult;
//	TextView mStatus;
//	TextView mResult;
	String mProvider;
	int mCount;
	
	
	int order = 0;
	
    private static final String TAG = "ppamTATester";
    private static final boolean D = true;
    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    //FILE 
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
        btnRun=(Button)findViewById(R.id.btn_start_script);
        btnRun.setOnClickListener(mClickListener);
        
		mLocMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocMan.addGpsStatusListener(mGpsStatusListener);
		mGpsListener = new mGpsLocationListener();
		mNetworkListener = new mNetworkLocationListener();
    }
	
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        setupAcc();
		setupLight();
        setupLoc();

		if(listElements.size()==0){
			Toast.makeText(getApplicationContext(), "Script is empty\nReturn to main page", Toast.LENGTH_SHORT).show();
			finish();
		}
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
        if(D) Log.e(TAG, "--- ON DESTROY ---");

		mSensorManager.unregisterListener(mLightL);
		mSensorManager.unregisterListener(mAccL);
    	mLocMan.removeUpdates(mGpsListener);
    	mLocMan.removeUpdates(mNetworkListener);
    	
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
	public void RunTestElement(TestScriptElement testElement) {
		if(testElement instanceof StrElement){
			tester.setText(testElement.toString());
		}
		else if(testElement instanceof EndElement){
			mSensorManager.unregisterListener(mAccL);
			mSensorManager.unregisterListener(mLightL);
	    	mLocMan.removeUpdates(mGpsListener);
	    	mLocMan.removeUpdates(mNetworkListener);
			tester.setText(testElement.toString());
		}
		else if(testElement instanceof AccElement){
        	Log.d("RunScriptActivity", "AccElement ** ");  
			mSensorManager.unregisterListener(mAccL);
			mSensorManager.registerListener(mAccL, mAcc, ((AccElement) testElement).getMode());
			tester.setText(testElement.toString());
		}
		else if(testElement instanceof AccEndElement){
        	Log.d("RunScriptActivity", "AccEndElement ** ");  
			mSensorManager.unregisterListener(mAccL);
			tester.setText(testElement.toString());
		}
		else if(testElement instanceof LightElement){
			Log.d("RunScriptActivity", "LightElement ** ");
			mSensorManager.unregisterListener(mLightL);
			mSensorManager.registerListener(mLightL, mLight, ((LightElement) testElement).getMode());
			tester.setText(testElement.toString());
		}
		else if(testElement instanceof LightEndElement){
			Log.d("RunScriptActivity", "LightEndElement ** ");
			mSensorManager.unregisterListener(mLightL);
			tester.setText(testElement.toString());
		}
		else if(testElement instanceof GpsElement){
        	Log.d("RunScriptActivity", "GpsElement ** partNum ++");  
        	mLocMan.removeUpdates(mGpsListener);
	    	mCount = 0;
	    	mLocMan.requestLocationUpdates(	mLocMan.GPS_PROVIDER, 
					((GpsElement) testElement).getMinTime(), 
					((GpsElement) testElement).getMinDistance(), 
					mGpsListener);
	    	tester.setText(testElement.toString());
		}
		else if(testElement instanceof GpsEndElement){
        	Log.d("RunScriptActivity", "GpsEndElement **");   
        	mLocMan.removeUpdates(mGpsListener);
	    	tester.setText(testElement.toString());
		}
		else if(testElement instanceof NetworkElement){
        	Log.d("RunScriptActivity", "NetworkElement ** "); 
        	mLocMan.removeUpdates(mNetworkListener);
	    	mCount = 0;
	    	mLocMan.requestLocationUpdates(	mLocMan.NETWORK_PROVIDER, 
					((NetworkElement) testElement).getMinTime(), 
					((NetworkElement) testElement).getMinDistance(), 
					mNetworkListener);
	    	tester.setText(testElement.toString());
		}
		else if(testElement instanceof NetworkEndElement){
        	Log.d("RunScriptActivity", "NetworkEndElement ** ");   
        	mLocMan.removeUpdates(mNetworkListener);
	    	tester.setText(testElement.toString());
		}
		else{
            Log.d("RunScriptActivity", "Default ** partNum ");   
	    	tester.setText(testElement.toString());
		}
	}
    
    private void setupAcc() {

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAccL = new mAccListener();
    }

	private void setupLight() {

		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mLightL = new mLightListener();
	}
    
    private void setupLoc(){

		// 위치 관리자 구함
		mLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mLocMan.addGpsStatusListener(mGpsStatusListener);
		
    }

    //타이머를 처리하기 위해 핸들러 객체 생성
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
			Log.d("RunScriptActivity", "AccAccuracyChanged **");
		}

		@Override
		public void onSensorChanged(SensorEvent arg0) {
			Log.d("RunScriptActivity", "AccValueChanged **");
		}
	}

	private class mLightListener implements SensorEventListener{

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			Log.d("RunScriptActivity", "LightAccuracyChanged **");
		}

		@Override
		public void onSensorChanged(SensorEvent arg0) {
			Log.d("RunScriptActivity", "LightValueChanged **");
		}
	}

	private class mGpsLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location arg0) {
			Log.d("RunScriptActivity", "GpsLocationChanged **");
			Log.d("RunScriptActivity", "GpsLocation : "+arg0.getLatitude()+"  ,  "+arg0.getLongitude());
			tvLocTitle.setText("GpsLocationChanged **");
		}

		@Override
		public void onProviderDisabled(String arg0) {
			Log.d("RunScriptActivity", "GPS / onProviderDisabled **");
			tvLocTitle.setText("onProviderDisabled **");
		}

		@Override
		public void onProviderEnabled(String arg0) {
			Log.d("RunScriptActivity", "GPS / onProviderEnabled **");
			tvLocTitle.setText("onProviderEnabled **");
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			Log.d("RunScriptActivity", "GPS / onStatusChanged"+arg1+" **");
			tvLocTitle.setText("onStatusChanged **");
		}
	}
	
	private class mNetworkLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location arg0) {
			Log.d("RunScriptActivity", "NetworkLocationChanged **");
			Log.d("RunScriptActivity", "NetworkLocation : "+arg0.getLatitude()+"  ,  "+arg0.getLongitude());
			tvLocTitle.setText("NetworkLocationChanged **");
		}

		@Override
		public void onProviderDisabled(String arg0) {
			Log.d("RunScriptActivity", "Network / NetProviderDisabled **");
			tvLocTitle.setText("NetProviderDisabled **");
		}

		@Override
		public void onProviderEnabled(String arg0) {
			Log.d("RunScriptActivity", "Network / onProviderEnabled **");
			tvLocTitle.setText("onProviderEnabled **");
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			Log.d("RunScriptActivity", "Network / onStatusChanged"+arg1+" **");
			tvLocTitle.setText("onStatusChanged **");
		}
	}
	
	private final GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener()
	{

		@Override
		public void onGpsStatusChanged(int event) {
			switch(event) {
			case GpsStatus.GPS_EVENT_FIRST_FIX :
				Log.d("RunScriptActivity", "GPS / GPS_EVENT_FIRST_FIX **");
				tvLocTitle.setText("GPS_EVENT_FIRST_FIX **");
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS :
				Log.d("RunScriptActivity", "GPS / GPS_EVENT_SATELLITE_STATUS **");
				tvLocTitle.setText("GPS_EVENT_SATELLITE_STATUS **");
				break;
			case GpsStatus.GPS_EVENT_STARTED :
				Log.d("RunScriptActivity", "GPS / GPS_EVENT_STARTED **");
				tvLocTitle.setText("GPS_EVENT_STARTED **");
				break;
			case GpsStatus.GPS_EVENT_STOPPED :
				Log.d("RunScriptActivity", "GPS / GPS_EVENT_STOPPED **");
				tvLocTitle.setText("GPS_EVENT_STOPPED **");
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
	            order=0;
	            
				break;
			}
		}
	};
}

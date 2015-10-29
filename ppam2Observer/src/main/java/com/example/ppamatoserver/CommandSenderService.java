package com.example.ppamatoserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.PPAMStates;
import android.widget.Toast;


public class CommandSenderService extends Service {

    private int partNumber = 0;
    private int partPower[] = new int[500];
    
    private static final int SUB_LIMIT = 9000;
	
    private static final String TAG = "BluetoothSender";
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
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    
//    //FILE 
//	private String dirPath;
//	private File file;
//	private File savefile;
//	private FileOutputStream fos;
    
    private int gpsStatus = -1;
    private int locationListener = 0;
	
	String buttonActionFilter = "ppam.test.bnt";
	String bluetoothActionFilter = "ppam.test.bluetooth";
	String CommandKey = "CommandMessage";
	String bluetoothKey = "bluetoothMassage";
	
	int runflag;
	
	public void onCreate(){
		super.onCreate();
		if(D) Log.e(TAG, "-- ON CREATE --");
		Toast.makeText(getBaseContext(), "Service is Created", Toast.LENGTH_SHORT).show();
	        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            stopSelf(START_NOT_STICKY);
        }
        
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
        
//		dirPath = getFilesDir().getAbsolutePath();
//		file = new File(dirPath);
//		
//		if(!file.exists()){
//			file.mkdirs();
//		}
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		int i = super.onStartCommand(intent, flags, startId);
		if(D) Log.e(TAG, "++ ON START ++");
		Toast.makeText(getBaseContext(), "Service is Started",Toast.LENGTH_SHORT).show();

		Toast.makeText(getBaseContext(), intent.getStringExtra("ADDRESS"), Toast.LENGTH_SHORT).show();
		
        if (!mBluetoothAdapter.isEnabled()) {
    		Toast.makeText(getBaseContext(), "Bluetooth is disable", Toast.LENGTH_SHORT).show();
    		stopSelf(START_NOT_STICKY);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null && intent.getStringExtra("ADDRESS").contains(":")){ 
            	setupChat();
	            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(intent.getStringExtra("ADDRESS"));
	            mChatService.connect(device);
	            
            } else if(mChatService == null){
        		Toast.makeText(getBaseContext(), "device_name is not accepted", Toast.LENGTH_SHORT).show();
        		stopSelf(START_NOT_STICKY);
            }
        }		
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(PPAMStates.FILTER);
        filter.addAction(buttonActionFilter);
		registerReceiver(MessageChecker, filter);
        
        partNumber = 0;
        		
        return i;
	}
	
	public void onDestroy() {
        if(D) Log.e(TAG, "--- ON DESTROY ---");
		Toast.makeText(getBaseContext(), "Service is Destroied", Toast.LENGTH_SHORT).show();
		
        // Stop the Bluetooth chat services
        if (mChatService != null){ 
        	mChatService.stop();
        	mChatService = null;
        }
		unregisterReceiver(MessageChecker);
		
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void setupChat() {
	    Log.d(TAG, "setupChat()");
	
	    // Initialize the BluetoothChatService to perform bluetooth connections
	    mChatService = new BluetoothChatService(this, mHandler);
	}
	
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
//            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
//            stopSelf(START_NOT_STICKY);
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
                break;
                
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                break;
                
            case MESSAGE_READ:
                // construct a string from the valid bytes in the buffer
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
/**/
//                Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_SHORT).show();
        	    Log.d(TAG, "Message Read : "+readBuf);
        	    
        		Intent intent = new Intent();
        		//intent.setComponent(component);
        		intent.setAction(bluetoothActionFilter);
        		intent.putExtra(bluetoothKey, readMessage);
        		sendBroadcast(intent);
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
    
    private void makePartMessage (String partName)
    {
    	
        String message;    
		if(partNumber<=SUB_LIMIT){	//&&subOn==0&&msgOn==1){
			message = "$SUBCHANGE"+String.format("%04d",partNumber)+"#"+(partNumber+1)+"_"+partName+"@";  
        	partNumber ++;	    
		}
		else
			message = "$SUBEND"+String.format("%04d",partNumber)+"#"+(partNumber+1)+"_"+partName+"@$WRRT@";
		sendMessage(message);
    }
    
    public BroadcastReceiver MessageChecker = new BroadcastReceiver() {
		
    	public void onReceive(Context context, Intent intent) {
    		// TODO Auto-generated method stub
    		if(intent.getAction().equals(PPAMStates.FILTER)&&runflag==1){
    			Bundle bundle = intent.getExtras();
	    		int tempGpsStatus = bundle.getInt(PPAMStates.GPS_STATUS, -1);
	    		int tempLocationListener = bundle.getInt(PPAMStates.GPS_LOCATIONLISTENER, -1);
	    		
	    		if(tempGpsStatus != -1)
	    			gpsStatus = tempGpsStatus;
	    		if(tempLocationListener != -1)
	    			locationListener = tempLocationListener;
/*	    		String tempName = "";
	    		
	    		switch(gpsStatus){
	    		case PPAMStates.GPS_EVENT_STARTED: gpsStatusName = "STARTED"; break; 
	    		case PPAMStates.GPS_EVENT_FIRST_FIX: gpsStatusName = "FIRST_FIX"; break; 
	    		case PPAMStates.GPS_EVENT_STOPPED: gpsStatusName = "STOPPED"; break; 
	    		case PPAMStates.GPS_EVENT_SATELLITE_STATUS: gpsStatusName = "SATELLITE"; break; 
//	    		case 4: PPAMStates.GPS_EVENT_SATELLITE_STATUS1 = "GPS_EVENT_SATELLITE_STATUS1"; break; 
//	    		case 5: PPAMStates.GPS_EVENT_SATELLITE_STATUS2 = "GPS_EVENT_SATELLITE_STATUS2"; break; 
//	    		case 6: PPAMStates.GPS_EVENT_SATELLITE_STATUS3 = "GPS_EVENT_SATELLITE_STATUS3"; break; 
	    		}

	    		switch(locationListener){
	    		case PPAMStates.GPS_UNKNOWN: locationListenerName = "UNKNOWN"; break; 
	    		case PPAMStates.GPS_LOCATION_CHANGED: locationListenerName = "LOCATION_CHANGED"; break; 
	    		case PPAMStates.GPS_PROVIDER_DISABLED: locationListenerName = "PROVIDER_DISABLED"; break; 
	    		case PPAMStates.GPS_PROVIDER_ENABLED: locationListenerName = "PROVIDER_ENABLED"; break; 
	    		case PPAMStates.GPS_TEMPORARILY_UNAVAILABLE: locationListenerName = "TEMPORARILY"; break;
	    		case PPAMStates.GPS_PROVIDER_AVAILABLE: locationListenerName = "AVAILABLE"; break;
	    		case PPAMStates.GPS_OUT_OF_SERVICE: locationListenerName = "OUT_OF_SERVICE"; break;
//	    		case 4: locationListenerName = "(GPS_STATUS_CHANGED)"; break; 
	    		}
	    		
	    		tempName = gpsStatus+"/"+locationListener;
*/	    		
	    	//	Toast.makeText(context, BroadcastedMessage, 1).show();  	
	    		//makePartMessage("GPS_EVENT_SATELLITE_STATUSGPS_LOCATION_CHANGED");
	    		makePartMessage(gpsStatus +"/"+locationListener);
    		}
    		if(intent.getAction().equals(buttonActionFilter)){
    			Toast.makeText(context, intent.getStringExtra(CommandKey), Toast.LENGTH_SHORT).show();
    			
    			if(intent.getStringExtra(CommandKey).contains("NEW")){

    				Calendar c = Calendar.getInstance();
    				int cur_time = c.get(Calendar.DATE)*1000000 +c.get(Calendar.HOUR_OF_DAY)*10000 + c.get(Calendar.MINUTE)*100 + c.get(Calendar.SECOND);
    		        String message = "$SETTIME#" + String.format("%08d",cur_time) + "@";
    		        sendMessage(message);
    				SystemClock.sleep(20);
    				message = "$NEWFILE@";
    		        sendMessage(message);
    				SystemClock.sleep(20);
    		        
//    		        savefile = new File(dirPath+"/"+ String.format("%08d",cur_time) +".csv");
//    		        try{
//    		        	fos = new FileOutputStream(savefile);
//    		        }
//    		        catch(IOException e){}
			        partNumber = 0;
    			}
    			else if(intent.getStringExtra(CommandKey).contains("START")){
    	    		String message = "$START@";
    				sendMessage(message);
    				SystemClock.sleep(20);
    				if(partNumber<=SUB_LIMIT){
    					message = "$SUBSTR"+String.format("%04d",partNumber)+"#START@";
    					sendMessage(message);
    				}
    				partNumber++;
    				runflag = 1;
    			}
    			else if(intent.getStringExtra(CommandKey).contains("END")){
    				
    				if(partNumber!=0){
    					String message= "$SUBEND"+String.format("%04d",partNumber)+"#PART"+partNumber+"@$WRRT@";
    					sendMessage(message);
    			    	Log.d("RunScriptActivity", "EndElement **");  
    					message = "$STOP@";
    					sendMessage(message);
        				runflag = 0;
    				}
    		    	
//    				try {
//    					if(fos!=null)
//    						fos.close();
//    				} catch (IOException e) {}
    				
    			}
    		}
			
		}
	}; 
}
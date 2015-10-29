package com.example.ppamoc2;


import java.util.Calendar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
    // Debugging
    private static final String TAG = "BluetoothChat";
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

    // Layout Views
//    private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    
    private Button mMainStart;
    private Button mMainEnd;
    private Button mWrrt;
    private Button mNewfile;
    
    private Button mSubStart;
    private Button mSubEnd;
    private Button mMsgStart;
    private Button mMsgEnd;
    private Button mRtc;
    
    private EditText mMsgEditText;
    
    private static int partNumber = 1;
    private static int subOn = 0;
    private static int msgOn = 0;
    private static String temp = "";

    
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
 //       requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
 //       getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
//        mTitle = (TextView) findViewById(R.id.title_left_text);
//        mTitle.setText(R.string.app_name);
//        mTitle = (TextView) findViewById(R.id.title_right_text);

        // Get local Bluetooth adapter
        //釉붾（�닾�뒪 �뼱�럞�꽣 �깮�꽦.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        //釉붾（�닾�뒪 �솢�꽦�솕 �솗�씤.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
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

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);
        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = "$SDCMD#" + view.getText().toString() + "@";
                sendMessage(message);
            }
        });
        
        mMainStart = (Button) findViewById(R.id.button_start);
        mMainStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                String message = "$START@";
                sendMessage(message);
			}
		});
        
        mMainEnd = (Button) findViewById(R.id.button_end);
        mMainEnd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                String message = "$STOP@";
                sendMessage(message);
			}
		});
        
        mWrrt = (Button) findViewById(R.id.button_wrrt);
        mWrrt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                String message = "$WRRT@";
                sendMessage(message);
			}
		});
        
        mNewfile = (Button) findViewById(R.id.button_new);
        mNewfile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                String message = "$NEWFILE@";
                sendMessage(message);
			}
		});
        
        mMsgStart = (Button) findViewById(R.id.button_msgstart);
        mMsgStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				if(msgOn==0){
					TextView view = (TextView) findViewById(R.id.edit_text_msg);
					temp = view.getText().toString();
					if(temp.isEmpty())
						temp = "TEST";
					String message = "$MSGSTR#"+temp+"@$WRRT@";
	                sendMessage(message);
//	                msgOn = 1;
//				}
//				else{
//				// 토스트 예정
//				}
			}
		});
        
        mMsgEnd = (Button) findViewById(R.id.button_msgend);
        mMsgEnd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				if(msgOn==1){
					String message = "$MSGEND#"+temp+"@$WRRT@";
	                sendMessage(message);
//	                msgOn = 0;
	                partNumber = 1;
//				}
//				else{
//				// 토스트 예정	
//				}
			}
		});
        

        mSubStart = (Button) findViewById(R.id.button_substart);
        mSubStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(partNumber<=9){	//&&subOn==0&&msgOn==1){
					String message = "$SUBSTR"+partNumber+"#PART"+partNumber+"@$WRRT@";
                	sendMessage(message);
//                	subOn = 1;
				}
//				else{
//				// 토스트 예정	
//				}
			}
		});
        
        mSubEnd = (Button) findViewById(R.id.button_subend);
        mSubEnd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				if(subOn==1&&msgOn==1){
					String message = "$SUBEND"+partNumber+"#PART"+partNumber+"@$WRRT@";
                	sendMessage(message);
                	partNumber ++;
//                	subOn = 0;
//				}
//				else{
//				// 토스트 예정	
//				}
			}
		});
        
        mRtc = (Button) findViewById(R.id.button_rtc);
        mRtc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                Calendar c = Calendar.getInstance();
                int cur_time = c.get(Calendar.DATE)*1000000 +c.get(Calendar.HOUR_OF_DAY)*10000 + c.get(Calendar.MINUTE)*100 + c.get(Calendar.SECOND);
                String message = "$SETTIME#" + String.format("%08d",cur_time) + "@";
            	sendMessage(message);
			}
		});

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        partNumber = 1;
        subOn = 0;
        msgOn = 0;
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {    	
        if(D) Log.d(TAG, "ensure discoverable");
        //�뒪耳� 紐⑤뱶 �꽕�젙. 
        
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
    	//�뿰寃곗긽�깭瑜� �솗�씤�븯怨� �뿰寃� �릺�뼱�엳�떎硫� 硫붿떆吏�瑜� �쟾�넚.
    	
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
    	
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
        	
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            
            case MESSAGE_STATE_CHANGE:
            	
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
//                    mTitle.setText(R.string.title_connected_to);
//                    mTitle.append(mConnectedDeviceName);
                    mConversationArrayAdapter.clear();
                    break;
                    
                case BluetoothChatService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
                    break;
                    
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
//                    mTitle.setText(R.string.title_not_connected);
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
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
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
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
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

}
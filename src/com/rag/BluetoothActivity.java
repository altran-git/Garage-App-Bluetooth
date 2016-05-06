package com.rag;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class BluetoothActivity extends Activity{
/** Called when the activity is first created. */
	
	/* Get Default Adapter */
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	//Set Bluetooth Device with known MAC ID
	private BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("00:12:03:19:75:12");
	private BluetoothSocket socket = null;
	
	private OutputStream outputStream;
	Button toggleGarage;
	
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//Lock the screen in Portrait mode
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
      //Toggle Garage Button
        toggleGarage = (Button) findViewById(R.id.toggleGarage);
        toggleGarage.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
        	try {
				outputStream.write("! ".getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  	      }
        });
        
        //Bluetooth enable thread
        Thread btOn = new Thread(){
        	public void run(){
	        	if (!mBluetoothAdapter.isEnabled()) {
					mBluetoothAdapter.enable();	
					while(mBluetoothAdapter.getState() != 12){
						try {
							sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
	        	synchronized(mBluetoothAdapter){
	        		Log.d("1", "1");
	        		try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	        		//Notify the Connection thread that Bluetooth is connected.
	    			mBluetoothAdapter.notify();
	    			Log.d("1.5", "1.5");
	    			
	    		}
        	}
        };
        
        //Bluetooth connect thread
        Thread btConnect = new Thread(){
    	    public void run(){
    	    	final EditText etConnected = (EditText) findViewById(R.id.statusText);
    	    	
    	    	try {
    	    		synchronized(mBluetoothAdapter){
    	    			Log.d("2", "2");
    	    			//Start the thread in Wait mode.  It waits for the Bluetooth Enable thread to signal it has
    	    			//enabled the Bluetooth device in the phone.
    	    			mBluetoothAdapter.wait();
    	    		}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
    	    	
    	    	try{
    	    		Log.d("3", "3");
    	    		mBluetoothAdapter.cancelDiscovery();
    	    		
    	    		/*Establish bluetooth socket and try to connect to Arduino device.
    	    		  00001101-0000-1000-8000-00805F9B34FB is a UUID for a Serial Port Service.
    	    		  I am using the Bluetooth as a serial device. */
    	    		socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
    	    		socket.connect();
    	    		outputStream = socket.getOutputStream();
    	    		
    	    		runOnUiThread(new Runnable(){
	    	    		public void run(){
	    	    			Log.d("4.5", "4.5");
	    	    			//Set the text in the textboxt to show connection
	    	    			etConnected.setText("Bluetooth Connected");
	    	    		}
	    	    	});
    	    		
    	    	} catch (IOException e) {
    				// TODO Auto-generated catch block
    	    		runOnUiThread(new Runnable(){
	    	    		public void run(){
	    	    			Log.d("4", "4");
	    	    			//Set the text in the textboxt to show fail
	    	    			etConnected.setText("Bluetooth Connection Failed");
	    	    	        
	    	    		}
	    	    	});
    	    		
    				e.printStackTrace();
    	    	}
    	    }
        };
        
        
        //Start the two threads
        btConnect.start();
        btOn.start();  
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			device = null;
			return false;
		}
		return false;
	}
    
    public void write(byte[] bytes) {
        try {
        	outputStream.write(bytes);
        } catch (IOException e) { }
    }
}

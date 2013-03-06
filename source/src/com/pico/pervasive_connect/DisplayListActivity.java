package com.example.wifidemo;

import java.util.List;

import com.example.wifidemo.MainActivity.WifiP2pEnvironmentInfo;

import android.app.ListActivity;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class DisplayListActivity extends ListActivity implements OnItemClickListener {
	
	public static final String DEVICE_NAME = "Device Name";
	public static final String PORT_NUMBER = "Port Number"; 
	
	public static final int PORT_NUM = 8888;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		// Get the list of discovered P2P devices
		Intent intent = getIntent();
		List<String> listofP2pDevices = intent.getStringArrayListExtra(MainActivity.WIFI_P2P_LIST);
		
		// Set layout for list activity and point its adapter to point to the returned list of P2P devices
		setContentView(R.layout.activity_display);
		setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, listofP2pDevices));
		
		// Get list view and set its handle to respond to user clicks on list items
		getListView().setOnItemClickListener(this);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void onItemClick(AdapterView listView, View view, int position, long id) {	
        String deviceName = (String) getListAdapter().getItem(position);
        
        if (environmentInfo == null) {
        	return;
        }
        
        final WifiP2pDevice targetDevice = environmentInfo.connectionToDeviceAvailable(deviceName);
        if (targetDevice == null) {
        	return;
        }
        
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = targetDevice.deviceAddress;
        config.wps.setup = WpsInfo.DISPLAY;
        config.groupOwnerIntent = 15;
		
        Log.d(DisplayListActivity.class.getName(), "Trying to connect to peer...");
        
        // Connect to the target device using configuration information
        DisplayListActivity.environmentInfo.manager.connect(environmentInfo.channel, config, new ActionListener() {
        	
			@Override
			public void onFailure(int reason) {
			}

			@Override
			public void onSuccess() {
				
				Log.d(DisplayListActivity.class.getName(), "Connected to peer ...");
				DisplayListActivity.deviceAddress = targetDevice.deviceAddress;
				
				DisplayListActivity.environmentInfo.manager.createGroup(DisplayListActivity.environmentInfo.channel, 
						new ActionListener() {
					
							@Override
							public void onFailure(int reason) {
								
								Log.d(DisplayListActivity.class.getName(), "Unable to create group of peers..." + String.valueOf(reason));
							}

							@Override
							public void onSuccess() {
							}
				});
				
				/* Try to get connection information. This is best effort based and not guaranteed to return the 
				 * connection information by the time the sender presses the send button after he has made a
				 * file selection.
				 */
				
				try {
					DisplayListActivity.environmentInfo.manager.requestConnectionInfo(DisplayListActivity.environmentInfo.channel, 
							SendActivity.class.newInstance());
				} catch (InstantiationException e) {
					Log.d(DisplayListActivity.class.getName(), e.getMessage());
				} catch (IllegalAccessException e) {
					Log.d(DisplayListActivity.class.getName(), e.getMessage());
				}
			}
		});
		
        // Create new activity once the user has made his selection
		Intent intent = new Intent(this, SendActivity.class);
		intent.putExtra(DisplayListActivity.DEVICE_NAME, deviceName);
		intent.putExtra(DisplayListActivity.PORT_NUMBER, Integer.valueOf(DisplayListActivity.PORT_NUM).toString());
		
		startActivity(intent);
	}
	
	public static void setWifiP2pEnvironmentInfo(final WifiP2pEnvironmentInfo environmentInfo) {
		
		DisplayListActivity.environmentInfo = environmentInfo;
	}
	
	static WifiP2pEnvironmentInfo environmentInfo = null;
	static String deviceAddress = null;
}

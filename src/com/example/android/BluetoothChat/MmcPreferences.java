package com.example.android.BluetoothChat;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MmcPreferences extends PreferenceActivity{

	
	public static final String PASSWORD = "password";
	public static final String DEVICE_ID = "deviceId";
	
	public MmcPreferences() {
		super();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_screen);
		
		
	}
	
	
}

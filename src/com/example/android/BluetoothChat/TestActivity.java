package com.example.android.BluetoothChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.TooManyListenersException;

import com.example.android.BluetoothChat.R.string;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TestActivity extends Activity  {

	private static final String TAG = TestActivity.class.getName();
	private static final int CENTER_HORIZONTAL = 1;
	private static final int CENTER_VERTICAL = 16;
	private TextView speedTextView;
	private TextView consumeTextView;
	private TextView voltageTextView;
	private TextView currentTextView;
	private TextView battTextView;
	private TextView reserveTextView;
	private ToggleButton onOffButton;
	private Button monitorButton;
	private ToggleButton lightButton;
	//private ProgressBar batteryBar;
	
	SharedPreferences prefs = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);	
	
		
		
		setContentView(R.layout.main_screen);
		speedTextView = (TextView) findViewById(R.id.speedTextView);

		speedTextView.setHeight(100);
		speedTextView.setTextColor(Color.BLACK);
		speedTextView.setTextSize(60);
		speedTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		speedTextView.setText("28.7 km/h");
		
		consumeTextView = (TextView) findViewById(R.id.consumeTextView);
		consumeTextView.setHeight(105);
		consumeTextView.setTextColor(Color.BLACK);
		consumeTextView.setTextSize(60);
		consumeTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		consumeTextView.setText("9.6 Wh/km");
		
		voltageTextView = (TextView) findViewById(R.id.voltageTextView);
		voltageTextView.setHeight(100);
		voltageTextView.setWidth(160);
		voltageTextView.setTextColor(Color.BLACK);
		voltageTextView.setTextSize(50);
		voltageTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		voltageTextView.setText("38.4 V");
		
		currentTextView = (TextView) findViewById(R.id.currentTextView);
		currentTextView.setHeight(100);
		currentTextView.setWidth(160);
		currentTextView.setTextColor(Color.BLACK);
		currentTextView.setTextSize(50);
		currentTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		currentTextView.setText("8.1 A");

		battTextView = (TextView) findViewById(R.id.battTextView);
		battTextView.setHeight(64);
		battTextView.setWidth(120);
		battTextView.setTextColor(Color.BLACK);
		battTextView.setTextSize(35);
		battTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		battTextView.setText("63 %");
		
		reserveTextView = (TextView) findViewById(R.id.reserveTextView);
		reserveTextView.setHeight(63);
		reserveTextView.setWidth(120);
		reserveTextView.setTextColor(Color.BLACK);
		reserveTextView.setTextSize(35);
		reserveTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		reserveTextView.setText("23 km");

		monitorButton = (Button) findViewById(R.id.monitorButton);
		monitorButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(TestActivity.this, BluetoothChat.class);
				startActivity(i);
			}
		});
		
		//reserveTextView.setHeight(30);
		
		onOffButton = (ToggleButton) findViewById(R.id.onOffButton);
		onOffButton.setWidth(98);
		onOffButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//
			}
		});

		lightButton = (ToggleButton) findViewById(R.id.lightButton);
		lightButton.setWidth(98);
		lightButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//
			}
		});
		
		//batteryBar = (ProgressBar) findViewById(R.id.batteryBar);
		//batteryBar.setBackgroundColor(Color.GRAY);
		//batteryBar.setAnimation(null);
		//batteryBar.setEnabled(true);	

	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		login();
	}
	
	BluetoothSocket socket = null;
	InputStream mmInStream = null;
	OutputStream mmOutStream = null;
	

	public void login() {
		Log.v(TAG, "starting login");
		try {
			
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			Log.v(TAG, "got adapter");
			BluetoothDevice mmDevice = mBluetoothAdapter
					.getRemoteDevice("00:12:6F:20:DF:B1");
			Log.v(TAG, "got device");
			Method m = mmDevice.getClass().getMethod("createRfcommSocket",
					new Class[] { int.class });
			socket = (BluetoothSocket) m.invoke(mmDevice,
					Integer.valueOf(1));
			
			socket.connect();
			Log.v(TAG, "socket connected");
			mmInStream  = socket.getInputStream();
			mmOutStream = socket.getOutputStream();

			final InputStream inStream = mmInStream;
			final BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
			
			new Thread(new Runnable() {
				
				public void run() {
					try {
						String line = null;
						while( (line = reader.readLine()) != null) {
							Log.v(TAG, line);
							try {
								StringTokenizer t = new StringTokenizer(line, "\t");
								if (t.countTokens() >= 4) {
									
									final String voltage = Float.toString(((float)Integer.parseInt(t.nextToken())) / 10);
									
									final String current = t.nextToken();
									final String speed = t.nextToken();
									final String capacity = t.nextToken();
									TestActivity.this.runOnUiThread(new Runnable() {
										
										public void run() {
											voltageTextView.setText(voltage + "V");
											currentTextView.setText(current);
											speedTextView.setText(speed);
											int speedInt = Integer.parseInt(speed);
											battTextView.setText(capacity);
										}
									});

									
								}
								
							} catch (Exception e) {
								Log.e(TAG, "Processing line failed",e);
							}
								
							
						}
					} catch (Exception e) {
						Log.v(TAG, "Reading thread throwed exception", e);
					}
				}
				
			}).start();
			
			// password_id?
			String password = prefs.getString("password_id", "");
			Log.v(TAG, "password read:" + password);
			mmOutStream.write((password + "\n\r").getBytes());
			//mmOutStream.write(("a\r").getBytes());

			//byte[] buffer = new byte[1024];
			//int len = mmInStream.read(buffer);
			//Log.v(TAG, "Read:" + new String(buffer, 0, len));
			
			Log.v(TAG, "password sent");
			// MK

			mmOutStream.write("at-push=1\r".getBytes());
			//mmOutStream.write("at-logout\r".getBytes());
			Log.v(TAG, "at-push=1 sent");
			

		} catch (Exception e) {
			AlertDialog dlg = new AlertDialog.Builder(this)
			.setTitle("Login failed")
			.setMessage(e.getMessage()).create();
			dlg.show();
			// TODO Auto-generated catch block
			Log.w(TAG, "login failed", e);
		}
		finally {
			
		}

	}
	
	private void logout() {
		Log.v(TAG, "start logout");
		try {
			if (mmOutStream != null) {
				mmOutStream.write("at-push=0\r".getBytes());
				mmOutStream.write("at-logout\r".getBytes());	
			}
			close();
		} catch (Exception e) {
			close();
		}
	}

	private void close() {
		Log.v(TAG, "Closing streams and socket");
		try {
			if (mmOutStream != null)  mmOutStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			if (mmInStream != null)  mmInStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			if (socket != null) socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mmc_options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.settings) {
			Intent i = new Intent(this, MmcPreferences.class);
			startActivity(i);
			return true;
		}
		if (item.getItemId() == R.id.logout) {
			logout();
		}
		if (item.getItemId() == R.id.login) {
			login();
		}
		return super.onOptionsItemSelected(item);
	}
}

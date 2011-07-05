package at.elfkw.mmc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
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
	private TextView mTitle;
	
	private Button battfullButton;
	 
	SharedPreferences prefs = null;
	private static final String PASSWORD_ID = "password_id";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);	
	
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main_screen);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		
		mTitle = (TextView) findViewById(R.id.title_left_text);
		
		mTitle.setText(R.string.title_elfkw);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		
		
		speedTextView = (TextView) findViewById(R.id.speedTextView);

		speedTextView.setTextColor(Color.BLACK);
		speedTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		speedTextView.setText("0.0km/h");
		
		consumeTextView = (TextView) findViewById(R.id.consumeTextView);
		consumeTextView.setTextColor(Color.BLACK);
		consumeTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		consumeTextView.setText("--.-Wh/km");
		
		voltageTextView = (TextView) findViewById(R.id.voltageTextView);
		voltageTextView.setTextColor(Color.BLACK);
		voltageTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		voltageTextView.setText("0.0V");
		
		
		currentTextView = (TextView) findViewById(R.id.currentTextView);
		currentTextView.setTextColor(Color.BLACK);
		currentTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		currentTextView.setText("0.0|0.0A");

		battTextView = (TextView) findViewById(R.id.battTextView);
		battTextView.setTextColor(Color.BLACK);
		battTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		battTextView.setText("0.0Ah");
		
		reserveTextView = (TextView) findViewById(R.id.reserveTextView);
		reserveTextView.setTextColor(Color.BLACK);
		reserveTextView.setGravity(CENTER_HORIZONTAL + CENTER_VERTICAL);
		reserveTextView.setText("--.-km");

		monitorButton = (Button) findViewById(R.id.monitorButton);
		monitorButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(TestActivity.this, BluetoothChat.class);
				startActivity(i);
			}
		});
		
		//reserveTextView.setHeight(30);
		
		onOffButton = (ToggleButton) findViewById(R.id.onOffButton);
		onOffButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				 try {
			         if (socket != null) {
			                 Log.v(TAG, "changing motor status");
			                 mmOutStream.write("at-push=0\r".getBytes());
			                 if (isChecked) {
			                         mmOutStream.write("at-eon\r".getBytes());
			                 }
			                 else {
			                         mmOutStream.write("at-eoff\r".getBytes());
			                 }
			                 mmOutStream.write("at-push=1\r".getBytes());
			         }
			 } catch (Exception e) {
			         Log.v(TAG, "changing motor status failed" ,e);
			 }
			}
		});

		lightButton = (ToggleButton) findViewById(R.id.lightButton);
		lightButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				try {
					Log.v(TAG, "changing light status");
					mmOutStream.write("at-push=0\r".getBytes());
					if (isChecked) {
						
						mmOutStream.write("at-light=0\r".getBytes());
					}
					else {
						mmOutStream.write("at-light=1\r".getBytes());
					}
					mmOutStream.write("at-push=1\r".getBytes());
				} catch (Exception e) {
					Log.v(TAG, "changing light status failed" ,e);
				}
			}
		});
		
		battfullButton = (Button) findViewById(R.id.battfullButton);
		battfullButton.setOnClickListener(new View.OnClickListener() {
		        public void onClick(View v) {
		                try {
		                        if (socket != null) {
		                                Log.v(TAG, "setting battery full");
		                                mmOutStream.write("at-push=0\r".getBytes());
		                                mmOutStream.write("at-ccap\r".getBytes());
		                                mmOutStream.write("at-push=1\r".getBytes());
		                        }
		                } catch (Exception e) {
		                        Log.v(TAG, "setting battery full failed" ,e);
		                }
		        }
		});
		
		//batteryBar = (ProgressBar) findViewById(R.id.batteryBar);
		//batteryBar.setBackgroundColor(Color.GRAY);
		//batteryBar.setAnimation(null);
		//batteryBar.setEnabled(true);
		
		 try {
	         String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
	         Editor edit = prefs.edit();
	         edit.putString("version_id", version);
		 } catch (Exception e2) {
	         // TODO: handle exception
		 }
	 
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	protected void onStop() {
		logout();
		super.onStop();
	};
	
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
	
	BluetoothAdapter mBluetoothAdapter = null;
	BluetoothSocket socket = null;
	InputStream mmInStream = null;
	OutputStream mmOutStream = null;
	
	   @Override
	    public void onStart() {
	        super.onStart();
	        Log.e(TAG, "++ ON START ++");

	        // If BT is not on, request that it be enabled.
	        // setupChat() will then be called during onActivityResult
	        if (mBluetoothAdapter != null) {
		        if (!mBluetoothAdapter.isEnabled()) {
		            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		        }
		        else {
		        	login();	
		        }
	        }
	    }

	public void login() {
		Log.v(TAG, "starting login");
		try {
			
			
			Log.v(TAG, "got adapter");
			String deviceId = prefs.getString("device_id","");
			
			if ("".equals(deviceId)) {
	            Intent serverIntent = new Intent(this, DeviceListActivity.class);
	            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	            return;
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			
			BluetoothDevice mmDevice = mBluetoothAdapter
					.getRemoteDevice(deviceId);
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
								if (t.countTokens() >= 5) {
									
									final float fVoltage = (float)Integer.parseInt(t.nextToken())/10;
									final String sVoltage = Float.toString(fVoltage);
									
									final float fCurrent = (float)(Math.round((float)Integer.parseInt(t.nextToken())/1000*100.0)/100.0);
									final String sCurrent = Float.toString(fCurrent);
									
									final float fSpeed = (float)Integer.parseInt(t.nextToken())/10;
									final String sSpeed = Float.toString(fSpeed);

									float fConsume = (float)0.0;
									String sConsume = Float.toString(fConsume);
									if ((fCurrent < 0.01) || (fSpeed < 0.1)) {
										sConsume = "--.-";
									} else {
										fConsume = (float)(Math.round(fVoltage*fCurrent/fSpeed*10.0)/10.0);
										sConsume = Float.toString(fConsume);
									}
									final String fsConsume = sConsume; 
									
									final float fUsedCapa = Math.round(((float)Integer.parseInt(t.nextToken())/3600)*100.0/100.0);
									final String sCapa = prefs.getString("capacity_id","");									
									float fCapa = (float)Integer.parseInt(sCapa);
									DecimalFormat df = new DecimalFormat("#.#");
									final float fRestCapa = (float)(fCapa-fUsedCapa)/1000;
									final String sRestCapa = df.format(fRestCapa);
									Log.v(TAG, "sRestCapa=" + sRestCapa);

									float fRestKm = (float)0.0;
									String sRestKm = Float.toString(fRestKm);	
									if (fCurrent < 0.01) {
										sRestKm = "--.-";
									} else {
										fRestKm = (float)(Math.round(fRestCapa/fCurrent*fSpeed*10.0)/10.0);
										sRestKm = Float.toString(fRestKm);
									}
									final String fsRestKm = sRestKm; 
									
									final float fMaxCurrent = (float)(Math.round((float)Integer.parseInt(t.nextToken())/1000*100.0)/100.0);
									final String sMaxCurrent = Float.toString(fMaxCurrent);
									 
									TestActivity.this.runOnUiThread(new Runnable() {	
										public void run() {
											currentTextView.setText(sCurrent + "|" + sMaxCurrent + "A");
											voltageTextView.setText(sVoltage + "V");
											speedTextView.setText(sSpeed + "km/h");
											consumeTextView.setText(fsConsume + "Wh/km");
											battTextView.setText(sRestCapa + "Ah");
											reserveTextView.setText(fsRestKm + "km");
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
					finally {
						/*
						TestActivity.this.runOnUiThread(new Runnable() {
							
							public void run() {
								mTitle.setText(R.string.title_not_connected);		
							}
						});
						
						try {
							socket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						*/
					}
				}
				
			}).start();
			
			Thread.sleep(500);
			
			// password_id?
			String password = prefs.getString(PASSWORD_ID, "");
			Log.v(TAG, "password read:" + password);
			mmOutStream.write((password + "\r").getBytes());
			//mmOutStream.write(("a\r").getBytes());

			//byte[] buffer = new byte[1024];
			//int len = mmInStream.read(buffer);
			//Log.v(TAG, "Read:" + new String(buffer, 0, len));
			
			Log.v(TAG, "password sent");
			
			mTitle.setText(R.string.title_connected_to);
			// MK

			mmOutStream.write("at-push=1\r".getBytes());
			//mmOutStream.write("at-logout\r".getBytes());
			Log.v(TAG, "at-push=1 sent");
			
			new Thread(new Runnable() {
				
				public void run() {
					while (true) {
						try {
							Thread.sleep(60000);
							Log.v(TAG, "Writing at-0");
							mmOutStream.write("at-0\r".getBytes());
							
						} catch (Exception e) {
							Log.v(TAG, "Writing at-0 thread throwed exception", e);
						}
					}
					
					
				}
			});
			

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
		finally {
			mTitle.setText(R.string.title_not_connected);
		}
	}

	private void close() {
		Log.v(TAG, "Closing streams and socket");
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
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                Editor editor = prefs.edit();
                editor.putString("device_id", address);
                editor.commit();
                
                String password = prefs.getString(PASSWORD_ID, "");
                if (password.equals("")) {
                        Editor edit = prefs.edit();
                        edit.putString(PASSWORD_ID, "1234");
                }
                // Attempt to connect to the device
                login();
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
            	login();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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
		if (item.getItemId() == R.id.exit) {
	        finish();
	        return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

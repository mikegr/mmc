/*
 *forced disconnect
 *(mmc sync thread in einen service auslagern)
 * möglichkeit direkt vom mainscreen aus commands zu senden
 * schriftfarbe
 * (gesture editor einfügen)
 * seek bars in preferences zum Einstellen der p und i werte
 * einstellbares Hintergrundbild
 */


package at.elfkw.mmc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
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

public class TestActivity extends Activity implements TextToSpeech.OnInitListener, OnGesturePerformedListener {

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
	private ToggleButton boostButton;
	private Button monitorButton;
	private ToggleButton lightButton;
	//private ProgressBar batteryBar;
	private TextView mTitle;
	private Button battfullButton;
	private TextToSpeech mTts;
	private boolean ttson=false;
	private boolean running=true;
	private boolean forbidLogin=false;
	final int HELLO_ID = 1234;
	
	private GestureLibrary mLibrary;

	SharedPreferences prefs = null;
	private static final String PASSWORD_ID = "password_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);	


		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main_screen);


		mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!mLibrary.load()) {
			//  finish();
			Toast toast = Toast.makeText(TestActivity.this, "error loading gestures", Toast.LENGTH_LONG);
			toast.show(); 
		}


		// Initialize text-to-speech. This is an asynchronous operation.
		// The OnInitListener (second argument) is called after initialization completes.

		mTts = new TextToSpeech(this,
				this  // TextToSpeech.OnInitListener
				);


		GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gesturescreen);
		gestures.addOnGesturePerformedListener(this);

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
							tts("Der Motor wurde gestartet");
						}
						else {
							mmOutStream.write("at-eoff\r".getBytes());
							tts("Der Motor wurde ausgeschaltet");
						}
						mmOutStream.write("at-push=1\r".getBytes());
					}
					else ttstoast("es ist ein Fehler aufgetreten", "es ist ein Fehler aufgetreten");

				} catch (Exception e) {
					Log.v(TAG, "changing motor status failed" ,e);
					ttstoast("es ist ein Fehler aufgetreten", "Error");

				}
			}
		});



		boostButton = (ToggleButton) findViewById(R.id.boostButton);
		boostButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				try {
					if (socket != null) {     
						Log.v(TAG, "changing speedlimit");
						mmOutStream.write("at-push=0\r".getBytes());
						if (isChecked) {
							String vboost = prefs.getString("vboost_id","");
							String vboostsend= "at-sl2="+vboost+"0\r";
							mmOutStream.write(vboostsend.getBytes());
							boostButton.setTextOn(vboost+"km/h");
							tts("v max " + vboost + " km/h");

						}
						else {
							String vmax = prefs.getString("vmax_id","");
							String vmaxsend= "at-sl2="+vmax+"0\r";
							mmOutStream.write(vmaxsend.getBytes());
							boostButton.setTextOff(vmax+ "km/h"); 
							tts("v max "+ vmax + "km/h");
						}
						mmOutStream.write("at-push=1\r".getBytes());
					}
					else{
						ttstoast("es ist ein Fehler aufgetreten", "Error");
					}
				}

				catch (Exception e) {
					Log.v(TAG, "changing speedlimit failed" ,e);
					ttstoast("es ist ein Fehler aufgetreten", "Error");
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
						mmOutStream.write("at-light=1\r".getBytes());
						tts("Licht an");
					}
					else {
						mmOutStream.write("at-light=0\r".getBytes());
						tts("Licht aus");
					}
					mmOutStream.write("at-push=1\r".getBytes());
				} catch (Exception e) {
					Log.v(TAG, "changing light status failed" ,e);
					ttstoast("es ist ein Fehler aufgetreten", "Error");

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
					else ttstoast("es ist ein Fehler aufgetreten", "Error");

				} catch (Exception e) {
					Log.v(TAG, "setting battery full failed" ,e);
					ttstoast("es ist ein Fehler aufgetreten", "Error");
				}
			}
		});

		//batteryBar = (ProgressBar) findViewById(R.id.batteryBar);
		//batteryBar.setBackgroundColor(Color.GRAY);
		//batteryBar.setAnimation(null);
		//batteryBar.setEnabled(true);

		//???
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
		//logout();
		super.onStop();

		boolean autologin = prefs.getBoolean("autologin_id",false);
		if (autologin==true){
			logout();
		}
		
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
		try {
			Thread.sleep(1000);//ohne gibt es teilweise Probleme (blackscreen beim �ffnen)
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

		Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (mBluetoothAdapter != null) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			}
			else {
				boolean autologin = prefs.getBoolean("autologin_id",false);
				if (autologin==true & forbidLogin==false){
					login(); //auftretende Probleme wenn man im eingeloggten zustand die Einstellung auf autolgin stellt
				}
				
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
					while(running==true){	//zus�tzliche Absicherung
					try {
						String line = null;
						while( (line = reader.readLine()) != null  && running==true) {
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
									final float fRestPercentage = 100000*fRestCapa/fCapa;
									final String sRestPercentage = df.format(fRestPercentage);
									Log.v(TAG, "sRestCapa=" + sRestCapa);

									float fRestKm = (float)0.0;
									String sRestKm = Float.toString(fRestKm);
									if ((fCurrent < 0.01) || (fSpeed < 0.1)) {
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
											statusbar("Restkapazit�t: " + sRestCapa + " Ah    " + sRestPercentage + " %");
											
											try {
												Thread.sleep(100);
												mmOutStream.write("at-0\r".getBytes());

											} catch (Exception e)
											{
												ttstoast("","at-0 Fehler");
											}

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
				}

			}).start();
			

			Thread.sleep(500);

			// password_id?
			String password = prefs.getString(PASSWORD_ID, "");
			Log.v(TAG, "password read:" + password);
			mmOutStream.write((password + "\r").getBytes());
			Thread.sleep(100);
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

			//Thread sendet keine commands ans mmc
			/*	new Thread(new Runnable() {

				public void run() {
					while (true) {
						try {
							Thread.sleep(5000);
							Log.v(TAG, "Writing at-0");
							mmOutStream.write("at-0\r".getBytes());
						} catch (Exception e) {
							Log.v(TAG, "Writing at-0 thread throwed exception", e);
							ttstoast("exception","at-0 thread throwed exception");
						}
					}



				}
			});
			 */

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
				//	String vmax = prefs.getString("vmax_id",""); //boost speed zur�cksetzen
				//	String vmaxsend= "at-sl2="+vmax+"\r";
				//	mmOutStream.write(vmaxsend.getBytes());
				mmOutStream.write("at-logout\r".getBytes());
				running=false;
			}
			close();
		} catch (Exception e) {
			close();
			running=false;
		}
		finally {
			mTitle.setText(R.string.title_not_connected);
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
			mNotificationManager.cancel(HELLO_ID); 

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
	public void onDestroy() {
		super.onDestroy();
		logout();
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.cancel(HELLO_ID);
		Toast.makeText(this, "mmc destroyed", Toast.LENGTH_SHORT).show();
		finish();
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
					ttstoast("Geben sie ihr Passwort in den Einstellungen ein","kein Passwort hinterlegt");
					//Editor edit = prefs.edit();
					//edit.putString(PASSWORD_ID, "1234");
				}
				// Attempt to connect to the device
				boolean autologin = prefs.getBoolean("autologin_id",false);
				if (autologin==true){
					login();
				}
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				boolean autologin = prefs.getBoolean("autologin_id",false);
				if (autologin==true){
					login();
				}
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
			boolean autologin = prefs.getBoolean("autologin_id",false);
			if (autologin==false){
				forbidLogin=true;
			}
			Intent i = new Intent(this, MmcPreferences.class);
			startActivity(i);
			return true;
		}
		if (item.getItemId() == R.id.logout) {
			logout();
		}
		if (item.getItemId() == R.id.login) {
			running=true;
			login();

		}
		if (item.getItemId() == R.id.exit) {
			logout();
			finish();
			return true;
		}
		if (item.getItemId() == R.id.applyall) {
			applyall();
		}
		return super.onOptionsItemSelected(item);
	}






	/////////////

	public void applyall(){

		try {
			if (socket != null) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				String vlow = prefs.getString("vlow_id","");
				String vlowsend= "at-sl1="+vlow+"0\r";

				String speedcalib = prefs.getString("speedcalib_id","");
				String speedcalibsend = "at-mm="+speedcalib+"\r";
				//at-mm=n set millimeters between 2 tacho pulses for speed measurement

				String pcurrent = prefs.getString("pcurrent_id", ""); 
				String pcurrentsend= "at-kpc="+pcurrent+"\r";
				//at-kpc=n proportional factor for current regulation (n = 0...16)

				String icurrent = prefs.getString("icurrent_id","");
				String icurrentsend= "at-kic="+icurrent+"\r";
				//at-kic=n integral factor for current regulation (n = 0...16)


				String pspeed = prefs.getString("pspeed_id", ""); 
				String pspeedsend= "at-kps="+pspeed+"\r";
				//at-kps=n proportional factor for speed regulation (n = 0...16)

				String ispeed = prefs.getString("ispeed_id","");
				String ispeedsend= "at-kis="+ispeed+"\r";
				//at-kis=n integral factor for speed regulation (n = 0...16)	

				String lightvoltage = prefs.getString("lightvoltage_id","");
				String lightvoltagesend= "at-lvl="+lightvoltage+"\r";
				//at-lvl=n	set light voltage level (n = 1...4)

				String pedal = prefs.getString("pedalmode_id","");
				String pedalmodesend= "at-pedal="+pedal+"\r";

				String lowcurrent = prefs.getString("lowcurrent_id","");
				String lowcurrentsend= "at-cl1="+lowcurrent+"\r";

				String highcurrent = prefs.getString("highcurrent_id","");
				String highcurrentsend= "at-cl2="+highcurrent+"\r";

				String highcurrenttime = prefs.getString("highcurrenttime_id","");
				String highcurrenttimesend= "at-clt="+highcurrenttime+"\r";

				//////////////////////////////////////

				Log.v(TAG, "apply all");
				//daten senden
				mmOutStream.write("at-push=0\r".getBytes());


				if (boostButton.isChecked()){
					String vboost = prefs.getString("vboost_id",""); 
					String vboostsend= "at-sl2="+vboost+"0\r";
					mmOutStream.write(vboostsend.getBytes());
				}
				else{
					String vmax = prefs.getString("vmax_id","");
					String vmaxsend= "at-sl2="+vmax+"0\r";
					mmOutStream.write(vmaxsend.getBytes());
				}
				Thread.sleep(100); //Teilweise nicht übernommene commands ohne sleep
				mmOutStream.write(vlowsend.getBytes());
				Thread.sleep(100); 
				mmOutStream.write(speedcalibsend.getBytes());
				Thread.sleep(100);
				mmOutStream.write(pcurrentsend.getBytes());
				Thread.sleep(100);
				mmOutStream.write(icurrentsend.getBytes());
				Thread.sleep(100);
				mmOutStream.write(pspeedsend.getBytes());
				Thread.sleep(100);
				mmOutStream.write(ispeedsend.getBytes());
				Thread.sleep(100);
				mmOutStream.write(lightvoltagesend.getBytes());
				Thread.sleep(100);
				mmOutStream.write(pedalmodesend.getBytes());
				Thread.sleep(100);
				mmOutStream.write(highcurrenttimesend.getBytes());
				Thread.sleep(100);
				mmOutStream.write(lowcurrentsend.getBytes());
				Thread.sleep(100);
				mmOutStream.write(highcurrentsend.getBytes());	
				Thread.sleep(100);
				mmOutStream.write("at-push=1\r".getBytes());
				ttstoast("Die Einstellungen wurden gesendet","gesendet");

			}
			else{
				ttstoast("Die Einstellungen konnten nicht gesendet werden", "Error");

			}
		} catch (Exception e) {
			ttstoast("Die Einstellungen konnten nicht gesendet werden", "Error");
			Log.v(TAG, "applyall failed" ,e);
		}
	}
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		if (status == TextToSpeech.SUCCESS) {
			int result = mTts.setLanguage(Locale.GERMAN);
			if (result == TextToSpeech.LANG_MISSING_DATA ||
					result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Lanuage data is missing or the language is not supported.
				Log.e(TAG, "Language is not available.");
			} else {
				//enable text output
				ttson=true;
				//tts("Willkommen");
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				String willkommen = prefs.getString("welcome_id","");
				tts(willkommen);
			}
		} else {
			// Initialization failed.
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}

	// Text to speech Ausgabe
	protected void tts(String text) {
		boolean ttssettings = prefs.getBoolean("ttssettings_id",false);
		if (ttson==true & ttssettings==true){
			mTts.speak(text,
					TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
					null);
		}
	}

	// Text to speech Ausgabe plus toast
	protected void ttstoast(String tts,String toast) {
		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
		boolean ttssettings = prefs.getBoolean("ttssettings_id",false);
		if (ttson==true & ttssettings==true){
			mTts.speak(tts,
					TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
					null);
		}
	}
	protected void statusbar(String text) {
		boolean autologin = prefs.getBoolean("autologin_id",false);
		if (autologin==false){
	
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.app_icon;
		CharSequence tickerText = "Akku Status";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = "Akku Status";
		CharSequence contentText = text;
		Intent notificationIntent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(HELLO_ID, notification);
		}

	}




	// gesten wurden mit dem gesture Builder aus den api Demos erstellt
	// ein gesture editor könnte gleich in den mmc viewer eingebunden werden
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		if (predictions.size() > 0 && predictions.get(0).score > 3.0) {
			String action = predictions.get(0).name;
			if ("fast".equals(action)) {
				boostButton = (ToggleButton) findViewById(R.id.boostButton);
				boostButton.setChecked(true);
			} else if ("slow".equals(action)) {
				boostButton = (ToggleButton) findViewById(R.id.boostButton);
				boostButton.setChecked(false);
			} else if ("hideboost".equals(action)) {
				boostButton = (ToggleButton) findViewById(R.id.boostButton);
				boostButton.setVisibility(8);


			} else if ("showboost".equals(action)) {
				boostButton = (ToggleButton) findViewById(R.id.boostButton);
				boostButton.setVisibility(0);
			}
		}
		else{
			ttstoast("Eingabe nicht erkannt", "Eingabe nicht erkannnt");


		}
	}

}





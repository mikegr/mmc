package at.elfkw.mmc;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;


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
		
		findPreference("version_id").setEnabled(false);	
		
	
		Toast toast = Toast.makeText(MmcPreferences.this, "Nicht vergessen die Einstellungen anschließend im Hauptbildschirm ans MMC zu senden", Toast.LENGTH_LONG);
		toast.show();
	}


}




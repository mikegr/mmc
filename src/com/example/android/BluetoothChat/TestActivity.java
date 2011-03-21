package com.example.android.BluetoothChat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.example.android.BluetoothChat.R.string;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.widget.TextView;

public class TestActivity extends Activity implements Runnable {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		run();
		setContentView(new TextView(this));
	}

	public void run() {
		// TODO Auto-generated method stub
		BluetoothSocket socket = null;
		InputStream mmInStream = null;
		OutputStream mmOutStream = null;
		try {
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();

			BluetoothDevice mmDevice = mBluetoothAdapter
					.getRemoteDevice("00:12:6F:20:DF:B1");

			Method m = mmDevice.getClass().getMethod("createRfcommSocket",
					new Class[] { int.class });
			socket = (BluetoothSocket) m.invoke(mmDevice,
					Integer.valueOf(1));
			socket.connect();
			mmInStream  = socket.getInputStream();
			mmOutStream = socket.getOutputStream();

		
			
			final InputStream inStream = mmInStream;
			new Thread(new Runnable() {
				
				public void run() {
					try {
						byte[] buffer = new byte[256];
						int size = 0;
						while(true) {
							size = inStream.read(buffer);
							out(buffer, size);		
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}

				public void out(byte[] buffer, int size) {
					
					System.out.println("\n" + new String(buffer, 0, size));
				}
			}).start();
			
			mmOutStream.write("a\r".getBytes());
			mmOutStream.write("at-push=1\r".getBytes());
			mmOutStream.write("at-logout\r".getBytes());
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			
			try {
				mmOutStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				mmInStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}


}

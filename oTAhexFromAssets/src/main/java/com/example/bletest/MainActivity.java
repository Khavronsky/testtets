package com.example.bletest;



import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private EditText et;
	private Button bt_day,bt_total,bt_goal;
	private TextView tv;
	private BroastRecevice recevicer;
	private RFLampDevice device;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		bt_day=(Button)findViewById(R.id.bt_day);
		bt_day.setOnClickListener(this);
		bt_total=(Button)findViewById(R.id.bt_total);
		bt_goal=(Button)findViewById(R.id.bt_goal);
		findViewById(R.id.bt_send).setOnClickListener(this);
		findViewById(R.id.bt_total).setOnClickListener(this);
		findViewById(R.id.bt_goal).setOnClickListener(this);
		et=(EditText)findViewById(R.id.et);
		tv=(TextView)findViewById(R.id.tv_recevice);
		recevicer=new BroastRecevice();
		IntentFilter filter=new IntentFilter();
		filter.addAction(LightBLEService.ACTION_DATA_AVAILABLE);
		filter.addAction(LightBLEService.ACTION_GATT_CONNECTED);
		filter.addAction(LightBLEService.ACTION_GATT_DISCONNECTED);
		registerReceiver(recevicer, filter);
		device=new RFLampDevice(this, Tools.device);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(device!=null&device.isConnected()){	
			device.disconnectedDevice();
		}
		unregisterReceiver(recevicer);
	}
	private int mode;
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.bt_day:
			mode=1;
			setColor();
			break;
		case R.id.bt_send:
			tv.setText("");
			sendData();
			break;
		case R.id.bt_total:
			mode=2;
			setColor();
			break;
		case R.id.bt_goal:
			mode=3;
			setColor();
			break;
		default:
			break;
		}

	}

	private void setColor() {
		bt_day.setBackgroundColor(Color.rgb(232, 246, 184));
		bt_total.setBackgroundColor(Color.rgb(232, 246, 184));
		bt_goal.setBackgroundColor(Color.rgb(232, 246, 184));
		if(mode==1){
			bt_day.setBackgroundColor(Color.LTGRAY);
		}else if(mode==2){
			bt_total.setBackgroundColor(Color.LTGRAY);
		}else if(mode==3){
			bt_goal.setBackgroundColor(Color.LTGRAY);
		}
		
	}

	private void sendData() {
		
//		String day="";
//		if(!TextUtils.isEmpty(et.getText().toString())){
//			day=et.getText().toString();
//		}
//		if(mode==1){//43
//			System.out.println("43");
//			Tools.device.SportDetali(day);
//		}else if(mode==2){//07
//			System.out.println("07");
//			Tools.device.total(day);
//		}else if(mode==3){//08
//			System.out.println("08");
//			Tools.device.goal(day);
//		}
		device.sendUpdate();
	}
class BroastRecevice extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent intent) {
		String action=intent.getAction();
		byte[]value=intent.getByteArrayExtra(LightBLEService.EXTRA_DATA);
		if(action.equals(LightBLEService.ACTION_DATA_AVAILABLE)){
			if(value!=null){
				tv.append(Tools.byte2Hex(value)+"\n");
			}
		}else if(action.equals(LightBLEService.ACTION_GATT_CONNECTED)){
			Toast.makeText(MainActivity.this, "device is connected", Toast.LENGTH_SHORT).show();
		}else if(action.equals(LightBLEService.ACTION_GATT_DISCONNECTED)){
			Toast.makeText(MainActivity.this, "device is disconnected", Toast.LENGTH_SHORT).show();
		}
		
	}
	
}
}

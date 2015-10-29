package com.example.ppam2modulepower;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class MakeScriptActivity extends Activity {
	ArrayList<TestScriptElement> listElements;
	ArrayAdapter<TestScriptElement> adapter;
	ListView scriptView;
	
	EditText editDefaultTime;
	
	Button btnMakeStart;
	Button btnMakeEnd;
	Button btnMakeAccSet;
	Button btnMakeAccEnd;
	Button btnMakeGpsSet;
	Button btnMakeGpsEnd;
	Button btnMakeNetworkSet;
	Button btnMakeNetworkEnd;
	Button btnMakeDelOne;
	Button btnMakeDelAll;
	Button btnMakeDefault;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        listElements = (ArrayList<TestScriptElement>) getIntent().getSerializableExtra("listElements");
        
        setContentView(R.layout.activity_makescript);
        scriptView = (ListView)findViewById(R.id.script_viewer_make);
        
        editDefaultTime = (EditText)findViewById(R.id.edit_duetime);
        
    	btnMakeStart = (Button)findViewById(R.id.btn_make_str);
    	btnMakeEnd = (Button)findViewById(R.id.btn_make_end);
    	btnMakeAccSet = (Button)findViewById(R.id.btn_make_acc);
    	btnMakeAccEnd = (Button)findViewById(R.id.btn_make_accend);
    	btnMakeGpsSet = (Button)findViewById(R.id.btn_make_gps);
    	btnMakeGpsEnd = (Button)findViewById(R.id.btn_make_gpsend);
    	btnMakeNetworkSet = (Button)findViewById(R.id.btn_make_network);
    	btnMakeNetworkEnd = (Button)findViewById(R.id.btn_make_networkend);
    	btnMakeDelOne = (Button)findViewById(R.id.btn_make_del_one);
    	btnMakeDelAll = (Button)findViewById(R.id.btn_newfile);
    	btnMakeDefault = (Button)findViewById(R.id.btn_default);
    	
        adapter = new ArrayAdapter<TestScriptElement>(this, android.R.layout.simple_list_item_1, listElements);
        scriptView.setAdapter(adapter);

    	btnMakeStart.setOnClickListener(mClickListener);
    	btnMakeEnd.setOnClickListener(mClickListener);
    	btnMakeAccSet.setOnClickListener(mClickListener);
    	btnMakeAccEnd.setOnClickListener(mClickListener);
    	btnMakeGpsSet.setOnClickListener(mClickListener);
    	btnMakeGpsEnd.setOnClickListener(mClickListener);
    	btnMakeNetworkSet.setOnClickListener(mClickListener);
    	btnMakeNetworkEnd.setOnClickListener(mClickListener);
    	btnMakeDelOne.setOnClickListener(mClickListener);
    	btnMakeDelAll.setOnClickListener(mClickListener);
    	btnMakeDefault.setOnClickListener(mClickListener);
    }
	
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }
    
    Button.OnClickListener mClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_make_str :
				listElements.add(new StrElement());
				adapter.notifyDataSetChanged();
				break;
			case R.id.btn_make_end :
				listElements.add(new EndElement());
				adapter.notifyDataSetChanged();
				break;
			case R.id.btn_make_acc :
				Toast.makeText(getApplicationContext(), "To acc element page", 500).show();				
				Intent mlacc = new Intent(getApplicationContext(),MakeAccElementActivity.class);
				mlacc.putExtra("listElements", listElements);
				startActivity(mlacc);
				finish();
				break;
			case R.id.btn_make_accend :
				listElements.add(new AccEndElement());
				adapter.notifyDataSetChanged();
				break;
			case R.id.btn_make_gps :
				Toast.makeText(getApplicationContext(), "To GPS element page", 500).show();				
				Intent mlgps = new Intent(getApplicationContext(),MakeLocGpsElementActivity.class);
				mlgps.putExtra("listElements", listElements);
				startActivity(mlgps);
				finish();
				break;
			case R.id.btn_make_gpsend :
				listElements.add(new GpsEndElement());
				adapter.notifyDataSetChanged();
				break;
			case R.id.btn_make_network :
				Toast.makeText(getApplicationContext(), "To Network element page", 500).show();				
				Intent mlnet = new Intent(getApplicationContext(),MakeLocNetworkElementActivity.class);
				mlnet.putExtra("listElements", listElements);
				startActivity(mlnet);
				finish();
				break;
			case R.id.btn_make_networkend :
				listElements.add(new NetworkEndElement());
				adapter.notifyDataSetChanged();
				break;
			case R.id.btn_make_del_one :
				if(listElements.size()!=0)
					listElements.remove(listElements.size()-1);
				adapter.notifyDataSetChanged();
				break;
			case R.id.btn_newfile :
				listElements.add(new NewElement());
				adapter.notifyDataSetChanged();
				break;
			case R.id.btn_default :
				if((""+editDefaultTime.getText()).matches(".*[0-9].*")) {
					listElements.add(new TestScriptElement(Integer.parseInt("" + editDefaultTime.getText())));
					adapter.notifyDataSetChanged();
				}
				else
					Toast.makeText(getApplicationContext(), "Not enough Parameters", 500).show();
				break;
			}
		}
	};
	
	public void onBackPressed(){
		
		Toast.makeText(getApplicationContext(), "To main page", 500).show();				
		Intent ml = new Intent(getApplicationContext(),MainActivity.class);
		ml.putExtra("listElements", listElements);
		startActivity(ml);
		finish();
		
//		super.onBackPressed(); 
	}
}

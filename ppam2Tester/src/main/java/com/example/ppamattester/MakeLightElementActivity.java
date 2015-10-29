package com.example.ppamattester;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;


public class MakeLightElementActivity extends Activity {
	ArrayList<TestScriptElement> listElements;
	
	Button btnModeNormal;
	Button btnModeFastest;
	Button btnModeGame;
	Button btnModeUI;
	Button btnCreateLight;
	
	EditText editDueTime;
	
	int mode=0;
	int dueTime=0;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        listElements = (ArrayList<TestScriptElement>) getIntent().getSerializableExtra("listElements");
        
        setContentView(R.layout.activity_makelightelement);
        
        editDueTime = (EditText)findViewById(R.id.edit_due_time_light);
        
        btnModeNormal = (Button)findViewById(R.id.btn_light_mode_normal);
    	btnModeFastest = (Button)findViewById(R.id.btn_light_mode_fastest);
    	btnModeGame = (Button)findViewById(R.id.btn_light_mode_game);
    	btnModeUI = (Button)findViewById(R.id.btn_light_mode_ui);
    	btnCreateLight = (Button)findViewById(R.id.btn_create_light);
    	
    	btnModeNormal.setOnClickListener(mClickListener);
    	btnModeFastest.setOnClickListener(mClickListener);
    	btnModeGame.setOnClickListener(mClickListener);
    	btnModeUI.setOnClickListener(mClickListener);
    	btnCreateLight.setOnClickListener(mClickListener);
    	
		btnModeFastest.setEnabled(false);
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
			case R.id.btn_light_mode_normal :
				btnModeNormal.setEnabled(false);
				btnModeFastest.setEnabled(true);
				btnModeGame.setEnabled(true);
				btnModeUI.setEnabled(true);
				mode=3;
				break;
			case R.id.btn_light_mode_fastest :
				btnModeNormal.setEnabled(true);
				btnModeFastest.setEnabled(false);
				btnModeGame.setEnabled(true);
				btnModeUI.setEnabled(true);
				mode=0;
				break;
			case R.id.btn_light_mode_game :
				btnModeNormal.setEnabled(true);
				btnModeFastest.setEnabled(true);
				btnModeGame.setEnabled(false);
				btnModeUI.setEnabled(true);
				mode=1;
				break;
			case R.id.btn_light_mode_ui :
				btnModeNormal.setEnabled(true);
				btnModeFastest.setEnabled(true);
				btnModeGame.setEnabled(true);
				btnModeUI.setEnabled(false);
				mode=2;
				break;
			case R.id.btn_create_light :
				if((""+editDueTime.getText()).matches(".*[0-9].*")){
					dueTime = Integer.parseInt( "" + editDueTime.getText() );
					listElements.add(new LightElement(dueTime,mode));
					
					Toast.makeText(getApplicationContext(), "To make page", Toast.LENGTH_SHORT).show();
				}			
				else
					Toast.makeText(getApplicationContext(), "Not enough Parameters\nTo make page", Toast.LENGTH_SHORT).show();
				
				Intent ml = new Intent(getApplicationContext(),MakeScriptActivity.class);
				ml.putExtra("listElements", listElements);
				startActivity(ml);
				finish();
				break;
			}
		}
	};
	public void onBackPressed(){

		Toast.makeText(getApplicationContext(), "To main page", 500).show();
		Intent ml = new Intent(getApplicationContext(),MakeScriptActivity.class);
		ml.putExtra("listElements", listElements);
		startActivity(ml);
		finish();

//		super.onBackPressed();
	}
}

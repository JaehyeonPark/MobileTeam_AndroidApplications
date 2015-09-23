package com.example.ppamattester;

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


public class MakeLocNetworkElementActivity extends Activity {
	ArrayList<TestScriptElement> listElements;
	
	Button btnModeGPS;
	Button btnModeNetwork;
	Button btnCreateLoc;
	
	EditText editDueTime;
	EditText editMinTime;
	EditText editMinDist;
	
	int dueTime=0;
	
	int minTime;
	int minDist;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        listElements = (ArrayList<TestScriptElement>) getIntent().getSerializableExtra("listElements");
        
        setContentView(R.layout.activity_makelocnetworkelement);
        
        editDueTime = (EditText)findViewById(R.id.edit_due_time_network);
        editMinTime =  (EditText)findViewById(R.id.edit_min_time_network);
        editMinDist =  (EditText)findViewById(R.id.edit_min_dist_network);
        
    	btnCreateLoc = (Button)findViewById(R.id.btn_create_network);
    	
    	btnCreateLoc.setOnClickListener(mClickListener);
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
			case R.id.btn_create_network :
				if((""+editDueTime.getText()).matches(".*[0-9].*")&&(""+editMinTime.getText()).matches(".*[0-9].*")&&(""+editMinDist.getText()).matches(".*[0-9].*")){

					dueTime = Integer.parseInt( "" + editDueTime.getText() );
					minTime = Integer.parseInt( "" + editMinTime.getText() );
					minDist = Integer.parseInt( "" + editMinDist.getText() );
					
					listElements.add(new NetworkElement(dueTime,minTime,minDist));
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
}

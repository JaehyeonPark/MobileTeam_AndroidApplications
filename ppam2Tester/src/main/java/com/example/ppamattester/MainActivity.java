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
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity {
    
	private static final boolean D = true;
    
	ArrayList<TestScriptElement> listElements;
	private ListView scriptView;
	private Button btnMakeScript;
	private Button btnRunScript;
    
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        scriptView = (ListView)findViewById(R.id.script_viewer);
        btnMakeScript = (Button)findViewById(R.id.btn_make_script);
        btnRunScript = (Button)findViewById(R.id.btn_run_script);
        
        listElements = (ArrayList<TestScriptElement>) getIntent().getSerializableExtra("listElements");
        if(listElements==null){
    		listElements = new ArrayList<TestScriptElement>();
        }
        
        ArrayAdapter<TestScriptElement> adapter;
        adapter = new ArrayAdapter<TestScriptElement>(this, android.R.layout.simple_list_item_1, listElements);
        scriptView.setAdapter(adapter);

        btnMakeScript.setOnClickListener(mClickListener);
        btnRunScript.setOnClickListener(mClickListener);
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
			case R.id.btn_make_script :
				Toast.makeText(getApplicationContext(), "To make page", Toast.LENGTH_SHORT).show();
				Intent mlms = new Intent(getApplicationContext(),MakeScriptActivity.class);
				mlms.putExtra("listElements", listElements);
				startActivity(mlms);
				finish();				
				break;
			case R.id.btn_run_script :
				Toast.makeText(getApplicationContext(), "To run page", Toast.LENGTH_SHORT).show();
				Intent mlrs = new Intent(getApplicationContext(),RunScriptActivity.class);
				mlrs.putExtra("listElements", listElements);
				startActivity(mlrs);			
				break;
			}
		}
	};
}

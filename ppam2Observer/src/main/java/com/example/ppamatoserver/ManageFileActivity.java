package com.example.ppamatoserver;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ManageFileActivity extends Activity {
	ArrayList<String> listFiles;
	ListView scriptView;
	TextView tester;
	
	String dirPath;
	File file;
	
	String TAG = "ViewFiles";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "setContentView");
        setContentView(R.layout.activity_managehistory);
        // Set up ScriptListView 
        Log.d(TAG, "Set up ScriptListView");
        
        listFiles = new ArrayList<String>();
        
        dirPath = getFilesDir().getAbsolutePath();
        file = new File(dirPath);
        
        if ( file.listFiles().length > 0 )       
        	for ( File f : file.listFiles() ) {
        		String str = f.getName();
        		if(str.contains(".csv"))
        			listFiles.add(str);
        	}
             
        scriptView = (ListView)findViewById(R.id.script_viewer_files);
        
        if(listFiles.size()==0){
			Toast.makeText(getApplicationContext(), "History is empty\nReturn to run page", Toast.LENGTH_SHORT).show();
			finish();
        }

        Log.d(TAG, "Set up adapter");
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listFiles);
        scriptView.setAdapter(adapter);
        
        scriptView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		ListView listView = (ListView) parent;
        		String item = (String) listView.getItemAtPosition(position);
    			
				Intent mlfv = new Intent(getApplicationContext(),FileViewerActivity.class);
				mlfv.putExtra("filename", item);
				startActivity(mlfv);	
        	}
		});
        
        // Set up Button and TextView
        Log.d(TAG, "Set up TextView");
        tester=(TextView)findViewById(R.id.text_test_man);
    }
	
	
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }
    
    
}

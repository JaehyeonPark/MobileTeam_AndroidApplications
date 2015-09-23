package com.example.ppamatoserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class FileViewerActivity extends Activity {
	ArrayList<String> listFileContents;
	ListView scriptView;

	String fileName = "";
	String dirPath;
	String loadPath;
	File file;
	
	String TAG = "FileViewer";
	
	Button btnDelFile;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "setContentView");
        setContentView(R.layout.activity_fileviewer);

        // Set up ScriptListView 
        listFileContents = new ArrayList<String>();
        
        Log.d(TAG, "Set up ScriptListView");
        fileName = getIntent().getStringExtra("filename");
        dirPath = getFilesDir().getAbsolutePath();
        file = new File(dirPath);
        
        loadPath = dirPath+"/"+fileName;
        try {
			FileInputStream fis = new FileInputStream(loadPath);
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));
			
			String content="", temp="";
			while((temp = bufferReader.readLine()) != null ) {
		        listFileContents.add(temp);
			}
		} catch (IOException e) {}

        scriptView = (ListView)findViewById(R.id.script_viewer_filecontent);
        
        if(listFileContents.size()==0){
			Toast.makeText(getApplicationContext(), "History is empty\nReturn to run page", Toast.LENGTH_SHORT).show();
			finish();
        }

        Log.d(TAG, "Set up adapter");
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listFileContents);
        scriptView.setAdapter(adapter);

        btnDelFile = (Button)findViewById(R.id.btn_del_file);
        btnDelFile.setOnClickListener(mClickListener);
    }
	
	Button.OnClickListener mClickListener = new View.OnClickListener() {
			
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_del_file :
				File file = new File(loadPath);
				file.delete();
				finish();				
				break;
			}
		}
	};
	
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

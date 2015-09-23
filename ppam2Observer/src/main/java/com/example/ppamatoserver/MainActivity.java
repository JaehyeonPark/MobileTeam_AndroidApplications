package com.example.ppamatoserver;

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
	private Button btnManageFiles;
	private Button btnTestBT;
    
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        btnManageFiles = (Button)findViewById(R.id.btn_manage_file);
        btnTestBT = (Button)findViewById(R.id.btn_bts_test);

        btnManageFiles.setOnClickListener(mClickListener);
        btnTestBT.setOnClickListener(mClickListener);
    }
    
    Button.OnClickListener mClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_manage_file :			
				Intent mlmf = new Intent(getApplicationContext(),ManageFileActivity.class);
				startActivity(mlmf);			
				break;
			case R.id.btn_bts_test :		
				Intent mlbt = new Intent(getApplicationContext(),PassBTInformActivity.class);
				startActivity(mlbt);
			}
		}
	};
}

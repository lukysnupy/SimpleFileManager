package com.elsnupator.test.simplefilemanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private AbsListView filesView;
    private File currentFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        filesView = findViewById(R.id.files_view);


        currentFolder = getObbDir().getParentFile();
    }

    @Override
    protected void onResume(){
        super.onResume();
        refreshFiles();
    }


    private void refreshFiles(){
        new Runnable() {
            @Override
            public void run() {
                filesView.setAdapter(new FileAdapter(currentFolder,MainActivity.this));
            }
        }.run();
    }



    // Overflow menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.overflow_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_settings:
                // Open Settings activity
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_refresh:
                refreshFiles();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

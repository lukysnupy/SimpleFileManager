package com.elsnupator.test.simplefilemanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private AbsListView filesView;
    private File currentFolder;
    private File defaultFolder;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        filesView = findViewById(R.id.files_view);

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }

        defaultFolder = new File("/");
        currentFolder = new File(defaultFolder.getAbsolutePath());
    }

    @Override
    protected void onResume(){
        super.onResume();
        refreshFiles();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "The storage won't be accesible..", Toast.LENGTH_SHORT).show();
    }


    private void refreshFiles(){
        new Runnable() {
            @Override
            public void run() {
                filesView.setAdapter(new FileAdapter(currentFolder, currentFolder.getAbsolutePath()
                        .equals(defaultFolder.getAbsolutePath()), MainActivity.this));
            }
        }.run();
        Log.i(TAG,"Files refreshed");
    }


    void setCurrentFolder(File folder){
        this.currentFolder = folder;
        Log.i(TAG,"Current folder: " + folder.getAbsolutePath());
        refreshFiles();
    }

    void setDefaultFolder(){
        this.currentFolder = new File(defaultFolder.getAbsolutePath());
        Log.i(TAG,"Current folder: " + defaultFolder.getAbsolutePath());
        refreshFiles();
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
                Toast.makeText(this, "Files refreshed..", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.elsnupator.test.simplefilemanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.webkit.MimeTypeMap;
import android.widget.*;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private AbsListView filesView;
    private FileAdapter filesAdapter;
    private File currentFolder;
    private File defaultFolder;
    private static final String TAG = "MainActivity";

    private Animation fadeOut;
    private AnimationSet animationIn;

    private ActionMode actionMode;

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
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }

        setDefaultPath();
        currentFolder = new File(defaultFolder.getAbsolutePath());

        setActionMode();

        setAnimations();
    }

    @Override
    protected void onResume(){
        super.onResume();
        setDefaultPath();
        refreshFiles();
    }

    @Override
    protected void onPause(){
        super.onPause();
        unsetActionMode();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "The storage won't be accesible..", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                Log.i(TAG,"Directing to Settings activity..");
                startActivity(intent);
                return true;
            case R.id.action_refresh:
                refreshFiles();
                Toast.makeText(this, "Files refreshed..", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    // Activity initialization

    private void setDefaultPath(){
        String internalStoragePath = getObbDir().getParentFile().getParentFile().getParentFile()
                .getAbsolutePath();

        defaultFolder = new File(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(
                getResources().getString(R.string.default_folder_key),internalStoragePath));
    }

    private void setAnimations(){
        fadeOut = new AlphaAnimation(1,0);
        fadeOut.setDuration(50);
        fadeOut.setFillAfter(true);

        Animation scaleIn = new ScaleAnimation(0.95f, 1f, 0.95f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleIn.setStartOffset(50);
        scaleIn.setDuration(150);
        Animation fadeIn = new AlphaAnimation(0,1);
        fadeIn.setStartOffset(50);
        fadeIn.setDuration(100);
        fadeIn.setFillAfter(true);

        animationIn = new AnimationSet(true);
        animationIn.addAnimation(scaleIn);
        animationIn.addAnimation(fadeIn);
    }

    private void setActionMode(){
        filesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(actionMode != null)
                    listItemSelect(position);
                else{
                    File item = (File)filesAdapter.getItem(position);
                    if(item == null)
                        return;
                    if(item.isDirectory())
                        setCurrentFolder(new File(currentFolder.getAbsolutePath() + File.separator +
                                item.getName()));
                    else{
                        Uri uri = Uri.fromFile(item);
                        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri,mime);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Log.i(TAG,"Opening file: " + item.getPath());
                        startActivity(intent);
                    }
                }
            }
        });
        filesView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                listItemSelect(position);
                return true;
            }
        });
    }



    // Files list changing

    void refreshFiles(){
        unsetActionMode();
        new Runnable() {
            @Override
            public void run() {
                filesView.startAnimation(fadeOut);

                filesAdapter = new FileAdapter(currentFolder, currentFolder.getAbsolutePath()
                        .equals(defaultFolder.getAbsolutePath()), MainActivity.this);
                filesView.setAdapter(filesAdapter);

                filesView.startAnimation(animationIn);
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



    // Methods for Action mode (CAB)

    private void listItemSelect(int position){
        filesAdapter.itemSelect(position);
        boolean hasCheckedItems = filesAdapter.getSelectedCount() > 0;
        if(hasCheckedItems && actionMode == null)
            actionMode = startSupportActionMode(new ActionModeCallback(this,filesAdapter,
                    currentFolder.getAbsolutePath()));
        else if(!hasCheckedItems && actionMode != null)
            actionMode.finish();

        if(actionMode != null)
            actionMode.setTitle(filesAdapter.getSelectedCount() + " selected items");
    }

    void unsetActionMode(){
        filesAdapter.removeSelection();
        if(actionMode != null)
            actionMode = null;
    }

    ActionMode getActionMode(){
        return actionMode;
    }
}

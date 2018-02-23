package com.elsnupator.test.simplefilemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ActionModeCallback implements ActionMode.Callback{

    MainActivity activity;
    private FileAdapter filesAdapter;
    private String currentPath;

    private static final String TAG = "ActionModeCallback";

    ActionModeCallback(MainActivity activity, FileAdapter filesAdapter, String currentPath){
        this.activity = activity;
        this.filesAdapter = filesAdapter;
        this.currentPath = currentPath;
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.contextual_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.action_delete){
            final List<File> filesToDelete = listFilesToDelete(filesAdapter.getSelectedItems());
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity,
                    R.style.AlertDialog));
            builder.setMessage("Do you really want to delete " + String.valueOf(filesToDelete.size()) + " files?")
                    .setNegativeButton(android.R.string.no,null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new DeleteAsyncTask(activity,filesToDelete).execute();
                        }
                    }).show();
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        activity.unsetActionMode();
    }


    private List<File> listFilesToDelete(Set<String> selectedFiles){
        List<File> filesToDelete = new ArrayList<>();
        List<File> folders = new ArrayList<>();
        for (String fileName : selectedFiles) {
            File file = new File(currentPath + File.separator + fileName);
            if(file.isDirectory()){
                folders.add(file);
                filesToDelete.add(file);
            }
            else
                filesToDelete.add(file);
        }

        while(folders.size() > 0){
            List<File> subfolders = new ArrayList<>();
            for (File folder : folders) {
                for (File file : folder.listFiles()) {
                    if(file.isDirectory()){
                        subfolders.add(file);
                        filesToDelete.add(file);
                    }
                    else
                        filesToDelete.add(file);
                }
            }
            folders = new ArrayList<>(subfolders);
        }

        return filesToDelete;
    }

    private class DeleteAsyncTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progressDialog;
        private MainActivity activity;
        private List<File> filesToDelete;
        private boolean succesful;

        DeleteAsyncTask(MainActivity activity, List<File> filesToDelete){
            progressDialog = new ProgressDialog(new ContextThemeWrapper(activity, R.style.AlertDialog));
            progressDialog.setMessage("The dataset is being downloaded..");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setCancelable(false);
            this.activity = activity;
            this.filesToDelete = filesToDelete;
            succesful = true;
        }

        @Override
        protected void onPreExecute(){
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids){
            for (int i = filesToDelete.size()-1; i >= 0; i--) {
                if(!filesToDelete.get(i).delete()){
                    Log.w(TAG,"Error during deleting " + filesToDelete.get(i).getPath());
                    succesful = false;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void results){
            progressDialog.dismiss();
            if(!succesful)
                Toast.makeText(activity, "Problem occured, some of selected files might not be deleted..",
                        Toast.LENGTH_SHORT).show();
            activity.unsetActionMode();
            activity.refreshFiles();
        }
    }
}

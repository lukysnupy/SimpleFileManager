package com.elsnupator.test.simplefilemanager;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileAdapter extends BaseAdapter {

    private File currentFolder;
    private List<File> folders = new ArrayList<>();
    private List<File> files = new ArrayList<>();
    private MainActivity activity;

    FileAdapter(File currentFolder, MainActivity mainActivity){
        this.currentFolder = currentFolder;
        this.activity = mainActivity;
        File[] foldersArray = currentFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() && !file.getName().startsWith(".");
            }
        });
        if(foldersArray != null)
            folders = new ArrayList<>(Arrays.asList(foldersArray));
        File[] filesArray = currentFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory() && !file.getName().startsWith(".");
            }
        });
        if(filesArray != null)
            files = new ArrayList<>(Arrays.asList(filesArray));
        Collections.sort(folders);
        Collections.sort(files);
    }

    @Override
    public int getCount() {
        return folders.size() + files.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if(position > folders.size())
            return files.get(position-1-folders.size());
        else if(position > 0)
            return folders.get(position-1);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.file_item, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.row_icon);
        TextView name = convertView.findViewById(R.id.row_name);

        if(position == 0){
            icon.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.icon_up));
            name.setText(activity.getResources().getString(R.string.up));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!currentFolder.getName().equals(""))
                        activity.setCurrentFolder(currentFolder.getParentFile());
                }
            });
        }
        else if(position <= folders.size()){
            icon.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.icon_folder));
            name.setText(folders.get(position-1).getName());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.setCurrentFolder(new File(currentFolder.getAbsolutePath() + File.separator +
                            folders.get(position-1).getName()));
                }
            });
        }
        else{
            icon.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.icon_file));
            name.setText(files.get(position-1-folders.size()).getName());
        }

        return convertView;
    }
}

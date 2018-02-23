package com.elsnupator.test.simplefilemanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private byte offset;

    FileAdapter(File currentFolder, boolean homeFolder, MainActivity mainActivity){
        this.currentFolder = currentFolder;
        if(homeFolder)
            offset = 1;
        else
            offset = 2;
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
        return folders.size() + files.size() + offset;
    }

    @Override
    public Object getItem(int position) {
        if(position > folders.size())
            return files.get(position-offset-folders.size());
        else if(position > 0)
            return folders.get(position-offset);
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

        if(position < offset){
            if(position == 0 && offset == 2){
                icon.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.icon_home));
                name.setText(activity.getResources().getString(R.string.home));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!currentFolder.getName().equals(""))
                            activity.setDefaultFolder();
                    }
                });
            }
            else{
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
        }
        else if(position-offset < folders.size()){
            icon.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.icon_folder));
            name.setText(folders.get(position-offset).getName());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.setCurrentFolder(new File(currentFolder.getAbsolutePath() + File.separator +
                            folders.get(position-offset).getName()));
                }
            });
        }
        else{
            icon.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.icon_file));
            name.setText(files.get(position-offset-folders.size()).getName());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.fromFile(files.get(position-offset-folders.size()));
                    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                            MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri,mime);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    activity.startActivity(intent);
                }
            });
        }

        return convertView;
    }
}

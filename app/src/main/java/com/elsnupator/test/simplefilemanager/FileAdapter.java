package com.elsnupator.test.simplefilemanager;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

public class FileAdapter extends BaseAdapter {

    private File currentFolder;
    private List<File> folders = new ArrayList<>();
    private List<File> files = new ArrayList<>();
    private MainActivity activity;
    private byte offset;
    private boolean homeFolder;

    private Set<String> selectedItems = new HashSet<>();


    FileAdapter(File currentFolder, boolean homeFolder, MainActivity mainActivity){
        this.currentFolder = currentFolder;
        this.homeFolder = homeFolder;
        if(homeFolder && currentFolder.getName().equals(""))
            offset = 0;
        else if(homeFolder || currentFolder.getName().equals(""))
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
        if(position < offset)
            return null;
        else if(position-offset < folders.size())
            return folders.get(position-offset);
        else
            return files.get(position-offset-folders.size());
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
            if(position == 0 && !homeFolder){
                icon.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.icon_home));
                name.setText(activity.getResources().getString(R.string.home));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(activity.getActionMode() == null)
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
                        if(activity.getActionMode() == null)
                            if(!currentFolder.getName().equals(""))
                                activity.setCurrentFolder(currentFolder.getParentFile());
                    }
                });
            }
        }
        else{
            File file;
            if(position-offset < folders.size()){
                icon.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.icon_folder));
                file = folders.get(position-offset);
            }
            else{
                icon.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.icon_file));
                file = files.get(position-offset-folders.size());
            }
            name.setText(file.getName());

            if(selectedItems.contains(file.getName())){
                convertView.setBackgroundColor(ContextCompat.getColor(activity,R.color.colorPrimary));
                name.setTextColor(ContextCompat.getColor(activity,R.color.white));
            }
            else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
                name.setTextColor(ContextCompat.getColor(activity,R.color.text));
            }
        }

        return convertView;
    }



    // Selections

    /**
     * Called when item is selected in Action mode
     * @param position clicked file position in list
     */
    void itemSelect(int position){
        boolean isSelected = false;
        for (String fileName : selectedItems) {
            if(fileName.equals(((File)getItem(position)).getName())){
                isSelected = true;
                break;
            }
        }
        if(!isSelected)
            selectedItems.add(((File)getItem(position)).getName());
        else
            selectedItems.remove(((File)getItem(position)).getName());

        notifyDataSetChanged();
    }

    /**
     * Removes all selected files (-> when setting Action mode off)
     */
    void removeSelection(){
        selectedItems = new HashSet<>();
        notifyDataSetChanged();
    }

    /**
     * Returns files selected in Action mode
     * @return selected files set
     */
    Set<String> getSelectedItems(){
        return selectedItems;
    }

    /**
     * Returns selected files count
     * @return selected files count
     */
    int getSelectedCount(){
        return selectedItems.size();
    }
}

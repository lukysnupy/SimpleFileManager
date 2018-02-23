package com.elsnupator.test.simplefilemanager;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileFilter;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        File[] sdCards = new File("/storage").listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().matches("[0-9A-Z]{4}-[0-9A-Z]{4}");
            }
        });

        String internalStoragePath = getActivity().getObbDir().getParentFile().getParentFile().getParentFile()
                .getAbsolutePath();


        int foldersCount = 2 + sdCards.length;

        CharSequence[] entries = new CharSequence[foldersCount];
        CharSequence[] entryValues = new CharSequence[foldersCount];

        for (int i = 0; i < foldersCount; i++) {
            switch (i){
                case 0:
                    entries[i] = "Root";
                    entryValues[i] = "/";
                    break;
                case 1:
                    entries[i] = "Internal storage";
                    entryValues[i] = internalStoragePath;
                    break;
                case 2:
                    entries[i] = "SD card";
                    entryValues[i] = sdCards[0].getAbsolutePath();
                    break;
                default:
                    entries[i] = "SD card " + (i - 1);
                    entryValues[i] = sdCards[i-2].getAbsolutePath();
                    break;
            }
        }

        ListPreference listPref = (ListPreference)findPreference(getResources().getString(R.string.default_folder_key));
        listPref.setEntries(entries);
        listPref.setEntryValues(entryValues);
        listPref.setDefaultValue(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                getResources().getString(R.string.default_folder_key),internalStoragePath));
    }
}

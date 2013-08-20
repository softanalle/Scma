package com.softanalle.scma;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.softanalle.scma.MainActivity;
import com.softanalle.scma.R;

/**
 * 
 */

/**
 * @author t2r
 * @license GNU GPL 3.0
 * 
    Preference activity for SCMA
 
    Copyright (C) 2013  Tommi Rintala <t2r@iki.fi>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class AppPreferenceActivity extends PreferenceActivity 
implements OnSharedPreferenceChangeListener
{
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {        
		setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		super.onCreate(savedInstanceState);        
		
		addPreferencesFromResource(R.xml.preferences);  
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initSummary(getPreferenceScreen().getPreference(i));
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//menu.add(Menu.NONE, R.menu.menu, 0, "Settings");
		menu.add(Menu.NONE, 0, 0, "Show current settings");
		return super.onCreateOptionsMenu(menu);
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			// startActivity(new Intent(this, MainActivity.class));
			return true;
			default:
				Toast.makeText(getApplicationContext(), "onOptionsItemSelected: " + item.getItemId(), Toast.LENGTH_LONG).show();
		
		}
		return false;
	}


	//@SuppressWarnings("deprecation")
	@SuppressWarnings("deprecation")
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		/*
        if (key.equals(MainActivity.KEY_PREF_FOCUSCOLOR)) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, "Selected color"));
        }
        */
        updatePrefSummary(findPreference(key));
    }

	@SuppressWarnings("deprecation")
	@Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
    
    private void initSummary(Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory pCat = (PreferenceCategory) p;
            for (int i = 0; i < pCat.getPreferenceCount(); i++) {
                initSummary(pCat.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary("Currently: " + listPref.getEntry());
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary("Current: " + editTextPref.getText());
        }
    }
}

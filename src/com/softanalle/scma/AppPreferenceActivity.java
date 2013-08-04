package com.softanalle.scma;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.softanalle.scma.MainActivity;
import com.softanalle.scma.R;

/**
 * 
 */

/**
 * @author t2r
 *
 */
public class AppPreferenceActivity extends PreferenceActivity 
implements OnSharedPreferenceChangeListener
{
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {        
		setTheme(android.R.style.Theme_Black);
		super.onCreate(savedInstanceState);        
		
		addPreferencesFromResource(R.xml.preferences);        
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
		
		}
		return false;
	}


	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(MainActivity.KEY_PREF_FOCUSCOLOR)) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }


}

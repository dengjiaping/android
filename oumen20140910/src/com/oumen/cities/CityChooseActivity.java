package com.oumen.cities;

import com.oumen.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class CityChooseActivity extends FragmentActivity {
	
	private CityFragment fragCities;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_container);
		if (fragCities == null) {
			fragCities = new CityFragment();
		}
		
		getSupportFragmentManager().beginTransaction().replace(R.id.circle_container, fragCities).commit();
	}
}

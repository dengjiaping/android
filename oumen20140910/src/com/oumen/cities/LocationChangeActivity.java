package com.oumen.cities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.base.Cache;

public class LocationChangeActivity extends BaseActivity {
	private TitleBar titleBar;
	private Button btnLeft;

	private RadioGroup chengshi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_location);
		init();
		initData();
	}

	private void init() {
		titleBar = (TitleBar) findViewById(R.id.titlebar);
		titleBar.getTitle().setText("城市切换");
		titleBar.getRightButton().setVisibility(View.GONE);

		btnLeft = titleBar.getLeftButton();
		btnLeft.setOnClickListener(clickListener);
		chengshi = (RadioGroup) findViewById(R.id.radiogroup);
		chengshi.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.beijing) {
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_LATITUDE, App.latitude);
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_LONGITUDE, App.longitude);
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_CITY_NAME, "beijing");
				}
				else if (checkedId == R.id.shanghai) {
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_LATITUDE, 31.230393f);
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_LONGITUDE, 121.473704f);
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_CITY_NAME, "shanghai");
				}
				else if (checkedId == R.id.guangzhou) {
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_LATITUDE, 23.117055f);
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_LONGITUDE, 113.275995f);
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_CITY_NAME, "guangzhou");
				}
				else if (checkedId == R.id.shenzhen) {
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_LATITUDE, 22.616670f);
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_LONGITUDE, 114.066670f);
					App.CACHE.save(Cache.CACHE_USER_CHOOSE_CITY_NAME, "shenzhen");
				}
			}
		});
	}
	
	private final OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				setResult(Activity.RESULT_OK);
				finish();
			}
		}
	};

	private void initData() {
		String temp = App.CACHE.read(Cache.CACHE_USER_CHOOSE_CITY_NAME);
		if (temp.equals("beijing")) {
			chengshi.check(R.id.beijing);
		}
		else if (temp.equals("shanghai")) {
			chengshi.check(R.id.shanghai);
		}
		else if (temp.equals("guangzhou")) {
			chengshi.check(R.id.guangzhou);
		}
		else if (temp.equals("shenzhen")) {
			chengshi.check(R.id.shenzhen);
		}
	}
}

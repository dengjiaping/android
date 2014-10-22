package com.oumen.usercenter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.cities.City;
import com.oumen.tools.ELog;

public class CityPickerActiviry extends BaseActivity {
	private TitleBar titleBar;
	private Button btnLeft, btnRight;
	private TextView tvTitle;
	private ListView listview;

	private ProvinceAdapter adapter = new ProvinceAdapter();
	private boolean isOnProvinces = true;
	private String citystr = null;
	public static final String CITY_CHOOSE_TAG = "citychoose";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pick_city);
		
		init();
		SetProvincesData();
		
		btnLeft.setOnClickListener(clickListener);
		btnRight.setOnClickListener(clickListener);
		listview.setOnItemClickListener(itemClickListener);
	}
	
	private void init() {
		titleBar = (TitleBar) findViewById(R.id.titlebar);
		btnLeft = titleBar.getLeftButton();
		btnRight = titleBar.getRightButton();
		btnRight.setText(R.string.ok);
		tvTitle = titleBar.getTitle();
		tvTitle.setText("省份选择");
		
		listview = (ListView) findViewById(R.id.list);
		listview.setDivider(new ColorDrawable(getResources().getColor(R.color.default_grey_text_bg)));
		listview.setDividerHeight(1);
		listview.setAdapter(adapter);
	}
	
	private void SetProvincesData() {
		adapter.data.clear();
		Iterator<Map.Entry<String, List<City>>> it = App.CITIES.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<City>> entry = (Entry<String, List<City>>) it.next();
			adapter.data.add(entry.getKey()); //返回与此项对应的键
		}
		adapter.notifyDataSetChanged();
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				if (isOnProvinces) {
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
				else {
					tvTitle.setText("省份选择");
					SetProvincesData();
					ProvinceAdapter.selectedId = -1;
					isOnProvinces = true;
				}
			}
			else if (v == btnRight) {
				Intent i = new Intent();
				i.putExtra(CITY_CHOOSE_TAG, citystr);
				setResult(Activity.RESULT_OK, i);
				isOnProvinces = true;
				finish();
			}
		}
	};

	private final OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (isOnProvinces) {
				tvTitle.setText("城市选择");
				ProvinceAdapter.selectedId = -1;
				ELog.i("得到的城市信息：" + view.getTag());
				adapter.data.clear();
				List<City> tempList = App.CITIES.get(view.getTag());
				
				for (int i = 0;i<tempList.size();i++) {
					adapter.data.add(tempList.get(i).getName());
				}
				
				adapter.notifyDataSetChanged();
				isOnProvinces = false;
			}
			else {
				ProvinceAdapter.selectedId = position;
				btnRight.setVisibility(View.VISIBLE);
				citystr = adapter.data.get(position);
				adapter.notifyDataSetChanged();
			}

		}
	};
}

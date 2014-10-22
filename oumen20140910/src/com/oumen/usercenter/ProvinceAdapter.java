package com.oumen.usercenter;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.oumen.R;

public class ProvinceAdapter extends BaseAdapter {
	public final List<String> data = new ArrayList<String>();
	//绘制的时候判断是否是选中项，是绘制选中的状态，否则绘制非选中的状态
	public static int selectedId = -1;

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView item = null;
		
		if (convertView == null) {
			item = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.choosecities_list_item, null);
		}
		else {
			item = (TextView) convertView;
		}
		
		item.setText(data.get(position));
		item.setTag(data.get(position));
		
		if (selectedId == position) {
			item.setBackgroundResource(R.color.nav);
		}
		else {
			item.setBackgroundResource(R.color.white);
		}
		
		return item;
	}
}

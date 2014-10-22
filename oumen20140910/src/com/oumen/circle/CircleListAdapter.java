package com.oumen.circle;

import android.view.View;
import android.view.ViewGroup;

import com.oumen.android.peers.entity.CircleUserMsg;
import com.oumen.widget.list.HSZListViewAdapter;

public class CircleListAdapter extends HSZListViewAdapter<CircleUserMsg> implements View.OnClickListener {

	@Override
	synchronized public View getView(int position, View convertView, ViewGroup parent) {
		synchronized (this) {
			CircleItem item = null;
			if (convertView == null) {
				CircleItemData itemData = new CircleItemData();
				item = new CircleItem(parent.getContext());
				item.setTag(itemData);
				item.setButtonClickListener(this);
				itemData.groupIndex = position;
				itemData.groupData = get(position);
			}
			else {
				item = (CircleItem) convertView;
				CircleItemData itemData = (CircleItemData) item.getTag();
				itemData.groupIndex = position;
				itemData.groupData = get(position);
			}
			item.update();
			return item;
		}
	}

	@Override
	public void onClick(View v) {}
}

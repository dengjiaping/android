package com.oumen.message;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MessageListAdapter extends BaseAdapter {
	protected final ArrayList<MessageListItemDataProvider> data = new ArrayList<MessageListItemDataProvider>();
	
	protected View.OnClickListener clickListener;
	protected View.OnLongClickListener longClickListener;

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
		MessageListItemDataProvider provider = data.get(position);
		MessageListItem item = null;
		if (convertView == null) {
			item = new MessageListItem(parent.getContext());
			item.setOnClickListener(clickListener);
//			item.setIconClickListener(clickListener);
			item.setButtonClickListener(clickListener);
			item.setOnLongClickListener(longClickListener);
		}
		else {
			item = (MessageListItem) convertView;
		}
		item.update(provider);
		return item;
	}

}

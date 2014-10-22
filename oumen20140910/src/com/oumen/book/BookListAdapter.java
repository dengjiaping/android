package com.oumen.book;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class BookListAdapter extends BaseAdapter {
	protected final List<BookMessage> data = new ArrayList<BookMessage>();
	protected View.OnClickListener clickListener;

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
		BookMessage msg = data.get(position);
		
		BookListItem item = null;
		if (convertView == null) {
			item = new BookListItem(parent.getContext());
			item.setOnClickListener(clickListener);
		}
		else {
			item = (BookListItem) convertView;
		}
		item.update(msg);
		
		if (position == 0) {
			item.setTimeVisibility(View.VISIBLE);
		}
		else {
			Calendar lastTime = Calendar.getInstance(), currentTime = Calendar.getInstance();
			lastTime.setTime(data.get(position - 1).createAt);
			currentTime.setTime(msg.createAt);
			int y1 = lastTime.get(Calendar.YEAR), m1 = lastTime.get(Calendar.MONTH), d1 = lastTime.get(Calendar.DAY_OF_MONTH),
				y2 = currentTime.get(Calendar.YEAR), m2 = currentTime.get(Calendar.MONTH), d2 = currentTime.get(Calendar.DAY_OF_MONTH);
			if (y1 != y2 || m1 != m2 || d1 != d2) {
				item.setTimeVisibility(View.VISIBLE);
			}
			else {
				item.setTimeVisibility(View.GONE);
			}
		}
		return item;
	}

}

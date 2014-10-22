package com.oumen.message;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class BiaoQingAdapter extends BaseAdapter {
	public final int PAGE_TOTAL = 8;
	
	public final ArrayList<BiaoQing> data = new ArrayList<BiaoQing>();
	
	protected int page;

	protected int cellSize;

	BiaoQingAdapter(int cellSize) {
		this.cellSize = cellSize;
	}

	public int getCount() {
		return PAGE_TOTAL;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView image;
		if (convertView == null) {
			image = new ImageView(parent.getContext());
			image.setLayoutParams(new AbsListView.LayoutParams(cellSize, cellSize));
			Bitmap img = null;
			if (data.size() > position + page * PAGE_TOTAL) {
				img = data.get(position + page * PAGE_TOTAL).getBitmap();

				image.setImageBitmap(img);
			}
			else {
				img = data.get(0).getBitmap();
				image.setImageBitmap(img);
				image.setVisibility(View.GONE);
			}
		}
		else {
			image = (ImageView) convertView;
		}
		return image;
	}
}

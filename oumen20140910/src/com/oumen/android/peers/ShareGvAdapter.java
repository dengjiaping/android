package com.oumen.android.peers;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.oumen.R;
import com.oumen.android.App;

class ShareGvAdapter extends BaseAdapter {
	final ArrayList<Bitmap> data = new ArrayList<Bitmap>();

	@Override
	public int getCount() {
			return data.size();
	}

	@Override
	public Bitmap getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imgView;
		if (convertView == null) {
			imgView = new ImageView(parent.getContext());
			imgView.setImageResource(R.drawable.photos);
//			imgView.setScaleType(ScaleType.FIT_XY);
			imgView.setLayoutParams(new AbsListView.LayoutParams(App.DEFAULT_CELL_SIZE, App.DEFAULT_CELL_SIZE));
		}
		else {
			imgView = (ImageView)convertView;
		}
		imgView.setImageBitmap(data.get(position));
		return imgView;
	}
}

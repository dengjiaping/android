package com.oumen.biaoqing;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.message.Type;
/**
 * 表情适配器
 */
public class GridImageAdapter extends BaseAdapter {
	public final ArrayList<BiaoQing> data = new ArrayList<BiaoQing>();
	public int size;
	public Context context;
	private OnClickListener clickListener;
	
	public GridImageAdapter(Context context, OnClickListener clickListener) {
		this.context = context;
		this.clickListener = clickListener;
	}
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
		ImageView item;
		BiaoQing temp = data.get(position);
		
		if (convertView == null) {
			item = new ImageView(parent.getContext());
			if (Type.TEXT.equals(temp.getType()) || Type.OTHER.equals(temp.getType())){
				size = parent.getContext().getResources().getDimensionPixelSize(R.dimen.default_tobig_gap);
			}
			else {
				size = parent.getContext().getResources().getDimensionPixelSize(R.dimen.big_photo_size);
			}
			item.setLayoutParams(new AbsListView.LayoutParams(size, size));
			item.setOnClickListener(clickListener);
		}
		else {
			item = (ImageView) convertView;
		}
		
		if (Type.TEXT.equals(temp.getType()) || Type.OTHER.equals(temp.getType())){
			item.setImageBitmap(temp.getBitmap());
		}
		else {
			ImageLoader.getInstance().displayImage(App.SCHEMA_FILE + temp.getImagePath(), item);
		}
		item.setTag(data.get(position));
		return item;
	}
}

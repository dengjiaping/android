package com.oumen.chat;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ChatBiaoQingAdapter extends BaseAdapter {

	Context mContext;
	private int mPageIndex;
	public static final ArrayList<ExpressionBean> mData = new ArrayList<ExpressionBean>();

	public int size;
	ChatBiaoQingAdapter(Context context, int pageIndex) {
		mContext = context;
		mPageIndex = pageIndex;
	}

	public int getCount() {
		return 8;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		size = parent.getResources().getDisplayMetrics().widthPixels / 4;

//		AppItem appItem;
		ImageView image;
		if (convertView == null) {
			image= new ImageView(parent.getContext());
			image.setLayoutParams(new AbsListView.LayoutParams(size, size));
//			convertView = LayoutInflater.from(mContext).inflate(R.layout.singleexpress, null);
//			appItem = new AppItem();
//			appItem.mAppIcon = (ImageView) convertView.findViewById(R.id.ivAppIcon);
			
			Bitmap bm1 = null;
			if (mData.size() > position + mPageIndex * 8) {
				
				bm1 = mData.get(position + mPageIndex * 8).getBitmap();
				
				image.setImageBitmap(bm1);
			}
			else {
				bm1 = mData.get(0).getBitmap();
				image.setImageBitmap(bm1);
				image.setVisibility(View.GONE);
			}
		}
		else {
			image = (ImageView) convertView;
		}
		return image;
	}

//	class AppItem {
//		ImageView mAppIcon;
//	}
}

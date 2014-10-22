package com.oumen.activity.detail;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.oumen.R;
import com.oumen.activity.detail.cell.CircleCornerImageHasDownloadView;
import com.oumen.widget.file.ImageData;
import com.oumen.widget.image.ImagePreviewActivity;
import com.oumen.widget.preview.image.ImagePagerFragment;

/**
 * viewPager适配器
 * 
 */
public class HeaderAdapter extends PagerAdapter {
	public final ArrayList<ImageData> datas = new ArrayList<ImageData>();
	private boolean hasClick = true;

	private OnClickListener outerlistener;
	private int padding = 0;

	public void setImageViewHasClickListener(boolean has, OnClickListener listener) {
		hasClick = has;
		this.outerlistener = listener;
	}
	
	public void setPadding(int padding) {
		this.padding = padding;
	}
	
	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		CircleCornerImageHasDownloadView item = new CircleCornerImageHasDownloadView(container.getContext());
		item.setRadius(container.getResources().getDimensionPixelSize(R.dimen.radius_large));
		item.setViewPadding(padding);
		
		if (TextUtils.isEmpty(datas.get(position).path)) {
			item.getImageView().setImageResource(R.drawable.pic_default);
		}
		else {
			item.update(datas.get(position).path);
		}
		
		if (hasClick) {
			item.setOnClickListener(clickListener);
		}
		else {
			item.setOnClickListener(outerlistener);
		}
		
		item.setTag(datas.get(position).path);
		((ViewPager) container).addView(item);
		return item;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String path = (String) v.getTag();
			for (int i = 0; i < datas.size(); i++) {
				if (datas.get(i).path.equals(path)) {

					Bundle params = new Bundle();
					params.putInt(ImagePagerFragment.PARAMS_KEY_START_INDEX, i);
					params.putSerializable(ImagePagerFragment.PARAMS_KEY_DATA, datas);

					Intent intent = new Intent(v.getContext(), ImagePreviewActivity.class);
					intent.putExtra(ImagePreviewActivity.INTENT_KEY_DATA, params);
					v.getContext().startActivity(intent);
					return;
				}
			}
		}
	};
}

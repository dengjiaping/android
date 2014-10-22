package com.oumen.activity.detail;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.message.ActivityMember;

public class ApplyerGridAdapter extends BaseAdapter {
	final ArrayList<ActivityMember> data = new ArrayList<ActivityMember>(4);

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
		Item item = convertView == null ? new Item(parent.getContext()) : (Item) convertView;
		item.update(data.get(position));
		return item;
	}

	private class Item extends LinearLayout {
		ImageView mPhoto;
		TextView mNickname;

		Item(Context context) {
			super(context);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.amuse_applyer_item, this, true);
			mPhoto = (ImageView) findViewById(R.id.iv_gvamuseappler_img);
			mNickname = (TextView) findViewById(R.id.tv_gvamuseappler_name);
			mNickname.setVisibility(View.GONE);
		}

		void update(ActivityMember applyer) {
			mNickname.setText(applyer.getNickname());
			if (applyer.getPhotoUrl() != null) {
				ImageLoader.getInstance().displayImage(applyer.getPhotoUrl(), mPhoto, App.CIRCLE_IMAGE_LOADING_LISTENER);
			}
			else {
				mPhoto.setImageResource(R.drawable.round_user_photo);
			}
		}
	}
}

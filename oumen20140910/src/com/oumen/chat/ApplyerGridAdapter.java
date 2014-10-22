package com.oumen.chat;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.message.ActionType;
import com.oumen.message.ActivityMember;
import com.oumen.message.ChatMessage;
import com.oumen.message.SendType;
import com.oumen.message.Type;
import com.oumen.tools.ELog;
import com.oumen.user.UserInfoActivity;
import com.oumen.widget.image.shape.RoundRectangleImageView;

/**
 * 活动列表，参与用户item
 *
 */
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
		RoundRectangleImageView mPhoto;
		TextView mNickname;
		private Context context;
		private ActivityMember applyer;

		Item(Context context) {
			super(context);
			this.context = context;
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.huodong_applyer_item, this, true);
			mPhoto = (RoundRectangleImageView) findViewById(R.id.applyer_photo);
			mPhoto.setRadius(10);
			mPhoto.setOnClickListener(clickListener);
			mNickname = (TextView) findViewById(R.id.applyer_name);
		}

		void update(ActivityMember applyer) {
			this.applyer = applyer;
			mNickname.setText(applyer.getNickname());
			if (applyer.getPhotoUrl() != null) {
				ImageLoader.getInstance().displayImage(applyer.getScalePhotoUrl(2 * getResources().getDimensionPixelSize(R.dimen.medium_photo_size)), mPhoto);
			}
			else {
				mPhoto.setImageResource(R.drawable.rectangle_photo);
			}
		}

		private final OnClickListener clickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == mPhoto) {
					//TODO 有可能会变，此处不要删除（跳转到个人主页）
					Intent intent = new Intent(context, UserInfoActivity.class);
					ELog.i(String.valueOf(applyer.getUid()));
					intent.putExtra(UserInfoActivity.INTENT_KEY_UID, applyer.getUid());
					context.startActivity(intent);
					// TODO 跳转到聊天界面
//					if (applyer.getUid() == App.USER.getUid()) {// 如果为本人
//						return;
//					}
//					ChatMessage msg = new ChatMessage();
//					msg.setTargetId(applyer.getUid());
//					msg.setTargetNickname(applyer.getNickname());
//					msg.setTargetPhotoUrl(applyer.getPhotoUrl());
//					msg.setActionType(ActionType.CHAT);
//					msg.setType(Type.OTHER);
//					msg.setSendType(SendType.READ);
//
//					Intent intent = new Intent(context, ChatActivity.class);
//					intent.putExtra(ChatActivity.REQUEST_MESSAGE, msg);
//					context.startActivity(intent);
				}

			}
		};

	}
}

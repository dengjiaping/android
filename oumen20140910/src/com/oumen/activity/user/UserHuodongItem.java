package com.oumen.activity.user;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.activity.message.UserActivityMessage;
import com.oumen.android.App;
import com.oumen.chat.ChatActivity;
import com.oumen.message.ActionType;
import com.oumen.message.ActivityMessage;
import com.oumen.message.MultiChatMessage;
import com.oumen.message.SendType;
import com.oumen.message.Type;
import com.oumen.tools.ELog;
import com.oumen.widget.image.shape.RoundRectangleImageView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UserHuodongItem extends RelativeLayout {
	private RoundRectangleImageView photo;
	private TextView name, address, time, chat;
	
	private ActivityMessage activityMsg = null;
	
	private Context context;

	public UserHuodongItem(Context context) {
		this(context, null, 0);
	}

	public UserHuodongItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public UserHuodongItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.user_huodong_item, this, true);
		
		this.context = context;

		photo = (RoundRectangleImageView) findViewById(R.id.photo);
		photo.setRadius(10);
		name = (TextView) findViewById(R.id.name);
		address = (TextView) findViewById(R.id.address);
		time = (TextView) findViewById(R.id.time);
		chat = (TextView) findViewById(R.id.chat);
		chat.setVisibility(View.GONE);
		chat.setOnClickListener(clickListener);
	}

	public void update(UserActivityMessage bean) {
		File cache = ImageLoader.getInstance().getDiskCache().get(bean.getPicUrl(getResources().getDimensionPixelSize(R.dimen.big_photo_size)));
		if (cache != null && cache.exists()) {
			Bitmap pic = BitmapFactory.decodeFile(cache.getAbsolutePath());
			photo.setImageBitmap(pic);
		}
		else {
			ImageLoader.getInstance().displayImage(bean.getPicUrl(getResources().getDimensionPixelSize(R.dimen.big_photo_size)), photo, App.OPTIONS_PIC);
		}
		name.setText(bean.getName());
		address.setText(bean.getAddress());
		
		activityMsg = new ActivityMessage(bean); 

		try {
			Calendar startTime = Calendar.getInstance();
			Calendar endTime = Calendar.getInstance();
			startTime.setTime(App.YYYY_MM_DD_HH_MM_FORMAT.parse(bean.getStartTime()));
			endTime.setTime(App.YYYY_MM_DD_HH_MM_FORMAT.parse(bean.getEndTime()));
			long hours = (endTime.getTimeInMillis() - startTime.getTimeInMillis()) / (1000 * 60 * 60);
			if (hours >= 0) {
				String tempStr = hours / 24 + "天" + hours % 24 + "小时结束";
				SpannableStringBuilder builder = new SpannableStringBuilder(tempStr);

				ForegroundColorSpan tipColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.detail_orange));
				AbsoluteSizeSpan tipSizeSpen = new AbsoluteSizeSpan(16 * 2);
				builder.setSpan(tipColorSpan, 0, String.valueOf(hours / 24).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				builder.setSpan(tipSizeSpen, 0, String.valueOf(hours / 24).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				tipColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.detail_orange));
				tipSizeSpen = new AbsoluteSizeSpan(16 * 2);
				builder.setSpan(tipColorSpan, (hours / 24 + "天").length(), (hours / 24 + "天" + hours % 24).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				builder.setSpan(tipSizeSpen, (hours / 24 + "天").length(), (hours / 24 + "天" + hours % 24).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				tipColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.detail_orange));
				builder.setSpan(tipColorSpan, tempStr.length() - "结束".length(), tempStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				time.setText(builder);
			}
			else {
				time.setText("活动已结束");
				time.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_large));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == chat) {// 进入群聊
				ELog.i("进入群聊");
				int selfUid = App.PREFS.getUid();
				ActivityMessage.insert(selfUid, activityMsg, App.DB);

				boolean hasCreatedMessage = MultiChatMessage.hasActivityCreateMessage(selfUid, activityMsg.getMultiId(), App.DB);
				ELog.i("Has created message:" + hasCreatedMessage);
				MultiChatMessage multiMsg = new MultiChatMessage();

				multiMsg.setSelfName(App.USER.getNickname());
				multiMsg.setSelfPhotoUrl(App.USER.getPhotoSourceUrl());
				multiMsg.setActionType(ActionType.MULTI_CREATE);
				multiMsg.setType(Type.TEXT);
				multiMsg.setActivityId(activityMsg.getId());
				multiMsg.setMultiId(activityMsg.getMultiId());
//					multiMsg.setRead(true);
				multiMsg.setSendType(SendType.READ);
				multiMsg.setDatetime(new Date(App.getServerTime()));
				if (activityMsg.getOwnerId() == selfUid) {
					multiMsg.setActionType(ActionType.MULTI_CREATE);
					multiMsg.setContent(MultiChatMessage.getCreateMultiChatInfo(context));
				}
				else {
					multiMsg.setActionType(ActionType.MULTI_JOIN);
					multiMsg.setTargetId(App.USER.getUid());
					multiMsg.setContent(MultiChatMessage.getJoinMultiChatInfo(context, activityMsg.getTitle()));
				}

				if (!hasCreatedMessage) {
					MultiChatMessage.insert(selfUid, multiMsg, App.DB);
				}
				multiMsg.setActivityMessage(activityMsg);
				//更新底部消息提醒

				Intent intent = new Intent(context, ChatActivity.class);
				intent.putExtra(ChatActivity.REQUEST_MESSAGE, multiMsg);

				context.startActivity(intent);
			}
		}
	};

}

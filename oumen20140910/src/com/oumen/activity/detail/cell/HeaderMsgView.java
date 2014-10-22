package com.oumen.activity.detail.cell;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.activity.detail.HuodongDetailHeaderProvider;
import com.oumen.android.App;
import com.oumen.chat.ChatActivity;
import com.oumen.home.LoginConfrim;
import com.oumen.message.ActionType;
import com.oumen.message.ChatMessage;
import com.oumen.message.SendType;
import com.oumen.message.Type;

/**
 * 活动详情头部
 * 
 */
@SuppressWarnings("deprecation")
public class HeaderMsgView extends RelativeLayout {
	private ImageView photo;
	private TextView huodongTitle, address, sender, huodongTime, lookNum;
	private ImageView label;
	private RelativeLayout root;

	private Context context;

	private HuodongDetailHeaderProvider provider = null;
	
	private Resources res ;
	
	private LoginConfrim loginConfirm;

	public HeaderMsgView(Context context) {
		this(context, null, 0);
	}

	public HeaderMsgView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HeaderMsgView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.context = context;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.huodong_detail_header_msg, this, true);
		
		loginConfirm = new LoginConfrim(context);

		res = context.getResources();
		
		root = (RelativeLayout) findViewById(R.id.relativelayout_root);
		photo = (ImageView) findViewById(R.id.photo);
		huodongTitle = (TextView) findViewById(R.id.title);
		address = (TextView) findViewById(R.id.address);
		sender = (TextView) findViewById(R.id.sender);
		huodongTime = (TextView) findViewById(R.id.time);
		lookNum = (TextView) findViewById(R.id.looknum);
		label = (ImageView) findViewById(R.id.label);
		label.setVisibility(View.GONE);

		photo.setOnClickListener(clickListener);
	}
	
	public void setViewHeight(int width ,int padding) {
		ViewGroup.LayoutParams params = root.getLayoutParams();
		params.width = width - res.getDimensionPixelSize(padding) * 2;
		params.height = params.width * 18 / 29 ;//Banner宽高比为29:18
		root.setLayoutParams(params);
	}

	public void setPhotoNoClickable() {
		photo.setClickable(false);
	}

	public void update(HuodongDetailHeaderProvider provider) {
		this.provider = provider;
		
		huodongTitle.setText(provider.getHuodongTitle());
		address.setText(provider.getHuodongAddress());
		sender.setText(provider.getHuodongSenderName());
		huodongTime.setText(provider.getHuodongTime());
		lookNum.setText(provider.getLookNum() + "人看过");

		if (provider.getHot()) {
			label.setVisibility(View.VISIBLE);
			label.setImageResource(R.drawable.huodong_hot);
		}
		else {
			if (provider.getTui()) {
				label.setVisibility(View.VISIBLE);
				label.setImageResource(R.drawable.huodong_push);
			}
			else {
				label.setVisibility(View.GONE);
			}
		}
		
		File cache = ImageLoader.getInstance().getDiskCache().get(provider.getHuodongSenderPhoto());
		if (cache != null && cache.exists()) {
			photo.setImageBitmap(BitmapFactory.decodeFile(cache.getAbsolutePath()));
		}
		else {
			ImageLoader.getInstance().displayImage(provider.getHuodongSenderPhoto(), photo, App.CIRCLE_IMAGE_LOADING_LISTENER);
		}
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == photo) {
				// TODO 此处保留，（跳转到个人主页界面）
//				Intent intent = new Intent(context, UserInfoActivity.class);
//				ELog.i(String.valueOf(provider.getHuodongSendId()));
//				intent.putExtra(UserInfoActivity.INTENT_KEY_UID, provider.getHuodongSendId());
//				context.startActivity(intent);

				if (provider == null) {
					return ;
				}
				
				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					loginConfirm.openDialog();
					return ;
				}
				if (provider.getHuodongSendId() == App.USER.getUid()) {
					return ;
				}
				// TODO 跳转到聊天界面
				ChatMessage msg = new ChatMessage();
				msg.setTargetId(provider.getHuodongSendId());
				msg.setTargetNickname(provider.getHuodongSenderName());
				msg.setTargetPhotoUrl(provider.getHuodongSenderPhoto());
				msg.setActionType(ActionType.CHAT);
				msg.setType(Type.OTHER);
				msg.setSendType(SendType.READ);
				
				Intent intent = new Intent(context, ChatActivity.class);
				intent.putExtra(ChatActivity.REQUEST_MESSAGE, msg);
				context.startActivity(intent);
			}
		}
	};

}

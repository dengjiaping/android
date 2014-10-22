package com.oumen.circle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.UserProfile;
import com.oumen.message.CircleMessage;

/**
 * 偶们圈头部布局
 * 
 * 
 */
public class CircleListHeader extends LinearLayout {
	protected View viewBackground;
	protected ProgressBar pgs;
	protected ImageView imgPhoto;
	protected TextView txtBabyInfo;
	protected TextView txtNickname;

	protected Button btnBannerMessage;

	public CircleListHeader(Context context) {
		this(context, null, 0);
	}

	public CircleListHeader(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleListHeader(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.circles_list_header, this, true);

		//Header
		viewBackground = findViewById(R.id.user_header);
		
		pgs = (ProgressBar) findViewById(R.id.progress);

		imgPhoto = (ImageView) findViewById(R.id.photo);

		txtBabyInfo = (TextView) findViewById(R.id.info);

		txtNickname = (TextView) findViewById(R.id.nickname);

		btnBannerMessage = (Button) findViewById(R.id.banner_message);
	}
	
	public void setProgressVisibility(int visibility) {
		pgs.setVisibility(visibility);
	}

	public void setClickListener(View.OnClickListener listener) {
		viewBackground.setOnClickListener(listener);
		imgPhoto.setOnClickListener(listener);
		btnBannerMessage.setOnClickListener(listener);
	}

	public void update() {
		UserProfile profile = App.USER;
		txtNickname.setText(profile.getNickname());

		int babyType = profile.getBabyType();
		switch (babyType) {
			case UserProfile.BABY_TYPE_HUAI_YUN:// 怀孕
				txtBabyInfo.setText(profile.getGravidity());
				break;

			case UserProfile.BABY_TYPE_CHU_SHENG:// 出生
				txtBabyInfo.setText(profile.getBirthday());
				break;

			default:
				txtBabyInfo.setText("");
		}

		if (profile.hasPhoto()) {
			ImageLoader.getInstance().displayImage(profile.getPhotoUrl(App.BIG_PHOTO_SIZE), imgPhoto, App.OPTIONS_HEAD_RECT);
		}
		else {
			imgPhoto.setImageResource(R.drawable.rectangle_photo);
		}
		updateMessage();
	}

	public void updateMessage() {
		// 初始化消息提醒
		int count = CircleMessage.queryNewCount(Integer.valueOf(App.USER.getUid()), App.DB);
		if (count > 0) {
			btnBannerMessage.setVisibility(View.VISIBLE);
			if (count < 100) {
				btnBannerMessage.setText(count + "条新消息");
			} 
			else {
				btnBannerMessage.setText("99+条新消息");
			}
		}
		else {
			btnBannerMessage.setVisibility(View.GONE);
		}
	}
}

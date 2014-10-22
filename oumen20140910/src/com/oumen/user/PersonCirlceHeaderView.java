package com.oumen.user;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.tools.ImageTools;

public class PersonCirlceHeaderView extends RelativeLayout {
	
//	private final DisplayImageOptions options = new DisplayImageOptions.Builder()
//		.showImageForEmptyUri(R.drawable.oumen_title_background)
//		.showImageOnFail(R.drawable.oumen_title_background)
//		.imageScaleType(ImageScaleType.EXACTLY)
//		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new FadeInBitmapDisplayer(300))
//		.build();

	private ImageView ivPhoto;
	private ImageView llBackground;
	private TextView tvNickname, tvState, tvSign, tvAddress;
	private Button btnAddfriend;

	public PersonCirlceHeaderView(Context context) {
		this(context, null, 0);
	}

	public PersonCirlceHeaderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PersonCirlceHeaderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.userinfo_title, this, true);

		llBackground = (ImageView) findViewById(R.id.ll_userinfotitle_background);
		llBackground.setBackgroundResource(R.drawable.oumen_title_background);
		// 获取头部文件中的控件
		ivPhoto = (ImageView) findViewById(R.id.iv_userinfotitle_photo);
		ivPhoto.setImageResource(R.drawable.round_user_photo);
		tvNickname = (TextView) findViewById(R.id.tv_userinfotitle_nickname);
		tvState = (TextView) findViewById(R.id.tv_userinfotitle_statetime);
		tvSign = (TextView) findViewById(R.id.tv_userinfotitle_sign);
		tvAddress = (TextView) findViewById(R.id.tv_userinfotitle_location);
		btnAddfriend = (Button) findViewById(R.id.btn_userinfotitle_addfriend);
		btnAddfriend.setVisibility(View.GONE);
	}

	public void update(UserInfo info) {

		int headPhotoSize = getResources().getDimensionPixelSize(R.dimen.userinfo_head_photo_size);
		// 背景
//		if (!TextUtils.isEmpty(info.getBackPic())) {
//			ImageLoader.getInstance().displayImage(info.getBackgroundUrl(App.SCREEN_WIDTH), llBackground, options);
//		}
//		else {
		llBackground.setBackgroundResource(R.drawable.oumen_title_background);
//		}
		// 头像
		if (!TextUtils.isEmpty(info.getPhotoSourceUrl())) {
			File cache = ImageLoader.getInstance().getDiskCache().get(info.getPhotoUrl(headPhotoSize));
			if (cache != null && cache.exists()) {
				Bitmap pic = BitmapFactory.decodeFile(cache.getAbsolutePath());
				pic = ImageTools.clip2square(pic);
				ivPhoto.setImageBitmap(pic);
			}
			else {
				ImageLoader.getInstance().displayImage(info.getPhotoUrl(headPhotoSize), ivPhoto, App.OPTIONS_HEAD_RECT);
			}
		}
		else {
			ivPhoto.setImageResource(R.drawable.rectangle_photo);
		}
		
		// 昵称
		if (!TextUtils.isEmpty(info.getNickname())) {
			tvNickname.setText(info.getNickname());
		}
		else {
			// tvNickname.setText("");
			tvNickname.setVisibility(View.GONE);
		}
		// 签名
		if (!TextUtils.isEmpty(info.getSign())) {
			tvSign.setText(info.getSign());
		}
		else {
			tvSign.setText("");
			tvSign.setVisibility(View.GONE);
		}
		// 宝宝类别
		int type = info.getBabytype();
		if (type == 0) {
			// 怀孕
			tvState.setText("怀孕 " + info.getGravidity());
		}
		else if (type == 1) {
			// 出生
			tvState.setText(info.getGravidity());
		}
		else if (type == 2) {
			// 备孕
			tvState.setText("备孕");
		}
		else if (type == 3) {
			// 其他
			tvState.setText("其他");
		}
		else {
			tvState.setVisibility(View.GONE);
		}
		if (info.getAddress() != null) {
			tvAddress.setText(info.getAddress());
		}
		else {
//			tvAddress.setVisibility(View.GONE);
			tvAddress.setText("");
		}
//		if (info.isFriend()) {//TODO 如果是好友就不显示添加好友
//			btnAddfriend.setVisibility(View.GONE);
//		}
//		else {
//			btnAddfriend.setVisibility(View.VISIBLE);
//		}
	}

	public ImageView getPhoto() {
		return ivPhoto;
	}

	public Button getAddfriend() {
		return btnAddfriend;
	}

}

package com.oumen.activity.list;

import java.io.File;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.activity.HuodongTypeUtil;
import com.oumen.activity.message.BaseActivityMessage;
import com.oumen.android.App;
import com.oumen.widget.image.shape.RoundRectangleImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NearHuodongItem extends RelativeLayout {
	private RoundRectangleImageView photo;
	private TextView name, address, money, distance, type;

	public NearHuodongItem(Context context) {
		this(context, null, 0);
	}

	public NearHuodongItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NearHuodongItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.near_huodong_item, this, true);

		photo = (RoundRectangleImageView) findViewById(R.id.photo);
		photo.setRadius(10);
		name = (TextView) findViewById(R.id.name);
		address = (TextView) findViewById(R.id.address);
		money = (TextView) findViewById(R.id.money);
		distance = (TextView) findViewById(R.id.distence);
		distance.setVisibility(View.GONE);
		type = (TextView) findViewById(R.id.type);
		type.setVisibility(View.GONE);
	}

	public void update(BaseActivityMessage bean) {
		String url = bean.getPicUrl(getResources().getDimensionPixelSize(R.dimen.big_photo_size));
		File cache = ImageLoader.getInstance().getDiskCache().get(url);
		if (cache != null && cache.exists()) {
			Bitmap pic = BitmapFactory.decodeFile(cache.getAbsolutePath());
			photo.setImageBitmap(pic);
		}
		else {
			ImageLoader.getInstance().displayImage(url, photo, App.OPTIONS_PIC);
		}
		name.setText(bean.getName());
		address.setText(bean.getAddress());
		if (bean.getMoney().equals("0")) {
			money.setTextSize(20);
			money.setText("免费");
		}
		else {
			SpannableStringBuilder builder = new SpannableStringBuilder("¥" + bean.getMoney() + "起");
			AbsoluteSizeSpan tipSizeSpen = new AbsoluteSizeSpan(20 * 2);
			builder.setSpan(tipSizeSpen, 1, 1 + bean.getMoney().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			money.setText(builder);
		}

		if (bean.getDistance() > 0) {
			distance.setVisibility(View.VISIBLE);
			double dis = Double.valueOf(bean.getDistance()) / 1000.000;
			distance.setText(String.valueOf(dis) + "km");
			distance.setText(bean.getDistance() / 1000.00 + "km");
		}
		else {
			distance.setVisibility(View.GONE);
		}
	}

	/**
	 * 显示活动类型
	 * @param bean
	 * @param huodongtype
	 */
	public void update(BaseActivityMessage bean, int huodongtype) {
		update(bean);
		type.setVisibility(View.VISIBLE);
		if (huodongtype == HuodongTypeUtil.CONDITION_SHINEI) {
			type.setText("室内");
		}
		else if (huodongtype == HuodongTypeUtil.CONDITION_XIANSHANG) {
			type.setText("线上");
		}
		else {
			type.setText("户外");
		}
	}
}

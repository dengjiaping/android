package com.oumen.activity.list;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.oumen.R;
import com.oumen.activity.HuodongTypeUtil;
import com.oumen.activity.detail.RectImageHasDownloadView;
import com.oumen.activity.message.BaseActivityMessage;
import com.oumen.android.App;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NearHuodongItem extends RelativeLayout {
//	private RoundRectangleImageView photo;
	private ImageView photo;
	private TextView name, address, money, distance, type;

	private final SpannableStringBuilder builder = new SpannableStringBuilder();

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

		photo = (ImageView) findViewById(R.id.photo);
//		photo.setRadius(10);
		name = (TextView) findViewById(R.id.name);
		address = (TextView) findViewById(R.id.address);
		money = (TextView) findViewById(R.id.money);
		distance = (TextView) findViewById(R.id.distence);
		distance.setVisibility(View.GONE);
		type = (TextView) findViewById(R.id.type);
		type.setVisibility(View.GONE);
	}

	public void update(BaseActivityMessage bean) {
		ImageLoader.getInstance().displayImage(bean.getPicUrl(), photo, App.OPTIONS_ROUND_PIC);

		name.setText(bean.getName());
		address.setText(bean.getAddress());
		if (bean.getMoney().equals("0")) {
			money.setTextSize(20);
			money.setText("免费");
		}
		else {
			builder.clear();
			builder.append("¥" + bean.getMoney() + "起");
			AbsoluteSizeSpan tipSizeSpen = new AbsoluteSizeSpan(20 * 2);
			builder.setSpan(tipSizeSpen, 1, 1 + bean.getMoney().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			money.setText(builder);
		}

		if (bean.getDistance() > 0) {
			distance.setVisibility(View.VISIBLE);
			distance.setText(bean.getDistance() / 1000.00 + "km");
		}
		else {
			distance.setVisibility(View.GONE);
		}
	}

//	private static final ImageLoadingListener loadingListener = new SimpleImageLoadingListener() {
//		final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());
//
//		@Override
//		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//		}
//
//		@Override
//		public void onLoadingCancelled(String imageUri, View view) {
//		}
//
//		@Override
//		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//			ELog.i("下载完成");
//			ImageView image = (ImageView) view;
//			loadedImage = ImageTools.clip2square(loadedImage);
//			image.setImageBitmap(loadedImage);
//			if (loadedImage != null) {
//				ImageView imageView = (ImageView) view;
//				boolean firstDisplay = !displayedImages.contains(imageUri);
//				if (firstDisplay) {
//					FadeInBitmapDisplayer.animate(imageView, 500);
//					displayedImages.add(imageUri);
//				}
//			}
//		}
//	};

	public void updateImage(BaseActivityMessage bean) {
		ELog.e("正在加载图片");
		String url = bean.getPicUrl(getResources().getDimensionPixelSize(R.dimen.big_photo_size));
		File cache = ImageLoader.getInstance().getDiskCache().get(url);
		if (cache != null && cache.exists()) {
			Bitmap pic = BitmapFactory.decodeFile(cache.getAbsolutePath());
			pic = ImageTools.clip2square(pic);
			photo.setImageBitmap(pic);
		}
		else {
			ImageLoader.getInstance().displayImage(url, photo, App.OPTIONS_PIC);
		}
	}

	/**
	 * 显示活动类型
	 * 
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

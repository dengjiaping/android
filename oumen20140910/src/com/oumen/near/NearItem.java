package com.oumen.near;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.UserProfile;
import com.oumen.tools.CalendarTools;
import com.oumen.tools.ImageTools;
import com.oumen.widget.image.shape.RoundRectangleImageView;
/**
 * 偶们附近item
 */
public class NearItem extends RelativeLayout {
	public static final DisplayImageOptions OPTIONS = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.near_photo_default)
		.showImageOnFail(R.drawable.near_photo_default)
		.resetViewBeforeLoading(true)
		.cacheOnDisk(true)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
	
	private RoundRectangleImageView photo;
	private TextView distence;
	private TextView name;
	private TextView babyState;
	private TextView sign;
	

	public NearItem(Context context) {
		this(context, null,0);
	}
	
	public NearItem(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public NearItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.near_list_item, this, true);
		
		photo = (RoundRectangleImageView) findViewById(R.id.photo);
		photo.setRadius(context.getResources().getDimensionPixelSize(R.dimen.radius_micro));
		distence = (TextView) findViewById(R.id.distance);
		name = (TextView) findViewById(R.id.name);
		babyState = (TextView) findViewById(R.id.baby_state);
		sign = (TextView) findViewById(R.id.sign);
	}
	
	void update(NearBean near) {
		NearBean bean = (NearBean) near;
		// 头像
		if (!TextUtils.isEmpty(bean.getPhotoSourceUrl())) {
			ImageLoader.getInstance().displayImage(bean.getPhotoSourceUrl(), photo, OPTIONS, new SimpleImageLoadingListener() {
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					if (view instanceof ImageView && loadedImage != null) {
						Bitmap img = ImageTools.clip2square(loadedImage);
						
						ImageView v = (ImageView) view;
						v.setImageBitmap(img);
					}
				}
			});
		}
		else {
			photo.setImageResource(R.drawable.near_photo_default);
		}
		
		// 距离
		if (!TextUtils.isEmpty(bean.getDiss())) {
			distence.setVisibility(View.VISIBLE);
			double dis = Double.valueOf(bean.getDiss())/1000.000;
			distence.setText(String.valueOf(dis) + "km");
		}
		else {
			distence.setVisibility(View.GONE);
		}
		
		// 名称
		name.setText(bean.getUsername());
		// 宝宝签名
		sign.setText(bean.getManifesto());
		
		// 宝宝状态
		int type = bean.getBabytype();
		
		if (type == UserProfile.BABY_TYPE_CHU_SHENG) {//出生
			//宝宝岁数
			Date date;
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", App.LOCALE);
				date = format.parse(bean.getBirthday());
				//TODO 计算宝宝日期
				Calendar sCalendar = Calendar.getInstance();
				sCalendar.setTime(new Date());
				Calendar eCalendar = Calendar.getInstance();
				eCalendar.setTime(date);
				int mounts = CalendarTools.getOffsetMounths(sCalendar, eCalendar);
				String babayAge =(mounts / 12 == 0 ? "" : +mounts / 12 + "岁") + (mounts % 12 == 0 ? "" : mounts % 12 + "月");
				babyState.setText(babayAge);
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
			//宝宝性别
			String babaytype = bean.getSex();
			if ("1".equals(babaytype)) {//女宝宝
				babyState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.near_baby_girl, 0, 0, 0);
				babyState.setBackgroundResource(R.drawable.round_baby_girl);
			}
			else {//男宝宝
				babyState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.near_baby_boy, 0, 0, 0);
				babyState.setBackgroundResource(R.drawable.round_baby_boy);
			}
		}
		else if (type == UserProfile.BABY_TYPE_HUAI_YUN) {//怀孕
			babyState.setText("怀孕");
			babyState.setBackgroundResource(R.drawable.round_pregnancy);
		}
		else if (type == UserProfile.BABY_TYPE_BEI_YUN) {//备孕
			babyState.setText("备孕");
			babyState.setBackgroundResource(R.drawable.round_prepare);
		}
		else if (type == UserProfile.BABY_TYPE_QI_TA) {//其他
			babyState.setText("其他");
			babyState.setBackgroundResource(R.drawable.round_other);
		}
	}
}

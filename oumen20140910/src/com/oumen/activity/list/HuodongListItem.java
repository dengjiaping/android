package com.oumen.activity.list;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.oumen.R;
import com.oumen.activity.detail.HuodongDetailHeaderProvider;
import com.oumen.activity.detail.cell.HeaderMsgView;
import com.oumen.activity.widget.ActivityProgressBar;
import com.oumen.android.App;
import com.oumen.widget.image.shape.RoundRectangleImageView;
/**
 * 
 *活动list的item
 *
 */
@SuppressWarnings("deprecation")
public class HuodongListItem extends FrameLayout {
	private RoundRectangleImageView img;
	private FrameLayout finishLab;
	
	private ActivityProgressBar pgs;

	public HuodongListItem(Context context) {
		this(context, null, 0);
	}

	public HuodongListItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HuodongListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.huodong_list_item, this, true);
		
		img = (RoundRectangleImageView) findViewById(R.id.image);
		img.setRadius(getResources().getDimensionPixelSize(R.dimen.radius_large));
		img.setTag(this);
		
		pgs = (ActivityProgressBar) findViewById(R.id.progress_container);
		
		finishLab = (FrameLayout) findViewById(R.id.finish_label);
		finishLab.setVisibility(View.GONE);
	}
	
	public void update(HuodongDetailHeaderProvider provider, ImageLoadingListener loadingListener, ImageLoadingProgressListener progressListener) {
		String uri = provider.getHuodongPic();
		if (provider.ishuodongFinish()) {
			finishLab.setVisibility(View.VISIBLE);
		}
		else {
			finishLab.setVisibility(View.GONE);
		}

		File cache = ImageLoader.getInstance().getDiskCache().get(uri);
		if (cache != null && cache.exists()) {
			Bitmap pic = BitmapFactory.decodeFile(cache.getAbsolutePath());
			img.setImageBitmap(pic);
			onLoadingComplete();
		}
		else {
			ImageLoader.getInstance().displayImage(uri, img, App.OPTIONS_PIC, loadingListener, progressListener);
		}
	}
	
	public void onLoadingComplete() {
		if (pgs.getVisibility() == View.VISIBLE) {
			pgs.setVisibility(View.GONE);
//			headerMsg.setLabelVisibility(View.VISIBLE);
		}
	}

	public void onProgressUpdate(String uri, int current, int total) {
		if (pgs.getVisibility() != View.VISIBLE) {
			pgs.setVisibility(View.VISIBLE);
//			headerMsg.setLabelVisibility(View.GONE);
		}
		pgs.setMax(total);
		pgs.setProgress(current);
	}
}

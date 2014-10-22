package com.oumen.activity.widget;

import java.io.File;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.oumen.R;
import com.oumen.activity.detail.HuodongDetailHeaderProvider;
import com.oumen.activity.detail.cell.HeaderMsgView;
import com.oumen.android.App;
import com.oumen.tools.ELog;
import com.oumen.widget.image.shape.RoundRectangleImageView;
/**
 * 
 *活动list的item
 *
 */
@SuppressWarnings("deprecation")
public class HuodongListDefaultView extends FrameLayout {
	private RoundRectangleImageView img;
	private HeaderMsgView headerMsg;
	private FrameLayout finishLab;
	
	private ActivityProgressBar pgs;
	private Resources res;
	private int width = App.INT_UNSET;

	public HuodongListDefaultView(Context context) {
		this(context, null, 0);
	}

	public HuodongListDefaultView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HuodongListDefaultView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.huodong_list_item, this, true);
		
		res = context.getResources();
		width = res.getDisplayMetrics().widthPixels;
		
		img = (RoundRectangleImageView) findViewById(R.id.image);
		img.setRadius(getResources().getDimensionPixelSize(R.dimen.radius_large));
		img.setTag(this);
		
		headerMsg = (HeaderMsgView) findViewById(R.id.header);
		headerMsg.setPhotoNoClickable();
		
		pgs = (ActivityProgressBar) findViewById(R.id.progress_container);
		
		finishLab = (FrameLayout) findViewById(R.id.finish_label);
		finishLab.setVisibility(View.GONE);
		
		setViewPadding(getResources().getDimensionPixelSize(R.dimen.padding_large));
	}
	
	public void setViewPadding(int padding) {
		ViewGroup.LayoutParams imgParams = img.getLayoutParams();
		imgParams.width = width - padding *2;
		imgParams.height = imgParams.width * 18 / 29;//宽高比为29:18
		img.setLayoutParams(imgParams);
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
			ImageLoader.getInstance().displayImage(uri, img, App.OPTIONS_DEFAULT_PIC, loadingListener, progressListener);
		}
		headerMsg.update(provider);
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

package com.oumen.activity.detail;

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
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.oumen.R;
import com.oumen.android.App;
/**
 * 
 * 有下载进度的ImageView
 *
 */
public class RectImageHasDownloadView extends FrameLayout {
	private ImageView img;
	private RectActivityProgressBar pgs;
	
	private FrameLayout root;
	private int width = App.INT_UNSET;
	private Resources res ;
	
	public RectImageHasDownloadView(Context context) {
		this(context, null, 0);
	}

	public RectImageHasDownloadView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RectImageHasDownloadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.rectangle_imageview_has_download_view, this, true);
		
		root = (FrameLayout) findViewById(R.id.fragment_root);
		
		res = context.getResources();
		width = res.getDisplayMetrics().widthPixels;
		
		img = (ImageView) findViewById(R.id.image);
		
		pgs = (RectActivityProgressBar) findViewById(R.id.progress_container);
		
		setViewPadding(getResources().getDimensionPixelSize(R.dimen.padding_zreo));
	}
	
	public void setViewPadding(int padding) {
		ViewGroup.LayoutParams rootParams = root.getLayoutParams();
		rootParams.width = width - padding *2;
		rootParams.height = rootParams.width * 18 / 29;//宽高比为29:18
		root.setLayoutParams(rootParams);
		
		ViewGroup.LayoutParams imgParams = img.getLayoutParams();
		imgParams.width = width - padding *2;
		imgParams.height = imgParams.width * 18 / 29;//宽高比为29:18
		img.setLayoutParams(imgParams);
	}
	
	public ImageView getImageView() {
		return img;
	}
	
	public void update(String url) {
		File cache = ImageLoader.getInstance().getDiskCache().get(url);
		if (cache != null && cache.exists()) {
			Bitmap pic = BitmapFactory.decodeFile(cache.getAbsolutePath());
			img.setImageBitmap(pic);
			onLoadingComplete();
		}
		else {
			ImageLoader.getInstance().displayImage(url, img, App.OPTIONS_DEFAULT_PIC, loadingListener, progressListener);
		}
	}
	
	public void onLoadingComplete() {
		if (pgs.getVisibility() == View.VISIBLE) {
			pgs.setVisibility(View.GONE);
		}
	}

	public void onProgressUpdate(String uri, int current, int total) {
		if (pgs.getVisibility() != View.VISIBLE) {
			pgs.setVisibility(View.VISIBLE);
		}
		pgs.setMax(total);
		pgs.setProgress(current);
	}
	
	
	final ImageLoadingListener loadingListener = new SimpleImageLoadingListener() {
		private void updateOnMainThread(final String imageUri, final View view) {
			view.post(new Runnable() {

				@Override
				public void run() {
					RectImageHasDownloadView rootview =	(RectImageHasDownloadView) view.getParent().getParent();
					rootview.onLoadingComplete();
				}
			});
		}

		@Override
		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			updateOnMainThread(imageUri, view);
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			updateOnMainThread(imageUri, view);
		}

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			updateOnMainThread(imageUri, view);
		}
	};

	final ImageLoadingProgressListener progressListener = new ImageLoadingProgressListener() {

		@Override
		public void onProgressUpdate(final String imageUri, final View view, final int current, final int total) {
//			ELog.v("Uri:" + imageUri + " Progress:" + current + "/" + total + " View:" + view);
			RectImageHasDownloadView rootview =	(RectImageHasDownloadView) view.getParent().getParent();
			rootview.onProgressUpdate(imageUri, current, total);
		}
	};

}

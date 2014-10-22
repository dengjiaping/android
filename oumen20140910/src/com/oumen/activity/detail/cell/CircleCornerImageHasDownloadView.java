package com.oumen.activity.detail.cell;

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
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.oumen.R;
import com.oumen.activity.widget.ActivityProgressBar;
import com.oumen.android.App;
import com.oumen.tools.ImageTools;
import com.oumen.widget.image.shape.RoundRectangleImageView;

/**
 * 
 * 有下载进度的ImageView
 *
 */
public class CircleCornerImageHasDownloadView extends RelativeLayout {
	private RoundRectangleImageView img;
	private ActivityProgressBar pgs;

	private RelativeLayout root;
	private int width = App.INT_UNSET;
	private Resources res;

	public CircleCornerImageHasDownloadView(Context context) {
		this(context, null, 0);
	}

	public CircleCornerImageHasDownloadView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleCornerImageHasDownloadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.roundrectangle_imageview_has_download_view, this, true);

		root = (RelativeLayout) findViewById(R.id.fragment_root);
		res = context.getResources();
		width = res.getDisplayMetrics().widthPixels;

		img = (RoundRectangleImageView) findViewById(R.id.image);

		pgs = (ActivityProgressBar) findViewById(R.id.progress_container);
//		pgs.setVisibility(View.GONE);
		setViewPadding(getResources().getDimensionPixelSize(R.dimen.padding_large));
	}

	public void setViewPadding(int padding) {
		ViewGroup.LayoutParams params = img.getLayoutParams();
		params.width = width - padding * 2;
		params.height = params.width * 18 / 29;//Banner宽高比为29:18
		img.setLayoutParams(params);
	}

	public void setRadius(int radius) {
		img.setRadius(radius);
	}

	public RoundRectangleImageView getImageView() {
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
					CircleCornerImageHasDownloadView rootview = (CircleCornerImageHasDownloadView) view.getParent().getParent();
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
			CircleCornerImageHasDownloadView rootview = (CircleCornerImageHasDownloadView) view.getParent().getParent();
			rootview.onProgressUpdate(imageUri, current, total);
		}
	};

}

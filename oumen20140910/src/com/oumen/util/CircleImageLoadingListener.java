package com.oumen.util;

import java.io.IOException;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;

@SuppressWarnings("deprecation")
public class CircleImageLoadingListener extends SimpleImageLoadingListener {

	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		if (loadedImage == null)
			return;
		
		Bitmap square = ImageTools.clip2square(loadedImage);
		loadedImage = ImageTools.toOvalBitmap(square);
		
		if (view != null && view instanceof ImageView) {
			if (loadedImage.isRecycled()) {
				ELog.i("Recycled:" + imageUri);
			}
			else {
				ImageView imgView = (ImageView)view;
				imgView.setImageBitmap(loadedImage);
			}
		}

		DiskCache diskCache = ImageLoader.getInstance().getDiskCache();
		try {
			diskCache.save(imageUri, loadedImage);
		}
		catch (IOException e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
	}

}

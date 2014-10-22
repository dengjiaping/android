package com.oumen.widget.image;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.oumen.R;
import com.oumen.tools.ImageTools;
import com.oumen.widget.file.ImageData;
import com.oumen.widget.preview.image.ImagePagerFragment;

public class GridImageAdapter extends BaseAdapter {
	public final ArrayList<ImageData> data = new ArrayList<ImageData>();
	public int size;
	private final BitmapProcessor preBitmapProcessor = new BitmapProcessor() {
		
		@Override
		public Bitmap process(Bitmap bitmap) {
			Bitmap img = ImageTools.clip2square(bitmap);
			return img;
		}
	};

	public final DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.pic_default)
		.showImageOnFail(R.drawable.pic_default)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.cacheOnDisk(true)
		.preProcessor(preBitmapProcessor)
		.build();

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView item;
		if (convertView == null) {
			size = parent.getContext().getResources().getDimensionPixelSize(R.dimen.default_cell_size);
			item = new ImageView(parent.getContext());
			item.setLayoutParams(new AbsListView.LayoutParams(size, size));
			item.setOnClickListener(clickListener);
		}
		else {
			item = (ImageView) convertView;
		}
		String path = data.get(position).path;
		item.setTag(path);
		
		File cache = ImageLoader.getInstance().getDiskCache().get(path);
		if (cache != null && cache.exists()) {
			Bitmap pic = BitmapFactory.decodeFile(cache.getAbsolutePath());
			pic = ImageTools.clip2square(pic);
			item.setImageBitmap(pic);
		}
		else {
			ImageLoader.getInstance().displayImage(path, item, options);
		}
		
		return item;
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String path = (String)v.getTag();
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).path.equals(path)) {
//					String[] paths = new String[data.size()];
					
					Bundle params = new Bundle();
					params.putInt(ImagePagerFragment.PARAMS_KEY_START_INDEX, i);
					params.putSerializable(ImagePagerFragment.PARAMS_KEY_DATA, data);
					
					Intent intent = new Intent(v.getContext(), ImagePreviewActivity.class);
					intent.putExtra(ImagePreviewActivity.INTENT_KEY_DATA, params);
					v.getContext().startActivity(intent);
					return;
				}
			}
		}
	};
}

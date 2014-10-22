package com.oumen.widget.preview.image;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.oumen.R;
import com.oumen.app.BaseApplication;
import com.oumen.app.callback.CompleteCallback;
import com.oumen.tools.ELog;
import com.oumen.widget.file.ImageData;

public class ImagePagerFragment extends Fragment {
	public static final String PARAMS_KEY_START_INDEX = "start";
	public static final String PARAMS_KEY_DATA = "data";
	public static final String PARAMS_KEY_SELECTABLE = "selectable";
	public static final String PARAMS_KEY_CLOSEABLE = "closeable";
	public static final String PARAMS_KEY_SELECT_MAX = "max";
	public static final String PARAMS_KEY_SELECT_COUNT = "count";
	
	private ArrayList<ImageData> data;
	
	private boolean isSelectable;
	private boolean isCloseable;
	private int selectMax;
	private int selectCount;
	
	private CompleteCallback callback;
	
	private ImagePager pager;
	
	private final PagerAdapterImpl adapter = new PagerAdapterImpl();

	public static final DisplayImageOptions options = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565)
		.displayer(new FadeInBitmapDisplayer(300))
		.build();
	
	private final View.OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.checkbox) {
				ImageView view = (ImageView)v;
				ImageData itemData = (ImageData)v.getTag();
				if (!itemData.select && selectMax != BaseApplication.INT_UNSET && selectCount >= selectMax) {
					return;
				}
				
				itemData.select = !itemData.select;
				if (itemData.select) {
					selectCount++;
				}
				else {
					selectCount--;
				}
				view.setImageResource(itemData.select ? R.drawable.icon_checked : R.drawable.icon_unchecked);
			}
			else if (callback != null) {
				int[] results = new int[selectCount];
				int count = 0;
				for (ImageData i : data) {
					if (i.select) {
						results[count++] = i.index;
						
						if (count == selectCount) {
							break;
						}
					}
				}
				
				callback.onComplete(ImagePagerFragment.this, results);
			}
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		isSelectable = savedInstanceState.getBoolean(PARAMS_KEY_SELECTABLE, false);
		isCloseable = savedInstanceState.getBoolean(PARAMS_KEY_CLOSEABLE, false);
		selectMax = savedInstanceState.getInt(PARAMS_KEY_SELECT_MAX, BaseApplication.INT_UNSET);
		selectCount = savedInstanceState.getInt(PARAMS_KEY_SELECT_COUNT, 0);
		data = (ArrayList<ImageData>)savedInstanceState.getSerializable(PARAMS_KEY_DATA);
		for (ImageData i : data) {
			if (i.select) {
				selectCount++;
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		int startIndex = savedInstanceState.getInt(PARAMS_KEY_START_INDEX, 0);
		
		pager = new ImagePager(container.getContext());
		
		pager.setAdapter(adapter);
		pager.setCurrentItem(startIndex);
		return pager;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(PARAMS_KEY_START_INDEX, pager.getCurrentItem());
		outState.putInt(PARAMS_KEY_SELECT_MAX, selectMax);
		outState.putInt(PARAMS_KEY_SELECT_COUNT, selectCount);
		outState.putBoolean(PARAMS_KEY_SELECTABLE, isSelectable);
		outState.putBoolean(PARAMS_KEY_CLOSEABLE, isCloseable);
		outState.putSerializable(PARAMS_KEY_DATA, data);
		super.onSaveInstanceState(outState);
	}

	class PagerAdapterImpl extends PagerAdapter {

		@Override
		public int getCount() {
			return data == null ? 0 : data.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			ImageData imageData = data.get(position);
			
			ELog.i("Position:" + position);
			
			View root = View.inflate(container.getContext(), R.layout.image_view_pager_item, null);
			final PhotoView photoView = (PhotoView) root.findViewById(R.id.image);
			final ProgressBar progress = (ProgressBar) root.findViewById(R.id.image_progress);
			progress.setVisibility(View.VISIBLE);
			ELog.i(imageData.path);
			
			final ImageLoadingListener listener = new SimpleImageLoadingListener() {
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					progress.setVisibility(View.GONE);
				}
			};
			ImageLoader.getInstance().displayImage(imageData.path, photoView, options,listener);
//			ImageLoader.getInstance().loadImage(imageData.path,options,listener);

			if (!isSelectable && !isCloseable) {//如果两个都不需要，就将整个fragment都隐藏
				FrameLayout frag = (FrameLayout) root.findViewById(R.id.framgnent_buttom);
				frag.setVisibility(View.GONE);
			}
			else {
				FrameLayout frag = (FrameLayout) root.findViewById(R.id.framgnent_buttom);
				frag.setVisibility(View.VISIBLE);
			}
			
			if (isSelectable) {
				ImageView view = (ImageView) root.findViewById(R.id.checkbox);
				view.setTag(imageData);
				view.setOnClickListener(clickListener);
				view.setVisibility(View.VISIBLE);
				view.setImageResource(imageData.select ? R.drawable.icon_checked : R.drawable.icon_unchecked);
			}
			else {
				ImageView view = (ImageView) root.findViewById(R.id.checkbox);
				view.setOnClickListener(null);
				view.setVisibility(View.GONE);
			}
			
			if (isCloseable) {
				Button view = (Button) root.findViewById(R.id.finish);
				view.setOnClickListener(clickListener);
				view.setVisibility(View.VISIBLE);
			}
			else {
				Button view = (Button) root.findViewById(R.id.finish);
				view.setOnClickListener(null);
				view.setVisibility(View.GONE);
			}

			container.addView(root, 0);

			return root;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

	private class ImagePager extends ViewPager {

	    public ImagePager(Context context) {
	        super(context);
	    }

	    public ImagePager(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }

	    @Override
	    public boolean onInterceptTouchEvent(MotionEvent ev) {
	        try {
	            return super.onInterceptTouchEvent(ev);
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }
	}
	
	public void setData(ArrayList<ImageData> data) {
		this.data = data;
	}
	
	public void setCompleteCallback(CompleteCallback callback) {
		this.callback = callback;
	}
}

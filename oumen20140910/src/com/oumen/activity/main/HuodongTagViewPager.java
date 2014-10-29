package com.oumen.activity.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import widget.viewpager.CirclePageIndicator;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.oumen.R;
import com.oumen.activity.message.ActivityTag;

public class HuodongTagViewPager extends FrameLayout {
	private final int NUM = 4;
	protected ViewPager pager;
	protected CirclePageIndicator indicator;

	private final AdapterImpl adapter = new AdapterImpl();

	public HuodongTagViewPager(Context context) {
		this(context, null, 0);
	}

	public HuodongTagViewPager(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HuodongTagViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		int padding = getResources().getDimensionPixelSize(R.dimen.padding_large);

		pager = new ViewPager(context);
		pager.setAdapter(adapter);
		addView(pager, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		indicator = new CirclePageIndicator(context);
		indicator.setPadding(padding, padding, padding, padding);
		indicator.setViewPager(pager);
		addView(indicator, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
	}
	
	public void addAll(Collection<ActivityTag> collection) {
		synchronized (adapter.data) {
			adapter.data.clear();
			adapter.data.addAll(collection);
			
			if (adapter.data.size() <= 1) {
				indicator.setVisibility(View.GONE);
			}
			else {
				indicator.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public void clear() {
		synchronized (adapter.data) {
			adapter.data.clear();
		}
	}

	public boolean isEmpty() {
		synchronized (adapter.data) {
			return adapter.data.isEmpty();
		}
	}
	
	public void notifyDataSetChanged() {
		adapter.notifyDataSetChanged();
	}

	class AdapterImpl extends PagerAdapter {

		final List<ActivityTag> data = new ArrayList<ActivityTag>();

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			LinearLayout item = createItem(container.getContext());
			for (int i = position * 4; i < position * 4 + NUM; i ++) {
				if (i > data.size() - 1) break;
				ActivityTag itemData = data.get(i);
				Item cell = new Item(container.getContext());
				cell.setWeightSum(1.0f);
				cell.update(itemData);
				cell.setTag(itemData);
				cell.setOnClickListener(clickListener);
				item.addView(cell);
			}
			return item;
		}

		@Override
		public int getCount() {
			return data.size() / 4;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		
		private LinearLayout createItem(Context context) {
			LinearLayout container = new LinearLayout(context);
			container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			container.setOrientation(LinearLayout.HORIZONTAL);
			return container;
		}
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v instanceof Item) {
				// TODO 
			}

//			if (v instanceof CircleCornerImageHasDownloadView) {
//				ItemData itemData = (ItemData) v.getTag();
//				if (itemData.type == ItemData.TYPE_ACTIVITY_DETAIL) {// 跳转到活动详情界面
//
//					Intent intent = new Intent(context, HuoDongDetailActivity.class);
//					intent.putExtra(HuoDongDetailActivity.INTENT_KEY_ACTIVITY_ID, itemData.activityId);
//					context.startActivity(intent);
//				}
//				else if (itemData.type == ItemData.TYPE_ACTIVITY_URL) {// 跳转到活动网页界面
//					Intent intent = new Intent(context, ActivityWebViewActivity.class);
//					intent.putExtra(ActivityWebViewActivity.INTENT_WEBVIEW_ACTIVITY_URL, itemData.webUrl);
//					intent.putExtra(ActivityWebViewActivity.INTENT_WEBVIEW_SHARE_MSG, itemData.shareJson);
//					context.startActivity(intent);
//				}
//				else if (itemData.type == ItemData.TYPE_LAGER_IAMGE) {// 点击查看大图
//
//					ArrayList<ImageData> templist = new ArrayList<ImageData>();
//					templist.add(new ImageData(itemData.picUrl));
//
//					Bundle params = new Bundle();
//					params.putInt(ImagePagerFragment.PARAMS_KEY_START_INDEX, 0);
//					params.putSerializable(ImagePagerFragment.PARAMS_KEY_DATA, templist);
//
//					Intent intent = new Intent(v.getContext(), ImagePreviewActivity.class);
//					intent.putExtra(ImagePreviewActivity.INTENT_KEY_DATA, params);
//					v.getContext().startActivity(intent);
//				}
//			}
		}
	};

	class Item extends LinearLayout {
		ImageView image;
		TextView tag;

		public Item(Context context) {
			this(context, null, 0);
		}

		public Item(Context context, AttributeSet attrs) {
			this(context, attrs, 0);
		}

		public Item(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.textview_has_top_image, this, true);
			image = (ImageView) findViewById(R.id.tag_image);
			tag = (TextView) findViewById(R.id.tag_text);
		}

		public void update(final ActivityTag data) {
			tag.setText(data.getTagName());

			if (data.getTagBitmap() != null) {
				image.setImageBitmap(data.getTagBitmap());
			}
			else {
				ImageLoader.getInstance().displayImage(data.getTagUrl(), image, new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {
					}

					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					}

					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						if (loadedImage != null) {
							data.setTagBitmap(loadedImage);
						}
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {
					}
				});
			}

		}

	}

}

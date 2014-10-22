package com.oumen.biaoqing;

import java.io.File;

import com.oumen.R;
import com.oumen.android.App;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * 聊天表情界面控件
 */

public class EmojiView extends RelativeLayout implements Callback {
	public static final int HANDLER_DOWNLOAD_SUCCESS = 1;
	//界面控件
	private RelativeLayout root;
	private ViewPager viewPager;
	private ImageView ivSmall, ivOuba, ivCiwei;

	private BiaoqingPagerViewAdapter adapter = new BiaoqingPagerViewAdapter();
	private ViewPagerChangeListener listener = new ViewPagerChangeListener();
	//下载完了以后文件保存的地址
	private File file_ouba, file_ciwei;
	private OnClickListener itemClickListener;
	private Handler handler = new Handler(this);

	public EmojiView(Context context) {
		this(context, null, 0);
	}

	public EmojiView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EmojiView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.chat_biaoqing, this, true);
		
		root = (RelativeLayout) findViewById(R.id.rootview);
		
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(listener);

		ivSmall = (ImageView) findViewById(R.id.imageview1);
		ivOuba = (ImageView) findViewById(R.id.imageview2);
		ivCiwei = (ImageView) findViewById(R.id.imageview3);
		
		ivSmall.setVisibility(View.VISIBLE);
		ivOuba.setVisibility(View.GONE);
		ivCiwei.setVisibility(View.GONE);
		
		ivSmall.setOnClickListener(clickListener);
		ivOuba.setOnClickListener(clickListener);
		ivCiwei.setOnClickListener(clickListener);

	}
	
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == ivSmall) {
				viewPager.setCurrentItem(0);
			}
			else if (v == ivOuba) {
				viewPager.setCurrentItem(1);
			}
			else if (v == ivCiwei) {
				viewPager.setCurrentItem(2);
			}
		}
	};

	public void setClickListener(OnClickListener clickListener) {
		this.itemClickListener = clickListener;
	}
	
	public void setBiaoqingHeight(int height) {
		ViewGroup.LayoutParams params = root.getLayoutParams();
		params.height = height;
		root.setLayoutParams(params);
	}

	/**
	 * 添加小表情
	 */
	public void addDefaultBiaoqing() {
		adapter.datas.clear();
		EmojiChildView childView = new EmojiChildView(getContext());
		childView.setClickListener(itemClickListener);
		childView.setHandler(handler);
		childView.addImageBiaoqing(getContext(), App.getAssertBiaoqingPath(), true);
		adapter.datas.add(childView);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 添加所有表情数据
	 */
	public void addAllBiaoqing() {
		//添加数据
		addDefaultBiaoqing();
		try {
			file_ouba = new File(App.getChatBiaoqingOubaPath());
			if (!file_ouba.getParentFile().exists()) {
				file_ouba.getParentFile().mkdirs();
			}

			if (!file_ouba.exists()) {
				file_ouba.mkdir();
			}

			file_ciwei = new File(App.getChatBiaoqingCiweiPath());
			if (!file_ciwei.getParentFile().exists()) {
				file_ciwei.getParentFile().mkdirs();
			}
			if (!file_ciwei.exists()) {
				file_ciwei.mkdir();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		ivOuba.setVisibility(View.VISIBLE);
		EmojiChildView childView = new EmojiChildView(getContext());
		childView.setHandler(handler);
		childView.setClickListener(itemClickListener);
		childView.addImageBiaoqing(getContext(), file_ouba.getAbsolutePath(), false);
		adapter.datas.add(childView);
		
		ivCiwei.setVisibility(View.VISIBLE);
		EmojiChildView childView1 = new EmojiChildView(getContext());
		childView1.setHandler(handler);
		childView1.setClickListener(itemClickListener);
		childView1.addImageBiaoqing(getContext(), file_ciwei.getAbsolutePath(), false);
		adapter.datas.add(childView1);
		
		adapter.notifyDataSetChanged();
	}

	/**
	 * viewPager切换监听
	 * 
	 */
	public class ViewPagerChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
			changeTag(position);
		}
	}
	
	private void changeTag (int position) {
		switch (position) {
			case 0:
				ivSmall.setBackgroundColor(getResources().getColor(R.color.biaoqing_tip_click));
				ivOuba.setBackgroundColor(getResources().getColor(R.color.white));
				ivCiwei.setBackgroundColor(getResources().getColor(R.color.white));
				break;

			case 1:
				ivSmall.setBackgroundColor(getResources().getColor(R.color.white));
				ivOuba.setBackgroundColor(getResources().getColor(R.color.biaoqing_tip_click));
				ivCiwei.setBackgroundColor(getResources().getColor(R.color.white));
				break;
			case 2:
				ivSmall.setBackgroundColor(getResources().getColor(R.color.white));
				ivOuba.setBackgroundColor(getResources().getColor(R.color.white));
				ivCiwei.setBackgroundColor(getResources().getColor(R.color.biaoqing_tip_click));
				break;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_DOWNLOAD_SUCCESS:
				String path = (String)msg.obj;
				if (App.getChatBiaoqingCiweiPath().equals(path)) {
					EmojiChildView temp = (EmojiChildView)adapter.datas.get(2);
					temp.addImageBiaoqing(getContext(), file_ciwei.getAbsolutePath(), false);
				}
				else if (App.getChatBiaoqingOubaPath().equals(path)) {
					EmojiChildView temp = (EmojiChildView)adapter.datas.get(1);
					temp.addImageBiaoqing(getContext(), file_ouba.getAbsolutePath(), false);
				}
				adapter.notifyDataSetChanged();
				break;

			default:
				break;
		}
		return false;
	}
}

package com.oumen.chat;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.oumen.R;

public class EmojiAdapter extends BaseAdapter implements OnClickListener {

	private Context mContext;

	private LayoutInflater mInflater;

	int sumCount = 16;
	int startIndex = 10;
	int endIndex = startIndex + sumCount;

	Bitmap[][] emoji = new Bitmap[2][8];
	private HashMap<String, Bitmap> emojis=new HashMap<String, Bitmap>();


	public EmojiAdapter(Context _c) {

		this.mContext = _c;
		this.mInflater = LayoutInflater.from(mContext);
		getScreenSize((Activity) mContext);
		initData();
	}

	private void initData() {

		for (int i = 0; i < sumCount / 2; i++) {

			emoji[0][i] = getImageFromAssetsFile("emoji_icon/" + "a" + (startIndex + i) + ".png");
			emojis.put("a" + (startIndex + i),emoji[0][i]);
		}

		for (int i = 0; i < sumCount / 2; i++) {

			emoji[1][i] = getImageFromAssetsFile("emoji_icon/" + "a" + (startIndex + sumCount / 2 + i) + ".png");
			emojis.put("a" + (startIndex + sumCount / 2 + i),emoji[1][i]);
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return emojis.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;

		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.item_emoji, null);
			holder = new ViewHolder();
			holder.topEmojiLayout = (LinearLayout) convertView.findViewById(R.id.layout_emoji_top);
			holder.bottomEmojiLayout = (LinearLayout) convertView.findViewById(R.id.layout_emoji_bottom);
			convertView.setTag(holder);
			// 显示两行、每行四个
			for (int i = 0; i < sumCount/2; i++) {
				// 第一行
				if (i < 4) {
					ImageView image = new ImageView(mContext);
					image.setImageBitmap(emoji[position][i]);
					image.setAdjustViewBounds(true);
					image.setLayoutParams(new Gallery.LayoutParams(screenSize[0] / 4, screenSize[0] / 4));
					holder.topEmojiLayout.addView(image);
					holder.topEmojiLayout.setOnClickListener(this);
//					image.setOnClickListener(this);
					// 第二行
				} else {
					LayoutParams params = new Gallery.LayoutParams(screenSize[0] / 4, screenSize[0] / 4);
					ImageView image = new ImageView(mContext);
					image.setImageBitmap(emoji[position][i]);
					image.setAdjustViewBounds(true);
					image.setLayoutParams(params);
					holder.bottomEmojiLayout.addView(image);
					holder.bottomEmojiLayout.setOnClickListener(this);
//					image.setOnClickListener(this);
				}
			}
		} else {

			holder = (ViewHolder) convertView.getTag();
		}

		return convertView;
	}

	@Override
	public void onClick(View v) {
		
	}

	/**
	 * 从Assets中读取图片
	 */
	private Bitmap getImageFromAssetsFile(String fileName) {
		Bitmap image = null;
		AssetManager am = mContext.getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return image;

	}

	class ViewHolder {

		private LinearLayout topEmojiLayout;
		private LinearLayout bottomEmojiLayout;
	}

	/**
	 * 存储屏幕高宽的数组
	 */
	private static int[] screenSize = null;

	/**
	 * 获取屏幕高宽
	 * 
	 * @Description:
	 * @param activity
	 * @return 屏幕宽高的数组 [0]宽， [1]高
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-7-30
	 */
	public static int[] getScreenSize(Activity activity) {
		if (screenSize == null) {
			Display display = activity.getWindowManager().getDefaultDisplay();
			screenSize = new int[2];
			screenSize[0] = display.getWidth();
			screenSize[1] = display.getHeight();
		}
		return screenSize;
	}
//	OumenLogicCallBack callBack = new OumenLogicCallBack() {
//	};
}

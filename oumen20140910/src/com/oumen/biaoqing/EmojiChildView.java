package com.oumen.biaoqing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import widget.viewpager.CirclePageIndicator;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.file.download.Download;
import com.oumen.file.download.Download.ProgressListener;
import com.oumen.message.Type;
import com.oumen.tools.ApacheZip;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;

/**
 * 聊天子类ViewPager
 */
public class EmojiChildView extends FrameLayout {
	private FrameLayout container;
	private ViewPager viewPager;
	private CirclePageIndicator pointerIndicator;

	private BiaoqingPagerViewAdapter adapter = new BiaoqingPagerViewAdapter();

	private int LINE_NUM = 0; // 图片表情一行显示的个数
	private int PAGE_NUM = 0; // 某个文件下表情一页显示的表情个数

	private DownLoadEmojiView defaultView;

	private OnClickListener itemClickListener;

	private Handler handler;

	public EmojiChildView(Context context) {
		this(context, null, 0);
	}

	public EmojiChildView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EmojiChildView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.chat_child_biaoqing, this, true);

		container = (FrameLayout) findViewById(R.id.container);

		pointerIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		pointerIndicator.setStrokeColor(getResources().getColor(R.color.biaoqing_tip_click));
		pointerIndicator.setFillColor(getResources().getColor(R.color.red));
		pointerIndicator.setVisibility(View.GONE);
	}

	public void setClickListener(OnClickListener clickListener) {
		this.itemClickListener = clickListener;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	/**
	 * 读取表情消息
	 * 
	 * @param context
	 * @param fileName
	 * @param fromAssets
	 */
	public void addImageBiaoqing(Context context, String fileName, boolean fromAssets) {
		container.removeAllViews();
		int size = 0;
		// 获取文件名称列表
		ArrayList<BiaoQing> assetslists = new ArrayList<BiaoQing>();
		ArrayList<String> filedatas = new ArrayList<String>();
		if (fromAssets) {
			assetslists = DownLoadUtil.getSmallBiaoqing();
			size = App.SMALLBIAOQING.getSmallBiaoqingsize();
		}
		else {
			// TODO　在此处进行文件过滤，hashcode
			filedatas = DownLoadUtil.filterFile(fileName, true);
			if (filedatas == null)
				size = 0;
			else
				size = filedatas.size();
		}

		if (size == 0) {
			//TODO 显示下载界面
			if (App.getChatBiaoqingOubaPath().equals(fileName)) {//欧巴表情
				defaultView = new DownLoadEmojiView(context);
				defaultView.setImageBackground(R.drawable.biaoqing_ouba_bg);
				defaultView.setType(DownLoadEmojiView.BIAOQING_TYPE.OUBA);
				defaultView.setDownLoadListener(clickListener);
			}
			else if (App.getChatBiaoqingCiweiPath().equals(fileName)) {
				defaultView = new DownLoadEmojiView(context);
				defaultView.setImageBackground(R.drawable.biaoqing_ciwei_bg);
				defaultView.setType(DownLoadEmojiView.BIAOQING_TYPE.CIWEI);
				defaultView.setDownLoadListener(clickListener);
			}
			container.addView(defaultView);
		}
		else {
			adapter.datas.clear();
			viewPager = new ViewPager(getContext());
			viewPager.setAdapter(adapter);
			//计算每行显示几个
			if (App.getAssertBiaoqingPath().equals(fileName)) {
				LINE_NUM = 7;
				PAGE_NUM = LINE_NUM * 3;
				
				BiaoQing biaoqing = new BiaoQing();
				biaoqing.setType(Type.OTHER);
				biaoqing.setSendMsg(null);
				Bitmap tempBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.biaoqing_delete);
				tempBitmap = ImageTools.scale(tempBitmap, 48, 48);
				biaoqing.setBitmap(tempBitmap);
				biaoqing.setImagePath(null);
				
				for (int i = 0;i < size/PAGE_NUM ; i++) {
					assetslists.add(PAGE_NUM * (i + 1) - 1, biaoqing);
					size++;
				}
			}
			else {
				LINE_NUM = 4;
				PAGE_NUM = LINE_NUM * 2;
			}

			int totalPager = size / PAGE_NUM + (size % PAGE_NUM > 0 ? 1 : 0);

			//创建引导pointer
			pointerIndicator.setVisibility(View.VISIBLE);
			pointerIndicator.setViewPager(viewPager);
			//创建viewpager的子view
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for (int j = 0; j < totalPager; j++) {
				View view = inflater.inflate(R.layout.chat_biaoqing_item, this, false);
				LinearLayout root = (LinearLayout) view.findViewById(R.id.rooter_view);
				GridView gridview = (GridView) view.findViewById(R.id.gridview);
				
				if (fromAssets) {
					gridview.setHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.padding_small));
					gridview.setVerticalSpacing(getResources().getDimensionPixelSize(R.dimen.padding_super));
					gridview.setNumColumns(7);
				}
				else {
					gridview.setHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.padding_small));
					gridview.setVerticalSpacing(getResources().getDimensionPixelSize(R.dimen.padding_small));
					gridview.setNumColumns(4);
				}

				GridImageAdapter imageAdapter = new GridImageAdapter(context, itemClickListener);
				gridview.setAdapter(imageAdapter);

				if (j == totalPager - 1) { // 最后一页的加载
					
					for (int i = 0; i < size - (totalPager - 1) * PAGE_NUM; i++) {
						//从资源文件下加载图片
						BiaoQing biaoqing = new BiaoQing();
						if (fromAssets) {
							root.setGravity(Gravity.TOP);
							root.setPadding(0, 80, 0, 0);
							biaoqing = assetslists.get(i + j * PAGE_NUM);
						}
						else {
							if (App.getChatBiaoqingOubaPath().equals(fileName)) {//欧巴表情
								biaoqing.setType(Type.OUBA);
							}
							else if (App.getChatBiaoqingCiweiPath().equals(fileName)) {
								biaoqing.setType(Type.CIWEI);
							}
							biaoqing.setBitmap(null);
							biaoqing.setImagePath(filedatas.get(i + j * PAGE_NUM));
						}
						imageAdapter.data.add(biaoqing);
					}
				}
				else {
					for (int i = 0; i < PAGE_NUM; i++) {//其他页面的加载
						//从资源文件下加载图片
						BiaoQing biaoqing = new BiaoQing();
						if (fromAssets) {
							biaoqing = assetslists.get(i + j * PAGE_NUM);
						}
						else {
							if (App.getChatBiaoqingOubaPath().equals(fileName)) {//欧巴表情
								biaoqing.setType(Type.OUBA);
							}
							else if (App.getChatBiaoqingCiweiPath().equals(fileName)) {
								biaoqing.setType(Type.CIWEI);
							}
							biaoqing.setBitmap(null);
							biaoqing.setImagePath(filedatas.get(i + j * PAGE_NUM));
						}
						imageAdapter.data.add(biaoqing);
					}
				}
				adapter.datas.add(view);
			}
			adapter.notifyDataSetChanged();
			container.addView(viewPager);
		}
	}

	File file = null;
	String url = null;
	String path = null;
	Download d = null;

	private View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (defaultView.getText().equals(DownLoadEmojiView.DOWNLOAD)) {// 下载表情
				defaultView.setText(DownLoadEmojiView.DOWNLOADING);
				defaultView.setButtonUnClick();

				if (DownLoadEmojiView.BIAOQING_TYPE.CIWEI.equals(defaultView.getType())) {// 刺猬表情
					path = App.getChatBiaoqingCiweiPath();
					file = new File(path, "2.zip");
					url = Constants.CHAT_BIAOQING_CIWEI;
				}
				else if (DownLoadEmojiView.BIAOQING_TYPE.OUBA.equals(defaultView.getType())) {// 欧巴表情
					path = App.getChatBiaoqingOubaPath();
					file = new File(path, "1.zip");
					url = Constants.CHAT_BIAOQING_OUBA;
				}

				try {
					if (!file.getParentFile().exists())
						file.getParentFile().mkdirs();
					if (file.exists())
						file.delete();

					file.createNewFile();
				}
				catch (IOException e) {
					e.printStackTrace();
				}

				DownLoadProgressListener listener = new DownLoadProgressListener();
				d = new Download(url, file.getAbsolutePath(), listener);
				d.start();
			}
		}
	};

	private class DownLoadProgressListener implements ProgressListener {

		@Override
		public void onProgressUpdate(long progress, long total) {
			if (progress < total) {
				ELog.i("下载的进度：" + String.valueOf(progress));
				defaultView.setDownLoadProgress((int) progress, (int) total);
			}
			else if (progress != 0 && total != 0 && progress == total) {//下载完成
				ELog.i("下载完成");
				//1.将文件解压缩
				try {
					ApacheZip.readByApacheZipFile(file.getAbsolutePath(), path);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				//2.删除zip包
				if (file != null && file.exists()) {
					file.delete();
				}

				//3.修改文件后缀
				DownLoadUtil.renameFiles(path);

				handler.sendMessage(handler.obtainMessage(EmojiView.HANDLER_DOWNLOAD_SUCCESS, path));
			}
		}

		@Override
		public void onException(Exception e) {

		}

	}

}

package com.oumen.widget.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.oumen.R;
import com.oumen.app.BaseApplication;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.oumen.widget.preview.image.ImagePagerActivity;
import com.oumen.widget.preview.image.ImagePagerFragment;

public class PickImageFragment extends Fragment implements Handler.Callback {
	public static final String PARAM_KEY_DIR = "dir";
	public static final String PARAM_KEY_MAX = "max";
	public static final String PARAM_KEY_SELECTS = "selects";
	public static final String PARAM_KEY_SHOW_PREVIEW_BAR = "preview";
	public static final String PARAM_KEY_PREVIEW_BAR_TIP = "preview_tip";
	public static final String PARAM_KEY_PREVIEW_BAR_COLOR = "preview_color";
	public static final String PARAM_KEY_PREVIEW_BUTTON_BG_RESID = "preview_btn_bg";
	public static final String PARAM_KEY_FINISH_BUTTON_BG_RESID = "finish_btn_bg";
	
	public static final int ACTIVITY_REQUEST_CODE = 1234;
	
	private final int HANDLER_SCAN_COMPLETED = 0;

	private final ArrayList<ImageData> paths = new ArrayList<ImageData>();

	private final Handler handler = new Handler(this);

	private final GridAdapter gridAdapter = new GridAdapter();

	private String dir;
	private int max;
	private boolean isShowPreviewBar;

	private View progressContainer;
	private GridView lstMain;
	private View previewBar;
	private TextView txtTip;
	private Button btnPreview;
	private Button btnFinish;

	private int gridItemSize;
	
	private int selectCount;
	
	private Callback callback;

	private final Runnable taskScan = new Runnable() {

		@Override
		public void run() {
			List<String> tmp = Scanner.scanImageFiles(getActivity(), dir);
			int size = tmp.size();
			LinkedList<ImageData> list = new LinkedList<ImageData>();
			String[] selects = getArguments().getStringArray(PARAM_KEY_SELECTS);
			for (int i = 0; i < size; i++) {
				String path = tmp.get(i);
				
				if (path.startsWith("/")) {
					path = BaseApplication.SCHEMA_FILE + path;
				}
				
				boolean selected = false;
				if (selects != null) {
					for (int j = 0; j < selects.length; j++) {
						if (selects[j].equals(path)) {
							selectCount++;
							selected = true;
							break;
						}
					}
				}
				
				ImageData imageData = new ImageData(path, i);
				imageData.select = selected;
				list.add(imageData);
			}
			paths.addAll(list);

			handler.sendEmptyMessage(HANDLER_SCAN_COMPLETED);
		}
	};
	
	private View.OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v instanceof SelectableImageView) {
				SelectableImageView view = (SelectableImageView)v;
				ImageData itemData = view.getData();
				if (!itemData.select && max != BaseApplication.INT_UNSET && selectCount >= max) {
					return;
				}
				
				itemData.select = !itemData.select;
				if (itemData.select) {
					selectCount++;
				}
				else {
					selectCount--;
				}
				// TODO 已选择照片数量
				txtTip.setText("已选择" + selectCount + "/" + max + "张图片");
				view.update(itemData);
			}
			else if (callback == null) {
				return;
			}
			
			if (v == btnPreview) {
				Bundle params = new Bundle(); 
				params.putInt(ImagePagerFragment.PARAMS_KEY_START_INDEX, 0);
				params.putInt(ImagePagerFragment.PARAMS_KEY_SELECT_COUNT, 0);
				params.putInt(ImagePagerFragment.PARAMS_KEY_SELECT_MAX, max);
				params.putBoolean(ImagePagerFragment.PARAMS_KEY_CLOSEABLE, true);
				params.putBoolean(ImagePagerFragment.PARAMS_KEY_SELECTABLE, true);
				params.putSerializable(ImagePagerFragment.PARAMS_KEY_DATA, paths);
				Intent intent = new Intent(getActivity().getApplicationContext(), ImagePagerActivity.class);
				intent.putExtras(params);
				startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
			}
			else if (v == btnFinish) {
				callback.onComplete(getSelects());
			}
		}
	};
	
	private final BitmapProcessor preBitmapProcessor = new BitmapProcessor() {
		
		@Override
		public Bitmap process(Bitmap bitmap) {
			Bitmap img = ImageTools.clip2square(bitmap);
			return img;
		}
	};
	
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.image_default)
		.showImageOnFail(R.drawable.image_default)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.delayBeforeLoading(500)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.preProcessor(preBitmapProcessor)
		.build();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		dir = savedInstanceState.getString(PARAM_KEY_DIR);
		max = savedInstanceState.getInt(PARAM_KEY_MAX, BaseApplication.INT_UNSET);
		isShowPreviewBar = savedInstanceState.getBoolean(PARAM_KEY_SHOW_PREVIEW_BAR, true);
		ELog.i("Dir:" + dir);

		float density = getResources().getDisplayMetrics().density;

		int gap = (int) (3 * density);
		gridItemSize = (getResources().getDisplayMetrics().widthPixels - gap) / 4;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(PARAM_KEY_MAX, max);
		outState.putString(PARAM_KEY_DIR, dir);
		outState.putBoolean(PARAM_KEY_SHOW_PREVIEW_BAR, isShowPreviewBar);
		outState.putString(PARAM_KEY_PREVIEW_BAR_TIP, txtTip.getText().toString());
		
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.img_picker, null);
		
		progressContainer = root.findViewById(R.id.progress_container);

		lstMain = (GridView) root.findViewById(R.id.list);
		lstMain.setColumnWidth(gridItemSize);
		lstMain.setAdapter(gridAdapter);

		previewBar = root.findViewById(R.id.preview_bar);
		previewBar.setVisibility(isShowPreviewBar ? View.VISIBLE : View.GONE);
		
		txtTip = (TextView) root.findViewById(R.id.tip);
		
		btnPreview = (Button) root.findViewById(R.id.preview);
		btnPreview.setOnClickListener(clickListener);
		
		btnFinish = (Button) root.findViewById(R.id.finish);
		btnFinish.setOnClickListener(clickListener);

		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		String tip = savedInstanceState.getString(PARAM_KEY_PREVIEW_BAR_TIP);
		if (TextUtils.isEmpty(tip)) {
			txtTip.setText(null);
			txtTip.setVisibility(View.GONE);
		}
		else {
			txtTip.setText(tip);
			txtTip.setVisibility(View.VISIBLE);
		}
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (paths.isEmpty()) {
			BaseApplication.THREAD.execute(taskScan);
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == HANDLER_SCAN_COMPLETED) {
			progressContainer.setVisibility(View.GONE);
			gridAdapter.notifyDataSetChanged();
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ELog.i("Request:" + requestCode);
		if (requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			int[] results = data.getIntArrayExtra(ImagePagerActivity.ACTIVITY_RESULT_DATA);
			selectCount = 0;
			for (ImageData i : paths) {
				if (Arrays.binarySearch(results, i.index) >= 0) {
					i.select = true;
					selectCount++;
				}
				else {
					i.select = false;
				}
			}
			gridAdapter.notifyDataSetChanged();
		}
	}

	private class GridAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return paths.size();
		}

		@Override
		public Object getItem(int position) {
			return paths.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageData data = paths.get(position);

			ImageView imgView;
			SelectableImageView item;
			if (convertView == null) {
				item = new SelectableImageView(parent.getContext());
				item.setLayoutParams(new AbsListView.LayoutParams(gridItemSize, gridItemSize));
				item.setOnClickListener(clickListener);
				
				imgView = item.getImageView();
				ViewGroup.LayoutParams params = imgView.getLayoutParams();
				params.width = params.height = gridItemSize;
				imgView.setLayoutParams(params);
			}
			else {
				item = (SelectableImageView) convertView;
				imgView = item.getImageView();
			}
			item.update(data);
			
			ImageLoader.getInstance().displayImage(paths.get(position).path, item.getImageView(), options);

			return item;
		}
	}
	
	public String[] getSelects() {
		ArrayList<String> tmp = new ArrayList<String>(selectCount);
		int count = paths.size();
		for (int i = 0; i < count; i++) {
			ImageData data = paths.get(i);
			if (data.select) {
				tmp.add(data.path);
			}
			if (tmp.size() == selectCount)
				break;
		}
		String[] results = new String[selectCount];
		tmp.toArray(results);
		return results;
	}
	
	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	
	public interface Callback {
		public void onPreview(ArrayList<ImageData> data);
		
		public void onComplete(String[] selects);
	}
}

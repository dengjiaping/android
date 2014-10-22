package com.oumen.mv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.mv.MvActivity.FragmentType;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;

public class MvVideoEditFragment extends BaseFragment implements FragmentInfo {
	public static final String PARAM_KEY_SOURCE = "source";
	
	private final int AXIS_MAX = 8;
	
	private final List<Bitmap> axisData = new ArrayList<Bitmap>(AXIS_MAX);
	
	private ImageButton btnStart;
	private ImageButton btnEnd;
	private GridView axis;
	
	private TextView txtTotal;// 视频总长度
	private TextView txtStart;// 开始时间
	private TextView txtEnd;// 结束时间

	private VideoPlayerView player;// 视频预览

	private MediaMetadataRetriever meta = null;
	private BaseAdapter adapter = new BaseAdapterImpl();

	private int width;
	private int height;
	private int axisItemSize;
	private int axisButtonHeight;

	private int startTime;// 开始剪辑的时间 毫秒
	private int endTime;// 结尾剪辑时间 毫秒
	private int totalTime; // 视频总的时间长度，毫秒
	
	private String source;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MvActivity host = (MvActivity) getActivity();
		
		if (savedInstanceState != null) {
			source = savedInstanceState.getString(PARAM_KEY_SOURCE);
		}
		else {
			source = host.selectedLocalVideo;
		}

		int padding = getResources().getDimensionPixelSize(R.dimen.default_big_gap);
		width = getResources().getDisplayMetrics().widthPixels - padding * 2;
		height = width / AXIS_MAX;
		axisItemSize = width / AXIS_MAX;
		axisButtonHeight = axisItemSize + getResources().getDimensionPixelSize(R.dimen.padding_small) * 2;
		ELog.i("Axis:" + width + "/" + height + " Item:" + axisItemSize + " Button:" + axisButtonHeight);
		
		meta = new MediaMetadataRetriever();
		meta.setDataSource(source);

		// 视频时间长度
		totalTime = Integer.parseInt(meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));// 毫秒
		endTime = totalTime;
		ELog.i("Total:" + totalTime);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.mv_video_editor, container, false);

		btnStart = (ImageButton) view.findViewById(R.id.btn_start);
		btnEnd = (ImageButton) view.findViewById(R.id.btn_end);
		
		ViewGroup.LayoutParams params = btnStart.getLayoutParams();
		params.height = axisButtonHeight;
		btnStart.setLayoutParams(params);
		params = btnEnd.getLayoutParams();
		params.height = axisButtonHeight;
		btnEnd.setLayoutParams(params);

		txtTotal = (TextView) view.findViewById(R.id.tv_totaltime);

		axis = (GridView) view.findViewById(R.id.axis);
		axis.setColumnWidth(axisItemSize);
		axis.setEnabled(false);
		axis.setAdapter(adapter);
		params = axis.getLayoutParams();
		params.width = width;
		params.height = axisItemSize;
		axis.setLayoutParams(params);

		txtStart = (TextView) view.findViewById(R.id.tv_starttime);
		txtStart.setText("00:00");
		txtEnd = (TextView) view.findViewById(R.id.tv_endtime);

		player = (VideoPlayerView) view.findViewById(R.id.player);

		btnStart.setOnTouchListener(touchListener);
		btnEnd.setOnTouchListener(touchListener);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		String timestr = dateFormat(totalTime);

		txtTotal.setText(timestr);
		txtEnd.setText(timestr);
		
		player.setVideo(source, null);
		
		App.THREAD.execute(new Runnable() {
			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(1);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				int unit = totalTime / AXIS_MAX;
				for (int i = 0; i < AXIS_MAX; i++) {
					Bitmap bmp = meta.getFrameAtTime(unit * i * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
					axisData.add(ImageTools.scale(bmp, axisItemSize, axisItemSize));
				}
				handler.sendEmptyMessage(0);
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(PARAM_KEY_SOURCE, source);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onDestroy() {
		meta.release();
		super.onDestroy();
	}

	private class BaseAdapterImpl extends BaseAdapter {

		@Override
		public int getCount() {
			return axisData.size();
		}

		@Override
		public Object getItem(int position) {
			return axisData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView item = null;
			if (convertView == null) {
				item = new ImageView(parent.getContext());
				item.setScaleType(ScaleType.FIT_XY);
			}
			else {
				item = (ImageView) convertView;
			}
			item.setImageBitmap(axisData.get(position));
			return item;
		}
		
	}
	
	private View.OnTouchListener touchListener = new View.OnTouchListener() {
		float lastX;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = event.getRawX();
					break;
					
				case MotionEvent.ACTION_MOVE:
					int offset = (int)(event.getRawX() - lastX);
					lastX = event.getRawX();
					
					if (v == btnStart) {
						FrameLayout.LayoutParams p = (FrameLayout.LayoutParams)btnEnd.getLayoutParams();
						int x = width - (p.rightMargin + btnEnd.getWidth());
						int targetX = params.leftMargin + offset;
						if (targetX >= x) {
							break;
						}
						else if (targetX < 0) {
							params.leftMargin = 0;
							break;
						}
						params.leftMargin += offset;
					}
					else {
						FrameLayout.LayoutParams p = (FrameLayout.LayoutParams)btnStart.getLayoutParams();
						int x = width - (params.rightMargin + v.getWidth() + offset * -1);
						if (x <= p.leftMargin) {
							break;
						}
						else if (x + v.getWidth() > width) {
							params.rightMargin = 0;
							break;
						}
						params.rightMargin += offset * -1;
					}
					v.setLayoutParams(params);
					break;
					
				case MotionEvent.ACTION_UP:
					if (v == btnStart) {
						startTime = totalTime * params.leftMargin / width;
						// 根据当前时间点切一帧做为videoview的背景图
						player.seetTo(startTime);
						player.setCover(startTime);
						int duration = endTime - startTime;
						txtTotal.setText(dateFormat(duration));

						txtStart.setText(dateFormat(startTime));
					}
					else if (v == btnEnd) {
						endTime = totalTime * (width - params.rightMargin) / width;
						// 根据当前时间点切一帧做为videoview的背景图
						player.setCover(endTime);
						txtEnd.setText(dateFormat(endTime));
						int duration = endTime - startTime;
						txtTotal.setText(dateFormat(duration));
					}
					ELog.i("Start:" + startTime + " End:" + endTime);
					break;
			}
			return true;
		}
	};

	@Override
	public boolean handleMessage(android.os.Message msg) {
		adapter.notifyDataSetChanged();
		return false;
	};

	// 时间格式转换
	private String dateFormat(int time) {// 毫秒
		time /= 1000;
		int minute = time / 60;
		int second = time % 60;
		return String.format(App.LOCALE, "%02d:%02d", minute, second);
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	@Override
	public FragmentType getFragmentType() {
		return FragmentType.VIDEO_EDIT;
	}
}

package com.oumen.mv;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.app.BaseApplication;
import com.oumen.mv.MvActivity.FragmentType;
import com.oumen.tools.ELog;
import com.oumen.widget.file.Scanner;

public class ComposeAndPickAudioFragment extends BaseFragment implements FragmentInfo {
	private final int HANDLER_UPADTA_MUSIC = 1;
	private final int HANDLER_UPADTA_PROGRESS = 2;
	private final int HANDLER_COMPOSE_COMPLETE = 3;

	private final int COMPOSE_DURATION_FOR_COMPLETE = 160;
	private final int COMPOSE_DURATION_FOR_WAIT = 150;
	private final int COMPOSE_UPDATE_TIME = 1000;
	private final AtomicInteger currentProgress = new AtomicInteger(0);
	
	protected final MediaPlayer player = new MediaPlayer();
	
	protected final ArrayList<String> audioData = new ArrayList<String>();
	protected final ArrayList<String> audioNames = new ArrayList<String>();
	protected final BaseAdapter adapter = new BaseAdapterImpl();
	
	private MvActivity host;
	
	private View composeProgressContainer;
	private TextView txtPrecent;
	private TextView txtComposeTip;
	private ProgressBar pgsCompose;
	private VideoPlayerView playerView;
	
	private View listContainer;
	private View progressContainer;
	private ListView list;
	
	protected int selectIndex = App.INT_UNSET;
	protected int playIndex = App.INT_UNSET;
	
	private int colorSelected;
	
	protected boolean isComposing;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		host = (MvActivity) getActivity();
		
		colorSelected = getResources().getColor(R.color.list_selected);
		
		isComposing = true;
		host.compose();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int width = getResources().getDisplayMetrics().widthPixels;
		int height = width * 3 / 4;
		
		View root = inflater.inflate(R.layout.compose_and_pick_audio, null);
		composeProgressContainer = root.findViewById(R.id.compose_progress_container);
		
		View topContainer = root.findViewById(R.id.top_container);
		ViewGroup.LayoutParams params = topContainer.getLayoutParams();
		params.width = width;
		params.height = height;
		topContainer.setLayoutParams(params);
		
		listContainer = root.findViewById(R.id.list_container);
		FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) listContainer.getLayoutParams();
		frameParams.topMargin = height;
		listContainer.setLayoutParams(frameParams);
		
		progressContainer = root.findViewById(R.id.progress_container);
		
		txtPrecent = (TextView) root.findViewById(R.id.precent);
		txtComposeTip = (TextView) root.findViewById(R.id.compose_tip);
		pgsCompose = (ProgressBar) root.findViewById(R.id.progress);
		pgsCompose.setMax(COMPOSE_DURATION_FOR_COMPLETE);
		playerView = (VideoPlayerView) root.findViewById(R.id.player);
		playerView.setWidthAndHeight(width, height);
		list = (ListView) root.findViewById(R.id.list);
		list.setAdapter(adapter);
		
		host.registerReceiver(receiver, receiverFilter);
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if (!audioData.isEmpty()) {
			progressContainer.setVisibility(View.GONE);
			return;
		}
		
		BaseApplication.THREAD.execute(new Runnable() {
			
			@Override
			public void run() {
				List<String> paths = Scanner.scanAudioFiles(getActivity());
				if (paths != null) {
					audioData.addAll(paths);
					LinkedList<String> tmp = new LinkedList<String>();
					for (String path : paths) {
						tmp.add(path.substring(path.lastIndexOf(File.separatorChar) + 1));
					}
					audioNames.addAll(tmp);
				}
				handler.sendEmptyMessage(HANDLER_UPADTA_MUSIC);
			}
		});
	}

	@Override
	public void onDestroyView() {
		host.unregisterReceiver(receiver);
		
		if (playIndex != App.INT_UNSET) {
			player.stop();
			player.reset();
			player.release();
		}
		playIndex = App.INT_UNSET;
		selectIndex = App.INT_UNSET;
		super.onDestroyView();
	}
	
	public String getSelect() {
		return selectIndex == App.INT_UNSET ? null : audioData.get(selectIndex);
	}
	
	private final View.OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Item item = (Item)v;
			selectIndex = item.index;
			
			try {
				if (playIndex == item.index) {
					player.stop();
					player.reset();
					
					playIndex = App.INT_UNSET;
				}
				else {
					if (playIndex != App.INT_UNSET) {
						player.stop();
						player.reset();
					}
					player.setDataSource(audioData.get(item.index));
					player.prepare();
					player.start();
					
					playIndex = item.index;
				}
				
				adapter.notifyDataSetChanged();
			}
			catch (Exception e) {
				e.printStackTrace();
				
				ELog.e("Exception:" + e.getMessage());
			}
			
			adapter.notifyDataSetChanged();
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		// TODO handle
		if (msg.what == HANDLER_UPADTA_MUSIC) {
			progressContainer.setVisibility(View.GONE);
			adapter.notifyDataSetChanged();
		}
		else if (msg.what == HANDLER_UPADTA_PROGRESS) {
			if (currentProgress.get() == App.INT_UNSET) {
				txtPrecent.setText(R.string.mv_compose_err_compose);
				txtPrecent.setTextColor(Color.RED);
				isComposing = false;
			}
			else if (currentProgress.get() <= COMPOSE_DURATION_FOR_WAIT) {
				updateProgress(currentProgress.incrementAndGet());
				handler.sendEmptyMessageDelayed(HANDLER_UPADTA_PROGRESS, COMPOSE_UPDATE_TIME);
			}
			else {
				txtComposeTip.setText("马上就要合成好啦，不要退出喔～");
				updateProgress(currentProgress.get());
			}
		}
		else if (msg.what == HANDLER_COMPOSE_COMPLETE) {
			composeProgressContainer.setVisibility(View.GONE);
			playerView.setVideo(host.previewFile.getAbsolutePath(), host.data.prefix.coverFile.getAbsolutePath());
			playerView.setVisibility(View.VISIBLE);
			isComposing = false;
		}
		return false;
	}
	
	private void updateProgress(int progress) {
		pgsCompose.setProgress(progress);
		int tmp = (int) ((float) progress / COMPOSE_DURATION_FOR_COMPLETE * 100);
		txtPrecent.setText(tmp + "%");
	}

	public final IntentFilter receiverFilter = new IntentFilter(MvComposeService.MV_COMPOSE_SERVICE_NOTIFY_ACTION);

	public final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(MvComposeService.INTENT_KEY_NOTIFY_TYPE, -1);
			int max = intent.getIntExtra(MvComposeService.INTENT_KEY_NOTIFY_MAX, App.INT_UNSET);
			int current = intent.getIntExtra(MvComposeService.INTENT_KEY_NOTIFY_CURRENT, App.INT_UNSET);

			ELog.i("Notify:" + type + " Progress:" + current + "/" + max);

			switch (type) {
				case MvComposeService.NOTIFY_COMPLETED:
					App.ring(host);
					currentProgress.set(COMPOSE_DURATION_FOR_COMPLETE);
					
					handler.sendEmptyMessageDelayed(HANDLER_COMPOSE_COMPLETE, COMPOSE_UPDATE_TIME);
					break;
				case MvComposeService.NOTIFY_INIT_FAILED:
				case MvComposeService.NOTIFY_COMPOSE_FAILED:
					currentProgress.set(App.INT_UNSET);
					break;
					
				case MvComposeService.NOTIFY_BUILD_COMMANDS:
					host.previewFile = new File(intent.getStringExtra(MvActivity.PARAM_KEY_PREVIEW_FILE));
					break;

				case MvComposeService.NOTIFY_INIT_LIBRARY:
					currentProgress.set(0);
					txtPrecent.setTextColor(Color.BLACK);
					pgsCompose.setVisibility(View.VISIBLE);
					handler.sendEmptyMessageDelayed(HANDLER_UPADTA_PROGRESS, COMPOSE_UPDATE_TIME);
					break;
				case MvComposeService.NOTIFY_PROGRESS:
				case MvComposeService.NOTIFY_INIT_RESOURCE:
				case MvComposeService.NOTIFY_PROCESS_IMAGES:
					break;
			}
		}
	};

	private class BaseAdapterImpl extends BaseAdapter {

		@Override
		public int getCount() {
			return audioData.size();
		}

		@Override
		public Object getItem(int position) {
			return audioData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Item item;
			if (convertView == null) {
				item = new Item(parent.getContext());
			}
			else {
				item = (Item)convertView;
			}
			
			item.update(position);
			return item;
		}
		
	}
	
	private class Item extends LinearLayout {
		ImageView icon;
		TextView title;
		int index;

		public Item(Context context) {
			this(context, null, 0);
		}

		public Item(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.pick_audio_list_item, this, true);
			icon = (ImageView)findViewById(R.id.icon);
			title = (TextView)findViewById(R.id.title);
			
			setOnClickListener(clickListener);
		}
		
		void update(int index) {
			this.index = index;
			icon.setImageResource(playIndex == index ? R.drawable.icon_audio_play : R.drawable.icon_audio_pause);
			icon.setTag(index);
			title.setTextColor(selectIndex == index ? colorSelected : Color.BLACK);
			title.setText(audioNames.get(index));
			setBackgroundColor(selectIndex == index ? Color.WHITE : Color.TRANSPARENT);
		}
	}

	@Override
	public FragmentType getFragmentType() {
		return FragmentType.COMPOSE_AND_PICK_AUDIO;
	}
}

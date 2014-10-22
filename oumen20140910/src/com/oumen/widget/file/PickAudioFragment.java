package com.oumen.widget.file;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.app.BaseApplication;
import com.oumen.app.callback.SelectCallback;
import com.oumen.tools.ELog;

public class PickAudioFragment extends Fragment implements Handler.Callback {
	private final int UNSELECT_INDEX = -1;
			
	public static final String PARAM_KEY_SELECT_INDEX = "select";
	
	protected int colorSelected;
	
	protected int itemPadding;
	protected int itemGap;
	
	protected int selectIndex = UNSELECT_INDEX;
	protected int playIndex = UNSELECT_INDEX;
	
	protected final ArrayList<String> data = new ArrayList<String>();
	protected final ArrayList<String> names = new ArrayList<String>();
	protected final BaseAdapter adapter = new BaseAdapterImpl();
	
	protected final Handler handler = new Handler(this);
	
	protected ViewGroup root;
	protected ListView lst;
	protected ProgressBar progress;
	
	protected SelectCallback<String> selectCallback;
	
	protected final MediaPlayer player = new MediaPlayer();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		if (savedInstanceState != null) {
			selectIndex = savedInstanceState.getInt(PARAM_KEY_SELECT_INDEX, UNSELECT_INDEX);
		}
		
		colorSelected = 0xFFAF26CF;
		
		itemPadding = getResources().getDimensionPixelSize(R.dimen.pick_audio_item_padding);
		itemGap = getResources().getDimensionPixelSize(R.dimen.pick_audio_item_gap);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		root = (ViewGroup)inflater.inflate(R.layout.audio_picker, null);
		
		lst = (ListView)root.findViewById(R.id.list);
		lst.setAdapter(adapter);
		
		progress = (ProgressBar)root.findViewById(R.id.progress);
		
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if (!data.isEmpty()) {
			showList();
			return;
		}
		
		BaseApplication.THREAD.execute(new Runnable() {
			
			@Override
			public void run() {
				List<String> paths = Scanner.scanAudioFiles(getActivity());
				if (paths != null) {
					data.addAll(paths);
					LinkedList<String> tmp = new LinkedList<String>();
					for (String path : paths) {
						tmp.add(path.substring(path.lastIndexOf(File.separatorChar) + 1));
					}
					names.addAll(tmp);
				}
				handler.sendEmptyMessage(0);
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(PARAM_KEY_SELECT_INDEX, selectIndex);
		super.onSaveInstanceState(outState);
	}

	private class BaseAdapterImpl extends BaseAdapter {

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
			icon.setImageResource(playIndex == index ? R.drawable.icon_audio_pause : R.drawable.icon_audio_play);
			icon.setTag(index);
			title.setTextColor(selectIndex == index ? colorSelected : Color.BLACK);
			title.setText(names.get(index));
		}
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
					
					playIndex = UNSELECT_INDEX;
				}
				else {
					if (playIndex != UNSELECT_INDEX) {
						player.stop();
						player.reset();
					}
					player.setDataSource(data.get(item.index));
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
			
			if (selectCallback != null)
				selectCallback.onSelect(getSelect());
		}
	};

	@Override
	public void onDestroyView() {
		if (playIndex != UNSELECT_INDEX) {
			player.stop();
			player.reset();
			player.release();
		}
		playIndex = UNSELECT_INDEX;
		selectIndex = UNSELECT_INDEX;
		View view = getView();
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null)
			parent.removeAllViews();
		super.onDestroyView();
	}

	public void setSelectCallback(SelectCallback<String> selectCallback) {
		this.selectCallback = selectCallback;
	}
	
	public String getSelect() {
		return selectIndex == UNSELECT_INDEX ? null : data.get(selectIndex);
	}
	
	private void showList() {
		lst.setVisibility(View.VISIBLE);
		progress.setVisibility(View.GONE);
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean handleMessage(Message msg) {
		showList();
		return false;
	}
}

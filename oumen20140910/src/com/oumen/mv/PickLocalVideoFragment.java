package com.oumen.mv;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.mv.MvActivity.FragmentType;
import com.oumen.mv.SelectableVideoView.VideoData;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.oumen.widget.file.Scanner;

public class PickLocalVideoFragment extends BaseFragment implements FragmentInfo {
	private final List<VideoData> data = new ArrayList<VideoData>();
	
	private final BaseAdapter adapter = new BaseAdapterImpl();
	
	private View progressContainer;
	private GridView grid;
	private int itemSize;
	private int imageSize;
	private int innerPadding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int padding = getResources().getDimensionPixelSize(R.dimen.padding_medium);
		itemSize = (getResources().getDisplayMetrics().widthPixels - padding * 5) / 3;
		innerPadding = getResources().getDimensionPixelSize(R.dimen.padding_micro);
		imageSize = itemSize - innerPadding * 2;
		
		App.THREAD.execute(scanTask);
	}
	
	private final Runnable scanTask = new Runnable() {
		
		@Override
		public void run() {
			LinkedList<VideoData> tmp = new LinkedList<VideoData>();
			List<String> paths = Scanner.scanVideoFiles(getActivity());
			for (String i : paths) {
				VideoData item = new VideoData();
				item.path = i;
				MediaMetadataRetriever meta = new MediaMetadataRetriever();
				meta.setDataSource(i);
				Bitmap bitmapSrc = meta.getFrameAtTime();
				if(bitmapSrc == null)
					continue;
				Bitmap bitmapSquare = ImageTools.clip2square(bitmapSrc);
				item.frame = ImageTools.scale(bitmapSquare, imageSize, imageSize);
				bitmapSquare.recycle();
				item.duration = Integer.parseInt(meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
				tmp.add(item);
			}
			data.clear();
			data.addAll(tmp);
			handler.sendEmptyMessage(0);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_local_video, null);
		progressContainer = view.findViewById(R.id.progress_container);
		grid = (GridView) view.findViewById(R.id.grid);
		grid.setColumnWidth(itemSize);
		grid.setNumColumns(3);
		grid.setAdapter(adapter);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.leftMargin = params.rightMargin = params.topMargin = params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.padding_medium);
		grid.setLayoutParams(params);
		return view;
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		progressContainer.setVisibility(View.GONE);
		adapter.notifyDataSetChanged();
		return false;
	}
	
	public String getSelect() {
		for (VideoData i : data) {
			if (i.selected)
				return i.path;
		}
		return null;
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			SelectableVideoView videoView = (SelectableVideoView)v;
			VideoData itemData = videoView.getData();
			ELog.i("Path:" + itemData.path + " Duration:" + itemData.duration);
			VideoData selected = null;
			for (VideoData i : data) {
				if (i.selected) {
					selected = i;
					break;
				}
			}
			if (selected == null) {
				itemData.selected = true;
			}
			else {
				if (!itemData.path.equals(selected.path)) {
					selected.selected = false;
				}
				itemData.selected = true;
			}
			adapter.notifyDataSetChanged();
		}
	};

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
			SelectableVideoView item = null;
			if (convertView == null) {
				item = new SelectableVideoView(parent.getContext());
				item.setOnClickListener(clickListener);
			}
			else {
				item = (SelectableVideoView)convertView;
			}
			item.update(data.get(position));
			return item;
		}
		
	}

	@Override
	public FragmentType getFragmentType() {
		return FragmentType.PICK_LOCAL_VIDEO;
	}
}

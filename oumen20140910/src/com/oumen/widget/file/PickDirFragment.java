package com.oumen.widget.file;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.app.callback.SelectCallback;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;

public class PickDirFragment extends Fragment implements Handler.Callback {
	private final ExecutorService thread = Executors.newSingleThreadExecutor();
	
	private final int DEFAULT_IMAGE_SIZE = 80;//dp
	private final int DEFAULT_ITEM_PADDING_HORIZONTAL = 10;//dp
	private final int DEFAULT_ITEM_PADDING_VERTICAL = 5;//dp
	
	private final List<Dir> data = new ArrayList<Dir>();
	private final BaseAdapter adapter = new BaseAdapterImpl();
	
	private final Handler handler = new Handler(this);
	
	private final Runnable taskScan = new Runnable() {
		
		@Override
		public void run() {
			Map<String, Dir> map = Scanner.scanImageDirs(getActivity());
			data.addAll(map.values());
			for (Dir dir : data) {
				String firstPath = Scanner.getFirstImage(getActivity(), dir.path);
				if (firstPath == null)
					break;
				Bitmap tmp;
				switch (dir.type) {
					case IMAGE:
						tmp = ImageTools.decodeFile(firstPath, App.IMAGE_LENGTH_SMALL);
						tmp = ImageTools.clip2square(tmp);
						tmp = ImageTools.scale(tmp, iconSize, iconSize);
						dir.icon = tmp;
						break;

					default:
						break;
				}
			}
			
			handler.sendEmptyMessage(0);
		}
	};
	
	private final AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (selectCallback != null) {
				selectCallback.onSelect(data.get(position));
			}
		}
	};
	
	private int iconSize;
	private int paddingHorizontal;
	private int paddingVertical;
	
	private SelectCallback<Dir> selectCallback;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		float density = getResources().getDisplayMetrics().density;
		
		iconSize = (int)(DEFAULT_IMAGE_SIZE * density);
		ELog.i("Icon Size:" + iconSize);
		
		paddingHorizontal = (int)(DEFAULT_ITEM_PADDING_HORIZONTAL * density);
		paddingVertical = (int)(DEFAULT_ITEM_PADDING_VERTICAL * density);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.dir_picker, null);
		ListView lst = (ListView)root.findViewById(R.id.list);
		lst.setAdapter(adapter);
		lst.setOnItemClickListener(itemClickListener);
		return root;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if (data.isEmpty()) {
			thread.execute(taskScan);
		}
	}

	@Override
	public void onDestroyView() {
		View view = getView();
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null)
			parent.removeAllViews();
		super.onDestroyView();
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
			Dir dir = data.get(position);
			DirInfoView item;
			if (convertView == null) {
				item = new DirInfoView(parent.getContext());
				item.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
			}
			else {
				item = (DirInfoView)convertView;
			}
			item.setIcon(dir.icon);
			item.setInfo1(dir.name);
			item.setInfo2(dir.count + "个");
			return item;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		adapter.notifyDataSetChanged();
		return false;
	}
	
	public void setSelectCallback(SelectCallback<Dir> selectCallback) {
		this.selectCallback = selectCallback;
	}
}

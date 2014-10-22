package com.oumen.mv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;

import com.oumen.Controller;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;
import com.oumen.widget.downloader.DownloadListener;
import com.oumen.widget.downloader.DownloadTask;

public class PickPrefixVIdeoController extends Controller<PickPrefixVideoFragment> implements Handler.Callback, DownloadListener {
	private final String CACHE_KEY = "prefixs";
	
	private final int HANDLER_UPDATE = 0;
	
	final AdapterImpl adapter = new AdapterImpl();
	final Handler handler = new Handler(this);
	
	PrefixVideo selected;

	public PickPrefixVIdeoController(PickPrefixVideoFragment host) {
		super(host);
	}
	
	void obtainList() {
		String cache = App.CACHE.read(CACHE_KEY);
		if (!TextUtils.isEmpty(cache)) {
			try {
				processData(cache, true);
			}
			catch (JSONException e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
			}
		}
		
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String res = result.getResult();
					ELog.i(res);
					
					processData(res, false);
					
					App.CACHE.save(CACHE_KEY, res);
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
			}

			@Override
			public void onException(ExceptionHttpResult result) {
			}
		});
		
		HttpRequest req = new HttpRequest(Constants.GET_MV_PREFIX, null, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
	}
	
	private void processData(String res, boolean isCache) throws JSONException {
		JSONObject root = new JSONObject(res);
		JSONArray array = root.getJSONArray("data");
		LinkedHashMap<String, List<PrefixVideo>> tmp = new LinkedHashMap<String, List<PrefixVideo>>();
		for (int i = 0; i < array.length(); i++) {
			JSONObject group = array.getJSONObject(i);
			String title = group.getString("title");
			JSONArray data = group.getJSONArray("data");
			for (int j = 0; j < data.length(); j++) {
				final PrefixVideo info = new PrefixVideo(data.getJSONObject(j), title);
				info.downloadListener = this;
				if (!isCache) {
					PrefixVideo p = PrefixVideo.query(info.id);
					if (p == null) {
						PrefixVideo.insert(info);
					}
					else {
						info.total = p.total;
					}
				}
				info.initialize();
				List<PrefixVideo> items = tmp.get(title);
				if (items == null) {
					items = new LinkedList<PrefixVideo>();
					tmp.put(title, items);
				}
				items.add(info);
				
				if (!info.coverFile.exists()) {
					App.THREAD.execute(new Runnable() {
						
						@Override
						public void run() {
							info.downloadCoverImage();
							handler.sendEmptyMessage(HANDLER_UPDATE);
						}
					});
				}
			}
		}
		
		List<PrefixVideoGroup> groups = new LinkedList<PrefixVideoGroup>();
		Set<Entry<String, List<PrefixVideo>>> entrys = tmp.entrySet();
		for (Entry<String, List<PrefixVideo>> i : entrys) {
			PrefixVideoGroup group = new PrefixVideoGroup(i.getValue().get(0).type, i.getKey());
			group.items.addAll(i.getValue());
			groups.add(group);
		}
		synchronized (adapter.dataSource) {
			adapter.dataSource.clear();
			adapter.dataSource.addAll(groups);
		}
		handler.sendEmptyMessage(HANDLER_UPDATE);
	}
	
	class AdapterImpl extends BaseExpandableListAdapter implements View.OnClickListener {
		final List<PrefixVideoGroup> dataSource = new ArrayList<PrefixVideoGroup>();

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			if (host.list.isGroupExpanded(position)) {
				host.list.collapseGroup(position);
			}
			else {
				host.list.expandGroup(position, true);
			}
		}

		@Override
		public int getGroupCount() {
			return dataSource.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return dataSource.get(groupPosition).items.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return dataSource.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return dataSource.get(groupPosition).items.get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return groupPosition * 100 + childPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			PrefixVideoGroup data = dataSource.get(groupPosition);
			PrefixGroupItem item = null;
			if (convertView == null) {
				int height = (int) (80 * parent.getResources().getDisplayMetrics().density);
				item = new PrefixGroupItem(parent.getContext());
				item.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, height));
				item.setOnClickListener(this);
			}
			else {
				item = (PrefixGroupItem) convertView;
			}
			item.setTag(groupPosition);
			item.update(data.type, data.title);
			return item;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			PrefixVideoChildItem item = null;
			if (convertView == null) {
				int height = (int) (100 * parent.getResources().getDisplayMetrics().density);
				item = new PrefixVideoChildItem(parent.getContext());
				item.setOnClickListener(clickListener);
				item.viewActionContainer.setOnClickListener(clickListener);
				item.pgs.setOnClickListener(clickListener);
				item.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, height));
			}
			else {
				item = (PrefixVideoChildItem) convertView;
			}
			item.update(dataSource.get(groupPosition).items.get(childPosition));
			return item;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}
	}
	
	private final View.OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v instanceof PrefixVideoChildItem) {
				PrefixVideoChildItem item = (PrefixVideoChildItem) v;
				if (item.data.state != PrefixVideo.STATE_COMPLETE)
					return;
				
				synchronized (adapter.dataSource) {
					PrefixVideo lastSelected = null;
					for (PrefixVideoGroup i : adapter.dataSource) {
						for (PrefixVideo j : i.items) {
							if (j.selected) {
								lastSelected = j;
							}
						}
					}
					
					if (lastSelected == item.data) {
						selected = null;
						item.data.selected = false;
					}
					else {
						if (lastSelected != null) {
							lastSelected.selected = false;
						}
						item.data.selected = true;
						selected = item.data;
					}
					adapter.notifyDataSetChanged();
				}
			}
			else if (v.getId() == R.id.action_container) {
				PrefixVideo info = (PrefixVideo) v.getTag();
				switch (info.state) {
					case PrefixVideo.STATE_DESCRIPTION:
						ELog.i("STATE_DOWNLOAD");
						info.state = PrefixVideo.STATE_DOWNLOAD;
						break;
						
					case PrefixVideo.STATE_DOWNLOAD:
					case PrefixVideo.STATE_DOWNLOADING:
						ELog.i("STATE_DOWNLOADING");
						info.state = PrefixVideo.STATE_DOWNLOADING;
						download(info);
						break;
				}
				adapter.notifyDataSetChanged();
			}
			else if (v.getId() == R.id.progress) {
				PrefixVideo info = (PrefixVideo) v.getTag();
				ELog.e("State:" + info.state);
				if (info.state == PrefixVideo.STATE_DOWNLOADING) {
					if (info.downloadTask.isDownloading()) {
						disconnect(info);
					}
					else {
						download(info);
					}
				}
			}
		}
	};
	
	private void download(PrefixVideo info) {
		info.state = PrefixVideo.STATE_DOWNLOADING;
		adapter.notifyDataSetChanged();
		App.THREAD.execute(info.downloadTask);
	}
	
	private void disconnect(PrefixVideo info) {
		info.downloadTask.disconnect();
		adapter.notifyDataSetChanged();
	}
	
	private Timer timer;
	private TimerTask timerTask;
	private final AtomicInteger downloadingCound = new AtomicInteger(0);
	
	void startTimer() {
		if (timer != null)
			return;
		
		timer = new Timer(true);
		timerTask = new TimerTask() {
			
			@Override
			public void run() {
				ELog.i("Count:" + downloadingCound.get());
				handler.sendEmptyMessage(HANDLER_UPDATE);
			}
		};
		timer.schedule(timerTask, 1000, 1000);
	}
	
	private void stopTimer() {
		ELog.i("");
		if (timer != null) {
			handler.sendEmptyMessage(HANDLER_UPDATE);
			timer.cancel();
			timer = null;
			timerTask = null;
		}
	}

	public void onDestroyView() {
		stopTimer();
		for (PrefixVideoGroup i : adapter.dataSource) {
			for (PrefixVideo j : i.items) {
				if (j.downloadTask.isDownloading()) {
					ELog.i("Disconnect:" + j.name);
					j.downloadTask.disconnect();
				}
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == HANDLER_UPDATE) {
			if (!adapter.isEmpty()) {
				host.progressContainer.setVisibility(View.GONE);
			}
			adapter.notifyDataSetChanged();
		}
		return false;
	}

	@Override
	public void onProgressUpdate(DownloadTask task) {}

	@Override
	public void onStart(DownloadTask task) {
		ELog.i("Count:" + downloadingCound.get());
		downloadingCound.addAndGet(1);
		startTimer();
	}

	@Override
	public void onCompleted(DownloadTask task) {
		downloadingCound.addAndGet(-1);
		ELog.i("Count:" + downloadingCound.get());
		if (downloadingCound.get() == 0) {
			stopTimer();
		}
	}

	@Override
	public void onDisconnect(DownloadTask task) {
		downloadingCound.addAndGet(-1);
		ELog.i("Count:" + downloadingCound.get());
		if (downloadingCound.get() == 0) {
			stopTimer();
		}
	}

	@Override
	public void onFailed(DownloadTask task) {
		downloadingCound.addAndGet(-1);
		ELog.i("Count:" + downloadingCound.get());
		if (downloadingCound.get() == 0) {
			stopTimer();
		}
	}
}

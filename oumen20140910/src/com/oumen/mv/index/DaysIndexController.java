package com.oumen.mv.index;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.oumen.Controller;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.auth.ShareView;
import com.oumen.mv.ComposeFragment;
import com.oumen.mv.MV;
import com.oumen.mv.MvActivity;
import com.oumen.mv.MvComposeService;
import com.oumen.mv.MvInfo;
import com.oumen.tools.ELog;

public class DaysIndexController extends Controller<DaysIndexFragment> implements UploadTask.UploadListener, Handler.Callback, View.OnClickListener {
	private Calendar start;
	
	final Handler handler = new Handler(this);
	final ListAdapterImpl adapterList = new ListAdapterImpl();
	
	final int[] gotoPage = new int[] {App.INT_UNSET, App.INT_UNSET};
	
	private ShareView viewShare;

	DaysIndexController(DaysIndexFragment host) {
		super(host);
		
	}
	
	public ShareView getShareView() {
		return viewShare;
	}

	void onCreate(Bundle savedInstanceState) {
		viewShare = new ShareView(host.getActivity());
		
		int[] date = App.PREFS.getMVDate();
		start = Calendar.getInstance();
		start.set(date[0], date[1], date[2], 0, 0, 0);
		start.set(Calendar.MILLISECOND, 0);
		
//		MvInfo tmp = MvInfo.query(0, "07201540", 3, App.DB);
//		tmp.setType(MvInfo.TYPE_UNPUBLISHED);
//		MvInfo.update(App.PREFS.getUid(), "07291658", MvInfo.TYPE_PUBLISHED, MvInfo.TYPE_UPLOAD, App.DB);
		
//		Calendar c = Calendar.getInstance();
//		c.set(Calendar.HOUR_OF_DAY, 18);
//		c.set(Calendar.MINUTE, 20);
//		c.set(Calendar.SECOND, 0);
//		c.set(Calendar.MILLISECOND, 0);
//		c.set(Calendar.DAY_OF_MONTH, 16);13910989294
//		MvInfo.insert(0, "07161820", 27, MvInfo.TYPE_UNPUBLISHED, c.getTimeInMillis(), App.DB);
	}

	void onViewCreated(View view, Bundle savedInstanceState) {
		host.getActivity().registerReceiver(receiver, receiverFilter);
	}
	
	void onStart() {
		buildListData();
	}

	void onDestroyView() {
		host.getActivity().unregisterReceiver(receiver);
	}
	
	void buildListData() {
		ELog.i("");
		
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		
		gotoPage[0] = gotoPage[1] = App.INT_UNSET;
		
		int count = 0;
		synchronized (adapterList) {
			adapterList.dataSource.clear();
			
			while (now.getTimeInMillis() >= start.getTimeInMillis()) {
				Calendar cal = (Calendar) now.clone();
				Day day = new Day(cal);
				day.today = count++ == 0;//如果count为0，则该日期为当天
				day.setUploadListener(this);
				adapterList.dataSource.add(day);
				now.add(Calendar.DAY_OF_MONTH, -1);

				int pages = day.list.size();
				for (int j = 0; j < pages; j++) {
					MvInfo info = day.list.get(j);
					if (info.getType() == MvInfo.TYPE_COMPOSE) {
						gotoPage[0] = adapterList.dataSource.size() - 1;
						gotoPage[1] = j;
					}
				}
			}
			adapterList.notifyDataSetChanged();
		}
	}

	public void make(Calendar createAt) {
		if (!host.dialogComposeSelect.isShowing()) {
			host.dialogComposeSelect.show();
		}
		host.dialogComposeSelect.setCreateAt(createAt);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		ELog.i("Request:" + requestCode + " Result:" + resultCode);
		if (requestCode == MvActivity.REQUEST_CODE_MV_ACTIVITY) {
			if (resultCode == Activity.RESULT_OK) {
				buildListData();
			}
		}
		if (viewShare != null) {
			viewShare.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	private class ListAdapterImpl extends BaseAdapter {
		final List<Day> dataSource = new ArrayList<Day>();

		@Override
		public int getCount() {
			return dataSource.size();
		}

		@Override
		public Object getItem(int position) {
			return dataSource.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Day itemData = dataSource.get(position);
			PagerView item = null;
			if (convertView == null) {
				item = new PagerView(parent.getContext());
				item.controller = DaysIndexController.this;
			}
			else {
				item = (PagerView) convertView;
			}
			item.update(itemData);
			
			if (gotoPage[0] == position) {
				//需要程序翻页时执行这里
				item.pager.setCurrentItem(gotoPage[1]);
				
				gotoPage[0] = gotoPage[1] = App.INT_UNSET;
			}
			return item;
		}
		
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.image) {
			Intent intent = new Intent(v.getContext(), MvActivity.class);
			intent.putExtra(MvActivity.PARAM_CREATE_AT, host.dialogComposeSelect.getCreateAt());
			intent.putExtra(ComposeFragment.PARAM_KEY_COMPOSE_TYPE, MV.ACTION_COMPOSE_WITH_IMAGES);
			host.startActivityForResult(intent, MvActivity.REQUEST_CODE_MV_ACTIVITY);
			host.dialogComposeSelect.dismiss();
		}
		else if (id == R.id.record) {
			Intent intent = new Intent(v.getContext(), MvActivity.class);
			intent.putExtra(MvActivity.PARAM_CREATE_AT, host.dialogComposeSelect.getCreateAt());
			intent.putExtra(ComposeFragment.PARAM_KEY_COMPOSE_TYPE, MV.ACTION_COMPOSE_WITH_RECORD);
			host.startActivityForResult(intent, MvActivity.REQUEST_CODE_MV_ACTIVITY);
			host.dialogComposeSelect.dismiss();
		}
		else if (id == R.id.local) {
			Intent intent = new Intent(v.getContext(), MvActivity.class);
			intent.putExtra(MvActivity.PARAM_CREATE_AT, host.dialogComposeSelect.getCreateAt());
			intent.putExtra(ComposeFragment.PARAM_KEY_COMPOSE_TYPE, MV.ACTION_COMPOSE_WITH_EDIT);
			host.startActivityForResult(intent, MvActivity.REQUEST_CODE_MV_ACTIVITY);
			host.dialogComposeSelect.dismiss();
		}
	}
	
	private final IntentFilter receiverFilter = new IntentFilter(MvComposeService.MV_COMPOSE_SERVICE_NOTIFY_ACTION);

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			ELog.i("Data:" + intent);
			
			if (intent.getAction().equals(MvComposeService.MV_COMPOSE_SERVICE_NOTIFY_ACTION)) {
				int type = intent.getIntExtra(MvComposeService.INTENT_KEY_NOTIFY_TYPE, -1);
				int max = intent.getIntExtra(MvComposeService.INTENT_KEY_NOTIFY_MAX, App.INT_UNSET);
				int current = intent.getIntExtra(MvComposeService.INTENT_KEY_NOTIFY_CURRENT, App.INT_UNSET);

				ELog.i("Notify:" + type + " Progress:" + current + "/" + max);

				switch (type) {
					case MvComposeService.NOTIFY_COMPLETED:
						App.ring(context);
						buildListData();
						break;
						
					case MvComposeService.NOTIFY_INIT_FAILED:
					case MvComposeService.NOTIFY_COMPOSE_FAILED:
						break;

					case MvComposeService.NOTIFY_PROGRESS:
					case MvComposeService.NOTIFY_INIT_LIBRARY:
					case MvComposeService.NOTIFY_INIT_RESOURCE:
					case MvComposeService.NOTIFY_PROCESS_IMAGES:
					case MvComposeService.NOTIFY_BUILD_COMMANDS:
						break;
				}
			}
		}
	};

	//---------------- UploadTask ----------------//
	@Override
	public void onPrepare(UploadTask task) {
		ELog.i("Name:" + task.getInfo().getTitle() + " Progress:" + task.getProgress() + "/" + task.getTotal());
		handler.sendEmptyMessage(0);
	}

	@Override
	public void onProgressUpdate(UploadTask task) {
		ELog.i("Name:" + task.getInfo().getTitle() + " Progress:" + task.getProgress() + "/" + task.getTotal());
		handler.sendEmptyMessage(0);
	}

	@Override
	public void onCancel(UploadTask task) {
		ELog.i("Name:" + task.getInfo().getTitle() + " Progress:" + task.getProgress() + "/" + task.getTotal());
		handler.sendEmptyMessage(0);
	}

	@Override
	public void onFailed(UploadTask task) {
		ELog.i("Name:" + task.getInfo().getTitle() + " Progress:" + task.getProgress() + "/" + task.getTotal());
		handler.sendEmptyMessage(0);
	}

	@Override
	public boolean handleMessage(Message msg) {
		synchronized (adapterList) {
			adapterList.notifyDataSetChanged();
		}
		return false;
	}
}

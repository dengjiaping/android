package com.oumen.peer;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.message.BaseMessage;
import com.oumen.message.CircleMessage;
import com.oumen.message.MessageService;
import com.oumen.message.SendType;
import com.oumen.tools.ELog;
import com.oumen.widget.dialog.TwoButtonDialog;
import com.oumen.widget.list.HSZFooterView;
import com.oumen.widget.list.HSZListView;
import com.oumen.widget.list.HSZListViewAdapter;

/**
 * 偶们圈消息列表界面
 * 
 */
public class OumenCircleNoticeListActivity extends BaseActivity {
	private final int HANDLER_GET_DETAIL_FAIL = 2;
	private final int HANDLER_DELETE_ALL_NOTICE = 3;
	private final int HANDLER_CHECK_MORE_NOTICE = 4;

	// 标题行
	private TitleBar titleBar;
	private Button btnLeft, btnRight;
	private TextView tvTitle;
	private FrameLayout framContainer;

	private HSZListView<CircleMessage> listView;
	private HSZFooterView footerView;
	private AdapterImpl adapter = new AdapterImpl();

	private int page = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oumen_circle_noticelist);
		
		titleBar = (TitleBar) findViewById(R.id.titlebar);
		btnLeft = titleBar.getLeftButton();
		btnRight = titleBar.getRightButton();
		btnRight.setText("清空");
		tvTitle = titleBar.getTitle();
		tvTitle.setText("与我相关");
		
		framContainer = (FrameLayout) findViewById(R.id.container);
		
		footerView = new HSZFooterView(this);

		listView = new HSZListView<CircleMessage>(this);
		listView.setDivider(new ColorDrawable(getResources().getColor(R.color.login_line)));
		listView.setDividerHeight(1);
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setSelector(R.drawable.list_white_default_selector);
		listView.setFooterView(footerView, getResources().getDimensionPixelSize(R.dimen.list_footer_height_default));
		listView.setAdapter(adapter);
		framContainer.addView(listView);

		btnLeft.setOnClickListener(clickListener);
		btnRight.setOnClickListener(clickListener);
		
		List<CircleMessage> tempList = CircleMessage.queryNews(App.PREFS.getUid(), App.DB);
		synchronized (adapter) {
			adapter.clear();
			if (tempList != null) {
				adapter.addAll(tempList);
			}
			adapter.notifyDataSetChanged();
		}
		//将消息改为已读
		CircleMessage.updateAllRead(App.PREFS.getUid(), App.DB);
	}

	public final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final ItemData itemData = (ItemData) v.getTag();
			if (v instanceof NoticeItem) {
				CircleMessage temp = adapter.get(itemData.groupIndex);
				Intent intent = new Intent(v.getContext(), OumenCircleDetailActivity.class);
				intent.putExtra(BaseMessage.KEY_TARGET_ID, temp.getCircleId());
				startActivityForResult(intent,1);
			}
			else if (v == btnLeft) {// 返回
				// 清空当前消息列表
				synchronized (adapter) {
					adapter.clear();
				}
				setResult(Activity.RESULT_OK);
				Intent notify = MessageService.createResponseNotify(MessageService.TYPE_CIRCLE_MESSAGE);
				sendBroadcast(notify);
				finish();
			}
			else if (v == btnRight) {// 清空
				final TwoButtonDialog tip = new TwoButtonDialog(OumenCircleNoticeListActivity.this);
				tip.getTitleView().setText("偶们提示");
				tip.getMessageView().setText("确定要清除消息通知？清除后将不可恢复！");
				tip.getMessageView().setGravity(Gravity.LEFT);
				tip.getRightButton().setText("确定");
				tip.getRightButton().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						deleteNoticeList();
						tip.dismiss();
					}
				});
				tip.getLeftButton().setText("取消");
				tip.getLeftButton().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						tip.dismiss();
					}
				});
				tip.show();
			}
		}
	};

	/**
	 * 加载更多偶们圈消息提醒
	 */
	private void checkMoreNoticeMessage(int page) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", String.valueOf(App.USER.getUid())));
		params.add(new BasicNameValuePair("page", String.valueOf(page)));
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_NOTICE_CHECK_MORE, params, HttpRequest.Method.GET, checkmoreCallback);
		App.THREAD.execute(req);
	}

	final DefaultHttpCallback checkmoreCallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				boolean hasNoData = true;
				String res = result.getResult();
				ELog.i(res.toString());
				
				JSONObject obj = new JSONObject(res);
				Object temps = obj.get("content");
				if (temps instanceof JSONArray) {
					JSONArray array = (JSONArray) temps;
					synchronized (adapter) {
						for (int i = 0; i < array.length(); i++) {
							CircleMessage temp = new CircleMessage(array.getJSONObject(i));
							// 如果data里没有对应的数据才添加
							ArrayList<CircleMessage> tempList = adapter.copyDataSource();
							boolean hasMsg = false;
							for(CircleMessage msg: tempList) {
								if (msg.getCircleId() == temp.getCircleId() && msg.getAboutId() == temp.getAboutId()) {
									hasMsg = true;
								}
							}
							if (!hasMsg) {
//								temp.setRead(true);
								temp.setSendType(SendType.READ);
								adapter.add(temp);
								CircleMessage.insert(temp, App.DB);
								hasNoData = false;
							}
						}
					}
					if (array.length() == 0 || hasNoData) {
						handler.sendMessage(handler.obtainMessage(HANDLER_GET_DETAIL_FAIL, "没有历史记录了"));
						return;
					}
					else {
						page++;
					}
					handler.sendEmptyMessage(HANDLER_CHECK_MORE_NOTICE);
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.toString());
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {}

		@Override
		public void onException(ExceptionHttpResult result) {}
	});

	/**
	 * 清空消息列表
	 */
	private void deleteNoticeList() {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_NOTICE_DELDTE, params, HttpRequest.Method.GET, deleteAllNoticeMessageCallback);
		App.THREAD.execute(req);
	}

	private final DefaultHttpCallback deleteAllNoticeMessageCallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String res = result.getResult();
				ELog.i(res);
				JSONObject obj = new JSONObject(res);
				String tip = obj.getString("succeed");
				if (tip.equals("1")) {
					// 2.清空当前消息列表
					handler.sendMessage(handler.obtainMessage(HANDLER_DELETE_ALL_NOTICE, "清空列表成功"));
				}
				else {
					handler.sendMessage(handler.obtainMessage(HANDLER_GET_DETAIL_FAIL, "清空列表失败"));
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.toString());
				e.printStackTrace();
				handler.sendMessage(handler.obtainMessage(HANDLER_GET_DETAIL_FAIL, "清空列表失败"));
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.handleMessage(handler.obtainMessage(HANDLER_GET_DETAIL_FAIL, "清空列表失败"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.handleMessage(handler.obtainMessage(HANDLER_GET_DETAIL_FAIL, "清空列表失败"));
		}
	});

	public boolean handleMessage(android.os.Message msg) {
		switch (msg.what) {
			case HANDLER_GET_DETAIL_FAIL:
				Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;

			case HANDLER_DELETE_ALL_NOTICE:// 请求清空列表成功
				// 清除本地对应的数据库
				synchronized (adapter) {
					adapter.clear();
					adapter.notifyDataSetChanged();
				}
				CircleMessage.delete(Integer.valueOf(App.USER.getUid()), App.DB);
				break;

			case HANDLER_CHECK_MORE_NOTICE:// 查看更多成功
				adapter.notifyDataSetChanged();
				break;
		}
		return false;
	};

	public static class ItemData {
		int groupIndex;
		CircleMessage noticeData;
	}
	
	@Override
	public void onBackPressed() {
		synchronized (adapter) {
			adapter.clear();
		}
		setResult(Activity.RESULT_OK);
		Intent notify = MessageService.createResponseNotify(MessageService.TYPE_CIRCLE_MESSAGE);
		sendBroadcast(notify);
		super.onBackPressed();
	}
	
	class AdapterImpl extends HSZListViewAdapter<CircleMessage> {

		@Override
		public void onFooterLoad() {
			checkMoreNoticeMessage(page);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NoticeItem item;
			ItemData itemData;
			if (convertView == null) {
				itemData = new ItemData();
				item = new NoticeItem(parent.getContext());
				item.setTag(itemData);
				item.setOnClickListener(clickListener);
			}
			else {
				item = (NoticeItem) convertView;
				itemData = (ItemData) item.getTag();
			}
			itemData.groupIndex = position;
			itemData.noticeData = get(position);
			item.update();
			return item;
		}
	}
}

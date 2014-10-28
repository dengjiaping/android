package com.oumen.activity.detail.comment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;
import com.oumen.widget.refushlist.AbOnListViewListener;
import com.oumen.widget.refushlist.AbPullListView;

/**
 * 活动评论界面
 * 
 * @author oumen-xin.zhang
 *
 */
public class CommentListFragment extends BaseFragment {
	private final String CACHE_KEY = "huodong_comments_";
	private final int HANDLER_COMMENT = 1;
	private TitleBar titleBar;
	private Button btnLeft;
	private AbPullListView lstView;
	private Button publishComment;
	
	private TextView rate, describle;

	private AdapterImp adapter = new AdapterImp();
	
	private int atId;
	private boolean isApply;
	private int total;
	private float currentRate;
	private int currentPage = 1;
	
	private CommentActivity host;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		atId = getArguments().getInt(CommentActivity.INTENT_HUODONG_ID);
		isApply = getArguments().getBoolean(CommentActivity.INTENT_HUODONG_APPLY);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.huodong_comment, container, false);
		
		host = (CommentActivity) getActivity();
		
		titleBar = (TitleBar) view.findViewById(R.id.titlebar);
		titleBar.getRightButton().setVisibility(View.GONE);
		titleBar.getTitle().setText("活动评价");
		btnLeft = titleBar.getLeftButton();
		btnLeft.setOnClickListener(clickListener);

		View header = inflater.inflate(R.layout.huodong_comment_header, null);
		rate = (TextView)header.findViewById(R.id.rate);
		rate.setTextSize(40);
		describle = (TextView) header.findViewById(R.id.describe);
		
		lstView = (AbPullListView) view.findViewById(R.id.listview);
		lstView.addHeaderView(header);
		lstView.setSelector(android.R.color.transparent);
		//打开关闭下拉刷新加载更多功能
		lstView.setPullRefreshEnable(false);
		lstView.setPullLoadEnable(true);

		lstView.getHeaderView().setHeaderProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		lstView.getFooterView().setFooterProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		
		lstView.setAbOnListViewListener(listviewListener);
		
		lstView.setAdapter(adapter);

		publishComment = (Button) view.findViewById(R.id.publish);
		publishComment.setOnClickListener(clickListener);
		
		if (isApply) {
			publishComment.setVisibility(View.VISIBLE);
		}
		else {
			publishComment.setVisibility(View.GONE);
		}
		
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initData();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	private void initData() {
		String str = App.CACHE.read(getCacheKey());
		if (TextUtils.isEmpty(str)) {
			getHuodongComments(1);
		}
		else {
			try {
				List<Comment> results = getJson(new JSONObject(str));
				handler.sendMessage(handler.obtainMessage(HANDLER_COMMENT, 1, 0, results));
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private List<Comment> getJson(JSONObject obj) {
		List<Comment> data = new ArrayList<Comment>();
		try {
			if (obj.has("data")) {
				JSONArray array = obj.getJSONArray("data");
				for (int i = 0; i < array.length(); i ++) {
					Comment comment = new Comment(array.getJSONObject(i));
					if (comment != null) {
						data.add(comment);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	private String getCacheKey() {
		return CACHE_KEY + atId;
	}

	private AbOnListViewListener listviewListener = new AbOnListViewListener() {

		@Override
		public void onRefresh() {
		}

		@Override
		public void onLoadMore() {
			getHuodongComments(currentPage + 1);
		}
	};

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				getActivity().finish();
			}
			else if (v == publishComment) {
				host.switchFragment(CommentActivity.FRAGMENT_COMMENT_PUBLISH);
			}
		}
	};
	
	
	public boolean handleMessage(android.os.Message msg) {
		switch (msg.what) {
			case HANDLER_COMMENT:
				synchronized (adapter) {
					if (msg.obj instanceof List<?>) {
						
						List<Comment> results = (List<Comment>) msg.obj;
						if (msg.arg1 == 1) {
							adapter.data.clear();
						}
						
						adapter.data.addAll(results);
						if (!results.isEmpty()) {
							currentPage = msg.arg1;
						}
						
						String str = App.RATE_FORMAT.format(currentRate) + "%";
						rate.setText(str);
						describle.setText("参加此活动的用户满意度" + str + ",已有" + total + "人评价");
						
						adapter.notifyDataSetChanged();
					}
					else if (msg.obj instanceof CharSequence) {
						Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
					}
					
					lstView.stopLoadMore();
				}
				break;

			default:
				break;
		}
		return false;
	};
	/**
	 * 获取活动评论列表
	 * @param page
	 */
	private void getHuodongComments(final int page) {
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);
					if (TextUtils.isEmpty(str) || "{}".equals(str)) {
						handler.sendMessage(handler.obtainMessage(HANDLER_COMMENT, "没有评论"));
					}
					else {
						JSONObject obj = new JSONObject(str);
						List<Comment> data = getJson(obj);
						total = obj.getInt("total");
						currentRate = (float) obj.getDouble("star") * 100;
						handler.sendMessage(handler.obtainMessage(HANDLER_COMMENT, page, 0, data));
					}

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
				handler.sendMessage(handler.obtainMessage(HANDLER_COMMENT, "网络异常"));
			}
		});
		
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("page", String.valueOf(page)));
		list.add(new BasicNameValuePair("atid", String.valueOf(atId)));
		
		HttpRequest req = new HttpRequest(Constants.GET_ACTIVITY_COMMENTS, list, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
	}
	

	
	private class AdapterImp extends BaseAdapter {
		private List<Comment> data = new ArrayList<Comment>();
		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Comment getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CommentItem item ;
			if (convertView == null) {
				item = new CommentItem(parent.getContext());
			}
			else {
				item = (CommentItem) convertView;
			}
			item.update(data.get(position));
			
			return item;
		}
	}
}

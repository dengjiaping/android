package com.oumen.activity.search;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.util.Constants;
import com.oumen.base.Cache;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;
import com.oumen.widget.editview.ClearEditText;

/**
 * 搜索活动界面
 * 
 * @author oumen-xin.zhang
 *
 */
public class SearchFragment extends BaseFragment {
	private final int HANDLER_OBTAIN_KEY_WORDS = 1;

	private SearchTitleBar titleBar;
	private Button btnLeft, btnRight;
	private ClearEditText input;
	private TextView hotContent;
	private LinearLayout historyContainer;
	private ListView listview;
	private TextView footerView;

	private AdapterImp adapter = new AdapterImp();

	private final ArrayList<String> keyWordsData = new ArrayList<String>();

	private SearchActivity host;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);

		host = (SearchActivity) getActivity();

		titleBar = (SearchTitleBar) view.findViewById(R.id.titlebar);
		input = titleBar.getTitle();
		input.setHint("输入关键字");
		btnRight = titleBar.getRightButton();
		btnRight.setText(getResources().getString(R.string.search));
		btnRight.setOnClickListener(clickListener);
		btnLeft = titleBar.getLeftButton();
		btnLeft.setOnClickListener(clickListener);

		hotContent = (TextView) view.findViewById(R.id.hot_content);

		historyContainer = (LinearLayout) view.findViewById(R.id.container);

		footerView = new TextView(getActivity());
		footerView.setText("清空搜索历史");
		footerView.setTextSize(18);
		footerView.setTextColor(getResources().getColor(R.color.default_text_bg));
		footerView.setBackgroundResource(R.drawable.white_and_grey_selector);
		int padding = getResources().getDimensionPixelSize(R.dimen.padding_large);
		footerView.setPadding(padding, padding, padding, padding);
		footerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		footerView.setGravity(Gravity.CENTER_HORIZONTAL);

		listview = (ListView) view.findViewById(R.id.listview);
		listview.addFooterView(footerView);
		footerView.setOnClickListener(clickListener);
		listview.setAdapter(adapter);
		historyContainer.setVisibility(View.GONE);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ELog.i(App.CACHE.read(Cache.CACHE_USER_CHOOSE_CITY_NAME));

		initData();
	}

	private void initData() {
		if (App.PREFS.getHistorySearchList() != null && App.PREFS.getHistorySearchList().size() > 0) {
			historyContainer.setVisibility(View.VISIBLE);
			adapter.data.clear();
			adapter.data.addAll(App.PREFS.getHistorySearchList());
		}
		adapter.notifyDataSetChanged();

		obtainHotKeyWords();
	}

	private final OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				getActivity().setResult(Activity.RESULT_CANCELED);
				getActivity().finish();
//				getFragmentManager().popBackStack();
			}
			else if (v == btnRight) {
				// TODO 搜索跳回到活动列表界面
				String str = input.getText().toString().trim();

				if (TextUtils.isEmpty(str)) {
					Toast.makeText(getActivity(), "请输入内容", Toast.LENGTH_SHORT).show();
					return;
				}

				App.PREFS.addHistorySearch(str);

				openActivityList();
			}
			else if (v == footerView) {
				App.PREFS.getHistorySearchList().clear();
				adapter.data.clear();
				adapter.notifyDataSetChanged();
				historyContainer.setVisibility(View.GONE);
			}
			else if (v instanceof TextView) { // listview的item
				String str = (String) v.getTag();
				if (App.PREFS.getHistorySearchList().contains(str)) {
					App.PREFS.getHistorySearchList().remove(str);
				}

				App.PREFS.addHistorySearch(str);
				openActivityList();
			}

		}
	};

	private void openActivityList() {
//		Intent intent = new Intent(getActivity(), AmuseActivity.class);
//		intent.putExtra(ActivityFragment.HUODONG_TYPE, HuodongTypeUtil.CONDITION_FUZZY_SEARCH);
//		getActivity().startActivity(intent);
//		getActivity().finish();

		host.switchFragment(SearchActivity.FRAGMENT_TYPE_RESULT);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_OBTAIN_KEY_WORDS:
				// TODO 给hotContent设置值
				if (keyWordsData.size() > 0) {
					hotContent.setVisibility(View.VISIBLE);
					String gap = "    "; // 关键词之间的间距
					String tip = "[热门搜索]  ";
					final int gapLen = gap.length();

					SpannableStringBuilder builder = new SpannableStringBuilder();
					builder.append(tip);
					for (int i = 0; i < keyWordsData.size(); i++) {
						if (i == keyWordsData.size() - 1) {
							builder.append(keyWordsData.get(i));
						}
						else {
							builder.append(keyWordsData.get(i)).append(gap);
						}
					}

					ForegroundColorSpan tipColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.detail_free_tip));
					builder.setSpan(tipColorSpan, 0, tip.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					ClickableSpan[] spans = new ClickableSpan[keyWordsData.size()];
					int startLen = 0;
					int endLen = 0;
					for (int i = 0; i < keyWordsData.size(); i++) {
						final String content = keyWordsData.get(i);
						spans[i] = new ClickableSpan() {
							@Override
							public void onClick(View widget) {

								if (App.PREFS.getHistorySearchList().contains(content)) {
									App.PREFS.getHistorySearchList().remove(content);
								}
								ELog.i("content = " + content);
								App.PREFS.addHistorySearch(content);
								openActivityList();
							}

							@Override
							public void updateDrawState(TextPaint ds) {
								ds.setUnderlineText(false);
							}
						};
						if (i == 0) {
							startLen = tip.length() + startLen;
						}
						else {
							startLen += keyWordsData.get(i - 1).length() + gapLen;
						}

						endLen = startLen + keyWordsData.get(i).length();
//						ELog.i("position = " + i + ",startLen = " + startLen + ", endLen = " + endLen);

						builder.setSpan(spans[i], startLen - 1, endLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					hotContent.setMovementMethod(LinkMovementMethod.getInstance());
					hotContent.setText(builder);
				}
				else {
					hotContent.setVisibility(View.GONE);
				}
				break;

			default:
				break;
		}
		return super.handleMessage(msg);
	}

	private class AdapterImp extends BaseAdapter {
		private List<String> data = new ArrayList<String>();

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
			TextView item = null;

			if (convertView == null) {
				item = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.default_listview_item, null);
				item.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.history), null, null, null);
				item.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.padding_large));
			}
			else {
				item = (TextView) convertView;
			}

			item.setOnClickListener(clickListener);
			item.setText(data.get(position));
			item.setTag(data.get(position));

			return item;
		}

	}

	private void getJson(String str) throws Exception {
		JSONObject obj = new JSONObject(str);
		JSONArray array = obj.getJSONArray("data");
		keyWordsData.clear();
		for (int i = 0; i < array.length(); i++) {
			keyWordsData.add(array.getJSONObject(i).getString("name"));
		}

		handler.sendEmptyMessage(HANDLER_OBTAIN_KEY_WORDS);
	}

	/**
	 * 获取热门关键词
	 * 
	 * @param page
	 */
	public void obtainHotKeyWords() {
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);

					getJson(str);
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

		HttpRequest req = new HttpRequest(Constants.GET_HOT_KEY_WORDS, null, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
	}
}

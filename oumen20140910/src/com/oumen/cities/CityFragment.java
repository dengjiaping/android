package com.oumen.cities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.base.Cache;
import com.oumen.widget.sortview.SideBar;
import com.oumen.widget.sortview.SortDataItem;
import com.oumen.widget.sortview.SideBar.OnTouchingLetterChangedListener;

/**
 * 城市选择界面
 * @author oumen-xin.zhang
 *
 */
public class CityFragment extends BaseFragment {

	private final int HANDLER_GET_LIST = 1;
	private TitleBar titlebar;
	private Button btnLeft;

	private TextView txtLocationCity;

	private ListView listView;
	private SideBar sideBar;
	private TextView txtLetterTip;

	private final CitiesAdapter adapter = new CitiesAdapter();

	private int currentPosition = App.INT_UNSET;// 当前选中状态

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.huodong_pick_city, container, false);
		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		titlebar.getTitle().setText("城市列表");
		titlebar.getRightButton().setVisibility(View.GONE);
		btnLeft = titlebar.getLeftButton();
		btnLeft.setOnClickListener(clickListener);

		txtLocationCity = (TextView) view.findViewById(R.id.location_city);
		txtLocationCity.setOnClickListener(clickListener);

		txtLetterTip = (TextView) view.findViewById(R.id.tip);
		sideBar = (SideBar) view.findViewById(R.id.sidebar);
		sideBar.setTextView(txtLetterTip);
		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					listView.setSelection(position);
				}
			}
		});

		listView = (ListView) view.findViewById(R.id.list);
		listView.setBackgroundColor(getResources().getColor(R.color.white));
		listView.setDivider(new ColorDrawable(getResources().getColor(R.color.oumen_line)));
		listView.setDividerHeight(1);
		listView.setAdapter(adapter);
		return view;
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_GET_LIST:
				adapter.notifyDataSetChanged();
				break;

			default:
				break;
		}
		return super.handleMessage(msg);
	}

	private void initData() {
		txtLocationCity.setText(App.PREFS.getCurrentCityName());

		if (currentPosition == App.INT_UNSET) {
			txtLocationCity.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.confirm_huodong), null);
		}
		else {
			txtLocationCity.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}

		App.THREAD.execute(new Runnable() {

			@Override
			public void run() {
				adapter.data.clear();
				adapter.data.addAll(App.ALLCITIES);
				handler.sendEmptyMessage(HANDLER_GET_LIST);
			}
		});
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

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				getActivity().setResult(Activity.RESULT_OK);
				getActivity().finish();
			}
			else if (v == txtLocationCity) {
				txtLocationCity.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.confirm_huodong), null);
				currentPosition = App.INT_UNSET;
				listView.setSelection(App.INT_UNSET);
				adapter.notifyDataSetChanged();

				backSearchFragment(App.PREFS.getCurrentCityName(), App.PREFS.getLatitude(), App.PREFS.getLongitude());
			}
			else if (v instanceof Item) {
				Item item = (Item) v;
				item.setSelectionState();
				adapter.notifyDataSetChanged();

				int position = (Integer) v.getTag();
				currentPosition = position;

				SortDataItem<City> itemData = adapter.data.get(position);
				City city = (City) itemData.getObject();

				txtLocationCity.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

				backSearchFragment(city.getName(), city.getLat(), city.getLng());
			}
		}
	};

	/**
	 * 返回到搜索界面
	 * @param name
	 * @param lat
	 * @param lng
	 */
	private void backSearchFragment(String name, float lat, float lng) {
		App.CACHE.save(Cache.CACHE_USER_CHOOSE_CITY_NAME, name);

		getActivity().setResult(Activity.RESULT_OK);
		getActivity().finish();
	}

	/**
	 * 城市适配器
	 * 
	 */
	class CitiesAdapter extends BaseAdapter implements SectionIndexer {
		final ArrayList<SortDataItem<City>> data = new ArrayList<SortDataItem<City>>();

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
			Item item = null;

			if (convertView == null) {
				item = new Item(parent.getContext());
			}
			else {
				item = (Item) convertView;
			}
			item.update(position);
			item.setTag(position);
			item.setOnClickListener(clickListener);

			if (currentPosition == position) {
				item.setSelectionState();
			}
			else {
				item.setUnSelectionState();
			}

			return item;
		}

		@Override
		public int getPositionForSection(int section) {
			for (int i = 0; i < getCount(); i++) {
				char firstChar = data.get(i).getPinyin().charAt(0);
				if (firstChar == section) {
					return i;
				}
			}

			return -1;
		}

		@Override
		public int getSectionForPosition(int position) {
			return data.get(position).getFirst();
		}

		@Override
		public Object[] getSections() {
			return null;
		}
	}

	class Item extends LinearLayout {
		TextView txtCatalog;
		TextView txtContent;

		int position;
		String nick;

		public Item(Context context) {
			this(context, null, 0);
		}

		public Item(Context context, AttributeSet attrs) {
			this(context, attrs, 0);
		}

		public Item(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.huodong_cities_item, this, true);

			txtCatalog = (TextView) findViewById(R.id.catalog);
			txtContent = (TextView) findViewById(R.id.content);
		}

		public void setSelectionState() {
			txtContent.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.confirm_huodong), null);
		}

		public void setUnSelectionState() {
			txtContent.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}

		void update(int position) {
			this.position = position;
			SortDataItem<City> itemData = adapter.data.get(position);
			nick = itemData.getObject().getName();

			// 根据position获取分类的首字母的Char ascii值
			int section = adapter.getSectionForPosition(position);

			// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
			if (position == adapter.getPositionForSection(section)) {
				txtCatalog.setVisibility(View.VISIBLE);
				txtCatalog.setText(String.valueOf(itemData.getFirst()));
			}
			else {
				txtCatalog.setVisibility(View.GONE);
			}

			txtContent.setText(nick);
		}
	}
}

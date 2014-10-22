package com.oumen.mv.index;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.mv.MvComposeService;
import com.oumen.mv.MvInfo;

public class PagerView extends FrameLayout implements View.OnClickListener {
	private static final int DAYS = 3;
	
	protected final AdapterImpl adapter = new AdapterImpl();
	
	protected Day data;
	
	protected DateView viewDate;
	protected Button btnAdd;
	protected AddPageView viewAdd;
	protected FrameLayout container;
	protected Button btnOpen;
	protected ViewPager pager;
	
	protected DaysIndexController controller;

	public PagerView(Context context) {
		this(context, null, 0);
	}

	public PagerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.mv_list_pager_item, this, true);
		
		viewDate = (DateView) findViewById(R.id.date);
		btnAdd = (Button) findViewById(R.id.add);
		btnAdd.setOnClickListener(this);
		viewAdd = (AddPageView) findViewById(R.id.add_page);
		viewAdd.setOnClickListener(this);

		Resources res = context.getResources();
		int videoWidth = res.getDisplayMetrics().widthPixels - res.getDimensionPixelSize(R.dimen.padding_super) * 2 - res.getDimensionPixelSize(R.dimen.padding_micro) * 2;
		int videoHeight = videoWidth * 3 / 4;
		int videoPanelHeight = videoHeight + res.getDimensionPixelSize(R.dimen.padding_micro) * 2 + res.getDimensionPixelSize(R.dimen.mv_page_item_title_height);
		int height = videoPanelHeight + res.getDimensionPixelSize(R.dimen.mv_page_item_button_height);
		
		container = (FrameLayout) findViewById(R.id.container);
		btnOpen = (Button) findViewById(R.id.open);
		btnOpen.setOnClickListener(this);
		pager = new ViewPager(context);
		pager.setAdapter(adapter);
		container.addView(pager, 1, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, height));
	}

	void update(Day data) {
		this.data = data;
		
		viewDate.txtDay.setText(App.YYYY_MM_DD_CHINESE_FORMAT.format(data.date.getTime()));
		btnOpen.setVisibility(data.list.size() >= DAYS ? View.VISIBLE : View.GONE);
		
		if (data.today) {
			if (data.list.isEmpty()) {
				btnAdd.setVisibility(View.VISIBLE);
				container.setVisibility(View.GONE);
			}
			else {
				btnAdd.setVisibility(View.GONE);
				container.setVisibility(View.VISIBLE);
				
				viewAdd.setVisibility(View.GONE);
				pager.setVisibility(View.VISIBLE);
			}
		}
		else {
			btnAdd.setVisibility(View.GONE);
			container.setVisibility(View.VISIBLE);
			
			if (data.list.isEmpty()) {
				viewAdd.setVisibility(View.VISIBLE);
				pager.setVisibility(View.GONE);
			}
			else {
				viewAdd.setVisibility(View.GONE);
				pager.setVisibility(View.VISIBLE);
			}
		}
		adapter.notifyDataSetChanged();
	}

	class AdapterImpl extends PagerAdapter {

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			MvInfo itemData = data.list.get(position);
			PagerItem item = new PagerItem(container.getContext());
			item.controller = controller;
			item.update(itemData);
			container.addView(item);
			return item;
		}

		@Override
		public int getCount() {
			return data == null ? 0 : data.list.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.add || id == R.id.add_page) {
			if (MvComposeService.running) {
				Toast.makeText(getContext(), R.string.mv_compose_err_refuse_action, Toast.LENGTH_SHORT).show();
				return;
			}
			Calendar createAt = Calendar.getInstance();
			createAt.set(data.date.get(Calendar.YEAR), data.date.get(Calendar.MONTH), data.date.get(Calendar.DAY_OF_MONTH));
			createAt.set(Calendar.SECOND, 0);
			createAt.set(Calendar.MILLISECOND, 0);
			controller.make(createAt);
		}
		else if (id == R.id.open) {
			if (MvComposeService.running) {
				Toast.makeText(getContext(), R.string.mv_compose_err_refuse_action, Toast.LENGTH_SHORT).show();
				return;
			}
			DayListActivity.day = data;
			getContext().startActivity(new Intent(getContext(), DayListActivity.class));
		}
	}
}

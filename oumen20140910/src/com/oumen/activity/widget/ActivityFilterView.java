package com.oumen.activity.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.activity.HuodongTypeUtil;
import com.oumen.android.App;

public class ActivityFilterView extends LinearLayout implements View.OnClickListener {
	public enum Type {
		MULTI {
			@Override
			int getNormalBackgroundColorForGroup2(Resources res) {
				return res.getColor(R.color.activity_filter_big_btn_normal);
			}

			@Override
			int getNormalTextColorForGroup2(Resources res) {
				return res.getColor(R.color.white);
			}

			@Override
			int getPressedBackgroundColorForGroup2(Resources res) {
				return res.getColor(R.color.activity_filter_big_btn_pressed);
			}

			@Override
			int getPressedTextColorForGroup2(Resources res) {
				return res.getColor(R.color.activity_filter_text_pressed1);
			}
		},
		SINGLE {
			@Override
			int getNormalBackgroundColorForGroup2(Resources res) {
				return res.getColor(R.color.activity_filter_btn_dark);
			}

			@Override
			int getNormalTextColorForGroup2(Resources res) {
				return res.getColor(R.color.activity_filter_text_pressed2);
			}

			@Override
			int getPressedBackgroundColorForGroup2(Resources res) {
				return res.getColor(R.color.activity_filter_big_btn_pressed);
			}

			@Override
			int getPressedTextColorForGroup2(Resources res) {
				return res.getColor(R.color.activity_filter_text_pressed1);
			}
		};

		abstract int getNormalBackgroundColorForGroup2(Resources res);

		abstract int getNormalTextColorForGroup2(Resources res);

		abstract int getPressedBackgroundColorForGroup2(Resources res);

		abstract int getPressedTextColorForGroup2(Resources res);

		int getNormalBackgroundColorForGroup1(Resources res) {
			return res.getColor(R.color.activity_filter_big_btn_normal);
		}

		int getNormalTextColorForGroup1(Resources res) {
			return res.getColor(R.color.white);
		}

		int getPressedBackgroundColorForGroup1(Resources res) {
			return res.getColor(R.color.activity_filter_big_btn_pressed);
		}

		int getPressedTextColorForGroup1(Resources res) {
			return res.getColor(R.color.activity_filter_text_pressed1);
		}

		int getNormalIconForGroup1(int condition) {
			switch (condition) {
				case CONDITION_BEIYUN:
					return R.drawable.icon_beiyun_light;

				case CONDITION_HUAIYUN:
					return R.drawable.icon_huaiyun_light;

				case CONDITION_AGE_RANGE_1:
					return R.drawable.icon_age_range1_light;

				case CONDITION_AGE_RANGE_2:
					return R.drawable.icon_age_range2_light;

				case CONDITION_AGE_RANGE_3:
					return R.drawable.icon_age_range3_light;

				case CONDITION_AGE_RANGE_4:
					return R.drawable.icon_age_range4_light;

				default:
					return 0;
			}
		}

		int getPressedIconForGroup1(int condition) {
			switch (condition) {
				case CONDITION_BEIYUN:
					return R.drawable.icon_beiyun_dark;

				case CONDITION_HUAIYUN:
					return R.drawable.icon_huaiyun_dark;

				case CONDITION_AGE_RANGE_1:
					return R.drawable.icon_age_range1_dark;

				case CONDITION_AGE_RANGE_2:
					return R.drawable.icon_age_range2_dark;

				case CONDITION_AGE_RANGE_3:
					return R.drawable.icon_age_range3_dark;

				case CONDITION_AGE_RANGE_4:
					return R.drawable.icon_age_range4_dark;

				default:
					return 0;
			}
		}
	}

	//Group 1
	protected static final int CONDITION_BEIYUN = 0;
	protected static final int CONDITION_HUAIYUN = 1;
	protected static final int CONDITION_AGE_RANGE_1 = 2;
	protected static final int CONDITION_AGE_RANGE_2 = 3;
	protected static final int CONDITION_AGE_RANGE_3 = 4;
	protected static final int CONDITION_AGE_RANGE_4 = 5;

	protected TextView[] groupView1 = new TextView[6];
	protected TextView[] groupView2 = new TextView[3];
	protected Button btnOk;

	protected Type type = Type.MULTI;

	public ActivityFilterView(Context context) {
		this(context, null, 0);
	}

	public ActivityFilterView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActivityFilterView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.activity_filter, this, true);

		groupView1[CONDITION_BEIYUN] = (TextView) findViewById(R.id.btn_beiyun);
		groupView1[CONDITION_BEIYUN].setOnClickListener(this);
		groupView1[CONDITION_HUAIYUN] = (TextView) findViewById(R.id.btn_huaiyun);
		groupView1[CONDITION_HUAIYUN].setOnClickListener(this);
		groupView1[CONDITION_AGE_RANGE_1] = (TextView) findViewById(R.id.btn_range1);
		groupView1[CONDITION_AGE_RANGE_1].setOnClickListener(this);
		groupView1[CONDITION_AGE_RANGE_2] = (TextView) findViewById(R.id.btn_range2);
		groupView1[CONDITION_AGE_RANGE_2].setOnClickListener(this);
		groupView1[CONDITION_AGE_RANGE_3] = (TextView) findViewById(R.id.btn_range3);
		groupView1[CONDITION_AGE_RANGE_3].setOnClickListener(this);
		groupView1[CONDITION_AGE_RANGE_4] = (TextView) findViewById(R.id.btn_range4);
		groupView1[CONDITION_AGE_RANGE_4].setOnClickListener(this);

		groupView2[HuodongTypeUtil.CONDITION_HUWAI] = (TextView) findViewById(R.id.huwai);
		groupView2[HuodongTypeUtil.CONDITION_HUWAI].setOnClickListener(this);
		groupView2[HuodongTypeUtil.CONDITION_SHINEI] = (TextView) findViewById(R.id.shinei);
		groupView2[HuodongTypeUtil.CONDITION_SHINEI].setOnClickListener(this);
//		groupView2[CONDITION_LVYOU] = (TextView) findViewById(R.id.lvyou);
//		groupView2[CONDITION_LVYOU].setOnClickListener(this);
		groupView2[HuodongTypeUtil.CONDITION_XIANSHANG - 1] = (TextView) findViewById(R.id.xianshang);
		groupView2[HuodongTypeUtil.CONDITION_XIANSHANG - 1].setOnClickListener(this);

		btnOk = (Button) findViewById(R.id.ok);
	}
	
	public void initialize(int condition1, int condition2) {
		if (condition1 >= 0) {
			groupView1[condition1].setSelected(true);
			updateGroup1(condition1);
		}
		if (condition2 >= 0) {
			int i = 0;
			if (condition2 == 0)
				i = HuodongTypeUtil.CONDITION_HUWAI;
			else if (condition2 == 1)
				i = HuodongTypeUtil.CONDITION_SHINEI;
			else if (condition2 == 3)
				condition2 = HuodongTypeUtil.CONDITION_XIANSHANG;
			groupView2[i].setSelected(true);
			updateGroup2(i);
		}
	}
	
	public Button getOk() {
		return btnOk;
	}
	
	public int getCondition1() {
		for (int i = 0; i < groupView1.length; i++) {
			if (groupView1[i].isSelected()) {
				return i;
			}
		}
		return -1;
	}
	
	public int getCondition2() {
		int index = App.INT_UNSET;
		for (int i = 0; i < groupView2.length; i++) {
			if (groupView2[i].isSelected()) {
				index = i;
				continue;
			}
		}
		
		if (index == 0)
			return HuodongTypeUtil.CONDITION_HUWAI;
		else if (index == 1)
			return HuodongTypeUtil.CONDITION_SHINEI;
		else if (index == 2)
			return HuodongTypeUtil.CONDITION_XIANSHANG;
		
		return App.INT_UNSET;
	}

	@Override
	public void onClick(View v) {
		int index = -1;
		for (int i = 0; i < groupView1.length; i++) {
			if (v == groupView1[i]) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			for (int i = 0; i < groupView1.length; i++) {
				TextView btn = groupView1[i];
				boolean selected = i == index;
				if (btn.isSelected() == selected)
					continue;
				btn.setSelected(selected);
				updateGroup1(i);
			}
			return;
		}

		if (Type.SINGLE.equals(type))
			return;
		
		for (int i = 0; i < groupView2.length; i++) {
			if (v == groupView2[i]) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			for (int i = 0; i < groupView2.length; i++) {
				TextView btn = groupView2[i];
				boolean selected = i == index;
				if (btn.isSelected() == selected)
					continue;
				btn.setSelected(selected);
				updateGroup2(i);
			}
			return;
		}
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
		
		for (int i = 0; i < groupView1.length; i++) {
			updateGroup1(i);
		}
		
		for (int i = 0; i < groupView2.length; i++) {
			updateGroup2(i);
		}
	}
	
	protected void updateGroup1(int index) {
		Resources res = getResources();
		TextView item = groupView1[index];
		if (item.isSelected()) {
			item.setTextColor(type.getPressedTextColorForGroup1(res));
			item.setCompoundDrawablesWithIntrinsicBounds(0, type.getPressedIconForGroup1(index), 0, 0);
			item.setBackgroundColor(type.getPressedBackgroundColorForGroup1(res));
		}
		else {
			item.setTextColor(type.getNormalTextColorForGroup1(res));
			item.setCompoundDrawablesWithIntrinsicBounds(0, type.getNormalIconForGroup1(index), 0, 0);
			item.setBackgroundColor(type.getNormalBackgroundColorForGroup1(res));
		}
	}
	
	protected void updateGroup2(int index) {
		Resources res = getResources();
		TextView item = groupView2[index];
		if (item.isSelected()) {
			item.setTextColor(type.getPressedTextColorForGroup2(res));
			item.setBackgroundColor(type.getPressedBackgroundColorForGroup2(res));
		}
		else {
			item.setTextColor(type.getNormalTextColorForGroup2(res));
			item.setBackgroundColor(type.getNormalBackgroundColorForGroup2(res));
		}
	}
}

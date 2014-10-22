package com.oumen.mv.index;

import java.text.ParseException;
import java.util.Calendar;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.UserProfile;
import com.oumen.tools.CalendarTools;
import com.oumen.tools.ELog;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * mv列表头部信息
 * @author anyouruo
 *
 */
public class MVListHeader extends RelativeLayout {
	private ImageView ivBabyState;
	private TextView tvYear, tvMonth, tvDay;
	private ImageView ivQitaState;

	public MVListHeader(Context context) {
		this(context, null, 0);
	}

	public MVListHeader(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MVListHeader(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.mv_list_header_babystate, this, true);

		ivBabyState = (ImageView) findViewById(R.id.state_tag);
		tvYear = (TextView) findViewById(R.id.year);
		tvMonth = (TextView) findViewById(R.id.month);
		tvDay = (TextView) findViewById(R.id.day);

		ivQitaState = (ImageView) findViewById(R.id.qita_tag);
	}

	public void update() {
		if (App.USER.getBabyType() != App.INT_UNSET) {
			switch (App.USER.getBabyType()) {
				case UserProfile.BABY_TYPE_CHU_SHENG:
					ivBabyState.setVisibility(View.VISIBLE);
					tvYear.setVisibility(View.VISIBLE);
					tvYear.setBackgroundResource(R.drawable.header_age);
					tvMonth.setVisibility(View.VISIBLE);
					tvDay.setVisibility(View.VISIBLE);
					ivQitaState.setVisibility(View.GONE);

					ivBabyState.setImageResource(R.drawable.mv_header_chusheng);
					if (TextUtils.isEmpty(App.USER.getBirthdayTime()) || "0000-00-00".equals(App.USER.getBirthdayTime())) {
						return;
					}

					Calendar calendar = Calendar.getInstance();
					try {
						calendar.setTime(App.YYYY_MM_DD_FORMAT.parse(App.USER.getBirthdayTime()));
					}
					catch (ParseException e) {
						e.printStackTrace();
					}
					int gap = CalendarTools.getOffsetDays(Calendar.getInstance(), calendar);
					int years = gap / 365,
					months = (gap % 365) / 30,
					days = (gap % 365) % 30;
					tvYear.setText(App.NUMBER_FORMAT.format(years));
					tvMonth.setText(App.NUMBER_FORMAT.format(months));
					tvDay.setText(App.NUMBER_FORMAT.format(days));
					break;
				case UserProfile.BABY_TYPE_HUAI_YUN:
					ivBabyState.setVisibility(View.VISIBLE);
					tvYear.setVisibility(View.VISIBLE);
					tvYear.setBackgroundResource(R.drawable.header_year);
					tvMonth.setVisibility(View.VISIBLE);
					tvDay.setVisibility(View.VISIBLE);
					ivQitaState.setVisibility(View.GONE);

					ivBabyState.setImageResource(R.drawable.mv_header_huaiyun);

					if (TextUtils.isEmpty(App.USER.getGravidityTime()) || "0000-00-00".equals(App.USER.getGravidityTime())) {
						return;
					}

					calendar = Calendar.getInstance();
					try {
						calendar.setTime(App.YYYY_MM_DD_FORMAT.parse(App.USER.getGravidityTime()));
					}
					catch (ParseException e) {
						e.printStackTrace();
					}
					int gap1 = CalendarTools.getOffsetDays(calendar, Calendar.getInstance());
					ELog.i((1 / 365) + "年" + ((gap1 % 365) / 12) + "月" + ((gap1 % 365) % 12) + "日");
					ELog.i(App.USER.getGravidityTime() +"," + gap1);
					years = gap1 / 365;
					months = (gap1 % 365) / 30;
					days = (gap1 % 365) % 30;

					tvYear.setText(App.NUMBER_FORMAT.format(years));
					tvMonth.setText(App.NUMBER_FORMAT.format(months));
					tvDay.setText(App.NUMBER_FORMAT.format(days));
					break;
				default:
					ivBabyState.setVisibility(View.GONE);
					tvYear.setVisibility(View.GONE);
					tvMonth.setVisibility(View.GONE);
					tvDay.setVisibility(View.GONE);
					ivQitaState.setVisibility(View.VISIBLE);
					break;
			}
		}
	}

}

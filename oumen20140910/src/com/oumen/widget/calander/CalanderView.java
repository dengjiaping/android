package com.oumen.widget.calander;

import java.util.ArrayList;
import java.util.Calendar;

import com.oumen.R;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 日历
 *
 */
public class CalanderView extends LinearLayout {
	private TextView title, before, after;
	/*
	 * 日历的容器
	 */
	private LinearLayout container;

	private static int iDayCellSize = 0;
	private static int iDayHeaderHeight = 0;
	private int iFirstDayOfWeek = Calendar.SUNDAY;

	private int iMonthViewCurrentMonth = 0;
	private int iMonthViewCurrentYear = 0;

	private ArrayList<DateWidgetDayCell> days = new ArrayList<DateWidgetDayCell>();

	private Calendar calStartDate = Calendar.getInstance();
	private Calendar calToday = Calendar.getInstance();
	private Calendar calCalendar = Calendar.getInstance();
	private Calendar calSelected = Calendar.getInstance();

	private int mYear;
	private int mMonth;
	private int mDay;

	private Resources res;
	private Context context;

	public CalanderView(Context context) {
		this(context, null, 0);
	}

	public CalanderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CalanderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.calander_view, this, true);

		this.context = context;

		res = context.getResources();

		title = (TextView) findViewById(R.id.current_time);
		before = (TextView) findViewById(R.id.before);
		after = (TextView) findViewById(R.id.after);

		container = (LinearLayout) findViewById(R.id.time_container);

		before.setOnClickListener(clickListener);
		after.setOnClickListener(clickListener);

		initCalenderData();
	}

	/**
	 * 初始化日期
	 */
	private void initCalenderData() {
		iFirstDayOfWeek = Calendar.SUNDAY;

		mYear = calSelected.get(Calendar.YEAR);
		mMonth = calSelected.get(Calendar.MONTH);
		mDay = calSelected.get(Calendar.DAY_OF_MONTH);

		// 获取屏幕宽度，计算出每个cell的宽度和高度
		iDayCellSize = getResources().getDisplayMetrics().widthPixels / 7 + 1;
		iDayHeaderHeight = iDayCellSize / 2;
	}
	
	private DateWidgetDayCell.OnItemClick mOnDayCellClick;
	
	public void setCellClickListener(DateWidgetDayCell.OnItemClick mOnDayCellClick) {
		this.mOnDayCellClick = mOnDayCellClick;
		// 1. 添加日历的头部
		container.addView(generateCalendarHeader());

		//2.线
		LinearLayout line = new LinearLayout(context);
		line.setBackgroundColor(res.getColor(R.color.default_line_bg));
		line.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1));
		line.setOrientation(LinearLayout.HORIZONTAL);
		container.addView(line);

		// 2. 添加日历
		days.clear();
		for (int iRow = 0; iRow < 6; iRow++) {
			container.addView(generateCalendarRow());
		}
		//2.线
		LinearLayout line1 = new LinearLayout(context);
		line1.setBackgroundColor(res.getColor(R.color.default_line_bg));
		line1.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1));
		line1.setOrientation(LinearLayout.HORIZONTAL);
		container.addView(line1);

		calStartDate = getCalendarStartDate();
		
		calSelected.setTimeInMillis(0);
		updateCalendar();
		updateControlsState();
	}
	
//	private DateWidgetDayCell.OnItemClick mOnDayCellClick = new DateWidgetDayCell.OnItemClick() {
//	public void OnClick(DateWidgetDayCell item) {
//		calSelected.setTimeInMillis(item.getDate().getTimeInMillis());
//		item.setSelected(true);
//		updateCalendar();
//		updateControlsState();
//	}
//};
	
	public void updateCurrentDate(DateWidgetDayCell item) {
		calSelected.setTimeInMillis(item.getDate().getTimeInMillis());
		item.setSelected(true);
		updateCalendar();
		updateControlsState();
	}
	
	public void clearDateSelection() {
		calSelected.setTimeInMillis(0);
		updateCalendar();
		updateControlsState();
	}

	/**
	 * 添加每一行日期信息
	 * 
	 * @return
	 */
	private View generateCalendarRow() {
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		for (int iDay = 0; iDay < 7; iDay++) {
			DateWidgetDayCell dayCell = new DateWidgetDayCell(context, iDayCellSize, iDayCellSize);
			dayCell.setItemClick(mOnDayCellClick);
			days.add(dayCell);
			layRow.addView(dayCell);
		}
		return layRow;
	}

	/**
	 * 添加日期头部
	 * 
	 * @return
	 */
	private View generateCalendarHeader() {
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		for (int iDay = 0; iDay < 7; iDay++) {
			DateWidgetDayHeader day = new DateWidgetDayHeader(context, iDayCellSize, iDayHeaderHeight);
			final int iWeekDay = getWeekDay(iDay, iFirstDayOfWeek);
			day.setData(iWeekDay);
			layRow.addView(day);
		}
		return layRow;
	}

	private int getWeekDay(int index, int iFirstDayOfWeek) {
		int iWeekDay = -1;
		if (iFirstDayOfWeek == Calendar.MONDAY) {
			iWeekDay = index + Calendar.MONDAY;
			if (iWeekDay > Calendar.SATURDAY)
				iWeekDay = Calendar.SUNDAY;
		}
		if (iFirstDayOfWeek == Calendar.SUNDAY) {
			iWeekDay = index + Calendar.SUNDAY;
		}
		return iWeekDay;
	}

	private LinearLayout createLayout(int iOrientation) {
		LinearLayout lay = new LinearLayout(context);
		lay.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		lay.setOrientation(iOrientation);
		return lay;
	}

	private void setPrevMonthViewItem() {
		iMonthViewCurrentMonth--;
		if (iMonthViewCurrentMonth == -1) {
			iMonthViewCurrentMonth = 11;
			iMonthViewCurrentYear--;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		updateStartDateForMonth();
		updateCalendar();
		title.setText(new StringBuilder().append(iMonthViewCurrentYear).append("年").append(format(iMonthViewCurrentMonth + 1)).append("月"));
	}

	private void setNextMonthViewItem() {
		iMonthViewCurrentMonth++;
		if (iMonthViewCurrentMonth == 12) {
			iMonthViewCurrentMonth = 0;
			iMonthViewCurrentYear++;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		updateStartDateForMonth();
		updateCalendar();
		title.setText(new StringBuilder().append(iMonthViewCurrentYear).append("年").append(format(iMonthViewCurrentMonth + 1)).append("月"));
	}

	private DateWidgetDayCell updateCalendar() {
		DateWidgetDayCell daySelected = null;
		boolean bSelected = false;
		final boolean bIsSelection = (calSelected.getTimeInMillis() != 0);
		final int iSelectedYear = calSelected.get(Calendar.YEAR);
		final int iSelectedMonth = calSelected.get(Calendar.MONTH);
		final int iSelectedDay = calSelected.get(Calendar.DAY_OF_MONTH);
		calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());
		for (int i = 0; i < days.size(); i++) {
			final int iYear = calCalendar.get(Calendar.YEAR);
			final int iMonth = calCalendar.get(Calendar.MONTH);
			final int iDay = calCalendar.get(Calendar.DAY_OF_MONTH);
			final int iDayOfWeek = calCalendar.get(Calendar.DAY_OF_WEEK);
			DateWidgetDayCell dayCell = days.get(i);
			// check today
			boolean bToday = false;
			if (calToday.get(Calendar.YEAR) == iYear)
				if (calToday.get(Calendar.MONTH) == iMonth)
					if (calToday.get(Calendar.DAY_OF_MONTH) == iDay)
						bToday = true;
			// check holiday
			boolean bHoliday = false;
			if ((iDayOfWeek == Calendar.SATURDAY) || (iDayOfWeek == Calendar.SUNDAY))
				bHoliday = true;
			if ((iMonth == Calendar.JANUARY) && (iDay == 1))
				bHoliday = true;

			dayCell.setData(iYear, iMonth, iDay, bToday, bHoliday, iMonthViewCurrentMonth, iDayOfWeek);
			bSelected = false;
			if (bIsSelection)
				if ((iSelectedDay == iDay) && (iSelectedMonth == iMonth) && (iSelectedYear == iYear)) {
					bSelected = true;
				}
			dayCell.setSelected(bSelected);
			if (bSelected)
				daySelected = dayCell;
			calCalendar.add(Calendar.DAY_OF_MONTH, 1);
			dayCell.invalidate();
		}
		container.invalidate();
		return daySelected;
	}

	private void updateControlsState() {
		if (calSelected.getTimeInMillis() == 0) {
			title.setText("");
			return ;
		}
		mYear = calSelected.get(Calendar.YEAR);
		mMonth = calSelected.get(Calendar.MONTH);
		mDay = calSelected.get(Calendar.DAY_OF_MONTH);
		title.setText(new StringBuilder().append(mYear).append("年").append(format(mMonth + 1)).append("月").append(format(mDay)).append("日"));
	}

	private String format(int x) {
		String s = "" + x;
		if (s.length() == 1)
			s = "0" + s;
		return s;
	}

	private Calendar getCalendarStartDate() {
		calToday.setTimeInMillis(System.currentTimeMillis());
		calToday.setFirstDayOfWeek(iFirstDayOfWeek);

		if (calSelected.getTimeInMillis() == 0) {
			calStartDate.setTimeInMillis(System.currentTimeMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		}
		else {
			calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		}
		updateStartDateForMonth();

		return calStartDate;
	}

	private void updateStartDateForMonth() {
		iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);
		iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		// update days for week
		int iDay = 0;
		int iStartDay = iFirstDayOfWeek;
		if (iStartDay == Calendar.MONDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
			if (iDay < 0)
				iDay = 6;
		}
		if (iStartDay == Calendar.SUNDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
			if (iDay < 0)
				iDay = 6;
		}
		calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
	}

	private final OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == before) {
				setPrevMonthViewItem();
			}
			else if (v == after) {
				setNextMonthViewItem();
			}

		}
	};

}

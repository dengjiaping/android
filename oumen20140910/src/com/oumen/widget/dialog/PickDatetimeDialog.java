package com.oumen.widget.dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.oumen.R;
import com.oumen.android.App;

public class PickDatetimeDialog extends AbstractDialog {
	public enum Mode{DATETIME, DATE, TIME};
	
	protected Mode mode = Mode.DATETIME;
	
	protected DatePicker pickDate;
	protected TimePicker pickTime;
	
	protected Calendar calendar = Calendar.getInstance();
	protected SimpleDateFormat format;

	public PickDatetimeDialog(Context context) {
		super(context);
	}

	public PickDatetimeDialog(Context context, int theme) {
		super(context, theme);
	}

	public PickDatetimeDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreateView() {
		int margin = getContext().getResources().getDimensionPixelSize(R.dimen.default_gap);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = params.rightMargin = margin;
		pickDate = new DatePicker(getContext());
		layRoot.addView(pickDate, 1, params);

		pickTime = new TimePicker(getContext());
		pickTime.setIs24HourView(true);
		layRoot.addView(pickTime, 2, params);
	}
	
	public DatePicker getDatePickerView() {
		return pickDate;
	}
	public String getString(String template) {
		if (format == null) {
			format = new SimpleDateFormat(template, App.LOCALE);
		}
		else {
			format.applyPattern(template);
		}
		return format.format(calendar.getTime());
	}
	
	public void updateDatetime(int year, int month, int day, int hour, int minute) {
		calendar.set(year, month, day, hour, minute, 0);
		update();
	}
	
	public void updateDate(int year, int month, int day) {
		calendar.set(year, month, day, 0, 0, 0);
		update();
	}
	
	public void updateTime(int hour, int minute) {
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		update();
	}
	
	public long getTimestamp() {
		calendar.set(pickDate.getYear(), pickDate.getMonth(), pickDate.getDayOfMonth(), pickTime.getCurrentHour(), pickTime.getCurrentMinute(), 0);
		return calendar.getTimeInMillis();
	}
	
	public void setTimestamp(long timestamp) {
		calendar.setTimeInMillis(timestamp);
		update();
	}
	
	public Calendar getCalendar() {
		calendar.set(pickDate.getYear(), pickDate.getMonth(), pickDate.getDayOfMonth(), pickTime.getCurrentHour(), pickTime.getCurrentMinute(), 0);
		return calendar;
	}
	
	public void setDatetimeByString(String template, String datetime) throws ParseException {
		if (format == null) {
			format = new SimpleDateFormat(template, App.LOCALE);
		}
		else {
			format.applyPattern(template);
		}
		Date date = format.parse(datetime);
		calendar.setTime(date);
		update();
	}
	
	protected void update() {
		pickDate.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		pickTime.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		pickTime.setCurrentMinute(calendar.get(Calendar.MINUTE));
	}

	public void setMode(Mode mode) {
		this.mode = mode;
		switch (mode) {
			case DATETIME:
				pickDate.setVisibility(View.VISIBLE);
				pickTime.setVisibility(View.VISIBLE);
				break;
				
			case DATE:
				pickDate.setVisibility(View.VISIBLE);
				pickTime.setVisibility(View.GONE);
				break;
				
			case TIME:
				pickDate.setVisibility(View.GONE);
				pickTime.setVisibility(View.VISIBLE);
				break;
		}
	}
	
	public Mode getMode() {
		return mode;
	}
}

package com.oumen.tools;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarTools {
	public enum Description {
		NORMAL("年", "个月", "日"), AGE("岁", "个月", "天");
		
		private String year, month, day;
		
		Description(String year, String month, String day) {
			this.year = year;
			this.month = month;
			this.day = day;
		}

		public String getYear() {
			return year;
		}

		public String getMonth() {
			return month;
		}

		public String getDay() {
			return day;
		}
	}
	/**
	 * 计算两个时间差（年）
	 * @param src
	 * @param target
	 * @param description
	 * @return
	 */
	public static String getOffset(Calendar src, Calendar target, Description description) {
		if (src.get(Calendar.YEAR) > target.get(Calendar.YEAR)) {
			return (src.get(Calendar.YEAR) - target.get(Calendar.YEAR)) + description.year;
		}
		else if (src.get(Calendar.MONTH) > target.get(Calendar.MONTH)) {
			return (src.get(Calendar.MONTH) - target.get(Calendar.MONTH)) + description.month;
		}
		else {
			return (src.get(Calendar.DAY_OF_MONTH) - target.get(Calendar.DAY_OF_MONTH)) + description.day;
		}
	}
	
	/**
	 * 计算两个时间差（天）
	 * @param src
	 * @param target
	 * @return
	 */
	public static int getOffsetDays(Calendar src, Calendar target) {
		Calendar c1 = Calendar.getInstance(), c2 = Calendar.getInstance();
		c1.set(src.get(Calendar.YEAR), src.get(Calendar.MONTH), src.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		c1.set(Calendar.MILLISECOND, 0);
		c2.set(target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		c2.set(Calendar.MILLISECOND, 0);
		int offsetYears = (int)((c1.getTimeInMillis() - c2.getTimeInMillis()) / (24 * 60 * 60 * 1000));
		return offsetYears;
	}
	
	/**
	 * 计算两个时间差（月）
	 * @param start
	 * @param end
	 * @return
	 */
	public static int getOffsetMounths(Calendar start, Calendar end) {
		int temp = 0;
		int sYears = end.get(Calendar.YEAR);
		int sMonths = end.get(Calendar.MONTH);
		
		int eYears = start.get(Calendar.YEAR);
		int eMonths = start.get(Calendar.MONTH);
		
		temp = 12 * (eYears - sYears);
		temp += (eMonths - sMonths);
		// 返回计算结果
		return temp;
	}
}

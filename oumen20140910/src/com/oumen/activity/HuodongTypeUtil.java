package com.oumen.activity;

import com.oumen.android.App;

/**
 * 活动工具类
 *
 */
public class HuodongTypeUtil {
	public static enum FITLER_TYPE {
		ORDER, DATE, AGE, OTHER
	};

	/*
	 * 活动类型
	 * 0-->户外活动
	 * 1-->室内活动
	 * 3-->线上活动
	 * 4-->附近活动
	 * 5-->模糊查询
	 */
	public static final int CONDITION_HUWAI = 0;
	public static final int CONDITION_SHINEI = 1;
	public static final int CONDITION_XIANSHANG = 3;
	public static final int CONDITION_FUJIN = 4;
	
	public static final int CONDITION_FUZZY_SEARCH = 5;
//	public static final int CONDITION_CITY_SEARCH = 6;

	/*
	 * 活动筛选条件之年龄
	 * 0-->备孕
	 * 1-->怀孕
	 * 2-->0-1岁
	 * 3-->1-3岁
	 * 4-->3-6岁
	 * 5-->6岁以上
	 */
	public static enum AgeType {
		BEIYUN {
			@Override
			public String text() {
				return "备孕";
			}

			@Override
			public int code() {
				return 0;
			}
		},
		HUAIYUN {
			@Override
			public String text() {
				return "怀孕";
			}

			@Override
			public int code() {
				return 1;
			}
		},
		AGE_RANGE_1 {
			@Override
			public String text() {
				return "0-1岁";
			}

			@Override
			public int code() {
				return 2;
			}
		},
		AGE_RANGE_2 {
			@Override
			public String text() {
				return "1-3岁";
			}

			@Override
			public int code() {
				return 3;
			}
		},
		AGE_RANGE_3 {
			@Override
			public String text() {
				return "3-6岁";
			}

			@Override
			public int code() {
				return 4;
			}
		},
		AGE_RANGE_4 {
			@Override
			public String text() {
				return "6岁以上";
			}

			@Override
			public int code() {
				return 5;
			}
		},
		AGE_DEFAULT {
			@Override
			public String text() {
				return "不限年龄";
			}

			@Override
			public int code() {
				return App.INT_UNSET;
			}
		};

		public static AgeType parseType(String type) {
			if (AgeType.BEIYUN.text().equals(type)) {
				return AgeType.BEIYUN;
			}
			else if (AgeType.HUAIYUN.text().equals(type)) {
				return AgeType.HUAIYUN;
			}
			else if (AgeType.AGE_RANGE_1.text().equals(type)) {
				return AgeType.AGE_RANGE_1;
			}
			else if (AgeType.AGE_RANGE_2.text().equals(type)) {
				return AgeType.AGE_RANGE_2;
			}
			else if (AgeType.AGE_RANGE_3.text().equals(type)) {
				return AgeType.AGE_RANGE_3;
			}
			else if (AgeType.AGE_RANGE_4.text().equals(type)) {
				return AgeType.AGE_RANGE_4;
			}
			else {
				return AgeType.AGE_DEFAULT;
			}
		}

		public static AgeType parseType(int type) {
			if (AgeType.BEIYUN.code() == type) {
				return AgeType.BEIYUN;
			}
			else if (AgeType.HUAIYUN.code() == type) {
				return AgeType.HUAIYUN;
			}
			else if (AgeType.AGE_RANGE_1.code() == type) {
				return AgeType.AGE_RANGE_1;
			}
			else if (AgeType.AGE_RANGE_2.code() == type) {
				return AgeType.AGE_RANGE_2;
			}
			else if (AgeType.AGE_RANGE_3.code() == type) {
				return AgeType.AGE_RANGE_3;
			}
			else if (AgeType.AGE_RANGE_4.code() == type) {
				return AgeType.AGE_RANGE_4;
			}
			else {
				return AgeType.AGE_DEFAULT;
			}
		}

		abstract public String text();

		abstract public int code();
	}

	/**
	 * 
	 * 活动筛选条件之排序
	 * 0-->默认
	 * 1-->最新活动
	 * 2-->人气由高到底
	 */
	public static enum OrderType {
		DEFAULT {
			@Override
			public String text() {
				return "默认排序";
			}

			@Override
			public int code() {
				return 0;
			}
		},
		NEWEST {
			public String text() {
				return "最新活动";
			}

			@Override
			public int code() {
				return 1;
			}
		},
		HOT {
			public String text() {
				return "人气由高到底";
			}

			@Override
			public int code() {
				return 2;
			}
		};

		public static OrderType parseType(int type) {
			if (OrderType.NEWEST.code() == type) {
				return OrderType.NEWEST;
			}
			else if (OrderType.HOT.code() == type) {
				return OrderType.HOT;
			}
			else {
				return OrderType.DEFAULT;
			}
		}

		public static OrderType parseType(String type) {
			if (OrderType.NEWEST.text().equals(type)) {
				return OrderType.NEWEST;
			}
			else if (OrderType.HOT.text().equals(type)) {
				return OrderType.HOT;
			}
			else {
				return OrderType.DEFAULT;
			}
		}

		abstract public String text();

		abstract public int code();
	}

}

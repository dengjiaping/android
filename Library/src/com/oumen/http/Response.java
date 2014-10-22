package com.oumen.http;


public class Response {
	public static final String KEY_TYPE = "ret";
	public static final String KEY_DATA = "data";
	public static final String KEY_ERROR_CODE = "err";
	public static final int ERR_UNKNOWN = 0;
	public static final int ERR_EXCEPTION = 1;
	public static final int ERR_NOT_FOUND = 2;
	public static final int ERR_LOGIN_NOT = 3;//未登录
	public static final int ERR_LOGIN_HAD = 4;//已登录
	public static final int ERR_MISSING_PARAMS = 5;
	public static final int ERR_NO_TOKEN = 6;
	public static final int ERR_ILLEGAL = 7;
	public static final int ERR_ENCTYPE = 8;
	public static final int ERR_NO_FILE = 9;
	public static final int ERR_TOO_SMALL = 10;
	public static final int ERR_TOO_BIG = 11;
	public static final int ERR_NO_CONTACT = 12;
	public static final int ERR_DUPLICATE_EMAIL = 13;
	public static final int ERR_DUPLICATE_MOBILE = 14;
	public static final int ERR_NO_MOBILE = 15;
	public static final int ERR_VERIFY_CODE = 16;
	public static final int ERR_VERIFY_EXPIRING = 17;

	public static final int ERR_FIELD_CITY = 100;
	public static final int ERR_FIELD_LAT = 101;
	public static final int ERR_FIELD_LNG = 102;
	public static final int ERR_FIELD_ADDRESS = 103;
	public static final int ERR_FIELD_ID = 104;
	public static final int ERR_FIELD_NICK = 105;
	public static final int ERR_FIELD_PWD = 106;
	public static final int ERR_FIELD_GENDER = 107;
	public static final int ERR_FIELD_FACE = 108;
	public static final int ERR_FIELD_BIRTHDAY = 109;
	public static final int ERR_FIELD_INTRO = 110;
	public static final int ERR_FIELD_EMAIL = 111;
	public static final int ERR_FIELD_LINK = 112;
	public static final int ERR_FIELD_TEL = 113;
	public static final int ERR_FIELD_QQ = 114;
	public static final int ERR_FIELD_WEIXIN = 115;
	public static final int ERR_FIELD_SINA_WEIBO = 116;
	public static final int ERR_FIELD_TENCENT_WEIBO = 117;
	public static final int ERR_FIELD_RENREN = 118;
	public static final int ERR_FIELD_DOUBAN = 119;
	
	public static final int ERR_FIELD_ORIGINATOR = 120;
	public static final int ERR_FIELD_START = 121;
	public static final int ERR_FIELD_END = 122;
	public static final int ERR_FIELD_STATE = 123;
	public static final int ERR_FIELD_PURPOSE = 124;
	public static final int ERR_FIELD_MIN_EXPENSE = 125;
	public static final int ERR_FIELD_MAX_EXPENSE = 126;
	public static final int ERR_FIELD_PAY = 127;
	public static final int ERR_FIELD_MIN_AGE = 128;
	public static final int ERR_FIELD_MAX_AGE = 129;
	public static final int ERR_FIELD_STYLE = 130;
	public static final int ERR_FIELD_TITLE = 131;
	public static final int ERR_FIELD_DESCRIPTION = 132;
	public static final int ERR_FIELD_TARGET = 133;
	public static final int ERR_FIELD_BRANCH = 134;
	public static final int ERR_FIELD_NEAR = 135;
	public static final int ERR_FIELD_DIANPING_ID = 136;
	
	public static final int ERR_FIELD_NAME = 137;
	public static final int ERR_FIELD_TYPE = 138;
	public static final int ERR_FIELD_GRADE = 139;
	public static final int ERR_FIELD_EXPENSE = 140;
	public static final int ERR_FIELD_COMMENT_COUNT = 141;
	public static final int ERR_FIELD_OPEN = 142;
	public static final int ERR_FIELD_CLOSE = 143;

	public static final int ERR_FIELD_DATING = 144;
	public static final int ERR_FIELD_MSG = 145;
	public static final int ERR_FIELD_APPLICANT = 146;
	public static final int ERR_FIELD_REASON = 147;
	
	public enum Type {OK, ERR}
	
	protected Type type;

	public Response() {
		this.type = Type.OK;
	}

	public Response(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}

package com.oumen.message;

/**
 * 消息状态（已读，未读，正在发送，发送失败）
 */
public enum SendType {
	UNREAD {
		@Override
		public int code() {
			return 0;
		}
	},
	READ {
		@Override
		public int code() {
			return 1;
		}
	},
	SENDING {
		@Override
		public int code() {
			return 2;
		}
	},
	SENDFAIL {
		@Override
		public int code() {
			return 3;
		}
	};
	
	public static SendType parseSendType(int type) {
		if(SendType.READ.code() == type) {
			return SendType.READ;
		}
		else if(SendType.UNREAD.code() == type) {
			return SendType.UNREAD;
		}
		else if (SendType.SENDING.code() == type) {
			return SendType.SENDING;
		}
		else if (SendType.SENDFAIL.code() == type) {
			return SendType.SENDFAIL;
		}
		return SendType.READ;
	}

	abstract public int code();
}

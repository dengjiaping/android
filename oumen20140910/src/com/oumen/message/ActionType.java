package com.oumen.message;


public enum ActionType {
	CHAT {
		@Override
		public String text() {
			return "chat";
		}

		@Override
		public int code() {
			return 0;
		}
	},
	ACTIVITY_MULTI_CHAT {
		@Override
		public String text() {
			return "act";
		}

		@Override
		public int code() {
			return 1;
		}
	},
	CONFIRM_FRIEND {
		@Override
		public String text() {
			return "confirm_friend";
		}

		@Override
		public int code() {
			return 2;
		}
	},
	AGREE_FRIEND {
		@Override
		public String text() {
			return "agree_friend";
		}

		@Override
		public int code() {
			return 3;
		}
	},
	REQUEST_FRIEND {
		@Override
		public String text() {
			return "apply_friend";
		}

		@Override
		public int code() {
			return 4;
		}
	},
	HELP {
		@Override
		public String text() {
			return "help";
		}

		@Override
		public int code() {
			return 5;
		}
	},
	MULTI_CREATE {
		@Override
		public String text() {
			return "teamcreated";
		}

		@Override
		public int code() {
			return 6;
		}
	},
	MULTI_JOIN {
		@Override
		public String text() {
			return "jointeam";
		}

		@Override
		public int code() {
			return 7;
		}
	},
	PUSH_END {
		@Override
		public String text() {
			return "end";
		}

		@Override
		public int code() {
			return 8;
		}
	},
	CIRCLE {
		@Override
		public String text() {
			return "cycle";
		}

		@Override
		public int code() {
			return 9;
		}
	},
	OTHER {
		@Override
		public String text() {
			return null;
		}

		@Override
		public int code() {
			return -1;
		}
	};
	
	public static ActionType parseActionType(String type) {
		if (ActionType.CHAT.text().equals(type)) {
			return ActionType.CHAT;
		}
		else if (ActionType.ACTIVITY_MULTI_CHAT.text().equals(type)) {
			return ActionType.ACTIVITY_MULTI_CHAT;
		}
		else if (ActionType.HELP.text().equals(type)) {
			return ActionType.HELP;
		}
		else if (ActionType.CONFIRM_FRIEND.text().equals(type)) {
			return ActionType.CONFIRM_FRIEND;
		}
		else if (ActionType.AGREE_FRIEND.text().equals(type)) {
			return ActionType.AGREE_FRIEND;
		}
		else if (ActionType.REQUEST_FRIEND.text().equals(type)) {
			return ActionType.REQUEST_FRIEND;
		}
		else if (ActionType.MULTI_CREATE.text().equals(type)) {
			return ActionType.MULTI_CREATE;
		}
		else if (ActionType.MULTI_JOIN.text().equals(type)) {
			return ActionType.MULTI_JOIN;
		}
		else if (ActionType.PUSH_END.text().equals(type)) {
			return ActionType.PUSH_END;
		}
		return ActionType.OTHER;
	}
	
	public static ActionType parseActionType(int type) {
		if (ActionType.CHAT.code() == type) {
			return ActionType.CHAT;
		}
		else if (ActionType.ACTIVITY_MULTI_CHAT.code() == type) {
			return ActionType.ACTIVITY_MULTI_CHAT;
		}
		else if (ActionType.HELP.code() == type) {
			return ActionType.HELP;
		}
		else if (ActionType.CONFIRM_FRIEND.code() == type) {
			return ActionType.CONFIRM_FRIEND;
		}
		else if (ActionType.AGREE_FRIEND.code() == type) {
			return ActionType.AGREE_FRIEND;
		}
		else if (ActionType.REQUEST_FRIEND.code() == type) {
			return ActionType.REQUEST_FRIEND;
		}
		else if (ActionType.MULTI_CREATE.code() == type) {
			return ActionType.MULTI_CREATE;
		}
		else if (ActionType.MULTI_JOIN.code() == type) {
			return ActionType.MULTI_JOIN;
		}
		else if (ActionType.PUSH_END.code() == type) {
			return ActionType.PUSH_END;
		}
		return ActionType.OTHER;
	}
	
	abstract public String text();
	abstract public int code();
}

package com.oumen.message;

public enum Type {
	TEXT {
		@Override
		public String text() {
			return "txt";
		}

		@Override
		public int code() {
			return 0;
		}
	},
	OUBA {
		@Override
		public String text() {
			return "emj";
		}

		@Override
		public int code() {
			return 1;
		}
	},
	CIWEI {
		@Override
		public String text() {
			return "emj2";
		}

		@Override
		public int code() {
			return 2;
		}
	},
	AUDIO {
		@Override
		public String text() {
			return "aud";
		}

		@Override
		public int code() {
			return 3;
		}
	},
	IAMGE {
		@Override
		public String text() {
			return "img";
		}

		@Override
		public int code() {
			return 4;
		}
	},
	ENJOY {
		@Override
		public String text() {
			return "prize";
		}

		@Override
		public int code() {
			return 5;
		}
	},
	COMMENT {
		@Override
		public String text() {
			return "common";
		}

		@Override
		public int code() {
			return 6;
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
	
	public static Type parseType(String type) {
		if (Type.TEXT.text().equals(type))
			return Type.TEXT;
		else if (Type.OUBA.text().equals(type))
			return Type.OUBA;
		else if (Type.CIWEI.text().equals(type))
			return Type.CIWEI;
		else if (Type.AUDIO.text().equals(type))
			return Type.AUDIO;
		else if (Type.IAMGE.text().equals(type))
			return Type.IAMGE;
		else if (Type.ENJOY.text().equals(type))
			return Type.ENJOY;
		else if (Type.COMMENT.text().equals(type))
			return Type.COMMENT;
		else
			return Type.OTHER;
	}
	
	public static Type parseMessageType(int type) {
		if (Type.TEXT.code() == type)
			return Type.TEXT;
		else if (Type.OUBA.code() == type)
			return Type.OUBA;
		else if (Type.CIWEI.code() == type)
			return Type.CIWEI;
		else if (Type.AUDIO.code() == type)
			return Type.AUDIO;
		else if (Type.IAMGE.code() == type)
			return Type.IAMGE;
		else if (Type.ENJOY.code() == type)
			return Type.ENJOY;
		else if (Type.COMMENT.code() == type)
			return Type.COMMENT;
		else
			return Type.OTHER;
	}
	
	abstract public String text();
	abstract public int code();
}

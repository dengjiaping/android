package com.oumen.mv;

import java.util.ArrayList;
import java.util.List;

public class PrefixVideoGroup {
	protected int type;
	protected String title;
	protected final List<PrefixVideo> items = new ArrayList<PrefixVideo>();

	public PrefixVideoGroup(int type, String title) {
		this.type = type;
		this.title = title;
	}

}

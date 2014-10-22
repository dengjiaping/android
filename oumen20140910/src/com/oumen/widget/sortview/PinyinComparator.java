package com.oumen.widget.sortview;

import java.util.Comparator;

public class PinyinComparator implements Comparator<SortDataItem<?>> {

	public int compare(SortDataItem<?> o1, SortDataItem<?> o2) {
		char c1 = o1.getFirst(), c2 = o2.getFirst();
		if (c1 == '@' || c2 == '#') {
			return -1;
		}
		else if (c1 == '#' || c2 == '@') {
			return 1;
		}
		else {
			if (c1 > c2)
				return 1;
			else if (c1 < c2)
				return -1;
			else
				return 0;
		}
	}

}

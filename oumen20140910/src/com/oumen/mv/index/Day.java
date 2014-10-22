package com.oumen.mv.index;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.oumen.android.App;
import com.oumen.mv.MvInfo;
import com.oumen.mv.index.UploadTask.UploadListener;

public class Day {
	protected Calendar date;
	protected boolean today;
	protected final List<MvInfo> list = new ArrayList<MvInfo>();
	
	Day(Calendar date) {
		this.date = date;
		List<MvInfo> results = MvInfo.query(App.PREFS.getUid(), date, App.DB);
		list.addAll(results);
	}
	
	public void setUploadListener(UploadListener listener) {
		for (MvInfo i : list) {
			i.getUploadTask().setListener(listener);
		}
	}
}

package com.oumen.android.peers;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class PutActivityUtil {
	public static List<Activity> lists = new ArrayList<Activity>();
	public static void addActivity(Activity a){
		lists.add(a);
	}
	public static void closeActivity(){
		for(Activity a:lists){
			if(a != null){
				a.finish();
			}
		}
	}
}

package com.oumen.app;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import com.oumen.tools.ELog;

import android.app.Activity;

public class ActivityStack {
	
	private static final LinkedList<WeakReference<Activity>> activities = new LinkedList<WeakReference<Activity>>();
	
	public static void add(Activity activity) {
		synchronized (activities) {
			boolean has = false;
			for (WeakReference<Activity> i : activities) {
				if (i.get() == activity) {
					has = true;
					break;
				}
			}
			
			if (!has)
				activities.add(new WeakReference<Activity>(activity));
			
			ELog.i(has ? "Had " : "Add " + activity);
		}
	}
	
	public static boolean has(Activity activity) {
		synchronized (activities) {
			for (WeakReference<Activity> i : activities) {
				if (i.get() == activity)
					return true;
			}
		}
		
		return false;
	}
	
	public static boolean has(Class<?> activity) {
		synchronized (activities) {
			for (WeakReference<Activity> i : activities) {
				if (i.get().getClass().equals(activity))
					return true;
			}
		}
		
		return false;
	}
	
	public static boolean remove(Activity activity) {
		synchronized (activities) {
			WeakReference<Activity> ref = null;
			for (WeakReference<Activity> i : activities) {
				if (i.get() == activity) {
					ref = i;
					break;
				}
			}
			
			if (ref != null) {
				ELog.i(ref.get() == null ? "" : ref.get().toString());
				activities.remove(ref);
				return true;
			}
		}
		
		return false;
	}
	
	public static Activity getCurrent() {
		if (activities.isEmpty())
			return null;
		
		return activities.getLast().get();
	}
	
	public static void finishAll() {
		synchronized (activities) {
			WeakReference<Activity> ref = null;
			while ((ref = activities.pollLast()) != null) {
				Activity act = ref.get();
				if (act != null) {
					ELog.i("Finish:" + act.toString());
					act.finish();
				}
			}
			activities.clear();
		}
	}
	
	public static int size() {
		return activities.size();
	}
}

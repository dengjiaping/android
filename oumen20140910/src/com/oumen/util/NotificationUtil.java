package com.oumen.util;

import com.oumen.android.App;

import android.app.Notification;
import android.content.Context;
import android.media.AudioManager;

public class NotificationUtil {
	/**
	 * 设置notification参数
	 * @param context
	 * @param notification
	 * @return
	 */
	public static Notification setAlarmParams(Context context, Notification notification) {
		AudioManager volMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		switch (volMgr.getRingerMode()) {
			case AudioManager.RINGER_MODE_SILENT:// 静音模式
				notification.sound = null;
				notification.vibrate = null;
				break;
			case AudioManager.RINGER_MODE_NORMAL://常规模式
				// 有两种状态：1.响铃但不震动，2.响铃+震动
				if (App.PREFS.isShakeOpen()) {
					notification.defaults = Notification.DEFAULT_VIBRATE;
				}
				else {
					if (App.PREFS.isSoundOpen()) {
						notification.defaults = Notification.DEFAULT_SOUND;
					}
					else {
						notification.defaults = 0;
						notification.sound = null;
					}
					notification.vibrate = null;
				}
				
				if (App.PREFS.isSoundOpen()) {
					notification.defaults = Notification.DEFAULT_SOUND;
				}
				else {
					notification.sound = null;
				}
				break;
			case AudioManager.RINGER_MODE_VIBRATE://震动
				notification.sound = null;
				
				if (App.PREFS.isShakeOpen()) {
					notification.defaults = Notification.DEFAULT_VIBRATE;
				}
				else {
					notification.defaults = 0;
					notification.vibrate = null;
				}
				break;
		}
		return notification;
	}
}

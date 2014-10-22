package com.oumen.app;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.oumen.tools.ELog;

public class BaseApplication extends Application {
	public static final int INT_UNSET = -1;
	
	public static final String SCHEMA_FILE = "file://";
	public static final String SCHEMA_HTTP = "http://";
	public static final String SCHEMA_HTTPS = "https://";

	public static final Locale LOCALE = Locale.CHINA;
	
	public static final SimpleDateFormat DATE_FORMAT_YYYY_MM_HH = new SimpleDateFormat("yyyy-MM-dd", LOCALE);

	public static final ExecutorService THREAD = Executors.newFixedThreadPool(50);
	public static final DisplayImageOptions DEFAULT_IMAGE_LOADER_OPTIONS = new DisplayImageOptions.Builder()
		.cacheOnDisk(true)
		.delayBeforeLoading(300)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();

	@Override
	public void onCreate() {
		super.onCreate();
		ELog.i("");
		
		//--------------------cache--------------------//
		String image_cache = getCacheDir().getAbsolutePath() + "/images";
		File cacheDir = new File(image_cache);
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}

		//--------------------Image Loader--------------------//
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
			.threadPriority(Thread.NORM_PRIORITY + 5)
			.denyCacheImageMultipleSizesInMemory()
			.taskExecutor(THREAD)
			.tasksProcessingOrder(QueueProcessingType.FIFO)
			.defaultDisplayImageOptions(DEFAULT_IMAGE_LOADER_OPTIONS)
			.build();
		ImageLoader.getInstance().init(config);

		//--------------------Activity Lifecycle--------------------//
		registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
				ActivityStack.add(activity);
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				ActivityStack.remove(activity);
			}

			@Override
			public void onActivityStopped(Activity activity) {
			}

			@Override
			public void onActivityStarted(Activity activity) {
			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
			}

			@Override
			public void onActivityResumed(Activity activity) {
			}

			@Override
			public void onActivityPaused(Activity activity) {
			}
		});
	}
	
	private static long lastRingTimestamp;
	
	public static final Uri[] SYSTEM_RING = new Uri[] {
		Settings.System.DEFAULT_NOTIFICATION_URI,
        Settings.System.DEFAULT_RINGTONE_URI,
        Settings.System.DEFAULT_ALARM_ALERT_URI
	};

	public static void ring(Context context) {
		long now = System.currentTimeMillis();
		if (now < lastRingTimestamp)
			return;
		
		MediaPlayer player = null;
		for (Uri i : SYSTEM_RING) {
			player = MediaPlayer.create(context, i);
			if (player != null) {
				lastRingTimestamp = now + 5000;//响铃间隔5秒,即这次响铃后5秒内不再响
				player.start();
				break;
			}
		}
	}

	public static void destory() {
		ELog.i("");
		ActivityStack.finishAll();
	}

	@Override
	public void onTerminate() {
		destory();
		THREAD.shutdown();
		super.onTerminate();
	}
}

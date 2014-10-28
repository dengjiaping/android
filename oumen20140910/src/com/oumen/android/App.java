package com.oumen.android;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.oumen.MainActivity;
import com.oumen.R;
import com.oumen.android.util.Constants;
import com.oumen.android.util.SharePreferenceUtil;
import com.oumen.app.BaseApplication;
import com.oumen.base.Cache;
import com.oumen.biaoqing.SmallBiaoQing;
import com.oumen.cities.City;
import com.oumen.cities.CityUtils;
import com.oumen.db.DatabaseHelper;
import com.oumen.db.PinYin;
import com.oumen.http.HttpRequest;
import com.oumen.message.MessageService;
import com.oumen.message.connection.MessageConnection;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.oumen.util.CircleImageLoadingListener;
import com.oumen.widget.sortview.PinyinComparator;
import com.oumen.widget.sortview.SortDataItem;

public class App extends BaseApplication implements OnGetGeoCoderResultListener {
	public static final int IMAGE_SIZE_MAX = 1200;
	public static final int IMAGE_LENGTH_MAX = 800 * 1024;
	public static final int IMAGE_LENGTH_SMALL = 16 * 1024;

	public static final int DEFAULT_LIMIT = 30;//查询数据库时默认的Limit

	public static final int HANDLER_TOAST = 99;

	public static final String PATH_APP = Environment.getExternalStorageDirectory().getAbsolutePath() + "/oumen";

	private static String PATH_DOWNLOAD_CACHE = "/temp/dl";
	private static String PATH_UPLOAD_CACHE = "/temp/ul";

	private static String PATH_MV_IMAGES_CACHE = "/temp/images";
	private static String PATH_MV_VIDEOS_CACHE = "/temp/videos";
	private static String PATH_MV_SOURCE_CACHE = "/temp/source";
	public static final String PATH_MV = PATH_APP + "/mv";
	public static final String PATH_VIDEO_COVER = PATH_MV + "/cover";
	public static final String PATH_VIDEO_PREFIX = PATH_MV + "/p";
	public static final String PATH_VIDEO_SUFFIX = PATH_MV + "/s";
	
	public static final String PATH_AUDIO_MESSAGE = PATH_APP + "/audio";

	public static final String PATH_SPLASH_IMAGE = PATH_APP + "/splash";

	//表情下载保存目录
	private static final String ASSETS_FILE_PATH = "emoji";
	private static final String PATH_CHAT_BIAOQING_OUBA = PATH_APP + "/ouba";
	private static final String PATH_CHAT_BIAOQING_CIWEI = PATH_APP + "/ciwei";
	public static final String FILE_SUFFIX = ".o";

	public static final String GUEST_EMAIL_SUFFIX = "@guest.com";

	public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("00");
	public static final DecimalFormat RATE_FORMAT = new DecimalFormat("##0.0"); 
	public static final SimpleDateFormat YYYY_MM_DD_FORMAT = new SimpleDateFormat("yyyy-MM-dd", LOCALE);
	public static final SimpleDateFormat YYYY_MM_DD_CHINESE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日", LOCALE);
	public static final SimpleDateFormat YYYY_MM_DD_HH_MM_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", LOCALE);
	public static final SimpleDateFormat MM_DD_HH_MM_FORMAT = new SimpleDateFormat("MM-dd HH:mm", LOCALE);
	public static final SimpleDateFormat MM_DD_FORMAT = new SimpleDateFormat("MM月dd日", LOCALE);

	public static final SimpleDateFormat HH_MM_FORMAT = new SimpleDateFormat("HH:mm", LOCALE);

	public static final UserProfile USER = new UserProfile(); // 用户信息
	
	public static Map<String, List<City>> CITIES = new HashMap<String, List<City>>(); // 省份和城市列表
	public static LinkedList<SortDataItem<City>> ALLCITIES = new LinkedList<SortDataItem<City>>();
	
	public static Cache CACHE;
	public static SharePreferenceUtil PREFS;
	public static DatabaseHelper DB;

	public static float latitude = 39.919293f;
	public static float longitude = 116.526991f;

	// 通知管理器
	private NotificationManager mNotificationManager;

	private static long timeOffset;// 本机时间与服务器时间差

	public static SmallBiaoQing SMALLBIAOQING;

	public static final DisplayImageOptions OPTIONS_HEAD_ROUND = new DisplayImageOptions.Builder().
			showImageForEmptyUri(R.drawable.round_user_photo).
			showImageOnFail(R.drawable.round_user_photo)
//			.resetViewBeforeLoading(true)
			.cacheOnDisk(true).
			imageScaleType(ImageScaleType.EXACTLY).
			bitmapConfig(Bitmap.Config.RGB_565).build();

	public static final DisplayImageOptions OPTIONS_HEAD_RECT = new DisplayImageOptions.Builder().
			showImageForEmptyUri(R.drawable.rectangle_photo).
			showImageOnFail(R.drawable.rectangle_photo).
			resetViewBeforeLoading(true).
			cacheOnDisk(true).
			cacheInMemory(true).
			imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565).
			preProcessor(new BitmapProcessor() {

				@Override
				public Bitmap process(Bitmap bitmap) {
					return ImageTools.clip2square(bitmap);
				}
			}).build();

	public static final DisplayImageOptions OPTIONS_PIC = new DisplayImageOptions.Builder().
			showImageForEmptyUri(R.drawable.pic_default).
			showImageOnFail(R.drawable.pic_default).
			showImageOnLoading(R.drawable.pic_default).
			resetViewBeforeLoading(true).
			cacheOnDisk(true).
			imageScaleType(ImageScaleType.EXACTLY).
			bitmapConfig(Bitmap.Config.RGB_565).
			preProcessor(new BitmapProcessor() {

				@Override
				public Bitmap process(Bitmap bitmap) {
					return ImageTools.clip2square(bitmap);
				}
			})
			.build();
	public static final DisplayImageOptions OPTIONS_ROUND_PIC = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.pic_default)
			.showImageOnFail(R.drawable.pic_default)
			.showImageOnLoading(R.drawable.pic_default)
			.resetViewBeforeLoading(true)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new RoundedBitmapDisplayer(10))
			.preProcessor(new BitmapProcessor() {

				@Override
				public Bitmap process(Bitmap bitmap) {
					return ImageTools.clip2square(bitmap);
				}
			})
			.build();
	
	public static final DisplayImageOptions OPTIONS_DEFAULT_PIC = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.pic_default)
			.showImageOnFail(R.drawable.pic_default)
			.resetViewBeforeLoading(true)
			.cacheOnDisk(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

	public static final CircleImageLoadingListener CIRCLE_IMAGE_LOADING_LISTENER = new CircleImageLoadingListener();

	public static int DEFAULT_PHOTO_SIZE = 150;
	public static int DEFAULT_CELL_SIZE = 160;
	public static int SCREEN_WIDTH = 480;
	public static int SCREEN_HEIGHT = 800;
	public static int ACTIVITYS_LIST_ITEM_PIC_WIDTH = 94;
	public static int BIG_PHOTO_SIZE = 84;
	public static int MIDDLE_PHOTO_SIZE = 64;
	public static int CHAT_BIAOQING_SIZE = 48;

	private Thread.UncaughtExceptionHandler systemExceptionHandler;

	// 百度地图相关组件
	public static LocationClient locationClient;
	
	private GeoCoder mSearch = null; // 百度地图的搜索模块

	@Override
	public void onCreate() {
		super.onCreate();
		ELog.i("");
		long time1= System.currentTimeMillis();

		systemExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				ELog.e("Default exception:" + ex.getMessage());
				ex.printStackTrace();

				PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
				AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				alarm.set(AlarmManager.RTC, System.currentTimeMillis() + 1500, pending);

				systemExceptionHandler.uncaughtException(thread, ex);
			}
		});

		CACHE = new Cache(getApplicationContext());

		PREFS = new SharePreferenceUtil(getApplicationContext(), Constants.SAVE_USER);

		//--------------------cache--------------------//
		PATH_DOWNLOAD_CACHE = getCacheDir().getAbsolutePath() + PATH_DOWNLOAD_CACHE;
		File cache = new File(PATH_DOWNLOAD_CACHE);
		if (!cache.exists()) {
			cache.mkdirs();
		}

		PATH_UPLOAD_CACHE = getCacheDir().getAbsolutePath() + PATH_UPLOAD_CACHE;
		cache = new File(PATH_UPLOAD_CACHE);
		if (!cache.exists()) {
			cache.mkdirs();
		}

		PATH_MV_IMAGES_CACHE = getCacheDir().getAbsolutePath() + PATH_MV_IMAGES_CACHE;
		cache = new File(PATH_MV_IMAGES_CACHE);
		if (!cache.exists()) {
			cache.mkdirs();
		}

		PATH_MV_VIDEOS_CACHE = getCacheDir().getAbsolutePath() + PATH_MV_VIDEOS_CACHE;
		cache = new File(PATH_MV_VIDEOS_CACHE);
		if (!cache.exists()) {
			cache.mkdirs();
		}

		PATH_MV_SOURCE_CACHE = getCacheDir().getAbsolutePath() + PATH_MV_SOURCE_CACHE;
		cache = new File(PATH_MV_SOURCE_CACHE);
		if (!cache.exists()) {
			cache.mkdirs();
		}

		DEFAULT_PHOTO_SIZE = getResources().getDimensionPixelSize(R.dimen.default_photo_size);
		DEFAULT_CELL_SIZE = getResources().getDimensionPixelSize(R.dimen.default_cell_size);
		BIG_PHOTO_SIZE = getResources().getDimensionPixelSize(R.dimen.big_photo_size);
		MIDDLE_PHOTO_SIZE = getResources().getDimensionPixelSize(R.dimen.medium_photo_size);

		SCREEN_WIDTH = getResources().getDisplayMetrics().widthPixels;
		SCREEN_HEIGHT = getResources().getDisplayMetrics().heightPixels;

		ACTIVITYS_LIST_ITEM_PIC_WIDTH = getResources().getDimensionPixelSize(R.dimen.activitys_list_item_pic_size);

		DB = new DatabaseHelper(this);

		THREAD.execute(new Runnable() {

			@Override
			public void run() {
				// 初始化拼音数据库
				PinYin.initialize(getApplicationContext(), DB);
				
				// 创建城市数据文件
//				CityPickerActiviry.initCityDatabase(getApplicationContext());
				try {
					CITIES = CityUtils.getProvincesData(getApplicationContext());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				PinyinComparator comparator = new PinyinComparator();
				
				LinkedList<City> data = new LinkedList<City>();
				Iterator<Map.Entry<String, List<City>>> iter = App.CITIES.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, List<City>> entry = (Map.Entry<String, List<City>>) iter.next();
					data.addAll((List<City>)entry.getValue());
				}
				
				for (int i = 0; i< data.size(); i++) {
					if (TextUtils.isEmpty(data.get(i).getName()))
					 continue;
					
					String py = PinYin.getPinYin(data.get(i).getName(), App.DB).toUpperCase(App.LOCALE);
					ALLCITIES.add(new SortDataItem<City>(data.get(i), py));
				}
				
				Collections.sort(ALLCITIES, comparator);
			}
		});

		// 百度地图
		SDKInitializer.initialize(getApplicationContext());

		locationClient = new LocationClient(this.getApplicationContext());
		locationClient.registerLocationListener(new MyLocationListener());

		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型 bd09ll gcj02
		option.setScanSpan(1000);
		locationClient.setLocOption(option);
		locationClient.start();
		
		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		
		if (PREFS.getLatitude() != 0.0f && PREFS.getLongitude() != 0.0f) {
			latitude = PREFS.getLatitude();
			longitude = PREFS.getLongitude();
		}
		//TODO 初始化缓存的城市经纬度
		CACHE.save(Cache.CACHE_USER_CHOOSE_LATITUDE, latitude);
		CACHE.save(Cache.CACHE_USER_CHOOSE_LONGITUDE, longitude);
		
		registerReceiver(NETWORK_RECEIVER, NETWORK_RECEIVER_FILTER);
		updateNetworkType(getApplicationContext());

		SMALLBIAOQING = new SmallBiaoQing();
		SMALLBIAOQING.initialize(getApplicationContext());
		ELog.e("App创建时间："+ (System.currentTimeMillis() - time1));
	}

	public NotificationManager getmNotificationManager() {
		return mNotificationManager;
	}

	public void setmNotificationManager(NotificationManager mNotificationManager) {
		this.mNotificationManager = mNotificationManager;
	}

	public long getTimeOffset() {
		return timeOffset;
	}

	public void setTimeOffset(long timeOffset) {
		App.timeOffset = timeOffset;
	}

	public static Long getServerTime() {
		return System.currentTimeMillis() + timeOffset;
	}

	public static String getSmallPicUrl(String url, int maxLength) {
		return url + "/small?l=" + maxLength;
	}

	public static String getMvImagesCachePath() {
		return PATH_MV_IMAGES_CACHE;
	}

	public static String getMvVideosCachePath() {
		return PATH_MV_VIDEOS_CACHE;
	}

	public static String getMvSourceCachePath() {
		return PATH_MV_SOURCE_CACHE;
	}

	public static String getDownloadCachePath() {
		return PATH_DOWNLOAD_CACHE;
	}

	public static String getUploadCachePath() {
		return PATH_UPLOAD_CACHE;
	}

	public static String getChatBiaoqingOubaPath() {
		return PATH_CHAT_BIAOQING_OUBA;
	}

	public static String getChatBiaoqingCiweiPath() {
		return PATH_CHAT_BIAOQING_CIWEI;
	}

	public static String getAssertBiaoqingPath() {
		return ASSETS_FILE_PATH;
	}

	public static void destory() {
		ELog.i("");
		BaseApplication.destory();
		if (MessageConnection.instance.isConnected())
			MessageConnection.instance.close(false, true);
	}

	@Override
	public void onTerminate() {
		DB.close();
		super.onTerminate();
	}

	/**
	 * 实现定位回调监听
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			
			final float lat = (float)location.getLatitude();
			final float lng = (float)location.getLongitude();
			
			if (lat == 0.0f && lng == 0.0f) 
				return;
			latitude = lat;
			longitude = lng;
			

			//TODO 将消息保存到prefs
			App.PREFS.setLatitude(latitude);
			App.PREFS.setLongitude(longitude);
			
			// 根据经纬度定位出城市
			LatLng ptCenter = new LatLng(latitude, longitude);
			mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter));

			// 向服务器发送位置信息
			App.THREAD.execute(new Runnable() {
				
				@Override
				public void run() {
					while (App.getNetworkType() == null) {
						try {TimeUnit.MILLISECONDS.sleep(400);}catch (Exception e){}
					}

					if (!NetworkType.NONE.equals(App.getNetworkType())) {
						ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(4);
						params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
						params.add(new BasicNameValuePair("lat", String.valueOf(lat)));
						params.add(new BasicNameValuePair("lng", String.valueOf(lng)));
						HttpRequest req = new HttpRequest(Constants.GPS_URL, params, HttpRequest.Method.GET, null);
						App.THREAD.execute(req);
					}
				}
			});
			
			// 停止定位
			locationClient.stop();
		}

		@Override
		public void onReceivePoi(BDLocation location) {
		}
	}

	//--------------------- Network Receiver ---------------------//
	public enum NetworkType {
		NONE, WIFI, MOBILE, OTHER
	};

	private static NetworkType networkType;

	public static NetworkType getNetworkType() {
		return networkType;
	}

	private static void updateNetworkType(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 获取WIFI网络连接状态
		NetworkInfo info = cm.getActiveNetworkInfo();
		int type = info == null ? -1 : info.getType();
		switch (type) {
			case -1:
				networkType = NetworkType.NONE;
				break;

			case ConnectivityManager.TYPE_MOBILE:
			case ConnectivityManager.TYPE_MOBILE_MMS:
			case ConnectivityManager.TYPE_MOBILE_SUPL:
			case ConnectivityManager.TYPE_MOBILE_DUN:
			case ConnectivityManager.TYPE_MOBILE_HIPRI:
				networkType = NetworkType.MOBILE;
				break;

			case ConnectivityManager.TYPE_WIFI:
				networkType = NetworkType.WIFI;
				break;

			default:
				networkType = NetworkType.OTHER;
		}
	}

	private final IntentFilter NETWORK_RECEIVER_FILTER = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

	private final BroadcastReceiver NETWORK_RECEIVER = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateNetworkType(context);
			ELog.i("Network:" + networkType);

			sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_NETWORK_STATUS));
		}
	};

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) 
			return;
		//TODO 怎么设置
		ELog.i("" + result.getAddressDetail().city);
		if (!TextUtils.isEmpty(result.getAddressDetail().city)) {
			String str = result.getAddressDetail().city;
			if (str.endsWith("市")) {
				str = str.substring(0, str.length() - "市".length());
			}
			PREFS.setCurrentCityName(str);
			if (TextUtils.isEmpty(CACHE.read(Cache.CACHE_USER_CHOOSE_CITY_NAME))) {
				CACHE.save(Cache.CACHE_USER_CHOOSE_CITY_NAME, str);
			}
			sendBroadcast(MessageService.createResponseNotify(MessageService.TYPE_NOTIFY_LOCATION));
		}
	}
}

package com.oumen.mv;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Service;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.IBinder;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.app.BaseApplication;
import com.oumen.tools.ELog;
import com.oumen.tools.FileTools;
import com.oumen.widget.ffmpeg.FFMpeg;
import com.oumen.widget.ffmpeg.FFMpegListener;

public class MvComposeService extends Service implements FFMpegListener {
	
	public static final String MV_COMPOSE_SERVICE_ACTION = "com.oumen.mv.MvComposeService";
	public static final String MV_COMPOSE_SERVICE_NOTIFY_ACTION = "com.oumen.mv.MvComposeService.NOTIFY";

	public static final String INTENT_KEY_PARAM_ACTION = "action";
	public static final String INTENT_KEY_PARAM_IS_PREVIEW = "isPreview";
	public static final String INTENT_KEY_PARAM_TARGET = "target";
	
	public static final String INTENT_KEY_NOTIFY_TYPE = "type";
	public static final String INTENT_KEY_NOTIFY_DATA = "data";
	public static final String INTENT_KEY_NOTIFY_TITLE = "title";
	public static final String INTENT_KEY_NOTIFY_MAX = "max";
	public static final String INTENT_KEY_NOTIFY_CURRENT = "current";
	
	public static final int NOTIFY_INIT_LIBRARY = 0;
	public static final int NOTIFY_INIT_RESOURCE = 1;
	public static final int NOTIFY_PROCESS_IMAGES = 2;
	public static final int NOTIFY_BUILD_COMMANDS = 3;
	public static final int NOTIFY_PROGRESS = 96;
	public static final int NOTIFY_COMPLETED = 97;
	public static final int NOTIFY_COMPOSE_FAILED = 98;
	public static final int NOTIFY_INIT_FAILED = 99;
	
	private final String SUFFIX_VIDEO_PATH = App.PATH_VIDEO_SUFFIX + "/片尾.mp4";
	
	private static final LinkedBlockingQueue<MV> tasks = new LinkedBlockingQueue<MV>(3);
	
	private final ExecutorService thread = Executors.newSingleThreadExecutor();
	
	private File imagesCacheDir = new File(App.getMvImagesCachePath());
	private File videosCacheDir = new File(App.getMvVideosCachePath());
	private File sourceCacheDir = new File(App.getMvSourceCachePath());
//	private File videosCacheDir = new File(App.PATH_MV + "/vc");
//	private File sourceCacheDir = new File(App.PATH_MV + "/vs");
	
	public static MvInfo target;
	
	public static boolean running;

	@Override
	public void onCreate() {
		super.onCreate();
		
		running = true;
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		ELog.i("");
		if (intent != null) {
			int action = intent.getIntExtra(INTENT_KEY_PARAM_ACTION, BaseApplication.INT_UNSET);
			
			if (action == MV.ACTION_COMPOSE_WITH_IMAGES) {
				MV mv = new MV(action, 9, MvComposeService.this);//总共需要7个步骤
				mv.imagePaths = intent.getStringArrayExtra(MvActivity.PARAM_KEY_SELECTED_IMAGE);
				mv.videoPrefixPath = target.prefix.videoFile.getAbsolutePath();
				mv.prefixId = target.prefix.id;
				tasks.add(mv);
			}
			else if (action == MV.ACTION_COMPOSE_WITH_RECORD) {
				MV mv = new MV(action, 8, MvComposeService.this);
				mv.videoPath = intent.getStringExtra(MvActivity.PARAM_KEY_SELECTED_SOURCE_VIDEO);
				mv.videoPrefixPath = target.prefix.videoFile.getAbsolutePath();
				mv.prefixId = target.prefix.id;
				tasks.add(mv);
			}
			else if (action == MV.ACTION_COMPOSE_WITH_EDIT) {
				MV mv = new MV(action, 8, MvComposeService.this);
				mv.sourcePath = intent.getStringExtra(MvActivity.PARAM_KEY_SELECTED_SOURCE_VIDEO);
				mv.start = intent.getIntExtra(MvActivity.PARAM_KEY_START_TIME, 0);
				mv.end = intent.getIntExtra(MvActivity.PARAM_KEY_END_TIME, 0);
				mv.videoPrefixPath = target.prefix.videoFile.getAbsolutePath();
				mv.prefixId = target.prefix.id;
				tasks.add(mv);
			}
			else if (action == MV.ACTION_COMPOSE_FINAL) {
				MV mv = new MV(action, 3, MvComposeService.this);
				try {
					mv.init(getApplicationContext(), R.raw.ffmpeg);
				}
				catch (IOException e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
				mv.targetName = intent.getStringExtra(MvActivity.PARAM_KEY_TITLE);
				mv.previewPath = intent.getStringExtra(MvActivity.PARAM_KEY_PREVIEW_FILE);
				mv.audioPath = intent.getStringExtra(MvActivity.PARAM_KEY_SELECTED_AUDIO);
				mv.prefixId = target.prefix.id;
				tasks.add(mv);

				target.type = MvInfo.TYPE_COMPOSE;
				target.title = mv.targetName;
				MvInfo.insert(target, App.DB);
			}
			thread.execute(compose);
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	private final Runnable compose = new Runnable() {
		
		@Override
		public void run() {
			int action = 0;
			try {
				MV mv = tasks.take();
				action = mv.action;
				
				Intent notify = new Intent(MV_COMPOSE_SERVICE_NOTIFY_ACTION);
				notify.putExtra(INTENT_KEY_PARAM_ACTION, mv.action);
				
				if (mv.action == MV.ACTION_COMPOSE_FINAL) {
					File srcAudio = new File(mv.audioPath);
					File tmpAudio = new File(videosCacheDir, "audio.mp3");
					FileTools.copyFile(srcAudio, tmpAudio);
					
					mv.audioPath = tmpAudio.getAbsolutePath();
					mv.composeVideoAndAudio(videosCacheDir);

					notify.putExtra(INTENT_KEY_NOTIFY_TYPE, NOTIFY_BUILD_COMMANDS);
					notify.putExtra(INTENT_KEY_NOTIFY_TITLE, mv.targetName);
					notify.putExtra(INTENT_KEY_NOTIFY_MAX, mv.getSteps());
					notify.putExtra(INTENT_KEY_NOTIFY_CURRENT, mv.increase());
					sendBroadcast(notify);
				}
				else {
					mv.reset(imagesCacheDir, videosCacheDir, null);
					
					boolean isInited = false;
					try {
						isInited = mv.init(getApplicationContext(), R.raw.ffmpeg);
					}
					catch (IOException e) {
						ELog.e("Exception:" + e.getMessage());
						error(mv, NOTIFY_INIT_FAILED, notify);
						e.printStackTrace();
					}

					if (!isInited) {
						error(mv, NOTIFY_INIT_FAILED, notify);
						return;
					}
					else {
						//发送初始化FFMpeg完成通知
						notify.putExtra(INTENT_KEY_NOTIFY_TYPE, NOTIFY_INIT_LIBRARY);
						notify.putExtra(INTENT_KEY_NOTIFY_MAX, mv.getSteps());
						notify.putExtra(INTENT_KEY_NOTIFY_CURRENT, mv.increase());
						sendBroadcast(notify);
					}

					switch (mv.action) {
						case MV.ACTION_COMPOSE_WITH_IMAGES:
							composeWithImages(mv, notify);
							break;
							
						case MV.ACTION_COMPOSE_WITH_RECORD:
							composeWithRecord(mv, notify);
							break;
							
						case MV.ACTION_COMPOSE_WITH_EDIT:
							composeWithEdit(mv, notify);
							break;
					}
				}
				
				mv.run();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				Intent notify = new Intent(MV_COMPOSE_SERVICE_NOTIFY_ACTION);
				notify.putExtra(INTENT_KEY_PARAM_ACTION, action);
				notify.putExtra(INTENT_KEY_NOTIFY_TYPE, NOTIFY_COMPOSE_FAILED);
				sendBroadcast(notify);
				return;
			}
		}
	};
	
	private int composeInit(MV mv, Intent notify) {
		ELog.i("");
		
		File videoFile = new File(mv.videoPrefixPath);
		if (!videoFile.exists()) {
			error(mv, NOTIFY_INIT_FAILED, notify);
			return -1;
		}
		
		if (!imagesCacheDir.exists())
			imagesCacheDir.mkdirs();
		
		if (!videosCacheDir.exists())
			videosCacheDir.mkdirs();
		
		MediaMetadataRetriever metadata = new MediaMetadataRetriever();
		metadata.setDataSource(mv.videoPrefixPath);
		int duration = Integer.parseInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
		metadata.release();
		
		metadata = new MediaMetadataRetriever();
		metadata.setDataSource(SUFFIX_VIDEO_PATH);
		duration += Integer.parseInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
		metadata.release();

		//发送初始化资源完成通知
		notify.putExtra(INTENT_KEY_NOTIFY_TYPE, NOTIFY_INIT_RESOURCE);
		notify.putExtra(INTENT_KEY_NOTIFY_MAX, mv.getSteps());
		notify.putExtra(INTENT_KEY_NOTIFY_CURRENT, mv.increase());
		sendBroadcast(notify);
		
		return duration;
	}
	
	private void composeMultiVideo(MV mv, File mainVideo, int seconds, Intent notify) {
		ELog.i("片头转换");
		File prefixVideo = mv.createComposeTempFile(videosCacheDir, new File(mv.videoPrefixPath));
		if (prefixVideo == null) {
			ELog.e("片头转换失败");
			error(mv, NOTIFY_INIT_FAILED, notify);
			return;
		}
		ELog.i("片尾转换");
		File suffixVideo = mv.createComposeTempFile(videosCacheDir, new File(SUFFIX_VIDEO_PATH));
		if (suffixVideo == null) {
			ELog.e("片尾转换失败");
			error(mv, NOTIFY_INIT_FAILED, notify);
			return;
		}
		
		// Step 4：合并视频
		ELog.i("合并视频");
		File concatVideo = mv.concat(videosCacheDir, new File[]{prefixVideo, mainVideo, suffixVideo});
		if (concatVideo == null) {
			ELog.e("合并视频失败");
			error(mv, NOTIFY_INIT_FAILED, notify);
			return;
		}
		
		//发送构建命令行完成通知
		notify.putExtra(INTENT_KEY_NOTIFY_TYPE, NOTIFY_BUILD_COMMANDS);
		notify.putExtra(INTENT_KEY_NOTIFY_MAX, mv.getSteps());
		notify.putExtra(INTENT_KEY_NOTIFY_CURRENT, mv.increase());
		notify.putExtra(MvActivity.PARAM_KEY_PREVIEW_FILE, concatVideo.getAbsolutePath());
		sendBroadcast(notify);
	}
	
	private void composeWithRecord(MV mv, Intent notify) {
		int seconds = composeInit(mv, notify);
		if (seconds == -1)
			return;
		
		MediaMetadataRetriever metadata = new MediaMetadataRetriever();
		metadata.setDataSource(mv.videoPath);
		seconds += Integer.parseInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
		metadata.release();
		
		// Step 1：将录制的.mp4文件转成.ts格式
		File recordFile = mv.createComposeTempFile(videosCacheDir, new File(mv.videoPath));
		
		composeMultiVideo(mv, recordFile, seconds, notify);
	}
	
	private void composeWithEdit(MV mv, Intent notify) {
		int seconds = composeInit(mv, notify);
		if (seconds == -1)
			return;
		
		seconds += (mv.end - mv.start) * 1000;
		
		File tmpSource = new File(mv.sourcePath);
		String ext = mv.sourcePath.substring(mv.sourcePath.lastIndexOf('.'));
		File target = new File(sourceCacheDir, "edit" + ext);
		ELog.i("Copy file");
		if (target.exists())
			target.delete();
		FileTools.copyFile(tmpSource, target);
		mv.sourcePath = target.getAbsolutePath();
		
		target = new File(sourceCacheDir, "edit.ts");
		if (target.exists())
			target.delete();
		
		mv.clip(target);
		
		composeMultiVideo(mv, target, seconds, notify);
	}
	
	private void composeWithImages(MV mv, Intent notify) {
		int seconds = composeInit(mv, notify);
		if (seconds == -1)
			return;
		
		seconds += mv.imagePaths.length * MV.PER_IMAGE_SHOW_TIME * 1000;
		ELog.i("Duration:" + seconds + "s");
		
		// Step 1：将图片添加入缓存目录
		ELog.i("图片加入缓存目录");
		
		for (String path : mv.imagePaths) {
			if (!mv.addImage(imagesCacheDir, path)) {
				ELog.e("图片加入缓存目录失败");
				error(mv, NOTIFY_INIT_FAILED, notify);
				return;
			}
		}
		//发送图片处理完成通知
		notify.putExtra(INTENT_KEY_NOTIFY_TYPE, NOTIFY_PROCESS_IMAGES);
		notify.putExtra(INTENT_KEY_NOTIFY_TITLE, mv.targetName);
		notify.putExtra(INTENT_KEY_NOTIFY_MAX, mv.getSteps());
		notify.putExtra(INTENT_KEY_NOTIFY_CURRENT, mv.increase());
		sendBroadcast(notify);
		
		// Step 2：合成图片
		ELog.i("图片合成视频");
		File imagesVideo = mv.composeImages(imagesCacheDir, videosCacheDir, MV.PER_IMAGE_SHOW_TIME);
		if (imagesVideo == null) {
			ELog.e("图片合成视频失败");
			error(mv, NOTIFY_INIT_FAILED, notify);
			return;
		}
		
		// Step 3：把要合并的视频分别转换成mpg格式	
		composeMultiVideo(mv, imagesVideo, seconds, notify);
	}
	
	@Override
	public void onEvent(Phase phase, FFMpeg ffmpeg, Object data) {
		MV mv = (MV)ffmpeg;
		Intent notify = new Intent(MV_COMPOSE_SERVICE_NOTIFY_ACTION);
		switch (phase) {
			case PROGRESS:
				notify.putExtra(INTENT_KEY_NOTIFY_TYPE, NOTIFY_PROGRESS);
				notify.putExtra(INTENT_KEY_NOTIFY_TITLE, mv.targetName);
				notify.putExtra(INTENT_KEY_NOTIFY_MAX, mv.getSteps());
				notify.putExtra(INTENT_KEY_NOTIFY_CURRENT, mv.increase());
				sendBroadcast(notify);
				break;
				
			case COMPLETED:
				if (mv.action == MV.ACTION_COMPOSE_FINAL) {
					File finalFile = new File(mv.finalPath);
					File targetVideo = new File(target.getPath());
					FileTools.copyFile(finalFile, targetVideo);
					MvInfo.update(target.userId, target.title, target.type, MvInfo.TYPE_UNPUBLISHED, App.DB);
					mv.reset(imagesCacheDir, videosCacheDir, sourceCacheDir);
					target = null;
				}
				notify.putExtra(INTENT_KEY_NOTIFY_TYPE, NOTIFY_COMPLETED);
				notify.putExtra(INTENT_KEY_NOTIFY_TITLE, mv.targetName);
				notify.putExtra(INTENT_KEY_NOTIFY_MAX, mv.getSteps());
				notify.putExtra(INTENT_KEY_NOTIFY_CURRENT, mv.increase());
				sendBroadcast(notify);
				stopSelf();
				break;
				
			case FAILED:
				error(mv, NOTIFY_COMPOSE_FAILED, notify);
				sendBroadcast(notify);
				ELog.e("Failed");
				stopSelf();
				break;
				
			case RUNNING:
				ELog.i("Composing");
				break;
		}
	}
	
	private void error(MV mv, int type, Intent notify) {
		mv.reset(imagesCacheDir, videosCacheDir, sourceCacheDir);
		notify.putExtra(INTENT_KEY_NOTIFY_TYPE, type);
		notify.putExtra(INTENT_KEY_NOTIFY_TITLE, mv.targetName);
		notify.putExtra(INTENT_KEY_NOTIFY_MAX, mv.getSteps());
		notify.putExtra(INTENT_KEY_NOTIFY_CURRENT, mv.getCurrentStep());
		sendBroadcast(notify);
		target = null;
	}

	@Override
	public void onDestroy() {
		running = false;
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}

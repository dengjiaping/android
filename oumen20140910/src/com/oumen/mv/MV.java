package com.oumen.mv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.support.v4.app.NotificationCompat;

import com.oumen.app.BaseApplication;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.oumen.widget.ffmpeg.Command;
import com.oumen.widget.ffmpeg.FFMpeg;
import com.oumen.widget.ffmpeg.FFMpegListener;

public class MV extends FFMpeg {
	public static final int ACTION_COMPOSE_WITH_IMAGES = 1;
	public static final int ACTION_COMPOSE_WITH_RECORD = 2;
	public static final int ACTION_COMPOSE_WITH_EDIT = 3;
	public static final int ACTION_COMPOSE_FINAL = 4;
	
	public static final int PER_IMAGE_SHOW_TIME = 2;//每张图显示2秒
	public static final String BITRATE = "-b:v 1M -r 24";
	
	protected final String IMAGE_SUFFIX = ".jpg";
	protected final DecimalFormat FORMAT = new DecimalFormat("00");
	
	protected int videoWidth = DEFAULT_VIDEO_WIDTH;
	protected int videoHeight = DEFAULT_VIDEO_HEIGHT;
	
	protected int imageCount;

	protected String targetName;
	protected String audioPath;
	protected String videoPrefixPath;
	protected String videoPath;//录制或剪辑视频的地址
	protected String[] imagePaths;
	protected int prefixId;
	
	protected String sourcePath;
	protected int start;
	protected int end;
	
	protected String previewPath;
	protected String finalPath;
	
	protected int action;
	protected int steps;
	protected int currentStep;
	
	protected int notifyId;
	protected NotificationCompat.Builder notify;

	public MV(int action, int steps, FFMpegListener listener) {
		super(listener);
		this.action = action;
		this.steps = steps;
	}

	public void setVideoSize(int width, int height) {
		this.videoWidth = width;
		this.videoHeight = height;
	}
	
	public static void clear(File imagesCacheDir, File videosCacheDir, File sourceCacheDir) {
		ELog.i("");
		if (imagesCacheDir != null) {
			if (!imagesCacheDir.exists()) {
				imagesCacheDir.mkdirs();
			}
			File[] list = imagesCacheDir.listFiles();
			for (File f : list) {
				f.delete();
			}
		}
		
		if (videosCacheDir != null) {
			if (!videosCacheDir.exists()) {
				videosCacheDir.mkdirs();
			}
			File[] list = videosCacheDir.listFiles();
			for (File f : list) {
				f.delete();
			}
		}
		
		if (sourceCacheDir != null) {
			if (!sourceCacheDir.exists()) {
				sourceCacheDir.mkdirs();
			}
			File[] list = sourceCacheDir.listFiles();
			for (File f : list) {
				f.delete();
			}
		}
	}
	
	public void reset(File imagesCacheDir, File videosCacheDir, File sourceCacheDir) {
		clear(imagesCacheDir, videosCacheDir, sourceCacheDir);
		imageCount = 0;
		videoWidth = DEFAULT_VIDEO_WIDTH;
		videoHeight = DEFAULT_VIDEO_HEIGHT;
	}
	
	public boolean addImage(File imagesCacheDir, String path) {
		if (path.startsWith(BaseApplication.SCHEMA_FILE))
			path = path.substring(BaseApplication.SCHEMA_FILE.length());
		
		if (!imagesCacheDir.exists())
			imagesCacheDir.mkdirs();
		
		String targetPath = imagesCacheDir.getAbsolutePath() + "/" + FORMAT.format(imageCount++) + IMAGE_SUFFIX;
		ELog.i("Add image cache:" + targetPath + "(" + path + ")");
		File targetFile = new File(targetPath);
		if (!targetFile.getParentFile().exists())
			targetFile.getParentFile().mkdirs();
		
		Bitmap source = ImageTools.decodeSourceFile(path);
		
		if (source == null)
			return false;
		
		int w = source.getWidth(), h = source.getHeight();
		
		int degree = 0;
		try {
			ExifInterface exif = new ExifInterface(path);
			degree = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
			switch (degree) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;

				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;

				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;

				default:
					degree = 0;
					break;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		float rateSource = 0, rateVideo = 0;
		Matrix matrix = null;
		if (degree == 90 || degree == 270) {
			ELog.i("Rotate:" + degree + " Path:" + path);
			matrix = new Matrix();
			matrix.setRotate(degree, w / 2, h / 2);
			int tmp = w;
			w = h;
			h = tmp;
		}
		rateSource = (float)w / h;
		rateVideo = (float)videoWidth / videoHeight;
		
		int targetWidth = 0, targetHeight = 0;
		if (rateSource >= rateVideo) {
			targetWidth = videoWidth;
			targetHeight = (int)(videoWidth / rateSource);
		}
		else {
			targetWidth = (int)(rateSource * videoHeight);
			targetHeight = videoHeight;
		}
		
		Bitmap img;
		if (matrix == null) {
			img = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);
		}
		else {
			Bitmap tmp = Bitmap.createScaledBitmap(source, targetHeight, targetWidth, true);
			img = Bitmap.createBitmap(tmp, 0, 0, targetHeight, targetWidth, matrix, true);
			tmp.recycle();
		}
		source.recycle();
		
		if (img == null)
			return false;
		
		Bitmap target = Bitmap.createBitmap(videoWidth, videoHeight, Config.RGB_565);
		Canvas canvas = new Canvas(target);
//		canvas.drawARGB(0xFF, 0xFF, 0, 0);
		canvas.drawBitmap(img, (videoWidth - targetWidth) / 2, (videoHeight - targetHeight) / 2, new Paint());
		img.recycle();
		
		FileOutputStream fos = null;
		try {
			if (!targetFile.exists())
				targetFile.createNewFile();
			fos = new FileOutputStream(targetFile);
			target.compress(CompressFormat.JPEG, 100, fos);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (fos != null) {
				try {fos.close();}catch (Exception e){}
			}
		}
		
		return true;
	}
	
	public File composeImages(File imageCacheDir, File videoCacheDir, int seconds) {
		File target = new File(videoCacheDir, "images.ts");
		if (target.exists()) {
			target.delete();
		}
		
		StringBuilder cmd = new StringBuilder()
			.append(" -y -r 1/").append(seconds).append(" -i ").append(imageCacheDir).append("/%2d").append(IMAGE_SUFFIX)
			.append(" -s ").append(videoWidth).append("x").append(videoHeight).append(' ').append(BITRATE).append(' ').append(target.getAbsolutePath());
		
		Command command = new Command(cmd.toString());
		addCommand(command);
		
		return target;
	}
	
	public File createComposeTempFile(File cacheDir, File source) {
		String name = source.getName();
		name = name.substring(0, name.lastIndexOf('.')) + ".ts";
		File target = new File(cacheDir, name);
		if (target.exists()) {
			target.delete();
		}
		
//		StringBuilder cmd = new StringBuilder()
//			.append(" -i ").append(source.getAbsolutePath()).append(" -qscale:v 1 -s ").append(videoWidth).append('x').append(videoHeight)
//			.append(' ').append(BITRATE).append(" -an ").append(target.getAbsolutePath());
		
		StringBuilder cmd = new StringBuilder()
			.append(" -y -i ").append(source.getAbsolutePath()).append(" -qscale:v 1 ").append(target.getAbsolutePath());
		
		Command command = new Command(cmd.toString());
		addCommand(command);
		
		return target;
	}
	
	public File composeVideoAndAudio(File cacheDir) {
		File target = new File(cacheDir, targetName + ".mp4");
		if (!cacheDir.exists())
			cacheDir.mkdirs();
		
		finalPath = target.getAbsolutePath();
		
		MediaMetadataRetriever meta = new MediaMetadataRetriever();
		meta.setDataSource(previewPath);
		int seconds = Integer.parseInt(meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
		
		StringBuilder cmd = new StringBuilder(" -y")
		.append(" -i ").append(previewPath)
		.append(" -i ").append(audioPath)
		.append(" -t ").append(seconds).append(" -filter_complex amix -vcodec mpeg4 -qscale:v 1 -strict -2 ").append(finalPath);

		Command command = new Command(cmd.toString());
		addCommand(command);
		
		return target;
	}
	
	public File concat(File cacheDir, File[] videos, File audio, File target, int seconds) {
		if (target.exists()) {
			target.delete();
		}
		
		if (!target.getParentFile().exists()) {
			target.getParentFile().mkdirs();
		}
		
		StringBuilder cmd = new StringBuilder(" -y -i concat:");
		for (File v : videos) {
			cmd.append(v.getAbsolutePath()).append('|');
		}
//		cmd.deleteCharAt(cmd.length() - 1)
//			.append(" -i ").append(audio.getAbsolutePath())
//			.append(" -t ").append(seconds).append(" -vcodec mpeg4 -qscale:v 1 -strict -2 ").append(target.getAbsolutePath());
		
		cmd.deleteCharAt(cmd.length() - 1)
		.append(" -i ").append(audio.getAbsolutePath())
		.append(" -t ").append(seconds).append(" -filter_complex amix -vcodec mpeg4 -qscale:v 1 -strict -2 ").append(target.getAbsolutePath());

		Command command = new Command(cmd.toString());
		addCommand(command);
		
		return target;
	}
	
	public File concat(File cacheDir, File[] videos) {
		File target = new File(cacheDir, "tmp.mp4");
		if (target.exists()) {
			target.delete();
		}
		
		if (!target.getParentFile().exists()) {
			target.getParentFile().mkdirs();
		}
		
		StringBuilder cmd = new StringBuilder(" -y -threads 8 -i concat:");
		for (File v : videos) {
			cmd.append(v.getAbsolutePath()).append('|');
		}
		cmd.deleteCharAt(cmd.length() - 1).append(" -qscale:v 1 -strict -2 ").append(target.getAbsolutePath());

		Command command = new Command(cmd.toString());
		addCommand(command);
		
		return target;
	}
	
	public File composeVideoAndAudio(File video, File audio, File target, long seconds) {
		if (target.exists()) {
			target.delete();
		}

//		StringBuilder cmd = new StringBuilder()
//			.append(" -y -i ").append(video.getAbsolutePath()).append(" -i ").append(audio.getAbsolutePath())
//			.append(" -filter_complex amix=inputs=2:duration=first:dropout_transition=0 -vcodec mpeg4 -acodec ac3 ").append(target.getAbsolutePath());

		StringBuilder cmd = new StringBuilder()
			.append(" -y -i ").append(video.getAbsolutePath()).append(" -i ").append(audio.getAbsolutePath())
			.append(" -filter_complex [0:1][1:0]amerge=inputs=2 -vcodec mpeg4 -acodec ac3 ").append(target.getAbsolutePath());

		Command command = new Command(cmd.toString());
		addCommand(command);
		
		return target;
	}
	
	public void clip(File target) {
		MediaMetadataRetriever metadata = new MediaMetadataRetriever();
		metadata.setDataSource(sourcePath);
		int srcWidth = Integer.parseInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)),
			srcHeight = Integer.parseInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
		metadata.release();
		
		int targetWidth = 0, targetHeight = 0;
		float srcRate = (float)srcWidth / srcHeight, targetRate = (float)videoWidth / videoHeight;
		if (srcRate >= targetRate) {
			//原视频宽比较大
			targetWidth = srcHeight * 4 / 3;
			targetHeight = srcHeight;
		}
		else {
			//原视频高比较大
			targetWidth = srcWidth;
			targetHeight = srcWidth * 3 / 4;
		}
		
		int offset = (end - start) / 1000;

		//./ffmpeg -y -i video2.mp4 -vf crop=320:240 -s 800x600 -ss 6 -t 10 aaa.ts
		StringBuilder cmd = new StringBuilder()
			.append(" -y -ss ").append(start / 1000).append(" -t ").append(offset > 0 ? offset : 1)
			.append(" -i ").append(sourcePath).append(" -vf crop=").append(targetWidth).append(':').append(targetHeight)
			.append(" -s ").append(videoWidth).append('x').append(videoHeight)
			.append(" ").append(target.getAbsolutePath());

		Command command = new Command(cmd.toString());
		addCommand(command);
	}
	
	public File convert(File cacheDir, File source) {
		String name = source.getName();
		name = name.substring(0, '.') + ".mp4";
		File target = new File(cacheDir, name);
		if (target.exists()) {
			target.delete();
		}

		StringBuilder cmd = new StringBuilder()
			.append(" -i ").append(source.getAbsolutePath()).append(" -qscale:v 1 -strict -2 ").append(target.getAbsolutePath());

		Command command = new Command(cmd.toString());
		addCommand(command);
		
		return target;
	}

	public int getVideoWidth() {
		return videoWidth;
	}

	public void setVideoWidth(int videoWidth) {
		this.videoWidth = videoWidth;
	}

	public int getAction() {
		return action;
	}
	
	public int getSteps() {
		return steps;
	}
	
	public int getCurrentStep() {
		return currentStep;
	}
	
	public void setCurrentStep(int currentStep) {
		this.currentStep = currentStep;
		if (notify != null) {
			notify.setProgress(steps, currentStep, false);
		}
	}
	
	public int increase() {
		currentStep++;
		if (notify != null) {
			notify.setProgress(steps, currentStep, false);
		}
		return currentStep;
	}
	
	public void setNotify(int notifyId, NotificationCompat.Builder notify) {
		this.notifyId = notifyId;
		this.notify = notify;
	}
	
	public NotificationCompat.Builder getNotify() {
		return notify;
	}
	
	public int getNotifyId() {
		return notifyId;
	}
}

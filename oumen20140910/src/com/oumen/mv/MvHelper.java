package com.oumen.mv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.oumen.android.App;
import com.oumen.tools.ELog;
import com.oumen.tools.FileTools;

public class MvHelper {
	public static final String EXTENSION_PREFIX_VIDEO = ".mp4";
	public static final String EXTENSION_COVER = ".sqr";
	public static final String EXTENSION_COVER_CIRCLE = ".cir";
	
	public static final String SUFFIX_NAME = "suffix-video/suffix.mp4";
	
	public static boolean installSuffixVideo(Context context) {
		File targetVideo = new File(App.PATH_VIDEO_SUFFIX, "片尾.mp4");
		if (targetVideo.exists()) {
			return true;
		}
		
		boolean successed = false;
		FileOutputStream fos = null;
		InputStream is = null;
		try {
			if (!targetVideo.getParentFile().exists()) {
				targetVideo.getParentFile().mkdirs();
			}
			
			ELog.v("Create:" + targetVideo.getAbsolutePath());
			
			targetVideo.createNewFile();

			is = context.getAssets().open(SUFFIX_NAME);
			fos = new FileOutputStream(targetVideo);
			
			FileTools.copyStream(is, fos);
			successed = true;
		}
		catch (IOException e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
		finally {
			if (is != null) {
				try {is.close();}catch(Exception e){}
			}
			if (fos != null) {
				try {fos.close();}catch(Exception e){}
			}
		}
		return successed;
	}
	
	public static String getPrefixPath(String name) {
		return App.PATH_VIDEO_PREFIX + "/" + name + EXTENSION_PREFIX_VIDEO;
	}
	
	public static String getCoverPath(String name) {
		return App.PATH_VIDEO_COVER + "/" + name + EXTENSION_COVER;
	}
	
	public static String getSuffixPath() {
		return App.PATH_VIDEO_PREFIX + "/片尾.mp4";
	}
}

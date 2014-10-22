package com.oumen.biaoqing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map.Entry;

import android.graphics.Bitmap;

import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.message.Type;
import com.oumen.tools.FileTools;

public class DownLoadUtil {
	/**
	 * 重新命名文件
	 * @param fileName
	 */
	public static void renameFiles(String fileName) {
		File file = new File(fileName);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if (!file.exists()) {
			file.mkdir();
		}
		
		if (file != null && file.length() > 0){
			for (File f: file.listFiles()) {
				String path = f.getAbsolutePath();
				path += App.FILE_SUFFIX;
				f.renameTo(new File(path));
			}
		}
	}
	
	/**
	 * 过滤文件
	 * @param fileName
	 * @param ShowInChat
	 * @return
	 */
	public static ArrayList<String> filterFile(String fileName, boolean ShowInChat) {
		ArrayList<String> datas = new ArrayList<String>();
		
		File file = new File(fileName);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if (!file.exists()) {
			file.mkdir();
		}
		
		if (file != null && file.length() > 0){
			for (File f: file.listFiles()) {
				String path = f.getAbsolutePath();
				path = path.substring(path.lastIndexOf("/") + 1, path.length());
				if (ShowInChat) {
					if (path.startsWith("s")) {
						datas.add(f.getAbsolutePath());
					}
				}else {
					if (!path.startsWith("s")) {
						datas.add(f.getAbsolutePath());
					}
				}
			}
			return datas;
		}
		return null;
	}
	/**
	 * 判断是否有某张表情
	 * @param type
	 * @param fileName
	 * @return
	 */
	public static boolean hasFile(String fileName) {
		final File file = new File(fileName);
		if (file.exists()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 下载单张表情
	 * @param type
	 * @param biaoqingName
	 */
	public static boolean DownLoadOneBiaoqing(final Type type, final String biaoqingName) {
			try {
				File tempfile = null;
				if (Type.OUBA.equals(type)) {
					tempfile = new File(App.getChatBiaoqingOubaPath() +"/"+ biaoqingName);
				}
				else if (Type.CIWEI.equals(type)) {
					tempfile = new File(App.getChatBiaoqingCiweiPath() +"/"+ biaoqingName);
				}
				if (!tempfile.getParentFile().exists()) {
					tempfile.getParentFile().mkdirs();
				}
				if (!tempfile.exists()) {
					tempfile.createNewFile();
				}
				String url = null;//下载地址
				if (Type.OUBA.equals(type)) {
					url = Constants.CHAT_BIAOQING_OUBA_DEFAULT + biaoqingName;
				}
				else if (Type.CIWEI.equals(type)) {
					url = Constants.CHAT_BIAOQING_CIWEI_DEFAULT + biaoqingName;
				}
				//下载对应的表情
				//TODO 怎么判断下载完成了？
				boolean flag = FileTools.download(url, tempfile.getAbsoluteFile());
				if (flag) {
					File file = new File(tempfile.getAbsoluteFile()+ App.FILE_SUFFIX);
					tempfile.renameTo(file);
				} 
				return flag;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return false;
	}
	
	/**
	 * 获取展示用的小图标
	 * @return
	 */
	public static ArrayList<BiaoQing> getSmallBiaoqing() {
		ArrayList<BiaoQing> datas = new ArrayList<BiaoQing>();
		Set<Entry<CharSequence, Bitmap>> entries = App.SMALLBIAOQING.getEntries();
		for (Entry<CharSequence, Bitmap> entry : entries) {
			BiaoQing biaoqing = new BiaoQing();
			biaoqing.setType(Type.TEXT);
			biaoqing.setSendMsg(String.valueOf(entry.getKey()));
			biaoqing.setBitmap(entry.getValue());
			biaoqing.setImagePath(null);
			
			datas.add(biaoqing);
		}
		return datas;
	}
}

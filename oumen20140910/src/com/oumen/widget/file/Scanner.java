package com.oumen.widget.file;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.oumen.tools.ELog;

public class Scanner {
	private static final String DEFAULT_QUERY_ORDER = MediaStore.MediaColumns._ID + " desc";
	private static final String[] AUDIO_MIME = new String[] {"audio/mpeg"};
	private static final String[] AUDIO_EXTENSION = new String[] {".mp3"};
	private static final String[] VIDEO_MIME = new String[] {"video/mpeg4"};
	private static final String[] VIDEO_EXTENSION = new String[] {".mp4"};
	private static final String[] IMAGE_MIME = new String[] {"image/jpeg", "image/png"};

	private Scanner() {}
	
	public static List<String> scanAudioFiles(Context context) {
		return scanFiles(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, AUDIO_MIME, AUDIO_EXTENSION, null);
	}
	
	public static List<String> scanVideoFiles(Context context) {
		return scanFiles(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_MIME, VIDEO_EXTENSION, null);
	}

	public static Map<String, Dir> scanImageDirs(Context context) {
		return scanDirs(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_MIME, null);
	}
	
	public static List<String> scanImageFiles(Context context, String dirPath) {
		return scanFiles(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_MIME, null, dirPath);
	}
	
	public static List<String> scanImageFiles(Context context) {
		return scanFiles(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_MIME, null, null);
	}
	
	public static String getFirstImage(Context context, String dirPath) {
		return getFirstFile(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_MIME, null, dirPath);
	}
	
	public static Map<String, Dir> scanDirs(Context context, Uri uri, String[] mimeTypes, String[] extension) {
		String selection = null;
		StringBuilder builder = new StringBuilder();
		for (String mimeType : mimeTypes) {
			builder.append(MediaStore.Images.Media.MIME_TYPE).append("=").append('\'').append(mimeType).append('\'').append(" OR ");
		}
		if (builder.length() > 0) {
			builder.delete(builder.length() - 4, builder.length());
			selection = builder.toString();
		}
		
		ContentResolver resolver = context.getContentResolver();

		Cursor cursor = resolver.query(uri, null, selection, null, DEFAULT_QUERY_ORDER);

		Map<String, Dir> dirs = new LinkedHashMap<String, Dir>();
		
		if (cursor == null)
			return dirs;
		
		while (cursor.moveToNext()) {
			String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
			File target = new File(path);
			if (!target.exists())
				continue;
			
			File p = target.getParentFile();
			Dir dir = dirs.get(p.getPath());
			if (dir == null) {
				boolean add = false;
				if (extension != null) {
					for (String i : extension) {
						if (path.endsWith(i)) {
							add = true;
							break;
						}
					}
				}
				else {
					add = true;
				}
				
				if (add) {
					ELog.i("Path:" + p.getPath());
					dir = new Dir(p.getName(), p.getPath(), mimeTypes);
					dirs.put(p.getPath(), dir);
				}
			}
			dir.count++;
		}
		cursor.close();
		
		return dirs;
	}
	
	public static List<String> scanFiles(Context context, Uri uri, String[] mimeTypes, String[] extension, String dirPath) {
		String selection = null;
		StringBuilder builder = new StringBuilder();
		for (String mimeType : mimeTypes) {
			builder.append(MediaStore.Images.Media.MIME_TYPE).append("=").append('\'').append(mimeType).append('\'').append(" OR ");
		}
		if (builder.length() > 0) {
			builder.delete(builder.length() - 4, builder.length());
		}
		
		if (!TextUtils.isEmpty(dirPath) && builder.length() > 0) {
			builder.insert(0, '(');
			builder.append(')');
			builder.append(" AND ").append(MediaStore.MediaColumns.DATA).append(" like '").append(dirPath).append("%'");
		}
		
		ContentResolver resolver = context.getContentResolver();

		Cursor cursor = resolver.query(uri, null, selection, null, DEFAULT_QUERY_ORDER);

		LinkedList<String> paths = new LinkedList<String>();
		
		if (cursor == null)
			return paths;
		
		while (cursor.moveToNext()) {
			String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
			File target = new File(path);
			if (!target.exists())
				continue;
			
			boolean add = false;
			File p = target.getParentFile();
			if (TextUtils.isEmpty(dirPath) || p.getPath().equals(dirPath)) {
				if (extension == null) {
					add = true;
				}
				else {
					for (String i : extension) {
						if (path.endsWith(i)) {
							add = true;
							break;
						}
					}
				}
			}
			
			if (add) {
				paths.add(path);
			}
		}
		cursor.close();
		
		return paths;
	}
	
	public static String getFirstFile(Context context, Uri uri, String[] mimeTypes, String[] extension, String dirPath) {
		String path = null;
		String selection = null;
		StringBuilder builder = new StringBuilder();
		for (String mimeType : mimeTypes) {
			builder.append(MediaStore.Images.Media.MIME_TYPE).append("=").append('\'').append(mimeType).append('\'').append(" OR ");
		}
		if (builder.length() > 0) {
			builder.delete(builder.length() - 4, builder.length());
			builder.insert(0, '(');
			builder.append(')');
		}
		builder.append(" AND ").append(MediaStore.MediaColumns.DATA).append(" like '").append(dirPath).append("%'");
		
		ContentResolver resolver = context.getContentResolver();

		Cursor cursor = resolver.query(uri, null, selection, null, DEFAULT_QUERY_ORDER);

		if (cursor == null)
			return path;
		
		while (cursor.moveToNext()) {
			String tmp = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
			File target = new File(tmp);
			if (!target.exists())
				continue;

			File p = target.getParentFile();
			if (TextUtils.isEmpty(dirPath) || p.getPath().equals(dirPath)) {
				if (extension == null) {
					path = tmp;
					break;
				}
				else {
					for (String i : extension) {
						if (path.endsWith(i)) {
							path = tmp;
							break;
						}
					}
				}
			}
		}
		cursor.close();
		
		return path;
	}

}

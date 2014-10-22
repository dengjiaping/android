package com.oumen.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;

public class ImageTools {
	public static final int DEFAULT_IMAGE_WIDTH = 720;
	public static final int DEFAULT_IMAGE_HEIGHT = 1280;

	public static Bitmap clip2square(Bitmap src) {
		int w = src.getWidth(), h = src.getHeight();
		int x = 0, y = 0, size = 0;
		if (w >= h) {
			size = h;
			x = (w - h) / 2;
		}
		else {
			size = w;
			y = (h - w) / 2;
		}
		return Bitmap.createBitmap(src, x, y, size, size);
	}

	public static float rate(int srcWidth, int srcHeight, float targetWidthOrHeight) {
		return targetWidthOrHeight / Math.min(srcWidth, srcHeight);
	}

	public static Bitmap scale(Bitmap src, float rate) {
		// 获得图片的宽高
		int width = src.getWidth();
		int height = src.getHeight();
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(rate, rate);
		// 得到新的图片
		Bitmap target = Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
		return target;
	}

	public static Bitmap scale(Bitmap src, int targetWidth, int targetHeight) {
		// 获得图片的宽高
		int width = src.getWidth();
		int height = src.getHeight();
		// 计算缩放比例
		float scaleWidth = ((float) targetWidth) / width;
		float scaleHeight = ((float) targetHeight) / height;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// 得到新的图片
		Bitmap target = Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
		return target;
	}

	public static Bitmap decodeFile(String path, long maxLength) {
		Bitmap img = null;
		File f = new File(path);
		long length = f.length();
		FileInputStream fis = null;
		try {
			if (length > maxLength) {
				Options opts = new Options();
				opts.inPurgeable = true;
				opts.inPreferredConfig = Bitmap.Config.RGB_565;
				opts.inSampleSize = (int) (length / maxLength);
				fis = new FileInputStream(new File(path));
				img = BitmapFactory.decodeStream(fis, null, opts);
			}
			else {
				fis = new FileInputStream(new File(path));
				img = BitmapFactory.decodeStream(fis);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				}
				catch (Exception e) {
				}
			}
		}

		return img;
	}

	public static Bitmap decodeFile(String path, Options opts, long maxLength) {
		Bitmap img = null;
		File f = new File(path);
		long length = f.length();
		FileInputStream fis = null;
		try {
			if (length > maxLength) {
				opts.inSampleSize = (int) (length / maxLength);
				fis = new FileInputStream(f);
				img = BitmapFactory.decodeStream(fis, null, opts);
			}
			else {
				fis = new FileInputStream(new File(path));
				img = BitmapFactory.decodeStream(fis);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				}
				catch (Exception e) {
				}
			}
		}

		return img;
	}

	public static Bitmap decodeSourceFile(String path) {
		Bitmap img = null;
		FileInputStream fis = null;
		try {
			Options opts = new Options();
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			opts.inInputShareable = true;
			opts.inPurgeable = true;
			opts.inMutable = true;
			fis = new FileInputStream(new File(path));
			img = BitmapFactory.decodeStream(fis, null, opts);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				}
				catch (Exception e) {
				}
			}
		}

		return img;
	}

	public static Bitmap decodeSourceFile(String path, int targetWidth, int targetHeight) {
		Bitmap img = null;
		FileInputStream fis = null;
		try {
			Options opts = new Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, opts);
			if (targetWidth <= targetHeight) {
				opts.inSampleSize = opts.outWidth / targetWidth;
			}
			else {
				opts.inSampleSize = opts.outHeight / targetHeight;
			}
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			opts.inInputShareable = true;
			opts.inPurgeable = true;
			opts.inMutable = true;
			opts.inJustDecodeBounds = false;
			fis = new FileInputStream(new File(path));
			img = BitmapFactory.decodeStream(fis, null, opts);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				}
				catch (Exception e) {
				}
			}
		}

		return img;
	}

	public static Options getImageSize(String path) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		return opts;
	}

	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		ELog.i(String.valueOf(result.length));
		return result;
	}

	public static void save(Bitmap bm, File file, int quality) throws FileNotFoundException, IOException {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		if (!file.exists()) {
			file.createNewFile();
		}

		FileOutputStream out = new FileOutputStream(file);
		bm.compress(CompressFormat.JPEG, quality, out);
		out.flush();
		out.close();
	}
	
	public static void save(String path, File file) throws FileNotFoundException, IOException {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(file);

		getBitmap(path).compress(CompressFormat.JPEG, 100, out);
		out.flush();
		out.close();
	}

	/**
	 * 按照指定宽高等比例加载位图到内存
	 * 
	 * @param data
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap getBitmap(byte[] data, int width, int height) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, opts);
		int xScale = opts.outWidth / width;
		int yScale = opts.outHeight / height;
		opts.inSampleSize = xScale > yScale ? xScale : yScale;
		opts.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
	}

	public static Bitmap getBitmap(String path) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// opts.inJustDecodeBounds = true;
		opts.inSampleSize = 1;
		Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
		return bitmap;
	}

	public static Bitmap getBitmap(String path, int type) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// opts.inJustDecodeBounds = true;
		opts.inSampleSize = type;
		Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
		return bitmap;
	}

	/**
	 * 删除指定目录下文件及目录
	 * 
	 * @param deleteThisPath
	 * @param filepath
	 * @return
	 */
	public static void deleteAllFiles(File f) {
		if (f.exists()) {
			File[] files = f.listFiles();
			if (files != null) {
				for (File file : files)
					if (file.isDirectory()) {
						deleteAllFiles(file);
						file.delete(); // 删除目录下的所有文件后，该目录变成了空目录，可直接删除
					}
					else if (file.isFile()) {
						file.delete();
					}
			}
			// f.delete(); // 删除最外层的目录
		}
	}

	public static void deleteFolderFile(String filePath, boolean deleteThisPath) throws IOException {
		if (!TextUtils.isEmpty(filePath)) {
			File file = new File(filePath);
			if (file.isDirectory()) {// 处理目录
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					deleteFolderFile(files[i].getAbsolutePath(), true);
				}
			}
			if (deleteThisPath) {
				if (!file.isDirectory()) {// 如果是文件，删除
					file.delete();
				}
				else {// 目录
					if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
						file.delete();
					}
				}
			}
		}
	}

	/** * 根据手机的分辨率从dp 的单位 转成为px(像素) */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/** * 根据手机的分辨率从px(像素) 的单位 转成为dp */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static Bitmap rotate(String path, int maxSize) {
		Options opt = getImageSize(path);
		float rate = rate(opt.outWidth, opt.outHeight, maxSize);
		int w = (int) (rate * opt.outWidth), h = (int) (rate * opt.outHeight);

		//2.旋转图片
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

		Matrix matrix = null;
		if (degree == 90 || degree == 270) {
			ELog.i("Rotate:" + degree + " Path:" + path);
			matrix = new Matrix();
			matrix.setRotate(degree, w / 2, h / 2);
			int tmp = w;
			w = h;
			h = tmp;
		}
		else if (degree == 180) {
			ELog.i("Rotate:" + degree + " Path:" + path);
			matrix = new Matrix();
			matrix.setRotate(degree);
		}

		Bitmap img = decodeSourceFile(path, w, h);

		if (degree == 90 || degree == 270) {
			if (matrix == null) {
				img = Bitmap.createScaledBitmap(img, w, h, true);
			}
			else {
				img = Bitmap.createScaledBitmap(img, h, w, true);
				img = Bitmap.createBitmap(img, 0, 0, h, w, matrix, true);
			}
		}
		else {
			if (matrix == null) {
				img = Bitmap.createScaledBitmap(img, w, h, true);
			}
			else {
				img = Bitmap.createScaledBitmap(img, w, h, true);
				img = Bitmap.createBitmap(img, 0, 0, w, h, matrix, true);
			}
		}

		return img;
	}

	/**
	 * 加载图片
	 * 
	 * @param sourceFile
	 * @param path
	 * @return
	 */
	public static Bitmap handlePhotoFromLocation(File sourceFile, String path) {
		int videoWidth = 640;
		int videoHeight = 480;
		if (path.startsWith("file://"))
			path = path.substring("file://".length());

		if (!sourceFile.getParentFile().exists())
			sourceFile.getParentFile().mkdirs();

		Bitmap source = ImageTools.decodeSourceFile(path);
		if (source == null)
			return null;

		int w = source.getWidth(), h = source.getHeight();

		//1.将图片进行压缩
		float rate = 0.0f;
		if (w > ImageTools.DEFAULT_IMAGE_WIDTH && h > ImageTools.DEFAULT_IMAGE_HEIGHT) {// 如果宽高比都比极限值大，就进行等比压缩
			rate = w / h;
			if (w > h && rate < 10) {// 宽图
				rate = ImageTools.rate(w, h, ImageTools.DEFAULT_IMAGE_WIDTH);
				source = ImageTools.scale(source, rate);
			}
			else if (w < h && rate > 0.1) {// 长图
				rate = ImageTools.rate(w, h, ImageTools.DEFAULT_IMAGE_HEIGHT);
				source = ImageTools.scale(source, rate);
			}
		}
		//2.旋转图片
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

		Matrix matrix = null;
		if (degree == 90 || degree == 270) {
			ELog.i("Rotate:" + degree + " Path:" + path);
			matrix = new Matrix();
			matrix.setRotate(degree, w / 2, h / 2);
			int tmp = w;
			w = h;
			h = tmp;
		}
		else if (degree == 180) {
			ELog.i("Rotate:" + degree + " Path:" + path);
			matrix = new Matrix();
			matrix.setRotate(degree);

		}

		Bitmap img;
		float rateSource = 0, rateVideo = 0;

		if (degree == 90 || degree == 270) {
			rateSource = (float) w / h;
			rateVideo = (float) videoWidth / videoHeight;

			int targetWidth = 0, targetHeight = 0;
			if (rateSource >= rateVideo) {
				targetWidth = videoWidth;
				targetHeight = (int) (videoWidth / rateSource);
			}
			else {
				targetWidth = (int) (rateSource * videoHeight);
				targetHeight = videoHeight;
			}

			if (matrix == null) {
				img = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);
			}
			else {
				Bitmap tmp = Bitmap.createScaledBitmap(source, targetHeight, targetWidth, true);
				img = Bitmap.createBitmap(tmp, 0, 0, targetHeight, targetWidth, matrix, true);
				tmp.recycle();
			}

		}
		else {
			if (matrix == null) {
				img = Bitmap.createScaledBitmap(source, w, h, true);
			}
			else {
				Bitmap tmp = Bitmap.createScaledBitmap(source, w, h, true);
				img = Bitmap.createBitmap(tmp, 0, 0, w, h, matrix, true);
				tmp.recycle();
			}
		}
		source.recycle();

		if (img == null)
			return null;
		//3.将图片保存到指定路径下
		FileOutputStream fos = null;
		try {
			if (!sourceFile.exists())
				sourceFile.createNewFile();
			fos = new FileOutputStream(sourceFile);
			img.compress(CompressFormat.JPEG, 100, fos);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (fos != null) {
				try {
					fos.close();
				}
				catch (Exception e) {
				}
			}
		}
		return img;
	}

	/**
	 * 圆形头像
	 * 
	 * @param bitmap
	 * @param ratio
	 *            截取比例，如果是8，则圆角半径是宽高的1/8，如果是2，则是圆形图片
	 * @return
	 */
	public static Bitmap toOvalBitmap(Bitmap bitmap) {
		if (bitmap != null) {

			Bitmap output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			Paint paint = new Paint();
			int min = Math.min(bitmap.getWidth(), bitmap.getHeight());
			Rect rect = new Rect(0, 0, min, min);
			RectF rectF = new RectF(rect);
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			// canvas.drawRoundRect(rectF, bitmap.getWidth()/2,
			// bitmap.getHeight()/2, paint);
			// int x = bitmap.getWidth() / 2;
			// int y = bitmap.getHeight()/2;
			// canvas.drawCircle(x,
			// y, Math.min(x, y), paint);
			int x = min / 2;
			canvas.drawCircle(x, x, x, paint);
			paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rectF, paint);
			return output;
		}
		else {
			return null;
		}
	}
}

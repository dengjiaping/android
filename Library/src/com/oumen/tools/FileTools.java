package com.oumen.tools;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

public final class FileTools {
	private static final int unitSize = 384 * 1024;// 每个小文件的大小

	private static final int BUFFER_SIZE = 32 * 1024; // 32 KB

	private FileTools() {
	}

	public static void createFile(File file, boolean delete) throws IOException {
		File parent = file.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		if (file.exists() && delete) {
			file.delete();
		}
		if (!file.exists()) {
			file.createNewFile();
		}
	}

	public static void copyStream(InputStream is, OutputStream os) throws IOException {
		byte[] bytes = new byte[BUFFER_SIZE];
		while (true) {
			int count = is.read(bytes, 0, BUFFER_SIZE);
			if (count == -1) {
				break;
			}
			os.write(bytes, 0, count);
		}
	}

	public static void closeSilently(Closeable closeable) {
		try {
			closeable.close();
		}
		catch (Exception e) {
			// Do nothing
		}
	}

	public static boolean copyFile(File source, File target) {
		FileInputStream fin = null;
		FileOutputStream fout = null;
		try {
			if (!target.exists()) {
				// target.createNewFile();
				createFile(target, false);
			}
			fin = new FileInputStream(source);
			fout = new FileOutputStream(target);
			copyStream(fin, fout);
			return true;
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		finally {
			try {
				if (fin != null) {
					fin.close();
				}
				if (fout != null) {
					fout.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean download(String url, File target) throws MalformedURLException, IOException {
		boolean successed = false;
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = new URL(url).openStream();
			if (!target.getParentFile().exists())
				target.getParentFile().mkdirs();
			if (target.exists())
				target.delete();
			target.createNewFile();
			fos = new FileOutputStream(target);
			byte[] buf = new byte[1024];
			int available = -1;
			while ((available = is.read(buf)) != -1) {
				fos.write(buf, 0, available);
			}
			successed = true;
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (Exception e) {
				}
			}
			if (fos != null) {
				try {
					fos.close();
				}
				catch (Exception e) {
				}
			}
		}
		return successed;
	}

	public static File[] sortByDesc(File dir) {
		File[] sources = dir.listFiles();
		if (sources == null)
			return null;

		Arrays.sort(sources, new Comparator<File>() {

			@Override
			public int compare(File lhs, File rhs) {
				if (lhs.lastModified() > rhs.lastModified())
					return -1;
				else if (lhs.lastModified() < rhs.lastModified())
					return 1;
				else
					return 0;
			}
		});

		return sources;
	}

	public static File[] sortByAsc(File dir) {
		File[] sources = dir.listFiles();
		if (sources == null)
			return null;

		Arrays.sort(sources, new Comparator<File>() {

			@Override
			public int compare(File lhs, File rhs) {
				if (lhs.lastModified() > rhs.lastModified())
					return 1;
				else if (lhs.lastModified() < rhs.lastModified())
					return -1;
				else
					return 0;
			}
		});

		return sources;
	}

	/**
	 * 切割文件
	 * 
	 * @param fileName
	 *            需要切割的文件名称
	 * @param targetDir
	 *            分割后小文件所在的文件夹
	 * @return 文件的数目
	 * @throws Exception
	 */
	public static int CutFile(String fileName, String title, String targetDir) throws Exception {
		File file = new File(fileName);
		int count = 0;// 小文件数
		RandomAccessFile inn = null;
		try {
			inn = new RandomAccessFile(file, "r");// 打开要分割的文件
			long length = inn.length();
			count = (int) length / unitSize;// 文件的个数
			ELog.i(String.valueOf(count));
			// 根据要分割的数目输出文件
			long offset = 0L;
			for (int i = 0; i < count - 1; i++) {
				long fbegin = offset;
				long fend = (i + 1) * unitSize;
				File outFile = new File(targetDir + "/" + title + "/" + i);
				createFile(outFile, false);
				offset = write(fileName, targetDir + "/" + title, i, fbegin, fend);
			}
			if (length - offset > 0) {
				write(fileName, targetDir + "/" + title, count - 1, offset, length);
			}
			return count;
		}
		catch (Exception e1) {
			e1.printStackTrace();
			return -1;
		}
		finally {
			if (inn != null) {
				try {
					inn.close();
				}
				catch (Exception e) {
				}
			}
		}
	}

	/**
	 * <p>
	 * 指定每份文件的范围写入不同文件
	 * </p>
	 * 
	 * @param file
	 *            源文件
	 * @param index
	 *            文件顺序标识
	 * @param begin
	 *            开始指针位置
	 * @param end
	 *            结束指针位置
	 * @return
	 * @throws Exception
	 */
	private static long write(String file, String targetFile, int index, long begin, long end) {
		long endPointer;
		RandomAccessFile in = null;
		RandomAccessFile out = null;
		try {
			in = new RandomAccessFile(new File(file), "r");
			out = new RandomAccessFile(new File(targetFile + "/" + index), "rw");
			byte[] b = new byte[1024];
			int n = 0;
			in.seek(begin);// 从指定位置读取

			while (in.getFilePointer() <= end && (n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			endPointer = in.getFilePointer();
			in.close();
			out.close();
			return endPointer;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Exception e2) {
				}
			}
			if (out != null) {
				try {
					out.close();
				}
				catch (Exception e2) {
				}
			}
		}
		return -1;
	}

}

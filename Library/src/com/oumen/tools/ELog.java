package com.oumen.tools;

import android.util.Log;

/**
 * Enhance Log
 * @author wangchao
 *
 */
public class ELog {
	public static boolean isDebug = true;
//	private static File log = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/oumen", "log.txt");
//	
//	private static void writeToFile(String msg) throws IOException {
//		BufferedWriter writer = new BufferedWriter(new FileWriter(log, true));
//		writer.write(msg);
//		writer.close();
//	}
	
	public static void d(String msg) {
		if (!isDebug)
			return;
		
		StackTraceElement invoker = getInvoker();
		msg = "【" + invoker.getMethodName() + ":" + invoker.getLineNumber() + "】" + msg;
		Log.d(invoker.getClassName(), msg);
//		try {
//			writeToFile(msg + "\n");
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public static void i(String msg) {
		if (!isDebug)
			return;

		StackTraceElement invoker = getInvoker();
		Log.i(invoker.getClassName(), "【" + invoker.getMethodName() + ":" + invoker.getLineNumber() + "】" + msg);
	}
	
	public static void e(String msg) {
		if (!isDebug)
			return;

		StackTraceElement invoker = getInvoker();
		Log.e(invoker.getClassName(), "【" + invoker.getMethodName() + ":" + invoker.getLineNumber() + "】" + msg);
	}
	
	public static void v(String msg) {
		if (!isDebug)
			return;

		StackTraceElement invoker = getInvoker();
		Log.v(invoker.getClassName(), "【" + invoker.getMethodName() + ":" + invoker.getLineNumber() + "】" + msg);
	}
	
	public static void w(String msg) {
		if (!isDebug)
			return;

		StackTraceElement invoker = getInvoker();
		Log.w(invoker.getClassName(), "【" + invoker.getMethodName() + ":" + invoker.getLineNumber() + "】" + msg);
	}
	
	private static StackTraceElement getInvoker() {
		return Thread.currentThread().getStackTrace()[4];
	}
}

package com.oumen.widget.ffmpeg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;

import com.oumen.tools.ELog;
import com.oumen.tools.FileTools;
import com.oumen.widget.ffmpeg.FFMpegListener.Phase;

public class FFMpeg implements Runnable {
	public static final int DEFAULT_VIDEO_WIDTH = 640;
	public static final int DEFAULT_VIDEO_HEIGHT = 480;
	
	private final int IO_BUFFER_SIZE = 32256;
	private final String SHELL_CMD_CHMOD = "chmod";
	
	private FFMpegListener listener;
	
	private String pathFFMpeg;
	
	private final ArrayList<Command> commands = new ArrayList<Command>();
	
	public FFMpeg(FFMpegListener listener) {
		this.listener = listener;
	}
	
	public boolean init(Context context, int resId) throws IOException {
		boolean success = false;
		File[] dirs = new File[] {
				context.getCacheDir().getAbsoluteFile(),
				context.getExternalCacheDir(),
				Environment.getExternalStorageDirectory()
		};
		
		File ffmpegFile = null;
		for (File dir : dirs) {
			ffmpegFile = new File(dir, "ffmpeg");
			if (install(context, resId, ffmpegFile)) {
				pathFFMpeg = ffmpegFile.getAbsolutePath();
				success = true;
				break;
			}
		}
		ELog.i("FFMpeg path:" + pathFFMpeg);
		
		return success;
	}
	
	public void reset() {
		commands.clear();
	}
	
	public void addCommand(Command command) {
		if (command.cmd.startsWith(" "))
			command.cmd = pathFFMpeg + command.cmd;
		else
			command.cmd = pathFFMpeg + " " + command.cmd;
		commands.add(command);
	}
	
	public void addCommand(String cmd) {
		commands.add(new Command(cmd));
	}

	public void doChmod(File file) {
		final StringBuilder sb = new StringBuilder();
		sb.append(SHELL_CMD_CHMOD);
		sb.append(' ');
		sb.append(700);
		sb.append(' ');
		sb.append(file.getAbsolutePath());

		try {
			Runtime.getRuntime().exec(sb.toString());
		}
		catch (IOException e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean install(Context context, int resId, File file) {
		boolean success = false;
		InputStream is = null;
		OutputStream os = null;
		
		try {
			if (!file.exists())  {
				FileTools.createFile(file, true);
				file.setExecutable(true);
				
				is = context.getResources().openRawResource(resId);
				os = new FileOutputStream(file);
				
				byte[] buffer = new byte[IO_BUFFER_SIZE];
				int count;
				while ((count = is.read(buffer)) > 0) {
					os.write(buffer, 0, count);
				}
			}
			
			success = true;
		}
		catch (IOException e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
		finally {
			try {is.close();}catch (Exception e){}
			try {os.close();}catch (Exception e){}
		}
		
		int ret = 1;//1表示出错
		try {
			String cmd = file.getAbsolutePath() + " -L";
			ELog.i("CMD:" + cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			ret = p.waitFor();
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
		finally {
			success = (ret == 0);
		}
		
		doChmod(file);
		return success;
	}

	public boolean checkFilePerms(File file) {
		Command cmd = new Command("ls -l " + file.toString());
		return cmd.execute();
	}

	@Override
	public void run() {
		if (listener != null)
			listener.onEvent(Phase.RUNNING, this, commands.size());
		
		for (Command cmd : commands) {
			if (listener != null) {
				listener.onEvent(Phase.PROGRESS, this, cmd);
			}
			long t = System.currentTimeMillis();
			boolean success = cmd.execute();
			ELog.i("Duration:" + (System.currentTimeMillis() - t));
			if (!success) {
				if (listener != null) {
					listener.onEvent(Phase.FAILED, this, cmd);
				}
				return;
			}
		}
		
		if (listener != null)
			listener.onEvent(Phase.COMPLETED, this, null);
	}
}

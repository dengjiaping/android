package com.oumen.widget.ffmpeg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.oumen.tools.ELog;

public class Command{
	protected String cmd;
	
	public Command(String cmd) {
		this.cmd = cmd;
	}

	public boolean execute() {
		ELog.i("CMD:" + cmd);
		
		InputStream is = null;
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			
			int ret = p.waitFor();
			ELog.i("Result:" + ret);
			
			String info;
			if (ret == 1) {
				is = p.getErrorStream();
				info = print(is);
				ELog.e("Err:" + info);
				return false;
			}
			else {
				is = p.getInputStream();
				info = print(is);
				ELog.i("Info:" + info);
				return true;
			}
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		finally {
			if (is != null) {
				try {is.close();}catch (Exception e){}
			}
		}
	}

	protected String print(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}

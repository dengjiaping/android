package com.oumen.tools;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Tools {

	// Md5认证
	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String fileToMD5(String fileName) {
		InputStream fis = null;
		byte[] buffer = new byte[1024];
		int numRead = 0;
		MessageDigest md5;
		try {
			fis = new FileInputStream(fileName);
			md5 = MessageDigest.getInstance("MD5");
			while ((numRead = fis.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}
			return toHexString(md5.digest());
		}
		catch (Exception e) {
			ELog.i("Exception e" + e.toString());
			return null;
		}
		finally {
			if (fis != null) {
				try {fis.close();}catch(Exception e){}
			}
		}
	}

	public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static String getEncode(String codeType, String content) {
		String result = "";
		try {
			MessageDigest digest = MessageDigest.getInstance(codeType);
			digest.reset();
			digest.update(content.getBytes());
			StringBuilder builder = new StringBuilder();
			for (byte b : digest.digest()) {
				builder.append(Integer.toHexString((b >> 4) & 0xf));
				builder.append(Integer.toHexString(b & 0xf));
			}
			result = builder.toString();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
}

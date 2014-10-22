package com.oumen.db;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.oumen.R;
import com.oumen.tools.ELog;

public class PinYin {
	private static boolean initialized = false;
	
	public static boolean isInitialized() {
		return initialized;
	}

	public static void initialize(Context context, DatabaseHelper helper) {
		long t = System.currentTimeMillis();
		SQLiteDatabase db = helper.getWritableDatabase();

		int max = 0;
		Cursor cursor;
		
		try {
			cursor = db.query(DatabaseHelper.TABLE_PINYIN, new String[]{"MAX(`unicode`) AS max"}, null, null, null, null, null);
			if (cursor.getCount() > 0) {
				cursor.moveToNext();
				max = cursor.getInt(0);
				cursor.close();
			}
		}
		catch (SQLiteException e) {
			if (e.getMessage().startsWith("no such table")) {
				StringBuilder sql = new StringBuilder()
					.append("CREATE table IF NOT EXISTS ")
					.append(DatabaseHelper.TABLE_PINYIN)
					.append(" (`unicode` INTEGER PRIMARY KEY, py VARCHAR NOT NULL)");
				db.execSQL(sql.toString());
			}
		}
		
		ELog.i("Max unicode:" + max);
		
		if (max < 20903) {
			InputStream is = null;
			try {
				StringBuilder unicode = new StringBuilder(), py = new StringBuilder();
				StringBuilder sql = buildInsertSql(new StringBuilder());
				
				int read = -1;
				boolean isKey = true;
				
				is = context.getResources().openRawResource(R.raw.py_map);
				
				int count = 0;
				while ((read = is.read()) != -1) {
					char c = (char)read;
					if (c == ' ') {
						isKey = false;
						continue;
					}
					else if (c == '\n') {
						isKey = true;
						int code = Integer.decode("0x" + unicode.toString());
						if (code > max) {
							sql.append("(").append(code).append(",'").append(py.toString()).append("'),");
							
							if (++count >= 200) {
								insert(db, sql);
								
								buildInsertSql(sql);
								count = 0;
							}
						}
						unicode.delete(0, unicode.length());
						py.delete(0, py.length());
						continue;
					}
					
					if (isKey) {
						unicode.append(c);
					}
					else {
						if (c >= '1' && c <= '9') {
							continue;
						}
						else {
							py.append(c);
						}
					}
				}

				if (count > 0) {
					insert(db, sql);
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
			}
			finally {
				if (is != null) {
					try {
						is.close();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			initialized = true;
			ELog.i("Time:" + (System.currentTimeMillis() - t));
		}
	}
	
	public static String getPinYin(String str, DatabaseHelper helper) {
		SQLiteDatabase db = helper.getReadableDatabase();
		StringBuilder ret = new StringBuilder();
		
		String[] columns = new String[] {"py"};
		for (int i = 0; i < str.length(); i++) {
			int c = str.charAt(i);
			Cursor cursor = db.query(DatabaseHelper.TABLE_PINYIN, columns, "`unicode`=" + c, null, null, null, null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				String[] py = cursor.getString(0).split(",");
				ret.append(py[0]);
			}
			else {
				ret.append((char)c);
			}
			cursor.close();
		}
		return ret.toString();
	}
	
	private static void insert(SQLiteDatabase db, StringBuilder sql) {
		if (sql.charAt(sql.length() - 1) == ',') {
			sql.delete(sql.length() - 1, sql.length());
		}
		db.execSQL(sql.toString());
	}
	
	private static StringBuilder buildInsertSql(StringBuilder builder) {
		builder.delete(0, builder.length());
		builder.append("INSERT INTO " + DatabaseHelper.TABLE_PINYIN + " (`unicode`, `py`) VALUES ");
		return builder;
	}
}

package com.oumen.biaoqing;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import com.oumen.android.App;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;

public class SmallBiaoQing {
	private final Pattern pattern = Pattern.compile("/[\u4e00-\u9fa5]{2}");
	
	private final LinkedHashMap<CharSequence, Bitmap> map = new LinkedHashMap<CharSequence, Bitmap>();
	
	public Bitmap get(CharSequence tag) {
		return map.get(tag);
	}
	
	public Set<Entry<CharSequence, Bitmap>> getEntries() {
		return map.entrySet();
	}
	
	public boolean initialize(Context context) {
		String dir = "emoji", extension = ".png";
		String key = null, tag = null;
		Bitmap value = null;
		InputStream isXml = null;
		try {
			isXml = context.getAssets().open(dir + "/emoji.xml");
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(isXml, "UTF-8");
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				switch (event) {
					case XmlPullParser.START_TAG:
						tag = parser.getName();
						break;
						
					case XmlPullParser.TEXT:
						String text = parser.getText().trim();
						if (TextUtils.isEmpty(text))
							break;
						
						if ("key".equalsIgnoreCase(tag)) {
							InputStream isImage = context.getAssets().open(dir + "/" + text + extension);
							try {
								value = BitmapFactory.decodeStream(isImage);
							}
							catch (Exception e) {
								ELog.e("Exception:" + e.getMessage());
								e.printStackTrace();
							}
							finally {
								if (isImage != null) {
									try {isImage.close();} catch (IOException e) {e.printStackTrace();}
								}
							}
						}
						else if ("string".equalsIgnoreCase(tag)) {
							key = text;
							map.put(key, value);
						}
						break;
				}
				event = parser.next();
			}
			return true;
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		finally {
			if (isXml != null) {
				try {isXml.close();} catch (IOException e) {e.printStackTrace();}
			}
		}
	}
	
	public SpannableStringBuilder convert(Context context, SpannableStringBuilder builder, int size) {
		Matcher m = pattern.matcher(builder);
		while (m.find()) {
			CharSequence key = builder.subSequence(m.start(), m.end()).toString();
			Bitmap img = map.get(key);
			if (img == null) continue;
			
			if (size != App.INT_UNSET) {
				img = ImageTools.scale(img, size, size);
			}
			
//			ELog.w("Key:" + key + " Exist:" + (img != null) + " Position:" + m.start() + "/" + m.end());
			builder.setSpan(new ImageSpan(context, img), m.start(), m.end(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		return builder;
	}
	
	public int getSmallBiaoqingsize() {
		return map.size();
	}
	
}

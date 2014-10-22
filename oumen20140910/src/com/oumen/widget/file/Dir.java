package com.oumen.widget.file;

import android.graphics.Bitmap;

public class Dir {
	public enum Type {IMAGE, VIDEO, FILE};
	
	protected String name;
	protected String path;
	protected String[] mimeTypes;
	protected int count;
	protected Type type;
	
	protected Bitmap icon;

	public Dir(String name, String path, String[] mimeTypes) {
		this.name = name;
		this.path = path;
		this.mimeTypes = mimeTypes;
		
		if (mimeTypes != null) {
			if ("image/jpeg".equals(mimeTypes[0]) || "image/png".equals(mimeTypes[0])) {
				type = Type.IMAGE;
			}
			else {
				type = Type.FILE;
			}
		}
		else {
			type = Type.FILE;
		}
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public int getCount() {
		return count;
	}

	public Type getType() {
		return type;
	}

	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}
}

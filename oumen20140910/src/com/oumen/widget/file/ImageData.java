package com.oumen.widget.file;

import java.io.Serializable;

public class ImageData implements Serializable {
	private static final long serialVersionUID = 208413860308298505L;
	
	public String path;
	public int index;
	public boolean select;

	public ImageData(String path) {
		this.path = path;
	}

	public ImageData(String path, int index) {
		this.path = path;
		this.index = index;
	}

	public ImageData(String path, int index, boolean select) {
		this.path = path;
		this.index = index;
		this.select = select;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ImageData) {
			ImageData target = (ImageData)o;
			return index == target.index;
		}
		return false;
	}
}

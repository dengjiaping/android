package com.oumen.biaoqing;

import com.oumen.message.Type;

import android.graphics.Bitmap;

/**
 * 表情实体类
 */
public class BiaoQing {
	private Type type;
	/*
	 * 往外发消息的msg
	 * 1. 资源文件存的是影射码
	 * 2. 欧巴存的是jpg
	 * 3. 刺猬存的是png
	 */
	private String sendMsg;
	/*
	 * 展示图片有两种方式
	 */
	private String imagePath;
	private Bitmap bitmap;//图片

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public BiaoQing() {
		super();
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getSendMsg() {
		if (Type.TEXT.equals(type)){
			return sendMsg;
		}
		else {
			return getSendImageMessage();
		}
	}

	public void setSendMsg(String sendMsg) {
		this.sendMsg = sendMsg;
	}

	private String getSendImageMessage() {
		if (Type.OUBA.equals(type)){
			String msg = imagePath.substring(imagePath.lastIndexOf("/") + 2, imagePath.indexOf("."));
			msg = msg + ".gif";
			return msg;
		}
		else if (Type.CIWEI.equals(type)) {
			return imagePath.substring(imagePath.lastIndexOf("/") + 2, imagePath.lastIndexOf("."));
		}
		return null;
	}

}

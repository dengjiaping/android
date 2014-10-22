package com.oumen.near;

import java.io.Serializable;

public class NearResult implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean flag = false;
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
}

package com.oumen.android.peers;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Prise implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int priseUid;
	private String priseName;
	public Prise(){};
	public Prise(JSONObject obj) throws JSONException {
		priseUid = Integer.valueOf(obj.getString("uid"));
		priseName = obj.getString("nickname");
	};
	public int getPriseUid() {
		return priseUid;
	}
	public void setPriseUid(int priseUid) {
		this.priseUid = priseUid;
	}
	public String getPriseName() {
		return priseName;
	}
	public void setPriseName(String priseName) {
		this.priseName = priseName;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + priseUid;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Prise other = (Prise) obj;
		if (priseUid != other.priseUid)
			return false;
		return true;
	}
	
	
	
}

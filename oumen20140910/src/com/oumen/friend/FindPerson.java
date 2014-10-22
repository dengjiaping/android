package com.oumen.friend;

import java.io.Serializable;


public class FindPerson implements Serializable {
	private static final long serialVersionUID = 8804893514898949914L;

	private int isFriend;
	private Friend friend;

	public FindPerson() {
		super();
	}

	public FindPerson(int isFriend, Friend friend) {
		super();
		this.isFriend = isFriend;
		this.friend = friend;
	}

	public int getIsFriend() {
		return isFriend;
	}

	public void setIsFriend(int isFriend) {
		this.isFriend = isFriend;
	}

	public Friend getFriend() {
		return friend;
	}

	public void setFriend(Friend friend) {
		this.friend = friend;
	}

	@Override
	public String toString() {
		return "FindPerson [isFriend=" + isFriend + ", friend=" + friend + "]";
	}

}

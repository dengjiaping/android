package com.oumen.android;


public interface BasicUserInfo {
	public int getUid();
	
	public void setUid(int uid);

	public String getNickname();

	public void setNickname(String nickname);
	
	public boolean hasPhoto();
	
	public String getPhotoSourceUrl();

	public String getPhotoUrl(int maxLength);

	public void setPhotoUrl(String photoUrl);
}

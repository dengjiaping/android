package com.oumen.message;

import java.util.Date;


public interface MessageListItemDataProvider {
	public int getNewCount();
	
	public CharSequence getTitle();
	
	public int getTitleRightIconResId();
	
	public CharSequence getDescription();
	
	public int getIconResId();
	
	public String getIconPath();
	
	public int getButtonIconResId();
	
	public Date getTimestamp();
}

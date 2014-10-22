package com.oumen.message;

import org.json.JSONException;
import org.json.JSONObject;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.db.DatabaseHelper;

public class TopPushActivityMessage extends ActivityMessage implements MessageListItemDataProvider {
	protected int newCount;

	public TopPushActivityMessage() {}

	public TopPushActivityMessage(JSONObject json) throws NumberFormatException, JSONException {
		super(json, ActivityMessage.FROM_SOCKET);
	}
	
	public TopPushActivityMessage(ActivityMessage src) {
		id = src.id;
		title = src.title;
		description = src.description;
		address = src.address;
		startTime = src.startTime;
		timestamp = src.timestamp;
		picUrl = src.picUrl;
		read = src.read;
		delete = src.delete;
	}
	
	public int refreshNewCount(int selfUid, DatabaseHelper helper) {
		newCount = queryNewCount(selfUid, helper);
		return newCount;
	}

	@Override
	public int getNewCount() {
		return newCount;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public int getTitleRightIconResId() {
		return App.INT_UNSET;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getIconResId() {
		return R.drawable.icon_activity_recommend;
	}

	@Override
	public String getIconPath() {
		return null;
	}

	@Override
	public int getButtonIconResId() {
		return App.INT_UNSET;
	}
}

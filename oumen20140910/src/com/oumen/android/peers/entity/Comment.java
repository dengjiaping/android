package com.oumen.android.peers.entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.oumen.android.App;

import android.text.TextUtils;

public class Comment implements Serializable {
	private static final long serialVersionUID = -8431515757328499020L;

	private int circleId;//所属偶们圈ID
	
	private int id;//评论的ID
	private String content;
	private int authorId;//评论者uid
	private String authorName;//评论者昵称
	private int targetId;//被评论者的uid
	private String targetName;//被评论者的昵称

	public Comment() {
	}

	public Comment(int circleId, JSONObject obj) throws JSONException {
		this.circleId = circleId;
		
		id = Integer.parseInt(obj.getString("commid"));
		content = obj.optString("content");
		
		String value = obj.optString("uid");
		authorId = TextUtils.isEmpty(value) ? App.INT_UNSET : Integer.parseInt(value);
		value = obj.optString("nameuid");
		authorName = TextUtils.isEmpty(value) ? "" : value;
		
		value = obj.optString("oruid");
		targetId = TextUtils.isEmpty(value) ? App.INT_UNSET : Integer.parseInt(value);
		value = obj.optString("nameoruid");
		targetName = TextUtils.isEmpty(value) ? "" : value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getCircleId() {
		return circleId;
	}

	public void setCircleId(int circleId) {
		this.circleId = circleId;
	}

	public int getAuthorId() {
		return authorId;
	}

	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int target) {
		this.targetId = target;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject root = new JSONObject();
		root.put("commid", String.valueOf(id));
		root.put("content", content);
		root.put("oruid", targetId > 0 ? String.valueOf(targetId) : "");
		root.put("nameoruid", !TextUtils.isEmpty(targetName) ? targetName : "");
		root.put("uid", authorId > 0 ? String.valueOf(authorId) : "");
		root.put("nameuid", !TextUtils.isEmpty(authorName) ? String.valueOf(authorName) : "");
		return root;
	}

	@Override
	public String toString() {
		return "Comment [commid=" + id + ", cnid=" + circleId + ", uid=" + authorId + ", name=" + authorName + ", oruid=" + targetId + ", orname=" + targetName + ", content=" + content + "]";
	}

}

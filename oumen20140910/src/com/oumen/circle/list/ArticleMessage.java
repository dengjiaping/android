package com.oumen.circle.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.oumen.android.App;
import com.oumen.android.peers.Prise;
import com.oumen.android.peers.entity.Comment;
import com.oumen.auth.AuthAdapter.MessageType;
import com.oumen.auth.QqAuthAdapter;
import com.oumen.auth.ShareData;

public class ArticleMessage implements Serializable, ShareData {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String str = "http://114.113.228.183:8088/resource/0/png/nopic";// no pic flag

	public static final int MODE_SHARE = 1;// 分享模式
	public static final int MODE_EXCHANGE = 2;//交流模式
	public static final int MODE_HELP = 3;//求助模式

	int circleId;// 文章编号
	int uid;// 用户编号
	String nickname;// 昵称
	String photoUrl;// 头像
	String content;// 文字内容

	int modes;// 模式：分享，求助
	long time;// 发表的时间
	boolean open;// 是否公开（1所有人，0好友可见）

	int isprise;
	int prisenum;
	String prisename;// 最后一个赞的人
	String priselastname;// 倒数第二个赞的人

	final List<String> photos = new ArrayList<String>();// 发表图片数组
	final List<Prise> prises = new ArrayList<Prise>();
	final List<Comment> comments = new ArrayList<Comment>();

	public ArticleMessage() {
	}

	public ArticleMessage(JSONObject obj) throws JSONException {
		JSONObject resobj = obj.getJSONObject("content");
		circleId = Integer.parseInt(resobj.getString("cnid"));
		uid = resobj.getInt("uid");// 用户uid
		nickname = resobj.getString("nickname");// 昵称
		photoUrl = resobj.getString("head_photo");// 头像
		content = resobj.getString("content");// 内容

		String tempMode = resobj.getString("modes");
		if (TextUtils.isEmpty(tempMode)) {
			modes = MODE_SHARE;// 模式
		}
		else {
			modes = Integer.valueOf(tempMode);
		}

		String tempTime = resobj.has("time") ? resobj.getString("time") : null;
		if (TextUtils.isEmpty(tempTime)) {
			time = App.INT_UNSET;
		}
		else {
			time = Long.valueOf(tempTime);
		}
		open = resobj.has("isopen") ? true : false;

		JSONArray array = resobj.getJSONArray("pic");
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				if (!array.getString(i).equals(str)) {
					photos.add(array.getString(i));
				}
			}
		}

		isprise = obj.getInt("isprise");

		String tempNum = obj.getString("prisenum");
		if (TextUtils.isEmpty(tempNum)) {
			prisenum = 0;
		}
		else {
			prisenum = Integer.valueOf(tempNum);
		}
		prisename = obj.getString("prisename");
		priselastname = obj.getString("priselastname");

		if (obj.has("comment")) {
			JSONArray array1 = obj.getJSONArray("comment");
			for (int j = 0; j < array1.length(); j++) {
				JSONObject r = array1.getJSONObject(j);
				comments.add(0, new Comment(circleId, r));
			}
		}
		if (obj.has("prise")) {
			JSONArray array2 = obj.getJSONArray("prise");
			for (int i = 0; i < array2.length(); i++) {
				JSONObject r = array2.getJSONObject(i);
				prises.add(new Prise(r));
			}
		}
	}

	public int getCircleId() {
		return circleId;
	}

	public void setCircleId(int circleId) {
		this.circleId = circleId;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getModes() {
		return modes;
	}

	public void setModes(int modes) {
		this.modes = modes;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public int getIsprise() {
		return isprise;
	}

	public void setIsprise(int isprise) {
		this.isprise = isprise;
	}

	public int getPrisenum() {
		return prisenum;
	}

	public void setPrisenum(int prisenum) {
		this.prisenum = prisenum;
	}

	public String getPrisename() {
		return prisename;
	}

	public void setPrisename(String prisename) {
		this.prisename = prisename;
	}

	public String getPriselastname() {
		return priselastname;
	}

	public void setPriselastname(String priselastname) {
		this.priselastname = priselastname;
	}

	@Override
	public MessageType getShareType() {
		//偶们圈分享有两种情况，有图片就分享图片信息，没有图片就分享文字
		int size = photos.size();
		if (size == 0) {
			return MessageType.TEXT;
		}
		else {

			return MessageType.IMAGE_ONLY;
		}
	}

	@Override
	public int getActionType() {
		return App.INT_UNSET;
	}

	@Override
	public String getShareTitle() {
		return "【偶们】：" + nickname;
	}

	@Override
	public String getShareContent() {
		return content;
	}

	@Override
	public String getShareLinkUrl() {
		int size = photos.size();
		if (size == 0) {
			return QqAuthAdapter.DEFAULT_IMAGE;
		}
		else {
			return photos.get(0);//分享第一张图片
		}
	}

	@Override
	public String getShareImageUrl() {
		int size = photos.size();
		if (size == 0) {
			return QqAuthAdapter.DEFAULT_IMAGE;
		}
		else {
			return photos.get(0);
		}
	}

}

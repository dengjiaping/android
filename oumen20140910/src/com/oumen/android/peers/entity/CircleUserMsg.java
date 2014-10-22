package com.oumen.android.peers.entity;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.oumen.android.App;
import com.oumen.android.peers.Prise;
import com.oumen.auth.AuthAdapter.MessageType;
import com.oumen.auth.QqAuthAdapter;
import com.oumen.auth.ShareData;

/**
 * 偶们圈一条消息
 * 
 */
public class CircleUserMsg implements Serializable, ShareData {
	private static final long serialVersionUID = 7588610498808350702L;

	private CircleUserBasicMsg info;
	private int isprise;
	private int prisenum;
	private String prisename;// 最后一个赞的人
	private String priselastname;// 倒数第二个赞的人
	private String lookNum;
	//赞的所有人列表
	public final ArrayList<Prise> prises = new ArrayList<Prise>();
	//评论列表
	public final ArrayList<Comment> comments = new ArrayList<Comment>();

	public CircleUserMsg(JSONObject obj) throws JSONException {
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

		if (obj.has("content"))
			info = new CircleUserBasicMsg(obj.getJSONObject("content"));

		if (obj.has("comment")) {
			JSONArray array = obj.getJSONArray("comment");
			for (int j = 0; j < array.length(); j++) {
				JSONObject r = array.getJSONObject(j);
				comments.add(0, new Comment(info.getCircleId(), r));
			}
		}
		if (obj.has("prise")) {
			JSONArray array = obj.getJSONArray("prise");
			for (int i = 0; i < array.length(); i++) {
				JSONObject r = array.getJSONObject(i);
				prises.add(new Prise(r));
			}
		}
		if (obj.has("looknum")) {
			lookNum = obj.getString("looknum");
		}
	}

	public CircleUserBasicMsg getInfo() {
		return info;
	}

	public void setInfo(CircleUserBasicMsg info) {
		this.info = info;
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

	public String getLookNum() {
		return lookNum;
	}

	public void setLookNum(String lookNum) {
		this.lookNum = lookNum;
	}

	@Override
	public String toString() {
		return "PeerscircleInfo [info=" + info + ", isprise=" + isprise + ", prisenum=" + prisenum + ", prisename=" + prisename + ", priselastname=" + priselastname + ", prises=" + prises + ", comments=" + comments + "]";
	}

	@Override
	public MessageType getShareType() {
		//偶们圈分享有两种情况，有图片就分享图片信息，没有图片就分享文字
		int size = info.photos.size();
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
		return "【偶们】：" + info.getNickname();
	}

	@Override
	public String getShareContent() {
		return info.getContent();
	}

	@Override
	public String getShareLinkUrl() {
		int size = info.photos.size();
		if (size == 0) {
			return QqAuthAdapter.DEFAULT_IMAGE;
		}
		else {
			return info.photos.get(0);//分享第一张图片
		}
	}

	@Override
	public String getShareImageUrl() {
		int size = info.photos.size();
		if (size == 0) {
			return QqAuthAdapter.DEFAULT_IMAGE;
		}
		else {
			return info.photos.get(0);
		}
	}
}

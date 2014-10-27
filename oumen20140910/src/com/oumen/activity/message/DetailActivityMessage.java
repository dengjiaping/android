package com.oumen.activity.message;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.oumen.activity.HuodongTypeUtil.AgeType;
import com.oumen.activity.detail.HuodongDetailHeaderProvider;
import com.oumen.activity.detail.comment.Comment;
import com.oumen.android.App;
import com.oumen.auth.AuthAdapter.MessageType;
import com.oumen.auth.ShareData;
import com.oumen.message.ActivityMember;
import com.oumen.tools.ELog;

/**
 * 活动的实体类
 * 
 */
public class DetailActivityMessage implements Parcelable, ShareData, HuodongDetailHeaderProvider {
	/*
	 * 自主活动
	 */
	public static final int ACTIVITY_TYPE_OF_OUMEN = 0;
	/*
	 * 第三方活动
	 */
	public static final int ACTIVITY_TYPE_OF_THIRD_PARTY = 1;

	private int atId;// 活动编号(atid)
	private String huodongId;
	private String name;// 活动名称
	private String address;// 活动地址

	private int uid;// 用户编号
	private String description;// 活动描述
	private String pic;// 活动海报
	private String lat;// 经度
	private String lng;// 纬度
	private String cityCode;// 活动所在城市

	private String applyEndTime;// 报名截止日0000-00-00
	private String startTime;// 活动开始日 0000-00-00
	private String endTime;// 活动结束日 0000-00-00

	private int applyAge;// 适应年龄
	private String limitNum;// 名额限制
	private String applyNum;// 报名人数
	private String askPhone;// 咨询电话
	private String money;// 活动费用

	private boolean top;// 是否置顶，1是，0否
	private boolean push;// 是否推送，1是，0否

	private boolean hot;// 是否热门，0否，1是
	private boolean over;// 是否结束
	private int distance;// 离请求者的距离米数
	// 发起者信息
	private int senderUid;
	private String senderName;
	private String senderPic;
	private int senderSex;
	// 新增分享url
	private String ShareUrl;
	// 新增群聊ID
	private int teamId;

	private boolean isApply;// 是否报名

	//TODO 新增多少人查看
	private String lookNum;

	private boolean openNotice;// 是否接收群消息

	/*
	 * 活动类型（室内室外）
	 */
	private int huodongType;

	/*
	 * 自主活动或者第三方活动
	 * 0-->自主活动
	 * 1-->第三方活动
	 */
	private int type;

	/*
	 * 活动好评率
	 */
	private float rate;

	/*
	 * 需要跳转的url
	 * 第三方的活动有的可以跳转
	 */
	private String openUrl;

	/*
	 * 最新一条评论
	 */
	private Comment lastComment = null;

	public final ArrayList<ActivityMember> applyers = new ArrayList<ActivityMember>();
	//TODO 新增活动照片集合
	public final ArrayList<String> pics = new ArrayList<String>();
	//推荐
	public final ArrayList<String> commends = new ArrayList<String>();

	public DetailActivityMessage() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(atId);
		dest.writeString(huodongId);
		dest.writeString(name);
		dest.writeString(address);

		dest.writeInt(uid);
		dest.writeString(description);
		dest.writeString(pic);
		dest.writeString(lat);
		dest.writeString(lng);
		dest.writeString(cityCode);

		dest.writeString(applyEndTime);
		dest.writeString(startTime);
		dest.writeString(endTime);

		dest.writeInt(applyAge);
		dest.writeString(limitNum);
		dest.writeString(applyNum);
		dest.writeString(askPhone);
		dest.writeString(money);

		dest.writeInt(top ? 1 : 0);
		dest.writeInt(push ? 1 : 0);

		dest.writeInt(hot ? 1 : 0);
		dest.writeInt(over ? 1 : 0);
		dest.writeInt(distance);

		dest.writeTypedList(applyers);
		dest.writeStringList(pics);

		dest.writeInt(senderUid);
		dest.writeString(senderName);
		dest.writeString(senderPic);
		dest.writeInt(senderSex);
		dest.writeString(ShareUrl);

		dest.writeInt(teamId);
		dest.writeInt(isApply ? 1 : 0);
		dest.writeString(lookNum);
		dest.writeInt(openNotice ? 1 : 0);
		dest.writeInt(huodongType);
		dest.writeInt(type);
		dest.writeFloat(rate);
		dest.writeString(openUrl);
		dest.writeParcelable(lastComment, PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeStringList(commends);
	}

	public static final Parcelable.Creator<DetailActivityMessage> CREATOR = new Parcelable.Creator<DetailActivityMessage>() {
		public DetailActivityMessage createFromParcel(Parcel in) {
			DetailActivityMessage bean = new DetailActivityMessage();
			bean.atId = in.readInt();
			bean.huodongId = in.readString();
			bean.name = in.readString();
			bean.address = in.readString();

			bean.uid = in.readInt();
			bean.description = in.readString();
			bean.pic = in.readString();
			bean.lat = in.readString();
			bean.lng = in.readString();
			bean.cityCode = in.readString();

			bean.applyEndTime = in.readString();
			bean.startTime = in.readString();
			bean.endTime = in.readString();

			bean.applyAge = in.readInt();
			bean.limitNum = in.readString();
			bean.applyNum = in.readString();
			bean.askPhone = in.readString();
			bean.money = in.readString();

			bean.top = in.readInt() == 1 ? true : false;
			bean.push = in.readInt() == 1 ? true : false;

			bean.hot = in.readInt() == 1 ? true : false;
			bean.over = in.readInt() == 1 ? true : false;
			bean.distance = in.readInt();

			in.readTypedList(bean.applyers, ActivityMember.CREATOR);
			in.readStringList(bean.pics);

			bean.senderUid = in.readInt();
			bean.senderName = in.readString();
			bean.senderPic = in.readString();
			bean.senderSex = in.readInt();
			bean.ShareUrl = in.readString();

			bean.teamId = in.readInt();
			bean.isApply = in.readInt() == 1 ? true : false;
			bean.lookNum = in.readString();
			bean.openNotice = in.readInt() == 1 ? true : false;
			bean.huodongType = in.readInt();
			bean.type = in.readInt();
			bean.rate = in.readFloat();
			bean.openUrl = in.readString();
			bean.lastComment = in.readParcelable(DetailActivityMessage.class.getClassLoader());
			in.readStringList(bean.commends);
			return bean;
		}

		public DetailActivityMessage[] newArray(int size) {
			return new DetailActivityMessage[size];
		}
	};

	public DetailActivityMessage(JSONObject obj) throws Exception {
		atId = Integer.parseInt(obj.getString("atid"));
		huodongId = obj.has("huodongid") ? obj.getString("huodongid") : null;
		name = obj.optString("actname");
		address = obj.optString("address");
		uid = Integer.parseInt(obj.optString("uid"));
		description = obj.optString("dis");
		pic = obj.optString("pic");
		lat = obj.optString("lat");
		lng = obj.optString("lng");
		cityCode = obj.optString("ctcode");

		applyEndTime = obj.optString("bmendtime");
		startTime = obj.optString("starttime");
		endTime = obj.optString("endtime");

		String temp1 = obj.optString("applyage");
		if (temp1 != null) {
			applyAge = Integer.valueOf(temp1);
		}
		else {
			applyAge = AgeType.AGE_DEFAULT.code();
		}

		limitNum = obj.optString("limitnum");
		applyNum = obj.optString("bmnum");

		askPhone = obj.optString("askphone");
		money = obj.optString("actmoney");

		top = "1".equals(obj.optString("istop"));
		push = "1".equals(obj.optString("istui"));

		hot = obj.optInt("ishot") == 1;
		over = obj.optInt("isover") == 1;

		if (obj.has("distance")) {
			distance = obj.getInt("distance");
		}
		else if (obj.has("diss")) {
			distance = obj.getInt("diss");
		}

		JSONObject o = null;
		String temp = obj.has("sender") ? obj.getString("sender") : null;
		if (temp != null && !temp.startsWith("[")) {
			o = obj.has("sender") ? obj.getJSONObject("sender") : null;
			if (o != null) {
				//TODO 
				if (o.has("uid")) {
					senderUid = Integer.valueOf(o.getString("uid"));
				}
				senderName = o.getString("nickname");
				senderPic = o.getString("head_photo");
				if (o.has("sex")) {
					senderSex = Integer.valueOf(o.getString("sex"));
				}
			}
		}

		ShareUrl = obj.has("shareUrl") ? obj.getString("shareUrl") : null;

		int tempApply = obj.has("isbm") ? obj.getInt("isbm") : 0;
		isApply = tempApply == 1 ? true : false;
		if (obj.has("teamid")) {
			Object tempObj = obj.get("teamid");
			if (tempObj instanceof String) {
				if (TextUtils.isEmpty(obj.getString("teamid"))) {
					teamId = App.INT_UNSET;
				}
				else {
					teamId = Integer.valueOf((String) (tempObj));
				}
			}
			else {
				teamId = (Integer) (tempObj);
			}
		}
		if (obj.has("users")) {
			JSONArray array = obj.getJSONArray("users");
			for (int i = 0; i < array.length(); i++) {
				JSONObject j = array.getJSONObject(i);
				applyers.add(new ActivityMember(j));
			}
		}
		//TODO　活动多张图片信息
		if (obj.has("piclist")) {
			JSONArray array = obj.getJSONArray("piclist");
			for (int i = 0; i < array.length(); i++) {
				pics.add(array.getString(i));
			}
		}

		lookNum = String.valueOf(obj.getInt("looknum"));
		if (obj.has("isreceive")) {
			openNotice = obj.getInt("isreceive") == 1 ? true : false;
		}

		if (obj.has("hdtypes") && !TextUtils.isEmpty(obj.getString("hdtypes"))) {
			huodongType = Integer.valueOf(obj.getString("hdtypes"));
		}

		//type
		if (obj.has("type") && !TextUtils.isEmpty(obj.getString("type"))) {
			type = Integer.valueOf(obj.getString("type"));
		}
		else {
			type = ACTIVITY_TYPE_OF_OUMEN;
		}

//		rate = App.RATE_FORMAT.format((float) obj.getDouble("star") * 100) + "%";
		rate = (float) obj.getDouble("star") * 100;

		openUrl = obj.optString("viewurl");

		ELog.i(obj.getString("comment"));
		lastComment = TextUtils.isEmpty(obj.getString("comment")) ? null : new Comment(obj.getJSONObject("comment"));

		if (obj.has("commend") && !TextUtils.isEmpty(obj.getString("commend"))) {
			JSONArray array = obj.getJSONArray("commend");
			for (int i = 0; i < array.length(); i++) {
				commends.add(array.getString(i));
			}
		}
	}

	public boolean isApply() {
		return isApply;
	}

	public void setApply(boolean isApply) {
		this.isApply = isApply;
	}

	public String getHuodongId() {
		return huodongId;
	}

	public void setHuodongId(String huodongId) {
		this.huodongId = huodongId;
	}

	public int getSenderUid() {
		return senderUid;
	}

	public void setSenderUid(int senderUid) {
		this.senderUid = senderUid;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public void setSenderSex(int senderSex) {
		this.senderSex = senderSex;
	}

	public int getId() {
		return atId;
	}

	public void setId(int id) {
		this.atId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean hasPic() {
		return !TextUtils.isEmpty(pic);
	}

	public String getPicSourceUrl() {
		if (TextUtils.isEmpty(pic))
			return null;

		return pic;
	}

	public String getPicUrl(int maxLength) {
		if (TextUtils.isEmpty(pic))
			return null;

		return pic + "/small?l=" + maxLength;
	}

	public String getApplyerPhotoUrl(int maxLength) {
		if (TextUtils.isEmpty(senderPic))
			return null;

		return senderPic + "/small?l=" + maxLength;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getApplyEndTime() {
		return applyEndTime;
	}

	public void setApplyEndTime(String applyEndTime) {
		this.applyEndTime = applyEndTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String starttime) {
		this.startTime = starttime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endtime) {
		this.endTime = endtime;
	}

	public int getApplyaAge() {
		return applyAge;
	}

	public void setApplyAge(int applyAge) {
		this.applyAge = applyAge;
	}

	public String getLimitNum() {
		return limitNum;
	}

	public void setLimitNum(String limitNum) {
		this.limitNum = limitNum;
	}

	public String getApplyNum() {
		return applyNum;
	}

	public void setApplyNum(String applyNum) {
		this.applyNum = applyNum;
	}

	public String getAskPhone() {
		return askPhone;
	}

	public void setAskPhone(String askPhone) {
		this.askPhone = askPhone;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public boolean isTop() {
		return top;
	}

	public void setTop(boolean top) {
		this.top = top;
	}

	public boolean isPush() {
		return push;
	}

	public void setPush(boolean push) {
		this.push = push;
	}

	public boolean isHot() {
		return hot;
	}

	public void setHot(boolean hot) {
		this.hot = hot;
	}

	public boolean isOver() {
		return over;
	}

	public void setOver(boolean over) {
		this.over = over;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderPic() {
		return senderPic;
	}

	public void setSenderPic(String senderPic) {
		this.senderPic = senderPic;
	}

	public int getSenderSex() {
		return senderSex;
	}

	public String getShareUrl() {
		return ShareUrl;
	}

	public void setShareUrl(String shareUrl) {
		ShareUrl = shareUrl;
	}

	//==========================分享参数==================================
	/**
	 * 设置分享的参数
	 * shareTitle = "#亲子活动#，小伙伴们我正在偶们参加(" + activityBean.getName() + ")";
	 * shareContent = "专属爸妈版微信，辣妈奶爸必备神器“偶们”，亲们快来看看吧！活动地址：" + activityBean.getAddress() + "；偶们下载链接：" + activityBean.getShareUrl();
	 * shareLinkUrl = activityBean.getShareUrl();
	 * shareImageUrl = activityBean.getPicSourceUrl();
	 */
	@Override
	public MessageType getShareType() {
		return MessageType.TEXT_IMAGE;
	}

	@Override
	public int getActionType() {
		return App.INT_UNSET;
	}

	@Override
	public String getShareTitle() {
		return "#亲子活动#，小伙伴们我正在偶们参加(" + getName() + ")";
	}

	@Override
	public String getShareContent() {
		return "专属爸妈版微信，辣妈奶爸必备神器“偶们”，亲们快来看看吧！活动地址：" + getAddress() + "；偶们下载链接：" + getShareUrl();
	}

	@Override
	public String getShareLinkUrl() {
		return getShareUrl();
	}

	@Override
	public String getShareImageUrl() {
		return getPicSourceUrl();
	}

	@Override
	public ArrayList<String> getHuodongPics() {
		//TODO 现在没有数据
		if (pics.size() == 0) {
			pics.add(pic);
		}
		return pics;

	}

	@Override
	public String getHuodongSenderName() {
		return senderName;
	}

	@Override
	public String getHuodongSenderPhoto() {
		return senderPic;
	}

	@Override
	public String getHuodongTitle() {
		return name;
	}

	@Override
	public String getHuodongAddress() {
		return address;
	}

	@Override
	public String getHuodongTime() {
		return startTime.substring(5, 7) + "月" + startTime.substring(8, 10) + "日";
	}

	@Override
	public String getHuodongPic() {
		return pic;
	}

	@Override
	public String getLookNum() {
		return lookNum;
	}

	@Override
	public boolean getHot() {
		return hot;
	}

	@Override
	public boolean getTui() {
		if (push || top) {
			return true;
		}
		return false;
	}

	public boolean isOpenNotice() {
		return openNotice;
	}

	public void setOpenNotice(boolean openNotice) {
		this.openNotice = openNotice;
	}

	@Override
	public int getHuodongSendId() {
		return senderUid;
	}

	@Override
	public int getHuodongMultiId() {
		return teamId;
	}

	@Override
	public String getHuodongPic(int length) {
		return getPicUrl(length);
	}

	@Override
	public ArrayList<String> getHuodongPics(int length) {
		if (pics.size() == 0) {
			pics.add(getPicUrl(length));
		}
		return pics;
	}

	@Override
	public boolean ishuodongFinish() {
		return over;
	}

	public int getHuodongType() {
		return huodongType;
	}

	public void setHuodongType(int type) {
		this.huodongType = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	public String getOpenUrl() {
		return openUrl;
	}

	public void setOpenUrl(String openUrl) {
		this.openUrl = openUrl;
	}

	public Comment getLastComment() {
		return lastComment;
	}

	public void setLastComment(Comment lastComment) {
		this.lastComment = lastComment;
	}

	public void setLookNum(String lookNum) {
		this.lookNum = lookNum;
	}
}

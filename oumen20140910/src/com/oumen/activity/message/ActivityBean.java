package com.oumen.activity.message;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.oumen.activity.detail.HuodongDetailHeaderProvider;
import com.oumen.android.App;
import com.oumen.auth.AuthAdapter.MessageType;
import com.oumen.auth.ShareData;
import com.oumen.message.ActivityMember;

/**
 * 活动的实体类
 * 
 */
public class ActivityBean implements Parcelable, ShareData, HuodongDetailHeaderProvider {
	
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

	private String applyAge;// 适应年龄
	private String limitNum;// 名额限制
	private String applyNum;// 报名人数
	private String askPhone;// 咨询电话
	private String money;// 活动费用
	private String updateTime;//

	private boolean top;// 是否置顶，1是，0否
	private boolean push;// 是否推送，1是，0否
	private String pushTime;
	private boolean verified;// 是否通过审核

	private String modTime;// 活动修改时间
	private String addTime;// 活动添加时间
	private String userType;// 企业或者个人

	private boolean hot;// 是否热门，0否，1是
	private boolean today;// 是否当天
	private boolean over;// 是否结束
	private boolean weekend;
	private String topTime;
	private int distance;// 离请求者的距离米数
	public final ArrayList<ActivityMember> applyers = new ArrayList<ActivityMember>();
	//TODO 新增活动照片集合
	public final ArrayList<String> pics = new ArrayList<String>();

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

	private int haoPing;// 好评
	private int chaPing;// 差评

	private boolean startPingfen;// 是否开始评分
	
	private int type;

	public ActivityBean() {
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

		dest.writeString(applyAge);
		dest.writeString(limitNum);
		dest.writeString(applyNum);
		dest.writeString(askPhone);
		dest.writeString(money);
		dest.writeString(updateTime);

		dest.writeInt(top ? 1 : 0);
		dest.writeInt(push ? 1 : 0);
		dest.writeString(pushTime);
		dest.writeInt(verified ? 1 : 0);

		dest.writeString(modTime);
		dest.writeString(addTime);
		dest.writeString(userType);

		dest.writeInt(hot ? 1 : 0);
		dest.writeInt(today ? 1 : 0);
		dest.writeInt(over ? 1 : 0);
		dest.writeInt(weekend ? 1 : 0);
		dest.writeString(topTime);
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
		dest.writeInt(haoPing);
		dest.writeInt(chaPing);
		dest.writeInt(startPingfen ? 1 : 0);
		dest.writeInt(type);
	}

	public static final Parcelable.Creator<ActivityBean> CREATOR = new Parcelable.Creator<ActivityBean>() {
		public ActivityBean createFromParcel(Parcel in) {
			ActivityBean bean = new ActivityBean();
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

			bean.applyAge = in.readString();
			bean.limitNum = in.readString();
			bean.applyNum = in.readString();
			bean.askPhone = in.readString();
			bean.money = in.readString();
			bean.updateTime = in.readString();

			bean.top = in.readInt() == 1 ? true : false;
			bean.push = in.readInt() == 1 ? true : false;
			bean.pushTime = in.readString();
			bean.verified = in.readInt() == 1 ? true : false;

			bean.modTime = in.readString();
			bean.addTime = in.readString();
			bean.userType = in.readString();

			bean.hot = in.readInt() == 1 ? true : false;
			bean.today = in.readInt() == 1 ? true : false;
			bean.over = in.readInt() == 1 ? true : false;
			bean.weekend = in.readInt() == 1 ? true : false;
			bean.topTime = in.readString();
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
			bean.haoPing = in.readInt();
			bean.chaPing = in.readInt();
			bean.startPingfen = in.readInt() == 1 ? true : false;
			bean.type = in.readInt();
			return bean;
		}

		public ActivityBean[] newArray(int size) {
			return new ActivityBean[size];
		}
	};

	public ActivityBean(JSONObject obj) throws JSONException {
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

		applyAge = obj.optString("applyage");
		limitNum = obj.optString("limitnum");
		applyNum = obj.optString("bmnum");

		askPhone = obj.optString("askphone");
		money = obj.optString("actmoney");
		updateTime = obj.optString("updatetime");

		top = "1".equals(obj.optString("istop"));
		push = "1".equals(obj.optString("istui"));
		verified = "1".equals(obj.optString("ispass"));

		modTime = obj.optString("modtime");
		addTime = obj.optString("addtime");
		userType = obj.optString("usertype");

		hot = obj.optInt("ishot") == 1;
		today = obj.optInt("istoday") == 1;
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

		if (obj.has("haoping")) {
			String tempstr = obj.getString("haoping");
			if (!TextUtils.isEmpty(tempstr)) {
				haoPing = Integer.valueOf(tempstr);
			}
		}

		if (obj.has("chaping")) {
			String tempstr = obj.getString("chaping");
			if (!TextUtils.isEmpty(tempstr)) {
				chaPing = Integer.valueOf(tempstr);
			}
		}

		if (obj.has("startpingfen")) {
			startPingfen = obj.getInt("startpingfen") == 1 ? true : false;
		}
		
		if(obj.has("hdtypes") && !TextUtils.isEmpty(obj.getString("hdtypes"))) {
			type = Integer.valueOf(obj.getString("hdtypes"));
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

	public String getApplyaAge() {
		return applyAge;
	}

	public void setApplyAge(String applyAge) {
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

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
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

	public String getPushTime() {
		return pushTime;
	}

	public void setPushTime(String pushTime) {
		this.pushTime = pushTime;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public String getModTime() {
		return modTime;
	}

	public void setModTime(String modTime) {
		this.modTime = modTime;
	}

	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public boolean isHot() {
		return hot;
	}

	public void setHot(boolean hot) {
		this.hot = hot;
	}

	public boolean isToday() {
		return today;
	}

	public void setToday(boolean today) {
		this.today = today;
	}

	public boolean isOver() {
		return over;
	}

	public void setOver(boolean over) {
		this.over = over;
	}

	public boolean isWeekend() {
		return weekend;
	}

	public void setWeekend(boolean weekend) {
		this.weekend = weekend;
	}

	public String getTopTime() {
		return topTime;
	}

	public void setTopTime(String topTime) {
		this.topTime = topTime;
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

	public int getHaoPing() {
		return haoPing;
	}

	public void setHaoPing(int haoPing) {
		this.haoPing = haoPing;
	}

	public int getChaPing() {
		return chaPing;
	}

	public void setChaPing(int chaPing) {
		this.chaPing = chaPing;
	}
	
	public boolean isStartPingfen() {
		return startPingfen;
	}

	public void setStartPingfen(boolean startPingfen) {
		this.startPingfen = startPingfen;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}

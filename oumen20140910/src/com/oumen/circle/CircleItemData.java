package com.oumen.circle;

import com.oumen.android.peers.entity.Comment;
import com.oumen.android.peers.entity.CircleUserMsg;

public class CircleItemData {
	public int groupIndex;
	public CircleUserMsg groupData;

	public int commentIndex;
	public Comment commentData;

//	@Override
//	public MessageType getShareType() {
//		//偶们圈分享有两种情况，有图片就分享图片信息，没有图片就分享文字
//		int size = groupData.getInfo().photos.size();
//		if (size == 0) {
//			return MessageType.TEXT;
//		}
//		else {
//
//			return MessageType.IMAGE_ONLY;
//		}
//	}
//
//	@Override
//	public int getActionType() {
//		return App.INT_UNSET;
//	}
//
//	@Override
//	public String getShareTitle() {
//		return "【偶们】：" + groupData.getInfo().getNickname();
//	}
//
//	@Override
//	public String getShareContent() {
//		return groupData.getInfo().getContent();
//	}
//
//	@Override
//	public String getShareLinkUrl() {
//		int size = groupData.getInfo().photos.size();
//		if (size == 0) {
//			return QqAuthAdapter.DEFAULT_IMAGE;
//		}
//		else {
//			return groupData.getInfo().photos.get(0);//分享第一张图片
//		}
//	}
//
//	@Override
//	public String getShareImageUrl() {
//		int size = groupData.getInfo().photos.size();
//		if (size == 0) {
//			return QqAuthAdapter.DEFAULT_IMAGE;
//		}
//		else {
//			return groupData.getInfo().photos.get(0);
//		}
//	}
}

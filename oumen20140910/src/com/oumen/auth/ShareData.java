package com.oumen.auth;

import com.oumen.auth.AuthAdapter.MessageType;

public interface ShareData {
	/**
	 * 分享类型
	 * @param type
	 */
	public MessageType getShareType();
	/**
	 * 微信朋友圈，还是微信好友类型
	 * @return
	 */
	public int getActionType();
	/**
	 * 分享标题
	 * @return
	 */
	public String getShareTitle();
	/**
	 * 分享内容
	 * @return
	 */
	public String getShareContent();
	/**
	 * 分享网页链接
	 * @return
	 */
	public String getShareLinkUrl();
	/**
	 * 分享图片链接
	 * @return
	 */
	public String getShareImageUrl();
}

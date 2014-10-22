package com.oumen.activity.detail;

import java.util.ArrayList;
/**
 * 活动详情头部接口
 *
 */
public interface HuodongDetailHeaderProvider {
	/**
	 * 获取单张活动图图片
	 * @return
	 */
	public String getHuodongPic();
	/**
	 * 获取单张活动图片（指定大小）
	 * @param lenght
	 * @return
	 */
	public String getHuodongPic(int lenght);
	/**
	 * 获取活动详情图片集合
	 * @return
	 */
	public ArrayList<String> getHuodongPics();
	/**
	 * 获取活动详情图片集合（指定大小）
	 * @param lenght
	 * @return
	 */
	public ArrayList<String> getHuodongPics(int lenght);
	/**
	 * 获取活动发起者的id
	 * @return
	 */
	public int getHuodongSendId();
	/**
	 * 获取活动发起者的昵称
	 * @return
	 */
	public String getHuodongSenderName();
	
	/**
	 * 获取活动发起者的头像
	 * @return
	 */
	public String getHuodongSenderPhoto();
	
	/**
	 * 获取活动名称
	 * @return
	 */
	public String getHuodongTitle();
	
	/**
	 * 获取活动地址
	 * @return
	 */
	public String getHuodongAddress();
	
	/**
	 * 获取活动时间
	 * @return
	 */
	public String getHuodongTime();
	/**
	 * 获取多少人查看
	 */
	public String getLookNum();
	/**
	 * 是否热门
	 * @return
	 */
	public boolean getHot();
	public boolean getTui();
	
	public int getHuodongMultiId();
	
	public boolean ishuodongFinish();
}

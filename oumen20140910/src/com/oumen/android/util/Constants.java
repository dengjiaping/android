package com.oumen.android.util;

import android.os.Environment;

public class Constants {
	public static final boolean isDebug = false;
	public static String SOCKET_SERVER_IP = "114.113.228.183";// 服务器ip
	public static int SOCKET_SERVER_PORT = 9021;// 服务器端口
	public static final String HTTP_SERVER = "http://www.oumen.com";
	public static final String DEFALUT_URL = HTTP_SERVER + ":8088/";
	public static final String DEFALUT_ACTIVITY_URL = HTTP_SERVER + "/";
	public static final String MV_DEFAULT_URL = HTTP_SERVER + "/";
	public static final String GET_MV_ID = MV_DEFAULT_URL + "uploadmv/act/getId";// 获取视频编号
	public static final String MV_UPLOAD_FILE = MV_DEFAULT_URL + "uploadmv/act/uploadFile";//分片上传
	public static final String MV_SHARE_TO_SINA_URL = MV_DEFAULT_URL + "user/share/weibo_share";// mv新浪微博分享
	public static final String MV_WEB_URL = MV_DEFAULT_URL + "user/share/show?";// 网页查看mv链接
	public static final String MV_COVER_URL = DEFALUT_URL +"img/square/";// mv片头链接
	public static final String WEIXIN_APP_ID = "wx40c3611b66e1084a";// 微信app_id
	public static final String WEIXIN_APP_SECRET = "29e1452a53f670a3d8cf750234179cf2";

//	public static final boolean isDebug = true;
//	public static String SOCKET_SERVER_IP = "114.113.228.123";
//	public static int SOCKET_SERVER_PORT = 9020;
//	public static final String HTTP_SERVER = "http://114.113.228.123";
//	public static final String DEFALUT_URL = HTTP_SERVER + ":8088/";
//	public static final String DEFALUT_ACTIVITY_URL = HTTP_SERVER + ":9001/";
//	public static final String MV_DEFAULT_URL = HTTP_SERVER + ":8088/";
//	public static final String GET_MV_ID = MV_DEFAULT_URL + "mv/act/getId";// 获取视频编号
//	public static final String MV_UPLOAD_FILE = MV_DEFAULT_URL + "mv/act/uploadFile";//分片上传
//	public static final String MV_SHARE_TO_SINA_URL = MV_DEFAULT_URL + "user/share/weibo_share";// mv新浪微博分享
//	public static final String MV_WEB_URL = MV_DEFAULT_URL + "user/share/show?";//	 网页查看mv链接
//	public static final String MV_COVER_URL = DEFALUT_URL +"img/square/";//mv片头链接
//	public static final String WEIXIN_APP_ID = "wxc2fdcfbba7f53752";// 微信测试api
//	public static final String WEIXIN_APP_SECRET = "16e542c2d8517ab6b61c86c2f678ef3f";
//	public static final String WEIXIN_APP_SECRET = "29e1452a53f670a3d8cf750234179cf2";

	public static final String SAVE_USER = "saveUser";// 保存用户信息的xml文件名
	public static final String DBNAME = "omchat.db";// 数据库名称

	public static final int NULL_INT = -1;

	public static final int CONNECT_TIMEOUT = 10 * 1000;// 连接超时

	public static final String PATTERN_EMAIL = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
	public static final String PATTERN_TEL = "(^1(3[0-9]|5[0-35-9]|8[025-9])\\d{8}$)|(^1(34[0-8]|(3[5-9]|5[017-9]|8[278])\\d)\\d{7}$)|(^1(3[0-2]|5[256]|8[56])\\d{8}$)|(^1((33|53|8[09])[0-9]|349)\\d{7}$)|(^(0[0-9]{2,3}-)?[0-9]{6,8}$)";
	public static final String PATTERN_NUMBER = "^[0-9]+$";
	
	// 路径
	public static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath() + "/oumen/";
	public static final String PROFILE_PATH = BASE_PATH + "profile/%d/";
	public static final String SELF_HEAD_PHOTO_PATH = BASE_PATH + "profile/%d/photo";
	public static final String COMPANY_PATH = BASE_PATH + "company/";
	public static final String IMAGE_PATH = BASE_PATH + "images/";
	public static final String UPLOAD_PATH = BASE_PATH + "upload/";
	public static final String IMAGE_CACHE_PATH = BASE_PATH + "cache/";
	public static final String LIBARYS_PATH = BASE_PATH + "libs/";
	public static final String CITYS_DB_PATH = LIBARYS_PATH + "citys";
	public static final String APK_PATH = BASE_PATH + "downloads/";
	public static final String OUMEN_PUBLISH_PATH = IMAGE_PATH + "publish";
	public static final String HEAD_PHOTO_PATH = IMAGE_PATH + "avatar";
	public static final String ACTIVITY_COVER_PATH = IMAGE_PATH + "cover";
	public static final String HEAD_PHOTO_NAME = "head.jpg";

	// 是否有新版本
	public static final String CHECK_VERSION = DEFALUT_URL + "apk/act/getanLastVersion";
	// 向服务器发送位置信息
	public static final String GPS_URL = DEFALUT_URL + "user/location/new_location";
	public static final String OBTAIN_SPLASH_IMAGE = DEFALUT_URL + "user/user/firstPic";

	// ===============================登录注册======================================
	public static final String OBTAIN_GUEST_ID = DEFALUT_URL + "user/user/no_regist";//获取游客id
	public static final String LOGIN_URL = DEFALUT_URL + "user/user/login";// 登录界面
//	public static final String REGISTER_URL = DEFALUT_URL + "user/user/register";// 注册界面
	public static final String REGISTER_URL = DEFALUT_URL + "user/user/reg";// 注册界面(测试)
	public static final String LOGIN_FIND_USERMESSAGE_URL = DEFALUT_URL + "user/password/message_pass";
	public static final String LOGIN_FIND_PASSWORD_URL = DEFALUT_URL + "user/password/s_mail";// 找回密码
	
	public static final String REQUEST_PHONE_CODE = DEFALUT_URL + "user/user/sendCode";//获取手机验证码
	public static final String CONFRIM_PHONE_CODE =  DEFALUT_URL + "user/user/checkCode";//向服务器发送验证码
	
	public static final String PHONE_MODIFY_PASSWORD_URL = DEFALUT_URL + "user/user/findPwd";// 手机找回密码

	public static final String REGISTER_EMAIL = "register_email";
	public static final String LOGOUT_URL = DEFALUT_URL + "user/user/logout";// 注销
	public static final String LOGIN_OTHER_URL = DEFALUT_URL + "user/user/login_other_n";
	public static final String BIND_SINA_WEIBO = DEFALUT_URL + "user/token/weibo_t";
	public static final String BIND_TENCENT_QQ = DEFALUT_URL + "user/token/qq_t";

	// 新浪微博
	public static final String CONSUMER_KEY = "3640355425";// 替换为开发者的appkey，例如"1646212860";

	public static final String SHARE_ADD_PHOTOS = "imagepath";
	// =================================附近人信息====================================
	public static final String NRAR_BY_URL = DEFALUT_URL + "user/friend/f_nearby_n";
	public static final String NRAR_BY_NEXT = DEFALUT_URL + "user/friend/f_nearby_next_n";
	// 获取附近活动
	public static final String GET_NEAR_ACTIVITIES = DEFALUT_URL + "user/friend/actnearby";
	// 获取附近活动数量和人数
	public static final String GET_NEAR_DATA = DEFALUT_URL + "user/user/getTuiData";

	public static final String INVITE_FRIEND_URL = DEFALUT_URL + "user/mail/s_mail";
	// ==================================用户中心数据====================================
	public static final String USERCENTER_GET_MESSAGE = DEFALUT_URL + "user/message/message_message";
	// 上传文件接口
	public static final String UPLOAD_HEADPHOTO = DEFALUT_URL + "file/pic/upload";
	// 头像
	public static final String USERCENTER_PHOTO = DEFALUT_URL + "user/message/head_photo_n";
	// 验证旧密码
	public static final String USERCENTER_OLDPWD = DEFALUT_URL + "user/password/y_pass";
	// 新密码
	public static final String USERCENTER_NEWPWD = DEFALUT_URL + "user/password/e_pass";
	// 时间转换
	public static final String USERCENTER_TURNER = DEFALUT_URL + "user/message/message_babytypeshow";
	// 修改用户中心的信息
	public static final String USERCENTER_UPDATEUSERINFO = DEFALUT_URL + "user/message/message_edit";

	public static final String MANIFESTO = "setmanifestosuccess";

	// 退出
	public static final String USERCENTER_EXIT = DEFALUT_URL + "user/user/logout";
	// 新浪授权
	public static final String USERCENTER_SINA = DEFALUT_URL + "user/token/weibo_t";
	//修改昵称
	public static final String USERCENTER_UPDATE_NICKNAME = DEFALUT_URL + "user/message/message_username";
	//修改地址
	public static final String USERCENTER_UPDATE_ADDRESS = DEFALUT_URL + "user/message/message_address";
	//宝宝签名设置
	public static final String USERCENTER_UPDATE_SING = DEFALUT_URL + "user/message/message_manifesto";
	//未出生宝宝时间设置
	public static final String USERCENTER_UPDATE_HUAIYUN_TIME = DEFALUT_URL + "user/message/message_babytypeno";
	//已出生宝宝时间设置
	public static final String USERCENTER_UPDATE_CHUSEHNG_TIME = DEFALUT_URL + "user/message/message_babytypeyes";
	//已出生宝宝时间设置
	public static final String USERCENTER_UPDATE_CHUSHENG_SEX = DEFALUT_URL + "user/message/message_babysex";
	//用户性别设置
	public static final String USERCENTER_UPDATE_USER_SEX = DEFALUT_URL + "user/message/user_sex";
	
	// ==========================================偶们圈======================================
	// 获取发表的内容
	public static final String OUMENCIRCLE_GETCONTENT = DEFALUT_URL + "omcycle/act/getContent";
	// 发表
	public static final String OUMENCIRCLE_GREATECONTENT = DEFALUT_URL + "omcycle/act/createContent";
	// 赞（喜欢）
	public static final String OUMENCIRCLE_FAVOUR = DEFALUT_URL + "omcycle/comment/r_praise";
	// 取消赞
	public static final String OUMENCIRCLE_FAVOUR_CANCEL = DEFALUT_URL + "omcycle/comment/r_praise_del";
	// 更换背景图片
	public static final String OUMENCIRCLE_CHANGEBACKGROUND = DEFALUT_URL + "omcycle/act/setCyclePic";
	// 获取背景图片
	public static final String OUMENCIRCLE_GETBACKGROUND = DEFALUT_URL + "omcycle/act/getCyclePic";
	// 发表评论
	public static final String OUMENCIRCLE_WRITECOMMENT = DEFALUT_URL + "omcycle/comment/r_comment";
	// 删除评论
	public static final String OUMENCIRCLE_DELETECOMMENT = DEFALUT_URL + "omcycle/comment/r_comment_del";
	// 删除偶们圈的一条内容
	public static final String OUMENCIRCLE_DELETECONTENT = DEFALUT_URL + "omcycle/act/delOneConent";

	// 个人信息
	public static final String OUMENCIRCLE_GETUSERINFO = DEFALUT_URL + "omcycle/act/getOnesContent";
	// 分享
	public static final String OUMENCIRCLE_SHARESINA = DEFALUT_URL + "user/act/sendSina";

	// 聊天邀请
	public static final String CHATFRIENT_ADDFRIEND = DEFALUT_URL + "user/friend/f_find";
	// 联网查找好友
	public static final String FIND_FRIEND_SERVICE = DEFALUT_URL + "user/friend/user_friend_n";
	// 偶们圈消息详情
	public static final String OUMENCIRCLE_NOTICE_DETAIL = DEFALUT_URL + "omcycle/act/getDetail";
	// 清空偶们圈消息提醒列表
	public static final String OUMENCIRCLE_NOTICE_DELDTE = DEFALUT_URL + "omcycle/comment/messageListAllDel";
	// 点击查看更多偶们圈消息提醒
	public static final String OUMENCIRCLE_NOTICE_CHECK_MORE = DEFALUT_URL + "omcycle/comment/getMessageList";
	// =================================活动接口============================================
	// 获取活动
	public static final String GET_ACTIVITIES = DEFALUT_ACTIVITY_URL + "huodong/act/gethdlist";
	// 获取活动详细信息
	public static final String GET_AMUSEMENT_DETAIL = DEFALUT_ACTIVITY_URL + "huodong/act/getDetail";
	// 提交企业资质
	public static final String OUMENAMUSEMENT_USERINFO = DEFALUT_ACTIVITY_URL + "huodong/act/submitZiZhi";
	// 判断企业是否通过资质审核
	public static final String OUMENAMUSEMENT_ISPASSED = DEFALUT_ACTIVITY_URL + "huodong/act/ispass";
	// 发表活动
	public static final String OUMENAMUSEMENT_ADDACTION = DEFALUT_ACTIVITY_URL + "huodong/act/addHuodong";
	// 报名参加活动
	public static final String OUMENAMUSEMENT_APPLY = DEFALUT_ACTIVITY_URL + "huodong/act/baoming";
	// 获取轮播图信息
	public static final String OUMENAMUSEMENT_IMAGESWITCH = DEFALUT_ACTIVITY_URL + "huodong/fh/getScrollPic";
	// 活动新浪分享接口
	public static final String OUMENAMUSEMENT_SHARE_TO_SINA = DEFALUT_ACTIVITY_URL + "user/share/weibo_hd_share";
	//网页活动新浪分享
	public static final String OUMENAMUSEMENT_WEB_SHARE_TO_SINA = DEFALUT_URL + "user/share/shareSina";
	//向服务器发送请求活动推送请求
	public static final String GET_PUSH_ACTIVITY = DEFALUT_URL + "user/user/getPushData";
	//关闭或者打开活动消息提醒(POST)
	public static final String CLOSE_OR_OPEN_HUODONG_MESSAGET_NOTICE = DEFALUT_ACTIVITY_URL + "huodong/act/setReceive";
	//退出活动群(POST)
	public static final String EXIT_HUODONG = DEFALUT_ACTIVITY_URL + "huodong/act/delMember";
	//填写电话号码
	public static final String SET_PHONE_NUM = DEFALUT_ACTIVITY_URL + "huodong/act/addBMphone";
	// 活动评分接口
	public static final String SET_PING_FEN = DEFALUT_ACTIVITY_URL + "huodong/act/pingfen";
	// 用户是否评分
	public static final String NEED_PING_FEN = DEFALUT_ACTIVITY_URL + "huodong/act/ispingfen";
	// 获取用户自己参加的活动列表
	public static final String GET_USER_ACTIVITY_LIST = DEFALUT_ACTIVITY_URL + "huodong/act/myActivity";
	// 获取热门关键词
	public static final String GET_HOT_KEY_WORDS = DEFALUT_ACTIVITY_URL + "huodong/act/getHotword";
	//活动评论列表接口
	public static final String GET_ACTIVITY_COMMENTS = DEFALUT_ACTIVITY_URL + "huodong/act/commentList";
	// 活动发表评论
	public static final String PUBLISH_ACTIVITY_COMMENTS = DEFALUT_ACTIVITY_URL + "huodong/act/addComment";
	
	// =================================MV接口============================================
	public static final String GET_MV_PREFIX = DEFALUT_URL + "mv/act/getnewpiclist";
	
	//================================聊天表情接口=========================================
	public static final String CHAT_BIAOQING_OUBA_DEFAULT = "http://www.oumen.com/biaoqing/1/";
	public static final String CHAT_BIAOQING_OUBA = CHAT_BIAOQING_OUBA_DEFAULT + "1.zip";
	public static final String CHAT_BIAOQING_CIWEI_DEFAULT = "http://www.oumen.com/biaoqing/2/";
	public static final String CHAT_BIAOQING_CIWEI = CHAT_BIAOQING_CIWEI_DEFAULT + "2.zip";
	
	// ========================缓存参数=================================
	public static final String CACHE_NEAR_KEY = "near";
	public static final String CACHE_DADAY_ISHERE_KEY = "dadayishere";
	// 偶们圈的背景图片
	public static final String OUMENCIRCLE_BACKGROUND_PICTURE = "oumencirclebackground";
	// 偶们附近的信息
	public static final String OUMENNEARBY_MESSAGE = "oumennearbymessage";
	// 活动信息
	public static final String AMUSEMENT_MESSAGE = "amusementmessage";

	// 申请资质，获取城市信息
	public static boolean isGetCity = false;
	public static boolean isSetting = false;
	// 新建活动获取城市
	public static boolean isBuiding = false;

	// 没有网络显示字段
	public static final String NET_FAIL = "网络不给力，请检查网络连接";

	public static final int REQUEST_CODE_PICK_IMAGE = 9999;
	public static final int REQUEST_CODE_OPEN_CAMERA = 9998;

}

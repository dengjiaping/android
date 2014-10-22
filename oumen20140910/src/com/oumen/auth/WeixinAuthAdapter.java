package com.oumen.auth;

import java.net.URL;

import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.Toast;
/**
 * 
 * 微信分享
 *
 */
public class WeixinAuthAdapter extends AuthAdapter {
	public static final int SHARE_WEIXIN_FRIEND = SendMessageToWX.Req.WXSceneSession;//分享给朋友
	public static final int SHARE_WEIXIN_CIRCLE = SendMessageToWX.Req.WXSceneTimeline;//分享到朋友圈

	public static final String TYPE_FILE = "type_file_message";
	private IWXAPI api;
	private Activity activity;

	public WeixinAuthAdapter(AuthListener listener) {
		super(listener);
	}

	@Override
	public void authorize(Activity activity) {
		api = WXAPIFactory.createWXAPI(activity, null);
		api.registerApp(Constants.WEIXIN_APP_ID);
		// 调取微信授权界面
		final SendAuth.Req req = new SendAuth.Req();
		req.scope = "snsapi_userinfo";
		req.state = "wechat_sdk_demo_test";// TODO 此处可以自己定义，微信会原封不动的给返回来，用此进行判断
		api.sendReq(req);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	}

	@Override
	public void shareInit(Activity activity) {
		this.activity = activity;
		api = WXAPIFactory.createWXAPI(activity, Constants.WEIXIN_APP_ID, false);
	}
	
	public static boolean isAuthor() {
		if (!TextUtils.isEmpty(App.PREFS.getWeiXinId()) && !TextUtils.isEmpty(App.PREFS.getWeiXinToken()) && App.PREFS.getWeiXinExprise() != App.INT_UNSET) {
			String currenttime = String.valueOf(System.currentTimeMillis());
			if (currenttime.length() > 10) {
				currenttime = currenttime.substring(0, 10);
			}
			if (Long.valueOf(App.PREFS.getWeiXinExprise()) - Long.valueOf(currenttime) >= 0) {// 判断授权是否过期
				// 已经授权
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public void share(MessageType msgType, int actionType, String title, String content, String linkUrl, String imageUrl) {
		//判断本地是否安装微信
		if(!api.isWXAppInstalled() && !api.isWXAppSupportAPI()){
			Toast.makeText(activity, "本地没有安装微信客户端或者版本不支持", Toast.LENGTH_SHORT).show();
			return ;
		}
		
		if (!TextUtils.isEmpty(content) && content.length() > 120) {
			content = content.substring(0, 120);
		}
		
		if (MessageType.TEXT.equals(msgType)) {
			sendTextMessage(content, actionType);
		}
		else if (MessageType.IMAGE_ONLY.equals(msgType)) {
			sendImageMessage(linkUrl, actionType);
		}
		else if (MessageType.TEXT_IMAGE.equals(msgType)) {
			sendWebMessage(linkUrl, title, content, imageUrl, actionType);
		}
		else if (MessageType.VIDEO.equals(msgType)) {
			sendVideoMessage(linkUrl, title, content, imageUrl, actionType);
		}
	}

	/**
	 * 分享给微信好友
	 * 
	 * @param text
	 */
	public void sendTextMessage(String text, int type) {
		final WXTextObject textObj = new WXTextObject();
		textObj.text = text;
		// 用WXTextObject对象初始化一个WXMediaMessage对象
		final WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		// 发送文本类型的消息时，title字段不起作用
		if (text.length() > 342) {
			text = text.substring(0, 342);
		}
		msg.description = text;
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
		req.message = msg;
		req.scene = type;// 分享到微信好友/朋友圈
		// 调用api接口发送数据到微信
		api.sendReq(req);
	}

	/**
	 * 发送图片消息到微信朋友圈
	 * 
	 * @param url图片地址
	 */
	public void sendImageMessage(final String url, final int type) {
		App.THREAD.execute(new Runnable() {

			@Override
			public void run() {
				try {
//					tempFile = new File(App.getUploadCachePath(), System.currentTimeMillis() + ".jpg");
					WXImageObject imgObj = new WXImageObject();
					imgObj.imageUrl = url;

					WXMediaMessage msg = new WXMediaMessage();
					msg.mediaObject = imgObj;

					Bitmap bmp = BitmapFactory.decodeStream(new URL(url).openStream());
					bmp = ImageTools.scale(bmp, 100, 100);
					ELog.i(String.valueOf(bmp.getByteCount()));
//					ImageTools.save(bmp, tempFile);
//					Bitmap thumbBmp = ImageTools.decodeFile(tempFile.getAbsolutePath());// 将图片压缩到32k以下
//					bmp.recycle();
//					msg.thumbData = ImageTools.bmpToByteArray(thumbBmp, true);
					msg.setThumbImage(bmp);

					SendMessageToWX.Req req = new SendMessageToWX.Req();
					req.transaction = buildTransaction("img");
					req.message = msg;
					req.scene = type;
					api.sendReq(req);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * 分享链接到微信好友
	 * 
	 * @param webUrl链接
	 * @param title标题
	 * @param address地址
	 * @param imageUrl图片
	 */
	public void sendWebMessage(final String webUrl, final String title, final String content, final String imageUrl, final int type) {
		App.THREAD.execute(new Runnable() {
			@Override
			public void run() {
				try {
//					tempFile = new File(App.getUploadCachePath(), System.currentTimeMillis() + ".jpg");
					WXWebpageObject webpage = new WXWebpageObject();
					webpage.webpageUrl = webUrl;

					WXMediaMessage msg = new WXMediaMessage(webpage);
					msg.title = title;
					String temp = null;
					if (content.length() > 342) {
						temp = content.substring(0, 342);
					}
					else {
						temp = content;
					}
					msg.description = temp;
					Bitmap bmp = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
					bmp = ImageTools.scale(bmp, 100, 100);
					ELog.i(String.valueOf(bmp.getByteCount()));
					msg.setThumbImage(bmp);
//					ImageTools.save(bmp, tempFile);
//					ELog.i(tempFile.getAbsolutePath());
//					Bitmap thumbBmp = ImageTools.decodeFile(tempFile.getAbsolutePath());// 将图片压缩到32k以下
//					bmp.recycle();
//					msg.thumbData = ImageTools.bmpToByteArray(thumbBmp, true);// 转换成二进制

					SendMessageToWX.Req req = new SendMessageToWX.Req();
					req.transaction = buildTransaction("webpage");
					req.message = msg;
					req.scene = type;
					api.sendReq(req);
				}

				catch (Exception e) {
					ELog.e("Exception e =" + e.toString());
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 分享MV给微信好友
	 * 
	 * @param videoUrl
	 * @param title
	 * @param content
	 * @param imageCover
	 */
	public void sendVideoMessage(final String videoUrl, final String title, final String content, final String imageCover, final int type) {
		App.THREAD.execute(new Runnable() {
			@Override
			public void run() {
				try {
//					tempFile = new File(App.getUploadCachePath(), System.currentTimeMillis() + ".jpg");
					WXVideoObject video = new WXVideoObject();
					video.videoUrl = videoUrl;
					WXMediaMessage msg = new WXMediaMessage(video);
					msg.title = title;
					msg.description = "我正在用“偶们”制作宝宝MV，你也给你的宝宝制作一个吧！";

					Bitmap bmp = BitmapFactory.decodeStream(new URL(imageCover).openStream());
					bmp = ImageTools.scale(bmp, 100, 100);
					ELog.i(String.valueOf(bmp.getByteCount()));
					msg.setThumbImage(bmp);
					
//					ImageTools.save(imageCover, tempFile);
//					Bitmap thumbBmp = ImageTools.decodeFile(tempFile.getAbsolutePath());// 将图片压缩到32k以下
//					msg.thumbData = ImageTools.bmpToByteArray(thumbBmp, true);// 转换成二进制

					SendMessageToWX.Req req = new SendMessageToWX.Req();
					req.transaction = buildTransaction("video");
					req.message = msg;
					req.scene = type;
					api.sendReq(req);
				}
				catch (Exception e) {
					ELog.e("Exception e =" + e.toString());
					e.printStackTrace();
				}
			}
		});
	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
}

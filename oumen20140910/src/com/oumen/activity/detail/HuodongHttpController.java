package com.oumen.activity.detail;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.oumen.activity.message.DetailActivityMessage;
import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;

import android.os.Handler;

public class HuodongHttpController {
	public static final int HANDLER_GET_HUODONG_DETAIL = 10;
	public static final int HANDLER_SET_PHONENUM_SUCCESS = 11;
	public static final int HANDLER_SET_PHONENUM_FAIL = 12;
	
	public static final int HANDLER_PINGFEN_SUCCESS = 13;
	public static final int HANDLER_PINGFEN_FAIL = 14;
	
	public static final int HANDLER_NEED_PING_FEN = 15;
	public static final int HANDLER_NO_NEED_PING_FEN = 16;
	
	public static final int HANDLER_PUBLISH_COMMENT = 17;
	public static final int HANDLER_PUBLISH_COMMENT_FAIL = 18;

	private Handler handler;

	public HuodongHttpController(Handler handler) {
		this.handler = handler;
	}

	/**
	 * 获取活动详情
	 * 
	 * @param activityId
	 */
	public void getHuodongDetail(int activityId) {
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("atid", String.valueOf(activityId)));
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));

		HttpRequest req = new HttpRequest(Constants.GET_AMUSEMENT_DETAIL, list, HttpRequest.Method.GET, reqDetailCallback);
		App.THREAD.execute(req);
	}

	private final DefaultHttpCallback reqDetailCallback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);

				JSONObject obj = new JSONObject(str);
//				activityMsg = new ActivityMessage(obj, ActivityMessage.FROM_HTTP);
				DetailActivityMessage bean = new DetailActivityMessage(obj);
				if (bean != null) {
					handler.sendMessage(handler.obtainMessage(HANDLER_GET_HUODONG_DETAIL, bean));
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				handler.sendMessage(handler.obtainMessage(HANDLER_GET_HUODONG_DETAIL, "获取活动失败"));
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_GET_HUODONG_DETAIL, "获取活动失败"));
		}
	});

	/**
	 * 打开或者关闭活动消息提醒
	 * 
	 * @param activityId
	 */
	public void CloseOrOpenHuodongMsgNotice(int activityId, boolean open) {
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("atid", String.valueOf(activityId)));
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		list.add(new BasicNameValuePair("isreceive", (open ? "1" : "0")));

		HttpRequest req = new HttpRequest(Constants.CLOSE_OR_OPEN_HUODONG_MESSAGET_NOTICE, list, HttpRequest.Method.POST, CloseOrOpenCallback);
		App.THREAD.execute(req);
	}
	
	private final DefaultHttpCallback  CloseOrOpenCallback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);

			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
//				handler.sendMessage(handler.obtainMessage(HANDLER_PINGFEN_FAIL, "评分失败"));
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
//			handler.sendMessage(handler.obtainMessage(HANDLER_PINGFEN_FAIL, "评分失败"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
//			handler.sendMessage(handler.obtainMessage(HANDLER_PINGFEN_FAIL, "评分失败"));
		}
	});

	/**
	 * 退出活动
	 * 
	 * @param activityId
	 */
	public void ExitHuodong(int activityId) {
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("atid", String.valueOf(activityId)));
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));

		HttpRequest req = new HttpRequest(Constants.EXIT_HUODONG, list, HttpRequest.Method.POST, CloseOrOpenCallback);
		App.THREAD.execute(req);
	}
	
	/**
	 * 设置电话号码
	 * @param phoneNum
	 */
	public void setPhoneNumber(String phoneNum) {
		final ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("phone", phoneNum));
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));

		final DefaultHttpCallback reqPhoneCallback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);
					JSONObject obj = new JSONObject(str);
					int code = obj.getInt("code");
					if (code == 0) {//成功
						handler.sendEmptyMessage(HANDLER_SET_PHONENUM_SUCCESS);
					}
					else if(code == 2){// 失败
						handler.sendEmptyMessage(HANDLER_SET_PHONENUM_FAIL);
					}

				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
			}

			@Override
			public void onException(ExceptionHttpResult result) {
			}
		});
		
		HttpRequest req = new HttpRequest(Constants.SET_PHONE_NUM, list, HttpRequest.Method.POST, reqPhoneCallback);
		App.THREAD.execute(req);
	}
	
	/**
	 * 对活动进行评分
	 * @param activityId
	 * @param pingfei
	 */
	public void setPingfen(int activityId, int pingfei) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		params.add(new BasicNameValuePair("atid", String.valueOf(activityId)));
		params.add(new BasicNameValuePair("pingfen", String.valueOf(pingfei)));
		
		HttpRequest req = new HttpRequest(Constants.SET_PING_FEN, params, HttpRequest.Method.POST, pingfenCallback);
		App.THREAD.execute(req);
	}
	
	private final DefaultHttpCallback pingfenCallback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);

				JSONObject obj = new JSONObject(str);
				//0 成功评分，1，入参错误，2，用户未报名，3，已经评过分了
				int status = obj.getInt("code");
				if (status == 0) {
					handler.sendEmptyMessage(HANDLER_PINGFEN_SUCCESS);
				}
				else {
					handler.sendMessage(handler.obtainMessage(HANDLER_PINGFEN_FAIL, obj.getString("msg")));
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				handler.sendMessage(handler.obtainMessage(HANDLER_PINGFEN_FAIL, "评分失败"));
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_PINGFEN_FAIL, "评分失败"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_PINGFEN_FAIL, "评分失败"));
		}
	});
	
	public void publishComment(int activityId, int priseType, String content) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		params.add(new BasicNameValuePair("atid", String.valueOf(activityId)));
		params.add(new BasicNameValuePair("type", String.valueOf(priseType)));
		params.add(new BasicNameValuePair("content", content));
		
		HttpRequest req = new HttpRequest(Constants.PUBLISH_ACTIVITY_COMMENTS, params, HttpRequest.Method.POST, publishCommentCallback);
		App.THREAD.execute(req);
	}
	
	private final DefaultHttpCallback publishCommentCallback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);

				JSONObject obj = new JSONObject(str);
				int status = obj.getInt("success");
				if (status == 1) {
					handler.sendEmptyMessage(HANDLER_PUBLISH_COMMENT);
				}
				else {
					handler.sendMessage(handler.obtainMessage(HANDLER_PUBLISH_COMMENT_FAIL, obj.getString("msg")));
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_PUBLISH_COMMENT_FAIL, "发布评论失败"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_PUBLISH_COMMENT_FAIL, "发布评论失败"));
		}
	});
	
	/**
	 * 判断是否评过分
	 * @param activityId
	 */
	public void checkNeedPingFen(int activityId) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		params.add(new BasicNameValuePair("atid", String.valueOf(activityId)));
		
		HttpRequest req = new HttpRequest(Constants.NEED_PING_FEN, params, HttpRequest.Method.POST, needPingfenCallback);
		App.THREAD.execute(req);
	}
	
	private final DefaultHttpCallback needPingfenCallback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);

				JSONObject obj = new JSONObject(str);
				// 0,未评分，1，入参错误，2，未报名，3，已评过了, 4.不可以评分
				int status = obj.getInt("code");
				if (status == 0) {
					handler.sendEmptyMessage(HANDLER_NEED_PING_FEN);
				}
				else {
					handler.sendMessage(handler.obtainMessage(HANDLER_NO_NEED_PING_FEN, obj.getString("msg")));
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				handler.sendMessage(handler.obtainMessage(HANDLER_NO_NEED_PING_FEN, "评分失败"));
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_NO_NEED_PING_FEN, "评分失败"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_NO_NEED_PING_FEN, "评分失败"));
		}
	});
}

package com.oumen.circle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.peers.Prise;
import com.oumen.android.peers.entity.Comment;
import com.oumen.android.peers.entity.CircleUserMsg;
import com.oumen.android.util.Constants;
import com.oumen.circle.CircleController.CircleListAdapter;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;
import com.oumen.widget.list.HSZListViewAdapter;
/**
 * 偶们圈所有联网请求
 */
public class CircleHttpController {
	public static final int HANDLER_OBTAIN_LIST = 0;
	public static final int HANDLER_DELETE_CONTENT = 1;
	public static final int HANDLER_DELETE_COMMENT = 2;
	public static final int HANDLER_SEND_CONTENT = 3;
	public static final int HANDLER_SEND_COMMENT = 4;
	public static final int HANDLER_ENJOY = 5;
	public static final int HANDLER_UNENJOY = 6;
	
	public enum ObtainType {HEADER, FOOTER}
	protected ObtainType obtainType = ObtainType.FOOTER;
	
	public static class CircleDataWrapper {
		ObtainType obtainType;
		List<CircleUserMsg> data;
		boolean http;
		
		CircleDataWrapper(ObtainType obtainType, boolean cache, List<CircleUserMsg> data) {
			this.obtainType = obtainType;
			this.http = cache;
			this.data = data;
		}
	}
	
	private final Handler handler;

	public CircleHttpController(Handler handler) {
		this.handler = handler;
	}

	// TODO Obtain List
	/**
	 * 获取偶们圈内容
	 * @param obtainType
	 * @param adapter
	 * @return
	 */
	public HttpRequest obtainList(final ObtainType obtainType, CircleListAdapter adapter) {
		this.obtainType = obtainType;
		
		int selfUid = App.PREFS.getUid();
		if (obtainType == null) {
			LinkedList<CircleUserMsg> tmpList = new LinkedList<CircleUserMsg>();
			List<SimpleCircle> cache = SimpleCircle.query(selfUid, App.DB);
			for (SimpleCircle i : cache) {
				try {
					CircleUserMsg obj = new CircleUserMsg(i.reference);
					tmpList.add(obj);
				}
				catch (JSONException e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
			}
			handler.sendMessage(handler.obtainMessage(HANDLER_OBTAIN_LIST, 0, 1, new CircleDataWrapper(obtainType, false, tmpList)));
		}
		
		HttpRequest req = obtainListByHttp(obtainType, adapter);
		return req;
	}
	
	/**
	 * 获取偶们圈内容http请求
	 * @param obtainType
	 * @param adapter
	 * @return
	 */
	public HttpRequest obtainListByHttp(final ObtainType obtainType, CircleListAdapter adapter) {
		ELog.i("");
		this.obtainType = obtainType;
		
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			//网络不给力
			handler.sendMessage(handler.obtainMessage(App.HANDLER_TOAST, R.string.err_network_invalid, 0));
			return null;
		}
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		if (ObtainType.HEADER.equals(obtainType) && !adapter.isEmpty()) {
			params.add(new BasicNameValuePair("prev", String.valueOf(adapter.get(0).getInfo().getTime())));
		}
		else if (ObtainType.FOOTER.equals(obtainType) && !adapter.isEmpty()) {
			params.add(new BasicNameValuePair("after", String.valueOf(adapter.get(adapter.getCount() - 1).getInfo().getTime())));
		}
		
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);
					
					JSONObject object = new JSONObject(str);
					JSONArray resultArray = object.getJSONArray("data");

					LinkedList<CircleUserMsg> tmpList = new LinkedList<CircleUserMsg>();
					
					int selfUid = App.PREFS.getUid();
					
					if (!ObtainType.FOOTER.equals(obtainType)) {
						SimpleCircle.deleteAll(selfUid, App.DB);
					}

					for (int i = 0; i < resultArray.length(); i++) {
						JSONObject jsonCircle = resultArray.getJSONObject(i);
						CircleUserMsg peer = new CircleUserMsg(jsonCircle);
						tmpList.add(peer);
						
						if (i < 10 && !ObtainType.FOOTER.equals(obtainType)) {
							SimpleCircle simple = new SimpleCircle(jsonCircle);
							SimpleCircle.update(selfUid, simple, App.DB);
						}
					}
					handler.sendMessage(handler.obtainMessage(HANDLER_OBTAIN_LIST, 0, 1, new CircleDataWrapper(obtainType, true, tmpList)));
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_OBTAIN_LIST, R.string.circle_obtain_list_failed, 0));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
				handler.sendEmptyMessage(HANDLER_OBTAIN_LIST);
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_OBTAIN_LIST, R.string.circle_obtain_list_failed, 0));
			}
		});
		
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_GETCONTENT, params, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
		return req;
	}
	
	
	/**
	 * 爸爸在这儿列表获取
	 * @param obtainType
	 * @param adapter
	 * @return
	 */
	public HttpRequest obtainDadayIsHereListByHttp(final ObtainType obtainType, final CircleListAdapter adapter) {
		this.obtainType = obtainType;
		
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			//网络不给力
			handler.sendMessage(handler.obtainMessage(App.HANDLER_TOAST, R.string.err_network_invalid, 0));
			return null;
		}
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		params.add(new BasicNameValuePair("modes", String.valueOf(4)));// TODO 比偶们圈多的参数
		if (ObtainType.HEADER.equals(obtainType) && !adapter.isEmpty()) {
			params.add(new BasicNameValuePair("prev", String.valueOf(adapter.get(0).getInfo().getTime())));
		}
		else if (ObtainType.FOOTER.equals(obtainType) && !adapter.isEmpty()) {
			params.add(new BasicNameValuePair("after", String.valueOf(adapter.get(adapter.getCount() - 1).getInfo().getTime())));
		}
		
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);
					
					JSONObject object = new JSONObject(str);
					JSONArray resultArray = object.getJSONArray("data");

					LinkedList<CircleUserMsg> tmpList = new LinkedList<CircleUserMsg>();
					
//					int selfUid = App.PREFS.getUid();
					
//					if (!ObtainType.FOOTER.equals(obtainType)) {
//						SimpleCircle.deleteAll(selfUid, App.DB);
//					}

					for (int i = 0; i < resultArray.length(); i++) {
						JSONObject jsonCircle = resultArray.getJSONObject(i);
						CircleUserMsg peer = new CircleUserMsg(jsonCircle);
						tmpList.add(peer);
						
//						if (i < 10 && !ObtainType.FOOTER.equals(obtainType)) {
//							SimpleCircle simple = new SimpleCircle(jsonCircle);
//							SimpleCircle.update(selfUid, simple, App.DB);
//						}
					}

					synchronized (adapter) {
						if (ObtainType.FOOTER.equals(obtainType)) {
							adapter.addAll(tmpList);
						}
						else {
							adapter.clear();
							adapter.addAll(tmpList);
						}
					}

					handler.sendMessage(handler.obtainMessage(HANDLER_OBTAIN_LIST, 0, 1));
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_OBTAIN_LIST, R.string.circle_obtain_list_failed, 0));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
				handler.sendEmptyMessage(HANDLER_OBTAIN_LIST);
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_OBTAIN_LIST, R.string.circle_obtain_list_failed, 0));
			}
		});
		
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_GETCONTENT, params, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
		return req;
	}
	
	// TODO Enjoy
	public HttpRequest enjoy(final CircleItemData data) {
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			//网络不给力
			handler.sendMessage(handler.obtainMessage(App.HANDLER_TOAST, R.string.err_network_invalid, 0));
			return null;
		}

		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("cnid", String.valueOf(data.groupData.getInfo().getCircleId())));
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));

		DefaultHttpCallback callback = new DefaultHttpCallback(new EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String response = result.getResult();
					ELog.i(response);
					if ("1".equals(response)) {
						data.groupData.setIsprise(1);
						Prise prise = new Prise();
						prise.setPriseName(App.USER.getNickname());
						prise.setPriseUid(Integer.valueOf(App.USER.getUid()));
						data.groupData.prises.add(0, prise);//TODO 将新的数据插入到最前面
						
						handler.sendEmptyMessage(HANDLER_ENJOY);
					}
					else if (response.contains("tip")) {
						handler.sendMessage(handler.obtainMessage(HANDLER_ENJOY, new JSONObject(response).get("tip")));
					}
					else {
						handler.sendMessage(handler.obtainMessage(HANDLER_ENJOY, R.string.circle_enjoy_failed, 0));
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_ENJOY, R.string.circle_enjoy_failed, 0));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_ENJOY, R.string.circle_enjoy_failed, 0));
			}
		});
		
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_FAVOUR, list, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
		return req;
	}
	
	// TODO Unenjoy
	public HttpRequest unenjoy(final CircleItemData data) {
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			//网络不给力
			handler.sendMessage(handler.obtainMessage(App.HANDLER_TOAST, R.string.err_network_invalid, 0));
			return null;
		}

		DefaultHttpCallback callback = new DefaultHttpCallback(new EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String response = result.getResult();
					ELog.i(response);
					
					if ("1".equals(response)) {
						data.groupData.setIsprise(0);
//						data.groupData.prises.remove(data.groupData.prises.size() - 1);
						Prise prise = new Prise();
						prise.setPriseName(App.USER.getNickname());
						prise.setPriseUid(Integer.valueOf(App.USER.getUid()));
						data.groupData.prises.remove(prise);//TODO 假如以前已经赞过，位置就可能不在第一个
						
						handler.sendEmptyMessage(HANDLER_UNENJOY);
					}
					else if (response.contains("tip")) {
						handler.sendMessage(handler.obtainMessage(HANDLER_UNENJOY, new JSONObject(response).get("tip")));
					}
					else {
						handler.sendMessage(handler.obtainMessage(HANDLER_UNENJOY, R.string.circle_unenjoy_failed, 0));
					}
				}

				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_UNENJOY, R.string.circle_unenjoy_failed, 0));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_UNENJOY, R.string.circle_unenjoy_failed, 0));
			}
		});

		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("cnid", String.valueOf(data.groupData.getInfo().getCircleId())));
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));

		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_FAVOUR_CANCEL, list, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
		
		return req;
	}

	// TODO Send Content
	public HttpRequest sendContent(final CircleItemData data, final String content) {
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			//网络不给力
			handler.sendMessage(handler.obtainMessage(App.HANDLER_TOAST, R.string.err_network_invalid, 0));
			return null;
		}
		
		DefaultHttpCallback callback = new DefaultHttpCallback(new EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String response = result.getResult();
					ELog.i(response);
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_SEND_CONTENT, R.string.circle_send_content_failed, 0));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_SEND_CONTENT, R.string.circle_send_content_failed, 0));
			}
		});
		
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		if (data.commentData == null) {
			list.add(new BasicNameValuePair("cnid", String.valueOf(data.groupData.getInfo().getCircleId())));
			list.add(new BasicNameValuePair("oruid", ""));
		}
		else {
			list.add(new BasicNameValuePair("cnid", String.valueOf(data.commentData.getCircleId())));
			list.add(new BasicNameValuePair("oruid", String.valueOf(data.commentData.getAuthorId())));
		}
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		list.add(new BasicNameValuePair("content", content));
		
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_WRITECOMMENT, list, HttpRequest.Method.POST, callback);
		App.THREAD.execute(req);
		return req;
	}

	// TODO Send Comment
	public HttpRequest sendComment(final CircleItemData data, final String content) {
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			//网络不给力
			handler.sendMessage(handler.obtainMessage(App.HANDLER_TOAST, R.string.err_network_invalid, 0));
			return null;
		}
		
		DefaultHttpCallback callback = new DefaultHttpCallback(new EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String response = result.getResult();
					ELog.i(response);

					if (response.contains("tip")) {
						JSONObject obj = new JSONObject(response);
						handler.sendMessage(handler.obtainMessage(HANDLER_SEND_CONTENT, obj.getString("tip")));
					}
					else if (Integer.parseInt(response) > 0) {
						Comment cmt = new Comment();
						cmt.setId(Integer.parseInt(response));
						cmt.setAuthorId(App.PREFS.getUid());
						cmt.setAuthorName(App.USER.getNickname());
						cmt.setContent(content);
						if (data.commentData == null) {
							cmt.setCircleId(data.groupData.getInfo().getCircleId());
						}
						else {
							cmt.setCircleId(data.groupData.getInfo().getCircleId());
							cmt.setTargetId(data.commentData.getAuthorId());
							cmt.setTargetName(data.commentData.getAuthorName());
						}
						data.groupData.comments.add(cmt);

						handler.sendEmptyMessage(HANDLER_SEND_COMMENT);
					}
					else {
						handler.sendMessage(handler.obtainMessage(HANDLER_SEND_COMMENT, R.string.circle_send_comment_failed, 0));
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_SEND_COMMENT, R.string.circle_send_comment_failed, 0));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_SEND_COMMENT, R.string.circle_send_comment_failed, 0));
			}
		});
		
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		if (data.commentData == null) {
			list.add(new BasicNameValuePair("cnid", String.valueOf(data.groupData.getInfo().getCircleId())));
			list.add(new BasicNameValuePair("oruid", ""));
		}
		else {
			list.add(new BasicNameValuePair("cnid", String.valueOf(data.commentData.getCircleId())));
			list.add(new BasicNameValuePair("oruid", String.valueOf(data.commentData.getAuthorId())));
		}
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		list.add(new BasicNameValuePair("content", content));
		
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_WRITECOMMENT, list, HttpRequest.Method.POST, callback);
		App.THREAD.execute(req);
		return req;
	}
	
	// TODO Delete Comment
	public HttpRequest deleteComment(final CircleItemData data) {
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			//网络不给力
			handler.sendMessage(handler.obtainMessage(App.HANDLER_TOAST, R.string.err_network_invalid, 0));
			return null;
		}
		
		DefaultHttpCallback callback = new DefaultHttpCallback(new EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String response = result.getResult();
					ELog.i(response);
					if (response.contains("tip")) {
						JSONObject obj = new JSONObject(response);
						handler.sendMessage(handler.obtainMessage(HANDLER_DELETE_COMMENT, obj.getString("tip")));
					}
					else {
						if ("1".equals(response)) {
							data.groupData.comments.remove(data.commentData);
							handler.sendEmptyMessage(HANDLER_DELETE_COMMENT);
						}
						else {
							handler.sendMessage(handler.obtainMessage(HANDLER_DELETE_COMMENT, R.string.circle_delete_comment_failed, 0));
						}
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_DELETE_COMMENT, R.string.circle_delete_comment_failed, 0));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_DELETE_COMMENT, R.string.circle_delete_comment_failed, 0));
			}
		});
		
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("commid", String.valueOf(data.commentData.getId())));
		list.add(new BasicNameValuePair("cnid", String.valueOf(data.commentData.getCircleId())));
		
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_DELETECOMMENT, list, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
		return req;
	}
	
	// TODO Delete Content
	public HttpRequest deleteContent(final CircleItemData data, final HSZListViewAdapter<CircleUserMsg> adapter) {
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			//网络不给力
			handler.sendMessage(handler.obtainMessage(App.HANDLER_TOAST, R.string.err_network_invalid, 0));
			return null;
		}
		
		DefaultHttpCallback callback = new DefaultHttpCallback(new EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String response = result.getResult();
					ELog.i(response);
					if (response.contains("tip")) {
						JSONObject obj = new JSONObject(response);
						handler.sendMessage(handler.obtainMessage(HANDLER_DELETE_CONTENT, obj.getString("tip")));
					}
					else {
						if ("1".equals(response)) {
							adapter.remove(data.groupData);
							handler.sendEmptyMessage(HANDLER_DELETE_CONTENT);
						}
						else {
							handler.sendMessage(handler.obtainMessage(HANDLER_DELETE_CONTENT, R.string.circle_delete_content_failed, 0));
						}
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_DELETE_CONTENT, R.string.circle_delete_content_failed, 0));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_DELETE_CONTENT, R.string.circle_delete_content_failed, 0));
			}
		});
		
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("cnid", String.valueOf(data.groupData.getInfo().getCircleId())));
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_DELETECONTENT, list, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
		return req;
	}
}

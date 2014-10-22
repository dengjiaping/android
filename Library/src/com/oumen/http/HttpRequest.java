package com.oumen.http;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;

import com.oumen.tools.ELog;

public class HttpRequest implements Runnable {
	public static final int TIME_SHORT = 15 * 1000;
	public static final int TIME_LONG = 5 * 60 * 1000;
	public static int timeout = TIME_SHORT;

	public enum Method {
		GET, POST
	};

	private String uri;

	private List<NameValuePair> params;

	private NameValuePair data;

	private Map<String, String> headers;

	private Method method;

	private HttpUriRequest req;

	private HttpCallback callback;

	private boolean isForceClose;

	public HttpRequest(String uri, List<NameValuePair> params, Method method, HttpCallback callback) {
		this(uri, params, null, null, method, callback);
	}

	public HttpRequest(String uri, List<NameValuePair> params, NameValuePair data, Map<String, String> headers, Method method, HttpCallback callback) {
		this.uri = uri;
		this.params = params;
		this.data = data;
		this.headers = headers;
		this.method = method;
		this.callback = callback;
	}

	private void buildRequest() {
		String url;

		if (method == Method.GET) {
			if (params == null) {
				url = uri;
				url += url.indexOf('?')  == -1 ? "?os=android" : "os=android";
			}
			else {
				StringBuilder buf = new StringBuilder();
				for (NameValuePair i : params) {
					HttpHelper.addParma(buf, i.getName(), i.getValue());
				}
				HttpHelper.addParma(buf, "os", "android");

				url = uri + "?" + buf.toString();
			}
			ELog.i("GET:" + url);

			HttpGet get = new HttpGet(url);
			req = get;
		}
		else if (method == Method.POST) {
			HttpPost post = new HttpPost(uri);
			try {
				if (data == null) {
					post.setHeader("Content-Type", "application/x-www-form-urlencoded");
					if (params == null) {
						url = uri;
					}
					else {
						StringBuilder buf = new StringBuilder();
						for (NameValuePair i : params) {
							HttpHelper.addParma(buf, i.getName(), i.getValue());
						}
						HttpHelper.addParma(buf, "os", "android");
						post.setEntity(new StringEntity(buf.toString(), HTTP.UTF_8));

						url = post.getURI().toString() + "?" + buf.toString();
					}
					ELog.i("POST:" + url);
				}
				else {
					MultipartEntity entity = new MultipartEntity();
					if (params == null) {
						url = uri;
					}
					else {
						StringBuilder buf = new StringBuilder();
						for (NameValuePair i : params) {
							StringBody body = new StringBody(i.getValue(), Charset.forName(HTTP.UTF_8));
							entity.addPart(i.getName(), body);
							HttpHelper.addParma(buf, i.getName(), i.getValue());
						}
						HttpHelper.addParma(buf, "os", "android");

						url = post.getURI().toString() + "?" + buf.toString();
					}
					ELog.i("POST:" + url);

					File f = new File(data.getValue());
					FileBody body = new FileBody(f, "application/octet-stream");
					entity.addPart(data.getName(), body);

					post.setEntity(entity);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			req = post;
		}

		if (req != null && headers != null) {
			setHeader(req);
		}

		req.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
		req.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
	}

	public void setHeader(HttpUriRequest request) {
		Set<String> keys = headers.keySet();
		for (String key : keys) {
			request.setHeader(key, headers.get(key));
		}
	}

	public Method getMethod() {
		return method;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public NameValuePair getPic() {
		return data;
	}

	public List<NameValuePair> getParams() {
		return params;
	}

	public String getUri() {
		return uri;
	}

	public void close() {
		isForceClose = true;
		if (req != null) {
			req.abort();
			req = null;
		}
	}

	public HttpResult connect() {
		HttpResponse response = null;
		HttpResult result;
		try {
			buildRequest();
			response = HttpHelper.CLIENT.execute(req);
			result = new HttpResult(response);
		}
		catch (Exception e) {
			result = new ExceptionHttpResult(response, e, isForceClose);
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void run() {
		if (callback == null) {
			connect();
		}
		else {
			callback.setBusy(true);
			HttpResult result = connect();
			callback.setBusy(false);
			callback.onEvent(result);
		}
	}
}
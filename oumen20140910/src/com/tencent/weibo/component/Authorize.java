package com.tencent.weibo.component;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.tools.ELog;
import com.tencent.weibo.sdk.android.api.util.BackGroudSeletor;
import com.tencent.weibo.sdk.android.api.util.Util;

/**
 * 用户授权组件
 * 
 */
public class Authorize extends Activity {
	public static final int REQUEST_CODE = 999998;
	public static final String INTENT_DATA_KEY = "data";

	WebView webView;
	String _url;
	String _fileName;
	public static int WEBVIEWSTATE_1 = 0;
	int webview_state = 0;
	String path;
	Dialog _dialog;
	public static final int ALERT_DOWNLOAD = 0;
	public static final int ALERT_FAV = 1;
	public static final int PROGRESS_H = 3;
	public static final int ALERT_NETWORK = 4;
	private ProgressDialog dialog;
	private LinearLayout layout = null;
	private String redirectUri = null;
	private String clientId = null;
	private boolean isShow = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!Util.isNetworkAvailable(this)) {
			Authorize.this.showDialog(ALERT_NETWORK);
		}
		else {
			DisplayMetrics displaysMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
			String pix = displaysMetrics.widthPixels + "x" + displaysMetrics.heightPixels;
			BackGroudSeletor.setPix(pix);

			try {
				// Bundle bundle = getIntent().getExtras();
				clientId = Util.getConfig().getProperty("APP_KEY");// bundle.getString("APP_KEY");
				redirectUri = Util.getConfig().getProperty("REDIRECT_URI");// bundle.getString("REDIRECT_URI");
				if (clientId == null || "".equals(clientId) || redirectUri == null || "".equals(redirectUri)) {
					Toast.makeText(Authorize.this, "请在配置文件中填写相应的信息", Toast.LENGTH_SHORT).show();
				}
				ELog.i(redirectUri);
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
				requestWindowFeature(Window.FEATURE_NO_TITLE);
				int state = (int) Math.random() * 1000 + 111;
				path = "https://open.t.qq.com/cgi-bin/oauth2/authorize?client_id=" + clientId + "&response_type=token&redirect_uri=" + redirectUri + "&state=" + state;
				this.initLayout();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 初始化界面使用控件，并设置相应监听
	 * */
	public void initLayout() {
		RelativeLayout.LayoutParams fillParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		RelativeLayout.LayoutParams fillWrapParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams wrapParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setMessage("请稍后...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(false);
		dialog.show();

		layout = new LinearLayout(this);
		layout.setLayoutParams(fillParams);
		layout.setOrientation(LinearLayout.VERTICAL);

		RelativeLayout cannelLayout = new RelativeLayout(this);
		cannelLayout.setLayoutParams(fillWrapParams);
		cannelLayout.setBackground(BackGroudSeletor.getdrawble("up_bg2x", getApplication()));
		cannelLayout.setGravity(LinearLayout.HORIZONTAL);

		Button returnBtn = new Button(this);
		String[] pngArray = { "quxiao_btn2x", "quxiao_btn_hover" };
		returnBtn.setBackground(BackGroudSeletor.createBgByImageIds(pngArray, getApplication()));
		returnBtn.setText("取消");
		wrapParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		wrapParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		wrapParams.leftMargin = 10;
		wrapParams.topMargin = 10;
		wrapParams.bottomMargin = 10;

		returnBtn.setLayoutParams(wrapParams);
		returnBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				Authorize.this.finish();
			}
		});
		cannelLayout.addView(returnBtn);

		TextView title = new TextView(this);
		title.setText("授权");
		title.setTextColor(Color.WHITE);
		title.setTextSize(24f);
		RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		titleParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		title.setLayoutParams(titleParams);
		cannelLayout.addView(title);

		layout.addView(cannelLayout);

		webView = new WebView(this);

		// Build.VERSION_CODES.HONEYCOMB = 11
		if (Build.VERSION.SDK_INT >= 11) {
			Class<?>[] name = new Class[] { String.class };
			Object[] rmMethodName = new Object[] { "searchBoxJavaBridge_" };
			Method rji;
			try {
				rji = webView.getClass().getDeclaredMethod("removeJavascriptInterface", name);
				rji.invoke(webView, rmMethodName);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		LinearLayout.LayoutParams wvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		webView.setLayoutParams(wvParams);
		WebSettings webSettings = webView.getSettings();
		webView.setVerticalScrollBarEnabled(false);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(false);
		webView.loadUrl(path);
		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
			}

		});
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
//				ELog.i(url);
				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();
				}
				if (url.indexOf("access_token") != -1 && !isShow) {
					isShow = true;
					Intent data = new Intent();
					data.putExtra(INTENT_DATA_KEY, url);
					setResult(Activity.RESULT_OK, data);
					finish();
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.indexOf("access_token") != -1 && !isShow) {
					isShow = true;
					Intent data = new Intent();
					data.putExtra(INTENT_DATA_KEY, url);
					setResult(Activity.RESULT_OK, data);
					finish();
				}
				return false;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
//				ELog.i(url);
				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();
				}
				if (url.indexOf("access_token") != -1 && !isShow) {
					isShow = true;
					Intent data = new Intent();
					data.putExtra(INTENT_DATA_KEY, url);
					setResult(Activity.RESULT_OK, data);
					finish();
				}
			}
		});
		layout.addView(webView);
		this.setContentView(layout);
	}

	Handler handle = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case 100:
					Authorize.this.showDialog(ALERT_NETWORK);
					break;
			}
			return false;
		}
	});

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case PROGRESS_H:
				_dialog = new ProgressDialog(this);
				((ProgressDialog) _dialog).setMessage("加载中...");
				break;
				
			case ALERT_NETWORK:
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setTitle("网络连接异常，是否重新连接？");
				builder2.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (Util.isNetworkAvailable(Authorize.this)) {
							webView.loadUrl(path);
						}
						else {
							Message msg = Message.obtain();
							msg.what = 100;
							handle.sendMessage(msg);
						}
					}

				});
				builder2.setNegativeButton("否", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Authorize.this.finish();
					}
				});
				_dialog = builder2.create();
				break;
		}
		return _dialog;
	}

}

package com.oumen.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.auth.AuthAdapter.MessageType;
import com.oumen.auth.ShareData;
import com.oumen.auth.ShareView;
import com.oumen.home.FloatViewController;
import com.oumen.home.FloatViewHostController;
import com.oumen.home.SoftKeyboardController;
import com.oumen.tools.ELog;

public class ActivityWebViewActivity extends BaseActivity implements FloatViewHostController, View.OnTouchListener {
	public static final String INTENT_WEBVIEW_ACTIVITY_URL = "webview_url";
	public static final String INTENT_WEBVIEW_SHARE_MSG = "webview_share_message";
	private static final int HANDLER_TYPE_FAIL = 1;
	private final int FILECHOOSER_RESULTCODE = 1;
	// 标题行的三个控件
	private TitleBar titlebar;
	private Button btnLeft;
	private Button btnRight;
	private WebView webView;
	private ProgressBar loadProgress;

	private String webUrl;
	private String shareJsonStr;
	private final ArrayList<WebShareData> sharedata = new ArrayList<WebShareData>();
	// 分享的popwindow
	private ShareView viewShare;

	private RelativeLayout rootContainer;
	private View popupLayer;

	private WebShareData currenShare = null;

	private ValueCallback<Uri> mUploadMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.amusement_webview);
		
		init();
		viewShare = new ShareView(ActivityWebViewActivity.this);
		viewShare.setHost(ActivityWebViewActivity.this);
		initAnimation();

		webUrl = getIntent().getStringExtra(INTENT_WEBVIEW_ACTIVITY_URL);
		shareJsonStr = getIntent().getStringExtra(INTENT_WEBVIEW_SHARE_MSG);
		try {
			initData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() {
		rootContainer = (RelativeLayout) findViewById(R.id.rootview);
		popupLayer = findViewById(R.id.layer);

		titlebar = (TitleBar) findViewById(R.id.titlebar);
//		titlebar.setBackgroundTransparent();
		btnLeft = titlebar.getLeftButton();
		btnRight = titlebar.getRightButton();
		btnRight.setText(R.string.share);
		
		loadProgress = (ProgressBar) findViewById(R.id.progress);
		webView = (WebView) findViewById(R.id.webview_activity);
		
		webView.getSettings().setJavaScriptEnabled(true);
		
//		webView.getSettings().setBuiltInZoomControls(true);
//		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webView.getSettings().setAllowFileAccess(true);
		webView.clearCache(true);

		btnLeft.setOnClickListener(clickListener);
		btnRight.setOnClickListener(clickListener);

		webView.setDownloadListener(new DownloadListener() {

			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		webView.setWebChromeClient(new MyWebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				if (newProgress == 100) {
					loadProgress.setVisibility(View.GONE);
				}
			}
		});
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) { 
				ELog.i(url);
				view.loadUrl(url);
				CheckShareInfo(url);
				return true;
			}
		});
	}

	/**
	 * 查询是否可以分享
	 * 
	 * @param url
	 */
	private void CheckShareInfo(String url) {
		if (webUrl != null) {
			boolean flag = false;
			for (int i = 0; i < sharedata.size(); i++) {
				WebShareData share = sharedata.get(i);
				if (share.url.equals(url)) {// 判断url是否相同
					flag = true;
					currenShare = sharedata.get(i);
				}
			}
			if (flag) {
				btnRight.setVisibility(View.VISIBLE);
			}
			else {
				btnRight.setVisibility(View.GONE);
			}
		}
	}

	private void initData() throws Exception {
		//将jsonArray 转成数组
		if (shareJsonStr != null) {
			JSONArray jsonArray = new JSONArray(shareJsonStr);
			for (int i = 0; i < jsonArray.length(); i++) {
				WebShareData share = new WebShareData(jsonArray.getJSONObject(i));
				sharedata.add(share);
			}
		}
		if (webUrl != null) {
			webView.loadUrl(webUrl);
		}
		CheckShareInfo(webUrl);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
//			webView.goBack();// 返回前一个页面
//			return true;
//		}
//		else {
		finish();
//		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 监控文件选择
	 * 
	 * @author Administrator
	 * 
	 */
	class MyWebChromeClient extends WebChromeClient {
		// The undocumented magic method override
		// Eclipse will swear at you if you try to put @Override here
		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
			mUploadMessage = uploadMsg;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("*/*");
			ActivityWebViewActivity.this.startActivityForResult(Intent.createChooser(i, "file Browser"), FILECHOOSER_RESULTCODE);
		}

		public void openFileChooser(ValueCallback<Uri> uploadMsg) {

			mUploadMessage = uploadMsg;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("*/*");
			ActivityWebViewActivity.this.startActivityForResult(Intent.createChooser(i, "file Browser"), FILECHOOSER_RESULTCODE);
		}

		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
			mUploadMessage = uploadMsg;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("*/*");
			ActivityWebViewActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
		}
	}

	/**
	 * 返回文件选择
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if (null == mUploadMessage)
				return;
			Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;

		}
		//TODO 
		if (viewShare != null) {
			viewShare.onActivityResult(requestCode, resultCode, intent);
		}
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.left:
					setResult(Activity.RESULT_CANCELED);
					finish();
					break;
				case R.id.right:
					//TODO 
					viewShare.setShareData(currenShare);
					showFloatView(viewShare);
					break;
			}
		}
	};

	public boolean handleMessage(android.os.Message msg) {
		switch (msg.what) {
			case HANDLER_TYPE_FAIL:
				if (msg.obj != null) {
					Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
				}
				break;
		}
		return false;
	}

	/**
	 * 
	 * 每个链接对应的分享信息
	 * 
	 */
	public class WebShareData implements ShareData {

		String url;//分享链接，用于进行判断
		String picUrl;//分享时的图片
		String title;//分享时的标题
		String content;//分享时的内容

		public WebShareData() {
		}

		public WebShareData(JSONObject obj) throws Exception {

			url = obj.getString("url");
			picUrl = obj.getString("pic");
			content = obj.getString("dis");
			title = obj.getString("title");
		}

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
			return title;
		}

		@Override
		public String getShareContent() {
			return content;
		}

		@Override
		public String getShareLinkUrl() {
			return url;
		}

		@Override
		public String getShareImageUrl() {
			return picUrl;
		}
	}

	// ---------- Animation ----------//
	private FloatViewController floatViewController;
	private Animation animBottomIn;
	private Animation animBottomOut;

	private void initAnimation() {
		animBottomIn = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_in);
		animBottomIn.setAnimationListener(animListener);
		animBottomOut = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_out);
		animBottomOut.setAnimationListener(animListener);
	}

	private final Animation.AnimationListener animListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			floatViewController.setPlaying(false);
			if (animation == animBottomOut) {
				rootContainer.removeView(floatViewController.getRoot());
				floatViewController = null;

				popupLayer.setVisibility(View.GONE);
				popupLayer.setOnTouchListener(null);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	};

	@Override
	public Activity getActivity() {
		return ActivityWebViewActivity.this;
	}

	@Override
	public boolean isFloatViewShowing() {
		return floatViewController != null && (floatViewController.isPlaying() || floatViewController.isShowing());
	}

	@Override
	public void showFloatView(FloatViewController controller) {
		if (isFloatViewShowing())
			return;

		floatViewController = controller;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		View container = floatViewController.show();
		rootContainer.addView(container, params);
		container.startAnimation(animBottomIn);

		if (controller instanceof SoftKeyboardController) {
			SoftKeyboardController kb = (SoftKeyboardController) controller;
			kb.showSoftKeyboard();
		}

		popupLayer.setVisibility(View.VISIBLE);
		popupLayer.setOnTouchListener(this);
	}

	@Override
	public void hideFloatView() {
		if (!isFloatViewShowing())
			return;

		View container = floatViewController.hide();
		container.startAnimation(animBottomOut);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == popupLayer && event.getAction() == MotionEvent.ACTION_UP) {
			if (floatViewController != null && !floatViewController.isPlaying() && floatViewController.isShowing()) {
				hideFloatView();
			}
		}
		return true;
	};

}

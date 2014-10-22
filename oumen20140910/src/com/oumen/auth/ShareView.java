package com.oumen.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.home.FloatViewController;
import com.oumen.home.FloatViewHostController;
import com.oumen.tools.ELog;

public class ShareView extends FrameLayout implements FloatViewController, View.OnClickListener {
	protected boolean showing;
	protected boolean playing;

	protected FloatViewHostController host;

	protected View popupShareContainer;
	protected ImageView ivShareWeixinCircle;
	protected ImageView ivShareSina;
	protected ImageView ivShareWeixinFriend;
	protected ImageView ivShareQQ;
	protected Button btnCancel;

	protected Activity activity;
	protected AuthAdapter share;
	protected ShareData currentData;

	public ShareView(Context context) {
		this(context, null, 0);
	}

	public ShareView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ShareView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popupShareContainer = inflater.inflate(R.layout.popup_group_item_more, null);

		ivShareWeixinCircle = (ImageView) popupShareContainer.findViewById(R.id.iv_share_weixin_circle);
		ivShareWeixinCircle.setOnClickListener(this);

		ivShareWeixinFriend = (ImageView) popupShareContainer.findViewById(R.id.iv_share_weixin_friend);
		ivShareWeixinFriend.setOnClickListener(this);

		ivShareSina = (ImageView) popupShareContainer.findViewById(R.id.iv_share_sina);
		ivShareSina.setOnClickListener(this);

		ivShareQQ = (ImageView) popupShareContainer.findViewById(R.id.iv_share_qq);
		ivShareQQ.setOnClickListener(this);

		btnCancel = (Button) popupShareContainer.findViewById(R.id.cancel);
		btnCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		ShareData data = (ShareData) v.getTag();
		switch (v.getId()) {
			case R.id.iv_share_weixin_circle:
				share = AuthAdapter.create(null, AuthAdapter.Type.WEIXIN);
				share.shareInit(host.getActivity());
				share.share(data.getShareType(), WeixinAuthAdapter.SHARE_WEIXIN_CIRCLE, data.getShareTitle(), data.getShareContent(), data.getShareLinkUrl(), data.getShareImageUrl());
				host.hideFloatView();
				break;

			case R.id.iv_share_sina:
				share = AuthAdapter.create(authListener, AuthAdapter.Type.SINA_WEIBO);
				if (SinaAuthAdapter.isAuthor()) {
					share.shareInit(host.getActivity());
					share.share(data.getShareType(), data.getActionType(), data.getShareTitle(), data.getShareContent(), data.getShareLinkUrl(), data.getShareImageUrl());
				}
				else {
					currentData = data;
					share.authorize(host.getActivity());
				}
				host.hideFloatView();
				break;

			case R.id.iv_share_weixin_friend:
				share = AuthAdapter.create(null, AuthAdapter.Type.WEIXIN);
				share.shareInit(host.getActivity());
				share.share(data.getShareType(), WeixinAuthAdapter.SHARE_WEIXIN_FRIEND, data.getShareTitle(), data.getShareContent(), data.getShareLinkUrl(), data.getShareImageUrl());
				host.hideFloatView();
				break;

			case R.id.iv_share_qq:
				share = AuthAdapter.create(authListener, AuthAdapter.Type.QQ);
				if (QqAuthAdapter.isAuthor()) {
					// 已经授权，向服务器发送请求
					share.shareInit(host.getActivity());
					share.share(data.getShareType(), data.getActionType(), data.getShareTitle(), data.getShareContent(), data.getShareLinkUrl(), data.getShareImageUrl());
				}
				else {
					currentData = data;
					share.authorize(host.getActivity());
				}
				host.hideFloatView();
				break;

			case R.id.cancel:
				host.hideFloatView();
				break;
		}
	}

	public void setShareData(ShareData data) {
		ivShareWeixinCircle.setTag(data);
		ivShareWeixinFriend.setTag(data);
		ivShareSina.setTag(data);
		ivShareQQ.setTag(data);
	}

	//---------------------- FloatViewController ----------------------//

	@Override
	public boolean isShowing() {
		return showing;
	}

	@Override
	public boolean isPlaying() {
		return playing;
	}

	@Override
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	@Override
	public View getRoot() {
		return popupShareContainer;
	}

	@Override
	public View show() {
		showing = true;
		playing = true;
		return popupShareContainer;
	}

	@Override
	public View hide() {
		showing = false;
		playing = true;
		return popupShareContainer;
	}

	public FloatViewHostController getHost() {
		return host;
	}

	public void setHost(FloatViewHostController host) {
		this.host = host;
	}

	private final AuthListener authListener = new AuthListener() {

		@Override
		public void onFailed(Object obj) {
			ELog.e("Auth failed:" + obj);
			share = null;
		}

		@Override
		public void onComplete() {
			ELog.i("");
			String expires = String.valueOf(share.getExpires());
			if (expires.length() > 10) {
				expires = expires.substring(0, 10);
			}
			// 将第三方授权信息保存到本地prefs
			if (share.getType() == AuthAdapter.Type.SINA_WEIBO) {
				App.PREFS.setSinaId(share.getUid());
				App.PREFS.setSinaToken(share.getAccessToken());
				App.PREFS.setSinaExprise(Long.valueOf(expires));
				// 授权成功后，向服务器发送请求
				if (currentData != null) {
					share.shareInit(host.getActivity());
					share.share(currentData.getShareType(), currentData.getActionType(), currentData.getShareTitle(), currentData.getShareContent(), currentData.getShareLinkUrl(), currentData.getShareImageUrl());
				}
			}
			else if (share.getType() == AuthAdapter.Type.QQ) {
				App.PREFS.setQQId(share.getUid());
				App.PREFS.setQQToken(share.getAccessToken());
				App.PREFS.setQQExprise(Long.valueOf(expires));

				if (currentData != null) {
					share.shareInit(host.getActivity());
					share.share(currentData.getShareType(), currentData.getActionType(), currentData.getShareTitle(), currentData.getShareContent(), currentData.getShareLinkUrl(), currentData.getShareImageUrl());
				}
			}
		}

		@Override
		public void onCancel() {
			ELog.i("Auth cancel:" + share.getType());
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (share != null) {
			share.onActivityResult(requestCode, resultCode, data);
		}
	}
}

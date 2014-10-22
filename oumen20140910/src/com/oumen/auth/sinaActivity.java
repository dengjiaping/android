package com.oumen.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.android.BaseActivity;
import com.oumen.tools.ELog;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
/**
 * 新浪回调界面
 * @author oumen-o
 *
 */
public class sinaActivity extends BaseActivity implements IWeiboHandler.Response{
	
	private IWeiboShareAPI mWeiboShareAPI;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weixin_layout);
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(sinaActivity.this, SinaAuthAdapter.APP_KEY);
		mWeiboShareAPI.handleWeiboResponse(getIntent(),this);
	}
	@Override
	protected void onNewIntent(Intent intent) {
		mWeiboShareAPI.handleWeiboResponse(getIntent(),this);
	}

	/**
	 * 接收微客户端博请求的数据。 当微博客户端唤起当前应用并进行分享时，该方法被调用。
	 * 
	 * @param baseRequest
	 *            微博请求数据对象
	 * @see {@link IWeiboShareAPI#handleWeiboRequest}
	 */
	@Override
	public void onResponse(BaseResponse baseResp) {
		ELog.i("");
		switch (baseResp.errCode) {
			case WBConstants.ErrorCode.ERR_OK:
				 Toast.makeText(sinaActivity.this, "分享成功", Toast.LENGTH_LONG).show();
				 finish();
				break;
			case WBConstants.ErrorCode.ERR_CANCEL:
				Toast.makeText(sinaActivity.this, "取消分享", Toast.LENGTH_LONG).show();
				finish();
				break;
			case WBConstants.ErrorCode.ERR_FAIL:
				Toast.makeText(sinaActivity.this, "分享失败" + "Error Message: " + baseResp.errMsg, Toast.LENGTH_LONG).show();
				finish();
				break;
		}
	}
}

package com.oumen.account;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;
import com.oumen.widget.image.shape.RoundRectangleImageView;

/**
 * 找回密码界面
 * 
 */
public class FindPasswordFragment extends BaseFragment {
	private final int HANDLER_FINDPWD_SUCCESS = 1;
	private final int HANDLER_FINDPWD_FAIL = 2;
	private final int HANDLER_SEND_EMAIL_SUCCESS = 3;
	private final int HANDLER_SEND_EMAIL_FAIL = 4;

	//标题行
	private TitleBar titlebar;
	private Button btnBack;

	private TextView firstTip;

	private RelativeLayout firstContainer;
	private LinearLayout secondContainer;
	private EditText etInput;

	private RoundRectangleImageView headPhoto;
	private TextView tvNickName;
	private TextView secondTip;

	private Button btnNext;

	private boolean FirstFlag = true;
	private String input = null;

	private InputMethodManager inputManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.find_password, container, false);

		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		titlebar.getRightButton().setVisibility(View.GONE);
		titlebar.getTitle().setText(getResources().getString(R.string.find_password));
		btnBack = titlebar.getLeftButton();

		firstTip = (TextView) view.findViewById(R.id.tip);

		firstContainer = (RelativeLayout) view.findViewById(R.id.first_container);
		secondContainer = (LinearLayout) view.findViewById(R.id.second_container);

		etInput = (EditText) view.findViewById(R.id.email);

		headPhoto = (RoundRectangleImageView) view.findViewById(R.id.photo);
		headPhoto.setRadius(5);
		tvNickName = (TextView) view.findViewById(R.id.nickname);
		secondTip = (TextView) view.findViewById(R.id.tip2);

		btnNext = (Button) view.findViewById(R.id.next);

		btnBack.setOnClickListener(clickListener);
		btnNext.setOnClickListener(clickListener);

		firstContainer.setVisibility(View.VISIBLE);
		secondContainer.setVisibility(View.GONE);
		btnNext.setText(getResources().getString(R.string.find_password));
		firstTip.setText(getResources().getString(R.string.find_password_start_msg));

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public boolean onBackPressed() {
		getFragmentManager().popBackStack();
		return true;
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (inputManager.isActive()) {
				inputManager.hideSoftInputFromWindow(etInput.getWindowToken(), 0);
			}
			if (v == btnBack) {
				if (FirstFlag) {
					getFragmentManager().popBackStack();
				}
				else {
					firstContainer.setVisibility(View.VISIBLE);
					secondContainer.setVisibility(View.GONE);
					btnNext.setText(getResources().getString(R.string.next));
					FirstFlag = true;
				}
			}
			else if (v == btnNext) {
				if (FirstFlag) {
					input = etInput.getText().toString();
					if (TextUtils.isEmpty(input)) {
						Toast.makeText(getActivity(), "请输入邮箱", Toast.LENGTH_SHORT).show();
						return;
					}
					if (input.contains("@") && !input.matches(Constants.PATTERN_EMAIL)) {
						Toast.makeText(getActivity(), "邮箱格式不正确", Toast.LENGTH_SHORT).show();
						return;
					}
					findPassword(input);
				}
				else {
					sendEmail(etInput.getText().toString());
				}
			}
		}
	};

	private void sendEmail(String input) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", input));

		HttpRequest req = new HttpRequest(Constants.LOGIN_FIND_PASSWORD_URL, params, HttpRequest.Method.GET, sendEmailCallback);
		App.THREAD.execute(req);
	}

	final DefaultHttpCallback sendEmailCallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String res = result.getResult();
				ELog.i(res);
				JSONObject obj = new JSONObject(res);
				if (obj.getString("success").equals("1")) {
					handler.sendMessage(handler.obtainMessage(HANDLER_SEND_EMAIL_SUCCESS, obj.getString("tip")));
				}
				else {
					handler.sendMessage(handler.obtainMessage(HANDLER_SEND_EMAIL_FAIL, obj.getString("tip")));
				}
			}
			catch (Exception e) {
				ELog.i("Exception e= " + e.toString());
				handler.sendMessage(handler.obtainMessage(HANDLER_FINDPWD_FAIL, "邮件发送失败"));
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_FINDPWD_FAIL, "邮件发送失败"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_FINDPWD_FAIL, "邮件发送失败"));
		}
	});

	/**
	 * 找回密码联网请求
	 * 
	 * @param input
	 *            oumen账号或者邮箱地址
	 */
	private void findPassword(String input) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", input));

		HttpRequest req = new HttpRequest(Constants.LOGIN_FIND_USERMESSAGE_URL, params, HttpRequest.Method.GET, findpwdCallback);
		App.THREAD.execute(req);
	}

	final DefaultHttpCallback findpwdCallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String res = result.getResult();
				ELog.i(res);
				JSONObject obj = new JSONObject(res);
				int success = obj.has("success") ? Integer.valueOf(obj.getString("success")) : 2;

				if (success == 0) {
					handler.sendMessage(handler.obtainMessage(HANDLER_FINDPWD_FAIL, obj.getString("tip")));
				}
				else {
					handler.sendMessage(handler.obtainMessage(HANDLER_FINDPWD_SUCCESS, res));
				}
			}
			catch (Exception e) {
				ELog.e("Exception e = " + e.toString());
				handler.sendMessage(handler.obtainMessage(HANDLER_FINDPWD_FAIL, "获取信息失败"));
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_FINDPWD_FAIL, "获取信息失败"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_FINDPWD_FAIL, "获取信息失败"));
		}
	});

	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_FINDPWD_SUCCESS:
				try {
					String res = (String) msg.obj;
					JSONObject obj = new JSONObject(res);
					String nickName = obj.has("nickname") ? obj.getString("nickname") : null;
					String imageUrl = obj.has("head_photo") ? obj.getString("head_photo") : null;

					tvNickName.setText(nickName);
					ImageLoader.getInstance().displayImage(App.getSmallPicUrl(imageUrl, App.BIG_PHOTO_SIZE), headPhoto, App.OPTIONS_HEAD_RECT);

					firstContainer.setVisibility(View.GONE);
					secondContainer.setVisibility(View.VISIBLE);
					secondTip.setText("确认向" + input + "发送密码重置邮件");
					btnNext.setText(getResources().getString(R.string.confirm_send));
					FirstFlag = false;
					firstTip.setText("");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case HANDLER_SEND_EMAIL_SUCCESS:
				secondTip.setText("电子邮箱已发送!");
				secondTip.setVisibility(View.VISIBLE);
				break;
			case HANDLER_FINDPWD_FAIL:
				Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case HANDLER_SEND_EMAIL_FAIL:
				secondTip.setText("点击请求发送密码重置电子邮箱");
				Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
		}
		return false;
	};
}

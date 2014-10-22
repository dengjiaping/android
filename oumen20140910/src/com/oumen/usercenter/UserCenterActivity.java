package com.oumen.usercenter;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.UserProfile;
import com.oumen.android.peers.ClipImageFragment;
import com.oumen.android.util.Constants;
import com.oumen.auth.AuthAdapter;
import com.oumen.auth.AuthListener;
import com.oumen.auth.QqAuthAdapter;
import com.oumen.auth.SinaAuthAdapter;
import com.oumen.message.MessageService;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.oumen.widget.dialog.PickImageDialog;
import com.oumen.widget.dialog.TwoButtonDialog;
import com.oumen.widget.image.shape.RoundRectangleImageView;

/**
 * 新的用户中心界面
 * 
 */
public class UserCenterActivity extends BaseActivity implements View.OnClickListener {
	public static final String KEY_RESULT_LOGOUT = "logout";

	private final int REQUEST_CODE_PICK_CITY = 1;//城市选择标记
	private final int REQUEST_UPLOAD_NICKNAME = 2;
	private final int REQUEST_CODE_BABY_STATE = 3;
	private final int REQUEST_UPLOAD_BABY_SIGN = 4;
	private final int REQUEST_UPLOAD_BABY_SEX = 5;
	private final int REQUEST_UPLOAD_USER_SEX = 6;
	//缓存参数标记
	private final String STATE_BASEAPPLICATION_USERPROFILE = "baseapplication_userprofile";
	private final String STATE_DIALOG = "dialog";
	private final String STATE_PHOTO_PATH = "path";

	final int SINA_SUCCESS = 1;
	final int SINA_FAIL = 2;
	//标题行
	private TitleBar titlebar;
	private Button btnBack;
	private RelativeLayout photoItem;
	private RoundRectangleImageView ivPhoto;
	private ImageView ivChangePhotoArraw;//修改头像
	private RelativeLayout nickNameItem, addressItem;
	private TextView tvNickname, tvomNum, tvAddress;
	private RelativeLayout stateItem, babySexItem, babySignItem;
	private TextView tvState, tvBabySex, tvBabySign;
	private RelativeLayout sinaItem, qqItem;
	private TextView txtSinaWeibo, txtTencentQQ;
	private RelativeLayout rlEmail;
	private RelativeLayout userSexItem;
	private TextView tvEmail, tvUserSex;
	private RelativeLayout ivChangePwd;
	private Button btnExit;

	private TwoButtonDialog dialogLogout;

	private CenterHttpController controller;
	private File file;
	private String path;// 图片路径

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_center1);
		// 初始化
		controller = new CenterHttpController(handler);
		file = new File(Constants.HEAD_PHOTO_PATH, App.PREFS.getUid() + Constants.HEAD_PHOTO_NAME);
		init();
		initData();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(STATE_BASEAPPLICATION_USERPROFILE, App.USER);
		outState.putString(STATE_PHOTO_PATH, path);
		outState.putBoolean(STATE_DIALOG, dialogPickImages != null);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			ELog.i("");
			path = savedInstanceState.getString(STATE_PHOTO_PATH);
			if (savedInstanceState.getBoolean(STATE_DIALOG)) {
				retainPickImageDialog();
				ELog.i("Retain path:" + path);
			}
			App.USER.copyFrom((UserProfile) savedInstanceState.getSerializable(STATE_BASEAPPLICATION_USERPROFILE));
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	private void init() {
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		titlebar.getTitle().setText(R.string.nav_usercenter_title);
		titlebar.getRightButton().setVisibility(View.GONE);
		btnBack = titlebar.getLeftButton();// 返回按键

		photoItem = (RelativeLayout) findViewById(R.id.photo_item);
		ivPhoto = (RoundRectangleImageView) findViewById(R.id.photo);// 头像
		ivPhoto.setRadius(10);
		ivChangePhotoArraw = (ImageView) findViewById(R.id.change_photo);
		nickNameItem = (RelativeLayout) findViewById(R.id.center_username_item);

		tvNickname = (TextView) findViewById(R.id.center_username);// 昵称
		tvomNum = (TextView) findViewById(R.id.center_oumenname);// 偶们号
		tvAddress = (TextView) findViewById(R.id.tv_oumen_address);// 地址

		addressItem = (RelativeLayout) findViewById(R.id.address_item);

		tvState = (TextView) findViewById(R.id.tv_babystate);
		tvBabySex = (TextView) findViewById(R.id.tv_usercenter_sex);
		tvBabySign = (TextView) findViewById(R.id.tv_usercenter_sign);

		stateItem = (RelativeLayout) findViewById(R.id.babystate_item);
		babySexItem = (RelativeLayout) findViewById(R.id.usercenter_baby_sex_item);
		babySignItem = (RelativeLayout) findViewById(R.id.usercenter_sign_item);

		txtSinaWeibo = (TextView) findViewById(R.id.sina_weibo);
		txtTencentQQ = (TextView) findViewById(R.id.tencent_qq);

		sinaItem = (RelativeLayout) findViewById(R.id.weibo_item);
		qqItem = (RelativeLayout) findViewById(R.id.qq_item);

		rlEmail = (RelativeLayout) findViewById(R.id.rl_usercenter_email);
		tvEmail = (TextView) findViewById(R.id.tv_usercenter_email);

		tvUserSex = (TextView) findViewById(R.id.tv_usercenter_usersex);
		userSexItem = (RelativeLayout) findViewById(R.id.usersex_item);
		ivChangePwd = (RelativeLayout) findViewById(R.id.change_pwd);

		btnExit = (Button) findViewById(R.id.center_exit);// 退出按钮

		btnBack.setOnClickListener(this);
		photoItem.setOnClickListener(this);
//		ivPhoto.setOnClickListener(this);
		ivChangePhotoArraw.setOnClickListener(this);
		nickNameItem.setOnClickListener(this);
		sinaItem.setOnClickListener(this);
		qqItem.setOnClickListener(this);
		btnExit.setOnClickListener(this);
		addressItem.setOnClickListener(this);
		stateItem.setOnClickListener(this);
		babySexItem.setOnClickListener(this);
		babySignItem.setOnClickListener(this);
		userSexItem.setOnClickListener(this);
		ivChangePwd.setOnClickListener(this);
	}

	private void initData() {
		// 设置头像
		if (App.USER.hasPhoto()) {
			ImageLoader.getInstance().displayImage(App.USER.getPhotoUrl(2 * getResources().getDimensionPixelSize(R.dimen.big_photo_size)), ivPhoto, new SimpleImageLoadingListener() {

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					if (view instanceof ImageView) {
						ELog.i("Loaded:" + imageUri);
						Bitmap img = ImageTools.clip2square(loadedImage);

						ImageView v = (ImageView) view;
						v.setImageBitmap(img);
					}
				}
			});
		}
		else {
			ivPhoto.setImageResource(R.drawable.user_center_photo);
		}
		// 设置昵称
		if (!TextUtils.isEmpty(App.USER.getNickname())) {
			tvNickname.setText(App.USER.getNickname());
		}
		else {
			tvNickname.setText("");
		}
		// 设置偶们号
		tvomNum.setText(App.USER.getOmNumber());

		if (!TextUtils.isEmpty(App.USER.getAddress())) {
			tvAddress.setText(App.USER.getAddress());
		}
		else {
			tvAddress.setText("");
		}
		//宝宝状态设置
		setBabyState();
		// 宝宝签名
		if (!TextUtils.isEmpty(App.USER.getManifesto())) {
			tvBabySign.setText(App.USER.getManifesto());
		}
		else {
			tvBabySign.setText("");
		}

		// 新浪是否绑定
		if (SinaAuthAdapter.isAuthor()) {
			txtSinaWeibo.setText("已绑定     ");
			txtSinaWeibo.setTextColor(getResources().getColor(R.color.default_bg));
			txtSinaWeibo.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
		else {
			txtSinaWeibo.setText("未绑定");
			txtSinaWeibo.setTextColor(getResources().getColor(R.color.user_center_text_bg));
			txtSinaWeibo.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.center_arrow), null);
		}
		// 腾讯是否绑定
		if (QqAuthAdapter.isAuthor()) {
			txtTencentQQ.setText("已绑定     ");
			txtTencentQQ.setTextColor(getResources().getColor(R.color.default_bg));
			txtTencentQQ.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
		else {
			txtTencentQQ.setText("未绑定");
			txtTencentQQ.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.center_arrow), null);
			txtTencentQQ.setTextColor(getResources().getColor(R.color.user_center_text_bg));
		}
		// 如果为第三方登录过来的,就将邮箱隐藏起来
		if (App.USER.isThirdparty()) {
			rlEmail.setVisibility(View.GONE);
		}
		else {
			String[] email = App.PREFS.getEmail();
			rlEmail.setVisibility(View.VISIBLE);
			tvEmail.setText(email == null ? "" : email[1]);
		}
		// 用户性别设置
		setUserSex();
	}

	private void setUserSex() {
		int gender = App.USER.getUserGender();
		if (gender != Constants.NULL_INT) {
			if (gender == 0) {
				tvUserSex.setText("男");
			}
			else if (gender == 1) {
				tvUserSex.setText("女");
			}
		}
		else {
			tvUserSex.setText("");
		}
	}

	/**
	 * 设置宝宝状态
	 */
	private void setBabyState() {
		// 宝宝状态设置
		int babyType = App.USER.getBabyType();
		if (babyType != Constants.NULL_INT) {
			if (babyType == UserProfile.BABY_TYPE_HUAI_YUN) {
				if (null != App.USER.getGravidity()) {
					tvState.setText(App.USER.getGravidity());
				}
				else {
					tvState.setText("");
				}
				tvBabySex.setText("");
				tvBabySex.setClickable(false);
				tvBabySign.setText(App.USER.getManifesto());
			}
			else if (babyType == UserProfile.BABY_TYPE_CHU_SHENG) {
				if (!TextUtils.isEmpty(App.USER.getBirthday())) {
					tvState.setText(App.USER.getBirthday());
					// 宝宝性别设置
					int gender = App.USER.getBabyGender();
					switch (gender) {
						case 0:
							tvBabySex.setText("男宝宝");
							tvBabySex.setClickable(true);
							break;

						case 1:
							tvBabySex.setText("女宝宝");
							tvBabySex.setClickable(true);
							break;

						case -1:
						default:
							tvBabySex.setText("");
							tvBabySex.setClickable(false);
					}
				}
				else {
					tvState.setText("");
				}
				tvBabySign.setText(App.USER.getManifesto());
			}
			else if (babyType == UserProfile.BABY_TYPE_BEI_YUN) {
				tvState.setText("备孕中");
				tvBabySex.setText("");
				tvBabySex.setClickable(false);
				tvBabySign.setText(App.USER.getManifesto());
			}
			else if (babyType == UserProfile.BABY_TYPE_QI_TA) {
				tvState.setText("其他");
				tvBabySex.setText("");
				tvBabySex.setClickable(false);
				tvBabySign.setText(App.USER.getManifesto());
			}
		}
		else {
			tvState.setText("");
			tvBabySex.setText("");
			tvBabySex.setClickable(false);
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
			case R.id.left:
				setResult(Activity.RESULT_OK);
				//发广播，通知界面更新信息
				sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_USERINFO));
				finish();
				break;

			case R.id.change_photo://选取头像
				showPickImageDialog();
				break;
			case R.id.photo_item://头像全部
				showPickImageDialog();
				break;

			case R.id.photo://选取头像
				showPickImageDialog();
				break;

			case R.id.center_username_item:
				//TODO 
				intent = new Intent(UserCenterActivity.this, UploadUserMessageActivity.class);
				intent.putExtra(UploadUserMessageActivity.UPDATE_MESSAGE_TAG, UploadUserMessageActivity.TYPE_NICKNAME);
				startActivityForResult(intent, REQUEST_UPLOAD_NICKNAME);
				break;
			case R.id.address_item:
				ProvinceAdapter.selectedId = -1;
				// 跳转到城市选择界面
				intent = new Intent(UserCenterActivity.this, CityPickerActiviry.class);
				startActivityForResult(intent, REQUEST_CODE_PICK_CITY);
				break;
			case R.id.babystate_item:
				// 跳转到宝宝设置界面
				intent = new Intent(UserCenterActivity.this, BabyStateActivity.class);
				intent.putExtra(BabyStateActivity.INTENT_KEY_DATA, BabyStateActivity.REQUEST_FROM_USERCENTER);
				startActivityForResult(intent, REQUEST_CODE_BABY_STATE);
				break;

			case R.id.usercenter_baby_sex_item:
				if (App.USER.getBabyType() == UserProfile.BABY_TYPE_CHU_SHENG) {
					intent = new Intent(UserCenterActivity.this, UploadUserMessageActivity.class);
					intent.putExtra(UploadUserMessageActivity.UPDATE_MESSAGE_TAG, UploadUserMessageActivity.TYPE_BABY_SEX);
					startActivityForResult(intent, REQUEST_UPLOAD_BABY_SEX);
				}
				break;

			case R.id.usercenter_sign_item:
				intent = new Intent(UserCenterActivity.this, UploadUserMessageActivity.class);
				intent.putExtra(UploadUserMessageActivity.UPDATE_MESSAGE_TAG, UploadUserMessageActivity.TYPE_SIGN);
				startActivityForResult(intent, REQUEST_UPLOAD_BABY_SIGN);
				break;

			case R.id.usersex_item:
				intent = new Intent(UserCenterActivity.this, UploadUserMessageActivity.class);
				intent.putExtra(UploadUserMessageActivity.UPDATE_MESSAGE_TAG, UploadUserMessageActivity.TYPE_USER_SEX);
				startActivityForResult(intent, REQUEST_UPLOAD_USER_SEX);
				break;

			case R.id.weibo_item:
				if (SinaAuthAdapter.isAuthor()) {
					Toast.makeText(mBaseApplication, "新浪微博已绑定", Toast.LENGTH_SHORT).show();
				}
				else {
					if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
						auth = AuthAdapter.create(authListener, AuthAdapter.Type.SINA_WEIBO);
						auth.authorize(UserCenterActivity.this);
					}
					else {
						Toast.makeText(UserCenterActivity.this, "亲，您的网络没有打开~", Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case R.id.qq_item:

				if (QqAuthAdapter.isAuthor()) {
					Toast.makeText(mBaseApplication, "腾讯账号已绑定", Toast.LENGTH_SHORT).show();
				}
				else {
					if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
						auth = AuthAdapter.create(authListener, AuthAdapter.Type.QQ);
						auth.authorize(UserCenterActivity.this);
					}
					else {
						Toast.makeText(UserCenterActivity.this, "亲，您的网络没有打开~", Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case R.id.change_pwd:
				//TODO 修改密码
				// 判断是否为第三方登录
				if (App.USER.isThirdparty()) {
					Toast.makeText(mBaseApplication, "对不起，不能修改密码", Toast.LENGTH_SHORT).show();
				}
				else {
					// 更改密码,跳转到修改密码界面
					startActivity(new Intent(mBaseApplication, UpdatePwdActivity.class));
				}
				break;

			case R.id.center_exit:
				if (dialogLogout == null) {
					View.OnClickListener listener = new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							dialogLogout.dismiss();

							if (v == dialogLogout.getLeftButton()) {
								//聊天登出
								sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_LOGOUT));

								Intent intent = new Intent();
								intent.putExtra(KEY_RESULT_LOGOUT, true);
								setResult(Activity.RESULT_OK, intent);
								finish();
							}
						}
					};
					dialogLogout = new TwoButtonDialog(this);
					dialogLogout.setCancelable(true);
					dialogLogout.getMessageView().setText(R.string.logout_confirm);
					dialogLogout.getLeftButton().setOnClickListener(listener);
					dialogLogout.getRightButton().setOnClickListener(listener);
				}
				dialogLogout.show();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (auth != null) {
			auth.onActivityResult(requestCode, resultCode, data);
		}
		if (resultCode == Activity.RESULT_OK) {
			if (dialogPickImages != null) {
				String tempPath = dialogPickImages.onActivityResult(requestCode, resultCode, data);
				ELog.i(tempPath);
				if (tempPath == null && requestCode == Constants.REQUEST_CODE_OPEN_CAMERA) {//调取照相机拍照返回
					if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
						try {
							ELog.i(file.getAbsolutePath());
							Bitmap tmp = ImageTools.handlePhotoFromLocation(file, path);
							Bitmap square = ImageTools.clip2square(tmp);
							ivPhoto.setImageBitmap(square);

							controller.uploadUserPhoto(file.getAbsolutePath());
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					else {
						Toast.makeText(UserCenterActivity.this, "亲，您的网络没有打开~", Toast.LENGTH_SHORT).show();
					}
				}
			}

			if (requestCode == PickImageDialog.REQUEST_CRPO_IMAGE) {//从截取图片返回
				String temppath = data.getStringExtra(ClipImageFragment.INTENT_CROP_IMAGE_BACK);
				Bitmap temp = ImageTools.decodeSourceFile(temppath);
				Bitmap square = ImageTools.clip2square(temp);
				temp.recycle();
				ivPhoto.setImageBitmap(square);
				try {
					ImageTools.save(temppath, file);
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}

				if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
					try {
						ELog.i(temppath);
						controller.uploadUserPhoto(temppath);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					Toast.makeText(UserCenterActivity.this, "亲，您的网络没有打开~", Toast.LENGTH_SHORT).show();
				}
			}
			else if (requestCode == REQUEST_CODE_PICK_CITY) {// 设置地址信息
				String address = data.getStringExtra(CityPickerActiviry.CITY_CHOOSE_TAG);
				if (!TextUtils.isEmpty(address)) {
					tvAddress.setText(address);
					//2.修改本地用户信息
					App.USER.setAddress(address);
					//TODO 向服务器发送请求，修改地址
					controller.updateUserInfo();

				}
			}
			else if (requestCode == REQUEST_UPLOAD_NICKNAME) {
				String str = data.getStringExtra(UploadUserMessageActivity.UPDATE_MESSAGE_RESULE);
				if (str != null) {
					tvNickname.setText(str);
					App.USER.setNickname(str);
					controller.updateUserInfo();
				}
			}
			else if (requestCode == REQUEST_CODE_BABY_STATE) {
				App.USER.setBabyType(data.getIntExtra(UserProfile.FIELD_KEY_BABY_TYPE, Constants.NULL_INT));
				App.USER.setManifesto(data.getStringExtra(UserProfile.FIELD_KEY_MANIFESTO));
				App.USER.setGravidity(data.getStringExtra(UserProfile.FIELD_KEY_GRAVIDITY));
				App.USER.setGravidityTime(data.getStringExtra(UserProfile.FIELD_KEY_GRAVIDITY_TIME));
				App.USER.setBirthday(data.getStringExtra(UserProfile.FIELD_KEY_BIRTHDAY));
				App.USER.setBirthdayTime(data.getStringExtra(UserProfile.FIELD_KEY_BIRTHDAY_TIME));
				App.USER.setBabyGender(data.getIntExtra(UserProfile.FIELD_KEY_BABY_GENDER, Constants.NULL_INT));

				setBabyState();

//				babyStateHttpRequest();
				controller.updateUserInfo();
			}
			else if (requestCode == REQUEST_UPLOAD_BABY_SIGN) {
				String str = data.getStringExtra(UploadUserMessageActivity.UPDATE_MESSAGE_RESULE);
				if (str != null) {
					tvBabySign.setText(str);
					App.USER.setManifesto(str);
					controller.updateUserInfo();
				}
			}
			else if (requestCode == REQUEST_UPLOAD_BABY_SEX) {
				int type = data.getIntExtra(UploadUserMessageActivity.UPDATE_MESSAGE_RESULE, App.INT_UNSET);
				if (type != App.INT_UNSET) {

					switch (type) {
						case 0:
							tvBabySex.setText("男宝宝");
							break;

						case 1:
							tvBabySex.setText("女宝宝");
							break;

						case -1:
						default:
							tvBabySex.setText("");
					}

					App.USER.setBabyGender(type);
					controller.updateUserInfo();
				}
			}
			else if (requestCode == REQUEST_UPLOAD_USER_SEX) {
				int type = data.getIntExtra(UploadUserMessageActivity.UPDATE_MESSAGE_RESULE, App.INT_UNSET);
				if (type != App.INT_UNSET) {

					if (type == 0) {
						tvUserSex.setText("男");
					}
					else if (type == 1) {
						tvUserSex.setText("女");
					}
					App.USER.setUserGender(type);
					controller.updateUserInfo();
				}
			}
		}
	}

	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case CenterHttpController.HANDLER_BIND:
				if (msg.obj == null) {
					switch (auth.getType()) {
						case SINA_WEIBO:
							App.USER.setSinaBind(true);
							txtSinaWeibo.setText("已绑定     ");
							txtSinaWeibo.setTextColor(getResources().getColor(R.color.default_bg));
							txtSinaWeibo.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
							break;

						case QQ:
							App.USER.setQqBind(true);
							txtTencentQQ.setText("已绑定     ");
							txtTencentQQ.setTextColor(getResources().getColor(R.color.default_bg));
							txtTencentQQ.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
							break;

						case TENCENT_WEIBO:
							break;
						case WEIXIN:
							break;
					}
					Toast.makeText(this, "绑定成功", Toast.LENGTH_SHORT).show();
					auth = null;
				}
				else {
					Toast.makeText(this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				}
				break;

			case CenterHttpController.HANDLER_UPLOAD_PHOTO_SUCCESS:// 修改头像成功
				Toast.makeText(UserCenterActivity.this, "更换头像成功", Toast.LENGTH_SHORT).show();
				String temppath = (String) msg.obj;
				App.USER.setPhoto(temppath);
				ELog.i(App.USER.toString());
				break;

			case CenterHttpController.HANDLER_UPLOAD_PHOTO_FAIL:// 修改头像失败了
				if (App.USER.hasPhoto()) {
					ImageLoader.getInstance().displayImage(App.USER.getPhotoUrl(App.BIG_PHOTO_SIZE), ivPhoto, App.OPTIONS_HEAD_ROUND);
				}
				else {
					ivPhoto.setBackgroundResource(R.drawable.round_user_photo);
				}
				break;
		}
		return false;
	};

	// -------------------- 第三方绑定 --------------------//
	private AuthAdapter auth;

	private final AuthListener authListener = new AuthListener() {

		@Override
		public void onFailed(Object obj) {
			ELog.e("Auth failed:" + obj);
			auth = null;
		}

		@Override
		public void onComplete() {
			ELog.i("");
			// 将数据保存到App.PREFS里
			String expires = String.valueOf(auth.getExpires());
			if (expires.length() > 10) {
				expires = expires.substring(0, 10);
			}
			//将第三方授权信息保存到本地App.PREFS
			if (auth.getType() == AuthAdapter.Type.SINA_WEIBO) {
				App.PREFS.setSinaId(auth.getUid());
				App.PREFS.setSinaToken(auth.getAccessToken());
				App.PREFS.setSinaExprise(Long.valueOf(expires));
			}
			else if (auth.getType() == AuthAdapter.Type.QQ) {
				App.PREFS.setQQId(auth.getUid());
				App.PREFS.setQQToken(auth.getAccessToken());
				App.PREFS.setQQExprise(Long.valueOf(expires));
			}
			controller.bindThirdpart(auth.getType());
		}

		@Override
		public void onCancel() {
			ELog.i("Auth cancel:" + auth.getType());
		}
	};

	@Override
	public void onBackPressed() {
		if (dialogLogout != null && dialogLogout.isShowing()) {
			dialogLogout.dismiss();
		}
		else if (isShowingPickImageDialog()) {
			hidePickImageDialog();
		}
		else {
			Intent notify = MessageService.createResponseNotify(MessageService.TYPE_USERINFO);
			sendBroadcast(notify);
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		if (dialogLogout != null && dialogLogout.isShowing()) {
			dialogLogout.dismiss();
		}
		hidePickImageDialog();
		super.onDestroy();
	}

	// -------------------- Pick Images -------------------//
	private PickImageDialog dialogPickImages;

	private void showPickImageDialog() {
		if (dialogPickImages == null) {
			dialogPickImages = new PickImageDialog(this);
		}
		path = Constants.UPLOAD_PATH + System.currentTimeMillis();
		dialogPickImages.setPath(path);
		dialogPickImages.show();
	}

	private void retainPickImageDialog() {
		if (dialogPickImages != null)
			return;

		dialogPickImages = new PickImageDialog(this);
		dialogPickImages.setPath(path);
	}

	private void hidePickImageDialog() {
		if (dialogPickImages != null && dialogPickImages.isShowing())
			dialogPickImages.dismiss();
	}

	private boolean isShowingPickImageDialog() {
		return dialogPickImages != null && dialogPickImages.isShowing();
	}

}

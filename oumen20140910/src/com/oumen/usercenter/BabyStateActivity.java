package com.oumen.usercenter;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.UserProfile;
import com.oumen.android.util.Constants;
import com.oumen.tools.CalendarTools;
import com.oumen.tools.ELog;
import com.oumen.widget.dialog.PickDatetimeDialog;

public class BabyStateActivity extends BaseActivity {
	// -------------------- Intent Data --------------------//
	public static final String INTENT_KEY_FROM = "from";
	public static final String INTENT_KEY_DATA = "data";

	public static final int REQUEST_FROM_USERCENTER = 1;
	public static final int REQUEST_FROM_REGISTER = 2;
	public static final int REQUEST_FROM_THIRD_LOGIN = 3;

	private int babyGender = Constants.NULL_INT;
	private int babyType = UserProfile.BABY_TYPE_QI_TA;
	private String manifesto;
	private String gravidity;
	private String gravidityTime;
	private String birthday;
	private String birthdayTime;

	final int REQUEST_CODE_MANIFESTO = 1;

	private final int PICK_DATETIME_PREPARE = 0;
	private final int PICK_DATETIME_BIRTHDAY = 1;

	private final String DATE_FORMAT_TEMPLETE = "yyyy-MM-dd";
	private PickDatetimeDialog dialogPickDatetime;

	private TitleBar titlebar;
	private Button btnBack, btnEdit;

	private RadioGroup rg1, rg2;
	private RadioButton ivPrepare, ivOther, ivPregnant, ivBaby;
	private LinearLayout llPrepare, llOther, llPregnant, llBaby;
	private TextView tvPregnant, tvPregnantTime, tvPregnantSign;

	private TextView tvBirthday, tvBabyTime, tvBabySign;
	private Button btnGirl, btnBoy;
	private Boolean changeGroup = false;

	private int fromActivity = App.INT_UNSET;

	private CenterHttpController controller;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_center_babystate);
		init();
		initData();
		controller = new CenterHttpController(handler);

		fromActivity = getIntent().getIntExtra(INTENT_KEY_DATA, App.INT_UNSET);
	}

	private void init() {
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		titlebar.getTitle().setText(R.string.nav_babysetting_title);
		btnBack = titlebar.getLeftButton();
		btnBack.setOnClickListener(clickListener);

		btnEdit = titlebar.getRightButton();
		btnEdit.setText(R.string.finish);
		btnEdit.setOnClickListener(clickListener);

		// 两个RadioGroup
		rg1 = (RadioGroup) findViewById(R.id.rg_babystate1);
		rg2 = (RadioGroup) findViewById(R.id.rg_babystate2);

		ivPrepare = (RadioButton) findViewById(R.id.iv_babystate_prepare);
		ivPrepare.setOnClickListener(clickListener);

		ivOther = (RadioButton) findViewById(R.id.iv_babystate_other);
		ivOther.setOnClickListener(clickListener);

		ivPregnant = (RadioButton) findViewById(R.id.iv_babystate_paregent);
		ivPregnant.setOnClickListener(clickListener);

		ivBaby = (RadioButton) findViewById(R.id.iv_babystate_baby);
		ivBaby.setOnClickListener(clickListener);

		llPrepare = (LinearLayout) findViewById(R.id.ll_babystate_prepare);
		llOther = (LinearLayout) findViewById(R.id.ll_babystate_other);
		llPregnant = (LinearLayout) findViewById(R.id.ll_babystate_paregent);
		llBaby = (LinearLayout) findViewById(R.id.ll_babystate_baby);

		// 预产期设置
		tvPregnant = (TextView) findViewById(R.id.tv_babystate_paregent);

		tvPregnantTime = (TextView) findViewById(R.id.tv_babystate_pargenttime);
		tvPregnantTime.setOnClickListener(clickListener);

		tvPregnantSign = (TextView) findViewById(R.id.tv_babystate_pregnant_sign);
		tvPregnantSign.setOnClickListener(clickListener);

		// 宝宝出生设置
		tvBirthday = (TextView) findViewById(R.id.tv_babystate_birthday);

		tvBabyTime = (TextView) findViewById(R.id.tv_babaystate_babytime);
		tvBabyTime.setOnClickListener(clickListener);
		btnBoy = (Button) findViewById(R.id.rb_babystate_mam);
		btnGirl = (Button) findViewById(R.id.rb_babystate_femam);
		btnBoy.setOnClickListener(clickListener);
		btnGirl.setOnClickListener(clickListener);

		tvBabySign = (TextView) findViewById(R.id.tv_babystate_babysign);
		tvBabySign.setOnClickListener(clickListener);
	}

	private void initData() {
		babyGender = App.USER.getBabyGender();
		babyType = App.USER.getBabyType();

		manifesto = App.USER.getManifesto();
		gravidity = App.USER.getGravidity();
		gravidityTime = App.USER.getGravidityTime();
		birthday = App.USER.getBirthday();
		birthdayTime = App.USER.getBirthdayTime();

		updateBabyType();
		updateManifesto();
		// 宝宝签名的设置
		if (babyType == UserProfile.BABY_TYPE_HUAI_YUN) {// 怀孕
			tvPregnant.setText(gravidity);
			tvPregnantTime.setText(gravidityTime);
		}
		else if (babyType == UserProfile.BABY_TYPE_CHU_SHENG) {
			tvBirthday.setText(birthday);
			tvBabyTime.setText(birthdayTime);
			if (babyGender == Constants.NULL_INT) {
				babySexOfDefault();
			}
			else if (babyGender == 0) {
				babySexOfBoy();
			}
			else {
				babySexOfGirl();
			}
		}
	}

	private void updateManifesto() {
		if (TextUtils.isEmpty(manifesto)) {
			tvPregnantSign.setText("");
			tvBabySign.setText("");
		}
		else {
			tvPregnantSign.setText(manifesto);
			tvBabySign.setText(manifesto);
		}
	}

	private void updateGavidity() {
		tvPregnantTime.setText(TextUtils.isEmpty(gravidityTime) ? "" : gravidityTime);
		tvPregnant.setText(TextUtils.isEmpty(gravidity) ? "" : gravidity);
	}

	private void updateBabyBirthday() {
		tvBabyTime.setText(TextUtils.isEmpty(birthdayTime) ? "" : birthdayTime);
		tvBirthday.setText(TextUtils.isEmpty(birthday) ? "" : birthday);
	}

	private void updateBabyType() {
		switch (babyType) {
			case UserProfile.BABY_TYPE_HUAI_YUN:// 怀孕
				onCheckedChanged(rg2, R.id.iv_babystate_paregent);
				onCheckedChanged(rg2, R.id.iv_babystate_paregent);
				llPrepare.setVisibility(View.GONE);
				llOther.setVisibility(View.GONE);
				llPregnant.setVisibility(View.VISIBLE);
				llBaby.setVisibility(View.GONE);
				break;

			case UserProfile.BABY_TYPE_CHU_SHENG:// 出生
				onCheckedChanged(rg2, R.id.iv_babystate_baby);
				llPrepare.setVisibility(View.GONE);
				llOther.setVisibility(View.GONE);
				llPregnant.setVisibility(View.GONE);
				llBaby.setVisibility(View.VISIBLE);
				break;

			case UserProfile.BABY_TYPE_BEI_YUN:// 备孕
				onCheckedChanged(rg1, R.id.iv_babystate_prepare);
				llPrepare.setVisibility(View.VISIBLE);
				llOther.setVisibility(View.GONE);
				llPregnant.setVisibility(View.GONE);
				llBaby.setVisibility(View.GONE);
				break;

			case UserProfile.BABY_TYPE_QI_TA:// 其他
				onCheckedChanged(rg1, R.id.iv_babystate_other);
				llPrepare.setVisibility(View.GONE);
				llOther.setVisibility(View.VISIBLE);
				llPregnant.setVisibility(View.GONE);
				llBaby.setVisibility(View.GONE);
				break;
			default:
				onCheckedChanged(rg1, R.id.iv_babystate_other);
				llPrepare.setVisibility(View.GONE);
				llOther.setVisibility(View.VISIBLE);
				llPregnant.setVisibility(View.GONE);
				llBaby.setVisibility(View.GONE);
				break;
		}
	}

	final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.left:
					setResult(Activity.RESULT_CANCELED);
					finish();
					break;

				case R.id.right:// 完成设置
					if (babyType == UserProfile.BABY_TYPE_HUAI_YUN) {// 如果为怀孕（gravidityTime，gravidity，manifesto不为空）
						if (TextUtils.isEmpty(gravidity)) {
							Toast.makeText(BabyStateActivity.this, "请输入怀孕时间", Toast.LENGTH_SHORT).show();
							return;
						}
						if (TextUtils.isEmpty(manifesto)) {
							Toast.makeText(BabyStateActivity.this, "请输入宝宝签名", Toast.LENGTH_SHORT).show();
							return;
						}
					}
					else if (babyType == UserProfile.BABY_TYPE_CHU_SHENG) {// 出生
						if (TextUtils.isEmpty(birthday)) {
							Toast.makeText(BabyStateActivity.this, "请选择宝宝出生时间", Toast.LENGTH_SHORT).show();
							return;
						}
						if (TextUtils.isEmpty(manifesto)) {
							Toast.makeText(BabyStateActivity.this, "请输入宝宝签名", Toast.LENGTH_SHORT).show();
							return;
						}
						if (babyGender == Constants.NULL_INT) {
							Toast.makeText(BabyStateActivity.this, "请选择宝宝性别", Toast.LENGTH_SHORT).show();
							return;
						}
					}

					Intent intent = new Intent();
					if (fromActivity == REQUEST_FROM_USERCENTER) {
						// 设置完成
						intent.putExtra(UserProfile.FIELD_KEY_BABY_TYPE, babyType);
						intent.putExtra(UserProfile.FIELD_KEY_MANIFESTO, manifesto);
						intent.putExtra(UserProfile.FIELD_KEY_GRAVIDITY, gravidity);
						intent.putExtra(UserProfile.FIELD_KEY_GRAVIDITY_TIME, gravidityTime);
						intent.putExtra(UserProfile.FIELD_KEY_BIRTHDAY, birthday);
						intent.putExtra(UserProfile.FIELD_KEY_BIRTHDAY_TIME, birthdayTime);
						intent.putExtra(UserProfile.FIELD_KEY_BABY_GENDER, babyGender);
						setResult(Activity.RESULT_OK, intent);
						finish();
					}
					else {
						App.USER.setBabyType(babyType);
						App.USER.setManifesto(manifesto);
						App.USER.setGravidity(gravidity);
						App.USER.setGravidityTime(gravidityTime);
						App.USER.setBirthday(birthday);
						App.USER.setBirthdayTime(birthdayTime);
						App.USER.setBabyGender(babyGender);
						controller.updateUserInfo();
						finish();
					}
					break;

				case R.id.iv_babystate_paregent:
					// 怀孕
					babyType = UserProfile.BABY_TYPE_HUAI_YUN;
					updateBabyType();
					break;

				case R.id.iv_babystate_baby:
					// 已出生
					babyType = UserProfile.BABY_TYPE_CHU_SHENG;
					updateBabyType();
					break;

				case R.id.iv_babystate_prepare:
					// 备孕
					babyType = UserProfile.BABY_TYPE_BEI_YUN;
					updateBabyType();
					break;

				case R.id.iv_babystate_other:
					// 其他
					babyType = UserProfile.BABY_TYPE_QI_TA;
					updateBabyType();
					break;

				case R.id.positive:
					if (dialogPickDatetime != null && dialogPickDatetime.isShowing()) {
						dialogPickDatetime.getDatePickerView().clearFocus();
						try {
							Calendar target = Calendar.getInstance();
							target.set(Calendar.HOUR_OF_DAY, 0);
							target.set(Calendar.MINUTE, 0);
							target.set(Calendar.SECOND, 0);

							int type = (Integer) dialogPickDatetime.getData();

							if (type == PICK_DATETIME_PREPARE) {
								int offsetDays = CalendarTools.getOffsetDays(dialogPickDatetime.getCalendar(), target);
								// 怀孕，要设置预产期，时间最早比当前时间晚
								if (offsetDays >= 0 && offsetDays < 300) {
									// 对时间进行判断
//									gravidity = "距离宝宝出生还剩" + CalendarTools.getOffset(dialogPickDatetime.getCalendar(), target, CalendarTools.Description.NORMAL);
									gravidity = "距离宝宝出生还剩" + CalendarTools.getOffsetMounths(dialogPickDatetime.getCalendar(),target) + "个月";
									ELog.i("" + dialogPickDatetime.getString(DATE_FORMAT_TEMPLETE));
									gravidityTime = dialogPickDatetime.getString(DATE_FORMAT_TEMPLETE);
									updateGavidity();
								}
								else if (offsetDays < 0) {
									gravidity = gravidityTime = null;
									updateGavidity();
									Toast.makeText(mBaseApplication, "宝宝还未出生，请重新设置预产期时间~", Toast.LENGTH_SHORT).show();
								}
								else {
									gravidity = gravidityTime = null;
									updateGavidity();
									Toast.makeText(mBaseApplication, "怀孕时间有误，请重新设置预产期时间~", Toast.LENGTH_SHORT).show();
								}
							}
							else if (type == PICK_DATETIME_BIRTHDAY) {
								int time = CalendarTools.getOffsetDays(target, dialogPickDatetime.getCalendar());
								// 出生
								if (time > 0) {
									birthday = "宝宝已经" + CalendarTools.getOffset(target, dialogPickDatetime.getCalendar(), CalendarTools.Description.AGE) + "啦";
									birthdayTime = dialogPickDatetime.getString(DATE_FORMAT_TEMPLETE);
									updateBabyBirthday();
								}
								else {
									birthday = birthdayTime = null;
									updateBabyBirthday();
									Toast.makeText(mBaseApplication, "宝宝已出生，请设置宝宝出生时间~", Toast.LENGTH_SHORT).show();
								}
							}
						}
						catch (Exception e) {
							ELog.e("Exception:" + e.getMessage());
							e.printStackTrace();
						}

						dialogPickDatetime.dismiss();
						dialogPickDatetime = null;
					}
					break;

				case R.id.negative:
					if (dialogPickDatetime != null && dialogPickDatetime.isShowing()) {
						dialogPickDatetime.dismiss();
						dialogPickDatetime = null;
					}
					break;

				case R.id.tv_babystate_pargenttime:
					if (dialogPickDatetime == null) {
						dialogPickDatetime = new PickDatetimeDialog(v.getContext());
						dialogPickDatetime.setOnClickListener(clickListener);
						dialogPickDatetime.setMode(PickDatetimeDialog.Mode.DATE);
					}
					dialogPickDatetime.setData(PICK_DATETIME_PREPARE);
					dialogPickDatetime.show();
					break;

				case R.id.tv_babaystate_babytime:
					if (dialogPickDatetime == null) {
						dialogPickDatetime = new PickDatetimeDialog(v.getContext());
						dialogPickDatetime.setOnClickListener(clickListener);
						dialogPickDatetime.setMode(PickDatetimeDialog.Mode.DATE);
					}
					dialogPickDatetime.setData(PICK_DATETIME_BIRTHDAY);
					dialogPickDatetime.show();
					break;

				case R.id.tv_babystate_pregnant_sign:
					// 怀孕界面宝宝签名设置
					intent = new Intent(BabyStateActivity.this, UploadUserMessageActivity.class);
					intent.putExtra(UploadUserMessageActivity.UPDATE_MESSAGE_TAG, UploadUserMessageActivity.TYPE_SIGN);
					startActivityForResult(intent, REQUEST_CODE_MANIFESTO);
					break;

				case R.id.tv_babystate_babysign:
					// 怀孕界面宝宝签名设置
					intent = new Intent(BabyStateActivity.this, UploadUserMessageActivity.class);
					intent.putExtra(UploadUserMessageActivity.UPDATE_MESSAGE_TAG, UploadUserMessageActivity.TYPE_SIGN);
					startActivityForResult(intent, REQUEST_CODE_MANIFESTO);
					break;
				case R.id.rb_babystate_mam:
					babyGender = 0;
					babySexOfBoy();
					break;
				case R.id.rb_babystate_femam:
					babyGender = 1;
					babySexOfGirl();
					break;
			}
		}
	};
	private Drawable drawableBoy, drawableGirl;

	// 设置宝宝性别为女
	private void babySexOfGirl() {
		drawableBoy = getResources().getDrawable(R.drawable.setting_radiobtn);
		drawableBoy.setBounds(0, 0, drawableBoy.getMinimumWidth(), drawableBoy.getMinimumHeight());
		btnBoy.setCompoundDrawables(drawableBoy, null, null, null); // 设置左图标

		drawableGirl = getResources().getDrawable(R.drawable.setting_radiobtn_on);
		drawableGirl.setBounds(0, 0, drawableGirl.getMinimumWidth(), drawableGirl.getMinimumHeight());
		btnGirl.setCompoundDrawables(drawableGirl, null, null, null); // 设置左图标
	}

	// 设置宝宝性别为男
	private void babySexOfBoy() {
		drawableBoy = getResources().getDrawable(R.drawable.setting_radiobtn_on);
		drawableBoy.setBounds(0, 0, drawableBoy.getMinimumWidth(), drawableBoy.getMinimumHeight());
		btnBoy.setCompoundDrawables(drawableBoy, null, null, null); // 设置左图标

		drawableGirl = getResources().getDrawable(R.drawable.setting_radiobtn);
		drawableGirl.setBounds(0, 0, drawableGirl.getMinimumWidth(), drawableGirl.getMinimumHeight());
		btnGirl.setCompoundDrawables(drawableGirl, null, null, null); // 设置左图标
	}

	// 设置宝宝性别为男
	private void babySexOfDefault() {
		drawableBoy = getResources().getDrawable(R.drawable.setting_radiobtn);
		drawableBoy.setBounds(0, 0, drawableBoy.getMinimumWidth(), drawableBoy.getMinimumHeight());
		btnBoy.setCompoundDrawables(drawableBoy, null, null, null); // 设置左图标

		drawableGirl = getResources().getDrawable(R.drawable.setting_radiobtn);
		drawableGirl.setBounds(0, 0, drawableGirl.getMinimumWidth(), drawableGirl.getMinimumHeight());
		btnGirl.setCompoundDrawables(drawableGirl, null, null, null); // 设置左图标
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_MANIFESTO) {
				// 宝宝签名设置
				manifesto = data.getStringExtra(UploadUserMessageActivity.UPDATE_MESSAGE_RESULE);
				updateManifesto();
			}
		}
	};

	/**
	 * 单选按钮更换
	 * 
	 * @param group
	 * @param checkedId
	 */
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (group != null && checkedId > -1 && changeGroup == false) {
			if (group == rg1) {
				changeGroup = true;
				rg1.check(checkedId);
				rg2.clearCheck();
				changeGroup = false;
			}
			else if (group == rg2) {
				changeGroup = true;
				rg2.check(checkedId);
				rg1.clearCheck();
				changeGroup = false;
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}
}

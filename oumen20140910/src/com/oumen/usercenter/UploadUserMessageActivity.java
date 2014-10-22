package com.oumen.usercenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;

/**
 * 修改用户个人信息界面
 * 包括：1.修改昵称；
 * 2.修改宝宝签名；
 * 3.修改宝宝性别；
 * 4.修改用户性别。
 */
public class UploadUserMessageActivity extends BaseActivity {
	public static final String UPDATE_MESSAGE_TAG = "UploadUserMessageActivity";
	public static final String UPDATE_MESSAGE_RESULE = "upload_msg_result";

	public static final int TYPE_NICKNAME = 1;
	public static final int TYPE_SIGN = 2;
	public static final int TYPE_BABY_SEX = 3;
	public static final int TYPE_USER_SEX = 4;
	//标题行
	private TitleBar titlebar;
	private TextView title;
	private Button btnLeft, btnRight;

	private EditText etNickName, etSign;

	private LinearLayout llSex;
	private TextView tvBoy, tvGirl;

	private int currentType;

	private int sex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_usercenter);
		init();
		currentType = getIntent().getIntExtra(UPDATE_MESSAGE_TAG, App.INT_UNSET);
		if (currentType != App.INT_UNSET) {
			changeView(currentType);
		}

	}

	private void init() {
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		title = titlebar.getTitle();
		btnLeft = titlebar.getLeftButton();
		btnRight = titlebar.getRightButton();
		btnRight.setText(R.string.submit);

		etNickName = (EditText) findViewById(R.id.nickname);
		etSign = (EditText) findViewById(R.id.sign);

		llSex = (LinearLayout) findViewById(R.id.sex);
		tvBoy = (TextView) findViewById(R.id.boy);
		tvGirl = (TextView) findViewById(R.id.girl);

		btnLeft.setOnClickListener(clickListener);
		btnRight.setOnClickListener(clickListener);
		tvBoy.setOnClickListener(clickListener);
		tvGirl.setOnClickListener(clickListener);
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
			else if (v == btnRight) {
				// TODO 提交
				Intent i = new Intent();
				switch (currentType) {
					case TYPE_NICKNAME:
						if (TextUtils.isEmpty(etNickName.getText().toString())) {
							Toast.makeText(UploadUserMessageActivity.this, "请输入昵称", Toast.LENGTH_SHORT).show();
							return;
						}
						if (!etNickName.getText().toString().matches("^[\u4e00-\u9fa5A-Za-z0-9]{1,8}$")) {
							Toast.makeText(UploadUserMessageActivity.this, "昵称由汉字、英文、数字组成，不能超过6个字符", Toast.LENGTH_SHORT).show();
							return;
						}
						i.putExtra(UPDATE_MESSAGE_RESULE, etNickName.getText().toString());
						break;

					case TYPE_SIGN:
						if (TextUtils.isEmpty(etSign.getText().toString().trim())) {
							Toast.makeText(UploadUserMessageActivity.this, "请输入宝宝签名", Toast.LENGTH_SHORT).show();
							return;
						}
						if (etSign.getText().toString().trim().length() > 38) {
							Toast.makeText(UploadUserMessageActivity.this, "宝宝签名不能超过38个字符", Toast.LENGTH_SHORT).show();
							return;
						}
						i.putExtra(UPDATE_MESSAGE_RESULE, etSign.getText().toString());
						break;

					case TYPE_BABY_SEX:
						i.putExtra(UPDATE_MESSAGE_RESULE, sex);
						break;
					case TYPE_USER_SEX:
						i.putExtra(UPDATE_MESSAGE_RESULE, sex);
						break;
				}
				setResult(Activity.RESULT_OK, i);
				finish();
			}
			else if (v == tvBoy) {
				sex = 0;
				changeSex(sex);
			}
			else if (v == tvGirl) {
				sex = 1;
				changeSex(sex);
			}
		}
	};

	/**
	 * 显示三个界面中的哪个界面
	 * 
	 * @param type
	 */
	private void changeView(int type) {
		switch (type) {
			case TYPE_NICKNAME:
				etNickName.setVisibility(View.VISIBLE);
				etSign.setVisibility(View.GONE);
				llSex.setVisibility(View.GONE);
				title.setText(R.string.change_nickname);
				etNickName.setText(App.USER.getNickname());
				break;

			case TYPE_SIGN:
				etNickName.setVisibility(View.GONE);
				etSign.setVisibility(View.VISIBLE);
				llSex.setVisibility(View.GONE);
				title.setText(R.string.baby_description);
				etSign.setText(App.USER.getManifesto());
				break;

			case TYPE_BABY_SEX:
				etNickName.setVisibility(View.GONE);
				etSign.setVisibility(View.GONE);
				llSex.setVisibility(View.VISIBLE);
				title.setText(R.string.center_baby_sex);
				changeSex(App.USER.getBabyGender());
				break;
			case TYPE_USER_SEX:
				etNickName.setVisibility(View.GONE);
				etSign.setVisibility(View.GONE);
				llSex.setVisibility(View.VISIBLE);
				title.setText(R.string.user_sex);
				changeSex(App.USER.getUserGender());
				break;
		}
	}

	private void changeSex(int sex) {
		switch (sex) {
			case 0:// 男
				tvBoy.setBackgroundColor(getResources().getColor(R.color.white));
				tvGirl.setBackgroundColor(getResources().getColor(R.color.default_bg_up));

				tvBoy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.oumen_share_choose, 0, 0, 0);
				tvGirl.setCompoundDrawablesWithIntrinsicBounds(R.drawable.oumen_share_unchoose, 0, 0, 0);

				tvBoy.setTextColor(getResources().getColor(R.color.text_highlight));
				tvGirl.setTextColor(getResources().getColor(R.color.default_text_bg));
				break;

			case 1:// 女
				tvBoy.setBackgroundColor(getResources().getColor(R.color.default_bg_up));
				tvGirl.setBackgroundColor(getResources().getColor(R.color.white));

				tvBoy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.oumen_share_unchoose, 0, 0, 0);
				tvGirl.setCompoundDrawablesWithIntrinsicBounds(R.drawable.oumen_share_choose, 0, 0, 0);

				tvBoy.setTextColor(getResources().getColor(R.color.default_text_bg));
				tvGirl.setTextColor(getResources().getColor(R.color.text_highlight));
				break;
		}
	}
}

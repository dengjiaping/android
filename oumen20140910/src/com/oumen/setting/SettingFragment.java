package com.oumen.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;

/**
 * 设置界面
 * 
 */
public class SettingFragment extends BaseFragment {

	private TitleBar titlebar;
	private Button btnLeft;

	private Button btnMsgSwitch, btnSoundSwitch, btnShakeSwitch;
	private FrameLayout aboutContainer;
	
	private SettingActivity host;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		host = (SettingActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.setting_fragment, container, false);
		
		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		titlebar.getTitle().setText(R.string.setting);
		titlebar.getRightButton().setVisibility(View.GONE);
		btnLeft = titlebar.getLeftButton();

		btnMsgSwitch = (Button) view.findViewById(R.id.accept_switch);
		btnSoundSwitch = (Button) view.findViewById(R.id.sound_switch);
		btnShakeSwitch = (Button) view.findViewById(R.id.shake_switch);

		aboutContainer = (FrameLayout) view.findViewById(R.id.about_container);

		btnLeft.setOnClickListener(clickListener);
		btnMsgSwitch.setOnClickListener(clickListener);
		btnSoundSwitch.setOnClickListener(clickListener);
		btnShakeSwitch.setOnClickListener(clickListener);
		aboutContainer.setOnClickListener(clickListener);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		switchAcceptMsg();
		switchSound();
		switchShake();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	private void switchAcceptMsg() {
		if(App.PREFS.isAcceptMsg()) {
			btnMsgSwitch.setBackgroundResource(R.drawable.apply_msg_on);
		}
		else {
			btnMsgSwitch.setBackgroundResource(R.drawable.apply_msg_off);
		}
	}
	
	private void switchSound() {
		if (App.PREFS.isSoundOpen()) {
			btnSoundSwitch.setBackgroundResource(R.drawable.apply_msg_on);
		}
		else {
			btnSoundSwitch.setBackgroundResource(R.drawable.apply_msg_off);
		}
	}
	
	private void switchShake() {
		if (App.PREFS.isShakeOpen()) {
			btnShakeSwitch.setBackgroundResource(R.drawable.apply_msg_on);
		}
		else {
			btnShakeSwitch.setBackgroundResource(R.drawable.apply_msg_off);
		}
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				getActivity().finish();
			}
			else if (v == btnMsgSwitch) {// 是否接受消息
				App.PREFS.setAcceptMsg(App.PREFS.isAcceptMsg()? false : true);
				switchAcceptMsg();
			}
			else if (v == btnSoundSwitch) {// 声音提醒
				App.PREFS.setSoundOpen(App.PREFS.isSoundOpen() ? false : true);
				switchSound();
			}
			else if (v == btnShakeSwitch) {// 震动提醒
				App.PREFS.setShakeOpen(App.PREFS.isShakeOpen() ? false : true);
				switchShake();
			}
			else if (v == aboutContainer) {// 关于偶们
				host.switchFragment(host.FRAGMENT_ABOUT);
			}
		}
	};
}

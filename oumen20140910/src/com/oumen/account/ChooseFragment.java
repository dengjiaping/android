package com.oumen.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.oumen.MainActivity;
import com.oumen.MainActivity.Frag;
import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.message.connection.MessageConnection;

public class ChooseFragment extends BaseFragment implements View.OnClickListener {
	private TextView txtLogin, txtRegister;
	private TitleBar titlebar;
	private Button btnBack;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.choose_login, container, false);
		txtLogin = (TextView) view.findViewById(R.id.login);
		txtRegister = (TextView) view.findViewById(R.id.register);
		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		titlebar.getRightButton().setVisibility(View.GONE);
		titlebar.setBackgroundTransparent();
		btnBack = titlebar.getLeftButton();

		txtLogin.setOnClickListener(this);
		txtRegister.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		
		if (!MessageConnection.instance.isConnected()) {
			btnBack.setVisibility(View.GONE);
		}
		else {
			btnBack.setVisibility(View.VISIBLE);
		}
		return view;
	}

	@Override
	public boolean onBackPressed() {
		if (LoginTask.hadLogin()) {
			MainActivity host = (MainActivity) getActivity();
			host.switchFragment(Frag.HOME);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		MainActivity host = (MainActivity) getActivity();
		if (v == txtLogin) {
			host.switchFragment(Frag.ACCOUNT, AccountFragment.FRAGMENT_LOGIN);
		}
		else if (v == txtRegister) {
			host.switchFragment(Frag.ACCOUNT, AccountFragment.FRAGMENT_REGISTER);
		}
		else if (v == btnBack) {
			if (LoginTask.hadLogin()) {
				host.switchFragment(Frag.HOME);
			}
			else {
				getActivity().finish();
			}
		}
	}
}

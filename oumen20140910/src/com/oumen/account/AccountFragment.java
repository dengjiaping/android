package com.oumen.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.oumen.MainActivity;
import com.oumen.MainActivity.Frag;
import com.oumen.R;
import com.oumen.android.BaseFragment;
import com.oumen.home.HomeFragment;

public class AccountFragment extends BaseFragment {
	public static final String KEY_FRAGMENT = "frag";
	
	public static final int FRAGMENT_HOME = -1;
	public static final int FRAGMENT_CHOOSE = 0;
	public static final int FRAGMENT_LOGIN = 1;
	public static final int FRAGMENT_FIND_PASSWORD = 2;
	public static final int FRAGMENT_REGISTER = 3;
	public static final int FRAGMENT_DOCUMENT = 4;
	public static final int FRAGMENT_PHONE_FIND_PASSWORD = 5;
	
	ChooseFragment fragChoose = new ChooseFragment();
	
	LoginFragment fragLogin = new LoginFragment();
	FindPasswordFragment fragFindPassword = new FindPasswordFragment();
	PhoneFindPwdFragment fragPhoneFindPassword = new PhoneFindPwdFragment();
	
//	RegisterFragment fragRegister = new RegisterFragment();
	TwoTypeRegisterFragment fragRegister = new TwoTypeRegisterFragment();
	DocumentFragment fragDocument = new DocumentFragment();
	
	MainActivity host;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		host = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FrameLayout root = new FrameLayout(container.getContext());
		root.setId(R.id.fragment_container);
		return root;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		FragmentManager manager = getFragmentManager();
		Fragment current = manager.findFragmentById(R.id.fragment_container);

		if (current != null) {
			current.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Bundle args = getArguments();
		if (args != null && args.containsKey(KEY_FRAGMENT)) {
			int frag = args.getInt(KEY_FRAGMENT);
			switchFragment(frag);
		}
		else {
			switchFragment(FRAGMENT_CHOOSE);
		}
	}

	public void switchFragment(int frag, Object... params) {
		FragmentManager manager = getFragmentManager();
		Bundle args;
		switch (frag) {
			case FRAGMENT_CHOOSE:
				manager.beginTransaction()
					.setCustomAnimations(R.anim.screen_right_in, R.anim.fade_out, R.anim.fade_in, R.anim.screen_right_out)
					.replace(R.id.fragment_container, fragChoose).commitAllowingStateLoss();
				break;
				
			case FRAGMENT_LOGIN:
				manager.beginTransaction()
					.setCustomAnimations(R.anim.screen_right_in, R.anim.fade_out, R.anim.fade_in, R.anim.screen_right_out)
					.addToBackStack("login")
					.replace(R.id.fragment_container, fragLogin).commit();
				break;
				
			case FRAGMENT_FIND_PASSWORD:
				manager.beginTransaction()
					.setCustomAnimations(R.anim.screen_right_in, R.anim.fade_out, R.anim.fade_in, R.anim.screen_right_out)
					.addToBackStack("password")
					.replace(R.id.fragment_container, fragFindPassword).commit();
				break;
				
			case FRAGMENT_PHONE_FIND_PASSWORD:
				manager.beginTransaction()
				.setCustomAnimations(R.anim.screen_right_in, R.anim.fade_out, R.anim.fade_in, R.anim.screen_right_out)
				.addToBackStack("password")
				.replace(R.id.fragment_container, fragPhoneFindPassword).commit();
				break;
				
			case FRAGMENT_REGISTER:
				manager.beginTransaction()
					.setCustomAnimations(R.anim.screen_right_in, R.anim.fade_out, R.anim.fade_in, R.anim.screen_right_out)
					.addToBackStack("register")
					.replace(R.id.fragment_container, fragRegister).commit();
				break;
				
			case FRAGMENT_DOCUMENT:
				args = new Bundle();
				args.putInt(DocumentFragment.KEY_CONTENT, (Integer)params[0]);
				fragDocument.setArguments(args);
				manager.beginTransaction()
					.setCustomAnimations(R.anim.screen_right_in, R.anim.fade_out, R.anim.fade_in, R.anim.screen_right_out)
					.addToBackStack("document")
					.replace(R.id.fragment_container, fragDocument).commit();
				break;
				
			case FRAGMENT_HOME:
				host.switchFragment(Frag.HOME, HomeFragment.TYPE_ACTIVITY);
				break;
		}
	}

	@Override
	public boolean onBackPressed() {
		BaseFragment current = (BaseFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
		if (current != null) {
			boolean process = current.onBackPressed();
			if (process) {
				return true;
			}
			
			if (LoginTask.hadLogin()) {
				MainActivity host = (MainActivity) getActivity();
				host.switchFragment(Frag.HOME);
				return true;
			}
		}
		return false;
	}
}

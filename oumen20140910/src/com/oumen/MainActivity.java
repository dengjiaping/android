package com.oumen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.FrameLayout;

import com.oumen.account.AccountFragment;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.home.HomeFragment;
import com.oumen.tools.ELog;

/**
 * 主界面（包括闪屏，账户（登录注册）界面，主界面）
 */
public class MainActivity extends FragmentActivity {
	public static final int DEFAULE_HOME = HomeFragment.TYPE_MESSAGE;
	public static final String INTENT_KEY_CURRENT_FRAGMENT = "currentfragment";
	public static final String TYPE = "hometype";

	public enum Frag {
		SPLASH, ACCOUNT, HOME
	}

	private FrameLayout fragContainer;

	private final MainController controller = new MainController(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ELog.e("");

		fragContainer = new FrameLayout(this);
		fragContainer.setId(R.id.container);
		fragContainer.setBackgroundColor(Color.TRANSPARENT);
		setContentView(fragContainer);

		controller.onCreate(savedInstanceState);
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FragmentManager manager = getSupportFragmentManager();
		Fragment current = manager.findFragmentById(R.id.container);

		if (current != null) {
			current.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		ELog.i("");
		controller.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		BaseFragment current = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.container);
		if (current == null || !current.onBackPressed()) {
			ELog.e("Finish");
			finish();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		ELog.i("");
		Frag current = (Frag) intent.getSerializableExtra(INTENT_KEY_CURRENT_FRAGMENT);
		if (Frag.HOME.equals(current)) {//一般从通知栏过来
			int type = intent.getIntExtra(TYPE, HomeFragment.TYPE_MESSAGE);
			switchFragment(Frag.HOME, type);
		}
		else if (Frag.SPLASH.equals(current)) {//一般从注销过来
			switchFragment(Frag.SPLASH);
		}
		else if (Frag.ACCOUNT.equals(current)) {
			switchFragment(Frag.ACCOUNT);
		}
		else {
			switchFragment(Frag.HOME);
		}
	}

	public void switchFragment(Frag frag, final Object... params) {
		ELog.w("Current:" + frag);

		FragmentManager manager = getSupportFragmentManager();
		Fragment current = manager.findFragmentById(R.id.container);

		if (Frag.SPLASH.equals(frag)) {
			if (!(current instanceof SplashFragment)) {
				current = new SplashFragment();
				manager.beginTransaction().replace(R.id.container, current).commit();
			}
		}
		else if (Frag.ACCOUNT.equals(frag)) {
			if (current instanceof AccountFragment) {
				AccountFragment account = (AccountFragment) current;
				if (params != null && params.length > 0) {
					account.switchFragment((Integer) params[0]);
				}
				else {
					account.switchFragment(AccountFragment.FRAGMENT_CHOOSE);
				}
			}
			else {
				current = new AccountFragment();
				if (params != null && params.length > 0) {
					Bundle args = new Bundle();
					args.putInt(AccountFragment.KEY_FRAGMENT, (Integer) params[0]);
					current.setArguments(args);
				}
				manager.beginTransaction().replace(R.id.container, current).commitAllowingStateLoss();
			}
		}
		else {
			if (current instanceof SplashFragment) {
				App.THREAD.execute(new Runnable() {

					@Override
					public void run() {
						ELog.i("Wait splash");
						SplashFragment splash = (SplashFragment) getSupportFragmentManager().findFragmentById(R.id.container);
						splash.close();

						controller.handler.post(new Runnable() {

							@Override
							public void run() {
//								if (isDestroyed()) return;
								toHome(params);
							}
						});
					}
				});
			}
			else {
				toHome(params);
			}
		}
	}

	private void toHome(Object... params) {
		Fragment current = (Fragment) getSupportFragmentManager().findFragmentById(R.id.container);
		if (current instanceof HomeFragment) {
			HomeFragment home = (HomeFragment) current;
			if (params != null && params.length > 0) {
				home.setCurrentFragment((Integer) params[0]);
			}
			home.switchFragments();
		}
		else {
			HomeFragment home = new HomeFragment();
			if (params != null && params.length > 0) {
				Bundle bundle = new Bundle();
				bundle.putInt(HomeFragment.STATE_CURRENT_FRAGMENT, (Integer) params[0]);
				home.setArguments(bundle);
			}
			getSupportFragmentManager().beginTransaction().replace(R.id.container, home).commitAllowingStateLoss();
		}
	}

	protected Fragment getCurrentFragment() {
		return getSupportFragmentManager().findFragmentById(R.id.container);
	}
}

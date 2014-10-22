package com.oumen.activity.search;

import com.oumen.R;
import com.oumen.android.BaseFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class SearchActivity extends FragmentActivity {
	public static final int FRAGMENT_TYPE_SEARCH = 1;
	public static final int FRAGMENT_TYPE_RESULT = 2;
	private SearchFragment fragSearch;
	private SearchResultFragment fragResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_container);
		switchFragment(FRAGMENT_TYPE_SEARCH);
	}

	public void switchFragment(int type) {
		switch (type) {
			case FRAGMENT_TYPE_SEARCH:
				if (fragSearch == null) {
					fragSearch = new SearchFragment();
				}
				getSupportFragmentManager().beginTransaction().replace(R.id.circle_container, fragSearch).addToBackStack(null).commit();
				break;

			case FRAGMENT_TYPE_RESULT:
				if (fragResult == null) {
					fragResult = new SearchResultFragment();
				}
				getSupportFragmentManager().beginTransaction().replace(R.id.circle_container, fragResult).addToBackStack(null).commit();
				break;
		}
	}

	@Override
	public void onBackPressed() {
		BaseFragment current = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.circle_container);
		if (current != null) {
			boolean processed = current.onBackPressed();
			if (processed) {
				return;
			}
		}
		finish();
	}
}

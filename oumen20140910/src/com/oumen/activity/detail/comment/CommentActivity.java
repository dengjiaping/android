package com.oumen.activity.detail.comment;

import com.oumen.R;
import com.oumen.android.App;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
/**
 * 活动评论界面
 * @author oumen-xin.zhang
 *
 */
public class CommentActivity extends FragmentActivity {
	public static final String INTENT_HUODONG_ID = "huodong_id";
	public static final String INTENT_HUODONG_APPLY = "huodng_is_apply";
	
	public static final int FRAGMENT_COMMENT_LIST = 1;
	public static final int FRAGMENT_COMMENT_PUBLISH = 2;
	
	private CommentListFragment fragList;
	private PublishCommentFragment fragPublish;
	
	private int atId;
	private boolean isApply;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_container);
		atId = getIntent().getIntExtra(INTENT_HUODONG_ID, App.INT_UNSET);
		isApply = getIntent().getBooleanExtra(INTENT_HUODONG_APPLY, false);
		switchFragment(FRAGMENT_COMMENT_LIST);
	}
	
	public void switchFragment(int type) {
		FragmentManager manager = getSupportFragmentManager();
		switch (type) {
			case FRAGMENT_COMMENT_LIST:
				if (fragList == null) {
					fragList = new CommentListFragment();
				}
				
				Bundle bundle = new Bundle();
				bundle.putInt(INTENT_HUODONG_ID, atId);
				bundle.putBoolean(INTENT_HUODONG_APPLY, isApply);
				fragList.setArguments(bundle);
				
				manager.beginTransaction().replace(R.id.circle_container, fragList).addToBackStack(null).commit();
				break;

			case FRAGMENT_COMMENT_PUBLISH:
				if (fragPublish == null) {
					fragPublish = new PublishCommentFragment();
				}

				Bundle bundle1 = new Bundle();
				bundle1.putInt(INTENT_HUODONG_ID, atId);
				fragPublish.setArguments(bundle1);
				
				manager.beginTransaction().replace(R.id.circle_container, fragPublish).addToBackStack(null).commit();
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
}

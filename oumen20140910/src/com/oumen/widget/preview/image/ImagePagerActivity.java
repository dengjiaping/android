package com.oumen.widget.preview.image;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.oumen.R;
import com.oumen.app.callback.CompleteCallback;

public class ImagePagerActivity extends FragmentActivity {
	public static final String ACTIVITY_RESULT_DATA = "data";
	
	private ImagePagerFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null) {
			savedInstanceState = getIntent().getExtras();
		}
		
		FrameLayout root = new FrameLayout(this);
		root.setBackgroundColor(Color.BLACK);
		root.setId(R.id.container);
		setContentView(root);
		
		fragment = new ImagePagerFragment();
		fragment.setArguments(savedInstanceState);
		fragment.setCompleteCallback(imagePagercallback);
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.container, fragment).commit();
	}
	
	private CompleteCallback imagePagercallback = new CompleteCallback() {
		
		@Override
		public void onComplete(Object host, Object data) {
			Intent intent = new Intent();
			intent.putExtra(ACTIVITY_RESULT_DATA, (int[])data);
			setResult(RESULT_OK, intent);
			finish();
		}
	};
}

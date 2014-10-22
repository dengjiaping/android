package com.oumen;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.FrameLayout;

import com.oumen.mv.PickPrefixVideoFragment;

public class TestActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FrameLayout root = new FrameLayout(this);
		root.setId(R.id.fragment_container);
		setContentView(root);
		
		PickPrefixVideoFragment frag = new PickPrefixVideoFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frag).commit();
	}

}

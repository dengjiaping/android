package com.oumen.widget.image;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.oumen.R;
import com.oumen.widget.preview.image.ImagePagerFragment;

public class ImagePreviewActivity extends FragmentActivity {
	public static final String INTENT_KEY_DATA = "data";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.container);
		
		ImagePagerFragment fragImagePager = new ImagePagerFragment();
		fragImagePager.setArguments(getIntent().getBundleExtra(INTENT_KEY_DATA));
		getSupportFragmentManager().beginTransaction().replace(R.id.container, fragImagePager).commit();
	}
}

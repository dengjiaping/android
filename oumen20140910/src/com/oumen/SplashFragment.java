package com.oumen;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.oumen.widget.dialog.TwoButtonDialog;

public class SplashFragment extends BaseFragment implements View.OnClickListener {
	private final Handler handler = new Handler();
	private final int WAIT_DEFALUT = 2000;
	private final int WAIT_SPLASH_IMAGE = 3000;
	
	private long closeTime;
	
	private TwoButtonDialog dialog;
	
	private ImageView img;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ELog.i("");
		closeTime = System.currentTimeMillis() + WAIT_DEFALUT;
		
		img = new ImageView(container.getContext());
		img.setScaleType(ScaleType.FIT_XY);
		img.setImageResource(R.drawable.splash);
		return img;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			if (dialog == null) {
				dialog = new TwoButtonDialog(getActivity());
				dialog.setCancelable(false);
				dialog.getMessageView().setText(R.string.err_network_invalid);
				dialog.getLeftButton().setOnClickListener(this);
				dialog.getLeftButton().setText(R.string.exit);
				dialog.getRightButton().setOnClickListener(this);
				dialog.getRightButton().setText(R.string.setting);
			}
			dialog.show();
		}
		else {
			File splashImageFile = new File(App.PATH_SPLASH_IMAGE);
			if (splashImageFile.exists()) {
				closeTime += WAIT_SPLASH_IMAGE;
				
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						img.setImageBitmap(ImageTools.decodeSourceFile(App.PATH_SPLASH_IMAGE));
					}
				}, WAIT_DEFALUT);
			}
		}
	}

	public void close() {
		long now = System.currentTimeMillis();
		if (now < closeTime) {
			try {
				TimeUnit.MILLISECONDS.sleep(closeTime - now);
			}
			catch (Exception e) {
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_left) {
			dialog.dismiss();
			App.destory();
		}
		else if (id == R.id.btn_right) {
			dialog.dismiss();
			v.getContext().startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
		}
	}
}

package com.oumen.setting;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.home.CheckVersion;
/**
 * 关于偶们
 *
 */
public class AboutOumenFragment extends BaseFragment {
	private TitleBar titlebar;
	private Button btnLeft;
	
	private TextView version;
	private SettingActivity host;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		host = (SettingActivity) getActivity();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.about_oumen_fragment, container, false);
		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		titlebar.getRightButton().setVisibility(View.GONE);
//		titlebar.getTitle().setText("关于我们");
		titlebar.getTitle().setVisibility(View.GONE);
		btnLeft = titlebar.getLeftButton();
		version = (TextView) view.findViewById(R.id.version);
		
		btnLeft.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				host.switchFragment(host.FRAGMENT_SETTING);
			}
		});
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		try {
			String v = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
			if(v != null) {
				version.setText(v);
			}
		}
		catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		//版本检测
		if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
			new CheckVersion(getActivity()).checkCurrentVersion();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}

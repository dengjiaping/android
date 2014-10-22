package com.oumen.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.BaseFragment;

public class DocumentFragment extends BaseFragment {
	public static final String KEY_CONTENT = "content";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int content = getArguments().getInt(KEY_CONTENT);
		
		View view = inflater.inflate(R.layout.serveritem, container, false);

		TitleBar barTitle = (TitleBar) view.findViewById(R.id.titlebar);
		barTitle.getRightButton().setVisibility(View.GONE);
		barTitle.getTitle().setText(content == R.string.serveritem ? "隐私政策" : "服务条款");
		
		TextView txtContent = (TextView) view.findViewById(R.id.tv_serveritem);
		txtContent.setText(content);
		
		barTitle.getLeftButton().setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				AccountFragment host = (AccountFragment) getFragmentManager().findFragmentById(R.id.container);
//				host.switchFragment(AccountFragment.FRAGMENT_REGISTER);
				getFragmentManager().popBackStack();
			}
		});
		return view;
	}

	@Override
	public boolean onBackPressed() {
		getFragmentManager().popBackStack();
		return true;
	}
}

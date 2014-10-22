package com.oumen.mv;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.oumen.R;
import com.oumen.android.BaseFragment;

public class ComposeFinalFragment extends BaseFragment {
	private MvActivity host;
	
	private VideoPlayerView playerView;
	private EditText edtTitle;
	private Button btnFinish;
	
	private View.OnClickListener listener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		host = (MvActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.mv_compose_final, null);
		
		int width = getResources().getDisplayMetrics().widthPixels - getResources().getDimensionPixelSize(R.dimen.padding_medium) * 2;
		int height = width * 3 / 4;
		
		playerView = (VideoPlayerView) root.findViewById(R.id.player);
		playerView.setVideo(host.previewFile.getAbsolutePath(), host.data.prefix.coverFile.getAbsolutePath());
		playerView.setWidthAndHeight(width, height);
		
		edtTitle = (EditText) root.findViewById(R.id.nav_title);
		edtTitle.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				String tmp = s.toString();
				tmp = tmp.replaceAll("[\\.\\/\n,，{}\\[\\]?!？！。%=@]?", "");
				if (tmp.length() != s.length()) {
					edtTitle.setText(tmp);
					edtTitle.setSelection(tmp.length());
				}
			}
		});
		
		btnFinish = (Button) root.findViewById(R.id.ok);
		btnFinish.setOnClickListener(listener);
		
		return root;
	}
	
	public String getTitle() {
		return edtTitle.getText().toString();
	}

	public void setOnClickListener(View.OnClickListener listener) {
		this.listener = listener;
	}
}

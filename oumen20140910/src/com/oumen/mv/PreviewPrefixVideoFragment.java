package com.oumen.mv;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oumen.R;
import com.oumen.android.BaseFragment;
import com.oumen.mv.MvActivity.FragmentType;

public class PreviewPrefixVideoFragment extends BaseFragment implements FragmentInfo {
	private MvActivity host;
	
	private View.OnClickListener clickListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		host = (MvActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.mv_preview_prefix_video, null);
		
		int padding = getResources().getDimensionPixelSize(R.dimen.padding_medium);
		int width = getResources().getDisplayMetrics().widthPixels - padding * 2;
		int height = width * 3 / 4;
		
		VideoPlayerView player = (VideoPlayerView) root.findViewById(R.id.player);
		player.setVideo(host.data.prefix.videoFile.getAbsolutePath(), MvHelper.getCoverPath(PrefixVideo.obtainTitle(host.data.prefix.id)));
		player.setWidthAndHeight(width, height);
		
		View btn = root.findViewById(R.id.ok);
		btn.setOnClickListener(clickListener);
		
		return root;
	}
	
	public void setOnClickListener(View.OnClickListener listener) {
		clickListener = listener;
	}

	@Override
	public FragmentType getFragmentType() {
		return FragmentType.PREVIEW_PREFIX_VIDEO;
	}
}

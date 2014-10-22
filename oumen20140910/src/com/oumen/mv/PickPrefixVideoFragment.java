package com.oumen.mv;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.mv.MvActivity.FragmentType;

public class PickPrefixVideoFragment extends BaseFragment implements FragmentInfo {
	
	private PickPrefixVIdeoController controller = new PickPrefixVIdeoController(this);
	
	ExpandableListView list;
	View progressContainer;
	
//	private MvActivity host;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		host = (MvActivity) getActivity();
		
		App.THREAD.execute(new Runnable() {
			
			@Override
			public void run() {
				MvHelper.installSuffixVideo(getActivity());

				controller.obtainList();
			}
		});

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.pick_prefix_video, null);
		
		progressContainer = root.findViewById(R.id.progress_container);
		if (!controller.adapter.dataSource.isEmpty()) {
			progressContainer.setVisibility(View.GONE);
		}
		
		list = (ExpandableListView) root.findViewById(R.id.list);
		list.setAdapter(controller.adapter);
		
		return root;
	}

	@Override
	public void onDestroyView() {
		controller.onDestroyView();
		super.onDestroyView();
	}
	
	public PrefixVideo getSelect() {
		return controller.selected;
	}

	@Override
	public FragmentType getFragmentType() {
		return FragmentType.PICK_PREFIX_VIDEO;
	}
}

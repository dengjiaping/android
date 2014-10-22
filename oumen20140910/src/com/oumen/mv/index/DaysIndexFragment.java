package com.oumen.mv.index;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.oumen.android.BaseFragment;
import com.oumen.mv.ComposeSelectDialog;
import com.oumen.tools.ELog;

public class DaysIndexFragment extends BaseFragment {
	private final DaysIndexController controller = new DaysIndexController(this);
	
	ListView listView;
	ComposeSelectDialog dialogComposeSelect;
	
	private MVListHeader header;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ELog.i("");
		
		controller.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ELog.i("");
		dialogComposeSelect = new ComposeSelectDialog(getActivity());
		dialogComposeSelect.setClickListener(controller);
		
		header = new MVListHeader(container.getContext());
		header.update();
		
		listView = new ListView(container.getContext());
		listView.addHeaderView(header);
		listView.setDividerHeight(0);
		listView.setAdapter(controller.adapterList);
		
		return listView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		controller.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		controller.onStart();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		controller.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDestroyView() {
		controller.onDestroyView();
		super.onDestroyView();
	}
}

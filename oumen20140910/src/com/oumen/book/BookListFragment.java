package com.oumen.book;

import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;

public class BookListFragment extends BaseFragment {
	protected final BookListAdapter adapter = new BookListAdapter();
	private ListView lstView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		List<BookMessage> tmp = BookMessage.query(App.PREFS.getUid(), App.DB);
		adapter.data.addAll(tmp);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (lstView == null) {
			int padding = getResources().getDimensionPixelSize(R.dimen.padding_medium);
			
			lstView = new ListView(container.getContext());
			lstView.setCacheColorHint(Color.TRANSPARENT);
			lstView.setSelector(android.R.color.transparent);
			lstView.setFastScrollEnabled(false);
			lstView.setPadding(padding, padding, padding, padding);
			lstView.setAdapter(adapter);
			lstView.setSelection(lstView.getBottom());
		}
		return lstView;
	}

	@Override
	public void onDestroyView() {
		ViewGroup parent = (ViewGroup) lstView.getParent();
		parent.removeAllViews();
		super.onDestroyView();
	}

}

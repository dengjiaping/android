package com.oumen.book;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.oumen.android.BaseFragment;

@SuppressLint("SetJavaScriptEnabled")
public class BookDetailFragment extends BaseFragment {
	private WebView web;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (web == null) {
			web = new WebView(container.getContext());
			web.getSettings().setJavaScriptEnabled(true);
			web.getSettings().setAllowFileAccess(true);
			web.clearCache(true);
		}
		return web;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		String url = savedInstanceState.getString(BookMessage.KEY_URL);
		web.loadUrl(url);
	}

	@Override
	public void onDestroyView() {
		ViewGroup parent = (ViewGroup) web.getParent();
		parent.removeAllViews();
		super.onDestroyView();
	}
}

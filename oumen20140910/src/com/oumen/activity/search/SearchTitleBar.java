package com.oumen.activity.search;

import com.oumen.R;
import com.oumen.widget.editview.ClearEditText;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class SearchTitleBar extends RelativeLayout {
	protected ClearEditText txtTitle;
	protected Button btnLeft;
	protected Button txtRight;

	public SearchTitleBar(Context context) {
		this(context, null, 0);
	}

	public SearchTitleBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SearchTitleBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.search_titlebar, this, true);
		
		btnLeft = (Button) findViewById(R.id.left);
		txtRight = (Button) findViewById(R.id.right);
		txtTitle = (ClearEditText) findViewById(R.id.title_input);
	}

	public Button getLeftButton() {
		return btnLeft;
	}
	
	public Button getRightButton() {
		return txtRight;
	}
	
	public ClearEditText getTitle() {
		return txtTitle;
	}
}

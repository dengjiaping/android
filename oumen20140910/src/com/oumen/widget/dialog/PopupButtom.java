package com.oumen.widget.dialog;

import com.oumen.R;
import com.oumen.home.FloatViewController;
import com.oumen.home.FloatViewHostController;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class PopupButtom extends LinearLayout implements FloatViewController, View.OnClickListener {
	protected boolean showing;
	protected boolean playing;
	
	protected FloatViewHostController host;
	
	protected View root;
	protected Button top, middle, cancel;

	public PopupButtom(Context context) {
		this(context, null, 0);
	}

	public PopupButtom(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PopupButtom(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		root = inflater.inflate(R.layout.popup_buttom, null);
		
		top = (Button) root.findViewById(R.id.top_button);
		middle = (Button) root.findViewById(R.id.buttom_button);
		cancel = (Button) root.findViewById(R.id.cancel_button);
		
		cancel.setOnClickListener(this);
	}
	
	public void setOnClickListener(View.OnClickListener clickListener) {
		top.setOnClickListener(clickListener);
		middle.setOnClickListener(clickListener);
	}
	
	@Override
	public boolean isShowing() {
		return showing;
	}

	@Override
	public boolean isPlaying() {
		return playing;
	}

	@Override
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	@Override
	public View getRoot() {
		return root;
	}


	@Override
	public View show() {
		showing = true;
		playing = true;
		return root;
	}

	@Override
	public View hide() {
		showing = false;
		playing = true;
		return root;
	}

	public FloatViewHostController getHost() {
		return host;
	}

	public void setHost(FloatViewHostController host) {
		this.host = host;
	}

	@Override
	public void onClick(View v) {
		if (v == cancel) {
			host.hideFloatView();
		}
	}
}

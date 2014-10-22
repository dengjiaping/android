package com.oumen.mv;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.widget.VerticalProgressBar;

public class PrefixVideoChildItem extends RelativeLayout {
	protected ImageView imgCover;
	protected VerticalProgressBar pgs;
	protected TextView txtTitle;
	protected TextView txtDescription;
	
	protected View viewActionContainer;
	protected TextView txtAction;
	protected TextView txtActionDescription;
	
	protected PrefixVideo data;

	public PrefixVideoChildItem(Context context) {
		this(context, null, 0);
	}

	public PrefixVideoChildItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PrefixVideoChildItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.mv_pick_video_item, this, true);
		
		imgCover = (ImageView) findViewById(R.id.cover);
		txtTitle = (TextView) findViewById(R.id.title);
		txtDescription = (TextView) findViewById(R.id.description);
		viewActionContainer = findViewById(R.id.action_container);
		txtAction = (TextView) findViewById(R.id.action);
		txtActionDescription = (TextView) findViewById(R.id.action_description);
		pgs = (VerticalProgressBar) findViewById(R.id.progress);
		
		pgs.setIndicatorColor(0xFF57CBEC);
	}

	public void update(PrefixVideo data) {
		this.data = data;

		viewActionContainer.setTag(data);
		pgs.setTag(data);
		txtTitle.setText(data.name);
		txtDescription.setText(data.description);
		
		if (data.coverCircleFile.exists()) {
			ImageLoader.getInstance().displayImage(data.getCoverCircleLocalUrl(), imgCover);
		}
		
		setBackgroundColor(data.selected ? 0xFFD9EFFF : 0xFFF5F1ED);
		
		if (data.state == PrefixVideo.STATE_DESCRIPTION) {
			pgs.setVisibility(View.GONE);
			viewActionContainer.setVisibility(View.VISIBLE);
			txtActionDescription.setVisibility(View.VISIBLE);
			txtAction.setText("免费");
			txtActionDescription.setText(data.totalDescription);
		}
		else if (data.state == PrefixVideo.STATE_DOWNLOAD) {
			pgs.setVisibility(View.GONE);
			viewActionContainer.setVisibility(View.VISIBLE);
			txtActionDescription.setVisibility(View.GONE);
			txtAction.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_download, 0, 0);
			txtAction.setText("下载");
		}
		else if (data.state == PrefixVideo.STATE_DOWNLOADING) {
			pgs.setVisibility(View.VISIBLE);
			pgs.update(data.current, data.total);
			viewActionContainer.setVisibility(View.GONE);
		}
		else if (data.state == PrefixVideo.STATE_COMPLETE) {
			pgs.setVisibility(View.GONE);
			viewActionContainer.setVisibility(View.VISIBLE);
			txtActionDescription.setVisibility(View.GONE);
			txtAction.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_selected, 0, 0);
			txtAction.setText("完成");
		}
	}
}

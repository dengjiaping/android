package com.oumen.activity.detail.comment;

import java.util.Date;

import com.oumen.R;
import com.oumen.android.App;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CommentItem extends LinearLayout {
	TextView nick, content, time;

	public CommentItem(Context context) {
		this(context, null, 0);
	}

	public CommentItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CommentItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.huodong_comment_list_item, this, true);
		
		nick = (TextView) findViewById(R.id.nick);
		content = (TextView) findViewById(R.id.content);
		time = (TextView) findViewById(R.id.time);
	}
	
	public void update(Comment comment) {
		nick.setText(comment.getNickName());
		
		if (comment.getPriseType() == Comment.PRISE_TYPE_HAOPING) {
			nick.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.huodong_comment_haoping), null);
		}
		else {
			nick.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.huodong_comment_chaping), null);
		}
		
		content.setText(comment.getContent());
		
		time.setText(App.YYYY_MM_DD_FORMAT.format(new Date(comment.getTime())));
	}

}

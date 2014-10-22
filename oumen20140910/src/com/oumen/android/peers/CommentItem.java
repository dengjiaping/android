package com.oumen.android.peers;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.peers.entity.Comment;
import com.oumen.circle.CircleItemData;
import com.oumen.circle.CircleListFragment;

public class CommentItem extends TextView {
	int biaoqingIconSize = App.INT_UNSET;
	
	public CommentItem(Context context) {
		this(context, null, 0);
	}

	public CommentItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CommentItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		int padding = getResources().getDimensionPixelSize(R.dimen.small_gap);
		setPadding(padding, 0, padding, 0);
		setTextColor(context.getResources().getColor(R.color.content));
		setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		
		biaoqingIconSize = (int) (14 * context.getResources().getDisplayMetrics().scaledDensity) + 2;
	}

	public void update() {
		CircleItemData itemData = (CircleItemData) getTag();

		Comment c = itemData.commentData;
		int authorEnd = c.getAuthorName().length();
		if (c.getTargetId() > 0) {
			int targetStart = authorEnd + 2;
			ForegroundColorSpan spanAuthor = new ForegroundColorSpan(getResources().getColor(R.color.text_highlight));
			SpannableStringBuilder spans = new SpannableStringBuilder(c.getAuthorName() + "回复" + c.getTargetName() + ":" + c.getContent());
			spans.setSpan(spanAuthor, 0, authorEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			if (c.getTargetName() == null) {
				c.setTargetName("");
			}
			spans.setSpan(CircleListFragment.SPAN_NICKNAME, targetStart, targetStart + c.getTargetName().length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			spans = App.SMALLBIAOQING.convert(getContext(), spans, biaoqingIconSize);
			setText(spans);
		}
		else {
			SpannableStringBuilder spans = new SpannableStringBuilder(c.getAuthorName() + ":" + c.getContent());
			spans.setSpan(CircleListFragment.SPAN_NICKNAME, 0, authorEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			spans = App.SMALLBIAOQING.convert(getContext(), spans, biaoqingIconSize);
			setText(spans);
		}
	}
	
	public void update(Comment c) {
		setTag(c);

		int authorEnd = c.getAuthorName().length();
		if (c.getTargetId() > 0) {
			int targetStart = authorEnd + 2;
			ForegroundColorSpan spanAuthor = new ForegroundColorSpan(getResources().getColor(R.color.text_highlight));
			SpannableStringBuilder spans = new SpannableStringBuilder(c.getAuthorName() + "回复" + c.getTargetName() + ":" + c.getContent());
			spans.setSpan(spanAuthor, 0, authorEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			spans.setSpan(CircleListFragment.SPAN_NICKNAME, targetStart, targetStart + c.getTargetName().length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			spans = App.SMALLBIAOQING.convert(getContext(), spans, biaoqingIconSize);
			setText(spans);
		}
		else {
			SpannableStringBuilder spans = new SpannableStringBuilder(c.getAuthorName() + ":" + c.getContent());
			spans.setSpan(CircleListFragment.SPAN_NICKNAME, 0, authorEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			spans = App.SMALLBIAOQING.convert(getContext(), spans, biaoqingIconSize);
			setText(spans);
		}
	}
}

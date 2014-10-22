package com.oumen.near;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.activity.HuodongTypeUtil;
import com.oumen.activity.list.ActivityFragment;
import com.oumen.activity.list.AmuseActivity;
import com.oumen.activity.widget.IndexViewPager;
import com.oumen.android.App;
import com.oumen.home.LoginConfrim;
import com.oumen.tools.ELog;

public class ActivityPanel extends LinearLayout {
	protected IndexViewPager pager;
	protected TextView txtTip;
	
	private LoginConfrim loginConfrim;
	
	private ForegroundColorSpan spanCountColor = new ForegroundColorSpan(0xFF17B9E8);
	private ForegroundColorSpan spanClickColor = new ForegroundColorSpan(0xFFFF790C);
	private ClickableSpan spanClick = new ClickableSpan() {
		
		@Override
		public void onClick(View widget) {
			
			if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
				loginConfrim.openDialog();
				return;
			}
			
			Intent intent = new Intent(getContext(), AmuseActivity.class);
			intent.putExtra(ActivityFragment.HUODONG_TYPE, HuodongTypeUtil.CONDITION_FUJIN);
			getContext().startActivity(intent);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(false);
			ds.setFakeBoldText(true);
		}
	};

	public ActivityPanel(Context context) {
		this(context, null, 0);
	}

	public ActivityPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActivityPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setBackgroundColor(Color.TRANSPARENT);
		setOrientation(VERTICAL);
		
		loginConfrim = new LoginConfrim(context);
		
		int paddingLarge = getResources().getDimensionPixelSize(R.dimen.padding_large),
			paddingSmall = getResources().getDimensionPixelSize(R.dimen.padding_small);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 200);
		params.topMargin = params.bottomMargin = params.leftMargin = params.rightMargin = paddingLarge;
		params.width = getResources().getDisplayMetrics().widthPixels - paddingLarge * 2;
		params.height = params.width * 18 / 29; // Banner宽高比为29:18
		pager = new IndexViewPager(context);
		addView(pager, params);
		
		txtTip = new TextView(context);
		txtTip.setMovementMethod(LinkMovementMethod.getInstance());
		txtTip.setPadding(paddingLarge, paddingSmall, paddingLarge, paddingSmall);
		txtTip.setBackgroundColor(getResources().getColor(R.color.near_huodong_bg));
		txtTip.setTextColor(Color.BLACK);
		txtTip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_pretty));
		addView(txtTip);
	}
	
	public void updateTip(String n) {
		String res = getResources().getString(R.string.activity_near_list_tip).replace("n", n);
		SpannableStringBuilder builder = new SpannableStringBuilder(res);

		int nStart = res.indexOf(n);
		int nEnd = nStart + n.length();
		builder.setSpan(spanCountColor, nStart, nEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		
		nStart = res.length() - 4;
		nEnd = res.length();
		builder.setSpan(spanClickColor, nStart, nEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		builder.setSpan(spanClick, nStart, nEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//		builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), nStart, nEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
		txtTip.setText(builder);
	}
	
	public void notifyDataSetChanged() {
		pager.notifyDataSetChanged();
	}
	
	public void addAll(Collection<IndexViewPager.ItemData> collection) {
		pager.addAll(collection);
	}
	
	public void clear() {
		pager.clear();
	}
	
	public void startTimer() {
		ELog.i("");
		pager.stopPlay();
	}
	
	public void stopTimer() {
		pager.stopPlay();
	}
	
	public ArrayList<IndexViewPager.ItemData> copyDataSource() {
		return pager.copyDataSource();
	}
}

package com.oumen.activity.list;

import com.oumen.R;
import com.oumen.activity.detail.RectImageHasDownloadView;
import com.oumen.activity.message.BaseActivityMessage;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Item extends LinearLayout {
	RectImageHasDownloadView image;
	TextView title, address, money;

	public Item(Context context) {
		this(context, null, 0);
	}

	public Item(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Item(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.huodong_list_item1, this, true);

		image = (RectImageHasDownloadView) findViewById(R.id.huodong_image);
		title = (TextView) findViewById(R.id.title);
		address = (TextView) findViewById(R.id.address);
		money = (TextView) findViewById(R.id.money);
	}

	public void update(BaseActivityMessage provider) {
		image.update(provider.getPicUrl());
		title.setText(provider.getName());
		address.setText(provider.getCity());
		if (!"0".equals(provider.getMoney())) {
			money.setTextSize(18);
			SpannableStringBuilder builder = new SpannableStringBuilder();
			builder.append("¥" + provider.getMoney() + "起");
			AbsoluteSizeSpan contentSizeSpen = new AbsoluteSizeSpan(26 * 2);
			builder.setSpan(contentSizeSpen,"¥".length(),("¥" + provider.getMoney()).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			money.setText(builder);
		}
		else {
			money.setTextSize(24);
			money.setText("免费");
		}
	}
}

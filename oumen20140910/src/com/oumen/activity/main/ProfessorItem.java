package com.oumen.activity.main;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.activity.message.BaseActivityMessage;
import com.oumen.android.App;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfessorItem extends LinearLayout {
	ImageView image;
	TextView title, address, money;

	public ProfessorItem(Context context) {
		this(context, null, 0);
	}

	public ProfessorItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ProfessorItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.professor_item, this, true);

		image = (ImageView) findViewById(R.id.huodong_image);
		title = (TextView) findViewById(R.id.huodong_title);
		address = (TextView) findViewById(R.id.huodong_address);
		money = (TextView) findViewById(R.id.huodong_money);
		setProfessorItemHeight();
	}
	
	public void setProfessorItemHeight() {
		int padding = getResources().getDimensionPixelSize(R.dimen.padding_large);
		int width = (App.SCREEN_WIDTH - padding * 3)/2;
		ViewGroup.LayoutParams params = image.getLayoutParams();
		params.width = width;
		params.height = params.width* 18 / 29;
		image.setLayoutParams(params);
	}

	public void update(BaseActivityMessage provider) {
		ImageLoader.getInstance().displayImage(provider.getPicUrl(),image);
		title.setText(provider.getName());
		address.setText(provider.getCity());
		if (!"0".equals(provider.getMoney())) {
			money.setTextSize(16);
			SpannableStringBuilder builder = new SpannableStringBuilder();
			builder.append("¥" + provider.getMoney() + "起");
			AbsoluteSizeSpan contentSizeSpen = new AbsoluteSizeSpan(22 * 2);
			builder.setSpan(contentSizeSpen,"¥".length(),("¥" + provider.getMoney()).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			money.setText(builder);
		}
		else {
			money.setTextSize(20);
			money.setText("免费");
		}
	}
}

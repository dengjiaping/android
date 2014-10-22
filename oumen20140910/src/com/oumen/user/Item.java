package com.oumen.user;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.peers.entity.CircleUserBasicMsg;
import com.oumen.home.LoginConfrim;
import com.oumen.message.BaseMessage;
import com.oumen.peer.OumenCircleDetailActivity;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.oumen.widget.file.ImageData;
import com.oumen.widget.image.GridImageAdapter;

class Item extends LinearLayout {
	ImageView babyStateFlag;
	TextView txtContent;
	GridView grid;
	TextView txtTime;

	TextView commentCount;
	TextView favorCount;
	TextView lookCount;

	private SpannableStringBuilder builder = new SpannableStringBuilder();
	private SpannableStringBuilder tempBuilder = new SpannableStringBuilder();

	final GridImageAdapter adapterCell = new GridImageAdapter();

	CircleUserBasicMsg info;
	
	private Context context;
	
	private LoginConfrim loginConfirm;

	Item(Context context) {
		this(context, null, 0);
	}

	Item(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	Item(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.userinfo_item, this, true);
		
		this.context = context;
		
		loginConfirm = new LoginConfrim(context);

		babyStateFlag = (ImageView) findViewById(R.id.babystate_flag);
		grid = (GridView) findViewById(R.id.grid);
		txtTime = (TextView) findViewById(R.id.userinfo_time);
		txtContent = (TextView) findViewById(R.id.userinfo_content);
		grid.setAdapter(adapterCell);

		commentCount = (TextView) findViewById(R.id.comment_count);
		favorCount = (TextView) findViewById(R.id.favour_count);
		lookCount = (TextView) findViewById(R.id.look_count);
		
		commentCount.setOnClickListener(clickListener);
		favorCount.setOnClickListener(clickListener);
		lookCount.setOnClickListener(clickListener);
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == commentCount || v == favorCount || v == lookCount) {
				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					loginConfirm.openDialog();
					return;
				}
				Intent intent = new Intent(v.getContext(), OumenCircleDetailActivity.class);
				intent.putExtra(BaseMessage.KEY_TARGET_ID, info.getCircleId());
				context.startActivity(intent);
			}
		}
	};

	void update(CircleUserBasicMsg info) {
		this.info = info;

		txtTime.setText(info.getCreatetime());
		int mode = info.getModes();
		if (CircleUserBasicMsg.MODE_EXCHANGE == mode) {
			babyStateFlag.setImageResource(R.drawable.user_exchange);
		}
		else if (CircleUserBasicMsg.MODE_HELP == mode) {
			babyStateFlag.setImageResource(R.drawable.user_help);
		}
		else {
			babyStateFlag.setImageResource(R.drawable.user_share);
		}
		String str = info.getContent();
		int len = str.length();
		if (len >= 100) {
			builder.clear();
			txtContent.setVisibility(View.VISIBLE);
			builder.append(str.substring(0, 100));
			builder.append("\n查看全文");
			final ClickableSpan span = new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					txtContent.setMovementMethod(LinkMovementMethod.getInstance());
					txtContent.setText(tempBuilder);
				}

				@Override
				public void updateDrawState(TextPaint ds) {
					ds.setColor(getResources().getColor(R.color.text_highlight));
					ds.setUnderlineText(false);
				}

			};
			builder.setSpan(span, 100, 100 + "\n查看全文".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			txtContent.setMovementMethod(LinkMovementMethod.getInstance());
			txtContent.setText(builder);

			// TODO 此处点击查看原文
			tempBuilder.clear();
			tempBuilder.append(str);
			tempBuilder.append("\n收起");
			final ClickableSpan tempSpan = new ClickableSpan() {

				@Override
				public void onClick(View widget) {
					ELog.i("");
					txtContent.setMovementMethod(LinkMovementMethod.getInstance());
					txtContent.setText(builder);
				}

				@Override
				public void updateDrawState(TextPaint ds) {
					ds.setColor(getResources().getColor(R.color.text_highlight));
					ds.setUnderlineText(false);
				}
			};
			tempBuilder.setSpan(tempSpan, len, len + "\n收起".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		else {
			if (len == 0) {
				txtContent.setVisibility(View.GONE);
			}
			else {
				txtContent.setVisibility(View.VISIBLE);
				txtContent.setText(str);
			}
		}
		// 照片显示
		adapterCell.data.clear();
		if (info.photos.size() > 0) {
			grid.setVisibility(View.VISIBLE);
			final ArrayList<ImageData> images = new ArrayList<ImageData>();
			for (int i = 0; i < info.photos.size(); i++) {
				ImageData image = new ImageData(info.photos.get(i));
				images.add(image);
			}
			adapterCell.data.addAll(images);
			adapterCell.notifyDataSetChanged();
			
			int colum = info.photos.size() - 3 > 0 ? 2 : 1;
			ViewGroup.LayoutParams gridparams = grid.getLayoutParams();
			gridparams.width = ImageTools.dip2px(getContext(), 84 * 3 + 8 * 2);
			gridparams.height = ImageTools.dip2px(getContext(), 84 * colum + 5 * (colum - 1));
			grid.setLayoutParams(gridparams);
			grid.requestLayout();
		}
		else {
			grid.setVisibility(View.GONE);
		}

		commentCount.setText(info.getCommentNum());
		favorCount.setText(info.getPriseNum());
		lookCount.setText(info.getLookNum());
	}
}

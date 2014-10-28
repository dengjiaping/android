package com.oumen.circle;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.peers.CommentItem;
import com.oumen.android.peers.Prise;
import com.oumen.android.peers.entity.CircleUserBasicMsg;
import com.oumen.android.peers.entity.CircleUserMsg;
import com.oumen.home.LoginConfrim;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.oumen.user.UserInfoActivity;
import com.oumen.widget.file.ImageData;
import com.oumen.widget.image.GridImageAdapter;

public class CircleItem extends LinearLayout implements View.OnClickListener {
	private int biaoqingIconSize = App.INT_UNSET;

	final GridImageAdapter adapterGridImage = new GridImageAdapter();

	ImageView imgPhoto;
	TextView txtNickname;
	TextView txtTime;
	TextView txtLookNum; // 新增查看人数
	TextView txtMode;
	TextView txtContent;
	GridView gridImages;
	ImageView imgMore;
	ViewGroup containerMessages;
	TextView txtEnjoyDescription;
	View viewline;
	TextView txtShow;
	LinearLayout lstComment;

	PopupWindow popupWindow;
	MoreView viewMore;

	TextView txtDelete;
	TextView txtDistance;

	View.OnClickListener hostClickListener;

	int lineHeight;
	int padding;
	int lineGap;

	private LoginConfrim loginConfrim;

	private SpannableStringBuilder builder = new SpannableStringBuilder();
	private SpannableStringBuilder tempBuilder = new SpannableStringBuilder();

	public CircleItem(Context context) {
		this(context, null, 0);
	}

	public CircleItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		lineHeight = getResources().getDimensionPixelSize(R.dimen.default_line_height);
		padding = getResources().getDimensionPixelSize(R.dimen.padding_medium);
		lineGap = getResources().getDimensionPixelSize(R.dimen.padding_micro);

		loginConfrim = new LoginConfrim(context);
		biaoqingIconSize = (int) (14 * context.getResources().getDisplayMetrics().scaledDensity) + 2;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.group_item, this, true);

		imgPhoto = (ImageView) findViewById(R.id.photo);
		imgPhoto.setOnClickListener(this);
		txtNickname = (TextView) findViewById(R.id.nickname);
		txtDistance = (TextView) findViewById(R.id.distance);
		txtMode = (TextView) findViewById(R.id.mode);
		txtContent = (TextView) findViewById(R.id.content);
		txtTime = (TextView) findViewById(R.id.time);
		txtLookNum = (TextView) findViewById(R.id.looknum);
		txtLookNum.setVisibility(View.GONE);

		imgMore = (ImageView) findViewById(R.id.more);
		imgMore.setOnClickListener(this);
		txtDelete = (TextView) findViewById(R.id.delete);// 删除
		txtDelete.setOnClickListener(this);

		// 图片信息
		gridImages = (GridView) findViewById(R.id.grid);
		gridImages.setAdapter(adapterGridImage);
		gridImages.setVisibility(View.GONE);

		containerMessages = (ViewGroup) findViewById(R.id.ll_groupitem_message);
		containerMessages.setVisibility(View.GONE);
		// 显示有多少人赞了
		txtEnjoyDescription = (TextView) findViewById(R.id.enjoy_description);
		txtEnjoyDescription.setVisibility(View.GONE);
		viewline = (View) findViewById(R.id.line_behind);

		// 是否公开
		txtShow = (TextView) findViewById(R.id.show);
		txtShow.setVisibility(View.GONE);

		// 评论
		lstComment = (LinearLayout) findViewById(R.id.comments);
		lstComment.setDrawingCacheEnabled(true);
		lstComment.setVisibility(View.GONE);

		viewMore = new MoreView(context);
		viewMore.setButtonClickListener(this);
		popupWindow = new PopupWindow(viewMore, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setFocusable(true); // 设置PopupWindow可获得焦点
		popupWindow.setTouchable(true); // 设置PopupWindow可触摸
		popupWindow.setOutsideTouchable(false);
	}

	public void setButtonClickListener(View.OnClickListener listener) {
		hostClickListener = listener;
	}

	public void update(boolean scrollFlag) {
		CircleItemData itemData = (CircleItemData) getTag();
		CircleUserMsg info = itemData.groupData;

		viewMore.setData(itemData);
		imgPhoto.setTag(itemData);
		txtDelete.setTag(itemData);

		// 设置昵称
		txtNickname.setText(info.getInfo().getNickname());
		// 设置时间
		txtTime.setText(info.getInfo().getCreatetime());
		txtLookNum.setVisibility(View.GONE);
//		if (TextUtils.isEmpty(info.getLookNum())) {
//			txtLookNum.setVisibility(View.GONE);
//		}
//		else {
//			txtLookNum.setVisibility(View.GONE);
//			txtLookNum.setText(info.getLookNum() + "人阅读");
//		}

		// 设置模式
		int mode = info.getInfo().getModes();
		if (CircleUserBasicMsg.MODE_EXCHANGE == mode) {
			txtMode.setText("交流");
			txtMode.setBackgroundColor(getResources().getColor(R.color.oumen_circle_exchange));
		}
		else if (CircleUserBasicMsg.MODE_HELP == mode) {
			txtMode.setText("求助");
			txtMode.setBackgroundColor(getResources().getColor(R.color.oumen_circle_help));
		}
		else {
			txtMode.setText("分享");
			txtMode.setBackgroundColor(getResources().getColor(R.color.oumen_circle_share));
		}
		// 判断是不是自己发的，只有自己发的，才可以删除
		if (info.getInfo().getUid() == App.USER.getUid()) {
			txtDelete.setVisibility(View.VISIBLE);
		}
		else {
			txtDelete.setVisibility(View.GONE);
		}

		// 设置内容
		final String content = info.getInfo().getContent();
		if (content.length() <= 100) {
			builder.clear();
			builder.append(content);
			builder = App.SMALLBIAOQING.convert(getContext(), builder, biaoqingIconSize);
			txtContent.setText(builder);
		}
		else {
			builder.clear();
			builder.append(content.substring(0, 100));
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
			builder = App.SMALLBIAOQING.convert(getContext(), builder, biaoqingIconSize);
			txtContent.setMovementMethod(LinkMovementMethod.getInstance());
			txtContent.setText(builder);

			// TODO 此处点击查看原文
			tempBuilder.clear();
			tempBuilder.append(content);
			tempBuilder.append("\n收起");
			final ClickableSpan tempSpan = new ClickableSpan() {

				@Override
				public void onClick(View widget) {
					txtContent.setMovementMethod(LinkMovementMethod.getInstance());
					txtContent.setText(builder);
				}

				@Override
				public void updateDrawState(TextPaint ds) {
					ds.setColor(getResources().getColor(R.color.text_highlight));
					ds.setUnderlineText(false);
				}
			};
			tempBuilder.setSpan(tempSpan, content.length(), content.length() + "\n收起".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			tempBuilder = App.SMALLBIAOQING.convert(getContext(), tempBuilder, biaoqingIconSize);
		}
		
		txtDistance.setVisibility(View.GONE);//TODO 距离隐藏
//		txtDistance.setText(info.getInfo().getDistance() + "km");

		//===============================如果正在滚动就不加载图片=================================
		if (scrollFlag) {
			return;
		}

		int isFavour = info.getIsprise();
		if (isFavour == 0) {
			viewMore.setEnjoyText(R.string.enjoy, R.drawable.oumen_circle_zan, R.drawable.oumen_circle_more_pop_btn_bg);
		}
		else if (isFavour == 1) {
			viewMore.setEnjoyText(R.string.cancel, R.drawable.oumen_circle_zan_click, R.drawable.oumen_circle_more_pop_btn_click_bg);
		}

		File cache = ImageLoader.getInstance().getDiskCache().get(info.getInfo().getHeadPhotoUrl(App.DEFAULT_PHOTO_SIZE));
		if (cache != null && cache.exists()) {
			Bitmap pic = BitmapFactory.decodeFile(cache.getAbsolutePath());
			pic = ImageTools.clip2square(pic);
			imgPhoto.setImageBitmap(pic);
		}
		else {
			// 设置头像
			if (info.getInfo().hasHeadPhoto()) {
				ImageLoader.getInstance().displayImage(info.getInfo().getHeadPhotoUrl(App.DEFAULT_PHOTO_SIZE), imgPhoto, App.OPTIONS_HEAD_RECT);
			}
			else {
				imgPhoto.setImageResource(R.drawable.rect_user_photo);
			}
		}

		// ===========================设置图片==========================================
		if (info.getInfo().photos.size() == 0) {
			gridImages.setVisibility(View.GONE);
		}
		else {
			gridImages.setVisibility(View.VISIBLE);
			adapterGridImage.data.clear();
			int size = info.getInfo().photos.size();
			for (int i = 0; i < size; i++) {
				ImageData image = new ImageData(info.getInfo().photos.get(i));
				adapterGridImage.data.add(image);
			}
			adapterGridImage.notifyDataSetChanged();

			int colum = size - 3 > 0 ? 2 : 1;
			ViewGroup.LayoutParams gridparams = gridImages.getLayoutParams();
			gridparams.width = ImageTools.dip2px(getContext(), 84 * 3 + 8 * 2);
			gridparams.height = ImageTools.dip2px(getContext(), 84 * colum + 5 * (colum - 1));
			gridImages.setLayoutParams(gridparams);
			gridImages.requestLayout();
		}

		// ===============================获取评论信息==================================
		int commentCount = info.comments.size();
		if (commentCount > 0) {
			long time = System.currentTimeMillis();
			// 设置评论显示
			containerMessages.setVisibility(View.VISIBLE);
			lstComment.setVisibility(View.VISIBLE);
			lstComment.removeAllViews();

			for (int i = 0; i < commentCount; i++) {
				CircleItemData cmtItemData = new CircleItemData();
				final CommentItem item = new CommentItem(getContext());
				item.setOnClickListener(hostClickListener);
				item.setTag(cmtItemData);
				cmtItemData.groupIndex = itemData.groupIndex;
				cmtItemData.groupData = itemData.groupData;
				cmtItemData.commentIndex = i;
				cmtItemData.commentData = info.comments.get(i);
				final boolean isLast = i == (commentCount - 1);
				item.update();
//				item.post(new Runnable() {
//
//					@Override
//					public void run() {
//						LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) item.getLayoutParams();
//						params.height = isLast ? (item.getLineHeight() * item.getLineCount() + padding) : item.getLineHeight() * item.getLineCount() + lineGap;
//						item.setLayoutParams(params);
//					}
//				});
				lstComment.addView(item);
			}
			ELog.e("加载评论需要时间：" + (System.currentTimeMillis() - time));
		}
		else {
//			// 设置评论隐藏
			lstComment.removeAllViews();
			lstComment.setVisibility(View.GONE);
		}
		// ===========================设置赞的人数初始化====================================
		ArrayList<Prise> prises = info.prises;
		if (prises != null && prises.size() > 0) {
			containerMessages.setVisibility(View.VISIBLE);
			txtEnjoyDescription.setVisibility(View.VISIBLE);
			viewline.setVisibility(View.VISIBLE);

			SpannableStringBuilder builder = new SpannableStringBuilder();
			int len = 0;
			for (int i = 0; i < prises.size(); i++) {
				ForegroundColorSpan fgSpan = new ForegroundColorSpan(getResources().getColor(R.color.text_highlight));
				final Prise priser = prises.get(i);
				builder.append(priser.getPriseName());
				if (i < prises.size() - 1) {
					builder.append("，");
				}
				ClickableSpan span = new ClickableSpan() {

					@Override
					public void onClick(View widget) {
						Intent intent = new Intent(getContext(), UserInfoActivity.class);
						intent.putExtra(UserInfoActivity.INTENT_KEY_UID, priser.getPriseUid());
						getContext().startActivity(intent);
					}

					@Override
					public void updateDrawState(TextPaint ds) {
						ds.setColor(getResources().getColor(R.color.text_highlight));
						ds.setUnderlineText(false);
					}
				};
				builder.setSpan(span, len, len + priser.getPriseName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				builder.setSpan(fgSpan, len, len + priser.getPriseName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				len += priser.getPriseName().length() + "，".length();
			}
			builder.insert(0, "icon");
			builder.setSpan(CircleListFragment.SPAN_ENJOY_ICON, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			txtEnjoyDescription.setText(builder);
			txtEnjoyDescription.setMovementMethod(LinkMovementMethod.getInstance());
//			txtEnjoyDescription.post(new Runnable() {
//
//				@Override
//				public void run() {
//					ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) txtEnjoyDescription.getLayoutParams();
//					if (txtEnjoyDescription.getLineCount() == 1) {
//						params.height = lineHeight;
//					}
//					else {
//						params.height = lineHeight + (txtEnjoyDescription.getLineCount() - 1) * txtEnjoyDescription.getLineHeight();
//					}
//					txtEnjoyDescription.setLayoutParams(params);
//				}
//			});
		}
		else if (prises.size() == 0) {
			txtEnjoyDescription.setVisibility(View.GONE);
			viewline.setVisibility(View.GONE);
		}
		if (prises.size() == 0 && commentCount == 0) {
			containerMessages.setVisibility(View.GONE);
		}

	}

	@Override
	public void onClick(View v) {
		if (v == imgMore) {
			if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
				//TODO 跳转到登录界面
				loginConfrim.openDialog();
				return;
			}
			if (popupWindow.isShowing()) {
				popupWindow.dismiss();
			}
			else {
				int width = getResources().getDimensionPixelSize(R.dimen.circle_more_item_width) * 3 + getResources().getDimensionPixelSize(R.dimen.padding_small) * 2 + getResources().getDimensionPixelSize(R.dimen.padding_large) * 2;
				int height = getResources().getDimensionPixelSize(R.dimen.circle_more_height);
				int x = width * -1;
				int y = (height / 2 + (height - imgMore.getHeight()) / 2) * -1;
				popupWindow.showAsDropDown(imgMore, x, y);
			}
		}
		else {
			if (popupWindow.isShowing()) {
				popupWindow.dismiss();
			}
			if (hostClickListener != null) {
				hostClickListener.onClick(v);
			}
		}
	}
}

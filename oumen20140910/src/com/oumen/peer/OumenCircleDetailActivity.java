package com.oumen.peer;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.peers.CommentItem;
import com.oumen.android.peers.Prise;
import com.oumen.android.peers.entity.CircleUserBasicMsg;
import com.oumen.android.peers.entity.CircleUserMsg;
import com.oumen.android.peers.entity.Comment;
import com.oumen.android.util.Constants;
import com.oumen.auth.ShareView;
import com.oumen.circle.MoreView;
import com.oumen.home.FloatViewController;
import com.oumen.home.FloatViewHostController;
import com.oumen.home.SendMessageView;
import com.oumen.home.SoftKeyboardController;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.message.BaseMessage;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.oumen.user.UserInfoActivity;
import com.oumen.widget.dialog.TwoButtonDialog;
import com.oumen.widget.file.ImageData;
import com.oumen.widget.image.GridImageAdapter;

/**
 * 偶们圈详情展示界面
 * 
 */
public class OumenCircleDetailActivity extends BaseActivity implements FloatViewHostController, View.OnTouchListener {
	private int biaoqingIconSize = App.INT_UNSET;
	private final int HANDLER_TYPE_FAIL = 2;
	private final int HANDLER_TYPE_ZAN_SUCCESS = 3;
	private final int HANDLER_TYPE_DELETE_SUCCESS = 4;
	private final int HANDLER_TYPE_DELETE_COMMENT_SUCCESS = 5;

	private final int HANDLER_GET_DETAIL_SUCCESS = 1;
	private final int HANDLER_GET_DETAIL_FAIL = 6;

	final GridImageAdapter adapterGridImage = new GridImageAdapter();

	private int lineHeight;

	//标题行控件
	private TitleBar titlebar;
	private Button btnLeft;
	private TextView tvTitle;

	private ImageView imgHeadPhoto;
	private TextView txtNickname;
	private TextView txtTime;
	private Button btnMode;
	private TextView txtContent;
	private GridView gridImages;
	private ImageView imgMore;
	private TextView txtEnjoyDescription;
	private View viewline;
	private TextView txtShow;
	private LinearLayout list;

	private PopupWindow popupWindow;
	private MoreView viewMore;
	private TextView txtDelete;
	private TextView tvLookNum;
	// 新增(距离信息)
	private TextView txtDistance;

	private View popupLayer;
	private RelativeLayout rootContainer;//整个布局

	private CircleUserMsg info;
	private Comment currentComment = null;

	protected ShareView viewShare;
	protected SendMessageView viewSend;

	private SpannableStringBuilder builder = new SpannableStringBuilder();
	private SpannableStringBuilder tempBuilder = new SpannableStringBuilder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oumen_circle_detail);

		lineHeight = getResources().getDimensionPixelSize(R.dimen.default_line_height);
		// 初始化
		initAnimation();
		viewShare = new ShareView(this);
		viewShare.setHost(this);

		viewSend = new SendMessageView(this);
		viewSend.setButtonClickListener(clicklistener);
		init();
		//获取上个界面传过来的id
		int targetId = getIntent().getIntExtra(BaseMessage.KEY_TARGET_ID, App.INT_UNSET);
		if (targetId != App.INT_UNSET) {
			//向服务器发送请求获取偶们圈具体内容
			if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
				requestDetail(String.valueOf(targetId));
			}
			else {
				Toast.makeText(mBaseApplication, "网络不给力~，请检查网络是否连接", Toast.LENGTH_SHORT).show();
			}
		}
		biaoqingIconSize = (int) (14 * getResources().getDisplayMetrics().scaledDensity) + 2;

	}

	private void init() {
		//标题行
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		titlebar.getRightButton().setVisibility(View.GONE);
		btnLeft = titlebar.getLeftButton();
		tvTitle = titlebar.getTitle();
		tvTitle.setText("内容详情");

		imgHeadPhoto = (ImageView) findViewById(R.id.head_photo);

		txtNickname = (TextView) findViewById(R.id.groupitem_nickname);

		txtTime = (TextView) findViewById(R.id.groupitem_time);

		btnMode = (Button) findViewById(R.id.iv_groupitem_type);

		txtContent = (TextView) findViewById(R.id.groupitem_content);

		// 图片信息
		gridImages = (GridView) findViewById(R.id.grid);
		gridImages.setAdapter(adapterGridImage);

		// 更多
		imgMore = (ImageView) findViewById(R.id.iv_groupimage_more);

		// 显示有多少人赞了
		txtEnjoyDescription = (TextView) findViewById(R.id.enjoy_description);
		txtEnjoyDescription.setVisibility(View.GONE);

		viewline = (View) findViewById(R.id.line_behind);
		// 是否公开
		txtShow = (TextView) findViewById(R.id.show);
		txtShow.setVisibility(View.GONE);
		// 评论

		txtDelete = (TextView) findViewById(R.id.groupitem_delete);

		tvLookNum = (TextView) findViewById(R.id.groupitem_looknum);

		txtDistance = (TextView) findViewById(R.id.tv_distance);

		list = (LinearLayout) findViewById(R.id.list);
		list.setVisibility(View.GONE);

		popupLayer = findViewById(R.id.layer);
		popupLayer.setOnTouchListener(this);
		rootContainer = (RelativeLayout) findViewById(R.id.root);

		btnLeft.setOnClickListener(clicklistener);

		txtDelete.setOnClickListener(clicklistener);
		imgMore.setOnClickListener(clicklistener);

		initPopWindow();
	}

	private final OnClickListener clicklistener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v instanceof CommentItem) {
				final Comment cmt = (Comment) v.getTag();

				if (App.PREFS.getUid() == cmt.getAuthorId()) {
					// 显示删除
					if (dialogDeleteComment == null) {
						dialogDeleteComment = new TwoButtonDialog(v.getContext());
						dialogDeleteComment.getTitleView().setText(R.string.default_dialog_title);

						dialogDeleteComment.getMessageView().setText(R.string.circle_confrim_delete_comment);
						dialogDeleteComment.getLeftButton().setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								ELog.i("Delete comment event");
								deleteComment(currentComment);
								dialogDeleteComment.dismiss();
							}
						});
						dialogDeleteComment.getRightButton().setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								dialogDeleteComment.dismiss();
							}
						});
					}
					dialogDeleteComment.show();
					currentComment = cmt;
				}
				else {
					popupWindow.dismiss();
					currentComment = cmt;
					showSendView();
				}
				return;
			}
			switch (v.getId()) {
				case R.id.left://返回
					setResult(Activity.RESULT_CANCELED);
					finish();
					break;
				case R.id.head_photo:// 头像
					Intent intent = new Intent(OumenCircleDetailActivity.this, UserInfoActivity.class);
					intent.putExtra(UserInfoActivity.INTENT_KEY_UID, info.getInfo().getUid());
					startActivity(intent);
					break;

				case R.id.iv_groupimage_more:// 更多按钮
					if (popupWindow.isShowing()) {
						popupWindow.dismiss();
					}
					else {
						int width = getResources().getDimensionPixelSize(R.dimen.circle_more_item_width) * 3 + getResources().getDimensionPixelSize(R.dimen.padding_small) * 2 + getResources().getDimensionPixelSize(R.dimen.padding_large) * 2;
						int height = getResources().getDimensionPixelSize(R.dimen.circle_more_height);
						int x = width * -1;
						int y = (height / 2 + (height - imgMore.getHeight()) / 2) * -1;
//						ELog.w("Coordrate:" + x + "/" + y + " Width:" + viewMore.getWidth() + "/" + width + " Height:" + viewMore.getHeight() + "/" + height);
						popupWindow.showAsDropDown(imgMore, x, y);
					}
					break;

				case R.id.groupitem_delete:// 删除
					final TwoButtonDialog tip = new TwoButtonDialog(OumenCircleDetailActivity.this);
					tip.getTitleView().setText("偶们提示");
					tip.getMessageView().setText("确定删除吗？");
					tip.getMessageView().setGravity(Gravity.LEFT);
					tip.getRightButton().setText("确定");
					tip.getRightButton().setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
								// 删除偶们圈的内容
								deleteContent();
							}
							else {
								Toast.makeText(mBaseApplication, "网络不给力~，请检查网络是否连接", Toast.LENGTH_SHORT).show();
							}
							tip.dismiss();
						}
					});
					tip.getLeftButton().setText("取消");
					tip.getLeftButton().setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							tip.dismiss();
						}
					});
					tip.show();
					break;
				case R.id.enjoy:// 赞
					if (popupWindow.isShowing()) {
						popupWindow.dismiss();
					}
					if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
						// 赞或者取消赞
						zan(info.getIsprise() == 1);
					}
					else {
						Toast.makeText(mBaseApplication, "网络不给力~，请检查网络是否连接", Toast.LENGTH_SHORT).show();
					}

					break;
				case R.id.comment:// 评论
					if (popupWindow.isShowing()) {
						popupWindow.dismiss();
					}
					viewSend.setData(info);
					viewSend.setInputHint("回复：" + info.getInfo().getNickname());
					showSendView();
					break;
				case R.id.share:// 更多分享按钮
					if (popupWindow.isShowing()) {
						popupWindow.dismiss();
					}
					showShareView();
					viewShare.setShareData(info);
					break;
				case R.id.send:// 发送评论消息
					String content = viewSend.getText().trim();
					if (TextUtils.isEmpty(content)) {
						Toast.makeText(mBaseApplication, "请输入内容", Toast.LENGTH_SHORT).show();
						return;
					}
					if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
						sendContent(currentComment, content);
					}
					else {
						Toast.makeText(mBaseApplication, "网络不给力~，请检查网络是否连接", Toast.LENGTH_SHORT).show();
					}
					viewSend.clear();
					hideFloatView();
					break;
			}

		}
	};

	/**
	 * 获取偶们圈详情
	 * 
	 * @param cid
	 */
	private void requestDetail(final String cid) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		params.add(new BasicNameValuePair("cid", cid));
		final DefaultHttpCallback noticeDetailCallback = new DefaultHttpCallback(new EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String res = result.getResult();
					ELog.i("" + res.toString());
					JSONObject obj = new JSONObject(res);
					CircleUserMsg peer = new CircleUserMsg(obj);
					handler.sendMessage(handler.obtainMessage(HANDLER_GET_DETAIL_SUCCESS, peer));
				}
				catch (Exception e) {
					ELog.e("Exception e=" + e.toString());
					e.printStackTrace();
					handler.sendMessage(handler.obtainMessage(HANDLER_GET_DETAIL_FAIL, "获取详情失败"));
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_GET_DETAIL_FAIL, "获取详情失败"));
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_GET_DETAIL_FAIL, "获取详情失败"));
			}
		});

		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_NOTICE_DETAIL, params, HttpRequest.Method.GET, noticeDetailCallback);
		App.THREAD.execute(req);
	}

	private TwoButtonDialog dialogDeleteComment;

	/**
	 * 初始化更多Popwindow
	 * 
	 * @param inflater
	 */
	private void initPopWindow() {
		// 赞，评论，分享
		viewMore = new MoreView(this);
		viewMore.setButtonClickListener(clicklistener);
		popupWindow = new PopupWindow(viewMore, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setFocusable(true); // 设置PopupWindow可获得焦点
		popupWindow.setTouchable(true); // 设置PopupWindow可触摸
		popupWindow.setOutsideTouchable(false);
	}

	public boolean isShownPopupWindow() {
		return popupWindow.isShowing();
	}

	public void hidePopupWindow() {
		popupWindow.dismiss();
	}

	private void initData() {
		ELog.i(info.toString());
		int isFavour = info.getIsprise();
		if (isFavour == 0) {
			viewMore.setEnjoyText(R.string.enjoy, R.drawable.oumen_circle_zan, R.drawable.oumen_circle_more_pop_btn_bg);
		}
		else if (isFavour == 1) {
			viewMore.setEnjoyText(R.string.cancel, R.drawable.oumen_circle_zan_click, R.drawable.oumen_circle_more_pop_btn_click_bg);
		}

		// 设置头像
		if (info.getInfo().hasHeadPhoto()) {
			ImageLoader.getInstance().displayImage(info.getInfo().getHeadPhotoUrl(App.DEFAULT_PHOTO_SIZE), imgHeadPhoto, App.OPTIONS_HEAD_ROUND);
		}
		else {
			imgHeadPhoto.setImageResource(R.drawable.round_user_photo);
		}

		// 设置昵称
		txtNickname.setText(info.getInfo().getNickname());
		// 设置时间
		txtTime.setText(info.getInfo().getCreatetime());
		// 设置模式
		int mode = info.getInfo().getModes();
		if (CircleUserBasicMsg.MODE_EXCHANGE == mode) {
			btnMode.setText("交流");
			btnMode.setBackgroundColor(getResources().getColor(R.color.oumen_circle_exchange));
		}
		else if (CircleUserBasicMsg.MODE_HELP == mode) {
			btnMode.setText("求助");
			btnMode.setBackgroundColor(getResources().getColor(R.color.oumen_circle_help));
		}
		else {
			btnMode.setText("分享");
			btnMode.setBackgroundColor(getResources().getColor(R.color.oumen_circle_share));
		}
		// 判断是不是自己发的，只有自己发的，才可以删除
		if (String.valueOf(App.PREFS.getUid()).equals(info.getInfo().getUid())) {
			txtDelete.setVisibility(View.VISIBLE);
		}
		else {
			txtDelete.setVisibility(View.GONE);
		}
		tvLookNum.setVisibility(View.GONE);
		tvLookNum.setText(info.getLookNum() + "人阅读");
		// 设置内容
		final String content = info.getInfo().getContent();
		if (content.length() <= 100) {
			builder.append(content);
			builder = App.SMALLBIAOQING.convert(this, builder, biaoqingIconSize);
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
			builder = App.SMALLBIAOQING.convert(this, builder, biaoqingIconSize);
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
			builder = App.SMALLBIAOQING.convert(this, builder, biaoqingIconSize);
		}
		// ===========================设置图片==========================================
		adapterGridImage.data.clear();
		if (info.getInfo().photos.size() > 0) {
			gridImages.setVisibility(View.VISIBLE);

			final ArrayList<ImageData> images = new ArrayList<ImageData>();
			for (int i = 0; i < info.getInfo().photos.size(); i++) {
				ImageData image = new ImageData(info.getInfo().photos.get(i));
				images.add(image);
			}
			adapterGridImage.data.addAll(images);
			adapterGridImage.notifyDataSetChanged();

			int colum = info.getInfo().photos.size() - 3 > 0 ? 2 : 1;
			ViewGroup.LayoutParams gridparams = gridImages.getLayoutParams();
			gridparams.width = ImageTools.dip2px(this, 84 * 3 + 8 * 2);
			gridparams.height = ImageTools.dip2px(this, 84 * colum + 5 * (colum - 1));
			gridImages.setLayoutParams(gridparams);
			gridImages.requestLayout();

		}
		else {
			gridImages.setVisibility(View.GONE);
		}
		// ===============================获取评论信息==================================
		ArrayList<Comment> comments = info.comments;
		if (comments.size() > 0 || info.prises.size() > 0) {
			// 设置评论显示
			list.setVisibility(View.VISIBLE);
			if (comments.size() > 0 && info.prises.size() > 0) {//两者都有值，线才显示出来
				viewline.setVisibility(View.VISIBLE);
			}
			else {
				viewline.setVisibility(View.GONE);
			}
		}
		else {
			// 设置评论隐藏
			list.setVisibility(View.GONE);
		}
		rebuildCommentList();
		initZan();
		txtDistance.setText(info.getInfo().getDistance() + "km");
	}

	private void initZan() {
		// ===========================设置赞的人数初始化====================================
		ArrayList<Prise> prises = info.prises;
		if (prises != null && prises.size() > 0) {
			txtEnjoyDescription.setVisibility(View.VISIBLE);
//			viewline.setVisibility(View.VISIBLE);

			SpannableStringBuilder builder = new SpannableStringBuilder();
			int len = 0;
			for (int i = 0; i < prises.size(); i++) {
				ForegroundColorSpan fgSpan = new ForegroundColorSpan(getResources().getColor(R.color.text_highlight));
				final Prise priser = prises.get(i);
				builder.append(priser.getPriseName());
				if (i < prises.size() - 1) {
					builder.append("，");
				}
				final ClickableSpan span = new ClickableSpan() {
					@Override
					public void onClick(View widget) {
//						Intent intent = new Intent(OumenCircleDetailActivity.this, UserInfoActivity.class);
//						intent.putExtra(UserInfoActivity.INTENT_KEY_UID, priser.getPriseUid());
//						startActivity(intent);
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
			builder.setSpan(new ImageSpan(this, R.drawable.oumen_circle_zan_list, DynamicDrawableSpan.ALIGN_BOTTOM), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			txtEnjoyDescription.post(new Runnable() {

				@Override
				public void run() {
					ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) txtEnjoyDescription.getLayoutParams();
					if (txtEnjoyDescription.getLineCount() == 1) {
						params.height = lineHeight;
					}
					else {
						params.height = lineHeight + (txtEnjoyDescription.getLineCount() - 1) * txtEnjoyDescription.getLineHeight();
					}
					txtEnjoyDescription.setLayoutParams(params);
				}
			});
			txtEnjoyDescription.setText(builder);
			txtEnjoyDescription.setMovementMethod(LinkMovementMethod.getInstance());
		}
		else if (prises.size() == 0) {
			txtEnjoyDescription.setVisibility(View.GONE);
			viewline.setVisibility(View.GONE);
		}
	}

	/**
	 * 赞联网操作
	 * 
	 * @param btn联网操作的控件
	 * @param cnid赞的文章编号
	 */
	public void zan(boolean isCancel) {
		HttpRequest req;
		ELog.i("" + isCancel);
		if (isCancel) {// 取消赞
			ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
			list.add(new BasicNameValuePair("cnid", String.valueOf(info.getInfo().getCircleId())));
			list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));

			DefaultHttpCallback cancelZanCallback = new DefaultHttpCallback(new EventListener() {

				@Override
				public void onSuccess(HttpResult result) {
					try {
						String response = result.getResult();
						ELog.i(response);
						if ("1".equals(response)) {
							info.setIsprise(0);
							info.prises.remove(info.prises.size() - 1);
							ELog.i(info.toString());
							handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_ZAN_SUCCESS, "取消赞成功了"));
						}
					}

					catch (Exception e) {
						ELog.e("Exception e=" + e.getMessage());
						e.printStackTrace();
					}
				}

				@Override
				public void onForceClose(ExceptionHttpResult result) {
				}

				@Override
				public void onException(ExceptionHttpResult result) {
				}
			});

			req = new HttpRequest(Constants.OUMENCIRCLE_FAVOUR_CANCEL, list, HttpRequest.Method.GET, cancelZanCallback);
		}
		else {
			ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
			list.add(new BasicNameValuePair("cnid", String.valueOf(info.getInfo().getCircleId())));
			list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));

			DefaultHttpCallback zanCallback = new DefaultHttpCallback(new EventListener() {

				@Override
				public void onSuccess(HttpResult result) {
					try {
						String response = result.getResult();
						ELog.i(response);
						if ("1".equals(response)) {
							info.setIsprise(1);
							Prise prise = new Prise();
							prise.setPriseName(App.USER.getNickname());
							prise.setPriseUid(Integer.valueOf(App.USER.getUid()));
							info.prises.add(prise);
							handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_ZAN_SUCCESS, "赞成功了"));
						}
					}
					catch (Exception e) {
						ELog.e("Exception:" + e.getMessage());
						e.printStackTrace();
					}
				}

				@Override
				public void onForceClose(ExceptionHttpResult result) {
				}

				@Override
				public void onException(ExceptionHttpResult result) {
				}
			});
			req = new HttpRequest(Constants.OUMENCIRCLE_FAVOUR, list, HttpRequest.Method.GET, zanCallback);
		}

		App.THREAD.execute(req);
	}

	/**
	 * 删除偶们圈某一条内容
	 * 
	 * @param cnid
	 *            偶们圈内容的id
	 */
	private void deleteContent() {
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("cnid", String.valueOf(info.getInfo().getCircleId())));
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		final DefaultHttpCallback deleteMessageCallback = new DefaultHttpCallback(new EventListener() {
			@Override
			public void onSuccess(HttpResult result) {
				try {
					String response = result.getResult();
					ELog.i(response);
					if (response.contains("tip")) {
						JSONObject obj = new JSONObject(response);
						handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, obj.getString("tip")));
					}
					else {
						if ("1".equals(response)) {
							handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_DELETE_SUCCESS, "删除成功"));
						}
						else {
							handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, "删除失败"));
						}
					}
				}
				catch (Exception e) {
					ELog.e("Exception e=" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, "删除失败"));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {

			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, "删除失败"));
			}
		});
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_DELETECONTENT, list, HttpRequest.Method.GET, deleteMessageCallback);
		App.THREAD.execute(req);
	}

	/**
	 * 删除评论
	 * 
	 * @param cnid
	 *            评论id
	 * @param Oruid
	 *            要删除评论所属的偶们圈内容id
	 */
	private void deleteComment(final Comment c) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("commid", String.valueOf(c.getId())));
		params.add(new BasicNameValuePair("cnid", String.valueOf(c.getCircleId())));
		final DefaultHttpCallback deleteCommentMessageCallback = new DefaultHttpCallback(new EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String response = result.getResult();
					ELog.i(response);
					if (response.contains("tip")) {
						JSONObject obj = new JSONObject(response);
						handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, obj.getString("tip")));
					}
					else {
						if ("1".equals(response)) {
							info.comments.remove(c);
							handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_DELETE_COMMENT_SUCCESS, "删除评论成功"));
						}
						else {
							handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, "删除评论失败"));
						}
					}
				}
				catch (Exception e) {
					ELog.e("Exception e=" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, "删除评论失败"));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, "删除评论失败"));
			}
		});
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_DELETECOMMENT, params, HttpRequest.Method.GET, deleteCommentMessageCallback);
		App.THREAD.execute(req);
	}

	/**
	 * 写评论（回复偶们圈内容和回复某人的评论）
	 * 
	 */
	private void sendContent(final Comment cmt, final String content) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		if (cmt == null) {
			params.add(new BasicNameValuePair("cnid", String.valueOf(info.getInfo().getCircleId())));
			params.add(new BasicNameValuePair("oruid", ""));
		}
		else {
			ELog.i("c" + cmt.toString());
			params.add(new BasicNameValuePair("cnid", String.valueOf(info.getInfo().getCircleId())));
			params.add(new BasicNameValuePair("oruid", String.valueOf(cmt.getAuthorId())));

		}
		params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		params.add(new BasicNameValuePair("content", content));
		final DefaultHttpCallback writeCommentCallback = new DefaultHttpCallback(new EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String response = result.getResult();
					ELog.i(response);

					if (response.contains("tip")) {
						JSONObject obj = new JSONObject(response);
						handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, obj.getString("tip")));
					}
					else if (Integer.parseInt(response) > 0) {
						Comment c = new Comment();
						c.setCircleId(info.getInfo().getCircleId());
						c.setAuthorId(App.PREFS.getUid());
						c.setAuthorName(App.USER.getNickname());
						c.setContent(content);
						c.setTargetId(cmt == null ? App.INT_UNSET : cmt.getAuthorId());
						c.setTargetName(cmt == null ? null : cmt.getAuthorName());
						info.comments.add(c);
						CommentItem item = new CommentItem(OumenCircleDetailActivity.this);
						item.setOnClickListener(clicklistener);
						item.update(c);
						handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_DELETE_COMMENT_SUCCESS, item));
					}
					else {
						// handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL,
						// "发表评论失败"));
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					// handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL,
					// "发表评论失败"));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {

			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, "发表评论失败"));
			}
		});
		HttpRequest req = new HttpRequest(Constants.OUMENCIRCLE_WRITECOMMENT, params, HttpRequest.Method.POST, writeCommentCallback);
		App.THREAD.execute(req);
	}

	@Override
	public boolean handleMessage(Message msg) {

		switch (msg.what) {
			case HANDLER_TYPE_FAIL:// 失败了
				Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;

			case HANDLER_TYPE_ZAN_SUCCESS:
				int isFavour = info.getIsprise();
				if (isFavour == 0) {
					viewMore.setEnjoyText(R.string.enjoy, R.drawable.oumen_circle_zan, R.drawable.oumen_circle_more_pop_btn_bg);
				}
				else if (isFavour == 1) {
					viewMore.setEnjoyText(R.string.cancel, R.drawable.oumen_circle_zan_click, R.drawable.oumen_circle_more_pop_btn_click_bg);
				}

				ArrayList<Comment> cts = info.comments;
				if (cts.size() > 0 || info.prises.size() > 0) {
					// 设置评论显示
					list.setVisibility(View.VISIBLE);
					if (cts.size() > 0 && info.prises.size() > 0) {//两者都有值，线才显示出来
						viewline.setVisibility(View.VISIBLE);
					}
					else {
						viewline.setVisibility(View.GONE);
					}
				}
				else {
					// 设置评论隐藏
					list.setVisibility(View.GONE);
				}

				initZan();
				break;

			case HANDLER_TYPE_DELETE_SUCCESS:// 删除偶们圈
				setResult(Activity.RESULT_OK);
				finish();
				break;

			case HANDLER_TYPE_DELETE_COMMENT_SUCCESS:// 删除一条评论或者发表一条评论
				currentComment = null;
				ArrayList<Comment> comments = info.comments;

				if (comments.size() > 0 || info.prises.size() > 0) {
					// 设置评论显示
					list.setVisibility(View.VISIBLE);
					if (comments.size() > 0 && info.prises.size() > 0) {//两者都有值，线才显示出来
						viewline.setVisibility(View.VISIBLE);
					}
					else {
						viewline.setVisibility(View.GONE);
					}
					rebuildCommentList();
				}
				else {
					// 设置评论隐藏
					list.setVisibility(View.GONE);
				}
				break;

			case HANDLER_GET_DETAIL_SUCCESS:
				CircleUserMsg peer = (CircleUserMsg) msg.obj;
				if (peer != null) {
					info = peer;
					initData();
				}
				break;

			case HANDLER_GET_DETAIL_FAIL:
				Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
		}
		return super.handleMessage(msg);
	}

	private void rebuildCommentList() {
		if (list.getChildCount() > 2) {
			list.removeViews(2, list.getChildCount() - 2);
		}
		for (int i = 0; i < info.comments.size(); i++) {
			Comment cmt = info.comments.get(i);
			CommentItem item = new CommentItem(list.getContext());
			item.setOnClickListener(clicklistener);
			item.setTag(cmt);
			item.update(cmt);
			list.addView(item);
		}
	}

	//----------------------- Animation -----------------------//
	private FloatViewController floatViewController;
	private Animation animBottomIn;
	private Animation animBottomOut;

	private void initAnimation() {
		animBottomIn = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_in);
		animBottomIn.setAnimationListener(animListener);
		animBottomOut = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_out);
		animBottomOut.setAnimationListener(animListener);
	}

	private final Animation.AnimationListener animListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			floatViewController.setPlaying(false);
			if (animation == animBottomOut) {
				rootContainer.removeView(floatViewController.getRoot());
				floatViewController = null;

				popupLayer.setVisibility(View.GONE);
				popupLayer.setOnTouchListener(null);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	};
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (viewShare != null) {
			viewShare.onActivityResult(requestCode, resultCode, data);
		}
	}

	//----------------------- FloatViewHostController -----------------------//
	@Override
	public boolean isFloatViewShowing() {
		return floatViewController != null && (floatViewController.isPlaying() || floatViewController.isShowing());
	}

	@Override
	public void showFloatView(FloatViewController controller) {
		if (isFloatViewShowing())
			return;

		floatViewController = controller;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		View container = floatViewController.show();
		rootContainer.addView(container, params);
		container.startAnimation(animBottomIn);

		if (controller instanceof SoftKeyboardController) {
			SoftKeyboardController kb = (SoftKeyboardController) controller;
			kb.showSoftKeyboard();
		}

		popupLayer.setVisibility(View.VISIBLE);
		popupLayer.setOnTouchListener(this);
	}

	@Override
	public void hideFloatView() {
		if (!isFloatViewShowing())
			return;

		View container = floatViewController.hide();
		container.startAnimation(animBottomOut);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == popupLayer && event.getAction() == MotionEvent.ACTION_UP) {
			if (floatViewController != null && !floatViewController.isPlaying() && floatViewController.isShowing()) {
				hideFloatView();
			}
			if (popupWindow.isShowing())
				popupWindow.dismiss();
		}
		return true;
	}

	public View getPopupWindowLayer() {
		return popupLayer;
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	public void showShareView() {
		showFloatView(viewShare);
	}

	public void showSendView() {
		showFloatView(viewSend);
	}
}

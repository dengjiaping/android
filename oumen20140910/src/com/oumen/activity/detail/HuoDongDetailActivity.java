package com.oumen.activity.detail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.BasicMapActivity;
import com.oumen.activity.detail.comment.CommentActivity;
import com.oumen.activity.message.ActivityBean;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.util.Constants;
import com.oumen.auth.ShareView;
import com.oumen.chat.HuodongApplyerActivity;
import com.oumen.chat.ChatActivity;
import com.oumen.home.FloatViewController;
import com.oumen.home.FloatViewHostController;
import com.oumen.home.LoginConfrim;
import com.oumen.home.SoftKeyboardController;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.message.ActionType;
import com.oumen.message.ActivityMessage;
import com.oumen.message.MessageService;
import com.oumen.message.MultiChatMessage;
import com.oumen.message.SendType;
import com.oumen.message.Type;
import com.oumen.tools.ELog;
import com.oumen.widget.dialog.TwoButtonDialog;
import com.oumen.widget.dialog.VerticalTwoButtonDialog;
import com.sina.weibo.sdk.api.CmdObject;

/**
 * 
 * 活动详情展示界面
 * 
 */
public class HuoDongDetailActivity extends BaseActivity implements FloatViewHostController, View.OnTouchListener {
	public static final String INTENT_KEY_ACTIVITY_ID = "activityId";
	private final int RESULT_FROM_APPLYER_LIST = 1;

	//保存activityBean
	public final String STATE_ACTIVITY_CONTENT = "activity_content";

	private final int HANDLER_GET_DETAIL = 1;
	private final int HANDLER_APPLY = 2;
	private final int HANDLER_TYPE_FAIL = 3;

	//标题行
	private TitleBar titlebar;
	private Button btnLeft;
	private Button btnRight;

	//头部
	private HuodongDetailHeader header;

	private TextView title, price, sender;
	private TextView applyerStopTime, applyerStopContent;
	private RelativeLayout addressBar;
	private TextView address, suitableAge, period;

	private RelativeLayout pingfenBar;
	private TextView pingfen;

	private RelativeLayout detailContentBar;
	private TextView contentTip;
	private TextView content;
	private RelativeLayout rlApplyerBar;
	private TextView applyerDescrible;
	private GridView gridViewApplyers;
	//底部报名按钮
	private Button btnApply, btnPhone;
	//浮层
	private RelativeLayout rootContainer;
	private FrameLayout popupLayer;

	private final ApplyerGridAdapter adapter = new ApplyerGridAdapter();
	private ActivityBean activityBean = null;

	// 分享的popwindow
	private ShareView viewShare;
	//评分弹出框
	private VerticalTwoButtonDialog priseDialog;

	private ActivityMessage activityMsg;
	private int activityId;

	private LoginConfrim loginconfrim;

	private HuodongHttpController controller;

	private SpannableStringBuilder builder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_huodong_detail);
		init();
		viewShare = new ShareView(HuoDongDetailActivity.this);
		viewShare.setHost(HuoDongDetailActivity.this);

		controller = new HuodongHttpController(handler);
		//初始化动画
		initAnimation();
		loginconfrim = new LoginConfrim(HuoDongDetailActivity.this);

		activityId = getIntent().getIntExtra(INTENT_KEY_ACTIVITY_ID, -1);
		if (activityId != -1) {
			if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
				showProgressDialog();
				getDetail(activityId);
			}
			else {
				Toast.makeText(HuoDongDetailActivity.this, "亲，您的网络没有打开~", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
		}
		else {
			Toast.makeText(this, "获取活动详情失败", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		update();
	}

	private void init() {
		//标题行
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		titlebar.getTitle().setText(R.string.nav_activitydescrible_title);
		btnLeft = titlebar.getLeftButton();
		btnRight = titlebar.getRightButton();
		btnRight.setText(R.string.share);

		//头部
		header = (HuodongDetailHeader) findViewById(R.id.headerview);
		header.setViewDefaultHeight(getResources().getDisplayMetrics().widthPixels);
		header.setFocusable(true);
		header.setFocusableInTouchMode(true);
		header.requestFocus();
		//中间部分
		title = (TextView) findViewById(R.id.title);
		price = (TextView) findViewById(R.id.price);
		sender = (TextView) findViewById(R.id.sender);

		applyerStopTime = (TextView) findViewById(R.id.aplyer_stop_time);//TODO 
		applyerStopContent = (TextView) findViewById(R.id.stop_content);

		addressBar = (RelativeLayout) findViewById(R.id.address_container);
		address = (TextView) findViewById(R.id.address);
		suitableAge = (TextView) findViewById(R.id.age);
		period = (TextView) findViewById(R.id.period);

		pingfenBar = (RelativeLayout) findViewById(R.id.pingfen_container);
		pingfen = (TextView) findViewById(R.id.pingfen);

		detailContentBar = (RelativeLayout) findViewById(R.id.detail_container);
		contentTip = (TextView) findViewById(R.id.detail_content);

		content = (TextView) findViewById(R.id.content);
//		content.setVisibility(View.GONE);

		rlApplyerBar = (RelativeLayout) findViewById(R.id.applyer_container);
		applyerDescrible = (TextView) findViewById(R.id.applyer);

		gridViewApplyers = (GridView) findViewById(R.id.gridview);
		gridViewApplyers.setAdapter(adapter);

		//底部
		btnApply = (Button) findViewById(R.id.apply);
		btnPhone = (Button) findViewById(R.id.phone);
		// 浮层
		rootContainer = (RelativeLayout) findViewById(R.id.rootview);
		popupLayer = (FrameLayout) findViewById(R.id.layer);
		popupLayer.setOnTouchListener(this);

		btnLeft.setOnClickListener(clickListener);
		btnRight.setOnClickListener(clickListener);
		addressBar.setOnClickListener(clickListener);
		detailContentBar.setOnClickListener(clickListener);
		rlApplyerBar.setOnClickListener(clickListener);
		btnApply.setOnClickListener(clickListener);
		btnPhone.setOnClickListener(clickListener);
		pingfenBar.setOnClickListener(clickListener);
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
				setResult(Activity.RESULT_OK);
				finish();
			}
			else if (v == btnRight) {

				if (App.PREFS.getUserProfile() == null) {
					loginconfrim.openDialog();
					return;
				}

				if (activityBean == null) {
					return;
				}

				viewShare.setShareData(activityBean);
				showFloatView(viewShare);
			}
			else if (v == addressBar) {// 跳转到地图界面
				if (activityBean == null) {
					return;
				}
				if (!"0".equals(activityBean.getLat()) && !"0".equals(activityBean.getLng())) {
					Intent intent1 = new Intent(HuoDongDetailActivity.this, BasicMapActivity.class);
					Bundle b = new Bundle();
					b.putParcelable("amusementmap", activityBean);
					intent1.putExtras(b);
					startActivity(intent1);
				}
				else {
					address.setClickable(false);
				}
			}
			else if (v == rlApplyerBar) {
				if (activityBean == null) {
					return;
				}
				//如果没有参与者，就不跳转界面了 
				if (activityBean.applyers.size() > 0) {
					Intent i = new Intent(HuoDongDetailActivity.this, HuodongApplyerActivity.class);
					i.putExtra(HuodongApplyerActivity.HUODONG_MSG, activityBean);
					i.putExtra(HuodongApplyerActivity.FROM_ACTIVITY_TAG, HuodongApplyerActivity.FROM_HUODONG_DETAIL);
					startActivityForResult(i, RESULT_FROM_APPLYER_LIST);
				}
			}
			else if (v == btnPhone) {
				if (activityBean == null) {
					return;
				}
				if (TextUtils.isEmpty(activityBean.getAskPhone())) {
					return;
				}
				final TwoButtonDialog tip = new TwoButtonDialog(HuoDongDetailActivity.this);
				tip.getTitleView().setText("偶们提示");
				tip.getMessageView().setText("是否拨打" + activityBean.getAskPhone() + "？");
				tip.getMessageView().setGravity(Gravity.LEFT);
				tip.getLeftButton().setText("拨打");
				tip.getLeftButton().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 拨打电话
						Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + activityBean.getAskPhone()));
						startActivity(intent);
						tip.dismiss();
					}
				});
				tip.getRightButton().setText("返回");
				tip.getRightButton().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						tip.dismiss();
					}
				});
				tip.show();
			}
			else if (v == btnApply) {
				if (App.PREFS.getUserProfile() == null) {
					loginconfrim.openDialog();
					return;
				}
				if (activityBean == null) {
					return;
				}
				//如果已报名，或者是发起者，就直接跳转到群聊界面，不是再进行报名
				ELog.i(activityBean.toString());
				if (activityBean.isApply() || activityBean.getSenderUid() == App.PREFS.getUid()) {
					openChatActivity();
				}
				else {
					apply(activityBean.getId());
				}
			}
			else if (v == detailContentBar) {
				if (content.getVisibility() == View.GONE) {
					content.setVisibility(View.VISIBLE);
					contentTip.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.arrow_up), null);
				}
				else {
					content.setVisibility(View.GONE);
					contentTip.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.arrow_open), null);
				}
			}
			else if (v == pingfenBar) {
				// TODO 判断是否需要弹出好评的dialog
//				if (activityBean.isStartPingfen()) {
//					controller.checkNeedPingFen(activityId);
//				}
				Intent intent = new Intent(HuoDongDetailActivity.this, CommentActivity.class);
				intent.putExtra(CommentActivity.INTENT_HUODONG_ID, activityId);
				intent.putExtra(CommentActivity.INTENT_HUODONG_APPLY, activityBean.isApply());
				startActivity(intent);
			}
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_GET_DETAIL:
				dismissProgressDialog();
				if (msg.obj != null) {
					if (msg.obj instanceof ActivityBean) {
						activityBean = (ActivityBean) msg.obj;
						adapter.data.clear();
						int count = activityBean.applyers.size() < 5 ? activityBean.applyers.size() : 5;
						for (int i = 0; i < count; i++) {
							adapter.data.add(activityBean.applyers.get(i));
						}
						update();
						adapter.notifyDataSetChanged();
					}
					else if (msg.obj instanceof String) {
						Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case HANDLER_APPLY:

				activityBean.setApply(true);

				final TwoButtonDialog tip = new TwoButtonDialog(HuoDongDetailActivity.this);
				tip.getTitleView().setText("恭喜您，报名成功");
				tip.getMessageView().setText("是否立即进入活动群聊了解更多详情");
				tip.getMessageView().setGravity(Gravity.LEFT);
				tip.getLeftButton().setText("是");
				tip.getLeftButton().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						openChatActivity();
						tip.dismiss();
					}
				});
				tip.getRightButton().setText("否");
				tip.getRightButton().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						tip.dismiss();
					}
				});
				tip.show();
				//设置btn颜色值

				btnApply.setText(getResources().getString(R.string.activity_apply_success));
				break;
			case HANDLER_TYPE_FAIL:// 失败了
				if (msg.obj != null) {
					Toast.makeText(HuoDongDetailActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				}
				break;
			case HuodongHttpController.HANDLER_NEED_PING_FEN:// 需要评分
				if (priseDialog == null) {
					priseDialog = new VerticalTwoButtonDialog(HuoDongDetailActivity.this);
					priseDialog.getTitleView().setText("活动评价");
					priseDialog.getMessageView().setText(R.string.huodong_detail_prise_tip);
					priseDialog.getTopButton().setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {// 好评
							activityBean.setHaoPing(activityBean.getHaoPing() + 1);
							controller.setPingfen(activityId, 1);
							priseDialog.dismiss();
						}
					});

					priseDialog.getButtomButton().setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {// 差评
							activityBean.setChaPing(activityBean.getChaPing() + 1);
							controller.setPingfen(activityId, 0);
							priseDialog.dismiss();
						}
					});
				}
				priseDialog.show();
				break;

			case HuodongHttpController.HANDLER_NO_NEED_PING_FEN://不需要评分
				Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;

			case HuodongHttpController.HANDLER_PINGFEN_SUCCESS:// 评分成功
				// 修改对应的文字
				setHaoping();
				break;

			case HuodongHttpController.HANDLER_PINGFEN_FAIL: //评分失败
				Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
		}
		return false;
	}

	/**
	 * 添加初始化数据
	 */
	private void update() {
		if (activityBean != null) {
			//TODO 更新头部信息
			header.update(activityBean);

			ELog.i(activityBean.getPicSourceUrl());
			ELog.i("---->" + activityBean.getHuodongId());
			title.setText(activityBean.getName());
			//activityBean.getMoney().startsWith("0")
			if ("0".equals(activityBean.getMoney())) {
				builder = new SpannableStringBuilder();
				String str = "免费" + " 活动由商家提供";
				builder.append(str);
				ForegroundColorSpan tipColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.detail_free_tip));
				AbsoluteSizeSpan tipSizeSpen = new AbsoluteSizeSpan(21 * 2);
				builder.setSpan(tipColorSpan, 0, "免费".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				builder.setSpan(tipSizeSpen, 0, "免费".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				ForegroundColorSpan contentColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.detail_free_content));
				AbsoluteSizeSpan contentSizeSpen = new AbsoluteSizeSpan(16 * 2);
				builder.setSpan(contentColorSpan, "免费".length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				builder.setSpan(contentSizeSpen, "免费".length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				price.setText(builder);
			}
			else {
				price.setText("¥" + activityBean.getMoney() + "元");
			}

			sender.setText(activityBean.getSenderName());

			setHaoping();

			// TODO 
			/*
			 * 1.如果本活动是自己发起的，就显示“进入群聊”
			 * 2.如果报名人数已购，就显示“名额已满”
			 * 3.如果时间过了报名截止时间，就显示“报名已截止”
			 * 4.如果已经报名了，就显示“已报名，直接进入活动群”
			 */
			if (activityBean.getLimitNum().matches(Constants.PATTERN_NUMBER)) {
				if (activityBean.applyers.size() == Integer.valueOf(activityBean.getLimitNum())) {
					btnApply.setText("名额已满");
					btnApply.setBackgroundColor(getResources().getColor(R.color.detail_apply_grey));
					btnApply.setTextColor(getResources().getColor(R.color.detail_apply_grey_text));
					btnApply.setClickable(false);
				}
			}

			try {
				Date timeTip = App.YYYY_MM_DD_HH_MM_FORMAT.parse(activityBean.getApplyEndTime());
				if (App.getServerTime() / 1000 - timeTip.getTime() / 1000 >= 0) {// 报名截止
					btnApply.setText("报名已截止");
					btnApply.setBackgroundColor(getResources().getColor(R.color.detail_apply_grey));
					btnApply.setTextColor(getResources().getColor(R.color.detail_apply_grey_text));
					btnApply.setClickable(false);
				}
			}
			catch (ParseException e) {
				e.printStackTrace();
			}

			if (activityBean.getSenderUid() == App.PREFS.getUid()) {
				btnApply.setClickable(true);
				btnApply.setText(getResources().getString(R.string.activity_apply_sender_success));
			}
			else if (activityBean.isApply()) {
				btnApply.setClickable(true);
				btnApply.setText(getResources().getString(R.string.activity_apply_success));
			}

			try {
				//===============================================================================================
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(App.YYYY_MM_DD_HH_MM_FORMAT.parse(activityBean.getApplyEndTime()));

				String str = calendar.get(Calendar.YEAR) + "年\n" + calendar.get(Calendar.MONTH) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日\n报名截止";
				builder = new SpannableStringBuilder();
				builder.append(str);
				AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(22 * 2);
				int len = (calendar.get(Calendar.YEAR) + "年\n").length();
				builder.setSpan(sizeSpan, len, len + (calendar.get(Calendar.MONTH) + "月" + +calendar.get(Calendar.DAY_OF_MONTH) + "日\n").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				applyerStopTime.setText(builder);

				Calendar startTime = Calendar.getInstance();
				Calendar endTime = Calendar.getInstance();
				startTime.setTime(App.YYYY_MM_DD_HH_MM_FORMAT.parse(activityBean.getStartTime()));
				endTime.setTime(App.YYYY_MM_DD_HH_MM_FORMAT.parse(activityBean.getEndTime()));
				long hours = (endTime.getTimeInMillis() - startTime.getTimeInMillis()) / (1000 * 60 * 60);

				String str1 = null;
				builder = new SpannableStringBuilder();
				if (activityBean.getLimitNum().matches(Constants.PATTERN_NUMBER)) {
					int len1 = Integer.valueOf(activityBean.getLimitNum()) - activityBean.applyers.size();
					if (len1 < 0) {
						len1 = 0;
					}
					str1 = "仅剩" + String.valueOf(len1) + "个名额\n";
					String tempStr = str1 + hours / 24 + "天" + hours % 24 + "小时结束";
					builder.append(tempStr);
					ForegroundColorSpan colorSpen = new ForegroundColorSpan(getResources().getColor(R.color.detail_orange));
					builder.setSpan(colorSpen, "仅剩".length(), ("仅剩" + String.valueOf(len1)).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					ForegroundColorSpan colorSpen1 = new ForegroundColorSpan(getResources().getColor(R.color.detail_orange));
					builder.setSpan(colorSpen1, str1.length(), tempStr.length() - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				else {
					str1 = "不限名额\n";
					String tempStr = str1 + hours / 24 + "天" + hours % 24 + "小时结束";
					builder.append(tempStr);
					ForegroundColorSpan colorSpen1 = new ForegroundColorSpan(getResources().getColor(R.color.detail_orange));
					builder.setSpan(colorSpen1, str1.length(), tempStr.length() - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				}
				applyerStopContent.setText(builder);

				String str2 = (startTime.get(Calendar.MONTH) + 1) + "月" + startTime.get(Calendar.DAY_OF_MONTH) + "日 — " + (endTime.get(Calendar.MONTH) + 1) + "月" + endTime.get(Calendar.DAY_OF_MONTH) + "日";
				period.setText(str2);
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
			// 活动详情，适宜年龄，人数限制，咨询电话，费用
			if (!TextUtils.isEmpty(activityBean.getApplyaAge())) {
				int type = Integer.valueOf(activityBean.getApplyaAge());
				switch (type) {
					case 0:
						suitableAge.setText("准备怀孕");
						break;
					case 1:
						suitableAge.setText("怀孕期");
						break;
					case 2:
						suitableAge.setText("0-1岁");
						break;
					case 3:
						suitableAge.setText("1-3岁");
						break;
					case 4:
						suitableAge.setText("3-6岁");
						break;
					case 5:
						suitableAge.setText("6岁以上");
						break;
					case 6:
						suitableAge.setText("不限");
						break;
					default:
						suitableAge.setText("不限");
						break;
				}
			}
			else {
				suitableAge.setText("不限");
			}
			// 活动描述
			content.setText(activityBean.getDescription());
			// 目前参加人数，和总人数
			builder = new SpannableStringBuilder();
			String tStr = activityBean.getApplyNum() + "/" + activityBean.getLimitNum();
			builder.append(tStr);
			ForegroundColorSpan colorSpen1 = new ForegroundColorSpan(getResources().getColor(R.color.detail_orange));
			builder.setSpan(colorSpen1, 0, String.valueOf(activityBean.applyers.size()).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			applyerDescrible.setText(builder);

			// TODO 判断是否显示评分(如果为发起方，就不显示)
			address.setText(activityBean.getAddress());
		}
	}

	public void setHaoping() {
		if (activityBean.getHaoPing() == 0 && activityBean.getChaPing() == 0) {
			builder = new SpannableStringBuilder();
			builder.append("好评率:100%");
			ForegroundColorSpan colorSpen = new ForegroundColorSpan(getResources().getColor(R.color.default_bg));
			builder.setSpan(colorSpen, "好评率:".length(), ("好评率:100%").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(18 * 2);
			builder.setSpan(sizeSpan, "好评率:".length(), ("好评率:100%").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			pingfen.setText(builder);
		}
		else {
			ELog.i("provider.getHaoPing() = " + activityBean.getHaoPing() + ",provider.getChaPing() = " + activityBean.getChaPing());
			int temp = (activityBean.getHaoPing() / (activityBean.getHaoPing() + activityBean.getChaPing())) * 100;
			builder = new SpannableStringBuilder();
			builder.append("好评率" + temp + "%");
			ForegroundColorSpan colorSpen = new ForegroundColorSpan(getResources().getColor(R.color.default_bg));
			builder.setSpan(colorSpen, "好评率".length(), ("好评率" + temp + "%").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(18 * 2);
			builder.setSpan(sizeSpan, "好评率".length(), ("好评率" + temp + "%").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			pingfen.setText(builder);
		}
	}

	/**
	 * 跳转到聊天界面
	 */
	private void openChatActivity() {
		int selfUid = App.PREFS.getUid();
		ActivityMessage.insert(selfUid, activityMsg, App.DB);

		boolean hasCreatedMessage = MultiChatMessage.hasActivityCreateMessage(selfUid, activityMsg.getMultiId(), App.DB);
		ELog.i("Has created message:" + hasCreatedMessage);
		MultiChatMessage multiMsg = new MultiChatMessage();

		multiMsg.setSelfName(App.USER.getNickname());
		multiMsg.setSelfPhotoUrl(App.USER.getPhotoSourceUrl());
		multiMsg.setActionType(ActionType.MULTI_CREATE);
		multiMsg.setType(Type.TEXT);
		multiMsg.setActivityId(activityMsg.getId());
		multiMsg.setMultiId(activityMsg.getMultiId());
//			multiMsg.setRead(true);
		multiMsg.setSendType(SendType.READ);
		multiMsg.setDatetime(new Date(App.getServerTime()));
		if (activityMsg.getOwnerId() == selfUid) {
			multiMsg.setActionType(ActionType.MULTI_CREATE);
			multiMsg.setContent(MultiChatMessage.getCreateMultiChatInfo(mBaseApplication));
		}
		else {
			multiMsg.setActionType(ActionType.MULTI_JOIN);
			multiMsg.setTargetId(App.USER.getUid());
			multiMsg.setContent(MultiChatMessage.getJoinMultiChatInfo(mBaseApplication, activityMsg.getTitle()));
		}

		if (!hasCreatedMessage) {
			MultiChatMessage.insert(selfUid, multiMsg, App.DB);
		}
		multiMsg.setActivityMessage(new ActivityMessage(activityBean));
		//更新底部消息提醒

		Intent intent = new Intent(mBaseApplication, ChatActivity.class);
		intent.putExtra(ChatActivity.REQUEST_MESSAGE, multiMsg);

		startActivity(intent);

	}

	/**
	 * 获取活动详情
	 * 
	 * @param activityId
	 */
	private void getDetail(int activityId) {
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("atid", String.valueOf(activityId)));
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		HttpRequest req = new HttpRequest(Constants.GET_AMUSEMENT_DETAIL, list, HttpRequest.Method.GET, reqDetailCallback);
		App.THREAD.execute(req);
	}

	private final DefaultHttpCallback reqDetailCallback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);

				JSONObject obj = new JSONObject(str);
				activityMsg = new ActivityMessage(obj, ActivityMessage.FROM_HTTP);
				ActivityBean bean = new ActivityBean(obj);

				handler.sendMessage(handler.obtainMessage(HANDLER_GET_DETAIL, bean));
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				handler.sendMessage(handler.obtainMessage(HANDLER_GET_DETAIL, "获取活动失败"));
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_GET_DETAIL, "获取活动失败"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_GET_DETAIL, "获取活动失败"));
		}
	});

	/**
	 * 报名参加活动
	 * 
	 * @param aid
	 */
	private void apply(int aid) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		params.add(new BasicNameValuePair("atid", String.valueOf(aid)));
//		params.add(new BasicNameValuePair("phone", phone));
		HttpRequest req = new HttpRequest(Constants.OUMENAMUSEMENT_APPLY, params, HttpRequest.Method.POST, reqApplyCallback);
		App.THREAD.execute(req);
	}

	private final DefaultHttpCallback reqApplyCallback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);

				JSONObject obj = new JSONObject(str);
				String status = obj.getString("status");
				String tip;
				if ("success".equals(status)) {
					tip = obj.getString("msg");
					getDetail(activityBean.getId());
					handler.sendMessage(handler.obtainMessage(HANDLER_APPLY, "报名成功"));
				}
				else {
					tip = obj.getString("msg");
					handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, tip));
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, "报名失败"));
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, "报名失败"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_TYPE_FAIL, "报名失败"));
		}
	});

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == RESULT_FROM_APPLYER_LIST) {
				int flag = data.getIntExtra(HuodongApplyerActivity.BACK_FROM_APPLYER_LIST, App.INT_UNSET);
				if (flag != App.INT_UNSET && flag == HuodongApplyerActivity.BACK_UPDATE) {
					btnApply.setText("点击报名");
					if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
						getDetail(activityId);
					}
					else {
						Toast.makeText(HuoDongDetailActivity.this, "亲，您的网络没有打开~", Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
		if (viewShare != null) {
			viewShare.onActivityResult(requestCode, resultCode, data);
		}
	}

	// ---------- Animation ----------//
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
	public Activity getActivity() {
		return HuoDongDetailActivity.this;
	}

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
		}
		return true;
	};

}

package com.oumen.chat;

import java.util.Date;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.UserProfile;
import com.oumen.message.ActionType;
import com.oumen.message.ActivityMessage;
import com.oumen.message.BaseMessage;
import com.oumen.message.ChatMessage;
import com.oumen.message.MultiChatMessage;
import com.oumen.message.SendType;
import com.oumen.message.Type;
import com.oumen.tools.ELog;
import com.oumen.user.UserInfoActivity;
import com.oumen.widget.gifview.GifMovieView;

/**
 * 聊天的布局（单聊，群聊） 消息的总类有6中类型
 * 1.活动报名成功以后显示的活动消息
 * 2.活动描述消息(参加活动成功，或者有多少人参加了活动)
 * 3.别人发过来的文字消息
 * 4.别人发过来的gif图片消息
 * 5.自己发出去的文字消息
 * 6.自己发出去的gif图片消息
 * 
 */
public class ChatItem extends LinearLayout {
	// 时间
	private TextView tvTime;

	// 活动描述(参加活动成功，或者有多少人参加了活动)
	private TextView TvDescrible;
	// 左边的布局
	private RelativeLayout LeftRelayout;
	private ImageView leftPhoto;
	private TextView leftName;
	private TextView leftContent;
	private GifMovieView leftGifView;
	private ImageView leftImage;
	// 右边的布局
	private RelativeLayout rightRelayout;
	private ImageView rightPhoto;
	private TextView rightName;
	private TextView rightContent;
	private GifMovieView rightGifView;
	private ImageView rightImage;
	// TODO 新增两个状态，发送中的状态和发送失败的状态
	private ProgressBar sendProgress;
	private ImageView sendFail;

	protected BaseMessage provider;

	private ActivityMessage huodongMsg = null;

	private Context context;

	public static final DisplayImageOptions OPTIONS_CHAT_HEAD = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.round_user_photo).showImageOnFail(R.drawable.round_user_photo).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).build();

	public ChatItem(Context context) {
		this(context, null, 0);
	}

	public ChatItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public void setMessage(ActivityMessage message) {
		this.huodongMsg = message;
	}

	public ChatItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.context = context;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.chat_item, this, true);

		tvTime = (TextView) findViewById(R.id.sendtime);

		TvDescrible = (TextView) findViewById(R.id.describle);

		LeftRelayout = (RelativeLayout) findViewById(R.id.left);
		leftContent = (TextView) findViewById(R.id.left_content);
		leftPhoto = (ImageView) findViewById(R.id.left_icon);
		leftName = (TextView) findViewById(R.id.left_name);
		leftGifView = (GifMovieView) findViewById(R.id.left_gif);
		leftImage = (ImageView) findViewById(R.id.left_image);

		rightRelayout = (RelativeLayout) findViewById(R.id.right);
		rightContent = (TextView) findViewById(R.id.right_content);
		rightPhoto = (ImageView) findViewById(R.id.right_icon);
		rightName = (TextView) findViewById(R.id.right_name);
		rightGifView = (GifMovieView) findViewById(R.id.right_gif);
		rightImage = (ImageView) findViewById(R.id.right_image);
		sendProgress = (ProgressBar) findViewById(R.id.right_progress);
		sendFail = (ImageView) findViewById(R.id.right_send_fail);

		TvDescrible.setVisibility(View.GONE);

		LeftRelayout.setVisibility(View.GONE);
		leftContent.setVisibility(View.GONE);
		leftGifView.setVisibility(View.GONE);
		leftImage.setVisibility(View.GONE);

		rightRelayout.setVisibility(View.GONE);
		rightContent.setVisibility(View.GONE);
		rightGifView.setVisibility(View.GONE);
		rightImage.setVisibility(View.GONE);
		sendProgress.setVisibility(View.GONE);
		sendFail.setVisibility(View.GONE);

		leftPhoto.setOnClickListener(clickListener);
		rightPhoto.setOnClickListener(clickListener);
//		sendFail.setOnClickListener(clickListener);

	}

	public void setOnReSendListener(View.OnClickListener sendListener) {
		sendFail.setOnClickListener(sendListener);
		sendFail.setTag(provider);
	}

	void update(BaseMessage message, long minTime) {
		this.provider = message;
		//TODO 
		UserProfile profile = App.USER;
		if (profile == null) {
			//取用户信息
			try {
				String res = App.PREFS.getUserProfile();
				if (res != null) {
					JSONObject obj = new JSONObject(res);
					UserProfile p = new UserProfile(obj);

//					App.PREFS.setLatitude(p.getLatitude());
//					App.PREFS.setLongitude(p.getLongitude());

					App.USER.copyFrom(p);
					profile = App.USER;
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
			}
		}
		// ========================TODO　时间的添加=======================================
		if (provider.getDatetime() != null) {// 有时间就显示，没有就显示当前时间
			if (minTime != 0 && provider.getDatetime().getTime() - minTime < 60 * 1000) {// 两条时间小于60秒就不显示时间了
				tvTime.setVisibility(View.GONE);
			}
			else {
				tvTime.setVisibility(View.VISIBLE);
				tvTime.setText(App.YYYY_MM_DD_HH_MM_FORMAT.format(provider.getDatetime()));
			}
		}
		else {
			tvTime.setVisibility(View.VISIBLE);
			tvTime.setText(App.YYYY_MM_DD_HH_MM_FORMAT.format(new Date(App.getServerTime())));
		}
		// 判断消息类型
		ActionType type = provider.getActionType();

		// ===============================================TODO 私聊====================================================
		if (type.equals(ActionType.CHAT)) {
			TvDescrible.setVisibility(View.GONE);

			ChatMessage msg = (ChatMessage) provider;
			// ===================发出去的消息==================
			if (msg.isSend()) {
				// 1.显示出右边的布局
				LeftRelayout.setVisibility(View.GONE);
				rightRelayout.setVisibility(View.VISIBLE);

				// TODO 新增消息状态
				if (SendType.SENDING.equals(msg.getSendType())) {// 正在发送
					sendProgress.setVisibility(View.VISIBLE);
					sendFail.setVisibility(View.GONE);
				}
				else if (SendType.SENDFAIL.equals(msg.getSendType())) {// 发送失败
					sendProgress.setVisibility(View.GONE);
					sendFail.setVisibility(View.VISIBLE);
				}
				else {
					sendProgress.setVisibility(View.GONE);
					sendFail.setVisibility(View.GONE);
				}

				// 2.头像的显示
				if (profile.getPhotoSourceUrl() != null) {
					ImageLoader.getInstance().displayImage(profile.getPhotoUrl(2 * getResources().getDimensionPixelSize(R.dimen.default_photo_size)), rightPhoto, OPTIONS_CHAT_HEAD, App.CIRCLE_IMAGE_LOADING_LISTENER);
				}
				else {
					rightPhoto.setImageResource(R.drawable.round_user_photo);
				}
				rightName.setText(profile.getNickname());

				// 3.内容的显示
				Type t = provider.getType();
				if (Type.TEXT.equals(t)) {// 文字消息
					rightContent.setVisibility(View.VISIBLE);
					rightGifView.setVisibility(View.GONE);
					rightImage.setVisibility(View.GONE);

					if (!TextUtils.isEmpty(msg.getContent())) {
						//TODO　小表情转化
						SpannableStringBuilder builder = new SpannableStringBuilder(msg.getContent());
						builder = App.SMALLBIAOQING.convert(getContext(), builder, App.INT_UNSET);
						rightContent.setText(builder);
					}
				}
				else if (Type.OUBA.equals(t)) {// 欧巴表情消息
					rightContent.setVisibility(View.GONE);
					rightGifView.setVisibility(View.VISIBLE);
					rightImage.setVisibility(View.GONE);

//					rightGifView.setMovieResource(biaoQingList[Integer.valueOf(msg.getContent()) - 10]);
					//GIF表情
					rightGifView.setMovieResource(App.getChatBiaoqingOubaPath() + "/" + msg.getContent());
				}
				else if (Type.CIWEI.equals(t)) {//刺猬表情

					rightContent.setVisibility(View.GONE);
					rightGifView.setVisibility(View.GONE);
					rightImage.setVisibility(View.VISIBLE);

					ImageLoader.getInstance().displayImage(App.SCHEMA_FILE + App.getChatBiaoqingCiweiPath() + "/" + msg.getContent(), rightImage, App.OPTIONS_PIC);
				}
				return;
			}
			// =====================接收到的消息==================
			else {
				// 显示出左边的布局
				LeftRelayout.setVisibility(View.VISIBLE);
				rightRelayout.setVisibility(View.GONE);

				// 头像的显示
				if (provider.getTargetUid() == ChatMessage.OUMEN_TEAM_ID) {// 偶们团队
					leftPhoto.setImageResource(R.drawable.avatar_omen);
					leftName.setText(R.string.oumen_team);
				}
				else {// 其他人发过来的
					if (TextUtils.isEmpty(msg.getTargetPhotoSourceUrl())) {
						leftPhoto.setImageResource(R.drawable.round_user_photo);
					}
					else {
						ImageLoader.getInstance().displayImage(msg.getTargetPhotoUrl(2 * getResources().getDimensionPixelSize(R.dimen.default_photo_size)), leftPhoto, OPTIONS_CHAT_HEAD, App.CIRCLE_IMAGE_LOADING_LISTENER);
					}
					leftName.setText(provider.getTargetNickname());
				}
				// 内容的显示
				Type t = provider.getType();
				if (Type.TEXT.equals(t)) {// 文字消息
					leftContent.setVisibility(View.VISIBLE);
					leftGifView.setVisibility(View.GONE);
					leftImage.setVisibility(View.GONE);

					if (!TextUtils.isEmpty(provider.getContent())) {
						SpannableStringBuilder builder = new SpannableStringBuilder(provider.getContent());
						builder = App.SMALLBIAOQING.convert(getContext(), builder, App.INT_UNSET);
						leftContent.setText(builder);
					}
				}
				else if (Type.OUBA.equals(t)) {// 表情消息
					leftContent.setVisibility(View.GONE);
					leftGifView.setVisibility(View.VISIBLE);
					leftImage.setVisibility(View.GONE);

//					leftGifView.setMovieResource(biaoQingList[Integer.valueOf(provider.getContent()) - 10]);
					leftGifView.setMovieResource(App.getChatBiaoqingOubaPath() + "/" + provider.getContent());
				}
				else if (Type.CIWEI.equals(t)) {// 刺猬表情
					leftContent.setVisibility(View.GONE);
					leftGifView.setVisibility(View.GONE);
					leftImage.setVisibility(View.VISIBLE);

					ImageLoader.getInstance().displayImage(App.SCHEMA_FILE + App.getChatBiaoqingCiweiPath() + "/" + provider.getContent(), leftImage, App.OPTIONS_PIC);
				}
				return;
			}
		}
		//=======================================TODO　群聊===========================================
		else if (type.equals(ActionType.ACTIVITY_MULTI_CHAT)) {
			TvDescrible.setVisibility(View.GONE);

			MultiChatMessage msg = (MultiChatMessage) provider;
			// ===================发出去的消息===========================
			if (msg.isSend()) {
				// 显示出右边的布局
				LeftRelayout.setVisibility(View.GONE);
				rightRelayout.setVisibility(View.VISIBLE);

				// 头像的显示
				if (provider.getToId() == Integer.valueOf(profile.getUid())) {
					if (profile.getPhotoSourceUrl() != null) {
						ImageLoader.getInstance().displayImage(profile.getPhotoUrl(2 * getResources().getDimensionPixelSize(R.dimen.default_photo_size)), rightPhoto, OPTIONS_CHAT_HEAD, App.CIRCLE_IMAGE_LOADING_LISTENER);
					}
					else {
						rightPhoto.setImageResource(R.drawable.round_user_photo);
					}
				}
				rightName.setText(profile.getNickname());

				// TODO 新增消息状态
				if (SendType.SENDING.equals(msg.getSendType())) {// 正在发送
					sendProgress.setVisibility(View.VISIBLE);
					sendFail.setVisibility(View.GONE);
				}
				else if (SendType.SENDFAIL.equals(msg.getSendType())) {// 发送失败
					sendProgress.setVisibility(View.GONE);
					sendFail.setVisibility(View.VISIBLE);
				}
				else {
					sendProgress.setVisibility(View.GONE);
					sendFail.setVisibility(View.GONE);
				}

				// 内容的显示
				Type t = provider.getType();
				if (Type.TEXT.equals(t)) {// 文字消息
					// 发出去的
					rightContent.setVisibility(View.VISIBLE);
					rightGifView.setVisibility(View.GONE);
					rightImage.setVisibility(View.GONE);

					if (!TextUtils.isEmpty(msg.getContent())) {
						SpannableStringBuilder builder = new SpannableStringBuilder(msg.getContent());
						builder = App.SMALLBIAOQING.convert(getContext(), builder, App.INT_UNSET);
						rightContent.setText(builder);
					}
				}
				else if (Type.OUBA.equals(t)) {// 表情消息
					// 发出去的
					rightContent.setVisibility(View.GONE);
					rightGifView.setVisibility(View.VISIBLE);
					rightImage.setVisibility(View.GONE);

					rightGifView.setMovieResource(App.getChatBiaoqingOubaPath() + "/" + msg.getContent());
				}
				else if (Type.CIWEI.equals(t)) {//刺猬表情

					rightContent.setVisibility(View.GONE);
					rightGifView.setVisibility(View.GONE);
					rightImage.setVisibility(View.VISIBLE);

					ImageLoader.getInstance().displayImage(App.SCHEMA_FILE + App.getChatBiaoqingCiweiPath() + "/" + msg.getContent(), rightImage, App.OPTIONS_PIC);
				}
				return;
			}
			// =====================接收到的消息=========================
			else {
				// 显示出左边的布局
				LeftRelayout.setVisibility(View.VISIBLE);
				rightRelayout.setVisibility(View.GONE);

				// 头像的显示
				if (provider.getTargetUid() == ChatMessage.OUMEN_TEAM_ID) {// 偶们团队
					leftPhoto.setImageResource(R.drawable.avatar_omen);
					leftName.setText(R.string.oumen_team);
				}
				else {// 其他人发过来的
					if (TextUtils.isEmpty(msg.getTargetPhotoSourceUrl())) {
						leftPhoto.setImageResource(R.drawable.round_user_photo);
					}
					else {
						ImageLoader.getInstance().displayImage(msg.getTargetPhotoUrl(2 * getResources().getDimensionPixelSize(R.dimen.default_photo_size)), leftPhoto, OPTIONS_CHAT_HEAD, App.CIRCLE_IMAGE_LOADING_LISTENER);
					}
					leftName.setText(msg.getTargetNickname());
				}
				// 内容的显示
				Type t = provider.getType();
				if (Type.TEXT.equals(t)) {// 文字消息
					leftContent.setVisibility(View.VISIBLE);
					leftGifView.setVisibility(View.GONE);
					leftImage.setVisibility(View.GONE);

					if (!TextUtils.isEmpty(provider.getContent())) {
						SpannableStringBuilder builder = new SpannableStringBuilder(provider.getContent());
						builder = App.SMALLBIAOQING.convert(getContext(), builder, App.INT_UNSET);
						leftContent.setText(builder);
					}
				}
				else if (Type.OUBA.equals(t)) {// 表情消息
					leftContent.setVisibility(View.GONE);
					leftGifView.setVisibility(View.VISIBLE);
					leftImage.setVisibility(View.GONE);

					leftGifView.setMovieResource(App.getChatBiaoqingOubaPath() + "/" + provider.getContent());
				}
				else if (Type.CIWEI.equals(t)) {// 刺猬表情
					leftContent.setVisibility(View.GONE);
					leftGifView.setVisibility(View.GONE);
					leftImage.setVisibility(View.VISIBLE);

					ImageLoader.getInstance().displayImage(App.SCHEMA_FILE + App.getChatBiaoqingCiweiPath() + "/" + provider.getContent(), leftImage, App.OPTIONS_PIC);
				}
				return;
			}

		}
		else if (type.equals(ActionType.MULTI_JOIN) || type.equals(ActionType.MULTI_CREATE)) {// 加入群聊
			MultiChatMessage msg = (MultiChatMessage) provider;
			LeftRelayout.setVisibility(View.GONE);
			leftContent.setVisibility(View.GONE);
			leftGifView.setVisibility(View.GONE);

			rightRelayout.setVisibility(View.GONE);
			rightContent.setVisibility(View.GONE);
			rightGifView.setVisibility(View.GONE);

			if (type.equals(ActionType.MULTI_CREATE) && huodongMsg.getOwnerId() == App.USER.getUid()) {// 如果为活动创建，并且是本用户创建的就不显示此条消息
				tvTime.setVisibility(View.GONE);
				return;
			}
			else {
				if (msg.getTargetUid() == App.USER.getUid()) {// TODO　如果是本人发起的活动，本人加入该活动，就不显示本条消息了
					tvTime.setVisibility(View.GONE);
					return;
				}
			}

			TvDescrible.setVisibility(View.VISIBLE);
			TvDescrible.setText(provider.getContent());
		}
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ELog.i("");
			switch (v.getId()) {
				case R.id.left_icon:// 左侧头像的点击事件
					if (provider.getTargetUid() == ChatMessage.OUMEN_TEAM_ID) {// 如果为偶们团队，就不让点击了
						return;
					}

					Intent intent = new Intent(context, UserInfoActivity.class);
					ELog.i(String.valueOf(provider.getTargetUid()));
					intent.putExtra(UserInfoActivity.INTENT_KEY_UID, provider.getTargetUid());
					context.startActivity(intent);
					break;
				case R.id.right_icon:// 右侧头像的点击事件
					intent = new Intent(context, UserInfoActivity.class);
					intent.putExtra(UserInfoActivity.INTENT_KEY_UID, App.USER.getUid());
					context.startActivity(intent);
					break;
			}
		}
	};

}

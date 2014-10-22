package com.oumen.chat;

import com.oumen.R;
import com.oumen.activity.detail.HuodongHttpController;
import com.oumen.activity.detail.HuodongDetailHeaderProvider;
import com.oumen.activity.detail.cell.ActivityDetailHeaderView;
import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.message.MessageService;
import com.oumen.message.MultiChatMessage;
import com.oumen.tools.ELog;
import com.oumen.widget.dialog.PhoneNumberDialog;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * 群聊界面头部
 * 
 */
public class MultiChatHeaderView extends LinearLayout implements Callback {

	private TextView txtApplyTime;
	private ActivityDetailHeaderView headerView;
	private TextView tvDescrible;
	private TextView tvPhoneDescrible;

	private PhoneNumberDialog dialogInput;

	private Handler handler = new Handler(this);
	private HuodongHttpController controller;
	private Context context;

	public MultiChatHeaderView(Context context) {
		this(context, null, 0);
	}

	public MultiChatHeaderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MultiChatHeaderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		controller = new HuodongHttpController(handler);
		this.context = context;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.chat_activity_header, this, true);

		txtApplyTime = (TextView) findViewById(R.id.activity_time);
		headerView = (ActivityDetailHeaderView) findViewById(R.id.headview_msg);
		headerView.setVisibility(View.GONE);

		tvDescrible = (TextView) findViewById(R.id.header_describle);
		tvPhoneDescrible = (TextView) findViewById(R.id.header_phone_describle);

		tvPhoneDescrible.setOnClickListener(clickListener);
	}

	public void updateTime(String time) {
		txtApplyTime.setText(time);
	}
	
	public void setViewVisible(int visibility) {
		txtApplyTime.setVisibility(visibility);
		tvDescrible.setVisibility(visibility);
		tvPhoneDescrible.setVisibility(visibility);
	}

	public void update(HuodongDetailHeaderProvider provider) {
//		headerView.update(provider);

		//TODO 判断是否是加入群消息
		if (provider.getHuodongSendId() == App.USER.getUid()) {//TODO　如果是本人发起的活动，就不用填写电话号码了
			tvDescrible.setVisibility(View.VISIBLE);
			tvPhoneDescrible.setVisibility(View.GONE);
			tvDescrible.setText(MultiChatMessage.getCreateMultiChatInfo(context));
		}
		else {// TODO 加入群的消息
			tvDescrible.setVisibility(View.VISIBLE);
			tvPhoneDescrible.setVisibility(View.VISIBLE);
			tvDescrible.setText(MultiChatMessage.getJoinMultiChatInfo(context, provider.getHuodongTitle()));
		}

		if (App.USER.hasPhoneNum()) {
			tvPhoneDescrible.setText(R.string.multi_create_phone_success);
			tvPhoneDescrible.setClickable(false);
			return;
		}

		tvPhoneDescrible.setClickable(true);
		String str = MultiChatMessage.getSetPhoneChatInfo(getContext(), provider.getHuodongTitle());
		SpannableStringBuilder spans = new SpannableStringBuilder(str);
		String tag = getResources().getString(R.string.multi_create_phone_tag);

		final ClickableSpan span = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				//打开dialog
				ELog.i("");
				if (dialogInput == null) {
					dialogInput = new PhoneNumberDialog(getContext());
					dialogInput.setOnClickListener(clickListener);
					dialogInput.show();
				}
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				ds.setColor(getResources().getColor(R.color.set_phonenum_text_bg));
				ds.setUnderlineText(false);
			}

		};
		spans.setSpan(span, str.length() - tag.length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tvPhoneDescrible.setText(spans);
	}

	public ActivityDetailHeaderView getHuodongHeaderView() {
		return headerView;
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ELog.i("");
			switch (v.getId()) {
				case R.id.positive:
					if (dialogInput != null) {
						String input = dialogInput.getEditView().getText().toString();
						if (input == null || !input.matches(Constants.PATTERN_TEL) || input.length() != 11) {
							dialogInput.getTipView().setVisibility(View.VISIBLE);
							return;
						}
						else {
							dialogInput.getTipView().setVisibility(View.GONE);
						}
						//TODO 发送修改电话号码请求
						ELog.i("");
						if (dialogInput != null) {
							dialogInput.dismiss();
							dialogInput = null;
						}
						
						controller.setPhoneNumber(input);
					}
					break;

				case R.id.negative:
					if (dialogInput != null) {
						dialogInput.dismiss();
						dialogInput = null;
					}
					break;
				case R.id.header_phone_describle:
					//打开dialog
					if (App.USER.hasPhoneNum()) {
						return ;
					}
					ELog.i("");
					if (dialogInput == null) {
						dialogInput = new PhoneNumberDialog(getContext());
						dialogInput.setOnClickListener(clickListener);
						dialogInput.show();
					}
					break;
			}
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HuodongHttpController.HANDLER_SET_PHONENUM_SUCCESS:// 成功
				// TODO 更新用户信息
				App.USER.setPhoneNum(true);
				
				tvPhoneDescrible.setText(R.string.multi_create_phone_success);
				
				Toast.makeText(getContext(), R.string.input_phone_success, Toast.LENGTH_LONG).show();
				
				//发送广播更新用户信息
				Intent intent = MessageService.createRequestNotify(MessageService.TYPE_USERINFO);
				context.sendBroadcast(intent);
				break;

			case HuodongHttpController.HANDLER_SET_PHONENUM_FAIL:// 失败
				dialogInput.getTipView().setVisibility(View.VISIBLE);
				break;
		}
		return false;
	}

}

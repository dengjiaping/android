package com.oumen.home;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.biaoqing.BiaoQing;
import com.oumen.biaoqing.EmojiView;
import com.oumen.message.Type;

public class SendMessageView extends FrameLayout implements FloatViewController, SoftKeyboardController {
	protected boolean showing;
	protected boolean playing;
	
	protected FloatViewHostController host;
	
	protected ImageView btnOpen;
	protected EditText edtInput;
	protected Button btnSend;
	
	protected EmojiView biaoqingView;
	
	protected InputMethodManager inputManager;

	public SendMessageView(Context context) {
		this(context, null, 0);
	}

	public SendMessageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SendMessageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		if (context instanceof FloatViewHostController)
			host = (FloatViewHostController) context;
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.popup_send_comment, this, true);

		edtInput = (EditText) findViewById(R.id.content);

		btnSend = (Button) findViewById(R.id.send);

		inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		btnOpen = (ImageView) findViewById(R.id.biaoqing);
		btnOpen.setOnClickListener(clickListener);
		
		biaoqingView = (EmojiView) findViewById(R.id.biaoqingview);
		biaoqingView.setClickListener(clickListener);
		biaoqingView.addDefaultBiaoqing();
		biaoqingView.setVisibility(View.GONE);
	}
	
	public void setButtonClickListener(View.OnClickListener listener) {
		btnSend.setOnClickListener(listener);
	}
	
	@Override
	public void showSoftKeyboard() {
		inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.SHOW_FORCED);
		edtInput.requestFocus();
	}

	@Override
	public void hideSoftKeyboard() {
		if (inputManager.isActive()) {
			inputManager.hideSoftInputFromWindow(edtInput.getWindowToken(), 0);
		}
	}
	
	public void clear() {
		edtInput.setText("");
	}
	
	public void setText(CharSequence text) {
		edtInput.setText(text);
	}
	
	public void setInputHint(CharSequence hint) {
		edtInput.setHint(hint);
	}
	
	public String getText() {
		return edtInput.getText().toString();
	}
	
	public void setData(Object data) {
		btnSend.setTag(data);
	}
	
	public Object getData() {
		return btnSend.getTag();
	}
	
	private View.OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v == btnOpen) {
				if (biaoqingView.getVisibility() == View.VISIBLE) {
					biaoqingView.setVisibility(View.GONE);
					btnOpen.setImageResource(R.drawable.expression);
					host.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
					
					inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				}
				else {
					biaoqingView.setVisibility(View.VISIBLE);
					btnOpen.setImageResource(R.drawable.keyboard);
					host.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
					
					if (inputManager.isActive()) {
						inputManager.hideSoftInputFromWindow(edtInput.getWindowToken(), 0);
					}
				}
//				biaoqingView.setVisibility(biaoqingView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
//				if (biaoQingShow() && inputManager.isActive()) {
//					hideSoftKeyboard();
//				}
			}
			else {
				BiaoQing biaoqingMsg = (BiaoQing)v.getTag();
				if (biaoqingMsg == null) {
					return ;
				}
				if (Type.TEXT.equals(biaoqingMsg.getType())) {//输入文字加入小表情
					String content = edtInput.getText().toString().trim();
					SpannableStringBuilder builder = new SpannableStringBuilder(content);
					builder.append(biaoqingMsg.getSendMsg());
					builder = App.SMALLBIAOQING.convert(getContext(), builder, App.INT_UNSET);
					edtInput.setText(builder);
					//TODO 设置光标位置
					edtInput.setSelection(builder.length());
				}
				else if (Type.OTHER.equals(biaoqingMsg.getType())) {// 删除
					//动作按下
					int action = KeyEvent.ACTION_DOWN;
					//code:删除，其他code也可以，例如 code = 0
					int code = KeyEvent.KEYCODE_DEL;
					KeyEvent event = new KeyEvent(action, code);
					edtInput.onKeyDown(KeyEvent.KEYCODE_DEL, event);
				}
			}
			
		}
	}; 

	//---------------------- FloatViewController ----------------------//
	
	@Override
	public boolean isShowing() {
		return showing;
	}

	@Override
	public boolean isPlaying() {
		return playing;
	}

	@Override
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	@Override
	public View getRoot() {
		return this;
	}

	@Override
	public View show() {
		showing = true;
		playing = true;
		showSoftKeyboard();
		return this;
	}

	@Override
	public View hide() {
		hideSoftKeyboard();
		showing = false;
		playing = true;
		return this;
	}
	
	public boolean biaoQingShow() {
		if(biaoqingView.getVisibility() == View.VISIBLE) {
			return true;
		}
		return false;
	}
	
	public void hiddenBiaoqingView() {
		biaoqingView.setVisibility(View.GONE);
	}

	public FloatViewHostController getHost() {
		return host;
	}

	public void setHost(FloatViewHostController host) {
		this.host = host;
	}
}

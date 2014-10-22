package com.oumen.activity.detail.comment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.detail.HuodongHttpController;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.util.DialogFactory;
import com.oumen.widget.dialog.ProgressDialog;

public class PublishCommentFragment extends BaseFragment {
	private TitleBar titlebar;
	private Button btnLeft, btnRight;
	private Button btnHaoPing, btnChaPing;
	private TextView tip;
	private EditText input;
	
	private int PriseType = App.INT_UNSET;
	
	private int atId;
	
	private HuodongHttpController controller;
	
	protected ProgressDialog dialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		controller = new HuodongHttpController(handler);
		atId = getArguments().getInt(CommentActivity.INTENT_HUODONG_ID);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.huodong_publish_comment, container, false);
		
		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		titlebar.getTitle().setText("我的");
		btnRight = titlebar.getRightButton();
		btnRight.setText(getResources().getString(R.string.publish));
		btnLeft = titlebar.getLeftButton();
		
		btnHaoPing = (Button) view.findViewById(R.id.haoping);
		btnChaPing = (Button) view.findViewById(R.id.chaping);
		
		input = (EditText) view.findViewById(R.id.content);
		
		tip = (TextView) view.findViewById(R.id.tip);
		
		btnLeft.setOnClickListener(clickListener);
		btnRight.setOnClickListener(clickListener);
		btnHaoPing.setOnClickListener(clickListener);
		btnChaPing.setOnClickListener(clickListener);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		PriseType = App.INT_UNSET;
		input.setText("");
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				if (getFragmentManager().getBackStackEntryCount() > 0) {
					getFragmentManager().popBackStack();
				}
				else {
					getActivity().finish();
				}
			}
			else if (v == btnRight) {
				if (PriseType == App.INT_UNSET) {
					Toast.makeText(getActivity(), "请选择好评或者差评", Toast.LENGTH_SHORT).show();
					return ;
				}
				String str = input.getText().toString().trim();
				if (TextUtils.isEmpty(str)) {
					Toast.makeText(getActivity(), "请输入评论，并且评论不少于10个字", Toast.LENGTH_SHORT).show();
					return ;
				}
				else if (str.length() < 10) {
					Toast.makeText(getActivity(), "评论不少于10个字", Toast.LENGTH_SHORT).show();
					return ;
				}
				showProgressDialog("正在发布，请稍后...");
				controller.publishComment(atId, PriseType, str);
			}
			else if (v == btnHaoPing) {
				PriseType = 1;
				btnHaoPing.setBackgroundResource(R.drawable.huodong_comment_publish_prise_choose);
				btnHaoPing.setTextColor(getResources().getColor(R.color.white));
				btnChaPing.setBackgroundResource(R.drawable.huodong_comment_publish_prise_default);
				btnChaPing.setTextColor(getResources().getColor(R.color.huodong_comment_text_unchoose));
			}
			else if (v == btnChaPing) {
				PriseType = 0;
				btnChaPing.setBackgroundResource(R.drawable.huodong_comment_publish_prise_choose);
				btnChaPing.setTextColor(getResources().getColor(R.color.white));
				btnHaoPing.setBackgroundResource(R.drawable.huodong_comment_publish_prise_default);
				btnHaoPing.setTextColor(getResources().getColor(R.color.huodong_comment_text_unchoose));
			}
		}
	};
	public boolean handleMessage(android.os.Message msg) {
		switch (msg.what) {
			case HuodongHttpController.HANDLER_PUBLISH_COMMENT:
				dismissProgressDialog();
				if (getFragmentManager().getBackStackEntryCount() > 0) {
					getFragmentManager().popBackStack();
				}
				else {
					getActivity().finish();
				}
				break;

			case HuodongHttpController.HANDLER_PUBLISH_COMMENT_FAIL:
				dismissProgressDialog();
				Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
		}
		return false;
		
	};
	
	public boolean isShowingProgressDialog() {
		return dialog != null && dialog.isShowing();
	}

	public void showProgressDialog(String message) {
		if (dialog == null) {
			dialog = DialogFactory.createProgressDialog(getActivity());
		}
		dialog.getMessageView().setText(message);

		if (!dialog.isShowing())
			dialog.show();
	}

	public void dismissProgressDialog() {
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
	}
}

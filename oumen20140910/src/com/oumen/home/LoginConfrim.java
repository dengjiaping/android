package com.oumen.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.oumen.MainActivity;
import com.oumen.MainActivity.Frag;
import com.oumen.R;
import com.oumen.message.MessageService;
import com.oumen.widget.dialog.TwoButtonDialog;
/**
 * 跳转到登录界面之前的确认
 *
 */
public class LoginConfrim implements View.OnClickListener {

	private TwoButtonDialog confrimLoginDialog;

	private Context context;

	public LoginConfrim(Context context) {
		this.context = context;
	}

	public void openDialog() {
		if (!isDialogShow()) {
			confrimLoginDialog = new TwoButtonDialog(context);
			confrimLoginDialog.getMessageView().setText(R.string.login_tip_default);
			confrimLoginDialog.getRightButton().setOnClickListener(this);
			confrimLoginDialog.getLeftButton().setOnClickListener(this);
			confrimLoginDialog.show();
		}
	}

	public void closeDialog() {
		if (confrimLoginDialog != null) {
			confrimLoginDialog.dismiss();
			confrimLoginDialog = null;
		}
	}
	
	public boolean isDialogShow() {
		if (confrimLoginDialog != null) {
			return confrimLoginDialog.isShowing();
		}
		else {
			return false;
		}
	}

	@Override
	public void onClick(View v) {
		if (v == confrimLoginDialog.getRightButton()) {
			if (confrimLoginDialog != null) {
				confrimLoginDialog.dismiss();
			}
		}
		else if (v == confrimLoginDialog.getLeftButton()) {
			if (confrimLoginDialog != null) {
				confrimLoginDialog.dismiss();
			}
			
			if (context instanceof MainActivity) {
				MainActivity host = (MainActivity) context;
				host.switchFragment(Frag.ACCOUNT);
			}
			else if (context instanceof Activity) {
//				Activity current = (Activity) context;
//				current.finish();
				Intent open = new Intent(context, MainActivity.class);
				open.putExtra(MainActivity.INTENT_KEY_CURRENT_FRAGMENT, MainActivity.Frag.ACCOUNT);
				open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(open);
				
				Intent notify = MessageService.createResponseNotify(MessageService.TYPE_SWITCH_FRAGMENT);
				notify.putExtra(MessageService.INTENT_KEY_PARAM, Frag.ACCOUNT);
				context.sendBroadcast(notify);
			}
		}
	}
}

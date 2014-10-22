package com.oumen.home;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.oumen.R;
import com.oumen.Version;
import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.app.ActivityStack;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.message.MessageService;
import com.oumen.tools.ELog;
import com.oumen.tools.FileTools;
import com.oumen.widget.dialog.TwoButtonDialog;

/**
 * 版本检测
 * @author oumen-o
 *
 */
public class CheckVersion implements Handler.Callback{
	public static final String INTENT_KEY_FLAG = "version_flag";
	public static final int VERSION_FIRST_CHANGE = 1;
	public static final int VERSION_OTHER_CHANGE = 2;
	
	private final int HANDLER_CHECK_VERSION_SUCCESS = 1;
	private final int HANDLER_CHECK_VERSION_FAIL = 2;
	private final int DOWNLOA_NEWVESION = 3;
	private final int DOWN_UPDATE = 4;
	private final int DOWM_UPDATE_FAIL = 5;

	private Context context;
	
	private Version serverVersion;
	private Version currentVersion;

	private Dialog downloadDialog;
	private ProgressBar mProgress;
	private boolean interceptFlag = false;

	private int progress;

	private Timer timer;

	private HttpRequest req;
	
	private final Handler handler = new Handler(this);
	
	public CheckVersion(Context context){
		this.context = context;
	}
	
	public void checkCurrentVersion() {
		try {
			String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			ELog.i(version);
			currentVersion = new Version(version);
		}
		catch (Exception e) {
			ELog.i("Exception:" + e.getMessage());
			e.printStackTrace();
			
			//发送广播
			Intent notify = MessageService.createResponseNotify(MessageService.TYPE_CHECK_VERSION);
			notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_FAILED);
			notify.putExtra(INTENT_KEY_FLAG, VERSION_OTHER_CHANGE);
			context.sendBroadcast(notify);
		}
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				ELog.i("版本检测超时");
				req.close();
				handler.sendEmptyMessage(HANDLER_CHECK_VERSION_FAIL);
			}
		}, 15 * 1000);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("version", currentVersion.getVersion()));
		req = new HttpRequest(Constants.CHECK_VERSION, params, HttpRequest.Method.GET, checkVersionCallback);
		App.THREAD.execute(req);
	}
	
	final DefaultHttpCallback checkVersionCallback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);

				if (TextUtils.isEmpty(str) && str.equals("[]")) {
					serverVersion = currentVersion.clone();
				}
				else {
					serverVersion = new Version(new JSONObject(str));
				}
				handler.sendEmptyMessage(HANDLER_CHECK_VERSION_SUCCESS);
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				handler.sendEmptyMessage(HANDLER_CHECK_VERSION_FAIL);
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendEmptyMessage(HANDLER_CHECK_VERSION_FAIL);
		}
	});
	
	private void timerCancel() {
		if (timer != null) {
			try {
				timer.cancel();
			}
			catch (Exception e) {
			}
		}
	}

	
	private void showTipDialog(String tip) {
		final TwoButtonDialog dialogTip = new TwoButtonDialog(context);
		dialogTip.getTitleView().setText("偶们提示");
		dialogTip.getMessageView().setText(tip);

		dialogTip.getRightButton().setText("取消");
		dialogTip.getRightButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogTip.dismiss();
				if (serverVersion.getVersion1() > currentVersion.getVersion1()) {
					// 大版本更新，必须升级，如果取消就退出应用
					Intent notify = MessageService.createResponseNotify(MessageService.TYPE_CHECK_VERSION);
					notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_FAILED);
					notify.putExtra(INTENT_KEY_FLAG, VERSION_FIRST_CHANGE);
					context.sendBroadcast(notify);
				}
				else if (serverVersion.getVersion2() > currentVersion.getVersion2()) {
					// 中版本更新，可以不升级，如果取消就执行自动登录
					//TODO
					Intent notify = MessageService.createResponseNotify(MessageService.TYPE_CHECK_VERSION);
					notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
					notify.putExtra(INTENT_KEY_FLAG, VERSION_OTHER_CHANGE);
					context.sendBroadcast(notify);
				}
			}
		});

		dialogTip.getLeftButton().setText("确定");
		dialogTip.getLeftButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 下载APK
				dialogTip.dismiss();
				showDownloadDialog();
				App.THREAD.execute(mdownApkRunnable);
			}
		});
		dialogTip.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialogTip.show();
	}

	private void showDownloadDialog() {
		AlertDialog.Builder builder = new Builder(ActivityStack.getCurrent());
		builder.setTitle("正在下载");

		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.progress);
		builder.setView(v);
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				interceptFlag = true;
			}
		});
		downloadDialog = builder.create();
		downloadDialog.show();
	}

	private Runnable mdownApkRunnable = new Runnable() {
		@Override
		public void run() {
			InputStream is = null;
			FileOutputStream fos = null;
			try {
				URL url = new URL(serverVersion.getApkPath());
				// URL url = new URL("http://www.oumen.com/oumen.apk");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(30 * 1000);
				conn.setReadTimeout(30 * 1000);
				conn.setDoInput(true);
				conn.connect();
				int length = conn.getContentLength();
				is = conn.getInputStream();

				String remotePath = serverVersion.getApkPath();
				File file = new File(Constants.APK_PATH + remotePath.substring(remotePath.lastIndexOf('/')));
				FileTools.createFile(file, true);

				fos = new FileOutputStream(file);

				int count = 0;
				byte buf[] = new byte[1024];

				do {
					int numread = is.read(buf);
					count += numread;
					progress = (int) (((float) count / length) * 100);
					handler.sendEmptyMessage(DOWN_UPDATE);
					if (numread <= 0) {
						break;
					}
					fos.write(buf, 0, numread);
				}
				while (!interceptFlag);

				if (interceptFlag) {
					handler.sendEmptyMessage(DOWM_UPDATE_FAIL);
				}
				handler.sendEmptyMessage(DOWNLOA_NEWVESION);
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (is != null)
						is.close();
					if (fos != null)
						fos.close();
				}
				catch (Exception e) {
				}

				if (downloadDialog != null) {
					downloadDialog.dismiss();
				}
			}
		}
	};

	private AlertDialog alertDialog;

	public void showInstallConfirmDialog() {
		AlertDialog.Builder tDialog = new AlertDialog.Builder(ActivityStack.getCurrent());
		tDialog.setTitle("提示");
		tDialog.setMessage("受否安装最新版本");
		tDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				installApk();
			}
		});

		tDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
				Intent notify = MessageService.createResponseNotify(MessageService.TYPE_CHECK_VERSION);
				notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_FAILED);
				notify.putExtra(INTENT_KEY_FLAG, VERSION_FIRST_CHANGE);
				context.sendBroadcast(notify);
			}
		});
		alertDialog = tDialog.create();
		alertDialog.show();
	}

	private void installApk() {
		if (alertDialog != null) {
			alertDialog.dismiss();
		}
		String remotePath = serverVersion.getApkPath();
		File file = new File(Constants.APK_PATH + remotePath.substring(remotePath.lastIndexOf('/')));
		if (!file.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + file.toString()), "application/vnd.android.package-archive");
		context.startActivity(i);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_CHECK_VERSION_SUCCESS:// 版本检测成功
				timerCancel();
				if (serverVersion.getVersion1() > currentVersion.getVersion1()) {
					// 大版本更新，必须升级
					showTipDialog("发现新版本，您必须更新才能正常使用");
				}
				else if (serverVersion.getVersion2() > currentVersion.getVersion2()) {
					// 中版本更新，可以不升级，但需要提示用户
					showTipDialog("发现新版本，是否更新？");
				}
				else {
					Intent notify = MessageService.createResponseNotify(MessageService.TYPE_CHECK_VERSION);
					notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
					notify.putExtra(INTENT_KEY_FLAG, VERSION_OTHER_CHANGE);
					context.sendBroadcast(notify);
				}
				break;
				
			case HANDLER_CHECK_VERSION_FAIL:// 版本检测失败
				timerCancel();
				//发送广播，告诉home界面，检测完成了
				Intent notify = MessageService.createResponseNotify(MessageService.TYPE_CHECK_VERSION);
				notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_FAILED);
				notify.putExtra(INTENT_KEY_FLAG, VERSION_OTHER_CHANGE);
				context.sendBroadcast(notify);
				break;
				
			case DOWM_UPDATE_FAIL://下载最新apk失败
				notify = MessageService.createResponseNotify(MessageService.TYPE_CHECK_VERSION);
				notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_FAILED);
				if (serverVersion.getVersion1() > currentVersion.getVersion1()) {
					notify.putExtra(INTENT_KEY_FLAG, VERSION_FIRST_CHANGE);
				}
				else if (serverVersion.getVersion2() > currentVersion.getVersion2()) {
					notify.putExtra(INTENT_KEY_FLAG, VERSION_OTHER_CHANGE);
				}
				context.sendBroadcast(notify);
				break;
				
			case DOWNLOA_NEWVESION:
				showInstallConfirmDialog();
				break;

			case DOWN_UPDATE:
				mProgress.setProgress(progress);
				break;
		}
		return false;
	}

}

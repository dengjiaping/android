package com.oumen.android.peers;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.peers.entity.CircleUserBasicMsg;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ApacheZip;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.oumen.widget.dialog.PickImageDialog;
import com.oumen.widget.dialog.ProgressDialog;

public class ShareFragment extends BaseFragment {
	private final int HANDLER_CREATE_CONTENT = 1;
	private final int IMAGE_MAX = 6;
	
	private final String STARE_MODES = "modes";
	private final String STATE_CONTENT = "content";
	private final String STATE_ISOPEN_FLAG = "isopen_flag";
	private final String STATE_PHOTO_PATH = "choose_photo_path";
	private final String STATE_PHOTO_LIST = "photo_list";
	private final String STATE_PHOTO_SRC_FILE = "photo_srcs";
	private final String STATE_PHOTO_TEMP_FILE= "photo_temps";
	private final String STATE_FIRST_FLAG = "isfirst_flag";
	private final String STATE_DIALOG = "dialog";

	// 标题行的三个控件
	private TitleBar titlbar;
	private Button btnLeft;
	private TextView tvTitle;
	private Button btnRight;

	private View view;
	private TextView tvContent;// 内容
	private TextView tvShare;
	private RelativeLayout rlControl;

	private RadioButton btnShare, btnExchange, btnHelp;
	private TextView tvOPen;// 公开
	private TextView tvClose;// 仅好友可见

	private ScrollView svFrist;
	private LinearLayout llSecond;

	private GridView gvPhotos;
	private final ShareGvAdapter adapter = new ShareGvAdapter();
	
	private ProgressDialog dialogProgress;

	private boolean isOpen = true;

	private int modes = 3;

	private String content = "";

	private String path;
	private File zipFile = null;
	private final ArrayList<File> srcFiles = new ArrayList<File>();
	private final ArrayList<File> tmpFiles = new ArrayList<File>();
	private boolean isFirstInto = true;
	
	private HttpRequest req;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirstInto) {
			adapter.data.add(BitmapFactory.decodeResource(getResources(), R.drawable.icon_add_image));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.oumencircle_share, container, false);

		titlbar = (TitleBar) view.findViewById(R.id.titlebar);
		
		btnLeft = titlbar.getLeftButton();
		tvTitle = titlbar.getTitle();
		btnRight = titlbar.getRightButton();

		tvContent = (TextView) view.findViewById(R.id.tv_share_content);

		gvPhotos = (GridView) view.findViewById(R.id.gv_share_picture);
		gvPhotos.setAdapter(adapter);

		btnShare = (RadioButton) view.findViewById(R.id.btn_peershare_share);
		btnExchange = (RadioButton) view.findViewById(R.id.btn_peershare_exchange);
		btnHelp = (RadioButton) view.findViewById(R.id.btn_peershare_help);
		btnShare.setSelected(true);

		rlControl = (RelativeLayout) view.findViewById(R.id.rl_share_control);
		tvShare = (TextView) view.findViewById(R.id.tv_share_show);

		svFrist = (ScrollView) view.findViewById(R.id.sv_first);

		llSecond = (LinearLayout) view.findViewById(R.id.ll_second);
		tvOPen = (TextView) view.findViewById(R.id.tv_share_open);
		tvClose = (TextView) view.findViewById(R.id.tv_share_friend);

		btnLeft.setOnClickListener(clickListener);
		btnRight.setOnClickListener(clickListener);
		btnShare.setOnClickListener(clickListener);
		btnExchange.setOnClickListener(clickListener);
		btnHelp.setOnClickListener(clickListener);
		tvOPen.setOnClickListener(clickListener);
		tvClose.setOnClickListener(clickListener);
		rlControl.setOnClickListener(clickListener);

		gvPhotos.setOnItemClickListener(itemClickListener);
		
		dialogProgress = new ProgressDialog(getActivity());
		dialogProgress.getMessageView().setText("正在发布，请稍后...");
		dialogProgress.setCancelable(true);
		dialogProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (req != null) {
					req.close();
				}
			}
		});
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STARE_MODES, modes);
		if (content != null && content.length() > 0) {
			outState.putString(STATE_CONTENT, content);
		}
		outState.putBoolean(STATE_ISOPEN_FLAG, isOpen);
		outState.putString(STATE_PHOTO_PATH, path);
		outState.putParcelableArrayList(STATE_PHOTO_LIST, adapter.data);
		outState.putSerializable(STATE_PHOTO_SRC_FILE, srcFiles);
		outState.putSerializable(STATE_PHOTO_TEMP_FILE, tmpFiles);
		outState.putBoolean(STATE_FIRST_FLAG, isFirstInto);

		outState.putString("path", path);
		outState.putBoolean(STATE_DIALOG, dialogPickImages != null);
		super.onSaveInstanceState(outState);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState != null) {
			modes = savedInstanceState.getInt(STARE_MODES);
			content = savedInstanceState.getString(STATE_CONTENT);
			isOpen = savedInstanceState.getBoolean(STATE_ISOPEN_FLAG);
			path = savedInstanceState.getString(STATE_PHOTO_PATH);
			ArrayList<Bitmap> b = savedInstanceState.getParcelableArrayList(STATE_PHOTO_LIST);
			for (Bitmap pic : b) {
				adapter.data.add(pic);
			}
			adapter.notifyDataSetChanged();
			isFirstInto = savedInstanceState.getBoolean(STATE_FIRST_FLAG);
			ArrayList<File> files = (ArrayList<File>) savedInstanceState.getSerializable(STATE_PHOTO_SRC_FILE);
			for (File file : files) {
				srcFiles.add(file);
			}
			files.clear();
			files = (ArrayList<File>) savedInstanceState.getSerializable(STATE_PHOTO_TEMP_FILE);
			for (File file : files) {
				tmpFiles.add(file);
			}
			path = savedInstanceState.getString("path");
			if (savedInstanceState.getBoolean(STATE_DIALOG)) {
				retainPickImageDialog();
				ELog.i("Retain path:" + path);
			}
		}
		initData();
	}

	private void share() {
		btnShare.setBackgroundResource(R.drawable.oumen_share_share_click);
		btnExchange.setBackgroundResource(R.drawable.oumen_share_exchange);
		btnHelp.setBackgroundResource(R.drawable.oumen_share_help);
		btnShare.setTextColor(getResources().getColor(R.color.white));
		btnExchange.setTextColor(getResources().getColor(R.color.black));
		btnHelp.setTextColor(getResources().getColor(R.color.black));
	}

	private void exchange() {
		btnShare.setBackgroundResource(R.drawable.oumen_share_share);
		btnExchange.setBackgroundResource(R.drawable.oumen_share_exchange_click);
		btnHelp.setBackgroundResource(R.drawable.oumen_share_help);
		btnShare.setTextColor(getResources().getColor(R.color.black));
		btnExchange.setTextColor(getResources().getColor(R.color.white));
		btnHelp.setTextColor(getResources().getColor(R.color.black));
	}

	private void help() {
		btnShare.setBackgroundResource(R.drawable.oumen_share_share);
		btnExchange.setBackgroundResource(R.drawable.oumen_share_exchange);
		btnHelp.setBackgroundResource(R.drawable.oumen_share_help_click);
		btnShare.setTextColor(getResources().getColor(R.color.black));
		btnExchange.setTextColor(getResources().getColor(R.color.black));
		btnHelp.setTextColor(getResources().getColor(R.color.white));
	}

	private void initData() {
		svFrist.setVisibility(View.VISIBLE);
		llSecond.setVisibility(View.GONE);
		tvTitle.setText("分享内容");
		btnRight.setText("发布");
		if (content.length() > 0) {
			tvContent.setText(content);
		}
		if (modes == CircleUserBasicMsg.MODE_SHARE) {
			share();
		}
		else if (modes == CircleUserBasicMsg.MODE_EXCHANGE) {
			exchange();
		}
		else if (modes == CircleUserBasicMsg.MODE_HELP) {
			help();
		}
		if (isOpen) {
			open();
		}
		else {
			close();
		}
	}

	@Override
	public void onDestroyView() {
		if (dialogProgress.isShowing()) {
			dialogProgress.cancel();
		}
		super.onDestroyView();
	}

	private void clearTempFiles() {
		// 清除文件
		for (File f : tmpFiles) {
			if (f.exists()) {
				f.delete();
			}
		}
		
		if (zipFile != null && zipFile.exists()) {
			zipFile.delete();
		}
		tmpFiles.clear();
	}

	private final OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			content = tvContent.getText().toString();
			int size = adapter.data.size();
			if ((size - 1 == position) && size < 7) {
				showPickImageDialog();
			}
		}
	};

	private void open() {
		Drawable drawable = getResources().getDrawable(R.drawable.oumen_share_choose);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tvOPen.setCompoundDrawables(drawable, null, null, null);
		tvOPen.setTextColor(getResources().getColor(R.color.text_highlight));

		Drawable drawable1 = getResources().getDrawable(R.drawable.oumen_share_unchoose);
		drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());
		tvClose.setCompoundDrawables(drawable1, null, null, null);
		tvClose.setTextColor(getResources().getColor(R.color.content));
		tvShare.setText("公开");
	}

	public void onKeyDown() {
		if (isFirstInto) {
			clearTempFiles();
			
			getActivity().setResult(Activity.RESULT_OK);
			getActivity().finish();
		}
		else {
			svFrist.setVisibility(View.VISIBLE);
			llSecond.setVisibility(View.GONE);
			isFirstInto = true;
			tvTitle.setText("分享内容");
			btnRight.setVisibility(View.VISIBLE);
			btnRight.setText("发布");
		}
	}

	private void close() {
		Drawable drawable1 = getResources().getDrawable(R.drawable.oumen_share_unchoose);
		drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());
		tvOPen.setCompoundDrawables(drawable1, null, null, null);
		tvOPen.setTextColor(getResources().getColor(R.color.content));

		Drawable drawable = getResources().getDrawable(R.drawable.oumen_share_choose);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tvClose.setCompoundDrawables(drawable, null, null, null);
		tvClose.setTextColor(getResources().getColor(R.color.text_highlight));
		tvShare.setText("仅对好友可见");
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				if (isFirstInto) {
					clearTempFiles();
					
					getActivity().setResult(Activity.RESULT_OK);
					getActivity().finish();
				}
				else {
					svFrist.setVisibility(View.VISIBLE);
					llSecond.setVisibility(View.GONE);
					isFirstInto = true;
					tvTitle.setText("分享内容");
					btnRight.setVisibility(View.VISIBLE);
					btnRight.setText("发布");
				}
			}
			else if (v == btnRight) {
				if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
					isOpen = false;
					createContent();
					content = "";
				}
				else {
					Toast.makeText(getActivity(), R.string.err_network_invalid, Toast.LENGTH_SHORT).show();
				}
			}
			else if (v == btnExchange) {
				exchange();
				modes = 2;
			}
			else if (v == btnHelp) {
				help();
				modes = 3;
			}
			else if (v == btnShare) {
				share();
				modes = 1;
			}
			else if (v == rlControl) {// 选择权限
				svFrist.setVisibility(View.GONE);
				llSecond.setVisibility(View.VISIBLE);
				isFirstInto = false;
				tvTitle.setText("选择可见范围");
				btnRight.setVisibility(View.GONE);
			}
			else if (v == tvOPen) {
				open();
				isOpen = true;
			}
			else if (v == tvClose) {
				close();
				isOpen = false;
			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		ELog.i("");
		if (resultCode == Activity.RESULT_OK) {
			if (dialogPickImages != null) {
				String tempPath = dialogPickImages.onActivityResult(requestCode, resultCode, data);
				ELog.i(tempPath);
				
				if (tempPath == null && requestCode == Constants.REQUEST_CODE_OPEN_CAMERA) {
					addImage(path);
				}
				else if (tempPath != null && requestCode == Constants.REQUEST_CODE_PICK_IMAGE) {
					try {
						path = tempPath;
						addImage(path);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 选取照片后的处理
	 * 
	 * @param path
	 */
	private void addImage(String path) {
		Bitmap tmp = ImageTools.rotate(path, App.IMAGE_SIZE_MAX);
		Bitmap selectedImg = ImageTools.clip2square(tmp);
		adapter.data.add(adapter.data.size() - 1, selectedImg);
		adapter.notifyDataSetChanged();
		
		srcFiles.add(new File(path));
		
		if (adapter.data.size() > IMAGE_MAX) {
			adapter.data.remove(adapter.data.size() - 1);
		}

		int colum = adapter.data.size() - 3 > 0 ? (adapter.data.size() - 6 > 0 ? 3 : 2) : 1;
		ViewGroup.LayoutParams params = gvPhotos.getLayoutParams();
		params.width = App.DEFAULT_CELL_SIZE * 3 + 10 * 4;
		params.height = App.DEFAULT_CELL_SIZE * colum + 10 * (colum + 1);
		gvPhotos.setLayoutParams(params);
		gvPhotos.requestLayout();
	}

	private void createContent() {
		// 发表评论
		// 获取输入的内容
		final String content = tvContent.getText().toString().trim();
		// 对内容和位置进行非空验证
		if (TextUtils.isEmpty(content) && srcFiles.isEmpty()) {
			Toast.makeText(getActivity(), "请输入文字或者选择至少一张图片", Toast.LENGTH_SHORT).show();
			return;
		}

		if (!dialogProgress.isShowing()) {
			dialogProgress.show();
		}
		App.THREAD.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					String zippath = null;
					if (!srcFiles.isEmpty()) {
						compressImage();
						
						// 将图片文件夹进行压缩
						zippath = Constants.IMAGE_PATH + UUID.randomUUID().toString() + ".zip";
						ELog.i(zippath);
						
						zipFile = new File(zippath);
						if (zipFile.exists()) {
							zipFile.delete();
						}
						ApacheZip.writeByApacheZipOutputStream(tmpFiles, zippath, "", "", false);
					}
					
					DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

						@Override
						public void onSuccess(HttpResult result) {
							try {
								HttpRequest.timeout = HttpRequest.TIME_SHORT;
								String str = result.getResult();
								ELog.i(str);
								
								JSONObject obj = new JSONObject(str);
								int res = obj.getInt("result");
								if (res == 0) {
									handler.sendMessage(handler.obtainMessage(HANDLER_CREATE_CONTENT, 0, 0));
								}
								else if (res > 0) {
									modes = 1;
									PutActivityUtil.closeActivity();

									handler.sendMessage(handler.obtainMessage(HANDLER_CREATE_CONTENT, res, 0));
								}

							}
							catch (Exception e) {
								ELog.e("Exception:" + e.getMessage());
								handler.sendMessage(handler.obtainMessage(HANDLER_CREATE_CONTENT, 0, 0));
							}
							finally {
								req = null;
							}
						}

						@Override
						public void onForceClose(ExceptionHttpResult result) {
							req = null;
						}

						@Override
						public void onException(ExceptionHttpResult result) {
							req = null;
							handler.sendMessage(handler.obtainMessage(HANDLER_CREATE_CONTENT, 0, 0));
						}
					});
					
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
					params.add(new BasicNameValuePair("content", TextUtils.isEmpty(content) ? "" : content));
					params.add(new BasicNameValuePair("modes", String.valueOf(modes)));
					params.add(new BasicNameValuePair("open", isOpen ? "1" : "0"));
					params.add(new BasicNameValuePair("lat", String.valueOf(App.latitude)));
					params.add(new BasicNameValuePair("lng", String.valueOf(App.longitude)));
					if (!srcFiles.isEmpty()) {
						HttpRequest.timeout = HttpRequest.TIME_LONG;
						req = new HttpRequest(Constants.OUMENCIRCLE_GREATECONTENT, params, new BasicNameValuePair("img", zippath), null, HttpRequest.Method.POST, callback);
						App.THREAD.execute(req);
					}
					else {
						req = new HttpRequest(Constants.OUMENCIRCLE_GREATECONTENT, params, HttpRequest.Method.POST, callback);
						App.THREAD.execute(req);
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_CREATE_CONTENT, 0, 0));
					e.printStackTrace();
				}
			}
		});

	}
	
	private void compressImage() {
		ELog.i("");
		LinkedList<File> tmp = new LinkedList<File>();
		for (File srcFile : srcFiles) {
			Bitmap srcImg;
			Options opt;
			long length = srcFile.length();
			if (length > App.IMAGE_LENGTH_MAX) {
				opt = new Options();
				opt.inSampleSize = (int)(length / App.IMAGE_LENGTH_MAX);
				opt.inPreferredConfig = Bitmap.Config.RGB_565;
				opt.inInputShareable = true;
				opt.inPurgeable = true;
				opt.inMutable = true;
				srcImg = ImageTools.rotate(srcFile.getAbsolutePath(), App.IMAGE_SIZE_MAX);
			}
			else {
				srcImg = ImageTools.rotate(srcFile.getAbsolutePath(), App.IMAGE_SIZE_MAX);
			}

			File tmpFile = new File(App.getUploadCachePath(), System.currentTimeMillis() + ".jpg");
			try {
				ImageTools.save(srcImg, tmpFile, 85);
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
			}
			tmp.add(tmpFile);
		}

		synchronized (tmpFiles) {
			tmpFiles.clear();
			tmpFiles.addAll(tmp);
		}
	}

	public boolean handleMessage(android.os.Message msg) {
		if (msg.what == HANDLER_CREATE_CONTENT) {
			if (dialogProgress.isShowing()) {
				dialogProgress.cancel();
			}
			
			clearTempFiles();

			if (msg.arg1 > 0) {
				getActivity().setResult(Activity.RESULT_OK);
				getActivity().finish();
			}
			else {
				Toast.makeText(getActivity(), "发表失败", Toast.LENGTH_SHORT).show();
			}
		}
		return false;
	};

	// ---------- Pick Images ----------//
	private PickImageDialog dialogPickImages;

	private void showPickImageDialog() {
		if (dialogPickImages == null) {
			dialogPickImages = new PickImageDialog(getActivity());
		}
		path = Constants.UPLOAD_PATH + System.currentTimeMillis();
		dialogPickImages.setPath(path);
		dialogPickImages.setClipFlag(false);
		dialogPickImages.show();
	}

	private void retainPickImageDialog() {
		if (dialogPickImages != null)
			return;

		dialogPickImages = new PickImageDialog(getActivity());
		dialogPickImages.setPath(path);
	}
}

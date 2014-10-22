package com.oumen.widget.dialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.android.peers.ClipImageActivity;
import com.oumen.android.peers.ClipImageFragment;
import com.oumen.android.util.Constants;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;

public class PickImageDialog extends Dialog {
	public static final String INTENT_KEY_PATH = "path";
	public static final int REQUEST_CRPO_IMAGE = 980;

	// private TextView txtTitle;
	private Button txtCamear;
	private Button txtAlbum;

	private Activity activity;

	private String path;

	public void setClipFlag(boolean isClip) {
		this.isClip = isClip;
	}

	private boolean isClip = true;

	public PickImageDialog(Context context) {
		super(context);
		preInitialize(context, true);
	}

	public PickImageDialog(Context context, int theme) {
		super(context, theme);
		preInitialize(context, true);
	}

	public PickImageDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		preInitialize(context, cancelable);
	}

	private void preInitialize(Context context, boolean cancelable) {
		setCancelable(cancelable);

		if (context instanceof Activity)
			activity = (Activity) context;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layContainer = inflater.inflate(R.layout.dialog_pick_images, null);

		// txtTitle = (TextView) layContainer.findViewById(R.id.title);

		txtCamear = (Button) layContainer.findViewById(R.id.btn_camera);
		txtCamear.setOnClickListener(clickListener);

		txtAlbum = (Button) layContainer.findViewById(R.id.btn_album);
		txtAlbum.setOnClickListener(clickListener);

		DisplayMetrics display = context.getResources().getDisplayMetrics();

		int padding = (int) (30 * display.density);
		setContentView(layContainer, new ViewGroup.LayoutParams(display.widthPixels - padding * 2, LayoutParams.WRAP_CONTENT));
		layContainer.getRootView().setBackgroundColor(Color.TRANSPARENT);
	}

	// public TextView getTitleView() {
	// return txtTitle;
	// }

	public TextView getCameraView() {
		return txtCamear;
	}

	public TextView getAlbumView() {
		return txtAlbum;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Activity getActivity() {
		return activity;
	}

	private File getFile(String uri) {
		File file = new File(uri);
		if (!file.exists()) {
			File parent = file.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}

			try {
				parent.createNewFile();
			}
			catch (IOException e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
			}
		}
		return file;
	}

	public String onActivityResult(int requestCode, int resultCode, Intent data) {
		String picturePath = null;
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == Constants.REQUEST_CODE_PICK_IMAGE) {// 调取图库返回
				String temppath = null;
				try {
					Uri uri = data.getData();
					ELog.i(uri.toString());

					Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
					cursor.moveToFirst();
					temppath = cursor.getString(1);
					
					cursor.close();
					if (temppath != null && isClip) {
						//1.先对选取的图片进行判断，是否旋转，是否图片太大了
						File file = new File(getActivity().getCacheDir().getAbsoluteFile() + "/clip", System.currentTimeMillis() + "temp.jpg");
						ImageTools.handlePhotoFromLocation(file, temppath);//将图片写到指定路径下
						//2.调取裁剪界面
						Intent intent = new Intent(activity, ClipImageActivity.class);
						intent.putExtra(ClipImageFragment.INTENT_CROP_IMAGE, file.getAbsolutePath());
						activity.startActivityForResult(intent, REQUEST_CRPO_IMAGE);
						temppath = null;
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
				ELog.i(temppath);
				return temppath;
			}
			else if (requestCode == REQUEST_CRPO_IMAGE) {
				try {
					picturePath = data.getStringExtra(ClipImageFragment.INTENT_CROP_IMAGE_BACK);
					ELog.i(picturePath);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return picturePath;
			}
			else if (requestCode == Constants.REQUEST_CODE_OPEN_CAMERA) {// 调取照相机返回
				ELog.i("Data:" + data);
			}
		}
		return null;
	}

	public static byte[] readStream(InputStream inStream) throws Exception {
		byte[] buffer = new byte[1024];
		int len = -1;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inStream.close();
		return data;

	}

	public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
		if (bytes != null)
			if (opts != null)
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
			else
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return null;
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			dismiss();
			if (v == txtCamear) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra("output", Uri.fromFile(getFile(path)));
				intent.putExtra("outputFormat", "JPEG");
				activity.startActivityForResult(intent, Constants.REQUEST_CODE_OPEN_CAMERA);
			}
			else if (v == txtAlbum) {
				// intent.putExtra("crop", "true");// 才能出剪辑的小方框，不然没有剪辑功能，只能选取图片
				// intent.putExtra("aspectX", 1); // 出现放大和缩小
				// intent.putExtra("aspectY", 1);
				// intent.setType("image/*"); // 查看类型 详细的类型在
				// // innerIntent.putExtra("return-data", true);
				// intent.putExtra("output", Uri.fromFile(getFile(path)));
				// 专入目标文件
				// intent.putExtra("outputFormat", "JPEG"); // 输入文件格式
				// Intent wrapperIntent = Intent.createChooser(intent, "先择图片");
				// // 开始并设置标题
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				activity.startActivityForResult(intent, Constants.REQUEST_CODE_PICK_IMAGE);
			}
		}
	};
}

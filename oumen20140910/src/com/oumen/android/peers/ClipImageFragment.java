package com.oumen.android.peers;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.oumen.widget.dialog.ClipView;

public class ClipImageFragment extends BaseFragment implements OnTouchListener {
	public static final String INTENT_CROP_IMAGE = "crop_image";
	public static final String INTENT_CROP_IMAGE_BACK = "bitmap";
	public static final String INTENT_CROP_IMAGET_PATH = "image_path";
	public static final int DEFAULT_IMAGE_MAX_BYTES = 1024 * 128;

	private View view;
	private ImageView srcPic;// 需裁剪的图片
	
	private TitleBar titlebar;
	private Button btnBack;// 返回
	private TextView tvTitle;// 标题
	private Button btnSure;// 使用按钮
	
	
	private ClipView clipview;// 裁剪框
	private FrameLayout framLayout;

	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	private String imagePath;

	private int statusBarHeight = 0;
	private int titleBarHeight = 0;
	
	private int bitmapWidth = 0;
	private int bitmapHeight = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.clipimage, container, false);

		srcPic = (ImageView) view.findViewById(R.id.src_pic);
		
		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		btnBack = titlebar.getLeftButton();
		tvTitle = titlebar.getTitle();
		btnSure = titlebar.getRightButton();
		
		clipview = (ClipView) view.findViewById(R.id.clipview);
		framLayout = (FrameLayout) view.findViewById(R.id.framlayout);
		framLayout.post(new Runnable() {
			
			@Override
			public void run() {
				int width = framLayout.getWidth();
				int height = framLayout.getHeight();
				ELog.i("width =" + width + ",height = " + height);
				ELog.i("bitmapWidth =" + bitmapWidth + ",bitmapHeight = " + bitmapHeight);
				matrix.preTranslate((width - bitmapWidth)/2, (height - bitmapHeight)/2);
				srcPic.setImageMatrix(matrix);
			}
		});

		btnBack.setOnClickListener(clickListener);
		btnSure.setOnClickListener(clickListener);
		srcPic.setOnTouchListener(this);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// 获取传递过来的图片路径
		imagePath = getActivity().getIntent().getStringExtra(INTENT_CROP_IMAGE);
		ELog.i(imagePath);
		Bitmap b = ImageTools.decodeSourceFile(imagePath);
		bitmapWidth = b.getWidth();
		bitmapHeight = b.getHeight();
		ELog.i("width = " + bitmapWidth + "/ height = " + bitmapHeight);
		
		if (bitmapWidth > App.IMAGE_SIZE_MAX && bitmapHeight > App.IMAGE_SIZE_MAX) {
			float scale = ImageTools.rate(bitmapWidth, bitmapHeight, App.IMAGE_SIZE_MAX);
			// 压缩图片
			b = ImageTools.scale(b, scale);
			bitmapWidth = b.getWidth();
			bitmapHeight = b.getHeight();
			ELog.i("width = " + bitmapWidth + "/ height = " + bitmapHeight);
		}
		srcPic.setImageBitmap(b);
		
		tvTitle.setText("裁剪图片");
		btnSure.setText("使用");
	}

	@Override
	public void onDestroyView() {
		ViewGroup parent = (ViewGroup) view.getParent();
		parent.removeAllViews();
		super.onDestroyView();
	}

	final OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.left:// 返回
					getActivity().finish();
					break;

				case R.id.right:// 使用
					try {
						Bitmap fianBitmap = getBitmap();
						ELog.i("裁剪完了以后，图片大小为："+fianBitmap.getWidth()+"/"+fianBitmap.getHeight());
						String clipPath = System.currentTimeMillis() + "clip.jpg";
						File file = new File(getActivity().getCacheDir().getAbsoluteFile() + "/clip", clipPath);
						ImageTools.save(fianBitmap, file, 100);
						Intent intent = new Intent();
						ELog.i(getActivity().getCacheDir().getAbsoluteFile() + "/clip/" + clipPath);
						intent.putExtra(INTENT_CROP_IMAGE_BACK, getActivity().getCacheDir().getAbsoluteFile() + "/clip/" + clipPath);
						getActivity().setResult(Activity.RESULT_OK, intent);
						getActivity().finish();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					break;
			}
		}
	};

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				savedMatrix.set(matrix);
				// 設置初始點位置
				start.set(event.getX(), event.getY());
				ELog.i("mode=DRAG");
				mode = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				ELog.i("oldDist=" + oldDist);
				if (oldDist > 10f) {
					savedMatrix.set(matrix);
					midPoint(mid, event);
					mode = ZOOM;
					ELog.i("mode=ZOOM");
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				ELog.i("mode=NONE");
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					// ...
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
				}
				else if (mode == ZOOM) {
					float newDist = spacing(event);
					if (newDist > 10f) {
						matrix.set(savedMatrix);
						float scale = newDist / oldDist;
						matrix.postScale(scale, scale, mid.x, mid.y);
					}
				}
				break;
		}

		view.setImageMatrix(matrix);
		return true; // indicate event was handled
	}

	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	/* 获取矩形区域内的截图 */
	private Bitmap getBitmap() {
		getBarHeight();
		Bitmap screenShoot = takeScreenShot();

		int width = clipview.getWidth();
		int height = clipview.getHeight();
		int Windowwidth = width;
		ELog.i(width+"/"+width);
		Bitmap finalBitmap = Bitmap.createBitmap(screenShoot, (width - Windowwidth) / 2 + 2, (height - Windowwidth) / 2 + titleBarHeight + statusBarHeight + 2, Windowwidth - 4, Windowwidth - 4);
		return finalBitmap;
	}

	private void getBarHeight() {
		// 获取状态栏高度
		Rect frame = new Rect();
		getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		statusBarHeight = frame.top;

		int contenttop = getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		// statusBarHeight是上面所求的状态栏的高度
		titleBarHeight = contenttop - statusBarHeight;

		ELog.i("statusBarHeight = " + statusBarHeight + ", titleBarHeight = " + titleBarHeight);
	}

	// 获取Activity的截屏
	private Bitmap takeScreenShot() {
		View view = getActivity().getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		return view.getDrawingCache();
	}

}

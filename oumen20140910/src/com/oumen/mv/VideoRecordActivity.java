package com.oumen.mv;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.tools.ELog;

public class VideoRecordActivity extends BaseActivity {
	public static final String PARAM_KEY_SAVE_PATH = "save";

	private Button btn_flash;// 闪光灯
	private ImageButton btn_carema;// 前置摄像头和后置摄像头切换按钮
	private Button btnBack;// 返回
	private Button btnSubmit;// 完成
	private Button mVideoStartBtn;// 开始录制
	private TextView timer;// 录制时间
	private SurfaceView mSurfaceview;
	private RelativeLayout relayout;

	private boolean isRecording = false;// 是否正在录制
	protected boolean isPreview;
	private int cameraPosition = 1;// 0代表前置摄像头，1代表后置摄像头

	protected Camera camera;

	private SurfaceHolder holder;
	private MediaRecorder mMediaRecorder;
	private SurfaceHolder mSurfaceHolder;

	private File targetFile;

	private int FLASH_MODE = 0;// 闪光灯

	private int second = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.land_page_capture_video);
		
		if (savedInstanceState == null) {
			targetFile = new File(getIntent().getStringExtra(PARAM_KEY_SAVE_PATH));
		}
		else {
			targetFile = new File(savedInstanceState.getString(PARAM_KEY_SAVE_PATH));
		}
		if (!targetFile.getParentFile().exists())
			targetFile.getParentFile().mkdirs();

		btn_flash = (Button) findViewById(R.id.btn_flash);
		btn_carema = (ImageButton) findViewById(R.id.btn_carema);

		btnBack = (Button) findViewById(R.id.btnBack);
		btnSubmit = (Button) findViewById(R.id.btncommit);

		mVideoStartBtn = (Button) findViewById(R.id.btnRecoder);
		timer = (TextView) findViewById(R.id.tv_timer);
		updateTime();

		mSurfaceview = (SurfaceView) findViewById(R.id.surfaceView);
		holder = mSurfaceview.getHolder();
		holder.addCallback(callback);
		// 设置录像区域的大小
		relayout = (RelativeLayout) findViewById(R.id.left);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relayout.getLayoutParams();
		params.width = getResources().getDisplayMetrics().widthPixels * 3 / 4;
		params.height = getResources().getDisplayMetrics().heightPixels;
		relayout.setLayoutParams(params);

		btnBack.setOnClickListener(clickListener);
		btn_flash.setOnClickListener(clickListener);
		btn_carema.setOnClickListener(clickListener);
		mVideoStartBtn.setOnClickListener(clickListener);
		btnSubmit.setOnClickListener(clickListener);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(PARAM_KEY_SAVE_PATH, targetFile.getAbsolutePath());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		if (isRecording) {
			Toast.makeText(this, R.string.mv_record_err_back, Toast.LENGTH_SHORT).show();
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		if (isRecording) {
			if (mMediaRecorder != null) {
				mMediaRecorder.stop();
				mMediaRecorder.release();
				mMediaRecorder = null;
			}
			if (targetFile.exists()) {
				targetFile.delete();
			}
		}
		else {
			if (mMediaRecorder != null) {
				mMediaRecorder.release();
				mMediaRecorder = null;
			}
		}
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (isRecording) {
			if (mMediaRecorder != null) {
				mMediaRecorder.stop();
				mMediaRecorder.release();
				mMediaRecorder = null;
			}
		}
		else {
			if (mMediaRecorder != null) {
				mMediaRecorder.release();
				mMediaRecorder = null;
			}
		}
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btnBack:// 返回
					if (isRecording) {
						stop();
					}
					if (targetFile.exists()) {
						targetFile.delete();
					}
					setResult(Activity.RESULT_CANCELED);
					finish();
					break;
					
				case R.id.btncommit:// 完成
					if (second == 0)
						return;
					
					if (isRecording) {
						stop();
					}
					setResult(Activity.RESULT_OK);
					finish();
					break;

				case R.id.btnRecoder:// 开始录制
					if (!isRecording) {
						if (targetFile.exists())
							targetFile.delete();
						
						btn_flash.setVisibility(View.GONE);
						btn_carema.setVisibility(View.GONE);
						mVideoStartBtn.setVisibility(View.GONE);
						second = 0;

						if (camera != null) {
							camera.unlock();
						}

						try {
							if (mMediaRecorder == null) {
								mMediaRecorder = new MediaRecorder();
							}
							else {
								mMediaRecorder.reset();
							}
							mMediaRecorder.setCamera(camera);
							mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
							mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
							mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
							
							boolean hasAudioEncoder = false, hasVideoEncoder = false;
							CamcorderProfile profile = null;
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_TIME_LAPSE_480P)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_480P);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
								else {
									hasAudioEncoder = true;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_TIME_LAPSE_720P)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_720P);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_TIME_LAPSE_1080P)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_1080P);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_LOW)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_TIME_LAPSE_LOW)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_LOW);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_CIF);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QCIF)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_QCIF);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_TIME_LAPSE_QCIF)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_QCIF);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile == null && CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_TIME_LAPSE_CIF)) {
								profile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_CIF);
								if (profile.videoFrameWidth != cameraWidth || profile.videoFrameHeight != cameraHeight) {
									profile = null;
								}
							}
							if (profile != null) {
								mMediaRecorder.setProfile(profile);
							}
							
							mMediaRecorder.setOutputFile(targetFile.getAbsolutePath());
							if (profile == null) {
								mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
								mMediaRecorder.setVideoFrameRate(30);
								mMediaRecorder.setVideoEncodingBitRate(3449000);
								mMediaRecorder.setAudioSamplingRate(48000);
								mMediaRecorder.setAudioEncodingBitRate(128000);
								mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
								mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
								mMediaRecorder.setVideoSize(cameraWidth, cameraHeight);
								hasAudioEncoder = false;
								hasVideoEncoder = false;
							}
							if (hasAudioEncoder) {
								mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
							}
							if (hasVideoEncoder) {
								mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
							}
							
							mMediaRecorder.prepare();
							handler.postDelayed(task, 1000);
							mMediaRecorder.start();
							isRecording = true;
							ELog.i("Started");
							break;
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					else {
						stop();
					}
					break;
					
				case R.id.btn_carema:// 前置摄像头与后置摄像头的切换
					if (cameraPosition == 1) {// 后置
						cameraPosition = 0;
					}
					else {
						cameraPosition = 1;
					}
					if (camera != null) {
						if (isPreview) {
							camera.stopPreview();
							isPreview = false;
						}
						camera.release();
						camera = null;
					}
					initCamera(mSurfaceHolder);
					ELog.i(mSurfaceHolder.toString());
					break;
					
				case R.id.btn_flash://闪光灯
					if (FLASH_MODE == 1) {
						FLASH_MODE = 0;
						btn_flash.setText("开启");
					}
					else {
						FLASH_MODE = 1;
						btn_flash.setText("关闭");
					}
					if (camera != null) {
						if (isPreview) {
							camera.stopPreview();
							isPreview = false;
						}
						camera.release();
						camera = null;
					}

					initCamera(mSurfaceHolder);
					break;
			}

		}
	};
	
	private final Callback callback = new Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (camera != null) {
				if (isPreview) {
					camera.stopPreview();
					isPreview = false;
				}
				camera.release();
				camera = null;
			}
			mSurfaceview = null;
			mSurfaceHolder = null;
			mMediaRecorder = null;

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			initCamera(holder);
			mSurfaceHolder = holder;

		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			mSurfaceHolder = holder;
		}
	};
	
	private void stop() {
		try {
			btn_flash.setVisibility(View.VISIBLE);
			btn_carema.setVisibility(View.VISIBLE);
			mVideoStartBtn.setVisibility(View.GONE);

			mMediaRecorder.stop();

			mMediaRecorder.release();
			mMediaRecorder = null;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		isRecording = !isRecording;
		try {
			camera.lock();
			isPreview = true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int cameraWidth, cameraHeight;
	
	public void initCamera(SurfaceHolder holder) {
		try {

			int count = Camera.getNumberOfCameras();
			if (count > 1) {
				if (cameraPosition == 1) {
					camera = Camera.open(0);
				}
				else {
					camera = Camera.open(1);
				}
			}
			else {
				camera = Camera.open(0);
			}

			Camera.Parameters parameters = camera.getParameters();

			if (FLASH_MODE == 0) {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			}
			else {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			}

			List<Size> previewsizes = parameters.getSupportedPreviewSizes();

			int desiredwidth = 640, desiredheight = 480;

			Size optimalPreviewSize = getOptimalPreviewSize(previewsizes, desiredwidth, desiredheight);
			cameraWidth = optimalPreviewSize.width;
			cameraHeight = optimalPreviewSize.height;
			ELog.i("Size:" + cameraWidth + "/" + cameraHeight);

			parameters.setPreviewSize(cameraWidth, cameraHeight);

			camera.setParameters(parameters);
			camera.setPreviewDisplay(holder);
			camera.startPreview();
			isPreview = true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.2;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the
		// requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		return optimalSize;
	}
	
	private void updateTime() {
		int tmp = MvActivity.VIDEO_DURATION_MAX - second;
		int min = tmp / 60, sec = tmp % 60;
		timer.setText(App.NUMBER_FORMAT.format(min) + ":" + App.NUMBER_FORMAT.format(sec));
	}

	/*
	 * 定时器设置，实现计时
	 */
	private Runnable task = new Runnable() {
		public void run() {
			if (isRecording) {
				handler.postDelayed(this, 1000);
				second++;
				if (second >= MvActivity.VIDEO_DURATION_MAX) {
					Toast.makeText(VideoRecordActivity.this, R.string.mv_compose_err_time_too_long, Toast.LENGTH_SHORT).show();
					stop();
				}
				updateTime();
			}
		}
	};
}

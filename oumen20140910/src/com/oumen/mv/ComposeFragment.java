package com.oumen.mv;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.tools.ImageTools;
import com.oumen.widget.file.PickImageFragment;

public class ComposeFragment extends BaseFragment {
	public static final String PARAM_KEY_SELECTED_AUDIO = "selected_audio";
	public static final String PARAM_KEY_SELECTED_SOURCE_VIDEO = "source_video";
	public static final String PARAM_KEY_SELECTED_PREFIX_VIDEO = "selected_video";
	public static final String PARAM_KEY_SELECTED_IMAGE = "selected_image";
	public static final String PARAM_KEY_MV_NAME = "name";
	public static final String PARAM_KEY_MV_PREFIX_ID = "prefix_id";
	public static final String PARAM_KEY_COMPOSE_TYPE = "compose_type";
	public static final String PARAM_KEY_START_TIME = "start";
	public static final String PARAM_KEY_END_TIME = "end";
	public static final String PARAM_KEY_OPEN_WINDOW = "open_window";
	
	private final int ACTIVITY_REQUEST_CODE = 999;
	
	protected final int PICK_IMAGE_MIN = 6;
	protected final int PICK_IMAGE_MAX = 20;
	
	private MvActivity host;
	
	private int composeAction;
	
	protected EditText edtTitle;
	protected Button btnSelectAudio;
	protected Button btnSelectBeginVideo;
	protected ImageButton btnAddImage;
	protected LinearLayout layoutImagesPreview;
	protected Button btnRecordAndEdit;
	protected VideoPlayerView videoPlayer;
	
	protected String selectedAudio;
	protected PrefixVideo selectedVideo;
	protected String[] selectedImages;
	protected String selectedLocalVideo;
	
	protected File recordFile;
	
	protected File editFile;
	protected int start;
	protected int end;
	
	private boolean isOpenWindow = false;
	
	private final BitmapProcessor preBitmapProcessor = new BitmapProcessor() {
		
		@Override
		public Bitmap process(Bitmap bitmap) {
			Bitmap img = ImageTools.clip2square(bitmap);
//			bitmap.recycle();
			return img;
		}
	};
	
	private final DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(android.R.drawable.ic_menu_gallery)
		.showImageOnFail(android.R.drawable.ic_menu_gallery)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.preProcessor(preBitmapProcessor)
		.build();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		host = (MvActivity)getActivity();
		
//		App.THREAD.execute(new Runnable() {
//			
//			@Override
//			public void run() {
//				MvHelper.installPrefixVideo(host);
//				MvHelper.installSuffixVideo(host);
//				ELog.i("Initialized");
//				
//				MV mv = new MV(1, 1, new FFMpegListener() {
//					
//					@Override
//					public void onEvent(Phase phase, FFMpeg target, Object data) {
//						ELog.i("Phase:" + phase);
//					}
//				});
//				try {
//					mv.init(host, com.oumen.widget.R.raw.ffmpeg);
//				}
//				catch (IOException e) {
//					e.printStackTrace();
//				}
////				StringBuilder buf = new StringBuilder(" -y -i /storage/sdcard0/oumen/mv/vc/target.mpg -i /storage/sdcard0/oumen/mv/vc/audio.mp3 -filter_complex amix=inputs=2:duration=shortest:dropout_transition=0 /storage/sdcard0/oumen/mv/vc/test.mpg");
//				StringBuilder buf = null;
////				buf = new StringBuilder(" -y -i concat:/storage/sdcard0/oumen/mv/vc/片尾.ts|/storage/sdcard0/oumen/mv/vc/images.ts|/storage/sdcard0/oumen/mv/vc/幸运宝贝.ts -i /storage/sdcard0/oumen/mv/vc/audio.mp3 -t 32 -filter_complex amix -qscale:v 1 -vcodec mpeg4 -strict -2 /storage/sdcard0/oumen/mv/vc/target.mp4");
////				mv.addCommand(new Command(buf.toString()));
//				buf = new StringBuilder(" -y -ss 01:00:00 -t 00:00:03 -i /storage/sdcard0/360Video/360VideoCache/芭比之森林公主.mp4 -vf crop=389:292 -s 640x480 /storage/sdcard0/oumen/mv/vs/edit.ts");
//				mv.addCommand(new Command(buf.toString()));
//				App.THREAD.execute(mv);
//			}
//		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			selectedAudio = savedInstanceState.getString(PARAM_KEY_SELECTED_AUDIO);
			selectedVideo = (PrefixVideo)savedInstanceState.getParcelable(PARAM_KEY_SELECTED_PREFIX_VIDEO);
			selectedImages = savedInstanceState.getStringArray(PARAM_KEY_SELECTED_IMAGE);
			selectedLocalVideo = savedInstanceState.getString(PARAM_KEY_SELECTED_SOURCE_VIDEO);
			composeAction = savedInstanceState.getInt(PARAM_KEY_COMPOSE_TYPE, MV.ACTION_COMPOSE_WITH_IMAGES);
			start = savedInstanceState.getInt(PARAM_KEY_START_TIME);
			end = savedInstanceState.getInt(PARAM_KEY_END_TIME);
			isOpenWindow = savedInstanceState.getBoolean(PARAM_KEY_OPEN_WINDOW);
		}
		else {
			composeAction = getArguments().getInt(PARAM_KEY_COMPOSE_TYPE, MV.ACTION_COMPOSE_WITH_IMAGES);
		}
		
		if (composeAction == MV.ACTION_COMPOSE_WITH_RECORD)
			recordFile = new File(App.PATH_MV + "/vs", "record.mp4");
		else if (composeAction == MV.ACTION_COMPOSE_WITH_EDIT)
			editFile = new File(App.PATH_MV + "/vs", "edit.mp4");
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.mv_compose, null);
		
		edtTitle = (EditText) root.findViewById(R.id.edt_title);
		
		btnSelectAudio = (Button) root.findViewById(R.id.select_audio);
		btnSelectAudio.setOnClickListener(clickListener);
		
		btnSelectBeginVideo = (Button) root.findViewById(R.id.select_video);
		btnSelectBeginVideo.setOnClickListener(clickListener);
		
		btnAddImage = (ImageButton) root.findViewById(R.id.add_image);
		btnAddImage.setOnClickListener(clickListener);
		
		videoPlayer = (VideoPlayerView) root.findViewById(R.id.video_player);
		videoPlayer.setOnClickListener(clickListener);
		
		if (composeAction == MV.ACTION_COMPOSE_WITH_IMAGES) {
			View tmp = root.findViewById(R.id.record_edit);
			root.removeView(tmp);
			
			layoutImagesPreview = (LinearLayout) root.findViewById(R.id.images_preview_container);
			
			if (selectedImages != null) {
				layoutImagesPreview.removeAllViews();
				int size = getResources().getDimensionPixelSize(R.dimen.medium_photo_size);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
				params.rightMargin = host.getResources().getDimensionPixelSize(R.dimen.padding_default);
				for (String path : selectedImages) {
					ImageView img = new ImageView(layoutImagesPreview.getContext());
					layoutImagesPreview.addView(img, params);
					ImageLoader.getInstance().displayImage(path, img, options);
				}
				layoutImagesPreview.addView(btnAddImage, params);
			}
		}
		else {
			View tmp = root.findViewById(R.id.scroll_container);
			root.removeView(tmp);

			btnRecordAndEdit = (Button) root.findViewById(R.id.record_edit);
			btnRecordAndEdit.setOnClickListener(clickListener);
			
			if (composeAction == MV.ACTION_COMPOSE_WITH_RECORD) {
				if (recordFile.exists()) {
					btnRecordAndEdit.setText(R.string.mv_compose_record_video_finish);
				}
				else {
					btnRecordAndEdit.setText(R.string.mv_compose_record_video);
				}
			}
			else if (composeAction == MV.ACTION_COMPOSE_WITH_EDIT) {
				if (editFile.exists()) {
					btnRecordAndEdit.setText(R.string.mv_compose_edit_video_finish);
				}
				else {
					btnRecordAndEdit.setText(R.string.mv_compose_edit_video);
				}
			}
		}
		
		if (!TextUtils.isEmpty(selectedAudio)) {
			String name = selectedAudio.substring(selectedAudio.lastIndexOf(File.separatorChar) + 1);
			name = name.substring(0, name.lastIndexOf('.'));
			btnSelectAudio.setText(name);
		}
		
		if (selectedVideo != null) {
			btnSelectBeginVideo.setText(selectedVideo.name);
			showVideoPreview();
		}
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (!isOpenWindow) {
			switch (composeAction) {
				case MV.ACTION_COMPOSE_WITH_IMAGES:
					switchAddImages();
					break;
					
				case MV.ACTION_COMPOSE_WITH_RECORD:
					switchRecord();
					break;
					
				case MV.ACTION_COMPOSE_WITH_EDIT:
					switchEdit();
					break;
			}
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(PARAM_KEY_SELECTED_AUDIO, selectedAudio);
		outState.putParcelable(PARAM_KEY_SELECTED_PREFIX_VIDEO, selectedVideo);
		outState.putString(PARAM_KEY_SELECTED_SOURCE_VIDEO, selectedLocalVideo);
		outState.putStringArray(PARAM_KEY_SELECTED_IMAGE, selectedImages);
		outState.putInt(PARAM_KEY_COMPOSE_TYPE, composeAction);
		outState.putInt(PARAM_KEY_START_TIME, start);
		outState.putInt(PARAM_KEY_END_TIME, end);
		outState.putBoolean(PARAM_KEY_OPEN_WINDOW, isOpenWindow);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			if (recordFile.exists()) {
				btnRecordAndEdit.setText(R.string.mv_compose_record_video_finish);
			}
			else {
				btnRecordAndEdit.setText(R.string.mv_compose_record_video);
			}
		}
	}
	
	private void showVideoPreview() {
		videoPlayer.setVisibility(View.VISIBLE);
		videoPlayer.setVideo(selectedVideo.videoFile.getAbsolutePath(), MvHelper.getCoverPath(PrefixVideo.obtainTitle(selectedVideo.id)));
	}

	public String getSelectLocalVideo() {
		return selectedLocalVideo;
	}

	public void setSelectLocalVideo(String selectLocalVideo) {
		this.selectedLocalVideo = selectLocalVideo;
	}
	
	private void switchAddImages() {
		isOpenWindow = true;
		Bundle arguments = new Bundle();
		arguments.putInt(PickImageFragment.PARAM_KEY_MAX, PICK_IMAGE_MAX);
		arguments.putStringArray(PickImageFragment.PARAM_KEY_SELECTS, selectedImages);
		arguments.putString(PickImageFragment.PARAM_KEY_PREVIEW_BAR_TIP, getString(R.string.mv_compose_images_count));
		host.switchFragment(MvActivity.FragmentType.PICK_IMAGE, arguments);
	}
	
	private void switchRecord() {
		isOpenWindow = true;
		Intent intent = new Intent(host, VideoRecordActivity.class);
		intent.putExtra(VideoRecordActivity.PARAM_KEY_SAVE_PATH, recordFile.getAbsolutePath());
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
	}
	
	private void switchEdit() {
		isOpenWindow = true;
		host.switchFragment(MvActivity.FragmentType.PICK_LOCAL_VIDEO);
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.add_image) {
				switchAddImages();
			}
			else if (id == R.id.record_edit) {
				if (composeAction == MV.ACTION_COMPOSE_WITH_RECORD) {
					switchRecord();
				}
				else if (composeAction == MV.ACTION_COMPOSE_WITH_EDIT) {
					switchEdit();
				}
			}
			else if (id == R.id.select_audio) {
				host.switchFragment(MvActivity.FragmentType.COMPOSE_AND_PICK_AUDIO);
			}
			else if (id == R.id.select_video) {
				host.switchFragment(MvActivity.FragmentType.PICK_PREFIX_VIDEO);
			}
		}
	};
	
	protected final View.OnClickListener submitListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
//			FragmentType type = host.getType();
//
//			switch (type) {
//				case PICK_AUDIO:
//					selectedAudio = host.getSelectedAudio();
//					if (selectedAudio == null) {
//						Toast.makeText(host, "请选择音乐", Toast.LENGTH_SHORT).show();
//						return;
//					}
//					host.back();
//					break;
//					
//				case PICK_PREFIX_VIDEO:
//					selectedVideo = host.getSelectedVideo();
//					if (selectedVideo == null) {
//						Toast.makeText(host, "请选择片头", Toast.LENGTH_SHORT).show();
//						return;
//					}
//					host.back();
//					showVideoPreview();
//					break;
//					
//				case PICK_LOCAL_VIDEO:
//					selectedLocalVideo = host.getSelectLocalVideo();
//					if (selectedLocalVideo == null) {
//						Toast.makeText(host, "请选择本地视频", Toast.LENGTH_SHORT).show();
//						return;
//					}
//					host.back();
//				    host.switchFragment(MvActivity.FragmentType.VIDEO_EDIT, selectedLocalVideo);
//					break;
//					
//				case VIDEO_EDIT:
//					ELog.i("VIDEO_EDIT");
//					start = host.getStart() / 1000;
//					end = host.getEnd() / 1000;
//					host.back();
//					btnRecordAndEdit.setText(R.string.mv_compose_edit_video_finish);
//					break;
//					
//				case COMPOSE:
//					compose();
//					break;
//					
//				case PICK_IMAGE:
//					break;
//			}
		}
	};
}

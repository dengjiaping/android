package com.oumen.mv;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.tools.ELog;
import com.oumen.widget.dialog.TwoButtonDialog;
import com.oumen.widget.file.ImageData;
import com.oumen.widget.file.PickImageFragment;

public class MvActivity extends FragmentActivity implements OnBackStackChangedListener {
	public static final int REQUEST_CODE_MV_ACTIVITY = 1;
	
	public static final int VIDEO_DURATION_MAX = 30;
	
	private final int ACTIVITY_REQUEST_CODE = 999;
	
	private final String PARAM_KEY_FRAGMENT_TYPE = "type";
	
	public static final String PARAM_KEY_SELECTED_AUDIO = "selected_audio";
	public static final String PARAM_KEY_SELECTED_SOURCE_VIDEO = "source_video";
	public static final String PARAM_KEY_SELECTED_PREFIX_VIDEO = "selected_video";
	public static final String PARAM_KEY_SELECTED_IMAGE = "selected_image";
	public static final String PARAM_KEY_TITLE = "name";
	public static final String PARAM_KEY_PREFIX_ID = "prefix_id";
	public static final String PARAM_KEY_COMPOSE_TYPE = "compose_type";
	public static final String PARAM_KEY_START_TIME = "start";
	public static final String PARAM_KEY_END_TIME = "end";
	public static final String PARAM_KEY_PREVIEW_FILE = "preview";
	public static final String PARAM_CREATE_AT = "create_at";
	
	protected final int PICK_IMAGE_MIN = 6;
	protected final int PICK_IMAGE_MAX = 20;
	
	public enum FragmentType {
		PICK_IMAGE {
			@Override
			public int getTitle() {
				return R.string.pick_image;
			}

			@Override
			public int getTitleBarRightButtonText() {
				return R.string.choose;
			}
		},
		COMPOSE_AND_PICK_AUDIO {
			@Override
			public int getTitle() {
				return R.string.pick_audio;
			}

			@Override
			public int getTitleBarRightButtonText() {
				return R.string.submit;
			}
		},
		COMPOSE_FINAL{
			@Override
			public int getTitle() {
				return R.string.pick_audio;
			}

			@Override
			public int getTitleBarRightButtonText() {
				return R.string.choose;
			}
		},
		PICK_PREFIX_VIDEO {
			@Override
			public int getTitle() {
				return R.string.mv_select_begin_video;
			}

			@Override
			public int getTitleBarRightButtonText() {
				return R.string.choose;
			}
		},
		PICK_LOCAL_VIDEO {
			@Override
			public int getTitle() {
				return R.string.mv_select_local_video;
			}

			@Override
			public int getTitleBarRightButtonText() {
				return R.string.choose;
			}
		},
		VIDEO_EDIT {
			@Override
			public int getTitle() {
				return R.string.mv_edit;
			}

			@Override
			public int getTitleBarRightButtonText() {
				return R.string.finish;
			}
		},
		PREVIEW_PREFIX_VIDEO {
			@Override
			public int getTitle() {
				return R.string.mv_preview_prefix_video;
			}

			@Override
			public int getTitleBarRightButtonText() {
				return 0;
			}
		};
		
		abstract public int getTitle();
		abstract public int getTitleBarRightButtonText();
	};
	
	private FragmentType type;
	private int composeAction;

	private PickImageFragment fragPickImages;
	private MvVideoEditFragment fragVideoEdit;
	private PickLocalVideoFragment fragPickLocalVideo;
	private PickPrefixVideoFragment fragPickPrefix;
	private ComposeAndPickAudioFragment fragComposeAndPickAudio;
	private ComposeFinalFragment fragComposeFinal;
	private PreviewPrefixVideoFragment fragPreviewPrefix;
	
	private TitleBar titlebar;
	private TextView txtTitle;
	private Button btnLeft;
	private Button btnRight;

	protected final MvInfo data = new MvInfo();
	protected String selectedAudio;
	protected String[] selectedImages;
	protected String selectedLocalVideo;
	protected int clipStart;
	protected int clipEnd;
	protected File previewFile;
	
	protected TwoButtonDialog dialogBack;
	
	protected final File sourceVideoFile = new File(App.getMvSourceCachePath(), "source.mp4");
//	protected final File sourceVideoFile = new File(App.PATH_MV + "/vs/source.mp4");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mv);
		
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		txtTitle = titlebar.getTitle();
		
		btnLeft = titlebar.getLeftButton();
		btnLeft.setOnClickListener(clickListener);
		
		btnRight = titlebar.getRightButton();
		
		getSupportFragmentManager().addOnBackStackChangedListener(this);
		
		composeAction = getIntent().getIntExtra(ComposeFragment.PARAM_KEY_COMPOSE_TYPE, MV.ACTION_COMPOSE_WITH_IMAGES);
		data.createAt = (Calendar) getIntent().getSerializableExtra(PARAM_CREATE_AT);
		data.userId = App.PREFS.getUid();
		
		if (savedInstanceState == null) {
			switch (composeAction) {
				case MV.ACTION_COMPOSE_WITH_IMAGES:
					type = FragmentType.PICK_IMAGE;
					break;
					
				case MV.ACTION_COMPOSE_WITH_EDIT:
					type = FragmentType.PICK_LOCAL_VIDEO;
					break;
					
				case MV.ACTION_COMPOSE_WITH_RECORD:
					type = null;
					break;
			}
		}
		else {
			type = (FragmentType) savedInstanceState.getSerializable(PARAM_KEY_FRAGMENT_TYPE);
			composeAction = savedInstanceState.getInt(ComposeFragment.PARAM_KEY_COMPOSE_TYPE);
		}

		switchFragment();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(PARAM_KEY_FRAGMENT_TYPE, type);
		outState.putSerializable(ComposeFragment.PARAM_KEY_COMPOSE_TYPE, composeAction);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		if (isShowingBackDialog())
			dialogBack.cancel();
		dialogBack = null;
		super.onDestroy();
	}

	public void switchFragment(FragmentType type, Object... params) {
		this.type = type;
		switchFragment(params);
	}

	protected void switchFragment(Object... params) {
		if (isShowingBackDialog()) {
			dialogBack.dismiss();
		}
		
		if (type == null) {
			if (sourceVideoFile.exists())
				sourceVideoFile.delete();
			Intent intent = new Intent(getApplicationContext(), VideoRecordActivity.class);
			intent.putExtra(VideoRecordActivity.PARAM_KEY_SAVE_PATH, sourceVideoFile.getAbsolutePath());
			startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
			return;
		}
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		switch (type) {
			case PICK_IMAGE:
				if (fragPickImages == null) {
					fragPickImages = new PickImageFragment();
					fragPickImages.setCallback(new PickImageFragment.Callback() {
						
						@Override
						public void onPreview(ArrayList<ImageData> data) {}
						
						@Override
						public void onComplete(String[] selects) {
							selectedImages = fragPickImages.getSelects();
							if (selectedImages == null || selectedImages.length == 0 || selectedImages.length < PICK_IMAGE_MIN || selectedImages.length > PICK_IMAGE_MAX) {
								selectedImages = null;
								Toast.makeText(MvActivity.this, R.string.mv_compose_images_count, Toast.LENGTH_SHORT).show();
								return;
							}
							switchFragment(FragmentType.PICK_PREFIX_VIDEO);
						}
					});
				}
				Bundle args = new Bundle();
				args.putInt(PickImageFragment.PARAM_KEY_MAX, PICK_IMAGE_MAX);
				args.putStringArray(PickImageFragment.PARAM_KEY_SELECTS, selectedImages);
				args.putString(PickImageFragment.PARAM_KEY_PREVIEW_BAR_TIP, getString(R.string.mv_compose_images_count));
				fragPickImages.setArguments(args);
				transaction.replace(R.id.container, fragPickImages).commit();
				break;
				
			case COMPOSE_AND_PICK_AUDIO:
				if (fragComposeAndPickAudio == null) {
					fragComposeAndPickAudio = new ComposeAndPickAudioFragment();
				}
				transaction.replace(R.id.container, fragComposeAndPickAudio).commit();
				
				btnRight.setOnClickListener(clickListener);
				break;
				
			case COMPOSE_FINAL:
				if (fragComposeFinal == null) {
					fragComposeFinal = new ComposeFinalFragment();
				}
				fragComposeFinal.setOnClickListener(clickListener);
				
				transaction.replace(R.id.container, fragComposeFinal).commit();
				break;
				
			case PICK_PREFIX_VIDEO:
				if (fragPickPrefix == null) {
					fragPickPrefix = new PickPrefixVideoFragment();
				}
				if (composeAction == MV.ACTION_COMPOSE_WITH_RECORD)
					transaction.replace(R.id.container, fragPickPrefix).commitAllowingStateLoss();
				else
					transaction.replace(R.id.container, fragPickPrefix).commit();
				
				btnRight.setOnClickListener(clickListener);
				break;
				
			case PICK_LOCAL_VIDEO:
				if (sourceVideoFile.exists())
					sourceVideoFile.delete();
				if (fragPickLocalVideo == null) {
					fragPickLocalVideo = new PickLocalVideoFragment();
				}
				transaction.replace(R.id.container, fragPickLocalVideo).commit();
				
				btnRight.setOnClickListener(clickListener);
				break;
				
			case VIDEO_EDIT:
				if (fragVideoEdit == null) {
					fragVideoEdit = new MvVideoEditFragment();
				}
				transaction.replace(R.id.container, fragVideoEdit).commit();
				
				btnRight.setOnClickListener(clickListener);
				break;
				
			case PREVIEW_PREFIX_VIDEO:
				if (fragPreviewPrefix == null) {
					fragPreviewPrefix = new PreviewPrefixVideoFragment();
				}
				fragPreviewPrefix.setOnClickListener(clickListener);
				
				transaction.replace(R.id.container, fragPreviewPrefix).commit();
				break;
		}

		update();
	}
	
	private void update() {
		txtTitle.setText(type.getTitle());

		switch (type) {
			case PICK_IMAGE:
			case PREVIEW_PREFIX_VIDEO:
			case COMPOSE_FINAL:
				btnRight.setVisibility(View.GONE);
				break;
				
			case COMPOSE_AND_PICK_AUDIO:
			case PICK_PREFIX_VIDEO:
			case PICK_LOCAL_VIDEO:
			case VIDEO_EDIT:
				btnRight.setText(type.getTitleBarRightButtonText());
				btnRight.setVisibility(View.VISIBLE);
				break;
		}
	}
	
	private final View.OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.left) {
				setResult(Activity.RESULT_CANCELED);
				onBackPressed();
			}
			else if (id == R.id.right) {
				switch (type) {
					case PICK_LOCAL_VIDEO:
						selectedLocalVideo = fragPickLocalVideo.getSelect();
						if (selectedLocalVideo == null) {
							Toast.makeText(MvActivity.this, R.string.mv_compose_err_no_edit, Toast.LENGTH_SHORT).show();
							return;
						}
						switchFragment(FragmentType.VIDEO_EDIT);
						break;
						
					case COMPOSE_AND_PICK_AUDIO:
						if (fragComposeAndPickAudio.isComposing) {
							Toast.makeText(MvActivity.this, R.string.mv_compose_err_refuse_action, Toast.LENGTH_SHORT).show();
							return;
						}
						
						selectedAudio = fragComposeAndPickAudio.getSelect();
						if (selectedAudio == null) {
							Toast.makeText(MvActivity.this, R.string.mv_compose_err_no_audio, Toast.LENGTH_SHORT).show();
							return;
						}
						switchFragment(FragmentType.COMPOSE_FINAL);
						break;
						
					case PICK_PREFIX_VIDEO:
						data.prefix = fragPickPrefix.getSelect();
						if (data.prefix == null) {
							Toast.makeText(MvActivity.this, R.string.mv_compose_err_no_video, Toast.LENGTH_SHORT).show();
							return;
						}
						switchFragment(FragmentType.PREVIEW_PREFIX_VIDEO);
						break;
						
					case VIDEO_EDIT:
						clipStart = fragVideoEdit.getStartTime();
						clipEnd = fragVideoEdit.getEndTime();
						if (clipEnd == 0 || clipStart > clipEnd) {
							clipStart = 0;
							clipEnd = 0;
							Toast.makeText(MvActivity.this, R.string.mv_compose_err_edit_time, Toast.LENGTH_SHORT).show();
							return;
						}
						else if (clipEnd - clipStart > VIDEO_DURATION_MAX * 1000) {
							clipStart = 0;
							clipEnd = 0;
							Toast.makeText(MvActivity.this, R.string.mv_compose_err_time_too_long, Toast.LENGTH_SHORT).show();
							return;
						}
						switchFragment(FragmentType.PICK_PREFIX_VIDEO);
						break;

					case PICK_IMAGE:
					case PREVIEW_PREFIX_VIDEO:
					case COMPOSE_FINAL:
						break;
				}
			}
			else if (id == R.id.ok) {
				if (FragmentType.PREVIEW_PREFIX_VIDEO.equals(type)) {
					switchFragment(FragmentType.COMPOSE_AND_PICK_AUDIO);
				}
				else if (FragmentType.COMPOSE_FINAL.equals(type)) {
					String title = fragComposeFinal.getTitle();
					int uid = App.USER.getUid();
					if (TextUtils.isEmpty(title)) {
						Toast.makeText(MvActivity.this, R.string.mv_compose_err_no_title, Toast.LENGTH_SHORT).show();
						return;
					}
					else if (title.indexOf('/') != -1) {
						Toast.makeText(MvActivity.this, R.string.mv_compose_err_title_symbol, Toast.LENGTH_SHORT).show();
						return;
					}
					else if (MvInfo.contain(uid, title, App.DB)) {
						Toast.makeText(MvActivity.this, R.string.mv_compose_err_exist, Toast.LENGTH_SHORT).show();
						return;
					}
					compose(title);
				}
			}
			else if (id == R.id.btn_left) {
				dialogBack.dismiss();
			}
			else if (id == R.id.btn_right) {
				dialogBack.dismiss();
				back();
			}
		}
	};
	
	private void compose(String title) {
		Intent intent = new Intent(this, MvComposeService.class);
		intent.putExtra(MvComposeService.INTENT_KEY_PARAM_ACTION, MV.ACTION_COMPOSE_FINAL);
		intent.putExtra(PARAM_KEY_TITLE, title);
		intent.putExtra(PARAM_KEY_PREVIEW_FILE, previewFile.getAbsolutePath());
		intent.putExtra(PARAM_KEY_SELECTED_AUDIO, selectedAudio);
		startService(intent);
		
		setResult(Activity.RESULT_OK);
		finish();
	}
	
	protected void compose() {
		MvComposeService.target = data;
		Intent intent = new Intent(this, MvComposeService.class);
		intent.putExtra(MvComposeService.INTENT_KEY_PARAM_ACTION, composeAction);
		intent.putExtra(PARAM_KEY_SELECTED_PREFIX_VIDEO, data.prefix.videoFile.getAbsolutePath());
		
		if (composeAction == MV.ACTION_COMPOSE_WITH_IMAGES) {
			intent.putExtra(PARAM_KEY_SELECTED_IMAGE, selectedImages);
		}
		else if (composeAction == MV.ACTION_COMPOSE_WITH_RECORD) {
			intent.putExtra(PARAM_KEY_SELECTED_SOURCE_VIDEO, sourceVideoFile.getAbsolutePath());
		}
		else if (composeAction == MV.ACTION_COMPOSE_WITH_EDIT) {
			intent.putExtra(PARAM_KEY_START_TIME, clipStart);
			intent.putExtra(PARAM_KEY_END_TIME, clipEnd);
			intent.putExtra(PARAM_KEY_SELECTED_SOURCE_VIDEO, selectedLocalVideo);
		}
		startService(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK && sourceVideoFile.exists()) {
				switchFragment(FragmentType.PICK_PREFIX_VIDEO);
			}
			else {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onBackPressed() {
		Fragment current = getSupportFragmentManager().findFragmentById(R.id.container);
//		ELog.i("Fragment:" + current);
		if (current instanceof ComposeAndPickAudioFragment) {
			ComposeAndPickAudioFragment frag = (ComposeAndPickAudioFragment)current;
			if (frag.isComposing) {
				Toast.makeText(this, R.string.mv_compose_err_refuse_action, Toast.LENGTH_LONG).show();
				return;
			}
		}
		
		showBackDialog();
	}
	
	protected void showBackDialog() {
		if (dialogBack == null) {
			dialogBack = new TwoButtonDialog(this);
			dialogBack.setCancelable(false);
			dialogBack.getMessageView().setText(R.string.mv_compose_back_dialog_msg);
			dialogBack.getLeftButton().setText(R.string.cancel);
			dialogBack.getLeftButton().setOnClickListener(clickListener);
			dialogBack.getRightButton().setText(R.string.abandon);
			dialogBack.getRightButton().setOnClickListener(clickListener);
		}
		dialogBack.show();
	}
	
	protected boolean isShowingBackDialog() {
		return dialogBack != null && dialogBack.isShowing();
	}
	
	private void back() {
		MV.clear(new File(App.getMvImagesCachePath()), new File(App.getMvVideosCachePath()), new File(App.getMvSourceCachePath()));
		
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	@Override
	public void onBackStackChanged() {
		Fragment current = getSupportFragmentManager().findFragmentById(R.id.container);
		if (current instanceof PickImageFragment) {
			type = FragmentType.PICK_IMAGE;
		}
		else if (current instanceof PickLocalVideoFragment) {
			type = FragmentType.PICK_LOCAL_VIDEO;
		}
		else if (current instanceof PickPrefixVideoFragment) {
			type = FragmentType.PICK_PREFIX_VIDEO;
		}
		else if (current instanceof MvVideoEditFragment) {
			type = FragmentType.VIDEO_EDIT;
		}
		else if (current instanceof PreviewPrefixVideoFragment) {
			type = FragmentType.PREVIEW_PREFIX_VIDEO;
		}
		else if (current instanceof ComposeAndPickAudioFragment) {
			type = FragmentType.COMPOSE_AND_PICK_AUDIO;
		}
		ELog.i("Type:" + type + " Fragment:" + current);
	}
}

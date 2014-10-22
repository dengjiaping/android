package com.oumen.mv.index;

import java.util.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.App.NetworkType;
import com.oumen.auth.ShareView;
import com.oumen.home.FloatViewHostController;
import com.oumen.mv.MvComposeService;
import com.oumen.mv.MvInfo;
import com.oumen.mv.VideoPlayerView;
import com.oumen.widget.CustomProgressBar;

public class PagerItem extends RelativeLayout implements View.OnClickListener, View.OnTouchListener {
	protected MvInfo data;

	protected CustomProgressBar viewProgress;
	protected VideoPlayerView player;
	protected TextView txtTitle;
	protected TextView txtTime;
	protected Button btnAdd;
	protected Button btnAction;

	protected DaysIndexController controller;

	private ShareView viewShare;
	
	private Context context;

	public PagerItem(Context context) {
		this(context, null, 0);
	}

	public PagerItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		this.context = context;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.mv_pager_item, this, true);

		View videoPanel = findViewById(R.id.container);
		viewProgress = (CustomProgressBar) findViewById(R.id.progress);
		player = (VideoPlayerView) findViewById(R.id.player);
		txtTitle = (TextView) findViewById(R.id.title);
		txtTime = (TextView) findViewById(R.id.time);
		btnAdd = (Button) findViewById(R.id.add);
		btnAdd.setOnClickListener(this);
		btnAction = (Button) findViewById(R.id.action);
		btnAction.setOnClickListener(this);

		Resources res = context.getResources();

		int indicatorHeight = res.getDimensionPixelSize(R.dimen.mv_upload_indicator_height);
		int indicatorMargin = res.getDimensionPixelSize(R.dimen.padding_large);
		int indicatorPadding = res.getDimensionPixelSize(R.dimen.padding_micro);
		int indicatorWidth = res.getDisplayMetrics().widthPixels - indicatorMargin * 2 - indicatorPadding * 2 - res.getDimensionPixelSize(R.dimen.padding_micro) * 2 - res.getDimensionPixelSize(R.dimen.padding_super) * 2;
		viewProgress.setPercentSize(res.getDimensionPixelSize(R.dimen.text_large));
		viewProgress.setPercentColor(Color.WHITE);
		viewProgress.setIndicatorBackground(R.drawable.progress_background);
		viewProgress.setIndicatorForeground(R.drawable.progress_foreground);
		viewProgress.setIndicatorWidth(indicatorWidth);
		viewProgress.setIndicatorHeight(indicatorHeight);
		viewProgress.setPadding(indicatorMargin, 0, indicatorMargin, 0);
		viewProgress.setIndicatorPadding(indicatorPadding, indicatorPadding, indicatorPadding, indicatorPadding);
		viewProgress.setOnTouchListener(this);

		int videoWidth = res.getDisplayMetrics().widthPixels - res.getDimensionPixelSize(R.dimen.padding_super) * 2 - res.getDimensionPixelSize(R.dimen.padding_micro) * 2;
		int videoHeight = videoWidth * 3 / 4;
		int videoPanelHeight = videoHeight + res.getDimensionPixelSize(R.dimen.padding_micro) * 2 + res.getDimensionPixelSize(R.dimen.mv_page_item_title_height);

		ViewGroup.LayoutParams params = player.getLayoutParams();
		params.width = videoWidth;
		params.height = videoHeight;
		player.setLayoutParams(params);

		params = videoPanel.getLayoutParams();
		params.height = videoPanelHeight;
		videoPanel.setLayoutParams(params);
	}

	public void setShareView(ShareView shareView) {
		this.viewShare = shareView;
		if (context instanceof IndexActivity) {
			viewShare.setHost((IndexActivity) context);
		}
		else if (context instanceof DayListActivity) {
			viewShare.setHost((DayListActivity) context);
		}
	}

	void update(MvInfo data) {
		if (controller != null) {
			setShareView(controller.getShareView());
		}
		this.data = data;

		txtTitle.setText(data.getTitle());
		txtTime.setText(App.HH_MM_FORMAT.format(data.getCreateAt().getTime()));

		int type = data.getType();
		if (type == MvInfo.TYPE_UPLOAD) {
			if (data.getUploadTask().isUploading()) {
				btnAction.setText(R.string.mv_pause_upload);
				btnAction.setBackgroundResource(R.drawable.mv_upload_bg);
			}
			else {
				btnAction.setText(R.string.mv_upload);
				btnAction.setBackgroundResource(R.drawable.mv_upload_bg);
			}
			player.setVideo(data.getPath(), data.getPrefix().getCoverFile().getAbsolutePath());
			player.setVisibility(View.VISIBLE);
			player.setIconVisibility(View.GONE);
			viewProgress.setIndeterminate(false);
			viewProgress.setMax(data.getUploadTask().getTotal() + 1);
			viewProgress.setProgress(data.getUploadTask().getProgress() + 1);
			viewProgress.setVisibility(View.VISIBLE);
		}
		else if (type == MvInfo.TYPE_COMPOSE) {
			player.setVisibility(View.GONE);
			viewProgress.setVisibility(View.VISIBLE);
			viewProgress.setIndeterminate(true);
		}
		else {
			player.setVideo(data.getPath(), data.getPrefix().getCoverFile().getAbsolutePath());
			player.setVisibility(View.VISIBLE);
			player.setIconVisibility(player.isPlaying() ? View.GONE : View.VISIBLE);
			viewProgress.setVisibility(View.GONE);

			if (type == MvInfo.TYPE_UNPUBLISHED) {
				btnAction.setText(R.string.mv_upload);
				btnAction.setBackgroundResource(R.drawable.mv_upload_bg);
			}
			else if (type == MvInfo.TYPE_PUBLISHED) {
				btnAction.setText(R.string.mv_share);
//				btnAction.setBackgroundResource(R.drawable.mv_share_bg);
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.add) {
			if (MvComposeService.running) {
				Toast.makeText(getContext(), R.string.mv_compose_err_refuse_action, Toast.LENGTH_SHORT).show();
				return;
			}
			Calendar createAt = Calendar.getInstance();
			createAt.set(data.getCreateAt().get(Calendar.YEAR), data.getCreateAt().get(Calendar.MONTH), data.getCreateAt().get(Calendar.DAY_OF_MONTH));
			createAt.set(Calendar.SECOND, 0);
			createAt.set(Calendar.MILLISECOND, 0);
			controller.make(createAt);
		}
		else if (id == R.id.action) {
			int type = data.getType();
			if (type == MvInfo.TYPE_UNPUBLISHED || (type == MvInfo.TYPE_UPLOAD && !data.getUploadTask().isUploading())) {
				//上传视频
				if (NetworkType.NONE.equals(App.getNetworkType())) {
					Toast.makeText(getContext(), R.string.err_network_invalid, Toast.LENGTH_SHORT).show();
				}
				else if (type == MvInfo.TYPE_COMPOSE) {
					Toast.makeText(getContext(), R.string.mv_compose_err_refuse_action, Toast.LENGTH_SHORT).show();
				}
				else {
					data.getUploadTask().start();

					btnAction.setText(R.string.mv_pause_upload);
					btnAction.setBackgroundResource(R.drawable.mv_upload_bg);
				}
			}
			else if (type == MvInfo.TYPE_UPLOAD && data.getUploadTask().isUploading()) {
				data.getUploadTask().stop();

				btnAction.setText(R.string.mv_upload);
				btnAction.setBackgroundResource(R.drawable.mv_upload_bg);
			}
			else if (type == MvInfo.TYPE_PUBLISHED) {
				//分享视频
				if (NetworkType.NONE.equals(App.getNetworkType())) {
					Toast.makeText(getContext(), R.string.err_network_invalid, Toast.LENGTH_SHORT).show();
				}
				else {
					if (getContext() instanceof FloatViewHostController) {
						((FloatViewHostController) getContext()).showFloatView(viewShare);
						viewShare.setShareData(data);
					}
				}
			}
		}
	}

	public Button getAddButton() {
		return btnAdd;
	}

	public Button getActionButton() {
		return btnAction;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean intercept = data.getType() == MvInfo.TYPE_UPLOAD;
		return intercept;
	}
}

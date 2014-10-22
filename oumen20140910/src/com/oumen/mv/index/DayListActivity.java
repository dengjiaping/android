package com.oumen.mv.index;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.auth.ShareView;
import com.oumen.home.FloatViewController;
import com.oumen.home.FloatViewHostController;
import com.oumen.home.SoftKeyboardController;
import com.oumen.mv.ComposeFragment;
import com.oumen.mv.ComposeSelectDialog;
import com.oumen.mv.MV;
import com.oumen.mv.MvActivity;
import com.oumen.mv.MvComposeService;
import com.oumen.mv.MvInfo;
import com.oumen.tools.ELog;

public class DayListActivity extends BaseActivity implements UploadTask.UploadListener, View.OnClickListener, FloatViewHostController, View.OnTouchListener {
	public static Day day;
	
	private LinearLayout list;
	private ComposeSelectDialog dialogComposeSelect;
	
	private View popupLayer;
	private RelativeLayout rootContainer;//整个布局
	
	private ShareView viewShare;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mv_day_list);
		
		viewShare = new ShareView(this);
		
		dialogComposeSelect = new ComposeSelectDialog(this);
		dialogComposeSelect.setClickListener(this);
		
		popupLayer = findViewById(R.id.layer);
		rootContainer = (RelativeLayout) findViewById(R.id.root);
		rootContainer.setOnTouchListener(this);
		initAnimation();
		
		DateView viewDate = (DateView) findViewById(R.id.date);
		viewDate.txtDay.setText(App.YYYY_MM_DD_CHINESE_FORMAT.format(day.date.getTime()));
		
		AddPageView addPage = (AddPageView) findViewById(R.id.add_page);
		addPage.txt.setText(null);
		addPage.setOnClickListener(this);
		
		TitleBar titlebar = (TitleBar) findViewById(R.id.titlebar);
		titlebar.getTitle().setText(R.string.mv_open_all);
		titlebar.getRightButton().setVisibility(View.GONE);
		titlebar.getLeftButton().setOnClickListener(this);
		
		list = (LinearLayout) findViewById(R.id.list);
		
		buildList();
		
		registerReceiver(receiver, receiverFilter);
	}
	
	private void buildList() {
		clearList();

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.padding_super);
		for (int i = 0; i < day.list.size(); i++) {
			View item = createItem(i);
			list.addView(item, i + 1, params);
		}
	}
	
	private void clearList() {
		while (list.getChildCount() > 2) {
			list.removeViewAt(1);
		}
	}
	
	private void addItem(int position) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.padding_super);

		View item = createItem(position);
		list.addView(item, position + 1, params);
	}
	
	private View createItem(int position) {
		PagerItem page = new PagerItem(this);
		page.setOnClickListener(DayListActivity.this);
		page.setShareView(viewShare);
		page.btnAdd.setVisibility(View.GONE);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) page.btnAction.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		page.btnAction.setLayoutParams(params);
		
		int paddingHorizontal = getResources().getDimensionPixelSize(R.dimen.padding_super),
			paddingVertical = getResources().getDimensionPixelSize(R.dimen.padding_medium);
		
		FrameLayout item = new FrameLayout(this);
		item.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
		item.setBackgroundColor(Color.WHITE);
		item.addView(page);

		MvInfo itemData = day.list.get(position);
		itemData.getUploadTask().setListener(this);
		page.update(itemData);
		return item;
	}
	
	private void updateList() {
		ELog.i("");
		if (day != null) {
			int count = day.list.size();
			for (int i = 0; i < count; i++) {
				ViewGroup wrapper = (ViewGroup) list.getChildAt(i + 1);
				PagerItem item = (PagerItem) wrapper.getChildAt(0);
				item.update(day.list.get(i));
			}
		}
	}

	@Override
	protected void onDestroy() {
		day = null;
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	public void make(Calendar createAt) {
		if (!dialogComposeSelect.isShowing()) {
			dialogComposeSelect.show();
		}
		dialogComposeSelect.setCreateAt(createAt);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		ELog.i("Request:" + requestCode + " Result:" + resultCode);
		if (requestCode == MvActivity.REQUEST_CODE_MV_ACTIVITY) {
			if (resultCode == Activity.RESULT_OK) {
				List<MvInfo> results = MvInfo.query(App.PREFS.getUid(), day.date, App.DB);
				for (int i = 0; i < results.size(); i++) {
					MvInfo info = results.get(i);
					if (!day.list.contains(info)) {
						ELog.w("Insert:" + info.getTitle());
						info.getUploadTask().setListener(this);
						day.list.add(i, info);
						addItem(i);
					}
				}
			}
		}
		
		if (viewShare != null) {
			viewShare.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.left) {
			finish();
		}
		else if (id == R.id.add_page) {
			Calendar createAt = Calendar.getInstance();
			createAt.set(day.date.get(Calendar.YEAR), day.date.get(Calendar.MONTH), day.date.get(Calendar.DAY_OF_MONTH));
			createAt.set(Calendar.SECOND, 0);
			createAt.set(Calendar.MILLISECOND, 0);
			make(createAt);
		}
		else if (id == R.id.image) {
			Intent intent = new Intent(v.getContext(), MvActivity.class);
			intent.putExtra(MvActivity.PARAM_CREATE_AT, dialogComposeSelect.getCreateAt());
			intent.putExtra(ComposeFragment.PARAM_KEY_COMPOSE_TYPE, MV.ACTION_COMPOSE_WITH_IMAGES);
			startActivityForResult(intent, MvActivity.REQUEST_CODE_MV_ACTIVITY);
			dialogComposeSelect.dismiss();
		}
		else if (id == R.id.record) {
			Intent intent = new Intent(v.getContext(), MvActivity.class);
			intent.putExtra(MvActivity.PARAM_CREATE_AT, dialogComposeSelect.getCreateAt());
			intent.putExtra(ComposeFragment.PARAM_KEY_COMPOSE_TYPE, MV.ACTION_COMPOSE_WITH_RECORD);
			startActivityForResult(intent, MvActivity.REQUEST_CODE_MV_ACTIVITY);
			dialogComposeSelect.dismiss();
		}
		else if (id == R.id.local) {
			Intent intent = new Intent(v.getContext(), MvActivity.class);
			intent.putExtra(MvActivity.PARAM_CREATE_AT, dialogComposeSelect.getCreateAt());
			intent.putExtra(ComposeFragment.PARAM_KEY_COMPOSE_TYPE, MV.ACTION_COMPOSE_WITH_EDIT);
			startActivityForResult(intent, MvActivity.REQUEST_CODE_MV_ACTIVITY);
			dialogComposeSelect.dismiss();
		}
	}
	
	private final IntentFilter receiverFilter = new IntentFilter(MvComposeService.MV_COMPOSE_SERVICE_NOTIFY_ACTION);

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MvComposeService.MV_COMPOSE_SERVICE_NOTIFY_ACTION)) {
				int type = intent.getIntExtra(MvComposeService.INTENT_KEY_NOTIFY_TYPE, -1);
				int max = intent.getIntExtra(MvComposeService.INTENT_KEY_NOTIFY_MAX, App.INT_UNSET);
				int current = intent.getIntExtra(MvComposeService.INTENT_KEY_NOTIFY_CURRENT, App.INT_UNSET);

				ELog.i("Notify:" + type + " Progress:" + current + "/" + max);

				if (type == MvComposeService.NOTIFY_COMPLETED) {
					App.ring(context);
					updateList();
				}
			}
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		updateList();
		return false;
	}

	//---------------- UploadTask ----------------//
	@Override
	public void onPrepare(UploadTask task) {
		ELog.i("Name:" + task.getInfo().getTitle() + " Progress:" + task.getProgress() + "/" + task.getTotal());
		handler.sendEmptyMessage(0);
	}

	@Override
	public void onProgressUpdate(UploadTask task) {
		ELog.i("Name:" + task.getInfo().getTitle() + " Progress:" + task.getProgress() + "/" + task.getTotal());
		handler.sendEmptyMessage(0);
	}

	@Override
	public void onCancel(UploadTask task) {
		ELog.i("Name:" + task.getInfo().getTitle() + " Progress:" + task.getProgress() + "/" + task.getTotal());
		handler.sendEmptyMessage(0);
	}

	@Override
	public void onFailed(UploadTask task) {
		ELog.i("Name:" + task.getInfo().getTitle() + " Progress:" + task.getProgress() + "/" + task.getTotal());
		handler.sendEmptyMessage(0);
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	//----------------------- Animation -----------------------//
	private FloatViewController floatViewController;
	private Animation animBottomIn;
	private Animation animBottomOut;

	private void initAnimation() {
		animBottomIn = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_in);
		animBottomIn.setAnimationListener(animListener);
		animBottomOut = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_out);
		animBottomOut.setAnimationListener(animListener);
	}

	private final Animation.AnimationListener animListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			floatViewController.setPlaying(false);
			if (animation == animBottomOut) {
				rootContainer.removeView(floatViewController.getRoot());
				floatViewController = null;

				popupLayer.setVisibility(View.GONE);
				popupLayer.setOnTouchListener(null);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	};

	//----------------------- FloatViewHostController -----------------------//
	@Override
	public boolean isFloatViewShowing() {
		return floatViewController != null && (floatViewController.isPlaying() || floatViewController.isShowing());
	}

	@Override
	public void showFloatView(FloatViewController controller) {
		if (isFloatViewShowing())
			return;

		floatViewController = controller;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		View container = floatViewController.show();
		rootContainer.addView(container, params);
		container.startAnimation(animBottomIn);

		if (controller instanceof SoftKeyboardController) {
			SoftKeyboardController kb = (SoftKeyboardController) controller;
			kb.showSoftKeyboard();
		}

		popupLayer.setVisibility(View.VISIBLE);
		popupLayer.setOnTouchListener(this);
	}

	@Override
	public void hideFloatView() {
		if (!isFloatViewShowing())
			return;

		View container = floatViewController.hide();
		container.startAnimation(animBottomOut);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == popupLayer && event.getAction() == MotionEvent.ACTION_UP) {
			if (floatViewController != null && !floatViewController.isPlaying() && floatViewController.isShowing()) {
				hideFloatView();
			}
		}
		return true;
	}
}

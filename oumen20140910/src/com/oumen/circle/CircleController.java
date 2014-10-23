package com.oumen.circle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.peers.CommentItem;
import com.oumen.android.peers.entity.CircleUserMsg;
import com.oumen.android.util.Constants;
import com.oumen.circle.CircleHttpController.CircleDataWrapper;
import com.oumen.circle.CircleHttpController.ObtainType;
import com.oumen.home.LoginConfrim;
import com.oumen.peer.OumenCircleNoticeListActivity;
import com.oumen.tools.ELog;
import com.oumen.user.UserInfoActivity;
import com.oumen.widget.dialog.PickImageDialog;
import com.oumen.widget.list.HSZListViewAdapter;

public class CircleController implements Handler.Callback, View.OnClickListener, OnScrollListener {
	protected final AdapterImpl adapter = new AdapterImpl();

	protected final Handler handler = new Handler(this);
	protected final CircleHttpController httpController = new CircleHttpController(handler);

	protected CircleListFragment host;

	protected final int selfUid;

	private LoginConfrim loginConfrim;
	
	private boolean firstFlag = true;

	CircleController(CircleListFragment host) {
		this.host = host;

		selfUid = App.PREFS.getUid();
		loginConfrim = new LoginConfrim(host.getActivity());
	}

	void onViewCreated(View view, Bundle savedInstanceState) {
		if (firstFlag) {
			host.showProgressDialog();
			firstFlag = false;
		}
		host.lstView.headerLoad();
	}

	// TODO onActivityResult
	void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (dialogPickImages != null) {
				String tempPath = dialogPickImages.onActivityResult(requestCode, resultCode, data);
				ELog.i(tempPath);
				if (tempPath == null && requestCode == Constants.REQUEST_CODE_OPEN_CAMERA) {
					try {
//						ClipBackground();
//						changeBackground();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (requestCode == CircleListFragment.REQUEST_SHARE) {// 发表成功了偶们圈的内容
				if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
					// TODO 偶们圈发布成功Callback
					httpController.obtainList(null, adapter);
				}
				else {
					Toast.makeText(host.getActivity(), "网络不给力~，请检查网络是否连接", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public boolean onBackPressed() {
		return false;
	}

	public class CircleListAdapter extends HSZListViewAdapter<CircleUserMsg> implements View.OnClickListener {

		@Override
		synchronized public View getView(int position, View convertView, ViewGroup parent) {
			CircleItem item = null;
			if (convertView == null) {
				CircleItemData itemData = new CircleItemData();
				item = new CircleItem(parent.getContext());
				item.setTag(itemData);
				item.setButtonClickListener(this);
				itemData.groupIndex = position;
				itemData.groupData = get(position);
			}
			else {
				item = (CircleItem) convertView;
				CircleItemData itemData = (CircleItemData) item.getTag();
				itemData.groupIndex = position;
				itemData.groupData = get(position);
			}
			long time = System.currentTimeMillis();
			item.update(scrollFlag);
			ELog.e("加载一条item需要时间：" + (System.currentTimeMillis() - time));
			return item;
		}

		@Override
		public void onClick(View v) {
		}
	}

	private class AdapterImpl extends CircleListAdapter {

		@Override
		public void onClick(View v) {
			// TODO Item Click
			CircleItemData data = (CircleItemData) v.getTag();

			if (v instanceof CommentItem) {
				ELog.i("Comment");
				onCommentItemClick(data);
			}
			else {
				int id = v.getId();
				switch (id) {
					case R.id.share:
						ELog.i("Share");
						host.showShareView();
						host.viewShare.setShareData(data.groupData);
						break;

					case R.id.comment:
						ELog.i("Comment");
						host.viewSend.setData(data);
						host.viewSend.setInputHint("回复：" + data.groupData.getInfo().getNickname());
						host.showSendView();
						break;

					case R.id.enjoy:
						ELog.i("Enjoy");
						if (data.groupData.getIsprise() == 1)
							httpController.unenjoy(data);
						else
							httpController.enjoy(data);
						break;

					case R.id.photo:
						ELog.i("Photo");
						if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
							//TODO 跳转到登录界面
							loginConfrim.openDialog();
							return;
						}
						// TODO　跳转到个人主页
						Intent intent = new Intent(host.getActivity(), UserInfoActivity.class);
						intent.putExtra(UserInfoActivity.INTENT_KEY_UID, Integer.valueOf(data.groupData.getInfo().getUid()));
						host.startActivity(intent);
						break;

					case R.id.delete:
						ELog.i("Delete");
						host.showDeleteContentDialog(data);
						break;
				}
			}
		}

		@Override
		public void onHeaderLoad() {
			ELog.i("");
			isLoading = true;
			host.headerView.setProgressVisibility(View.VISIBLE);
			if (adapter.isEmpty()) {
//				host.lstView.setLoadingViewVisibility(View.VISIBLE);
				host.lstView.setEmptyViewVisibility(View.GONE);
			}

			httpController.obtainList(null, adapter);
		}

		@Override
		public void onFooterLoad() {
			ELog.i("");
			isLoading = true;
			host.footerView.setState(HSZListViewAdapter.STATE_LOADING);
			httpController.obtainList(ObtainType.FOOTER, adapter);
		}
	}

	private void onCommentItemClick(CircleItemData data) {
		if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
			//TODO 跳转到登录界面
			loginConfrim.openDialog();
			return;
		}
		if (selfUid == data.commentData.getAuthorId()) {
			// 显示删除
			host.showDeleteCommentDialog(data);
		}
		else {
			ELog.i("Comment");
			host.viewSend.setData(data);
			host.showSendView();
		}
	}

	// TODO Pick Images
	private PickImageDialog dialogPickImages;

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.send:
				if (TextUtils.isEmpty(host.viewSend.getText().trim())) {
					Toast.makeText(host.getActivity(), "请输入评论内容", Toast.LENGTH_SHORT).show();
					return;
				}
				CircleItemData data = (CircleItemData) host.viewSend.getData();
				httpController.sendComment(data, host.viewSend.getText().trim());

				host.viewSend.clear();
				host.hideFloatView();
				break;

			case R.id.btn_left:
				// TODO 删除评论或者偶们圈内容
				if (host.dialogDeleteComment != null) {//删除评论
					data = (CircleItemData) host.dialogDeleteComment.getTag();
					httpController.deleteComment(data);
					host.hideDeleteCommentDialog();
				}
				else if (host.dialogDeleteContent != null) {//删除偶们圈
					data = (CircleItemData) host.dialogDeleteContent.getTag();
					httpController.deleteContent(data, adapter);
					host.hideDeleteContentDialog();
				}
				break;

			case R.id.btn_right:
				if (host.dialogDeleteComment != null) {//删除评论
					host.hideDeleteCommentDialog();
				}
				else if (host.dialogDeleteContent != null) {//删除偶们圈
					host.hideDeleteContentDialog();
				}
				break;

			// TODO Header listener
			case R.id.user_header:

				break;

			case R.id.photo:
				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					//TODO 跳转到登录界面
					loginConfrim.openDialog();
					return;
				}
				// TODO 跳转到个人主页
				Intent intent = new Intent(host.getActivity(), UserInfoActivity.class);
				intent.putExtra(UserInfoActivity.INTENT_KEY_UID, App.PREFS.getUid());
				host.startActivity(intent);
				break;

			case R.id.banner_message:
				intent = new Intent(host.getActivity(), OumenCircleNoticeListActivity.class);
				host.getActivity().startActivityForResult(intent, CircleListFragment.REQURST_NOTICE_MESSAGE);
				break;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case CircleHttpController.HANDLER_OBTAIN_LIST:
				isLoading = false;
				synchronized (adapter) {
					if (host.footerView.getState() == HSZListViewAdapter.STATE_LOADING) {
						host.footerView.setState(HSZListViewAdapter.STATE_NORMAL);
					}

					if (msg.obj instanceof CircleDataWrapper) {
						CircleDataWrapper wrapper = (CircleDataWrapper) msg.obj;

						if (ObtainType.FOOTER.equals(wrapper.obtainType)) {
							adapter.addAll(wrapper.data);
						}
						else {
							adapter.clear();
							adapter.addAll(wrapper.data);
						}

						if (wrapper.http) {
							host.headerView.setProgressVisibility(View.GONE);
							if (adapter.isEmpty()) {
								host.footerView.setVisibility(View.GONE);
//								host.lstView.setLoadingViewVisibility(View.GONE);
								host.lstView.setEmptyViewVisibility(View.VISIBLE);
							}
							else {
								host.footerView.setVisibility(View.VISIBLE);
//								host.lstView.setLoadingViewVisibility(View.GONE);
								host.lstView.setEmptyViewVisibility(View.GONE);
							}
						}
					}

					if (msg.arg2 != 0) {
						//数据发生改变
						adapter.notifyDataSetChanged();
						handler.postDelayed(new Runnable() {

							@Override
							public void run() {
								ELog.e("Delayed update");
								adapter.notifyDataSetChanged();

								host.lstView.loaded();
							}
						}, 100);
					}

					if (msg.arg1 != 0) {
						Toast.makeText(host.getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();

						host.headerView.setProgressVisibility(View.GONE);
						if (adapter.isEmpty()) {
							host.footerView.setVisibility(View.GONE);
//							host.lstView.setLoadingViewVisibility(View.GONE);
							host.lstView.setEmptyViewVisibility(View.VISIBLE);
						}
						else {
							host.footerView.setVisibility(View.VISIBLE);
//							host.lstView.setLoadingViewVisibility(View.GONE);
							host.lstView.setEmptyViewVisibility(View.GONE);
						}
					}
				}
				host.dismissProgressDialog();
				break;

			case CircleHttpController.HANDLER_DELETE_COMMENT:
				synchronized (adapter) {
					if (msg.arg1 == 0 && msg.obj == null) {
						adapter.notifyDataSetChanged();
					}
					else if (msg.arg1 != 0) {
						Toast.makeText(host.getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
					}
					else if (msg.obj != null && msg.obj instanceof String) {
						Toast.makeText(host.getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case CircleHttpController.HANDLER_DELETE_CONTENT:
				synchronized (adapter) {
					if (msg.arg1 == 0 && msg.obj == null) {
						adapter.notifyDataSetChanged();
					}
					else if (msg.arg1 != 0) {
						Toast.makeText(host.getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
					}
					else if (msg.obj != null && msg.obj instanceof String) {
						Toast.makeText(host.getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case CircleHttpController.HANDLER_SEND_CONTENT:
				synchronized (adapter) {
					if (msg.arg1 == 0 && msg.obj == null) {
						adapter.notifyDataSetChanged();
					}
					else if (msg.arg1 != 0) {
						Toast.makeText(host.getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
					}
					else if (msg.obj != null && msg.obj instanceof String) {
						Toast.makeText(host.getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case CircleHttpController.HANDLER_SEND_COMMENT:
				synchronized (adapter) {
					if (msg.arg1 == 0 && msg.obj == null) {
						adapter.notifyDataSetChanged();
					}
					else if (msg.arg1 != 0) {
						Toast.makeText(host.getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
					}
					else if (msg.obj != null && msg.obj instanceof String) {
						Toast.makeText(host.getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case CircleHttpController.HANDLER_ENJOY:
				synchronized (adapter) {
					if (msg.arg1 == 0 && msg.obj == null) {
						adapter.notifyDataSetChanged();
					}
					else if (msg.arg1 != 0) {
						Toast.makeText(host.getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
					}
					else if (msg.obj != null && msg.obj instanceof String) {
						Toast.makeText(host.getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case CircleHttpController.HANDLER_UNENJOY:
				synchronized (adapter) {
					if (msg.arg1 == 0 && msg.obj == null) {
						adapter.notifyDataSetChanged();
					}
					else if (msg.arg1 != 0) {
						Toast.makeText(host.getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
					}
					else if (msg.obj != null && msg.obj instanceof String) {
						Toast.makeText(host.getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case App.HANDLER_TOAST:
				if (msg.arg1 != 0) {
					Toast.makeText(host.getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
				}
				else if (msg.obj != null && msg.obj instanceof String) {
					Toast.makeText(host.getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
				}
				break;
		}
		return false;
	}

	private boolean isLoading = false;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem + visibleItemCount == totalItemCount && !isLoading) {
			isLoading = true;// == true 表示正在加载，加载完毕设置为false
			if (firstVisibleItem <= adapter.getCount() - 2) {
				handler.postDelayed(new Runnable() {

					public void run() {
						ELog.e("后台开始加载数据");
						host.lstView.footerLoad();
					}
				}, 500);
			}
		}
	}

	private boolean scrollFlag = false;

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_FLING:
				ELog.i("pause");
				scrollFlag = true;
				ImageLoader.getInstance().pause();
				break;
			case OnScrollListener.SCROLL_STATE_IDLE:
				ELog.i("resume");
				scrollFlag = false;
				ImageLoader.getInstance().resume();
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				scrollFlag = true;
				ELog.i("pause");
				ImageLoader.getInstance().pause();
				break;
			default:
				break;
		}
	}
}

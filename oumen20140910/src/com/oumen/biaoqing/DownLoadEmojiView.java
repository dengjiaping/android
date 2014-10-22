package com.oumen.biaoqing;

import com.oumen.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * 默认下载表情界面
 */
public class DownLoadEmojiView extends FrameLayout {

	public static enum BIAOQING_TYPE {
		OUBA, CIWEI
	};

	private ImageView background;//背景
	private Button btnDownLoad;//下载表情按钮
	private ProgressBar downLoadProgress;//下载进度

	public static final String DOWNLOAD = "下载表情";
	public static final String DOWNLOADING = "正在下载";
	public static final String DOWN_PAUSE = "暂停";

	private BIAOQING_TYPE type;

	public DownLoadEmojiView(Context context) {
		this(context, null, 0);
	}

	public DownLoadEmojiView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public DownLoadEmojiView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.default_baioqing_layout, this, true);
		background = (ImageView) findViewById(R.id.background);
		btnDownLoad = (Button) findViewById(R.id.download);
		downLoadProgress = (ProgressBar) findViewById(R.id.progress);
		btnDownLoad.setText(DOWNLOAD);
//		btnDownLoad.setOnClickListener(clickListener);

	}
	
	public void setDownLoadListener(OnClickListener clickListener) {
		btnDownLoad.setClickable(true);
		btnDownLoad.setOnClickListener(clickListener);
	}

	/**
	 * 设置背景图
	 * 
	 * @param res
	 */
	public void setImageBackground(int res) {
		background.setImageResource(res);
	}

	public void setType(BIAOQING_TYPE type) {
		this.type = type;
	}

	public BIAOQING_TYPE getType() {
		return type;
	}

	public void setText(String text) {
		btnDownLoad.setText(text);
	}

	public String getText() {
		return btnDownLoad.getText().toString();
	}

	public void setDownLoadProgress(int progress, int max) {
		downLoadProgress.setMax(max);
		downLoadProgress.setProgress(progress);
	}
	
	public void setButtonUnClick() {
		btnDownLoad.setClickable(false);
	}
}

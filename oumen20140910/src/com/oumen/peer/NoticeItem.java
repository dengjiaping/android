package com.oumen.peer;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.message.CircleMessage;
import com.oumen.message.Type;
import com.oumen.peer.OumenCircleNoticeListActivity.ItemData;
import com.oumen.tools.ImageTools;

public class NoticeItem extends LinearLayout {
	private ImageView ivHeadPhoto;// 评论人的头像
	private ImageView ivPic;// 评论的偶们圈内容的图片
	private TextView tvTargetContent;
	private TextView tvNickName, tvContent, tvTime;// 评论人的昵称，评论的内容，评论时间
	private CircleMessage itemsdata;
	
	private int biaoqingIconSize = App.INT_UNSET;
	private final BitmapProcessor preBitmapProcessor = new BitmapProcessor() {

		@Override
		public Bitmap process(Bitmap bitmap) {
			Bitmap img = ImageTools.clip2square(bitmap);
			return img;
		}
	};

	public final DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.pic_default)
		.showImageOnFail(R.drawable.pic_default)
		.preProcessor(preBitmapProcessor)
		.cacheOnDisk(true)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
	
	public NoticeItem(Context context) {
		this(context, null, 0);
	}

	public NoticeItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NoticeItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.oumen_notices_list_item, this, true);

		ivHeadPhoto = (ImageView) findViewById(R.id.notice_userhead);
		ivPic = (ImageView) findViewById(R.id.notice_pic);
		tvTargetContent = (TextView) findViewById(R.id.notice_txt);

		tvNickName = (TextView) findViewById(R.id.tv_notice_nickname);
		tvContent = (TextView) findViewById(R.id.tv_notice_content);
		tvTime = (TextView) findViewById(R.id.tv_notice_time);
		
		biaoqingIconSize = (int) (15 * context.getResources().getDisplayMetrics().scaledDensity) + 2;
	}

	void update() {
		ItemData itemData = (ItemData) getTag();
		CircleMessage itemdata = itemData.noticeData;
		this.itemsdata = itemdata;
		String photopath = itemsdata.getTargetPhotoSourceUrl();
		if (!TextUtils.isEmpty(photopath)) {
			ImageLoader.getInstance().displayImage(photopath, ivHeadPhoto, options);
		}
		else {
			ivHeadPhoto.setImageResource(R.drawable.round_user_photo);
		}

		String picpath = itemsdata.getCirclePic();
		if (!TextUtils.isEmpty(picpath)) {
			ivPic.setVisibility(View.VISIBLE);
			tvTargetContent.setVisibility(View.GONE);
			ImageLoader.getInstance().displayImage(picpath, ivPic, options);
		}
		else {
//			ivPic.setImageResource(R.drawable.pic_default);
			ivPic.setVisibility(View.GONE);
			tvTargetContent.setVisibility(View.VISIBLE);
			tvTargetContent.setText(itemdata.getCircleTitle());
		}
		tvNickName.setText(itemsdata.getTargetNickname());
		if (itemsdata.getType() == Type.COMMENT) {
			SpannableStringBuilder builder = new SpannableStringBuilder(itemsdata.getContent());
			builder = App.SMALLBIAOQING.convert(getContext(), builder, biaoqingIconSize);
			tvContent.setText(builder);
		}
		else if (itemsdata.getType() == Type.ENJOY) {
			tvContent.setText("给您点了个赞");
		}
		tvTime.setText(App.MM_DD_HH_MM_FORMAT.format(itemsdata.getDatetime()));
	}
}

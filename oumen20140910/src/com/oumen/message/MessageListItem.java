package com.oumen.message;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.tools.ImageTools;

@SuppressWarnings("deprecation")
public class MessageListItem extends RelativeLayout {
	private ImageView imgIcon;
	private TextView txtCount;
	private TextView txtTitle;
	private TextView txtDescription;
	private ImageView imgButton;
	
	protected MessageListItemDataProvider provider; 

	public MessageListItem(Context context) {
		this(context, null, 0);
	}

	public MessageListItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MessageListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.message_list_item, this, true);
		
		imgIcon = (ImageView) findViewById(R.id.icon);
		txtCount = (TextView) findViewById(R.id.count);
		txtTitle = (TextView) findViewById(R.id.nav_title);
		txtDescription = (TextView) findViewById(R.id.description);
		imgButton = (ImageView) findViewById(R.id.button);
	}

	public void setIconClickListener(View.OnClickListener clickListener) {
		imgIcon.setOnClickListener(clickListener);
	}
	
	public void setButtonClickListener(View.OnClickListener clickListener) {
		imgButton.setOnClickListener(clickListener);
	}
	
	public void update(MessageListItemDataProvider provider) {
		this.provider = provider;
		
		int resId = provider.getIconResId();
		if (resId == App.INT_UNSET) {
			String path = provider.getIconPath();
			if (TextUtils.isEmpty(path)) {
				imgIcon.setImageResource(R.drawable.round_user_photo);
			}
			else {
				if (path.startsWith(App.SCHEMA_HTTP) || path.startsWith(App.SCHEMA_HTTPS)) {
					path += path.indexOf('?') == -1 ? "?circle=1" : "&circle=1";
				}
				else {
					path = App.SCHEMA_FILE + path;
				}
				File cache = ImageLoader.getInstance().getDiskCache().get(path);
				if (cache != null && cache.exists()) {
					Bitmap img = ImageTools.clip2square(BitmapFactory.decodeFile(cache.getAbsolutePath()));
					img = ImageTools.toOvalBitmap(img);
					imgIcon.setImageBitmap(img);
				}
				else {
					ImageLoader.getInstance().displayImage(path, imgIcon, App.OPTIONS_HEAD_ROUND, App.CIRCLE_IMAGE_LOADING_LISTENER);
				}
			}
		}
		else {
			imgIcon.setImageResource(resId);
		}
		
		int count = provider.getNewCount();
		if (count == 0) {
			txtCount.setVisibility(View.GONE);
		}
		else {
			txtCount.setText(count <= 99 ? String.valueOf(count) : "99+");
			txtCount.setVisibility(View.VISIBLE);
		}
		
		resId = provider.getTitleRightIconResId();
		txtTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, resId == App.INT_UNSET ? 0 : resId, 0);
		txtTitle.setText(provider.getTitle());
		
		txtDescription.setText(provider.getDescription());
		
		resId = provider.getButtonIconResId();
		if (resId == App.INT_UNSET) {
			imgButton.setImageBitmap(null);
			imgButton.setVisibility(View.GONE);
		}
		else {
			imgButton.setImageResource(resId);
			imgButton.setVisibility(View.VISIBLE);
		}
	}
	
	public static MessageListItemDataProvider getDataProviderByChild(View child) {
		ViewParent parent = child.getParent();
		while (!(parent instanceof MessageListItem)) {
			parent = parent.getParent();
			if (parent == null)
				return null;
			else if (parent instanceof MessageListItem)
				break;
		}
		MessageListItem item = (MessageListItem)parent;
		return item.provider;
	}
}

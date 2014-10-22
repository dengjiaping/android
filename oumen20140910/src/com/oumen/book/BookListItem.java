package com.oumen.book;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.android.App;

public class BookListItem extends LinearLayout {
	private TextView txtTime;
	private TextView txtTitle1;
	private TextView txtTitle;
	private TextView txtContent;

	private TextView txtYear;
	private TextView txtMonth;
	private TextView txtDay;

	protected BookMessage data;

	public BookListItem(Context context) {
		this(context, null, 0);
	}

	public BookListItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BookListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.book_list_item, this, true);

		txtTime = (TextView) findViewById(R.id.time);
		txtTitle1 = (TextView) findViewById(R.id.title1);
		txtTitle = (TextView) findViewById(R.id.nav_title);
		txtContent = (TextView) findViewById(R.id.content);

		txtYear = (TextView) findViewById(R.id.year);
		txtMonth = (TextView) findViewById(R.id.month);
		txtDay = (TextView) findViewById(R.id.day);
	}

	public void update(BookMessage data) {
		this.data = data;

		if (data.babyType == BookMessage.BABY_TYPE_BORN) {
			txtTitle1.setText(R.string.book_title1);
		}
		else {
			txtTitle1.setText(R.string.book_title2);
		}
		String[] tmp = data.days.split("\\|");
		txtYear.setText(tmp[0] + " ");
		txtMonth.setText(" " + tmp[1] + " ");
		txtDay.setText(" " + tmp[2] + " ");

		txtTime.setText(App.YYYY_MM_DD_FORMAT.format(data.createAt));
		txtTitle.setText(data.title);
		txtContent.setText(data.content);
	}
	
	public void setTimeVisibility(int visibility) {
		txtTime.setVisibility(visibility);
	}
}

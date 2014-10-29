package com.oumen.activity.main;

import java.util.List;

import widget.viewpager.CirclePageIndicator;

import com.oumen.R;
import com.oumen.activity.message.ActivityTag;
import com.oumen.activity.message.BaseActivityMessage;
import com.oumen.activity.widget.IndexViewPager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivityMainListHeader extends RelativeLayout {
	//轮播图
	IndexViewPager imageSwitchView;
	
	HuodongTagViewPager tagView;
	CirclePageIndicator tagIndicator;

	// 专家推荐
	TextView professorTag;
	LinearLayout container1, container2;
	ProfessorItem professorItem1, professorItem2, professorItem3, professorItem4;

	public ActivityMainListHeader(Context context) {
		this(context, null, 0);
	}

	public ActivityMainListHeader(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActivityMainListHeader(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.activity_main_header_view, this, true);

		imageSwitchView = (IndexViewPager) findViewById(R.id.image_switch);
		imageSwitchView.setPagerViewLayoutParams();

		tagView = (HuodongTagViewPager) findViewById(R.id.tag_viewpager);
		
		professorTag = (TextView) findViewById(R.id.professer_flag);
		container1 = (LinearLayout) findViewById(R.id.linearlayout);
		container2 = (LinearLayout) findViewById(R.id.linearlayout1);
		
		professorItem1 = (ProfessorItem) findViewById(R.id.professor1);
		professorItem2 = (ProfessorItem) findViewById(R.id.professor2);
		professorItem3 = (ProfessorItem) findViewById(R.id.professor3);
		professorItem4 = (ProfessorItem) findViewById(R.id.professor4);
		
		professorItem1.setOnClickListener(clickListener);
		professorItem2.setOnClickListener(clickListener);
		professorItem3.setOnClickListener(clickListener);
		professorItem4.setOnClickListener(clickListener);
	}
	
	public IndexViewPager getImageSwitchView() {
		return imageSwitchView;
	}
	
	public HuodongTagViewPager getTagView() {
		return tagView;
	}
	
	public void updateProfessor(List<BaseActivityMessage> data) {
		professorItem1.update(data.get(0));
		professorItem2.update(data.get(1));
		professorItem3.update(data.get(2));
		professorItem4.update(data.get(3));
		
		professorItem1.setTag(data.get(0));
		professorItem2.setTag(data.get(1));
		professorItem3.setTag(data.get(2));
		professorItem4.setTag(data.get(3));
	}
	
	private final OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v instanceof ProfessorItem) {
				BaseActivityMessage data = (BaseActivityMessage) v.getTag();
			}
		}
	};
}

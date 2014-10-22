package com.oumen.book;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.tools.ELog;

public class BookActivity extends FragmentActivity {

	private TitleBar titlebar;
	private TextView txtTitle;
	private Button btnLeft;
	
	private BookListFragment fragList;
	private BookDetailFragment fragWeb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book);

		titlebar = (TitleBar) findViewById(R.id.titlebar);
		titlebar.getRightButton().setVisibility(View.GONE);
		txtTitle = titlebar.getTitle();

		btnLeft = titlebar.getLeftButton();
		btnLeft.setOnClickListener(clickListener);
		
		switchFragment(BookListFragment.class.getName(), null);
	}
	
	public void switchFragment(String fragment, Bundle args) {
		if (BookDetailFragment.class.getName().equals(fragment)) {
			txtTitle.setText("内容详情");
			if (fragWeb == null) {
				fragWeb = new BookDetailFragment();
			}
			fragWeb.setArguments(args);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, fragWeb).addToBackStack(null).commit();
		}
		else {
			txtTitle.setText(R.string.book);
			if (fragList == null) {
				fragList = new BookListFragment();
				fragList.adapter.clickListener = clickListener;
			}
			getSupportFragmentManager().beginTransaction().replace(R.id.container, fragList).commit();
		}
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				Fragment current = getSupportFragmentManager().findFragmentById(R.id.container);
				if (current instanceof BookListFragment) {
					finish();
				}
				else {
					getSupportFragmentManager().popBackStack();
				}
			}
			else if (v instanceof BookListItem) {
				BookListItem item = (BookListItem) v;
				ELog.i(item.data.title);
				
				Bundle args = new Bundle();
				args.putString(BookMessage.KEY_URL, item.data.url);
				switchFragment(BookDetailFragment.class.getName(), args);
			}
		}
	};
}

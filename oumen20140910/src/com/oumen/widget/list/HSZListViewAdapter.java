package com.oumen.widget.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.view.View;
import android.widget.BaseAdapter;

public abstract class HSZListViewAdapter<E> extends BaseAdapter {
	public static final int STATE_NORMAL = 0;
	public static final int STATE_PULL_OVER = 1;
	public static final int STATE_LOADING = 2;
	
	private final List<E> dataSource = new ArrayList<E>();
	
	private boolean dataSetChanged;
	
	synchronized public boolean isDataSetChanged() {
		return dataSetChanged;
	}
	
	synchronized public boolean contains(E object) {
		return dataSource.contains(dataSource);
	}
	
	synchronized public void clear() {
		dataSetChanged = true;
		dataSource.clear();
	}
	
	synchronized public void add(E object) {
		dataSetChanged = true;
		dataSource.add(object);
	}
	
	synchronized public void add(int location, E object) {
		dataSetChanged = true;
		dataSource.add(location, object);
	}
	
	synchronized public void addAll(Collection<? extends E> collection) {
		dataSetChanged = true;
		dataSource.addAll(collection);
	}
	
	synchronized public void addAll(int location, Collection<? extends E> collection) {
		dataSetChanged = true;
		dataSource.addAll(location, collection);
	}
	
	synchronized public boolean remove(E object) {
		dataSetChanged = true;
		return dataSource.remove(object);
	}
	
	synchronized public boolean isEmpty() {
		return dataSource.isEmpty();
	}
	
	synchronized public E get(int location) {
		return dataSource.get(location);
	}
	
	synchronized public int indexOf(Object obj) {
		return dataSource.indexOf(obj);
	}

	@Override
	synchronized public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		dataSetChanged = false;
	}
	
	synchronized public ArrayList<E> copyDataSource() {
		ArrayList<E> tmp = new ArrayList<E>();
		for (int i = 0; i < dataSource.size(); i++) {
			tmp.add(dataSource.get(i));
		}
		return tmp;
	}
	
	@Override
	synchronized public int getCount() {
		return dataSource.size();
	}

	@Override
	synchronized public E getItem(int position) {
		return dataSource.get(position);
	}

	@Override
	synchronized public long getItemId(int position) {
		return position;
	}

	public void onHeaderLoad() {}
	
	public void onFooterLoad() {}
	
	public void onHeaderPull(View headerView, int top) {}
	
	public void onHeaderPullOverLine(View headerView, int top) {}
}

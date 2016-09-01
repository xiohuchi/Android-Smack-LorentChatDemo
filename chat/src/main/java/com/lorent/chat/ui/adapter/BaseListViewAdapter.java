package com.lorent.chat.ui.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import com.lorent.chat.utils.cache.ImageFetcher;
import com.lorent.chat.ui.entity.Entity;

import java.util.List;

public abstract class BaseListViewAdapter extends BaseAdapter {

//	protected ImageFetcher imageFetcher;
	protected Context context;
	protected List<? extends Entity> datas;
	
	public BaseListViewAdapter(Context context,List<? extends Entity> datas){
		this.context = context;
		this.datas = datas;
		initImageFetcher();
	}
	
	protected abstract void initImageFetcher();

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}

package com.emilsjolander.components.StickyListHeaders.deprecated;

import com.emilsjolander.components.StickyListHeaders.R;
import com.emilsjolander.components.StickyListHeaders.StickyListHeadersAdapter;
import com.emilsjolander.components.StickyListHeaders.R.id;
import com.emilsjolander.components.StickyListHeaders.R.styleable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;
/**
 * 
 * @author Emil Sj�lander
 * 
 * 
Copyright 2012 Emil Sj�lander

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 *
 */
@Deprecated
/**
 * use StickyListHeadersListView instead
 */
public class StickyListHeadersListViewWrapper extends FrameLayout implements OnScrollListener {
	
	private static final String HEADER_HEIGHT = "headerHeight";
	private static final String SUPER_INSTANCE_STATE = "superInstanceState";
	
	private boolean areHeadersSticky;
	private int headerBottomPosition;
	private int headerHeight = -1;
	private View header;
	private ListView list;
	private OnScrollListener onScrollListener;
	private Drawable divider;
	private int dividerHeight;

	public StickyListHeadersListViewWrapper(Context context) {
		super(context);
		list = new ListView(context);
		setAreHeadersSticky(true);
		setup();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		headerHeight = ((Bundle)state).getInt(HEADER_HEIGHT);
		super.onRestoreInstanceState(((Bundle)state).getParcelable(SUPER_INSTANCE_STATE));
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle instanceState = new Bundle();
		instanceState.putInt(HEADER_HEIGHT, headerHeight);
		instanceState.putParcelable(SUPER_INSTANCE_STATE, super.onSaveInstanceState());
		return instanceState;
	}

	public StickyListHeadersListViewWrapper(Context context, AttributeSet attrs) {
		super(context,attrs);
		list = new ListView(context, attrs);
		TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.StickyListHeadersListView);
		setAreHeadersSticky(a.getBoolean(0, true));
		a.recycle();
		setup();
	}

	public StickyListHeadersListViewWrapper(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
		list = new ListView(context, attrs, defStyle);
		TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.StickyListHeadersListView);
		setAreHeadersSticky(a.getBoolean(0, true));
		a.recycle();
		setup();
	}

	private void setup() {
		list.setId(R.id.list_view);
		list.setOnScrollListener(this);
		list.setVerticalFadingEdgeEnabled(false);
		list.setClipToPadding(false);
		divider = list.getDivider();
		dividerHeight = list.getDividerHeight();
		list.setDivider(null);
		list.setDividerHeight(0);
		addView(list);
		
		//reset values so they don't interfere with same values on listview
		setPadding(0, 0, 0, 0);
		setBackgroundColor(0x00000000);
	}
	
	public ListView getWrappedList(){
		return list;
	}
	
	public void setWrappedList(ListView list){
		list.setOnScrollListener(this);
		list.setVerticalFadingEdgeEnabled(false);
		list.setClipToPadding(false);
		divider = list.getDivider();
		dividerHeight = list.getDividerHeight();
		list.setDivider(null);
		list.setDividerHeight(0);
		this.list = list;
	}
	
	/**
	 * Use this method instead of getWrappedList().setOnScrollListener(OnScrollListener onScrollListener), using that will break this class.
	 */
	public void setOnScrollListener(OnScrollListener onScrollListener){
		this.onScrollListener = onScrollListener;
	}

	public void setAreHeadersSticky(boolean areHeadersSticky) {
		this.areHeadersSticky = areHeadersSticky;
	}

	public boolean areHeadersSticky() {
		return areHeadersSticky;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if(onScrollListener!=null){
			onScrollListener.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
		}
		if(list.getAdapter()==null) return;
		if(!(list.getAdapter() instanceof StickyListHeadersAdapter)){
			throw new IllegalArgumentException("Adapter must be a subclass of StickyListHeadersAdapter");
		}
		StickyListHeadersAdapter stickyListHeadersAdapter = (StickyListHeadersAdapter) list.getAdapter();
		stickyListHeadersAdapter.setDivider(divider,dividerHeight);
		if(areHeadersSticky){
			if(list.getChildCount()!=0){
				View viewToWatch = list.getChildAt(0);
				if(!((Boolean)viewToWatch.getTag()) && list.getChildCount()>1){
					viewToWatch = list.getChildAt(1);
				}
				if((Boolean)viewToWatch.getTag()){
					if(headerHeight<0) headerHeight=viewToWatch.findViewById(R.id.header_view).getHeight();
					
					if(firstVisibleItem == 0 && viewToWatch.getTop()>0){
						headerBottomPosition = 0;
					}else{
						headerBottomPosition = Math.min(viewToWatch.getTop(), headerHeight);
						headerBottomPosition = headerBottomPosition<0 ? headerHeight : headerBottomPosition;
					}
				}else{
					headerBottomPosition = headerHeight;
				}
			}

			header = stickyListHeadersAdapter.getHeaderView(firstVisibleItem, header);
			if(getChildCount()>1){
				removeViewAt(1);
			}
			addView(header);
			LayoutParams params = (LayoutParams) header.getLayoutParams();
			params.height= headerHeight> 0 ? headerHeight : LayoutParams.WRAP_CONTENT;
			params.leftMargin = list.getPaddingLeft();
			params.rightMargin = list.getPaddingRight();
			params.bottomMargin = list.getPaddingBottom();
			params.topMargin = headerBottomPosition - headerHeight;
			params.gravity = 0;
			header.setLayoutParams(params);
			header.setVisibility(View.VISIBLE);
		}else{
			if(header != null){
				header.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(onScrollListener!=null){
			onScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

}

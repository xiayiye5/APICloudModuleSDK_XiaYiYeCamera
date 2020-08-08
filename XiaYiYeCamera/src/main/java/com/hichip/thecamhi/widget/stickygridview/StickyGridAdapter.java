package com.hichip.thecamhi.widget.stickygridview;

import java.util.List;

import com.bumptech.glide.Glide;
import com.hichip.R;
import com.hichip.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;
import com.hichip.thecamhi.widget.stickygridview.MyImageView.OnMeasureListener;
import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

public class StickyGridAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {

	private List<GridItem> list;
	private LayoutInflater mInflater;
	private GridView mGridView;
	private Point mPoint = new Point(0, 0);
	int     delMode;// false =0     true =1
	public  boolean[] checks;
	private Context  mContext;

	public StickyGridAdapter(Context context, List<GridItem> list,GridView mGridView) {
		this.list = list;
		this.mContext=context;
		mInflater = LayoutInflater.from(context);
		this.mGridView = mGridView;
		checks=new boolean[list.size()];
	}

	public void setDelMode(int delMode){
		this.delMode=delMode;
		for(int i  = 0 ;i < checks.length; i ++){
			checks[i] = false;
		}
		notifyDataSetChanged();
	}


	public int getDelMode(){
		return delMode;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder mViewHolder;
		if (convertView == null) {
			mViewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.grid_item, parent, false);
			mViewHolder.mImageView = (MyImageView) convertView
					.findViewById(R.id.grid_item);
			mViewHolder.grid_cb = (CheckBox) convertView
					.findViewById(R.id.grid_cb);

			convertView.setTag(mViewHolder);

			mViewHolder.mImageView.setOnMeasureListener(new OnMeasureListener() {  

				@Override  
				public void onMeasureSize(int width, int height) {

					mPoint.set(width, height);  
				}  
			}); 

		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		mViewHolder.grid_cb.setChecked(checks[position]);  

		String path = list.get(position).getPath();
		//mViewHolder.mImageView.setTag(path);
		//ImageView mImageView = (ImageView) mGridView.findViewWithTag(path);
		Glide.with(mContext).load(path).into(mViewHolder.mImageView);

//		Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(path, mPoint,
//				new NativeImageCallBack() {
//
//			@Override
//			public void onImageLoader(Bitmap bitmap, String path) {
//				ImageView mImageView = (ImageView) mGridView
//						.findViewWithTag(path);
//				if (bitmap != null && mImageView != null) {
//					mImageView.setImageBitmap(bitmap);
//				}
//			}
//		});
//
//		if (bitmap != null) {
//			mViewHolder.mImageView.setImageBitmap(bitmap);
//		} else {
//			mViewHolder.mImageView.setImageBitmap(null);
//		}

		if(delMode==1){
			//mViewHolder.grid_cb.setChecked(false);
			mViewHolder.grid_cb.setVisibility(View.VISIBLE);
		}else{
			mViewHolder.grid_cb.setVisibility(View.GONE);
		}
		return convertView;
	}


	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder mHeaderHolder;
		if (convertView == null) {
			mHeaderHolder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.grid_header, parent, false);
			mHeaderHolder.mTextView = (TextView) convertView
					.findViewById(R.id.grid_header);
			mHeaderHolder.mTextView = (TextView) convertView
					.findViewById(R.id.grid_header);
			convertView.setTag(mHeaderHolder);
		} else {
			mHeaderHolder = (HeaderViewHolder) convertView.getTag();
		}

		mHeaderHolder.mTextView.setText(list.get(position).getTime());

		return convertView;
	}

	public static class ViewHolder {
		public MyImageView mImageView;
		public CheckBox grid_cb;
	}

	public static class HeaderViewHolder {
		public TextView mTextView;
	}

	@Override
	public long getHeaderId(int position) {
		return list.get(position).getSection();
	}
	
}

package com.hichip.hichip.pictureviewer;

import com.bumptech.glide.Glide;
import com.hichip.R;
import com.hichip.widget.photoview.PhotoViewAttacher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageDetailFragment extends Fragment {
	private String mImageUrl;
	private ImageView mImageView;
	private PhotoViewAttacher mAttacher;

	public static ImageDetailFragment newInstance(String imageUrl) {
		final ImageDetailFragment imageDetailFragment = new ImageDetailFragment();

		final Bundle args = new Bundle();
		args.putString("url", imageUrl);
		imageDetailFragment.setArguments(args);

		return imageDetailFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageUrl = getArguments() != null ? getArguments().getString("url") : null;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_image_detail, container, false);
		mImageView = (ImageView) v.findViewById(R.id.image);
		Glide.with(getContext()).load(mImageUrl).into(mImageView);
		//mAttacher必须放在Glide下面,布局必须是 width="wrap_content" height="match_parent"   android:adjustViewBounds="true"
		mAttacher = new PhotoViewAttacher(mImageView);
		return v;
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if(mAttacher!=null){
			mAttacher=null;
		}
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);


	}
}

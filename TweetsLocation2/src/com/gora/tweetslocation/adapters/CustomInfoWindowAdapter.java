package com.gora.tweetslocation.adapters;

import pl.mg6.android.maps.extensions.GoogleMap.InfoWindowAdapter;
import pl.mg6.android.maps.extensions.Marker;
import twitter4j.Status;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gora.tweetslocation.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class CustomInfoWindowAdapter implements InfoWindowAdapter  {
	private View mContents = null;
	private Activity activity = null;
	private Marker ConcurrentMarker = null;
	private DisplayImageOptions options = null;
	private ImageLoader imageLoader = ImageLoader.getInstance();

	@SuppressWarnings("deprecation")
	public CustomInfoWindowAdapter(Activity _activity){
		activity = _activity;
		mContents = activity.getLayoutInflater().inflate(R.layout.custom_balloon_map, null);
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_launcher)
		.showImageForEmptyUri(R.drawable.ic_launcher)
		.showImageOnFail(R.drawable.ic_launcher)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		/*.displayer(new RoundedBitmapDisplayer(10))*/
		.build();
	}

	@Override
	public View getInfoWindow(Marker marker) {
		ConcurrentMarker = marker;
		ImageView imgLogo = (ImageView) mContents.findViewById(R.id.imgLogoBalloon);

		Status status = (Status) marker.getData();
		imageLoader.displayImage(status.getUser().getOriginalProfileImageURL(), imgLogo, options, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {}
			@Override
			public void onLoadingFailed(String imageUri, View view,FailReason failReason) {}
			@Override
			public void onLoadingCancelled(String imageUri, View view) {}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				try {
					new Handler().post(rnnblShowImg);
				} catch (Exception e) {
					Log.e("CustomInfoWindowAdapter", "onLoadingComplete Exception: "+e!=null&&e.getMessage()!=null?e.getMessage():"null");
				}
			}
		});

		String title = marker.getTitle();
		TextView titleUi = ((TextView) mContents.findViewById(R.id.txtVwTiltleBalloon));
		if (title != null) {
			// Spannable string allows us to edit the formatting of the text.
			SpannableString titleText = new SpannableString(title);
			titleUi.setText(titleText);
		} else {
			titleUi.setText("");
		}

		((TextView) mContents.findViewById(R.id.txtVwTime)).setText(status.getCreatedAt().toString());

		String snippet = marker.getSnippet();
		TextView snippetUi = ((TextView) mContents.findViewById(R.id.txtVwDireccionBalloon));
		if (snippet != null && snippet.length() > 12) {
			SpannableString snippetText = new SpannableString(snippet);
			snippetUi.setText(snippetText);
		}else{
			snippetUi.setText("");
		}

		return mContents;
	}

	@Override
	public View getInfoContents(Marker marker) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Runnable rnnblShowImg = new Runnable(){
		 @Override
		 public void run(){
			 ConcurrentMarker.showInfoWindow();
		 }
	 };
}
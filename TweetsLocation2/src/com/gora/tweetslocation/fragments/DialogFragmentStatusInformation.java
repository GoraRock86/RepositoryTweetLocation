package com.gora.tweetslocation.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gora.tweetslocation.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DialogFragmentStatusInformation extends DialogFragment {
	public static final String TAG_NAME = DialogFragmentStatusInformation.class.toString();
	public final static String ACTION_CONNETED = "DialogFragmentStatusInformation.BroadcastReceiver";
	public final static String KEY_RETWEET = "Action_Retwet";
	public final static String KEY_ADD_FAVORITE = "Action_favorite";
	public final static String KEY_ADD_BUNDLE = "BUNDLE_DATA_1";
	private View viewDetailStatus;
	private twitter4j.Status status = null;
	private DisplayImageOptions options = null;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private ImageView imgAvatar = null;

	@SuppressWarnings("deprecation")
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setCanceledOnTouchOutside(true);
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_launcher)
		.showImageForEmptyUri(R.drawable.ic_launcher)
		.showImageOnFail(R.drawable.ic_launcher)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		/*.displayer(new RoundedBitmapDisplayer(10))*/
		.build();
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Dialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		try {
			viewDetailStatus = inflater.inflate(R.layout.fragment_dialog_status_info, container, false);
			imgAvatar = (ImageView)viewDetailStatus.findViewById(R.id.imageViewAvatar);
			((TextView)viewDetailStatus.findViewById(R.id.textViewName)).setText(status.getUser().getName().toString());
			((TextView)viewDetailStatus.findViewById(R.id.textViewText)).setText(status.getText());
			((TextView)viewDetailStatus.findViewById(R.id.textViewDescription)).setText("Description: " + status.getUser().getDescription());
			((TextView)viewDetailStatus.findViewById(R.id.textViewDate)).setText("Date: " + status.getCreatedAt().toString());
			viewDetailStatus.findViewById(R.id.buttonRetwet).setOnClickListener(lisenerBtn);
			viewDetailStatus.findViewById(R.id.buttonAddFavorit).setOnClickListener(lisenerBtn);
			imageLoader.displayImage(status.getUser().getOriginalProfileImageURL(), imgAvatar, options);
		} catch (Exception e) {
			Log.e(TAG_NAME, "> " + " onCreateView: "+ e.getMessage());
		}
		return viewDetailStatus;
	}

	private View.OnClickListener lisenerBtn = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Bundle bundle = null;
			switch (v.getId()) {
			case R.id.buttonRetwet:
				if(!status.isRetweetedByMe()){
					bundle = new Bundle();
					bundle.putLong(KEY_RETWEET, status.getId());
				}else
					Toast.makeText(getActivity(), "Action Done", Toast.LENGTH_LONG).show();
				break;
			case R.id.buttonAddFavorit:
				if(!status.isFavorited()){
					bundle = new Bundle();
					bundle.putLong(KEY_ADD_FAVORITE, status.getId());
				}else
					Toast.makeText(getActivity(), "Action Done", Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
			if(bundle != null){
				Intent intent = new Intent(ACTION_CONNETED); 
				intent.putExtra(KEY_ADD_BUNDLE, bundle);
				getActivity().getApplicationContext().sendBroadcast(intent);
			}
		}
	};

	public twitter4j.Status getStatus() {
		return status;
	}

	public void setStatus(twitter4j.Status status) {
		this.status = status;
	}
}

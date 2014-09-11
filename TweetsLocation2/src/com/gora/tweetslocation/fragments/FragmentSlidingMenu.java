package com.gora.tweetslocation.fragments;


import java.util.List;

//import twitter4j.AsyncTwitter;
//import twitter4j.AsyncTwitterFactory;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
//import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
//import twitter4j.TwitterListener;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gora.tweetslocation.MainActivity;
import com.gora.tweetslocation.R;
import com.gora.tweetslocation.applicationdata.AppData;
//import static twitter4j.TwitterMethod.UPDATE_STATUS;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class FragmentSlidingMenu extends Fragment {
	private View viewContent = null;
	private String TAG_NAME = FragmentSlidingMenu.class.toString();

	// Login, Update status, Logout and getTime Line buttons
	private Button btnLoginTwitter = null,  btnUpdateStatus = null, btnLogoutTwitter = null, btnGetTimeLine = null;
		
	// EditText for update
	private EditText txtUpdate = null;
	private TextView lblUpdate = null, lblUserName = null;

	// Progress dialog
	private ProgressDialog pDialog = null;

	// Twitter
	private static Twitter twitter = null;
	private static RequestToken requestToken = null;
//	private static RequestToken requestToken2 = null;

	// Shared Preferences
	private static SharedPreferences mSharedPreferences = null;
	private User userTwitter = null;
	
	private DisplayImageOptions options = null;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private ImageView imgAvatar = null;

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			viewContent = inflater.inflate(R.layout.fragment_sliding_menu, container, false);
			
			// All UI elements
			btnLoginTwitter = (Button) viewContent.findViewById(R.id.btnLoginTwitter);
			btnUpdateStatus = (Button) viewContent.findViewById(R.id.btnUpdateStatus);
			btnLogoutTwitter = (Button) viewContent.findViewById(R.id.btnLogoutTwitter);
			btnGetTimeLine = (Button) viewContent.findViewById(R.id.btnShowRecentTweets);
			txtUpdate = (EditText) viewContent.findViewById(R.id.txtUpdateStatus);
			lblUpdate = (TextView) viewContent.findViewById(R.id.lblUpdate);
			lblUserName = (TextView) viewContent.findViewById(R.id.lblUserName);
			imgAvatar = (ImageView) viewContent.findViewById(R.id.imageViewAva);

			// Shared Preferences
			mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
			
			btnLoginTwitter.setOnClickListener(onClickLisener);
			btnUpdateStatus.setOnClickListener(onClickLisener);
			btnLogoutTwitter.setOnClickListener(onClickLisener);
			btnGetTimeLine.setOnClickListener(onClickLisener);
			
			options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.ic_launcher)
			.showImageForEmptyUri(R.drawable.ic_launcher)
			.showImageOnFail(R.drawable.ic_launcher)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.considerExifParams(true)
			.displayer(new RoundedBitmapDisplayer(10))
			.build();

			/** This if conditions is tested once is
			 * redirected from twitter page. Parse the uri to get oAuth
			 * Verifier
			 * */
			if (isTwitterLoggedInAlready()) {
				new getTwitterSession().execute();
			}else if (getActivity().getIntent().getData() != null){
				saveSeccion();
			}
		} catch (Exception e) {
			Log.e( TAG_NAME , "onCreateView() Exception: " + e!=null&&e.getMessage()!=null? e.getMessage():e+"");
		}
		return viewContent;
	}

	private View.OnClickListener onClickLisener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnLoginTwitter:
				loginToTwitter();
				break;
			case R.id.btnUpdateStatus:
				// Call update status function
				// Get the status from EditText
				String status = txtUpdate.getText().toString();

				// Check for blank text
				if (status.trim().length() > 0) {
					// update status
					new updateTwitterStatus().execute(status);
				} else {
					// EditText is empty
					Toast.makeText(getActivity(),"Please enter status message", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.btnLogoutTwitter:
				logoutFromTwitter();
				break;
			case R.id.btnShowRecentTweets:
				// update status
				if(AppData.loactionDevice != null){
					new getMostRecentTweets().execute(AppData.loactionDevice);
				}

				break;
			default:
				break;
			}
		}
	};

	private void saveSeccion() throws Exception {
		Uri uri = getActivity().getIntent().getData();
		if (uri != null && uri.toString().startsWith(AppData.TWITTER_CALLBACK_URL)) {
			// oAuth verifier
			final String verifier = uri.getQueryParameter(AppData.URL_TWITTER_OAUTH_VERIFIER);

			new AsyncTask<Void, Void, AccessToken>() {
				@Override
				protected AccessToken doInBackground(Void... params) {
					AccessToken accessToken = null;
					try {
						// Get the access token
						 accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
						 long userID = accessToken.getUserId();
						 userTwitter = twitter.showUser(userID);
					} catch (Exception e) {
						Log.e("Twitter Login Error", "> " + e.getMessage());
					}
					return accessToken;
				}

				@Override
				protected void onPostExecute(AccessToken accessToken) {
					try {
						if(accessToken!= null){
							// Shared Preferences
							Editor e = mSharedPreferences.edit();

							// After getting access token, access token secret
							// store them in application preferences
							e.putString(AppData.PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
							e.putString(AppData.PREF_KEY_OAUTH_SECRET,accessToken.getTokenSecret());
							// Store login status - true
							e.putBoolean(AppData.PREF_KEY_TWITTER_LOGIN, true);
							e.commit(); // save changes

							Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

							showData();
							if(AppData.loactionDevice != null){
								new getMostRecentTweets().execute(AppData.loactionDevice);
							}
						}
					} catch (Exception e) {
						Log.e("Twitter Login Error", "> " + e.getMessage());
					}
				}
			}.execute();
		}
	}

	/**
	 * Function to login twitter
	 * */
	private void loginToTwitter() {
		// Check if already logged in
		if (!isTwitterLoggedInAlready()) {
			TwitterFactory factory = new TwitterFactory(getConfiguration());
			twitter = factory.getInstance();
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... arg0) {
					try {
						requestToken = twitter.getOAuthRequestToken(AppData.TWITTER_CALLBACK_URL);
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
					} catch (TwitterException e) {
						e.printStackTrace();
					}
					return null;
				}
			}.execute();
			
		} else {
			// user already logged into twitter
			Toast.makeText(getActivity(),"Already Logged into twitter", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Function to getTwitterSession from Preferens
	 * */
	class getTwitterSession extends AsyncTask<Void , String, AccessToken> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoading();
		}

		@Override
		protected AccessToken doInBackground(Void... params)  {
			AccessToken accessToken = null;
			try {
				accessToken = getAccesTokenPreference();
				twitter = new TwitterFactory(getConfiguration()).getInstance(accessToken);
				long userID = accessToken.getUserId();
				userTwitter = twitter.showUser(userID);

//				synchronizeTweets();
			} catch (Exception e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
			}
			return accessToken;
		}

		@Override
		protected void onPostExecute(AccessToken accessToken) {
			try {
				if(accessToken!= null){
					// dismiss the dialog after getting all products
					pDialog.dismiss();
					showData();
					if(AppData.loactionDevice != null){
						new getMostRecentTweets().execute(AppData.loactionDevice);
					}
				}
			} catch (Exception e) {
				Log.e("Twitter Login Error", "> " + e.getMessage());
			}
		}
	}

	/**
	 * Function to update status
	 * */
	class updateTwitterStatus extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoading();
		}

		protected String doInBackground(String... args) {
			Log.d("Tweet Text", "> " + args[0]);
			String status = args[0];
			try {
//				AccessToken accessToken = getAccesTokenPreference();
//				Twitter twitter = new TwitterFactory(getConfiguration()).getInstance(accessToken);
				if(twitter!= null){
					// Update status
					twitter4j.Status response = twitter.updateStatus(status);
					Log.d("Status", "> " + response.getText());
				}
			} catch (TwitterException e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
			} catch (Exception e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(),"Status tweeted successfully", Toast.LENGTH_SHORT).show();
					// Clearing EditText field
					txtUpdate.setText("");
				}
			});
		}
	}
	
	/**
	 * Function to getTwitterSession from Preferens
	 * */
	class getMostRecentTweets  extends AsyncTask<android.location.Location , String, List<twitter4j.Status>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoading();
		}

		@Override
		protected List<twitter4j.Status> doInBackground(android.location.Location... location)  {
			List<twitter4j.Status> tweets  = null;
			try {
		        Query query = new Query();
		        GeoLocation locationt = new GeoLocation(location[0].getLatitude(), location[0].getLongitude());
		        Query.Unit unit = Query.MILES; // or Query.MILES;
		        query.setGeoCode(locationt, 1, unit);
		        query.setCount(100);
		        QueryResult result = twitter.search(query);
		        tweets = result.getTweets();
		    } catch (TwitterException te) {
		        System.out.println("Failed to search tweets: " + te.getMessage());
		        System.exit(-1);
		    } catch (Exception te) {
		    	System.out.println("Failed to search tweets: " + te.getMessage());
	        	System.exit(-1);
		    }
			return tweets;
		}

		@Override
		protected void onPostExecute(List<twitter4j.Status> statuses) {
			try {
				// dismiss the dialog after getting all products
				pDialog.dismiss();
				if(statuses!= null && statuses.size() >0){
					((MainActivity)getActivity()).addMarkersToMap(statuses);
				}
			} catch (Exception e) {
				Log.e("Twitter Login Error", "> " + e.getMessage());
			}
		}
	}
	
	/**
	 * Function to getTwitterSession from Preferens
	 * */
	class asyncReTweet  extends AsyncTask<Long , String, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoading();
		}

		@Override
		protected Void doInBackground(Long... strIdStatus)  {
			try {
		        twitter.retweetStatus(strIdStatus[0]);

		    } catch (TwitterException te) {
		        System.out.println("Failed to search tweets: " + te.getMessage());
		        System.exit(-1);
		    } catch (Exception te) {
		    	System.out.println("Failed to search tweets: " + te.getMessage());
	        	System.exit(-1);
		    }
			return null;
		}

		@Override
		protected void onPostExecute(Void asd) {
			try {
				// dismiss the dialog after getting all products
				pDialog.dismiss();
			} catch (Exception e) {
				Log.e("Twitter Login Error", "> " + e.getMessage());
			}
		}
	}
	
	/**
	 * Function to getTwitterSession from Preferens
	 * */
	class asyncAddFavorite  extends AsyncTask<Long , String, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoading();
		}

		@Override
		protected Void doInBackground(Long... strIdStatus)  {
			try {
		        twitter.createFavorite(strIdStatus[0]);
		    } catch (TwitterException te) {
		        System.out.println("Failed to search tweets: " + te.getMessage());
		        System.exit(-1);
		    } catch (Exception te) {
		    	System.out.println("Failed to search tweets: " + te.getMessage());
	        	System.exit(-1);
		    }
			return null;
		}

		@Override
		protected void onPostExecute(Void asd) {
			try {
				// dismiss the dialog after getting all products
				pDialog.dismiss();
			} catch (Exception e) {
				Log.e("Twitter Login Error", "> " + e.getMessage());
			}
		}
	}
	
//	private void synchronizeTweets(){
//		try {
//			TwitterListener listener = new TwitterAdapter() {
//				@Override
//				public void updatedStatus(twitter4j.Status status) {
//					System.out.println("Successfully updated the status to [" + status.getText() + "].");
//				}
//
//				@Override
//				public void onException(twitter4j.TwitterException e, twitter4j.TwitterMethod method) {
//					if (method == UPDATE_STATUS) {
//						e.printStackTrace();
//					} else {
//						throw new AssertionError("Should not happen");
//					}
//				}
//			};
//	        // The factory instance is re-useable and thread safe.
//	        AsyncTwitterFactory factory = new AsyncTwitterFactory(getConfiguration());
//	        AsyncTwitter asyncTwitter = factory.getInstance();
//	        asyncTwitter.addListener(listener);
////	        asyncTwitter.updateStatus("Ok es un Ejemplo");
//	        
//	        requestToken2 = asyncTwitter.getOAuthRequestToken();
//		} catch (Exception e) {
//			Log.e("synchronizeTweets", "> " + e.getMessage());
//		}
//	}

	/**
	 * Function to logout from twitter
	 * It will just clear the application shared preferences
	 * */
	private void logoutFromTwitter() {
		// Clear the shared preferences
		Editor e = mSharedPreferences.edit();
		e.remove(AppData.PREF_KEY_OAUTH_TOKEN);
		e.remove(AppData.PREF_KEY_OAUTH_SECRET);
		e.remove(AppData.PREF_KEY_TWITTER_LOGIN);
		e.commit();

		// After this take the appropriate action
		// I am showing the hiding/showing buttons again
		// You might not needed this code
		btnLogoutTwitter.setVisibility(View.GONE);
		btnUpdateStatus.setVisibility(View.GONE);
		btnGetTimeLine.setVisibility(View.GONE);
		txtUpdate.setVisibility(View.GONE);
		lblUpdate.setVisibility(View.GONE);
		lblUserName.setText("");
		lblUserName.setVisibility(View.GONE);
		btnLoginTwitter.setVisibility(View.VISIBLE);
		imgAvatar.setVisibility(View.GONE);
		((MainActivity)getActivity()).clearMap();
	}

	/**
	 * Check user already logged in your application using twitter Login flag is
	 * fetched from Shared Preferences
	 * */
	private boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences.getBoolean(AppData.PREF_KEY_TWITTER_LOGIN, false);
	}

	public void showLoading() {
		pDialog = new ProgressDialog(getActivity());
		pDialog.setMessage("Updating to twitter...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
	}

	public AccessToken getAccesTokenPreference() throws Exception{
		// Access Token 
		String access_token = mSharedPreferences.getString(AppData.PREF_KEY_OAUTH_TOKEN, "");
		// Access Token Secret
		String access_token_secret = mSharedPreferences.getString(AppData.PREF_KEY_OAUTH_SECRET, "");

		return new AccessToken(access_token, access_token_secret);
	}

	protected void showData()throws Exception {
		// Hide login button
		btnLoginTwitter.setVisibility(View.GONE);

		// Show Update Twitter
		lblUpdate.setVisibility(View.VISIBLE);
		txtUpdate.setVisibility(View.VISIBLE);
		btnUpdateStatus.setVisibility(View.VISIBLE);
		btnGetTimeLine.setVisibility(View.VISIBLE);
		btnLogoutTwitter.setVisibility(View.VISIBLE);
		lblUserName.setVisibility(View.VISIBLE);
		imgAvatar.setVisibility(View.VISIBLE);

		// Getting user details from twitter
		// For now i am getting his name only
		if(userTwitter!= null){
			String username = userTwitter.getName();
			// Displaying in xml ui
			lblUserName.setText(Html.fromHtml("<b>Welcome " + username + "</b>"));

			imageLoader.displayImage(userTwitter.getOriginalProfileImageURL(), imgAvatar, options);
		}
	}

	private Configuration getConfiguration(){
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(AppData.TWITTER_CONSUMER_KEY);
		builder.setOAuthConsumerSecret(AppData.TWITTER_CONSUMER_SECRET);
		Configuration configuration = builder.build();
		return configuration;
	}
	
	

	public void retweet(long strAction) {
		new asyncReTweet().execute(strAction);
	}

	public void addFavorite(long strAction2) {
		new asyncAddFavorite().execute(strAction2);
	}
}
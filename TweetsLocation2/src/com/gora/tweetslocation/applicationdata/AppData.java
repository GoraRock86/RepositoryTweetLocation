package com.gora.tweetslocation.applicationdata;

import android.location.Location;

public class AppData {
	// Constants
	/**
	 * Register your here app https://dev.twitter.com/apps/new and get your
	 * consumer key and secret
	 * */
	public static final String TWITTER_CONSUMER_KEY = "pcMgPjI11o4duyIyGrIAw"; // place your cosumer key here
	public static final String TWITTER_CONSUMER_SECRET = "y1mnS3V3LrwaLzHLmZ6xPp4Arr3baP2eeWars26wYI"; // place your consumer secret here

	// Preference Constants
	public static final String PREFERENCE_NAME = "twitter_oauth";
	public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	public static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	public static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

	public static final String TWITTER_CALLBACK_URL = "oauth://tweetslocation";
	public static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	
	public static Location loactionDevice = null;
}

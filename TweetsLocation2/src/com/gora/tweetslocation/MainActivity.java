package com.gora.tweetslocation;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.gora.tweetslocation.applicationdata.AppData;
import com.gora.tweetslocation.fragments.DialogFragmentStatusInformation;
import com.gora.tweetslocation.fragments.FragmentMaps;
import com.gora.tweetslocation.fragments.FragmentSlidingMenu;
import com.gora.tweetslocation.utils.AlertDialogManager;
import com.gora.tweetslocation.utils.ConnectionDetector;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity {
	
	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

	private FragmentSlidingMenu fragSlidingMn  = null;
	private CanvasTransformer mTransformer = null;
	
	// Internet Connection detector
	private ConnectionDetector cd = null;
	
	// Alert Dialog Manager
	private AlertDialogManager alert = new AlertDialogManager();

	private FragmentMaps fragmtMaps;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		checkPlayServices();

		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(MainActivity.this, "Internet Connection Error", "Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}

		// Check if twitter keys are set
		if(AppData.TWITTER_CONSUMER_KEY.trim().length() == 0 || AppData.TWITTER_CONSUMER_SECRET.trim().length() == 0){
			// Internet Connection is not present
			alert.showAlertDialog(MainActivity.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
			// stop executing code by return
			return;
		}

		setBehindContentView(R.layout.menu_frame);
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
			fragSlidingMn = new FragmentSlidingMenu();
			t.replace(R.id.menu_frame, fragSlidingMn,"FragmentSlidingMenu");
			t.commit();
		} else {
			fragSlidingMn = (FragmentSlidingMenu)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
		}

		// customize the SlidingMenu
		// Menu Effect
		mTransformer = new CanvasTransformer() {
			@Override
			public void transformCanvas(Canvas canvas, float percentOpen) {
				float scale = (float) (percentOpen * 0.25 + 0.75);
				canvas.scale(scale, scale, canvas.getWidth() / 2,canvas.getHeight() / 2);
			}
		};

		getSlidingMenu().setBehindOffsetRes(R.dimen.slidingmenu_offset);
		getSlidingMenu().setShadowWidthRes(R.dimen.shadow_width);
		getSlidingMenu().setShadowDrawable(R.drawable.shadow);
		getSlidingMenu().setFadeDegree(0.775f);
		getSlidingMenu().setBehindScrollScale(0.47f);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		getSlidingMenu().setBehindCanvasTransformer(mTransformer);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		fragmtMaps  = new FragmentMaps();
		getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmtMaps, FragmentMaps.TAG_NAME).commit();

		IntentFilter f = new IntentFilter(DialogFragmentStatusInformation.ACTION_CONNETED);
		registerReceiver(onEventReconnected, f);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private BroadcastReceiver onEventReconnected = new BroadcastReceiver() {
		public void onReceive(Context ctxt, Intent i) {
			try {
				Bundle bundle = i.getExtras().getBundle(DialogFragmentStatusInformation.KEY_ADD_BUNDLE);
				long strAction = bundle.getLong(DialogFragmentStatusInformation.KEY_RETWEET);
				long strAction2 = bundle.getLong(DialogFragmentStatusInformation.KEY_ADD_FAVORITE);
				if(strAction != 0 )
					fragSlidingMn.retweet(strAction);
				else if(strAction2 != 0)
					fragSlidingMn.addFavorite(strAction2);
			} catch (Exception e) {
				Log.e("MainActivity","onCreate() Error: " + e != null && e.getMessage() != null ? e.getMessage() : e + " e is null");
			}
		}
	};

	public void addMarkersToMap(List<twitter4j.Status> listStatus) {
		try {
			fragmtMaps.addMarkersToMap(listStatus);
			toggle();
		} catch (Exception e) {
			Log.e("MainActivity","onCreate() Error: " + e != null && e.getMessage() != null ? e.getMessage() : e + " e is null");
		}
	}
	
	public void clearMap(){
		try {
			fragmtMaps.clearMap();
		} catch (Exception e) {
			Log.e("MainActivity","onCreate() Error: " + e != null && e.getMessage() != null ? e.getMessage() : e + " e is null");
		}
	}

	private boolean checkPlayServices() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (status != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
				showErrorDialog(status);
			} else {
				Toast.makeText(this, "Este dispositivo no soporta la aplicacion.", Toast.LENGTH_LONG).show();
				finish();
			}
			return false;
		}
		return true;
	}

	void showErrorDialog(int code) {
		GooglePlayServicesUtil.getErrorDialog(code, this,REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}
}

package com.gora.tweetslocation.fragments;


import java.util.List;

import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.GoogleMap.OnInfoWindowClickListener;
import pl.mg6.android.maps.extensions.GoogleMap.OnMarkerClickListener;
import pl.mg6.android.maps.extensions.Marker;
import pl.mg6.android.maps.extensions.MarkerOptions;
import pl.mg6.android.maps.extensions.SupportMapFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.gora.tweetslocation.R;
import com.gora.tweetslocation.adapters.CustomInfoWindowAdapter;
import com.gora.tweetslocation.applicationdata.AppData;

public class FragmentMaps  extends Fragment  implements GooglePlayServicesClient.ConnectionCallbacks,
														GooglePlayServicesClient.OnConnectionFailedListener,
														LocationListener, OnInfoWindowClickListener, OnMarkerClickListener, OnItemSelectedListener{
	private  View viewMaps = null;
	public static final String TAG_NAME = FragmentMaps.class.toString();
	private SupportMapFragment mMapFragment = null;
	private GoogleMap mMap = null;
	private LocationClient lc = null;
	private LocationRequest lr = null;
	private CustomInfoWindowAdapter adptersMaps;
	private Spinner spinnerTypeMap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		try {
			if (viewMaps != null) {
				ViewGroup parent = (ViewGroup) viewMaps.getParent();
				if (parent != null)
					parent.removeView(viewMaps);
			}
			viewMaps = inflater.inflate(R.layout.google_maps, container, false);
			initializesControls();
		} catch (InflateException e) {
			Log.e(TAG_NAME, "onCreateView InflateException: "+e!=null&&e.getMessage()!=null?e.getMessage():"null");
		}catch (Exception e) {
			Log.e(TAG_NAME, "onCreateView Exception: "+e!=null&&e.getMessage()!=null?e.getMessage():"null");
		}
		return viewMaps;
	}
	
	  @Override
      public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          lr = LocationRequest.create();
          lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
          lc = new LocationClient(this.getActivity().getApplicationContext(),this, this);
          lc.connect();
      }

	private void initializesControls() {
		try {
			mMapFragment = new SupportMapFragment() {

				@Override
				public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
					View view = super.onCreateView(inflater, container,savedInstanceState);
					setMapTransparent((ViewGroup) view);
					getMap().setTrafficEnabled(false);
					getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
					getMap().setMyLocationEnabled(true);
					mMap = getExtendedMap();
					mMap.setOnInfoWindowClickListener(FragmentMaps.this);
					mMap.setOnMarkerClickListener(FragmentMaps.this);
					return view;
				}

				private void setMapTransparent(ViewGroup group) {
					int childCount = group.getChildCount();

					for (int i = 0; i < childCount; i++) {
						View child = group.getChildAt(i);
						if (child instanceof ViewGroup) {
							setMapTransparent((ViewGroup) child);
						} else if (child instanceof SurfaceView) {
							child.setBackgroundColor(0x00000000);
						}
					}
				}
			};

			getChildFragmentManager().beginTransaction().replace(R.id.contentMap, mMapFragment).commit();

			spinnerTypeMap = (Spinner)viewMaps.findViewById(R.id.spinnerTypeMap);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.typeMaps, R.layout.item_spiner_text);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerTypeMap.setAdapter(adapter);
			spinnerTypeMap.setVisibility(View.VISIBLE);
	
			spinnerTypeMap.setOnItemSelectedListener(this);

		} catch (Exception e) {}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		lc.requestLocationUpdates(lr,  this);
	}

	@Override
	public void onDisconnected() {
	}

	@Override
	public void onLocationChanged(Location location) {
		try {
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15);
			mMap.animateCamera(cameraUpdate);
			if (location != null)
				AppData.loactionDevice = location;
		} catch (Exception e) {
		}
	}
	
	public void clearMap(){
		if(mMap != null)
			mMap.clear();
	}

	public void addMarkersToMap(List<twitter4j.Status> listStatus) {
		try {
			mMap.clear();
			Bitmap arrowTemp = BitmapFactory.decodeResource(getResources(),R.drawable.arrow);
			for (twitter4j.Status status : listStatus) {
				boolean hasLocation = status.getGeoLocation() !=null;
				if (hasLocation) {
					LatLng coordinates = new LatLng(status.getGeoLocation().getLatitude(),status.getGeoLocation().getLongitude());
					mMap.addMarker(new MarkerOptions()
									.position(coordinates)
									.title(status.getUser().getName())
									.snippet(status.getText())
									.icon(BitmapDescriptorFactory.fromBitmap(arrowTemp)).anchor(0.5f, 1)).setData(status);
				}
			}
		} catch (Exception e) {}

		adptersMaps = new CustomInfoWindowAdapter(getActivity());
		mMap.setInfoWindowAdapter(adptersMaps);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		DialogFragmentStatusInformation dialogFragDetails = new DialogFragmentStatusInformation();
		dialogFragDetails.setStatus((twitter4j.Status)marker.getData());
		dialogFragDetails.show(getFragmentManager(), DialogFragmentStatusInformation.TAG_NAME);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		
		toBounceMarker(marker);
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		switch (pos) {
		case 0:
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case 1:
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case 2:
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;
		case 3:
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		default:
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
	
	
	private void toBounceMarker(final Marker marker) {
		try {
			final LatLng cordenatesTemp = marker.getPosition();
			final Handler handler = new Handler();
			final long start = SystemClock.uptimeMillis();
			Projection proj = mMap.getProjection();
			Point startPoint = proj.toScreenLocation(cordenatesTemp);
			startPoint.offset(0, -100);
			final LatLng startLatLng = proj.fromScreenLocation(startPoint);
			final long duration = 1500;

			final Interpolator interpolator = new BounceInterpolator();

			handler.post(new Runnable() {
				@Override
				public void run() {
					try {
						long elapsed = SystemClock.uptimeMillis() - start;
						float t = interpolator.getInterpolation((float) elapsed / duration);
						double lng = t * cordenatesTemp.longitude + (1 - t) * startLatLng.longitude;
						double lat = t * cordenatesTemp.latitude + (1 - t) * startLatLng.latitude;
						marker.setPosition(new LatLng(lat, lng));
						if (t < 1.0) {
							// Post again 16ms later.
							handler.postDelayed(this, 16);
						}
					} catch (Exception e) {}
				}
			});
		} catch (Exception e) {}	
	}
}

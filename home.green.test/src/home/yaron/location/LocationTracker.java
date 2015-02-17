package home.yaron.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**  
 * @author Yaron Ronen - 16/01/2015  
 */
public class LocationTracker
{
	private final static String TAG = LocationTracker.class.getSimpleName();

	// Consts
	final static float UI_UPDATE_DISTANCE = 10; // meters.

	private static LocationTracker locationTrackerInstance = null; // Singleton

	private Context context;
	private Location currentLocation = null; // The most updated location we have.	
	private LocationTrackerListener locationTrackerListener = null;	

	private LocationTracker()
	{
		// Singleton
	}

	public static LocationTracker getInstance()
	{
		// Create singleton object.
		if( LocationTracker.locationTrackerInstance == null )
			LocationTracker.locationTrackerInstance = new LocationTracker();

		return LocationTracker.locationTrackerInstance;
	}

	public static void startRequestLocation(Context context)
	{		
		getInstance().requestLocationUpdates(context);
	}

	public static void stopRequestLocation(Context context)
	{
		getInstance().removeUpdates(context);
	}

	private void requestLocationUpdates(Context context)
	{		
		final LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		final Criteria criteria = createCriteria();
		String provider = locationManager.getBestProvider(criteria, true);	

		if( provider != null )
		{
			final Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

			// First time call get fast location or a better location.
			if( currentLocation == null || isBetterLocation(lastKnownLocation, currentLocation) )
			{
				currentLocation = lastKnownLocation;
			}
			
			if( innerLocationListener != null )
				locationManager.removeUpdates(innerLocationListener); // Remove old updates, if any.
			locationManager.requestLocationUpdates(provider, 250, 2F,innerLocationListener);	
		}
	}

	private Criteria createCriteria()
	{
		final Criteria criteria = new Criteria();
		//criteria.setAccuracy(Criteria.ACCURACY_HIGH);

		//API level 9 and up
		criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
		criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
		criteria.setBearingAccuracy(Criteria.ACCURACY_LOW);
		criteria.setSpeedAccuracy(Criteria.ACCURACY_MEDIUM);

		return criteria;
	}

	private boolean isBetterLocation(Location newLocation, Location currentLocation)
	{
		if( newLocation.getTime() > currentLocation.getTime() || newLocation.getAccuracy() > currentLocation.getAccuracy() )
			return true;
		else
			return false;
	}

	/**
	 * Inner location listener.
	 */
	private LocationListener innerLocationListener = new LocationListener()
	{
		public void onLocationChanged(Location location)
		{
			updateMyCurrentLoc(location);
		}

		public void onProviderDisabled(String provider)	{ }
		public void onProviderEnabled(String provider) { }
		public void onStatusChanged(String provider, int status, Bundle extras) { }
	};	

	private void updateMyCurrentLoc(Location location)
	{
		// Update with a better location.
		if( isBetterLocation(location, currentLocation) )
		{
			currentLocation = location;	// Update current location.
			Log.d(TAG,"Better location:"+location.toString());			

			if( locationTrackerListener != null )
				this.locationTrackerListener.onLocationTrackerChanged(location);			
		}
	}

	private void removeUpdates(Context context)
	{
		// Remove the listener you previously added
		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(innerLocationListener);		
	}

	public static interface LocationTrackerListener
	{
		public void onLocationTrackerChanged(Location location);
	}

	public void setLocationTrackerListener(LocationTrackerListener locationTrackerListener)
	{
		this.locationTrackerListener = locationTrackerListener;
	}

	public Location getCurrentLocation()
	{
		return this.currentLocation;		
	}
}

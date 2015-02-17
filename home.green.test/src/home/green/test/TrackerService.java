package home.green.test;

import home.yaron.location.LocationTracker;
import home.yaron.location.LocationTracker.LocationTrackerListener;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

@Deprecated()
public class TrackerService extends Service
{
	private final static String TAG = TrackerService.class.getSimpleName();
	
	//private ArrayList<Location> locationList; 
	
	@Override
	public void onCreate() 
	{		
		super.onCreate();
		Log.d(TAG,"onCreate(..)");	
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{		
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG,"onStartCommand(..)");
		
		//this.locationList = new ArrayList<Location>();

		// Set the callback for the location.
		LocationTracker.getInstance().setLocationTrackerListener(new LocationTrackerListener() {

			@Override
			public void onLocationTrackerChanged(final Location newLocation) {							

				// Post the drawing to release the location listener.
				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {  
						final String message = "Latitude:"+newLocation.getLatitude()+" Longitude:"+newLocation.getLongitude()+
								   			   " Receiving Time:"+newLocation.getTime()+" Speed:"+newLocation.getSpeed();
						Log.d(TAG,"newLocation:"+message);
						//locationList.add(newLocation);					
					}
				});				
			}});

		LocationTracker.startRequestLocation(this.getApplicationContext());

		return START_NOT_STICKY;
	}	
	
	@Override
	public void onDestroy()
	{	
		super.onDestroy();
		Log.d(TAG,"onDestroy(..)");

		//LocationTracker.stopRequestLocation(getApplicationContext());
	}

	@Override
	public IBinder onBind(Intent intent)
	{		
		return null;
	}
}
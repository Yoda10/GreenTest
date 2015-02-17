package home.green.test;

import home.green.test.RecorderService.LocalBinder;
import home.green.test.R;
import home.yaron.location.LocationTracker;
import home.yaron.location.LocationTracker.LocationTrackerListener;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MainActivity extends Activity implements OnMapReadyCallback  
{
	private final static String TAG = MainActivity.class.getSimpleName();

	private MapController mapController;
	RecorderService recorderService;
	private boolean isServiceBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		

		final MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this); 

		// Set the callback for the location.
		LocationTracker.getInstance().setLocationTrackerListener(createMapLocationTrackerListener());

		// Start tracking the user for the first time.
		LocationTracker.startRequestLocation(this);

		// Set tracking button listener.
		final ToggleButton trackingButton = (ToggleButton)findViewById(R.id.tracking_button);
		trackingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				if( ((ToggleButton)v).isChecked() )
				{
					Log.d(TAG,"Tracking button checked.");					
					LocationTracker.getInstance().setLocationTrackerListener(createMapLocationTrackerListener());
					LocationTracker.startRequestLocation(v.getContext());
				}
				else
				{
					Log.d(TAG,"Tracking button unchecked.");
					LocationTracker.stopRequestLocation(v.getContext());
					mapController.clearMap();
				}
			}
		});
	}
	
	private LocationTrackerListener createMapLocationTrackerListener()
	{
		return new LocationTrackerListener() {

			@Override
			public void onLocationTrackerChanged(final Location location) {								

				// Post the drawing to release the location listener.
				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {		                
						mapController.drawMapLine(location); 
					}
				});				
			}};		
	}

	@Override
	public void onMapReady(GoogleMap map)
	{	
		Log.d(TAG,"Map is ready.");

		final ViewGroup activityContainer = (ViewGroup)findViewById(R.id.activity_main_container);

		mapController = new MapController(this, map, activityContainer); // Init the controller.
		final Location currentLocation = LocationTracker.getInstance().getCurrentLocation();
		mapController.setInitMapLocation(currentLocation);
	}

	@Override
	protected void onStop()
	{	
		super.onStop();
		Log.d(TAG,"onStop(..)");
		
		final ToggleButton trackingButton = (ToggleButton)findViewById(R.id.tracking_button);
		
		if( !this.isFinishing() && trackingButton.isChecked() )
		{
			final Intent intent = new Intent(this, RecorderService.class);
			bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		}		
	}

	@Override
	protected void onStart()
	{	
		super.onStart();
		Log.d(TAG,"onStart(..)");		

		// Unbind from the recorder service activity is in the foreground.
		unbindRecorderService();
		
		// Register to location tracker foreground listener.
		final ToggleButton trackingButton = (ToggleButton)findViewById(R.id.tracking_button);
		if( trackingButton.isChecked() )
		{
			LocationTracker.getInstance().setLocationTrackerListener(createMapLocationTrackerListener());
			LocationTracker.startRequestLocation(this);
		}
	}
	
	@Override
	protected void onDestroy() 
	{	
		super.onDestroy();
		Log.d(TAG,"onDestroy(..)");
		
		// Unbind from the recorder service.
		unbindRecorderService();
		
		serviceConnection = null;
		recorderService = null;
		mapController = null;
	}
	
	private void unbindRecorderService()
	{
		// Unbind from the recorder service.
		if( isServiceBound ) 
		{
			// Stop tracking in the background.			
			recorderService.stopRecording();

			unbindService(serviceConnection);
			isServiceBound = false;
		}	
	}
	

	/**
	 *  Defines callback for service binding, passed to bindService(). 
	 */
	private ServiceConnection serviceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			LocalBinder binder = (LocalBinder)service;
			recorderService = binder.getService();
			isServiceBound = true;
			
			// Start tracking location in the background.
			recorderService.startRecording();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0)
		{
			isServiceBound = false;			
		}
	};
}

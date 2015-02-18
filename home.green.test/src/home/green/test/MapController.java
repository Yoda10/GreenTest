package home.green.test;

import home.green.test.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapController
{
	private static final String TAG = MapController.class.getSimpleName();
	
	private static final float ZOOM_LEVE = 19f;

	private Context context;
	private Location mapLastLocation;
	private GoogleMap map;
	private ViewGroup activityView; // Root view of the activity.
	private TextView locationText;	

	public MapController(Context context, GoogleMap map, ViewGroup activityView)
	{
		this.context = context;
		this.map = map;
		this.activityView = activityView;

		init();
	}

	private void init()
	{
		locationText = (TextView)activityView.findViewById(R.id.location_text);		
	}

	/**
	 * Set the first view of the map.	 
	 */
	public void setInitMapLocation(Location currentLocation)
	{
		if(currentLocation != null)
		{
			// Init the map last location.
			mapLastLocation = currentLocation;
					
			map.setMyLocationEnabled(true);
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 13));

			CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))      // Sets the center of the map to location user
			.zoom(ZOOM_LEVE)            // Sets the zoom
			.bearing(90)                // Sets the orientation of the camera to east
			.tilt(40)                   // Sets the tilt of the camera to 30 degrees
			.build();                   // Creates a CameraPosition from the builder
			map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		}		
	}

	/**
	 * The location tracker callback is set for this method.	 
	 */
	@SuppressWarnings("unused")
	public void drawMapLine(Location newLocation)
	{
		Log.d(TAG,"drawMapLine(..) newLocation:"+newLocation.toString());

		if( newLocation == null )
		{
			Log.d(TAG,"drawMapLine(..) new location is null.");
			return;
		}
		else if( mapLastLocation == null ) // Check for first time map line, after enabling a new tracking.
		{
			mapLastLocation = newLocation;
		}
		
		final String message = "Lat:"+newLocation.getLatitude()+" Long:"+newLocation.getLongitude()+
							   " Time:"+convertUtcTime(newLocation.getTime())+" Speed:"+newLocation.getSpeed();
		locationText.setText(message);
		locationText.invalidate();

		// Draw path to the new location.
		final LatLng newLatLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
		final LatLng lastLatLng = new LatLng(mapLastLocation.getLatitude(), mapLastLocation.getLongitude());
		map.addPolyline(new PolylineOptions()
				.geodesic(true)
				.color(Color.GREEN)
				.width(20)
				.add(lastLatLng)
				.add(newLatLng)
				);        

		// Move the camera to the new location.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, ZOOM_LEVE));

		mapLastLocation = newLocation;
	}
	
	public void clearMap()
	{
		if( map != null )
		{
			map.clear();
			mapLastLocation = null;
		}
	}
	
	public void setMapLastLocation(Location location)
	{
		this.mapLastLocation = location;
	}
	
	private String convertUtcTime(Long utcTime)
	{		
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		final int gmtOffset = TimeZone.getDefault().getRawOffset();		
		final long milliLocalTime = utcTime + gmtOffset - 2000*60*60; // Time offset from Israel GMT time.
		final String localTime = sdf.format(new Date(milliLocalTime));

		return localTime;
	}
	
	public void drawBackgroundLocations(List<Location> locationList)
	{
		Log.d(TAG,"drawBackgroundLocations(..)");		

		// Draw first line from last foreground point to first background point.
		if( mapLastLocation != null )
		{
			final LatLng lastForeLatLng = new LatLng(mapLastLocation.getLatitude(), mapLastLocation.getLongitude());			
			final LatLng firstBackLatLng = new LatLng(locationList.get(0).getLatitude(), locationList.get(0).getLongitude());
			map.addPolyline(new PolylineOptions()
			.geodesic(true)
			.color(Color.BLUE)
			.width(20)
			.add(lastForeLatLng)
			.add(firstBackLatLng));
		}

		// Draw background lines.
		for( int i=0; i < locationList.size()-1 ; i++ )
		{			
			final LatLng startLatLng = new LatLng(locationList.get(i).getLatitude(), locationList.get(i).getLongitude());			
			final LatLng endLatLng = new LatLng(locationList.get(i+1).getLatitude(), locationList.get(i+1).getLongitude());
			map.addPolyline(new PolylineOptions()
			.geodesic(true)
			.color(Color.BLUE)
			.width(20)
			.add(startLatLng)
			.add(endLatLng));
		}
		
		// Set the last background location as last map location.
		setMapLastLocation(locationList.get(locationList.size()-1));
	}
}

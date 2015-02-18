package home.green.test;

import home.yaron.location.LocationTracker;
import home.yaron.location.LocationTracker.LocationTrackerListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

/**
 * Record the tracking location events when the application is in the background.
 * 
 * @author Yaron Ronen - 17/01/2015 
 */
public class RecorderService extends Service 
{
	private final static String TAG = RecorderService.class.getSimpleName();	
	private final static String LOCATION_FILE_NAME = "greenLocations";	
	
	private BufferedWriter bufferedWriter;
	private File tempFile;

	// ------- Binding -------
	
	private final IBinder binder = new LocalBinder();		

	@Override
	public IBinder onBind(Intent intent) 
	{	
		Log.d(TAG,"onBind(..)");
		
		return binder;
	}

	public class LocalBinder extends Binder
	{
		RecorderService getService()
		{
			return RecorderService.this;
		}
	}
	
	@Override
	public boolean onUnbind(Intent intent)
	{		
		Log.d(TAG,"onUnbind(..)");		
		stopRecording();		
		return false;
	}

	// ------- Service methods -------

	public void startRecording()
	{
		Log.d(TAG,"startRecording(..)");
		
		// Open a recorder file to store the location data.
		openRecorderFile();

		// Set the callback for the location.
		LocationTracker.getInstance().setLocationTrackerListener(new LocationTrackerListener() {

			@Override
			public void onLocationTrackerChanged(final Location newLocation) {							

				// Post the writing to release the location listener.
				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {  
						final String message = "Latitude:"+newLocation.getLatitude()+" Longitude:"+newLocation.getLongitude()+
											   " Receiving Time:"+newLocation.getTime()+" Speed:"+newLocation.getSpeed();
						Log.d(TAG,"newLocation:"+message);
						
						writeLocationToFile(newLocation);
					}
				});				
			}});

		LocationTracker.startRequestLocation(this);
	}
	
	public void stopRecording()
	{
		Log.d(TAG,"stopRecording(..)");
		
		LocationTracker.stopRequestLocation(this);		
		closeRecorderFile();
	}
	
	private void openRecorderFile()
	{
		Log.d(TAG,"openRecorderFile(..)");		

		try
		{
			// Construct the BufferedOutputStream object.
			tempFile = File.createTempFile(LOCATION_FILE_NAME,null);						
			bufferedWriter = new BufferedWriter(new FileWriter(tempFile),1024);		
			Log.d(TAG,"Locations file created at:"+tempFile.getAbsolutePath());		
		}
		catch(Exception ex)
		{			
			Log.e(TAG,"problems with the recorder file.",ex);		
		}			
	}
	
	private void closeRecorderFile()
	{
		Log.d(TAG,"closeRecorderFile(..)");
		
		// Close the bufferedWriter
		try 
		{
			if( bufferedWriter != null )
			{
				bufferedWriter.flush();
				bufferedWriter.close();
			}
		} 
		catch(Exception ex) 
		{
			Log.e(TAG,"problems closing the recorder file.",ex);	
		}
		finally
		{
			bufferedWriter = null;
		}
	}
	
	private void writeLocationToFile(Location newLocation)
	{
		try
		{
			if( bufferedWriter != null && newLocation != null )
			{
				final String latitudeString = Double.toString(newLocation.getLatitude());
				final String longitudeString = Double.toString(newLocation.getLongitude());				

				// Writing to the writer.
				bufferedWriter.write(latitudeString+'\n');
				bufferedWriter.write(longitudeString+'\n');				
			}
		} 
		catch(Exception ex)
		{			
			Log.e(TAG,"problems writting recorder data.",ex);	
		}
	}
	
	public List<Location> readLocationsFromFile()
	{
		// Init
		final ArrayList<Location> locationList = new ArrayList<Location>();

		if( tempFile == null)
			return locationList;

		try
		{
			// Construct the BufferedOutputStream object.						
			final BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile));			

			// Reading the input stream.
			String aLine = null;			
			while( (aLine = bufferedReader.readLine()) != null )
			{
				final Location newLocation = new Location(""); //provider name is unnecessary
				newLocation.setLatitude(Double.parseDouble(aLine));
				newLocation.setLongitude(Double.parseDouble(bufferedReader.readLine()));
				locationList.add(newLocation);
			}

			bufferedReader.close();			

			Log.d(TAG,"Locations file read from:"+tempFile.getAbsolutePath());
		}
		catch(Exception ex)
		{			
			Log.e(TAG,"problems reading locatins file.",ex);		
		}

		return locationList;		
	}	
}

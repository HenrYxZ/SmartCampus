package com.scampus.views;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.scampus.especial1.R;
import com.scampus.tools.PoisSQLiteHelper;
import com.scampus.tools.User;
import com.scampus.tools.MenuHelper;


public class claimsActivity extends Activity implements OnItemSelectedListener {
	LatLng myLocation;
	Marker myposition;
	String lat;
	String lng;
	String description;
	Button sendButton;
	EditText claim;
	InputStream inputStream;
	String filepath;
	User current_user;
	Spinner my_spin;
	String[] claims;
	int type_id;
	String type_selected;
	Context context;
	Uri mCapturedImageURI;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	public static final int RESULT_LOAD_IMAGE= 300;
	private GoogleMap map;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//LE SACA EL TITULO A LA APP

		setContentView(R.layout.activity_claims); 

		context=this;
		setUpMapIfNeeded();

		current_user = new User(this);
		claim = (EditText )findViewById(R.id.claims_text);

		sendButton = (Button) findViewById(R.id.sendClaimButton);
		sendButton.setOnClickListener(new View.OnClickListener() {	  	 

			@Override  
			public void onClick(View v) {
				lat=String.valueOf(myposition.getPosition().latitude);
				lng=String.valueOf(myposition.getPosition().longitude);
				description= String.valueOf(claim.getText());

				PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context, "DBPois", null,1);;
				SQLiteDatabase db = sesdbh.getWritableDatabase();

				Cursor c = db.rawQuery(" SELECT id FROM Claim_types WHERE name_cat='"+type_selected+"'", null);


				if (c.moveToFirst()) {
					//Recorremos el cursor hasta que no haya más registros
					do {
						type_id= c.getInt(0);

					} while(c.moveToNext());
				}
				Log.e("IDDEL TIPO",String.valueOf(type_id));



				new uploadImage().execute();
				String waitMessage = "Denuncia enviada.";
				Toast.makeText(getApplicationContext(), 
						waitMessage,
						Toast.LENGTH_LONG).show();	            
			}
		});

		Intent intent = getIntent();

		claims = intent.getStringArrayExtra("claim_types");
		my_spin=(Spinner)findViewById(R.id.my_spinner);
		my_spin.setOnItemSelectedListener(this);
		ArrayAdapter aa=new ArrayAdapter(this, android.R.layout.simple_spinner_item,claims);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		my_spin.setAdapter(aa);


	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);


		if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_YES) {
			FragmentManager fm = getFragmentManager();
			fm.beginTransaction()
			.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
			.show((MapFragment)getFragmentManager().findFragmentById(R.id.map_claims))
			.commit();
			Log.i("CONFIG","aaaaaa");


		} else if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
			FragmentManager fm = getFragmentManager();
			fm.beginTransaction()
			.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
			.hide((MapFragment)getFragmentManager().findFragmentById(R.id.map_claims))
			.commit();
			Log.i("CONFIG","bbbbb");

		}
	}
	

	private void setUpMapIfNeeded() {
		if(map ==null){

			//Intenta obtener el mapa desde la API de google
			map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map_claims)).getMap();
			//Si es que lo logra lo configura dependiendo de como queramos
			if(map != null){
				setUpMap();
			}
		}

	}

	private void setUpMap() {
		map.getUiSettings().setZoomControlsEnabled(false);
		//Encuentra la ubicación actual del celular (No es muy preciso)
		map.setMyLocationEnabled(true);

		try{
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			Criteria criteria = new Criteria();

			String provider = locationManager.getBestProvider(criteria, false);
			Location latLongaux = locationManager.getLastKnownLocation(provider);

			myLocation=new LatLng(latLongaux.getLatitude(), latLongaux.getLongitude());
		}
		catch (Exception e)
		{
			Log.e("Location error: ","Can't stablish location");
			myLocation=new LatLng(-30.3741,-70.5715);
		}

		map.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
		map.animateCamera(CameraUpdateFactory.zoomTo(15));

		myposition = map.addMarker(new MarkerOptions()
		.title("Mi posición")
		.position(myLocation)
		.draggable(true));

	}

	public void onClick_Gallery(View v)
	{
		Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, RESULT_LOAD_IMAGE);


	}

	public void onClick_Camera(View v)
	{

		String fileName = "temp.jpg";  
		ContentValues values = new ContentValues();  
		values.put(MediaStore.Images.Media.TITLE, fileName);  
		mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);  

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);  
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

	}
	public void onClick_Video(View v)  
	{

		String fileName = "temp.jpg";  
		ContentValues values = new ContentValues();  
		values.put(MediaStore.Images.Media.TITLE, fileName);  
		mCapturedImageURI = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);  

		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);  
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);  
		startActivityForResult(intent,CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE );
//		
//		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//		intent.putExtra(MediaStore.EXTRA_OUTPUT, "video"); 
//		// start the image capture Intent
//		this.startActivityForResult(intent,CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE );
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String[] projection = { MediaStore.Images.Media.DATA}; 
				Cursor cursor = managedQuery(mCapturedImageURI, projection, null, null, null); 
				int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); 
				cursor.moveToFirst(); 
				filepath = cursor.getString(column_index_data);
				Toast.makeText(this, "Imagen adjuntada a la denuncia", Toast.LENGTH_LONG).show();
			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the image capture
			} else {
				// Image capture failed, advise user
			}
		}

		if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String[] projection = { MediaStore.Video.Media.DATA}; 
				Cursor cursor = managedQuery(mCapturedImageURI, projection, null, null, null); 
				int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA); 
				cursor.moveToFirst(); 
				filepath = cursor.getString(column_index_data);
				Toast.makeText(this, "Video adjuntado a la denuncia"+filepath , Toast.LENGTH_LONG).show();
			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the video capture
			} else {
				// Video capture failed, advise user
			}
		}
		if (requestCode == RESULT_LOAD_IMAGE) {
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = {MediaStore.Images.Media.DATA};

				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				Log.d("bls", "picturePath: " + picturePath);
				filepath = picturePath;


				cursor.close();
				Toast.makeText(this, "Imagen adjuntada a la denuncia", Toast.LENGTH_LONG).show();

			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the video capture
			} else {
				// Video capture failed, advise user
			}
		}
	}
	public class uploadImage extends AsyncTask<Object, Void, HttpEntity>{

		@Override
		protected HttpEntity doInBackground(Object... params){

			DefaultHttpClient client = new DefaultHttpClient();
			Log.i("bla", current_user.getApiToken());
			String url= "http://especial1.ing.puc.cl/api/make_complaint/"+current_user.getApiToken();
			Log.i("bla", "image_url: "+url);
			HttpPost post = new HttpPost(url);
			MultipartEntity imageMPentity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			try{                

				imageMPentity.addPart("campus_id", new StringBody("1"));
				imageMPentity.addPart("complaint[complaint_type_id]", new StringBody(String.valueOf(type_id)));
				imageMPentity.addPart("complaint[latitude]", new StringBody(lat));
				imageMPentity.addPart("complaint[longitude]", new StringBody(lng));
				imageMPentity.addPart("complaint[description]", new StringBody(description));
				if(filepath!=null)
					imageMPentity.addPart("complaint[photo]", new FileBody(new File(filepath)));    

				post.setEntity(imageMPentity);                

			} catch(Exception e){
				Log.e("kvx", e.getLocalizedMessage(), e);
			}
			HttpResponse response = null;

			try {
				response = client.execute(post);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HttpEntity result = response.getEntity();
			return result;
		}

		protected void onPostExecute(HttpEntity result){
			if(result !=null){
				// add whatever you want it to do next here
			}
		}     
	}
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
			long arg3) {
		type_selected=claims[pos];

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}
}
package com.scampus.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.scampus.uc.R;
import com.scampus.tools.PoisSQLiteHelper;
import com.scampus.tools.User;
import com.scampus.tools.dontShowAgain;


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
	
	
	private AsyncHttpClient asyncClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//LE SACA EL TITULO A LA APP

		setContentView(R.layout.activity_claims); 

		context=this;
		setUpMapIfNeeded();
		new dontShowAgain().dialog(this, "Usa el buscador para encontrar la ubicación de salas, puntos de " +
				"reciclaje, baños u otros puntos de interés. También puedes filtrarlos por categorías.");
		current_user = new User(this);
		claim = (EditText )findViewById(R.id.claims_text);
		claim.setHint("Escribe aquí tu denuncia, esta será enviada a "+current_user.getUniversity().getName());
		
		this.asyncClient = new AsyncHttpClient();

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
	
				uploadImage();
				
				LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
				View content = inflater.inflate(R.layout.dialog_claim, null );
				new AlertDialog.Builder(context) 
				.setTitle("Denuncia")
				.setView(content)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent resultIntent = new Intent();
						((Activity) context).setResult(Activity.RESULT_OK, resultIntent);
						((Activity) context).finish();

					}
				})
				.show();

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
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
			long arg3) {
		type_selected=claims[pos];

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}
	
	public void uploadImage() {
		
		 if (android.os.Build.VERSION.SDK_INT > 9) {
		        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		        StrictMode.setThreadPolicy(policy);
		    }
			
		// Manejo de respuesta de API en JSON
					ResponseHandlerInterface responseHandler = new JsonHttpResponseHandler() {
						@Override
			            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
							
							Log.i("Response", response.toString());
							try {
			                	String status = response.getString("message");
			                	if (status.equalsIgnoreCase("Error")) {
			                		
			                		
			                		Toast.makeText(context, "Lo lamentamos, no se pudo crear el"+
			                		"usuario, \n vuelve a intentarlo.", Toast.LENGTH_LONG).show();
			                	} else {
			                		
			                		Toast.makeText(context, "Felicidades, fuiste registrado!",
			                				Toast.LENGTH_SHORT).show();
			                	}
			                } catch (JSONException e){
			                	e.printStackTrace();
			                }
			            }

			            @Override
			            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
			            		JSONObject errorResponse) {
			                Log.e("Register", headers.toString());
			                Log.e("Register", ""+statusCode);
			                Log.e("Register", throwable.toString());
			                if (errorResponse != null) {
			                    Log.e("Register", errorResponse.toString());
			                }
			                //current_user.cleanUser(context);
			            }
					};

	
		Log.i("bla", current_user.getApiToken());
		String url=  context.getString(R.string.web_server_url)+"/api/make_complaint/"+current_user.getApiToken();
		Log.i("bla", "image_url: "+url);
	
		// Parametros del request
		RequestParams params = new RequestParams();
		params.put("campus_id",current_user.getCampus().getID());
		params.put("complaint[complaint_type_id]",  String.valueOf(type_id));
		params.put("complaint[latitude]",lat);
		params.put("complaint[longitude]", lng);
		params.put("complaint[description]", description);
		
		//Log.e("Filepath",filepath);
		if(filepath!=null)
		{
			Log.e("ENTRO aca","qwert");
			if(filepath.contains(".jpg")||filepath.contains(".png"))
			{
				Log.e("ENTRO FOTO","ok");
				try {
					params.put("complaint[photo]", new File(filepath), "image/jpg");
				} catch (FileNotFoundException e){
					Log.e("Register", e.getMessage());
				}
			}
			if(filepath.contains(".3gp")||filepath.contains(".3gpp")||filepath.contains(".mp4"))
			{
				Log.e("ENTRO VIDEO","ok");
				try {
					params.put("complaint[video]", new File(filepath), "video/3gp");
				} catch (FileNotFoundException e){
					Log.e("Complaint", e.getMessage());
				}
			}
			asyncClient.post(url, params, responseHandler);
		}
		else{
			Toast.makeText(this, "Debes adjuntar una imagen o video", Toast.LENGTH_LONG).show();
		}
		//post.setEntity(imageMPentity);     
		// Se envia el POST!
		


}
}
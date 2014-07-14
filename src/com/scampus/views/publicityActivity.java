package com.scampus.views;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.google.android.gms.plus.PlusShare;
import com.scampus.uc.R;
import com.scampus.tools.User;
import com.scampus.tools.DBHelper;
import com.scampus.tools.PlaceDetails;
import com.scampus.uc.DiskBitmapCache;


public class publicityActivity extends Activity{
	private String src;//la fuente de la imagen a mostrar
	private ImageView image;
	private ImageLoader imageLoader;
	private RequestQueue requestQueue; //volley
	private Button shareButton; //para facebook
	//para compartir en facebook
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
	private String name;
	private String type;
	private String link;
	EditText comment;
	private User current_user;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publicity);
		
		
		requestQueue = Volley.newRequestQueue(this);
     	imageLoader = new ImageLoader(requestQueue, new DiskBitmapCache(this.getCacheDir()));
     	//el bundle es para pasar paramtros al iniciar una nueva actividad
		Bundle b = getIntent().getExtras();
		//recuperamos el parametro de la siguiente manera
		src = b.getString("image_source");
		name = b.getString("image_name");
		type = b.getString("image_type");
		link = b.getString("link");
		// TODO Poner el link en la img
		final int event_id = b.getInt("event_id");
		
		current_user = new User(this);
		
		//si el tipo es EVENT mostramos el boton para ir hacia el mapa
		Button goToMap = (Button)this.findViewById(R.id.showMap);
		Button go_to_publicity_link = (Button)this.findViewById(R.id.go_to_publicity_link);
		
		if(!type.equalsIgnoreCase("event")){
			
			goToMap.setVisibility(View.INVISIBLE);
			
			go_to_publicity_link.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					if (link.isEmpty())
						return;
					Intent intent = new Intent();
			        intent.setAction(Intent.ACTION_VIEW);
			        intent.addCategory(Intent.CATEGORY_BROWSABLE);
			        intent.setData(Uri.parse(link));
			        startActivity(intent);
					
				}
			});
		}
		else{
			
			go_to_publicity_link.setVisibility(View.INVISIBLE);
			
			goToMap.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					DBHelper dbHelper = new DBHelper(publicityActivity.this);
					PlaceDetails place = dbHelper.getEventByName(publicityActivity.this,
							name);
					Intent intent = new Intent(publicityActivity.this,
							markerDetailsActivity.class);
					intent.putExtra("placeTag", place);
					startActivity(intent);	
					
				}
			});
		}
		
		
		//buscamos la imagen
		this.image = (ImageView)findViewById(R.id.bigImage);
		//cargamos la imagen
	  	imageLoader.get(this.src, ImageLoader.getImageListener(image, R.drawable.transparent, R.drawable.loadingerror));
	  	//TODO cambiar el logo de error
	  	this.image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (type.equals("event")) {
					DBHelper dbHelper = new DBHelper(publicityActivity.this);
					PlaceDetails place = dbHelper.getEventByName(publicityActivity.this,
							name);
					Intent intent = new Intent(publicityActivity.this,
							markerDetailsActivity.class);
					intent.putExtra("placeTag", place);
					startActivity(intent);
				} else {
					if (link.isEmpty())
						return;
					Intent intent = new Intent();
			        intent.setAction(Intent.ACTION_VIEW);
			        intent.addCategory(Intent.CATEGORY_BROWSABLE);
			        intent.setData(Uri.parse(link));
			        startActivity(intent);
				}
			}
		});
	  	
	  	TextView title = (TextView) this.findViewById(R.id.publicityTitle);
	  	title.setText(name);
	  	comment = (EditText) this.findViewById(R.id.shareText);
	  	
	  	if(current_user.getProvider().equalsIgnoreCase("google")){
	  		shareButton = (Button) findViewById(R.id.shareButton_google);
	  		comment.setHint(R.string.google_share_hint);;
	  	}
	  	else {
	  		shareButton = (Button) findViewById(R.id.shareButton_facebook);
	  		comment.setHint(R.string.fb_share_hint);
	    }
	  	
	  	shareButton.setVisibility(View.VISIBLE);

	  	shareButton.setOnClickListener(new View.OnClickListener() {
	  	 
	  	  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	  	 @Override  
	  	  public void onClick(View v) {
	  	    	NetworkInfo ni = cm.getActiveNetworkInfo();
	  	    	if (ni == null) {
	  	    		String waitMessage = "Debes estar conectado a internet para compartir";
	  	            Toast.makeText(getApplicationContext(), 
	  	                 waitMessage,
	  	                 Toast.LENGTH_LONG).show();	        
	  	    	}
	  	    	else if(current_user.getProvider().equalsIgnoreCase("google")){
	  	    		publishStoryGoogle();  
	  	    	}
	  	    	else if(current_user.getProvider().equalsIgnoreCase("native")){
	  	    		
	  	    	}
	  	    	else{
	  	    		publishStoryFacebook();  
	  	    	}
	  	    }
	  	});	
	}
	

	protected void publishStoryGoogle() {
		
		Intent shareIntent = new PlusShare.Builder(this)
		//.setContentUrl(Uri.parse(this.getString(R.string.web_server_url)))
        .setText(this.comment.getText().toString())
        .setType("image/png")
        .setContentDeepLinkId("testID",
                this.name,
                "Test Description",
                Uri.parse(this.src))
        .getIntent();
		startActivityForResult(shareIntent, 0);
		
		this.finish();
		
	}


	private void publishStoryFacebook() {
	    Session session = Session.getActiveSession();

	    if (session != null){

	        // Check for publish permissions    
	        List<String> permissions = session.getPermissions();
	        if (!isSubsetOf(PERMISSIONS, permissions)) {
	            pendingPublishReauthorization = true;
	            Session.NewPermissionsRequest newPermissionsRequest = new Session
	                    .NewPermissionsRequest(this, PERMISSIONS);
	            session.requestNewPublishPermissions(newPermissionsRequest);
	            return; 
	        }
	        else {

	        Bundle postParams = new Bundle();
	        String texto = "";
	        if(comment.getText()!=null) texto = comment.getText().toString();
	        postParams.putString("name", name);
	        postParams.putString("caption", "Enviado a traves de SmartCampus UC"); //TODO cambiar el caption
	        postParams.putString("description", texto);
	        postParams.putString("link", "http://smartcampus.ing.puc.cl"); //TODO cambiar el link
	        postParams.putString("picture", this.src);

	        Request.Callback callback= new Request.Callback() {
	            public void onCompleted(Response response) {
	                JSONObject graphResponse = response
	                                           .getGraphObject()
	                                           .getInnerJSONObject();
	                String postId = null;
	                try {
	                    postId = graphResponse.getString("id");
	                } catch (JSONException e) {
	                    Log.i("FACEBOOK",
	                        "JSON error "+ e.getMessage());
	                }
	                FacebookRequestError error = response.getError();
	                if (error != null) {
	                    Toast.makeText(getApplicationContext(),
	                         error.getErrorMessage(),
	                         Toast.LENGTH_SHORT).show();
	                    } else {
	                    	String succesMessage = "Publicado con éxito";
	                        Toast.makeText(getApplicationContext(), 
	                             succesMessage,
	                             Toast.LENGTH_LONG).show();
	                        
	                }
	            }
	        };

	        Request request = new Request(session, "me/feed", postParams, 
	                              HttpMethod.POST, callback);

	        RequestAsyncTask task = new RequestAsyncTask(request);
	        task.execute();
	        String waitMessage = "Publicando en Facebook";
            Toast.makeText(getApplicationContext(), 
                 waitMessage,
                 Toast.LENGTH_LONG).show();	    
            this.finish();
	    }
	    }

	}

	
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
	    for (String string : subset) {
	        if (!superset.contains(string)) {
	            return false;
	        }
	    }
	    return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	   Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);

	}
	
}

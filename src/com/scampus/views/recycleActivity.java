//Esta clase maneja el resultadod del scanner, mostrando el mensaje obtenido del servidor.
//NOTA: Si se quiere cambiar el fragmento del scanner ir a la clase QRScaner

package com.scampus.views;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.scampus.uc.R;

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
import android.widget.TextView;
import android.widget.Toast;

import com.scampus.uc.R;
import com.scampus.tools.User;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.google.android.gms.plus.PlusShare;



public class recycleActivity extends Activity {
	private Button shareButton;
	private String title;
	private String textToShow;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
	private User current_user;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recycle);
		
		String value = getIntent().getStringExtra("text");
		String type = getIntent().getStringExtra("type");
		if(type.equalsIgnoreCase("E")){
			title = "Checkin con Smart Campus";
		}
		else{
			title = "Reciclaje con Smart Campus";
		}
		TextView a =(TextView)findViewById(R.id.recycleText);
		a.setText(value);
		
		current_user = new User(this);
		
		if(current_user.getProvider().equalsIgnoreCase("google")){
	  		shareButton = (Button) findViewById(R.id.googleshareQRButton);
	  	}
	  	else {
	  		shareButton = (Button) findViewById(R.id.fbshareQRButton);
	    }
		shareButton.setVisibility(View.VISIBLE);
		
	  	shareButton.setOnClickListener(new View.OnClickListener() {
	  	 
	  	  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	  	 @Override  
	  	  public void onClick(View v) {
	  	    	NetworkInfo ni = cm.getActiveNetworkInfo();
	  	    	if (ni == null) {
	  	    		String waitMessage = "Debes estar conectado a internet para compartir en Facebook";
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
		.setContentUrl(Uri.parse(this.getString(R.string.web_server_url)))
        .setText(this.title)
        .setType("image/png")
        .setContentDeepLinkId("testID",
                this.title,
                "Ven a compartir y disfrutar de la sustentabilidad en la uc",
                Uri.parse("http://smartcampus-user.herokuapp.com/"))
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

	        Bundle postParams = new Bundle();
	        
	        postParams.putString("name", title);
	        postParams.putString("caption", "");
	        postParams.putString("description", "Ven a compartir y disfrutar de la sustentabilidad en la uc");
	        postParams.putString("link", "http://smartcampus-user.herokuapp.com/");

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
	    }
	    
	    this.finish();

	}
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
	    for (String string : subset) {
	        if (!superset.contains(string)) {
	            return false;
	        }
	    }
	    return true;
	}
}
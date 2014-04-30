//Esta clase maneja el resultadod del scanner, mostrando el mensaje obtenido del servidor.
//NOTA: Si se quiere cambiar el fragmento del scanner ir a la clase QRScaner

package com.scampus.views;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.scampus.especial1.R;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.scampus.especial1.R;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;



public class recycleActivity extends Activity {
	private Button shareButton;
	private String title;
	private String textToShow;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
	
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
		
		shareButton = (Button) findViewById(R.id.shareQRButton);
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
	  	    	else 
	  	        publishStory();        
	  	    }
	  	});
		
	}
	
	private void publishStory() {
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
	        postParams.putString("link", "http://smartcampus.ing.puc.cl");

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
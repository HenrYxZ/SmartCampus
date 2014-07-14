package com.scampus.views;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.scampus.uc.R;
import com.scampus.tools.User;


public class facebookLoginActivity extends FragmentActivity {

	//esta variable es para hacer un listener que escuacha cuando hay un cambio en el estado de la sesion de FB
	private UiLifecycleHelper uiHelper;
	private RequestQueue requestQueue; //volley para mandar requests al servidor
	private User current_user;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private boolean asked_once = false;
	Context context;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facebooklogin);
		
		
		// start Facebook Login
		Session.openActiveSession(this, true, callback);
		context = this;
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		//para manejar los tres fragmentos se usa FragmentManager

		current_user = new User(this);
		
		requestQueue = Volley.newRequestQueue(this);
		requestQueue.cancelAll("VOLLEY");

	}

	private void requestPublishPermissions() {
		Session session = Session.getActiveSession();
		if (session != null && !asked_once){

			// Check for publish permissions    
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				Session.NewPermissionsRequest newPermissionsRequest = new Session
						.NewPermissionsRequest(this, PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);

			}
			asked_once = true;
		}
	}


	private void goToInitialSetting() {
		current_user.retrieveUser(this);
		if((current_user.hasApiToken(this) && !current_user.hasUniversity()) ||  (current_user.hasApiToken(this) && !current_user.hasCampus())){
			Intent i = new Intent(this, initSetActivity.class);
			startActivity(i);
			//finish();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();//FACEBOOK
		current_user = new User(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		//metodo del UiHelper
		uiHelper.onPause();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	//este metodo se llama cuando hay algun cambio en la sesion de facebook para manejar los problemas
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		// Solo hace cambios si la actividad esta visible
		current_user = new User(this);
		if (state.isOpened()) {
			
			

			//VALIDAMOS CON EL SERVIDOR QUE EL USUARIO QUE INICIA SESION ES VALIDO
			Log.i("SESSION", "Logged in...");
			// TODO: Agregar progress dialog
			this.validateUser(session,this);

			if(current_user.hasUniversity() && current_user.hasCampus()){
				// Mostramos el menu princiapl de la aplicacion (MainActivity)
				//startActivity(new Intent(this, MainActivity.class));
			}
			else if(!current_user.hasUniversity() || !current_user.hasCampus()){

				//this.goToInitialSetting();
			}
		}
	}

	//este es un listenter que avisa cuando hay un cambio de estado en la sesion
	//este metodo hace overide del metodo call()

	private Session.StatusCallback callback = 

			new Session.StatusCallback() {
		@Override
		public void call(Session session, 
				SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private void validateUser(Session session, final Context context) {
		JSONObject object = new JSONObject();
		try {
			object.put("access_token", session.getAccessToken());
		} catch (JSONException e1) {

			e1.printStackTrace();
		}

		String url = getString(R.string.web_server_url) + "/mobile_users/login";

		JsonObjectRequest jr = new JsonObjectRequest(com.android.volley.Request.Method.POST,url,object,new com.android.volley.Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				Log.i("Volley",response.toString());
				//				if(response.get("authenticated") == "true")
				//				validatedUser = true;
				//Como la respuesta es efectiva y se ha validado el usuario, creamos al usuario.

				try {
					if(current_user == null && response.getString("authenticated")=="true"){
						current_user= new User();
						current_user.setApiToken(response.getString("api_token"));
						current_user.saveUser(context);
						Log.i("Volley","Se creo un nuevo usuario");
					}
					else
					{
						current_user.setApiToken(response.getString("api_token"));
						current_user.saveUser(context);
						Log.i("Volley","Se reutilizo el usuario");
						Log.e("APITOKEN",current_user.getApiToken());

					}
					
					goToInitialSetting();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		},new com.android.volley.Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("VOLLEY","");
			}
		});
		requestQueue.add(jr);
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








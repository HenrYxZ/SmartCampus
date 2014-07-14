package com.scampus.views;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.Session;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.scampus.uc.R;
import com.scampus.tools.User;

import android.view.View.OnClickListener;

public class googleSignInActivity extends Activity implements OnClickListener,
ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<LoadPeopleResult> {
	private static final int RC_SIGN_IN = 0;
    // Logcat tag
    private static final String TAG = "MainActivity";
 
    // Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 400;
    
    //SCOPES para access token
    public static final String SCOPES ="https://www.googleapis.com/auth/plus.login "+"https://www.googleapis.com/auth/userinfo.email ";
    
    public static int REQUEST_CODE_TOKEN_AUTH = 9001;
 
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
 
    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;
 
    private boolean mSignInClicked;
    private boolean mfirstTime;
    private boolean accessTokenBolean;
 
    private ConnectionResult mConnectionResult;
 
    private SignInButton btnSignIn;
    private Button btnSignOut, btnRevokeAccess;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail;
    private LinearLayout llProfileLayout;
    
    private User current_user;
    private RequestQueue requestQueue; //volley para mandar requests al servidor
    public String accessToken;
    private boolean logout;
    
    private ProgressDialog progress;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlelogin);
        
        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        llProfileLayout = (LinearLayout) findViewById(R.id.llProfile);
        
        mfirstTime = true;
        accessTokenBolean = true;
        
        requestQueue = Volley.newRequestQueue(this);
		requestQueue.cancelAll("VOLLEY");
		
		accessToken = "";
		
		
        // Button click listeners
       btnSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
            	signInWithGplus();
            }
        });
        btnSignOut.setOnClickListener(new View.OnClickListener() {
        	
            @Override
            public void onClick(View view) {
            	signOutFromGplus();
            }
        });
        btnRevokeAccess.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
            	revokeGplusAccess();
            }
        });
 
       // Initializing google plus api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API, null)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
        
        Bundle b = getIntent().getExtras();
		//recuperamos el parametro de la siguiente manera
        
		logout = b.getBoolean("logout");
    
    }
 
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        
    }
 
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    
    protected void onResume() {
        super.onResume();
       
    }
    protected void onPostResume(){
    	super.onPostResume();
    	
    }
 
    /**
     * Button on click listener
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_sign_in:
            // Signin button clicked
            signInWithGplus();
            break;
        case R.id.btn_sign_out:
            // Signout button clicked
            signOutFromGplus();
            break;
        case R.id.btn_revoke_access:
            // Revoke access button clicked
            revokeGplusAccess();
            break;
        }
    }
    
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }
        
        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;
     
            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
            	mSignInClicked = false;
                resolveSignInError();
            }
        }
        
        //ACTIVAMOS AQUI PARA QUE SALGA AUTOMATICAMENTE!
        if(!mfirstTime){
        	Intent i = new Intent(this, loginActivity.class);
			startActivity(i);
			finish();
        }
        else{
        	signInWithGplus();
        	mfirstTime=false;
        }
     
    }
     
    @Override
    protected void onActivityResult(int requestCode, int responseCode,
            Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }
     
            mIntentInProgress = false;
            
            /*if (accessTokenBolean==false) {
                getToken();
            }*/
     
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
        
            
       
    }
     
    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        //Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
        
        //Si viene desde main activity entonces se desconecta.
        if(logout==true){
        	this.signOutFromGplus();
        }
     
        // Get user's information
       //else if(accessTokenBolean){
        else{
        	this.getToken();
	        accessTokenBolean = false;
	        
	        progress = ProgressDialog.show(this,
	    			"Verificando..", null, true);
        }
        //}
        // Update the UI after signin
        //updateUI(true);
    }
     
    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
        //updateUI(false);
    }
     
    /**
     * Updating the UI, showing/hiding buttons and profile layout
     * */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevokeAccess.setVisibility(View.VISIBLE);
            llProfileLayout.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
            llProfileLayout.setVisibility(View.GONE);
        }
    }
    
    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }
     
    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }
    
    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
        	
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            	
            	current_user = new User(this);
            	Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
            	
               // Log.i("Access Token", accessToken);
                
                /*if(accessToken == null){
                	this.getToken();
                	return;
                }*/
                

                	this.validateUser(currentPerson, this);
     
            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     
    
    
    /**
     * Sign-out from google
     * */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            //mGoogleApiClient.connect();
            //borramos por completo el usuario 
            current_user = new User(this);
            current_user.cleanUser(this);
            
            Intent i = new Intent(this, loginActivity.class);
			startActivity(i);
			finish();
            
        }
    }
    
    /**
     * Revoking access from google
     * */
    private void revokeGplusAccess() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e(TAG, "User access revoked!");
                            mGoogleApiClient.connect();
                            //updateUI(false);
                        }
     
                    });
        }
    }

	@Override
	public void onResult(LoadPeopleResult arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void validateUser(Person person, final Context context) {
		
		JSONObject object = new JSONObject();
		try {
			object.put("access_token", this.accessToken);
			object.put("provider", "google_oauth2");
		} catch (JSONException e1) {

			e1.printStackTrace();
		}
		
		Log.i("JSON",object.toString());

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
						current_user.setProvider("google");
						current_user.saveUser(context);
						Log.i("Volley","Se creo un nuevo usuario");
					}
					else
					{
						current_user.setApiToken(response.getString("api_token"));
						current_user.setProvider("google");
						current_user.saveUser(context);
						Log.i("Volley","Se reutilizo el usuario");
						Log.e("APITOKEN",current_user.getApiToken());

					}
					
					setProfileInformation();
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

	protected void setProfileInformation() {
		
		if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
        	
        	Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        	
        	String personName = currentPerson.getDisplayName();
            String personPhotoUrl = currentPerson.getImage().getUrl();
            String personGooglePlusProfile = currentPerson.getUrl();
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
 
            // by default the profile url gives 50x50 px image only
            // we can replace the value with whatever dimension we want by
            // replacing sz=X
            personPhotoUrl = personPhotoUrl.substring(0,
                    personPhotoUrl.length() - 2)
                    + PROFILE_PIC_SIZE;
            
            current_user.setFirstName(personName);
            current_user.setEmail(email);
            current_user.setProfilePhotoUrl(personPhotoUrl);
            current_user.saveUser(this);
		}
	}

	private void goToInitialSetting() {
		current_user.retrieveUser(this);
		if((current_user.hasApiToken(this) && !current_user.hasUniversity()) ||  (current_user.hasApiToken(this) && !current_user.hasCampus())){
			Intent i = new Intent(this, initSetActivity.class);
			progress.dismiss();
			startActivity(i);
			finish();
		}
	}
	
	
	private void getToken(){
		
		final Context c = this;
    	
    	AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String token = null;
                try {
                    token = GoogleAuthUtil.getToken(
                    		 c,
                             Plus.AccountApi.getAccountName(mGoogleApiClient),
                             "oauth2: " + SCOPES);
                    
                    Log.i("account name",Plus.AccountApi.getAccountName(mGoogleApiClient));
		
                } catch (IOException transientEx) {
                    // Network or server error, try later
                    Log.e(TAG, transientEx.toString());
                } catch (UserRecoverableAuthException e) {	
                    // Recover (with e.getIntent())
                    Log.e(TAG, e.toString());
                    Intent recover = e.getIntent();
                    startActivityForResult(recover, REQUEST_CODE_TOKEN_AUTH);
                    
                } catch (GoogleAuthException authEx) {
                    // The call is not ever expected to succeed
                    // assuming you have already verified that 
                    // Google Play services is installed.
                    Log.e(TAG, authEx.toString());
                }
                
                
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
            	accessToken = token;
                Log.i(TAG, "Access token retrieved:" + token);  
                if(token!=null){
                	getProfileInformation();
                }
            }

        };
       task.execute();  
       
	}
    
}

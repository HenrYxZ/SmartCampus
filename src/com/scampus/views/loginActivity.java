package com.scampus.views;

import java.util.concurrent.Callable;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.scampus.uc.R;
import com.scampus.tools.RequestHandler;
import com.scampus.tools.User;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class loginActivity extends Activity{

	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;
	private RequestHandler requestHandler;
	private User current_user;
	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private Button mLoginFacebook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		
		current_user = new User(this);
		// Si el usuario ya está registrado mandarlo a MainActivity
		if (!current_user.getApiToken().isEmpty()) {
			if (current_user.hasCampus())
				startActivity(new Intent(this, MainActivity.class));
			else
				startActivity(new Intent(this, initSetActivity.class));
			finish();
		}
		RequestQueue requestQueue = Volley.newRequestQueue(this);
		requestHandler = new RequestHandler(requestQueue);
		
		// Set up the login form.

		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		// Set up buttons
		
		mLoginFacebook = (Button) findViewById(R.id.fb_login_button);
		// esto es para mandar login default de facebook
		// mLoginFacebook.setReadPermissions(Arrays.asList("email")); 
		mLoginFacebook.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(v.getContext(), facebookLoginActivity.class));
				finish();
			}
		});
		
		
		
		checkConnectivity(this);
		
		findViewById(R.id.login_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		
		findViewById(R.id.registration_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(v.getContext(), RegisterActivity.class);
						startActivity(i);
						finish();
					}
				});
		
		//Click on google Button
				findViewById(R.id.google_login_button).setOnClickListener(
						new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent i = new Intent(v.getContext(), googleSignInActivity.class);
								Bundle b = new Bundle();					
								b.putBoolean("logout", false);
								i.putExtras(b);
								startActivity(i);	
								finish();
							}
						});
		
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	private void showProgress(final boolean show) {
	
		// The ViewPropertyAnimator APIs are not available, so simply show
		// and hide the relevant UI components.
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		
	}
	
	private final boolean checkConnectivity(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	  	NetworkInfo ni = cm.getActiveNetworkInfo();
	  	if (ni == null) {
	  		
	  		String waitMessage = "Tu conexión a internet está lenta, revisa que estés conectado a una red Wifi o 3G";
	          Toast.makeText(context, 
	               waitMessage,
	               Toast.LENGTH_LONG).show();	 
	          return false;
	  	}
	  	return true;
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			current_user.setEmail(mEmail);
			if (current_user.getAccessToken().isEmpty())
				requestHandler.retrieveNativeUser(current_user, mPassword,
						loginActivity.this, new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								boolean success = true;
								if (current_user.getAccessToken().isEmpty())
									success = false;
								responseAccessToken(success);
								return null;
							}
						});
			else
				requestHandler.sendNativeLogin(current_user, loginActivity.this,
						mPassword, new Callable<Void>(){
							public Void call(){
								if (current_user.getApiToken().isEmpty())
									responseLogin(false);
								else
									responseLogin(true);
								return null;
							}});
			return true;
		}
			

		@Override
		protected void onPostExecute(final Boolean success) {
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
	
	public void responseAccessToken (boolean success) {
		if (success) {
			// Para que el servidor no tire excepcion 406 por hacer request muy seguidos
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			requestHandler.sendNativeLogin(current_user, loginActivity.this,
					mPassword, new Callable<Void>(){
						public Void call(){
							if (current_user.getApiToken().isEmpty())
								responseLogin(false);
							else
								responseLogin(true);
							return null;
						}});
		} else {
			Toast.makeText(loginActivity.this, "Usuario o password inválido",
				Toast.LENGTH_LONG).show();
			showProgress(false);
			mAuthTask = null;
		}
	}
		
	public void responseLogin (boolean success) {
		showProgress(false);

		if (success) {
			Toast.makeText(loginActivity.this, 
					loginActivity.this.getString(R.string.toast_validated),
					Toast.LENGTH_SHORT).show();
			Intent i;
			if (current_user.hasCampus())
				i = new Intent(loginActivity.this, MainActivity.class);
			else {
				requestHandler.requestUserCampusInfo(current_user, this);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (current_user.hasCampus())
					i = new Intent(loginActivity.this, MainActivity.class);
				else
					i = new Intent(loginActivity.this, initSetActivity.class);
			}
				
			startActivity(i);
			finish();
		} else {
			Toast.makeText(loginActivity.this, "Usuario o password inválido",
					Toast.LENGTH_LONG).show();
			mAuthTask = null;
		}
	}
}

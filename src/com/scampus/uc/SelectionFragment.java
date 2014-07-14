package com.scampus.uc;

import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.scampus.uc.R;
import com.scampus.tools.User;
import com.scampus.views.accountActivity;

public class SelectionFragment extends Fragment{
	
//	private static final String TAG = "SelectionFragment";
	private ProfilePictureView profilePictureViewFacebook;
	private ImageView profilePictureViewGoogle;
	private TextView userNameView;	
	private UiLifecycleHelper uiHelper;
	//se usa cuando se hacen nuevos request
	private static final int REAUTH_ACTIVITY_CODE = 100;
//	private RequestQueue requestQueue;
	private Context context;
	User current_user;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.selection1, 
	            container, false);
	    
	    context = this.getActivity();
	    current_user = new User(context);
	    
	    Log.i("PROVIDER", current_user.getProvider());
	    boolean isNative = current_user.getProvider().equalsIgnoreCase("native");
	    boolean isGoogle = current_user.getProvider().equalsIgnoreCase("google");
	    if(isGoogle || isNative){
	    	
	    	profilePictureViewGoogle = (ImageView) view.findViewById(R.id.selection_profile_pic_google);
	    	profilePictureViewGoogle.setVisibility(View.VISIBLE);
	    	profilePictureViewGoogle.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(context, accountActivity.class);
					startActivity(i);					
				}
			});
	    	
	    	
	    	new LoadProfileImage(profilePictureViewGoogle).execute(current_user.getProfilePhotoUrl());
  	
	    } else {
	    	//ES FACEBOOK
	    	profilePictureViewFacebook = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic_facebook);
	    	profilePictureViewFacebook.setCropped(true);
	    	profilePictureViewFacebook.setVisibility(View.VISIBLE);
	     	
	    	profilePictureViewFacebook.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(context, accountActivity.class);
					startActivity(i);					
				}
			});
			
	    	// Se busca un sesion que este abierta
	    	Session session = Session.getActiveSession();
		    
		    if (session != null && session.isOpened()) {
		        // Hacemos un rerquest para obtener los datos del usuario
		    	//El metodo se define mas abajo
		    	makeMeRequest(session);	
		    }
	    }
	    
	    return view;
	}
	
	//este metodo es el que pide la informacion del usuario
	private void makeMeRequest(final Session session) {
	    // Hace el llamado a la API
	    // define un nuevo callback para mnejar la respuesta.
	    Request request = Request.newMeRequest(session, 
	            new Request.GraphUserCallback() {
	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            // Si la respuesta es positiva
	            if (session == Session.getActiveSession()) {
	                if (user != null) {
	                	//aqui se baja y se muestra la imagen del usuario en la vista principal
	                	profilePictureViewFacebook.setProfileId(user.getId());
	                	profilePictureViewFacebook.setPresetSize(ProfilePictureView.LARGE);
	                    User current_user = new User(context);
	                    current_user.setFirstName(user.getFirstName()+" "+user.getMiddleName());
	                    current_user.setLastName(user.getLastName());
	                    current_user.saveUser(context);
	                    

	                }
	            }
	            if (response.getError() != null) {
	                // TODO Aqui tenemos que manejar los errores
	            	Log.i("SESSION", "Problemas al hacer request para encontrar datos del usuario, desplegando mensaje...");
	            	
	            }
	        }
	    });
	    request.executeAsync();
	} 
	
	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
	    if (session != null && session.isOpened()) {
	        // Cuando hay cambios en el estado de la sesion buscamos denuevo los datos del usuario.
        	Log.i("SESSION", "Hubo un cambio en el estado de la sesion...");
	        makeMeRequest(session);
	    }
	}
			


	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(final Session session, final SessionState state, final Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);      
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);

	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == REAUTH_ACTIVITY_CODE) {
	        uiHelper.onActivityResult(requestCode, resultCode, data);
	    }
	}

	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();

	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
	    super.onSaveInstanceState(bundle);
	    uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
	
	/**
     * Background Async task to load user profile picture from url GOOGLE+
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
     
        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }
     
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }
     
        protected void onPostExecute(Bitmap result) {
        	bmImage.setScaleType(ScaleType.CENTER_CROP);
            bmImage.setImageBitmap(result);
        }
    }

}

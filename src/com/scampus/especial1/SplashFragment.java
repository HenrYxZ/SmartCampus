package com.scampus.especial1;

import java.util.Arrays;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.SessionLoginBehavior;
import com.facebook.widget.LoginButton;
//ESTE FRAGMENTE CORRE CUANDO EL USUARIO NO ESTA REGISTRADO o NO HA INICIADO SESION
public class SplashFragment extends Fragment{
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.splash, 
	            container, false);
	    //Al login button se agregan los permisos necesarios en las siguientes lineas.
	    LoginButton authButton = (LoginButton) view.findViewById(R.id.login_button);
	    authButton.setText(R.string.loginButtonText);
	    authButton.setReadPermissions(Arrays.asList("email"));
				    
	    checkConnectivity(getActivity());
	    
	    
	    
	    return view;
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
}

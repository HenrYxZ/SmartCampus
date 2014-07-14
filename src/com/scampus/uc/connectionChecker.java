package com.scampus.uc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class connectionChecker {
	
	Context context;
	
	public connectionChecker(Context context){
		this.context = context;
	}
	
	public boolean checkConnectivity(){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	  	NetworkInfo ni = cm.getActiveNetworkInfo();
	  	if (ni == null) {
	  		
	  		String waitMessage = "No estás conectado a intenet.";
	          Toast.makeText(context, 
	               waitMessage,
	               Toast.LENGTH_LONG).show();	 
	          return false;
	  	}
	  	return true;
	}

}


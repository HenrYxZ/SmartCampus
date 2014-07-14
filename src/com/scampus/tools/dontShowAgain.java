package com.scampus.tools;



import com.scampus.uc.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class dontShowAgain {
	
	public void dialog(final Activity activity, String input_text)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(activity);
		boolean dialog_status = prefs
				.getBoolean("dialog_status", false);//get the status of the dialog from preferences, if false you ,ust show the dialog
		if (!dialog_status) {
			LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		
			View content = inflater.inflate( R.layout.dialog_content, null );
			final TextView textview = (TextView) content.findViewById( R.id.dsatext );
			final CheckBox userCheck = (CheckBox) content.findViewById( R.id.dsacheck );
			
			textview.setText(input_text);
			//build the dialog
			new AlertDialog.Builder(activity) 
			
			.setView(content)
			.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {

				public void onClick(
						DialogInterface dialog,
						int which) {
					//find our if the user checked the checkbox and put true in the preferences so we don't show the dialog again 
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(activity);
					SharedPreferences.Editor editor = prefs
							.edit();
					editor.putBoolean("dialog_status",
							userCheck.isChecked());
					editor.commit();
					dialog.dismiss(); //end the dialog.
				}
			}).show();
			



		}
		
	}
	
	

}

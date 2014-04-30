package com.scampus.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.scampus.especial1.R;

public class initialDialog {


	private RadioGroup select;
	private RadioButton b;
	public void dialog(final Activity activity, final User current_user)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(activity);
		boolean dialog_status = prefs
				.getBoolean("initial_dialog", false);//get the status of the dialog from preferences, if false you ,just show the dialog
		if (!dialog_status) {
			setFirstMessage(activity, current_user);

		}

	}

	private void setFirstMessage(final Activity activity,
			final User current_user) {
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

		View content = inflater.inflate( R.layout.initial_dialog, null );

		// Gets a reference to our radio group
		// rBtnDigits is the name of our radio group (code not shown)
		select = (RadioGroup) content.findViewById(R.id.universityRadioGroup); 
		int selected = select.getCheckedRadioButtonId();
		b = (RadioButton)content.findViewById(selected);

		new AlertDialog.Builder(activity).setView(content).setPositiveButton("Siguiente",
				new DialogInterface.OnClickListener() {

			public void onClick(
					DialogInterface dialog,
					int which) {
			
				dialog.dismiss(); //end the dialog.

				String universityName = b.getText().toString();
				Log.i("INITIAL","seleccionaste "+b.getText().toString());
				University u = new University();
				u.setName(universityName);
				current_user.setUniversity(u);
				//ahora guardamos el string de la universidad que guardo para que pueda ser recuperado por el usuario
				new initialDialog().setSecondMessage(activity, current_user);
			}
		}).show();
	}

	private void setSecondMessage(final Activity activity,final User current_user) {

		LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

		View content = inflater.inflate( R.layout.second_dialog, null );

		// Gets a reference to our radio group
		// rBtnDigits is the name of our radio group (code not shown)
		select = (RadioGroup) content.findViewById(R.id.universityRadioGroup); 
		int selected = select.getCheckedRadioButtonId();
		b = (RadioButton)content.findViewById(selected);

		new AlertDialog.Builder(activity).setView(content).setPositiveButton("Entrar a Smart Campus",
				new DialogInterface.OnClickListener() {

			public void onClick(
					DialogInterface dialog,
					int which) {
				//find our if the user checked the checkbox and put true in the preferences so we don't show the dialog again 
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(activity);
				SharedPreferences.Editor editor = prefs
						.edit();
				editor.putBoolean("initial_dialog",
						true);

				editor.commit();

				dialog.dismiss(); //end the dialog.

				//TODO guardar campus

			}
		}).show();

	}



}

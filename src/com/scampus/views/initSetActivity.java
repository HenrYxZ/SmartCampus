package com.scampus.views;

import java.util.concurrent.Callable;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.scampus.especial1.R;
import com.scampus.tools.Campus;
import com.scampus.tools.DBHelper;
import com.scampus.tools.RequestHandler;
import com.scampus.tools.University;
import com.scampus.tools.User;

public class initSetActivity extends Activity{

	private User current_user;
	private RequestQueue requestQueue;
	private Button next;
	private Button back;
	private DBHelper dbh;
	private University[] universities;
	private Campus[] campuses;
	private ProgressDialog progress;
	private RequestHandler requestHandler;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_initset); 

		current_user = new User(this);//cuando creamos un usuario y le entregamos un contexto, la clase usuario se inicializa con los datos guardados
		//en la base de datos (shared preferences)
		dbh = new DBHelper(this);//usaremos este DBHelper para sacar datos de la base de datos

		requestQueue = Volley.newRequestQueue(this);	
		//cargamos las universidades y campus disponibles
		requestHandler =new RequestHandler(requestQueue);//clase creada para manejar los request

		next = (Button)findViewById(R.id.nextButton);
		back = (Button)findViewById(R.id.backButton);

		//this.waitProcess("Esperando a cargar universidades");
		//hacemos que el boton back no se vea al inicio
		back.setVisibility(View.INVISIBLE);
		//requestHandler.requestUniversitiesAndCampus(current_user, this);
		context = this;

		if(!current_user.hasUniversity()){
			//si el usuario no tiene universidad seleccionada
			requestHandler.requestCampus(current_user, context, null);
			requestHandler.requestUniversities(current_user, context, new Callable<Void>(){
				public Void call(){
					return setViewForUniversitySelection();
				}
			});//estas universidades y campuses quedan en la base de datos, luego queda sacarlas
		}
		else if(current_user.hasUniversity() && !current_user.hasCampus()){
			//si el usuario tiene universidad pero no tiene campus cargamos para seleccionar campus
			requestHandler.requestCampus(current_user, context, new Callable<Void>(){
				public Void call(){
					return setViewForCampusSelection(current_user.getUniversity());
				}
			});
		}
		else
			//si no se cumplen las opciones anteriores, entonces mandamos a la actividad main activity.
			this.sendToMain();
	}

	private void cleanUserForSetting() {
		current_user.setUniversity(null);
		current_user.setCampus(null);
	}

	public void onClick_next(View v)
	{
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.selectionRadioGroup);
		int radioButtonID = radioGroup.getCheckedRadioButtonId();
		RadioButton radioButton = (RadioButton)radioGroup.findViewById(radioButtonID);
		if(radioButtonID == -1){
			//si no hay ninguna seleccionada se muestra un mensaje
			sendToastMessage("Debes seleccionar tu universidad");
		}
		else if(!current_user.hasUniversity()){
			//si no tiene Universidad
			String universityName = radioButton.getText().toString();
			University u = this.findUnivByName(universityName);
			current_user.setUniversity(u);
			current_user.saveUser(this);
			Log.i("INIT","agregando al usuario la U"+current_user.getUniversity().toString());
			this.setViewForCampusSelection(u);			
		}
		else if(!current_user.hasCampus()){
			//si no tiene campus
			String campusName = radioButton.getText().toString();
			Campus c = this.findCampusByName(campusName);
			current_user.setCampus(c);
			current_user.saveUser(this);
			this.sendToMain();
		}

	}
	private Campus findCampusByName(String campusName) {
		for(int i = 0;i<campuses.length; i++){
			if(campuses[i].getName().equalsIgnoreCase(campusName))
				return campuses[i];
		}
		return null;
	}

	public void OnClick_back(View v){
		current_user.setUniversity(null);
		this.setViewForUniversitySelection();
	}
	private University findUnivByName(String universityName) {
		for(int i = 0;i<universities.length; i++){
			if(universities[i].getName().equalsIgnoreCase(universityName))
				return universities[i];
		}
		return null;
	}

	private void sendToMain() {
		//si estamos listos con el setting inicial enviamos al usuario a la vista principal
		Intent i = new Intent(this, MainActivity.class);
		startActivity(i);
		finish();
	}
	private Void setViewForCampusSelection(University university) {
		campuses = dbh.getCampus(university.getID());
		if(campuses==null){
			sendToastMessage("Ups! La universidad que seleccionaste no tiene campus inscritos.");
			this.setViewForUniversitySelection();
		}
		else{
			back.setVisibility(View.VISIBLE);
			RadioGroup radioGroup = (RadioGroup) findViewById(R.id.selectionRadioGroup);//este radio group debe tener las opciones de universidades
			radioGroup.removeAllViewsInLayout();
			for (int i=0; i<campuses.length; i++) {
				RadioButton radioButton = new RadioButton(getBaseContext());
				radioButton.setText(campuses[i].getName());
				radioButton.setTextColor(getResources().getColor(R.color.light_blue));
				radioGroup.addView(radioButton);
			}
		}
		return null;
	}

	private void sendToastMessage(String message) {
		Context context = getApplicationContext();
		CharSequence text = message;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	private Void setViewForUniversitySelection() {
		ProgressDialog progress = this.waitProcess("Cargando Universidades");
		universities = dbh.getUniversities();
		cleanUserForSetting();
		progress.dismiss();
		back.setVisibility(View.INVISIBLE);
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.selectionRadioGroup);//este radio group debe tener las opciones de universidades
		radioGroup.removeAllViewsInLayout();
		if(universities == null){
			this.sendToastMessage("No hay universidades para elegir");
		}
		else {
			for (int i=0; i<universities.length; i++) {
				RadioButton radioButton = new RadioButton(getBaseContext());
				radioButton.setText(universities[i].getName());
				radioButton.setTextColor(getResources().getColor(R.color.light_blue));
				radioGroup.addView(radioButton);
			}
		}
		return null;
	}
	private ProgressDialog waitProcess(String message){
		ProgressDialog progress = new ProgressDialog(this);
		progress.setTitle("Loading");
		progress.setMessage(message);
		progress.show();
		return progress;

	}
}

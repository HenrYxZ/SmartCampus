package com.scampus.tools;

import java.security.Timestamp;

import org.json.JSONArray;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.Session;


public class User {
	private String api_token;
	private String first_name;
	private String last_name;
	private String email;
	private Timestamp lastValidation;
	private University university;
	private Campus campus;
	public static final String PREFS_NAME = "UserPrefsFile";//Nombre del archivo donde se guardan los datos del usuario
	public JSONArray ranking;
	public int game_position;
	public int game_points;
	
	public User(Context context){
		//si es que el usuario ya habia guardado sus datos los recuperamos
		this.retrieveUser(context);
		game_position = 0;
		game_points = 0;
	}
	public User(){
		game_position = 0;
		game_points = 0;
	}

	public Timestamp getLastValidation(){
		return this.lastValidation;
	}
	
	public int getPoints(){
		return this.game_points;
	}
	public int getPosition(){
		return this.game_position;
	}
	public boolean isValidated(){
		if(this.lastValidation == null){
			return false;
		}
		return true;
	}
	public String getFirstName(){
		return this.first_name;
	}
	public String getLastName(){
		return this.last_name;
	}
	public String getEmail(){
		return this.email;
	}
	public String getApiToken(){
		return this.api_token;
	}
	public void setFirstName(String firstName){
		this.first_name = firstName;
	}
	public void setLastName(String lastName){
		this.last_name = lastName;
	}
	public void setEmail(String email){
		this.email = email;
	}
	public void setApiToken(String apitoken){
		this.api_token = apitoken;
	}
	public void setUniversity(University u){
		this.university = u;
	}
	public University getUniversity(){
		return this.university;
	}
	public void setCampus(Campus c){
		this.campus = c;
	}
	public Campus getCampus(){
		return this.campus;
	}
	public boolean hasApiToken(Context context){
		this.retrieveUser(context);

		if(this.api_token != null && this.api_token != "")
			return true;
		return false;
	}
	public boolean hasUniversity(){
		if(this.university != null)
			return true;

		return false;
	}
	public boolean hasCampus(){
		if(this.campus != null)
			return true;
		return false;
	}
	public String getAccesToken(){
		Session session = Session.getActiveSession();
		return session.getAccessToken();
	}
	public void setGameInfo(int points, int position, JSONArray ranking){
		this.game_points = points;
		this.game_position = position;
		this.ranking = ranking;
	}

	public void saveUser(Context context){
		// En este metodo se guardan los datos del usuario con SharedPreferences

		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		//guardamos los datos
		if(this.api_token != null)
			editor.putString("api_token", this.getApiToken());
		if(this.first_name != null)
			editor.putString("first_name", this.getFirstName());
		if(this.last_name != null)
			editor.putString("last_name", this.getLastName());
		if(this.email != null)
			editor.putString("email", this.getEmail());
		if(this.lastValidation != null)
			editor.putString("last_validation", this.getLastValidation().toString());//TODO guardar correctamente lastvalidation
		if(this.university != null)
			editor.putInt("university_id", this.university.getID());
		else
			editor.putInt("university_id", -1);
		if(this.campus != null)
			editor.putInt("campus_id", this.campus.getID());
		else
			editor.putInt("campus_id", -1);
		editor.putInt("game_points", this.game_points);
		editor.putInt("game_position", this.game_position);



		// Hacemos commit a los cambios
		editor.commit();
	}
	//con este metodo se recuperan de la memoria los atributos del usuario
	public void retrieveUser(Context context){
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);

		//recuperamos los atributos del usuario
		this.api_token = settings.getString("api_token", "");
		this.first_name = settings.getString("first_name", "");
		this.last_name = settings.getString("last_name", "");
		this.email = settings.getString("email", "");
		int university_id = settings.getInt("university_id", -1);
		int campus_id = settings.getInt("campus_id", -1);
		this.game_points = settings.getInt("game_points",0);
		this.game_points = settings.getInt("game_position",0);
		if(university_id!=-1){
			DBHelper dbh = new DBHelper(context);
			this.setUniversity(dbh.getUniversityById(university_id));
		}
		if(campus_id!=-1){
			DBHelper dbh = new DBHelper(context);
			this.setCampus(dbh.getCampusById(campus_id));
		}

	}
	public void cleanUniversity(Context context){
		
		this.api_token = null;		
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);		
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("university_id", -1);
		editor.putInt("campus_id", -1);
		editor.putString("api_token", "");
		// Hacemos commit a los cambios
		editor.commit();
	}
}




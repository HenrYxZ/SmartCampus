package com.scampus.tools;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Suggestion {
	
	private String id;
	private String description;
	private int status;
	private String answer;

	public Suggestion(){}

	public Suggestion(String id, String description, int status){

		this.description = description;
		this.status = status;
		this.id = id;
	}
	
	public Suggestion(String id, String description, int status, String answer){

		this.description = description;
		this.status = status;
		this.id = id;
		this.answer = answer;
	}
	
	public void setID(String id){
		this.id = id;
	}
	public void setDescription(String description){
		this.description = description;
	}
	public void setStatus(int status){
		this.status = status;
	}
	public String getID(){
		return this.id;
	}
	public String getDescription(){
		return this.description;
	}
	public int getStatus(){
		return this.status;
	}
	
	public void saveSuggestion(Context context){
		//Crea la base de datos DBPois version 1 (si se cambia la version hay que implementar el onUpdate del PoisSQLiiteHelper)
		SuggestionSQLiteHelper sesdbh = new SuggestionSQLiteHelper(context, "suggestions", null,1);

		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();
		
		try {
			db.execSQL("INSERT INTO suggestions (id, description, status) " +
					"VALUES ('"+this.id+"','"+this.getDescription()+"','"+this.getStatus()+"')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
	
	public void upDate(Context context){
		//Crea la base de datos DBPois version 1 (si se cambia la version hay que implementar el onUpdate del PoisSQLiiteHelper)
		SuggestionSQLiteHelper sesdbh = new SuggestionSQLiteHelper(context, "suggestions", null,1);

		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();
		
		try {
			db.execSQL("DELETE FROM suggestions WHERE id='"+id+"'");
			db.execSQL("INSERT INTO suggestions (id, description, status, answer) " +
					"VALUES ('"+this.id+"','"+this.getDescription()+"','"+this.getStatus()+"','"+this.getAnswer()+"')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
	}
}

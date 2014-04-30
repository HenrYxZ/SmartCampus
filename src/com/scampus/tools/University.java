package com.scampus.tools;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;




public class University {
	private String name;
	private String acronym;
	private int id;
	public University(){}

	public University(int id, String acronym, String name){
		this.id = id;
		this.name = name;
		this.acronym = acronym;
	}

	public void setID(int id){
		this.id = id;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setAcronym(String acr){
		this.acronym = acr;
	}
	public int getID(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public String getAcronym(){
		return this.acronym;
	}
	public void saveUniversity(Context context){
		//Crea la base de datos DBPois version 1 (si se cambia la version hay que implementar el onUpdate del PoisSQLiiteHelper)
		UniversitiesSQLiteHelper sesdbh = new UniversitiesSQLiteHelper(context, "universities", null,1);

		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();

		try {
			db.execSQL("INSERT INTO universities (web_id, name, acronym) " +
					"VALUES ('"+this.getID()+"', '"+this.getName()+"','"+this.getAcronym()+"')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
	}
	public String toString(){
		return this.getID()+" "+this.getAcronym()+" "+this.getName();
	}

}

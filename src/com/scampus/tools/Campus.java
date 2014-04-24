package com.scampus.tools;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Campus {
	private int ID;
	private int university_id;
	private String name;
	private String polygon;
	public Campus(){
		
	}
	public Campus(int id,int university_id,String name, String polygon){
		this.ID=id;
		this.university_id = university_id;
		this.name = name;
		this.polygon=polygon;
	}
	public void setID(int id){
		this.ID = id;
	}
	public void setPolygon(String poly){
		this.polygon = poly;
	}
	public int getID(){
		return this.ID;
	}
	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return this.name;
	}
	public String getPolygon(){
		return this.polygon;
	}
	public void setUniversityID(int id){
		this.university_id = id;
	}
	public int getUniversityID(){
		return this.university_id;
	}

	public void saveCampus(Context context){
		//Crea la base de datos DBPois version 1 (si se cambia la version hay que implementar el onUpdate del PoisSQLiiteHelper)
		CampusSQLiteHelper sesdbh = new CampusSQLiteHelper(context, "campuses", null,1);

		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();

		try {
			db.execSQL("INSERT INTO campuses (web_id, university_id, name, polygon) " +
					"VALUES ('"+this.getID()+"', '"+this.getUniversityID()+"', '"+this.getName()+"', '"+this.polygon+"')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
	}
	public String toString(){
		return this.getID()+" "+this.getName()+" "+this.polygon;
	}
}


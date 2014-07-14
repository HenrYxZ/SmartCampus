
package com.scampus.tools;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.PolygonOptions;




public class Building {
	int id;
	String name;
	String campus;
	String polygon;
	String description;
	Point center;
	
	Context context;
	
	public Building(){}

	public Building(Context context, int id, String name, String description, String campus, String polygon, Point center){
		
		
		this.id = id;
		this.name = name;
		this.campus= campus;
		this.polygon=polygon;
		this.description=description;
		this.context= context;
		this.center = center;
		
	}
	
	public void setID(int id){
		this.id = id;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setDescription(String description){
		this.description = description;
	}
	public void setPolygon(String poly){
		this.polygon = poly;
	}
	public void setCampus(String campus){
		this.campus = campus;
	}
	public void setCenter(Point p) {
		center = p;
	}

	
	public int getID(){
		return this.id;
	}
	public String getCampus(){
		return this.campus;
	}
	public String getDescription(){
		return this.description;
	}
	public String getName(){
		return this.name;
	}
	public String getPolygon(){
		return this.polygon;
	}
	public Point getCenter() {
		return this.center;
	}
	public void saveBuilding(Context context){
		//Crea la base de datos DBPois version 1 (si se cambia la version hay que implementar el onUpdate del PoisSQLiiteHelper)
		UniversitiesSQLiteHelper sesdbh = new UniversitiesSQLiteHelper(context, "DBPois", null,1);

		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();

		try {
			db.execSQL("INSERT INTO Buildings (id, name, description, campus, polygon, centerx, centery) " +
					"VALUES ("+this.id+", '"+this.name+"', '"+this.description+"', '"+this.campus+"', '"+
					this.polygon+"', "+this.center.x+", "+this.center.y+")");
		} catch (SQLException e) {
			Log.i("Buildings", e.getMessage());
		}
		db.close();
	}
	public PolygonOptions createPolygonOptions()
	{
		PolygonOptions options = new PolygonOptions();
		options.addAll(new Map().decodePoly(this.polygon));
		options.fillColor(0x7FFF0000);
		options.strokeWidth(2);
		options.visible(true);
		
		
		
		return options;
		
	}

	
	public String toString(){
		return "";
	}

}


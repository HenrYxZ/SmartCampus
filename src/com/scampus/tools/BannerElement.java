package com.scampus.tools;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class BannerElement {
	private int id;
	private String name;
	private String url;
	private boolean active;
	public String link;//solo publicidad y noticias (reports y advertises) lo tienen
	
	public enum Type{survey,report,event,advertise};
	private Type type;
	
	public BannerElement(int id,String name, String url, boolean active, Type type){
		this.id = id;
		this.name = name;
		this.url = url;
		this.active = active;
		this.type = type;
	}	
	public int getID(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public String getUrl(){
		if(this.url == null)return null;
		return "http://smartcampus.ing.puc.cl/"+this.url;
	}
	public boolean isActive(){
		return this.active;
	}
	public String toString(){
		return this.id+" \n nombre: "+this.name+" \n url: "+this.url+" \n activo: "+this.active+" \n tipo: "+this.type;
	}
	public Type getType(){
		return this.type;
	}
	
	public void saveElement(Context context){
		//Crea la base de datos DBPois version 1 (si se cambia la version hay que implementar el onUpdate del PoisSQLiiteHelper)
		BannerSQLiteHelper sesdbh = new BannerSQLiteHelper(context, "banner_elements", null,1);

		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();
		int active = 0;//no esta activo
		if(this.active) active =1;//esta activo (en sqlite3 no hay booleans)
		try {
			db.execSQL("INSERT INTO banner_elements (web_id, name, url, active,type) " +
					"VALUES ('"+this.getID()+"', '"+this.getName()+"', '"+this.url+"','"+active+"','"+this.type.toString()+"')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
	}
}

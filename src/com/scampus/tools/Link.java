package com.scampus.tools;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Link {
	int id;
	private String name;
	private String url;
	String type;
	String source;
	int place_id;
	String place_type;
	

	public Link(int id, String name, String url, String type, String source) {
		this.id = id;
		this.setName(name);
		this.setUrl(url);
		this.type = type;
		this.source = source;
	}

	public void setPlace(int place_id, String place_type) {
		this.place_id = place_id;
		this.place_type = place_type;
	}

	public void saveLink(SQLiteDatabase db) {

		// Se usa base de datos local para archivos que se guardan en el
		// servidor web
		String tableType;
		if (this.source.equals("local"))
			tableType = "Local";
		else
			tableType = "External";
		// Se guarda el link en la base de datos SQLite del celular
		try {
			db.execSQL("INSERT INTO " + tableType
					+ "Links (id, name, url, type, place_id, place_type) "
					+ "VALUES (" + this.id + ", '" + this.getName() + "' , '" + this.getUrl() + "' , '"
					+ this.type + "' , " + this.place_id + " , '"
					+ this.place_type + "')");
		} catch (SQLException e) {
			Log.e("Links", e.getMessage());
		}

	}

	public String getUrl() {
		return url;
	}
	
	public String getType() {
		return type;
	}


	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

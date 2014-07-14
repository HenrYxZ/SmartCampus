package com.scampus.tools;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Event {
	int id;
	String name;
	String description;
	LatLng position;
	// Los Strings date son strings de la forma RFC 3339, que pueden ser tratados como
	// Calendar para manejarlos, casteandolos primero como Time.parse(String)
	// Por ejemplo: "end_date":"2014-06-14T13:17:00-04:00"
	String start_date;
	String end_date;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LatLng getPosition() {
		return position;
	}

	public void setPosition(LatLng position) {
		this.position = position;
	}

	public String getStart_date() {
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	public String getEnd_date() {
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	public Event(int id, String name, String description, LatLng position,
			String start_date, String end_date) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.position = position;
		this.start_date = start_date;
		this.end_date = end_date;
	}
	
	public void save(SQLiteDatabase db) {
		try {
			String sql = "INSERT INTO Events" + 
					"(id, name, description, lat, lng, start_date, end_date)"+
					"VALUES (" + this.id + ", '" + this.name + "', '" + this.description +
					"', " + this.position.latitude + ", "+ this.position.longitude + ", '" +
					this.start_date + "', '" + this.end_date + "')";
			db.execSQL(sql );
		} catch (SQLException e){
			Log.i("Events", e.getMessage());
		}
		
	}
}

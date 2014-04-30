package com.scampus.tools;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.scampus.especial1.R;
import com.scampus.especial1.SConstants;


public class Map extends Activity implements SConstants {

	GoogleMap map;
	PoisSQLiteHelper sesdbh;
	SQLiteDatabase db;
	DBHelper dbh;
	Context context;
	Polygon[] pos;
	User current_user;
	RequestQueue requestQueue;
	public GoogleMap createMap(SQLiteDatabase database, GoogleMap gmap, Context context)
	{
		this.context=context;
		RequestHandler rh = new RequestHandler(requestQueue);
		PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context, "DBPois", null,1);
		//Abre la base de datos
		 db = sesdbh.getReadableDatabase();    
		dbh = new DBHelper(context);
		map= gmap;
		current_user= new User(context);
		
		//Abre la base de datos


		loadPois();
		loadPors();
		loadEvents();
		loadBuildings();


		return map;

	}
	
	public GoogleMap createMap(SQLiteDatabase database, GoogleMap gmap, Context context, int event_id)
	{
		this.context=context;

		PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context, "DBPois", null,1);
		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();    
		dbh = new DBHelper(context);
		map= gmap;
		
		loadBuildings();
		Cursor c = db.rawQuery(" SELECT * FROM Events WHERE id="+event_id, null);


		if (c.moveToFirst()) {

			//Recorremos el cursor hasta que no haya más registros
			do {

				Double lataux = Double.parseDouble(c.getString(5));;
				Double lngaux = Double.parseDouble(c.getString(6));;;
				String nameaux = c.getString(1);
				LatLng position = new LatLng(lataux,lngaux );
				map.addMarker(new MarkerOptions()
				.position(position)
				.title(nameaux)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.festival))
						);


			} while(c.moveToNext());
		} 
		db.close();
		return map;

	}



	private void loadBuildings() {
		Building[] buildings= dbh.getBuildings(context);
		pos = new Polygon[buildings.length];
		Log.e("QWERTYUI", String.valueOf(buildings.length));
		for(int i=0;i<buildings.length;i++)
		{


			Polygon a=map.addPolygon(buildings[i].createPolygonOptions());
			pos[i]= a;
			
			Double lataux = buildings[i].getCenter().y;
			Double lngaux = buildings[i].getCenter().x;
			String nameaux = buildings[i].getName();
			LatLng position = new LatLng(lataux,lngaux );
			// Agregado Henry 22-4
			MarkerOptions mo =new MarkerOptions()
			.position(position) 
			.title(nameaux)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_icon));
			
			map.addMarker(mo);
		}
	}
	public void setVisible(boolean visible)
	{
		if (!visible)
		{
			for(int i=0; i<pos.length; i++)
			{
				if(pos[i]!= null)
					pos[i].remove();
			}
		}
		else
		{
			loadBuildings();
		}
	}





	private void loadEvents() {
		Cursor c = db.rawQuery(" SELECT * FROM Events", null);


		if (c.moveToFirst()) {

			//Recorremos el cursor hasta que no haya más registros
			do {

				Double lataux = Double.parseDouble(c.getString(5));;
				Double lngaux = Double.parseDouble(c.getString(6));;;
				String nameaux = c.getString(1);
				LatLng position = new LatLng(lataux,lngaux );
				map.addMarker(new MarkerOptions()
				.position(position)
				.title(nameaux)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.festival))
						);


			} while(c.moveToNext());
		}


	}



	private void loadPors() {

		Cursor c = db.rawQuery(" SELECT * FROM Pors", null);

		if (c.moveToFirst()) {

			//Recorremos el cursor hasta que no haya más registros
			do {
				int idaux = c.getInt(0);
				Cursor d = db.rawQuery(" SELECT name_type FROM Dump_types WHERE id_por=" + idaux, null);
				String snippet="";
				if (d.moveToFirst()) {
					do {
						if (snippet!="")
							snippet= snippet+", ";
						snippet= snippet + d.getString(0);


					} while(d.moveToNext());
				}
				
				Double lataux = c.getDouble(3);
				Double lngaux = c.getDouble(4);
				String nameaux = c.getString(1);
				LatLng position = new LatLng(lataux,lngaux );
				map.addMarker(new MarkerOptions()
				.position(position)	
				.title(nameaux)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.recycle))
				.snippet(snippet)
						);


			} while(c.moveToNext());
		}

	}



	private void loadPois() {
		Cursor c = db.rawQuery(" SELECT * FROM Pois", null);


		if (c.moveToFirst()) {

			//Recorremos el cursor hasta que no haya más registros
			do {

				Double lataux = Double.parseDouble(c.getString(5));;
				Double lngaux = Double.parseDouble(c.getString(6));;;
				String nameaux = c.getString(1);
				String descaux = c.getString(2);
				if(descaux!=null || descaux!="null" || descaux.equals(null)|| descaux=="")
				{
					descaux= "sin descripción";
					
				}
				LatLng position = new LatLng(lataux,lngaux );
				MarkerOptions mo =new MarkerOptions()
				.position(position) 
				.title(nameaux)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_icon))
				.snippet(descaux);
				
				map.addMarker(mo);



			} while(c.moveToNext());
		}

	}





	// Este es un metodo utilizado para decodificar el string que tiene las posiciones de los vertices del edificio
	public List<LatLng> decodePoly(String encoded) {
		// Nos aseguramos de no tener mal ingresados el poligono
		encoded.trim();
		Log.i("Encoded string", encoded);
		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));
			poly.add(p);
		}

		return poly;
	}






}




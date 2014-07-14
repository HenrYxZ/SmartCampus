package com.scampus.tools;

import java.util.LinkedList;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

//el objetivo de esta clase es rescatar datos desde las distintas tablas en la base de datos local
public class DBHelper {

	private Context context;

	public DBHelper(Context context) {
		this.context = context;
	}

	public University[] getUniversities() {

		UniversitiesSQLiteHelper sesdbh = new UniversitiesSQLiteHelper(context,
				"universities", null, 1);
		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM universities", null);
		int univCount = c.getCount();

		if (univCount == 0) {
			return null;
		}

		University[] ret = new University[univCount];
		int count = 0;
		while (c.moveToNext()) {
			Log.i("DB",
					"sacando universidad " + c.getInt(1) + " " + c.getString(2)
							+ " " + c.getString(3));
			ret[count] = new University(c.getInt(1), c.getString(2),
					c.getString(3)); // id,acronym,name
			count++;
		}
		db.close();
		return ret;

	}

	public Campus[] getCampus(int university_id) {

		// retorna los campuses de una universidad especifica

		CampusSQLiteHelper sesdbh = new CampusSQLiteHelper(context, "campuses",
				null, 1);
		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM campuses WHERE university_id = "
				+ university_id, null);
		int univCount = c.getCount();

		if (univCount == 0) {
			return null;
		}

		Campus[] ret = new Campus[univCount];
		int count = 0;
		while (c.moveToNext()) {
			Log.i("DB", "sacando campus " + c.getInt(1) + " " + c.getInt(2)
					+ " " + c.getString(3));
			ret[count] = new Campus(c.getInt(1), c.getInt(2), c.getString(3),
					c.getString(4)); // id,universityID,name
			count++;
		}
		db.close();
		return ret;

	}

	public University getUniversityByName(String name) {
		UniversitiesSQLiteHelper sesdbh = new UniversitiesSQLiteHelper(context,
				"universities", null, 1);
		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM universities WHERE name = "
				+ name, null);
		int univCount = c.getCount();

		if (univCount == 0) {
			return null;
		}

		University ret;
		ret = new University(c.getInt(1), c.getString(2), c.getString(3)); // id,acronym,name
		Log.i("DB", "sacando la universidad" + ret.getName());
		db.close();
		return ret;
	}

	public University getUniversityById(int id) {
		UniversitiesSQLiteHelper sesdbh = new UniversitiesSQLiteHelper(context,
				"universities", null, 1);
		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM universities WHERE web_id = "
				+ id, null);
		int univCount = c.getCount();
		Log.i("DB", "el cursor tiene datos:" + univCount);
		if (univCount == 0) {
			return null;

		}
		c.moveToLast();// movemos el cursor para pararnos en el dato que
						// corresponde

		University ret;
		ret = new University(c.getInt(1), c.getString(2), c.getString(3)); // id,acronym,name
		Log.i("DB", "sacando la universidad" + ret.getName());

		db.close();

		return ret;
	}

	public Campus getCampusById(int id) {
		CampusSQLiteHelper sesdbh = new CampusSQLiteHelper(context, "campuses",
				null, 1);
		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM campuses WHERE web_id = " + id,
				null);
		int campusCount = c.getCount();
		if (campusCount == 0) {
			// return null;
			return new Campus(1, 1, "San Joaquín",
					"tfmkE`f_nLr[kDbDux@{Pi@G|AgRjDpB`w@"); // TODO

		}
		c.moveToLast();// movemos el cursor para pararnos en el dato que
						// corresponde

		Campus ret;
		ret = new Campus(c.getInt(1), c.getInt(2), c.getString(3),
				c.getString(4)); // id,universityID,name
		Log.i("DB", "sacando el campus" + ret.getName());

		db.close();

		return ret;
	}

	public Building[] getBuildings(Context context) {

		PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context, "DBPois", null,
				1);
		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM Buildings", null);
		int bCount = c.getCount();

		if (bCount == 0) {
			Log.i("OJO", "NO SE ENCONTRARON EDIFICIOS");
			return null;
		}

		Building[] ret = new Building[bCount];
		int count = 0;
		while (c.moveToNext()) {

			double x = c.getDouble(5);
			double y = c.getDouble(6);
			Point center = new Point(x, y);
			ret[count] = new Building(context, c.getInt(0), c.getString(1),
					c.getString(2), c.getString(3), c.getString(4), center);
			count++;
			Log.e("", "QWERTYUIOP");
		}
		db.close();
		return ret;

	}

	public Suggestion[] getSuggestions(Context context) {

		SuggestionSQLiteHelper sesdbh = new SuggestionSQLiteHelper(context,
				"suggestions", null, 1);
		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM Suggestions", null);
		int bCount = c.getCount();

		if (bCount == 0) {
			Log.i("OJO", "NO SE ENCONTRARON SUGERENCIAS");
			return null;
		}

		Suggestion[] ret = new Suggestion[bCount];
		int count = 0;
		while (c.moveToNext()) {
			if (c.getString(3) != null) {
				ret[count] = new Suggestion(c.getString(0), c.getString(1),
						c.getInt(2), c.getString(3));
			} else {
				ret[count] = new Suggestion(c.getString(0), c.getString(1),
						c.getInt(2));
			}
			count++;
		}
		db.close();
		return ret;

	}

	public Suggestion[] getSuggestionsAtPosition(Context context, int position) {

		SuggestionSQLiteHelper sesdbh = new SuggestionSQLiteHelper(context,
				"suggestions", null, 1);
		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM Suggestions", null);
		int bCount = c.getCount();

		if (bCount == 0) {
			Log.i("OJO", "NO SE ENCONTRARON SUGERENCIAS");
			return null;
		}

		Suggestion[] ret = new Suggestion[bCount];
		int count = 0;
		while (c.moveToNext()) {
			ret[count] = new Suggestion(c.getString(0), c.getString(1),
					c.getInt(2));
			// ret[count] = new Suggestion(c.getString(1), c.getInt(2));
			count++;
		}
		db.close();
		return ret;

	}

	public void deleteSuggestions() {

		SuggestionSQLiteHelper sesdbh = new SuggestionSQLiteHelper(context,
				"suggestions", null, 1);
		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();
		try {

		//db.rawQuery("DELETE FROM suggestions", null);
			int count = db.delete("suggestions", "1", null);
			Log.i("Numero sugerencias borradas",String.valueOf(count));

		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
	}

	public BannerElement[] getBannerElements() {
		BannerSQLiteHelper sesdbh = new BannerSQLiteHelper(context,
				"banner_elements", null, 1);
		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();
		Cursor c = db.rawQuery(
				"SELECT * FROM banner_elements WHERE status = 1", null);
		int elementsCount = c.getCount();
		if (elementsCount == 0) {
			BannerElement[] ret = new BannerElement[1];
			ret[0] = new BannerElement(
					1,
					"encuesta",
					"http://smartcampus.ing.puc.cl/system/rapidfire/question_groups/photos/000/000/045/original/Redes_Sociales1.png",
					1, BannerElement.Type.survey);
			return ret;
		}

		BannerElement[] ret = new BannerElement[elementsCount];
		int count = 0;
		while (c.moveToNext()) {
			BannerElement.Type t = BannerElement.Type.advertise;

			if (c.getInt(4) == 1) {

				String type = c.getString(6);
				if (type.equalsIgnoreCase("survey")) {
					t = BannerElement.Type.survey;
				} else if (type.equalsIgnoreCase("report")) {
					t = BannerElement.Type.report;
				} else if (type.equalsIgnoreCase("event")) {
					t = BannerElement.Type.event;
				} else if (type.equalsIgnoreCase("advertise")) {
					t = BannerElement.Type.advertise;
				}
				// en la base de datos estan con este orden:id,
				// web_id,name,url,active,type
				Log.i("DBHelper", c.getString(2));
				BannerElement banner = new BannerElement(c.getInt(1),
						c.getString(2), c.getString(3), c.getInt(4), t); 	
				banner.link = c.getString(5);
				ret[count] = banner;

			}
			count++;
		}
		for (int i = 0; i < ret.length; i++) {
			Log.i("BANNER", ret[i].toString());

		}
		db.close();
		return ret;
	}

	public boolean dropBannerTable(Context context) {

		BannerSQLiteHelper sesdbh = new BannerSQLiteHelper(context,
				"banner_elements", null, 1);
		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS banner_elements");
		sesdbh.onCreate(db);
		db.close();
		return true;
	}
	
	public void deleteSurvey(int id){}
		
		/*BannerSQLiteHelper sesdbh = new BannerSQLiteHelper(context,
				"banner_elements", null, 1);
		
		SQLiteDatabase db = sesdbh.getReadableDatabase();
		Cursor c = db.rawQuery(
				"DELETE FROM banner_elements WHERE web_id = "+ id +" AND type = "+BannerElement.Type.survey, null);
		int elementsCount = c.getCount();*/
	

	public LinkedList<Link> getUrlsFromPlace(Context context, int place_id,
			String place_type, String tableName, String typeOfUrl) {

		String filterUrls;
		if (typeOfUrl.equals("image"))
			filterUrls = "type = 'image'";
		else
			filterUrls = "NOT type = 'image'";

		LinkedList<Link> imagesUrls = new LinkedList<Link>();
		PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context, "DBPois", null,
				1);
		SQLiteDatabase db = sesdbh.getReadableDatabase();
		String sql = "SELECT * FROM " + tableName + " WHERE place_type='"
				+ place_type + "' AND place_id=" + place_id + " AND "
				+ filterUrls;
		Cursor c = db.rawQuery(sql, null);
		while (c.moveToNext()) {
			int id = c.getInt(0);
			String name = c.getString(1);
			String url = c.getString(2);
			String type = c.getString(3);
			String source = c.getString(4);
			Link link = new Link(id, name, url, type, source);
			imagesUrls.add(link);
		}
		db.close();
		return imagesUrls;
	}

	public LinkedList<Link> getUrlsFromPlace(Context context, int place_id,
			String place_type, String typeOfUrl) {

		String table1 = "LocalLinks";
		String table2 = "ExternalLinks";

		LinkedList<Link> links1;
		LinkedList<Link> links2;
		links1 = getUrlsFromPlace(context, place_id, place_type, table1,
				typeOfUrl);
		links2 = getUrlsFromPlace(context, place_id, place_type, table2,
				typeOfUrl);

		for (Link link : links2) {
			links1.add(link);
		}
		return links1;
	}

	public Link[] getLinksFromPlace(Context context, int place_id,
			String place_type) {

		LinkedList<Link> linksUrls = getUrlsFromPlace(context, place_id,
				place_type, "links");
		Link[] links = linksUrls.toArray(new Link[linksUrls.size()]);
		return links;
	}

	public LinkedList<Link> getImagesUrlsFromPlace(Context context,
			int place_id, String place_type) {
		return getUrlsFromPlace(context, place_id, place_type, "image");
	}
	
	public PlaceDetails getEventByName(Context context, String name) {
		// Esto se usa para obtener el place de un evento en el
		// publicityActivity, para llevar a su respectivo markerDetails
		// al cliquear la imagen.
		String sql = "SELECT * FROM Events WHERE name='"
				+ name + "' LIMIT 1";
		PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context, "DBPois", null,
				1);
		SQLiteDatabase db = sesdbh.getReadableDatabase();
		Cursor c = db.rawQuery(sql, null);
		int id = 0;
		String type = "", description = "";
		double lat = 0, lon =0;
		while (c.moveToNext()) {
			id = c.getInt(0);
			type = "event";
			description = c.getString(2);
			lat = c.getDouble(5);
			lon = c.getDouble(6);
		}
		db.close();
		LatLng position = new LatLng(lat, lon);
		PlaceDetails place = new PlaceDetails(id, name, description, type, position);
		return place;
	}

	public String[] getEventDates(int id) {
		// Esto se usa para obtener las fechas de inicio y término de un evento en el
		// markerDetailsActivity
		String sql = "SELECT start_date, end_date FROM Events WHERE id="+ id + " LIMIT 1";
		PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context, "DBPois", null,
				1);
		SQLiteDatabase db = sesdbh.getReadableDatabase();
		Cursor c = db.rawQuery(sql, null);
		String[] dates = new String[2];
		while (c.moveToNext()) {
			dates[0] = c.getString(0);
			dates[1] = c.getString(1);
		}
		db.close();
		return dates;
	}
}

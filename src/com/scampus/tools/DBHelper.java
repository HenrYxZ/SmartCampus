package com.scampus.tools;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;


//el objetivo de esta clase es rescatar datos desde las distintas tablas en la base de datos local
public class DBHelper {

	private Context context;

	public DBHelper(Context context){
		this.context = context;
	}

	public University[] getUniversities(){	



		UniversitiesSQLiteHelper sesdbh = new UniversitiesSQLiteHelper(context, "universities", null,1);
		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();    		
		Cursor c = db.rawQuery("SELECT * FROM universities", null);
		int univCount= c.getCount();

		if(univCount == 0){
			return null;
		}

		University[] ret = new University[univCount];
		int count=0;
		while(c.moveToNext()){
			Log.i("DB","sacando universidad "+c.getInt(1)+" "+c.getString(2)+" "+c.getString(3));
			ret[count] = new University(c.getInt(1),c.getString(2),c.getString(3)); //id,acronym,name
			count++;
		}
		db.close();
		return ret;

	}
	public Campus[] getCampus(int university_id){	

		//retorna los campuses de una universidad especifica

		CampusSQLiteHelper sesdbh = new CampusSQLiteHelper(context, "campuses", null,1);
		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();    		
		Cursor c = db.rawQuery("SELECT * FROM campuses WHERE university_id = "+university_id, null);
		int univCount= c.getCount();

		if(univCount == 0){
			return null;
		}

		Campus[] ret = new Campus[univCount];
		int count=0;
		while(c.moveToNext()){
			Log.i("DB","sacando campus "+c.getInt(1)+" "+c.getInt(2)+" "+c.getString(3));
			ret[count] = new Campus(c.getInt(1),c.getInt(2),c.getString(3),c.getString(4)); //id,universityID,name
			count++;
		}
		db.close();
		return ret;

	}

	public University getUniversityByName(String name){
		UniversitiesSQLiteHelper sesdbh = new UniversitiesSQLiteHelper(context, "universities", null,1);
		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();    		
		Cursor c = db.rawQuery("SELECT * FROM universities WHERE name = "+name, null);
		int univCount= c.getCount();

		if(univCount == 0){
			return null;
		}

		University ret;
		ret = new University(c.getInt(1),c.getString(2),c.getString(3)); //id,acronym,name
		Log.i("DB","sacando la universidad"+ret.getName());
		db.close();
		return ret;
	}
	public University getUniversityById(int id){
		UniversitiesSQLiteHelper sesdbh = new UniversitiesSQLiteHelper(context, "universities", null,1);
		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();    		
		Cursor c = db.rawQuery("SELECT * FROM universities WHERE web_id = "+id, null);
		int univCount= c.getCount();
		Log.i("DB","el cursor tiene datos:" +univCount);
		if(univCount == 0){
			return null;

		}
		c.moveToLast();//movemos el cursor para pararnos en el dato que corresponde

		University ret;
		ret = new University(c.getInt(1),c.getString(2),c.getString(3)); //id,acronym,name
		Log.i("DB","sacando la universidad"+ret.getName());

		db.close();

		return ret;
	}
	public Campus getCampusById(int id){
		CampusSQLiteHelper sesdbh = new CampusSQLiteHelper(context, "campuses", null,1);
		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();    		
		Cursor c = db.rawQuery("SELECT * FROM campuses WHERE web_id = "+id, null);
		int campusCount= c.getCount();
		if(campusCount == 0){
			//return null;
			return new Campus(1,1,"San Joaquín","tfmkE`f_nLr[kDbDux@{Pi@G|AgRjDpB`w@"); //TODO

		}
		c.moveToLast();//movemos el cursor para pararnos en el dato que corresponde

		Campus ret;
		ret = new Campus(c.getInt(1),c.getInt(2),c.getString(3), c.getString(4)); //id,universityID,name
		Log.i("DB","sacando el campus"+ret.getName());

		db.close();

		return ret;
	}



	public Building[] getBuildings(Context context){	



		PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context, "DBPois", null,1);
		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();    		
		Cursor c = db.rawQuery("SELECT * FROM Buildings", null);
		int bCount= c.getCount();

		if(bCount == 0){
			Log.e("ERROR", "NO ENTRA");
			return null;
		}

		Building[] ret = new Building[bCount];
		int count=0;
		while(c.moveToNext()){

			double x = c.getDouble(5);
			double y = c.getDouble(6);
			Point center = new Point(x, y);
			ret[count] = new Building(context,c.getInt(0),c.getString(1),c.getString(2), c.getString(3),
					c.getString(4), center);
			count++;
			Log.e("","QWERTYUIOP");
		}
		db.close();
		return ret;

	}

	public BannerElement[] getBannerElements(){
		BannerSQLiteHelper sesdbh = new BannerSQLiteHelper(context, "banner_elements", null,1);
		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getReadableDatabase();    		
		Cursor c = db.rawQuery("SELECT * FROM banner_elements WHERE active = 1", null);
		int elementsCount= c.getCount();
		if(elementsCount == 0){
			BannerElement[] ret = new BannerElement[1];
			ret[0] = new BannerElement(1,"Feria de Sustentabilidad", "http://junkitechture.files.wordpress.com/2013/03/afiche-feria-de-sustentabilidad-uc-2013.png?w=710", true, BannerElement.Type.advertise);
			return ret;

		}

		BannerElement[] ret = new BannerElement[elementsCount];
		int count=0;
		while(c.moveToNext()){
			BannerElement.Type t = BannerElement.Type.advertise;

			if(c.getInt(4)==1){

				String type = c.getString(5);
				if(type.equalsIgnoreCase("survey")){
					t=BannerElement.Type.survey;
				}
				else if(type.equalsIgnoreCase("report")){
					t=BannerElement.Type.report;
				}
				else if(type.equalsIgnoreCase("event")){
					t=BannerElement.Type.event;
				}
				else if(type.equalsIgnoreCase("advertise")){
					t=BannerElement.Type.advertise;
				}
				//en la base de datos estan con este orden:id, web_id,name,url,active,type
				ret[count] = new BannerElement(c.getInt(1),c.getString(2),c.getString(3),true,t); //contructor> id,name,url,active,type

			}
			count++;
		}
		for(int i=0;i<ret.length;i++){
			Log.i("BANNER",ret[i].toString());

		}
		db.close();
		return ret;
	}


	public boolean dropBannerTable(Context context){		

		BannerSQLiteHelper sesdbh = new BannerSQLiteHelper(context, "banner_elements", null,1);
		//Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase(); 
		db.execSQL("DROP TABLE IF EXISTS banner_elements");
		sesdbh.onCreate(db);
		db.close();
		return true;      
	}
}

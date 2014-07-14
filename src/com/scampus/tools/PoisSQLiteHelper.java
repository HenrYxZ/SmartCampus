package com.scampus.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PoisSQLiteHelper extends SQLiteOpenHelper {

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DESC = "description";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_CAMPUS = "campus";
	public static final String COLUMN_LAT = "lat";
	public static final String COLUMN_LNG = "lng";
	private static final String COLUMN_POLY = "polygon";
	private static final int DATABASE_VERSION = 1;
	private static final String COLUMN_ID_POI = "id_poi";
	private static final String COLUMN_NAME_CAT = "name_cat";
	private static final String COLUMN_ID_UNI = "id_university";
	private static final String COLUMN_ID_POR = "id_por";
	private static final String COLUMN_NAME_TYPE = "name_type";
	private static final String COLUMN_CENTERX = "centerx";
	private static final String COLUMN_CENTERY = "centery";
	private static final String COLUMN_URL = "url";
	private static final String COLUMN_SOURCE = "source";
	private static final String COLUMN_PLACE_ID = "place_id";
	private static final String COLUMN_PLACE_TYPE = "place_type";
	private static final String COLUMN_START_DATE = "start_date";
	private static final String COLUMN_END_DATE = "end_date";


	//Sentencia SQL para crear la tabla de Pois
	String sqlCreate1 = "CREATE TABLE Pois (" +
			COLUMN_ID+" integer primary key autoincrement," +
			COLUMN_NAME+" text, " +
			COLUMN_DESC+" text, " +
			COLUMN_TYPE+" text, " +
			COLUMN_CAMPUS+" text, " +
			COLUMN_LAT+" DOUBLE, " +
			COLUMN_LNG+" DOUBLE)";

	//Sentencia SQL para crear la tabla de Pors
	String sqlCreate2 = "CREATE TABLE Pors (" +
			COLUMN_ID+" integer primary key autoincrement," +
			COLUMN_DESC+" text, " +
			COLUMN_CAMPUS+" text, " +
			COLUMN_LAT+" DOUBLE, " +
			COLUMN_LNG+" DOUBLE)";

	//Sentencia SQL para crear la tabla de Buildings
	String sqlCreate3 = "CREATE TABLE Buildings (" +
			COLUMN_ID+" integer primary key autoincrement," +
			COLUMN_NAME+" text, " +
			COLUMN_DESC+" text, " +
			COLUMN_CAMPUS+" text, " +
			COLUMN_POLY+" text, " +
			COLUMN_CENTERX+" DOUBLE, " +
			COLUMN_CENTERY+" DOUBLE)";


	//Sentencia SQL para crear la tabla de Events
	String sqlCreate4 = "CREATE TABLE Events (" +
			COLUMN_ID+" integer primary key autoincrement," +
			COLUMN_NAME+" text, " +
			COLUMN_DESC+" text, " +
			COLUMN_CAMPUS+" text, " +
			COLUMN_LAT+" DOUBLE, " +
			COLUMN_LNG+" DOUBLE, " +
			COLUMN_START_DATE+" text, "+
			COLUMN_END_DATE+" text)";

	String sqlCreate5 = "CREATE TABLE Categories (" +
			COLUMN_ID_POI+" integer," +
			COLUMN_NAME_CAT+" text)";
	
	String sqlCreate6 = "CREATE TABLE Claim_types (" +
			COLUMN_ID+" integer primary key autoincrement," +
			COLUMN_ID_UNI+" integer," +
			COLUMN_NAME_CAT+" text)";
	
	String sqlCreate7 = "CREATE TABLE Dump_types (" +
			COLUMN_ID_POR+" integer," +
			COLUMN_NAME_TYPE+" text)";
	
	String sqlCreate8 = "CREATE TABLE ExternalLinks (" +
			COLUMN_ID + " integer primary key autoincrement, " +
			COLUMN_NAME + " text, " +
			COLUMN_URL +  " text, " +
			COLUMN_TYPE + " text, " +
			COLUMN_PLACE_ID + " integer, " +
			COLUMN_PLACE_TYPE + " text)";
	
	String sqlCreate9 = "CREATE TABLE LocalLinks (" +
			COLUMN_ID + " integer primary key autoincrement, " +
			COLUMN_NAME + " text, " +
			COLUMN_URL +  " text, " +
			COLUMN_TYPE + " text, " +
			COLUMN_PLACE_ID + " integer, " +
			COLUMN_PLACE_TYPE + " text)";

	public PoisSQLiteHelper(Context contexto, String nombre,
			CursorFactory factory, int version) {
		super(contexto, nombre, factory, DATABASE_VERSION );

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//Se ejecuta la sentencia SQL de creación de la tabla
		db.execSQL(sqlCreate1);
		db.execSQL(sqlCreate2);
		db.execSQL(sqlCreate3);
		db.execSQL(sqlCreate4);
		db.execSQL(sqlCreate5);
		db.execSQL(sqlCreate6);
		db.execSQL(sqlCreate7);
		db.execSQL(sqlCreate8);
		db.execSQL(sqlCreate9);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
		//NOTA: Aquí utilizamos directamente la opción de
		//      eliminar la tabla anterior y crearla de nuevo vacía con el nuevo formato.
		//      Sin embargo lo normal será que haya que migrar datos de la tabla antigua
		//      a la nueva, por lo que este método debería ser más elaborado.

		//Se elimina la versión anterior de la tabla
		db.execSQL("DROP TABLE IF EXISTS Pois");
		db.execSQL("DROP TABLE IF EXISTS Pors");
		db.execSQL("DROP TABLE IF EXISTS Buildings");
		db.execSQL("DROP TABLE IF EXISTS Events");
		db.execSQL("DROP TABLE IF EXISTS Categories");
		db.execSQL("DROP TABLE IF EXISTS Claim_types");
		db.execSQL("DROP TABLE IF EXISTS Dump_types");
		db.execSQL("DROP TABLE IF EXISTS ExternalLinks");
		db.execSQL("DROP TABLE IF EXISTS LocalLinks");
		
		//Se crea la nueva versión de la tabla
		db.execSQL(sqlCreate1);
		db.execSQL(sqlCreate2);
		db.execSQL(sqlCreate3);
		db.execSQL(sqlCreate4);
		db.execSQL(sqlCreate5);
		db.execSQL(sqlCreate6);
		db.execSQL(sqlCreate7);
		db.execSQL(sqlCreate8);
		db.execSQL(sqlCreate9);
	}

}
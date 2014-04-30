package com.scampus.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
 
public class CampusSQLiteHelper extends SQLiteOpenHelper {
 
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_WEB_ID = "web_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_UNIVERSITY_ID = "university_id";
	private static final String COLUMN_POLY = "polygon";
	
	private static final int DATABASE_VERSION = 1;
	

	
    //Sentencia SQL para crear la tabla de Pois
	String sqlCreate = "CREATE TABLE campuses (" +
    		COLUMN_ID+" integer primary key," +
    		COLUMN_WEB_ID+" integer UNIQUE," +
			COLUMN_UNIVERSITY_ID+" integer,"+
    		COLUMN_NAME+" text," +
    		COLUMN_POLY+" text)";
	
   
 
    public CampusSQLiteHelper(Context contexto, String nombre,
                               CursorFactory factory, int version) {
        super(contexto, nombre, factory, DATABASE_VERSION );
       
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Se ejecuta la sentencia SQL de creaci�n de la tabla
        db.execSQL(sqlCreate);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        //NOTA: Aqu� utilizamos directamente la opci�n de
        //      eliminar la tabla anterior y crearla de nuevo vac�a con el nuevo formato.
        //      Sin embargo lo normal ser� que haya que migrar datos de la tabla antigua
        //      a la nueva, por lo que este m�todo deber�a ser m�s elaborado.
 
        //Se elimina la versi�n anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS campuses");
 
        //Se crea la nueva versi�n de la tabla
        db.execSQL(sqlCreate);
    }
}
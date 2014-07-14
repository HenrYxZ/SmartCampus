package com.scampus.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
 
public class BannerSQLiteHelper extends SQLiteOpenHelper {
 
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_WEB_ID = "web_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_URL = "url";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_LINK = "link";
	public static final String COLUMN_TYPE = "type";

	
	private static final int DATABASE_VERSION = 1;

	
    //Sentencia SQL para crear la tabla de Pois
	String sqlCreate = "CREATE TABLE banner_elements (" +
    		COLUMN_ID+" integer primary key," +
    		COLUMN_WEB_ID+" integer," +
			COLUMN_NAME+" text,"+
			COLUMN_URL+" text,"+
			COLUMN_STATUS+" integer,"+
			COLUMN_LINK+" link,"+
    		COLUMN_TYPE+" text)";
	
   
 
    public BannerSQLiteHelper(Context contexto, String nombre,
                               CursorFactory factory, int version) {
        super(contexto, nombre, factory, DATABASE_VERSION );
        
        
       
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Se ejecuta la sentencia SQL de creación de la tabla
        db.execSQL(sqlCreate);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        //NOTA: Aquí utilizamos directamente la opción de
        //      eliminar la tabla anterior y crearla de nuevo vacía con el nuevo formato.
        //      Sin embargo lo normal será que haya que migrar datos de la tabla antigua
        //      a la nueva, por lo que este método debería ser más elaborado.
 
        //Se elimina la versión anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS banner_elements");
 
        //Se crea la nueva versión de la tabla
        db.execSQL(sqlCreate);
    }
    
    public void Update(SQLiteDatabase db){
    	 db.execSQL("DROP TABLE IF EXISTS banner_elements");
    	 db.execSQL(sqlCreate);
    }
    
  
}
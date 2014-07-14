package com.scampus.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
 
public class SuggestionSQLiteHelper extends SQLiteOpenHelper{
	
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_ANSWER = "answer";
	
	private static final int DATABASE_VERSION = 1;

	
    //Sentencia SQL para crear la tabla de Suggestion
	String sqlCreate = "CREATE TABLE suggestions (" +
    		COLUMN_ID+" text," +
			COLUMN_DESCRIPTION+" text,"+
    		COLUMN_STATUS+" integer,"+
			COLUMN_ANSWER+" text)";
   
 
    public SuggestionSQLiteHelper(Context contexto, String nombre,
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
        db.execSQL("DROP TABLE IF EXISTS suggestions");
 
        //Se crea la nueva versión de la tabla
        db.execSQL(sqlCreate);
    }
}

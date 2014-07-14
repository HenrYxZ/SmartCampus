package com.scampus.tools;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.scampus.uc.R;

public class BannerElement {
	private int id;
	private String name;
	public String url;
	private int status;
	public String link;// solo publicidad y noticias (reports y advertises) lo
						// tienen

	public enum Type {
		survey, report, event, advertise
	};

	private Type type;

	public BannerElement(int id, String name, String url, int status, Type type) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.status = status;
		this.type = type;
	}

	public BannerElement(int id, String name, String url, boolean active,
			Type type) {
		this.id = id;
		this.name = name;
		this.url = url;
		if (active == true)
			this.status = 1;
		else
			this.status = 0;
		this.type = type;
	}

	public int getID() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getUrl(Context context) {
		if (this.url == null)
			return null;
		return this.url;
	}

	public int isActive() {
		return this.status;
	}

	public String toString() {
		return this.id + " \n nombre: " + this.name + " \n url: " + this.url
				+ " \n activo: " + this.status + " \n tipo: " + this.type;
	}

	public Type getType() {
		return this.type;
	}

	public void saveElement(Context context) {
		// Crea la base de datos DBPois version 1 (si se cambia la version hay
		// que implementar el onUpdate del PoisSQLiiteHelper)
		BannerSQLiteHelper sesdbh = new BannerSQLiteHelper(context,
				"banner_elements", null, 1);

		// Abre la base de datos
		SQLiteDatabase db = sesdbh.getWritableDatabase();
		int status = 0;// no esta activo
		if (this.status == 1)
			status = 1;// esta activo (en sqlite3 no hay booleans)
		try {
			db.execSQL("INSERT INTO banner_elements (web_id, name, url, status, link, type) "
					+ "VALUES ("
					+ this.getID()
					+ ", '"
					+ this.getName()
					+ "', '"
					+ this.url
					+ "', "
					+ status
					+ ", '"
					+this.link
					+"', '"
					+ this.type.toString() + "')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
	}
}


//Esta es la clase encargada de manejar todo lo relacionado con el MAPA.

package com.scampus.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.scampus.especial1.R;
import com.scampus.especial1.SConstants;
import com.scampus.tools.Building;
import com.scampus.tools.Campus;
import com.scampus.tools.DBHelper;
import com.scampus.tools.Map;
import com.scampus.tools.PoisSQLiteHelper;
import com.scampus.tools.User;
import com.scampus.tools.dontShowAgain;
import com.scampus.tools.MenuHelper;
//TODAVIA NO CIERRO LA BD
public class mapActivity extends Activity implements SConstants, OnCheckedChangeListener  {
	SlidingMenu menu;
	LatLng myLocation;
	CheckBox[] ch;
	private GoogleMap map;
	SearchView a;
	PoisSQLiteHelper sesdbh;
	SQLiteDatabase db;
	ProgressDialog pd;
	private User current_user;
	private RequestQueue requestQueue; //volley para mandar requests al servidor
	boolean b_visible= false;
	Map mapHelper;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		setUpMapIfNeeded();
		menu= (SlidingMenu) new MenuHelper().create(this, 2, map);
		map.setMapType(1);
		mapHelper= new Map();
		current_user = new User(this);
		int campus_id = current_user.getCampus().getID();
		DBHelper dbh = new DBHelper(this);
		Campus[] campus = dbh.getCampus(campus_id);
		PolygonOptions options = new PolygonOptions();
		options.addAll(new Map().decodePoly(campus[0].getPolygon()));
		options.strokeWidth(4);
		options.visible(true);
		Bundle b = getIntent().getExtras();
		int event_id = b.getInt("event_id");
		if(event_id != -1){
			map = mapHelper.createMap(db,map, this, event_id);

		}
		else{
			map = mapHelper.createMap(db,map, this);

		}
		map.addPolygon(options);

		//Crea la base de datos DBPois version 1 (si se cambia la version hay que implementar el onUpdate del PoisSQLiiteHelper)
		sesdbh = new PoisSQLiteHelper(this, "DBPois", null,1);
		//Abre la base de datos
		db = sesdbh.getWritableDatabase();

		Cursor c = db.rawQuery(" SELECT DISTINCT name_cat FROM Categories", null);
		int cursorcount= c.getCount();
		ch=  new CheckBox[cursorcount+2];
		//		El +1 es por los PORS, mas adelante revisar si es que hay pors
		try
		{
			//			ch[0].setOnCheckedChangeListener(allChange());
			for (int j=0; j<cursorcount+2; j++)
			{
				ch[j]= (CheckBox) findViewById(1001+j);
				ch[j].setOnCheckedChangeListener(this);
			}
		}
		catch(Exception e)
		{

		}
		
		//Creación del dialogo con información. Con opción de no mostrar de nuevo, guarda esta info en Sharedepreferences
		//Llama a la clase dontShowAgain, que es la encargada de controlar todos estos dialogos		
		new dontShowAgain().dialog(mapActivity.this, "Usa el buscador para encontrar la ubicación de salas, puntos de " +
				"reciclaje, baños u otros puntos de interés. También puedes filtrarlos por categorías.");



	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);

		SearchManager searchManager =
				(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView =
				(SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(
				searchManager.getSearchableInfo(getComponentName()));


		return true;
	} 



	//Al apretar home(logo), abrir o cerrar el menu lateral	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.info:
			LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

			View content = inflater.inflate( R.layout.dialog_infography, null );
			new AlertDialog.Builder(this) 
			.setTitle("Info")
			.setView(content)
			.setPositiveButton("Ok",null)
			.show();
			break;

		case android.R.id.home:
			menu.toggle();
			break;
		case R.id.buildings:
			mapHelper.setVisible(b_visible);
			b_visible=!b_visible;
			break;




		}
		return true;

	}


	//Cerrar menu lateral al apretar Back	
	@Override
	public void onBackPressed() {
		if (menu.isMenuShowing()) {
			menu.showContent(true);
			return;
		}
		super.onBackPressed();
	}
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}
	//maneja el los intents
	private void handleIntent(Intent intent) {
		//si es que el intent viene de la busqueda

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY).toUpperCase();
			Toast.makeText(mapActivity.this, "Has buscado: "+query,
					Toast.LENGTH_LONG).show();
			map.clear();
			String[] args = new String[] {"%"+query+"%"};
			db = sesdbh.getWritableDatabase();
			Cursor c = db.rawQuery(" SELECT * FROM Pois WHERE name LIKE "+"'%"+query+"%'", null);
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
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_icon))
							);


				} while(c.moveToNext());
			}

			c = db.rawQuery(" SELECT * FROM Pors WHERE description LIKE "+"'%"+query+"%'", null);
			if (c.moveToFirst()) {

				//Recorremos el cursor hasta que no haya más registros
				do {
					Double lataux = Double.parseDouble(c.getString(3));;
					Double lngaux = Double.parseDouble(c.getString(4));;;
					String nameaux = c.getString(1);
					LatLng position = new LatLng(lataux,lngaux );
					map.addMarker(new MarkerOptions()
					.position(position)	
					.title(nameaux)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.recycle))
							);


				} while(c.moveToNext());
			}

			c = db.rawQuery(" SELECT * FROM Buildings WHERE name LIKE "+"'%"+query+"%'", null);
			if (c.moveToFirst()) {

				//Recorremos el cursor hasta que no haya más registros
				do {
					
					Building b = new Building();
					b.setPolygon(c.getString(4));
					map.addPolygon(b.createPolygonOptions());


				} while(c.moveToNext());
			}






		}
	}

	// Crea la instancia del mapa y llama a los google play services.

	private void setUpMapIfNeeded() {
		if(map ==null){

			//Intenta obtener el mapa desde la API de google
			map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
			//Si es que lo logra lo configura dependiendo de como queramos
			if(map != null){
				setUpMap();
			}
		}

	}
	private void setUpMap() {
		map.setMyLocationEnabled(true);		//Encuentra la ubicación actual del celular (No es muy preciso)

		//		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		//
		//		Criteria criteria = new Criteria();
		//		
		//		String provider = locationManager.getBestProvider(criteria, true);
		//		Location latLongaux = locationManager.getLastKnownLocation(provider);
		//		myLocation=new LatLng(latLongaux.getLatitude(), latLongaux.getLongitude());

		LatLng position = new LatLng(-33.49965, -70.61250);

		map.moveCamera(CameraUpdateFactory.newLatLng(position));
		map.animateCamera(CameraUpdateFactory.zoomTo(15));

	}



	//ESTE METODO SE PUEDE OPTIMIZAR MUCHISIMO
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		map.clear();
		String[] queryaux=new String[1];
		String[] queryaux2=new String[1];

		for (int j=0; j< ch.length; j++)
		{
			queryaux[0]=(String) ch[j].getText();
			if(ch[j].isChecked()){

				if(queryaux[0]=="All")
				{
					if(ch[0].isChecked())
					{
						for (int i=1; i< ch.length; i++)
						{
							if(!ch[i].isChecked())
								ch[i].setChecked(true);

						}
					}

				}
				if(queryaux[0]!="Pors" && queryaux[0]!="Buildings" && queryaux[0]!="All")
				{
					if(ch[0].isChecked())
						ch[0].setChecked(false);
					Cursor c = db.rawQuery(" SELECT id_poi FROM Categories WHERE name_cat=?", queryaux);

					if (c.moveToFirst()) {


						//Recorremos el cursor hasta que no haya más registros
						do {
							queryaux2[0]=(String) c.getString(0);
							Cursor d = db.rawQuery(" SELECT * FROM Pois WHERE id=?", queryaux2);
							if (d.moveToFirst()) {
								//Recorremos el cursor hasta que no haya más registros
								do {
									Double lataux = Double.parseDouble(d.getString(5));;
									Double lngaux = Double.parseDouble(d.getString(6));;;
									String nameaux = d.getString(1);
									LatLng position = new LatLng(lataux,lngaux );
									map.addMarker(new MarkerOptions()
									.position(position)
									.title(nameaux)
									.icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_icon))
											);


								} while(d.moveToNext());
							}

						} while(c.moveToNext());
					}
				}
				if ((queryaux[0]=="Puntos de reciclaje"))
				{
					Cursor c = db.rawQuery(" SELECT * FROM Pors", null);



					if (c.moveToFirst()) {

						//Recorremos el cursor hasta que no haya más registros
						do {

							Double lataux = Double.parseDouble(c.getString(3));;
							Double lngaux = Double.parseDouble(c.getString(4));;;
							String nameaux = c.getString(1);
							LatLng position = new LatLng(lataux,lngaux );
							map.addMarker(new MarkerOptions()
							.position(position)	
							.title(nameaux)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.recycle))
									);


						} while(c.moveToNext());
					}
				}
				if ((queryaux[0]=="Edificios"))
				{
					Cursor c = db.rawQuery(" SELECT * FROM Buildings WHERE campus!=?", queryaux);



					if (c.moveToFirst()) {

						//Recorremos el cursor hasta que no haya más registros
						do {


							PolygonOptions options = new PolygonOptions();
							options.addAll(new Map().decodePoly(c.getString(4)));
							options.fillColor(0x7F00FF00);
							options.strokeWidth(2);
							map.addPolygon(options);





						} while(c.moveToNext());
					}
				}





			}
		}

	}



}




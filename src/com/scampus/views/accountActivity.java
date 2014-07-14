package com.scampus.views;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.Session;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.scampus.uc.R;
import com.scampus.tools.Campus;
import com.scampus.tools.DBHelper;
import com.scampus.tools.MenuHelper;
import com.scampus.tools.PoisSQLiteHelper;
import com.scampus.tools.RequestHandler;
import com.scampus.tools.University;
import com.scampus.tools.User;
import com.scampus.uc.MySimpleArrayAdapter;

import android.util.Log;


public class accountActivity extends Activity {
	private int vidrio;
	private int papel;
	private int plastico;
	private int latas;
	private int carton;
	private int pet;
	private int pilas;
	private int lastWeek;
	private int lastMonth;
	private int total;
	private RequestQueue requestQueue;
	private User current_user;
	SlidingMenu smenu;
	private University[] universities;
	private Campus[] campuses;
	private DBHelper dbh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);//LE SACA EL TITULO A LA APP
		setContentView(R.layout.activity_account);
		
		dbh = new DBHelper(this);
		
		smenu= (SlidingMenu) new MenuHelper().create(this, 1, null);
		 
		TabHost tabs=(TabHost)findViewById(android.R.id.tabhost);
		tabs.setup();
		 
		TabHost.TabSpec spec=tabs.newTabSpec("mitab1");
		spec.setContent(R.id.tab1);
		spec.setIndicator("JUEGO");
		tabs.addTab(spec);
		 
		spec=tabs.newTabSpec("mitab2");
		spec.setContent(R.id.tab2);
		spec.setIndicator("ACTIVIDAD");
//		spec.setIndicator("TAB2",
//	    res.getDrawable(android.R.drawable.ic_dialog_map));
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("mitab3");
		spec.setContent(R.id.tab3);
		spec.setIndicator("INFORMACION");
		tabs.addTab(spec);
		 
		tabs.setCurrentTab(0);

		
		requestQueue = Volley.newRequestQueue(this);
		//vamos a la vista de settings inicial si es necesario

		RequestHandler requestHandler = new RequestHandler(requestQueue);
		current_user = new User(this);
		requestHandler.requestGameStatus(current_user, this,new Callable<Void>(){
			public Void call(){
				return setGameInfo();
			}
		});
		
		requestHandler.requestUserRecycleInfo(current_user, this, new Callable<Void>(){
			public Void call(){
				return setActivityInfo();
			}
		});
		
		setUserInfo();
		
	}
	private Void setActivityInfo() {
		
		SharedPreferences settings = this.getSharedPreferences("user_info", 0);

		//recuperamos los atributos del usuario
		settings.getString("api_token", "");
		
		//TAB 2 para agregar los textview recycles
		LinearLayout tab2 = (LinearLayout)findViewById(R.id.tab2);
		
		HashSet<String> recycleSet = new HashSet<String>();
		recycleSet = (HashSet<String>) settings.getStringSet("Reciclajes",null);
		
		Iterator<String> iter = recycleSet.iterator();
	    while (iter.hasNext()) {
	        
	    	String key = iter.next();
	        String recycleNumber = settings.getString(key,"");
			int recycleInt = Integer.valueOf(recycleNumber);
			
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			
			TextView recycleTittle = new TextView(this);
			TextView recycle = new TextView(this);
			
			recycleTittle.setLayoutParams(params);
			recycle.setLayoutParams(params);
			
			recycleTittle.setTextAppearance(this, android.R.style.TextAppearance_Medium);
			recycle.setTextAppearance(this,  android.R.style.TextAppearance_Small);
			
			recycleTittle.setText(key);
			recycle.setText(recycleInt+" "+ key);
			
			tab2.addView(recycleTittle);
			tab2.addView(recycle);
	    }
	    
	    
	    TextView espacio = new TextView(this);
	    
	    tab2.addView(espacio);
		
		lastWeek =  settings.getInt("LastWeek",0);
		lastMonth =  settings.getInt("LastMonth",0);
		total =  settings.getInt("Total",0);
		
		
		TextView week = (TextView)findViewById(R.id.userInfoLastWeek);
		week.setText("Reciclaste "+lastWeek+" veces");
		
		TextView month = (TextView)findViewById(R.id.userInfoLastMonth);
		month.setText("Reciclaste "+lastMonth+" veces");
		
		TextView all = (TextView)findViewById(R.id.userInfoAllTimes);
		all.setText("Desde que usas Smart Campus has reciclado "+total+" veces");
		
		return null;
	}
	
	private Void setUserInfo() {
		
		TextView name = (TextView)findViewById(R.id.userInfoName);
		name.setText(current_user.getFirstName());
		TextView lastName = (TextView)findViewById(R.id.userInfoLastName);
		lastName.setText(current_user.getLastName());
		
		String university="";

		//Spinner univerdidades
		setUniversitySpinner();
		setCampusSpinner(university);
		
		return null;
	
	}
	
	private Void setGameInfo(){
		
		ListView friends = (ListView) findViewById(R.id.gameList);
		
		
		TextView points = (TextView)findViewById(R.id.gameInfoPoints);
		points.setText(" "+current_user.getPoints());
		TextView position = (TextView)findViewById(R.id.gameInfoPosition);
		position.setText(" "+current_user.getPosition());
		SharedPreferences settings = this.getSharedPreferences("user_info", 0);
		int count = settings.getInt("friends_count", 0);
		String[] pics = new String[count];
		String[] names = new String[count];
		int[] positions = new int[count];
		int[] pointss = new int[count];
		if(count>0){
			for(int i = 0; i<count;i++){
				pics[i] = settings.getString("url_"+i, " ");
				names[i] = settings.getString("name_"+i, " ");
				positions[i] = settings.getInt("ranking_"+i, 1);
				pointss[i] = settings.getInt("points_"+i, 1);
			}
		}
	

		
		MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(getBaseContext(),names,pics,positions,pointss);
		friends.setAdapter(adapter);

		
		return null;
	}
	
	private Void setUniversitySpinner(){
		universities = dbh.getUniversities();
		
		Spinner universitySpinner = (Spinner)findViewById(R.id.universitySpinner);
		List<String> universitiesList = new ArrayList<String>();
		
		//Agrega primero la universidad a la que pertenece.
		if(current_user.getUniversity().getName()!=null)
		universitiesList.add(current_user.getUniversity().getName());
		
			for (int i=0; i<universities.length; i++) {
				//Si es la universidad a la que pertenece no la vuelve a agregar
				if(!universitiesList.contains(universities[i].getName()))
				universitiesList.add(universities[i].getName());
			}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_item, universitiesList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		universitySpinner.setAdapter(dataAdapter);
		
		//Para cuando se cambie la universidad
		addListenerOnSpinnerItemSelectionUniversity(universitySpinner); 
		
		return null;
	}
	
	private Void setCampusSpinner(String university){
		
		campuses = dbh.getCampus(current_user.getUniversity().getID());
		
		if(campuses!=null){
			Spinner campusSpinner = (Spinner)findViewById(R.id.campusSpinner);
			List<String> campusList = new ArrayList<String>();
			
			//Agrega primero el campus a la que pertenece.
			if(current_user.getCampus().getName()!=null && university != "" && university == current_user.getUniversity().getName())
			campusList.add(current_user.getCampus().getName());
			
				for (int i=0; i<campuses.length; i++) {
					//Si es el campus al que pertenece no la vuelve a agregar
					if(!campusList.contains(campuses[i].getName()))
						campusList.add(campuses[i].getName());
				}
	
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, campusList);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			campusSpinner.setAdapter(dataAdapter); 
			
			addListenerOnSpinnerItemSelectionCampus(campusSpinner); 
		}
		
		return null;
	}
	
	public void addListenerOnSpinnerItemSelectionUniversity(Spinner s) {
		
		s.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
            	
            	String universityName = arg0.getItemAtPosition(arg2).toString();
            	setUniversity(universityName);
            	setCampusSpinner(universityName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            	
            }
        });
		
		
	 }
	
	public void addListenerOnSpinnerItemSelectionCampus(Spinner s) {
		
		s.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
            	
            	String campusName = arg0.getItemAtPosition(arg2).toString();
            	setCampus(campusName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            	
            }
        });
	 }
	
	private void setUniversity(String universityName) {
		//realiza el cambio en la memoria del celular
		for(int i = 0;i<universities.length; i++){
			if(universities[i].getName().equalsIgnoreCase(universityName))
				current_user.setUniversity(universities[i]);
				current_user.saveUser(this);
				Log.i("INIT","Asiganada la "+current_user.getUniversity().toString());
		}
	}
	
	private void setCampus(String campusName) {
		//realiza el cambio en la memoria del celular
		for(int i = 0;i<campuses.length; i++){
			if(campuses[i].getName().equalsIgnoreCase(campusName) && campuses[i].getUniversityID() == current_user.getUniversity().getID())
				current_user.setCampus(campuses[i]);
				current_user.saveUser(this);
				Log.i("INIT","Asiganada al campus "+current_user.getCampus().toString()+" id: "+current_user.getCampus().getID());	
		}
		//realiza el cambio en el servidor
		RequestHandler requestHandler = new RequestHandler(requestQueue);
		requestHandler.editCampus(current_user, this,new Callable<Void>(){
			public Void call(){
				loadNewMapPoints();
				return null;
			}
		});
		
		//Carga los campus para obtener sus poligonos
		requestHandler.requestCampus(current_user, this,new Callable<Void>(){
			public Void call(){
				//return setGameInfo();
				return null;
			}
		});
		
		
	}
	
	private void loadNewMapPoints() {
		
		//Drop table points
		PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(this,"DBPois", null, 1);
		SQLiteDatabase db = sesdbh.getWritableDatabase();
		
		db.execSQL("DELETE FROM Pois");
		db.execSQL("DELETE FROM PoRs");
		db.execSQL("DELETE FROM Events");
		db.execSQL("DELETE FROM Buildings");
		db.execSQL("DELETE FROM Categories");
		db.execSQL("DELETE FROM Dump_types");
		
		//request map info
		RequestHandler requestHandler = new RequestHandler(requestQueue);
		requestHandler.requestMapInfo(current_user, this);
	}
	
	
	//-----------------SLIDING MENU----------------------------------------
	
	//Al apretar home(logo), abrir o cerrar el menu lateral	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	case android.R.id.home:
			smenu.toggle();
			break;
		}
		return true;

	}
		
	/*public void onClick_logout(View v)
	{
		smenu.toggle();
		current_user.cleanUniversity(this);
		this.getActionBar().setDisplayHomeAsUpEnabled(false);
		Log.i("SESSION", "Se disparo el evento onOptionsItemSelected...");
		//Buscamos la session del usuario actual.
		Session session = Session.getActiveSession();
		//agregamos este log para probar que pasa con las token y como se comportan
		Log.i("SESSION", session.getAccessToken());
		//Cerramos la sesion del usuario
		session.closeAndClearTokenInformation();
	}*/
	//metodo del boton de reciclar
	public void onClick_recycle(View v)
	{
		if (smenu.isMenuShowing()) 
			smenu.showContent(true);
		Intent i = new Intent(this, com.abhi.barcode.fragment.QrCode.class);

		startActivity(i);
		this.finish();
	}
	//metodo del boton de mapa
	public void onClick_map(View v)
	{
		if (smenu.isMenuShowing()) 
			smenu.showContent(true);

		Intent i = new Intent(this, mapActivity.class);

		Bundle b = new Bundle();					
		b.putInt("event_id", -1); //pasamos un -1 ya que no se pasa un evento en particular a mostrar en el mapa
		i.putExtras(b);
		startActivity(i);
		this.finish();
	}
	//metodo del boton de sugerencias
	public void onClick_suggestion(View v)
	{
		if (smenu.isMenuShowing()) 
			smenu.showContent(true);

		Intent i = new Intent(this, suggestionActivity.class);
		startActivity(i);
		this.finish();
	}
	
	//metodo del boton de perfil o cuenta del usuario
	public void onClick_account(View v)
	{
		if (smenu.isMenuShowing()) 
			smenu.showContent(true);

		Intent i = new Intent(this, accountActivity.class);
		startActivity(i);
		this.finish();
	}
	//metodo del boton de denuncias
	public void onClick_claims(View v)
	{
		// *********************************************************************************
		// CODIGO FUERA DE ESTANDAR
		// *********************************************************************************
		// *********************************************************************************
		if (smenu.isMenuShowing()) 
			smenu.showContent(true);

		PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(this, "DBPois", null,1);;
		SQLiteDatabase db = sesdbh.getWritableDatabase();

		Cursor c = db.rawQuery(" SELECT * FROM Claim_types ", null);
		String[] claim_types = new String[c.getCount()];
		int i=0;
		if (c.moveToFirst()) {
			//Recorremos el cursor hasta que no haya más registros
			do {
				Log.e("CAT",c.getString(2));
				claim_types[i]=c.getString(2);
				i++;

			} while(c.moveToNext());
		}

		Intent myIntent = new Intent(this, claimsActivity.class);
		myIntent.putExtra("claim_types",claim_types);

		startActivity(myIntent);
		this.finish();
	}
	
	
}
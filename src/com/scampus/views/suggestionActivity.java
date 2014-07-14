package com.scampus.views;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.scampus.uc.R;
import com.scampus.tools.DBHelper;
import com.scampus.tools.MenuHelper;
import com.scampus.tools.PoisSQLiteHelper;
import com.scampus.tools.RequestHandler;
import com.scampus.tools.Suggestion;
import com.scampus.tools.User;
import com.scampus.tools.suggestionArrayAdapter;

public class suggestionActivity extends Activity{
	
	private User current_user;
	private DBHelper dbh;
	private RequestQueue requestQueue;
	private Suggestion suggestions[];
	SlidingMenu smenu;
	Button button;

	suggestionArrayAdapter listAdapter;

	ArrayList<String> suggestionsListForAdapter;
	ArrayList<String> suggestionsValues;
	TabHost tabs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);//LE SACA EL TITULO A LA APP
		setContentView(R.layout.activity_suggestion);
		
		dbh = new DBHelper(this);
		smenu= (SlidingMenu) new MenuHelper().create(this, 1, null);
		requestQueue = Volley.newRequestQueue(this);
		
		suggestionsValues = new ArrayList<String>();
		
		button = (Button)findViewById(R.id.sendSuggestionButton);
		addListenerOnSendButton(button);
		addListenerOnListView();
		
		setTabHost();
		
		//Primero checkeamos los status para saber cuales han respondido los admin
		
		this.checkStatus();
	}
	

	public void setTabHost(){
		
		tabs=(TabHost)findViewById(android.R.id.tabhost);
		tabs.setup();
		 
		TabHost.TabSpec spec=tabs.newTabSpec("mitab1");
		spec.setContent(R.id.tab1);
		spec.setIndicator("Nueva sugerencia");
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("mitab2");
		spec.setContent(R.id.tab2);
		spec.setIndicator("Tus sugerencias");
		tabs.addTab(spec);
		
		tabs.setCurrentTab(0);
		
		
		//Listeners para esconder y mostrar el teclado.
		tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

		@Override
		public void onTabChanged(String tabId) {
		
			int i = tabs.getCurrentTab();
			EditText myEditText = (EditText) findViewById(R.id.suggestionText);  
			
				if (i == 0) {
					myEditText.setText("");
				}	
			    else if (i ==1) {
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);	
			    }
			  }
		});
	 
	}
	
	public void addListenerOnSendButton(Button b) {
		
		b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	sendSuggestion();
            	return;
            }
        });
		
		
	 }
	
	public void sendSuggestion(){
		
		RequestHandler requestHandler = new RequestHandler(requestQueue);
		current_user = new User(this);
		
		EditText suggestionText = (EditText)findViewById(R.id.suggestionText);
		
		String id = requestHandler.sendSuggestion(current_user, this, suggestionText.getText().toString());
		
		//Solo si hay conexion a internet
		if(id != "-1"){
			this.saveSuggestion(id);
			
			//Toast message para saber que ya se envio la sugerencia
			Toast.makeText(this, R.string.suggestion_sent, Toast.LENGTH_LONG).show();
		}
		
	}
	
	public void saveSuggestion(String id){
		
		EditText suggestionText = (EditText)findViewById(R.id.suggestionText);
	
		//insertar en base de datos.
		Suggestion newSuggestion = new Suggestion(id, suggestionText.getText().toString(), 0);
		newSuggestion.saveSuggestion(this);
		
		//actualiza las suggestions para que no se salga del arreglol
	
		this.suggestionsValues.add(newSuggestion.getDescription());
		if(suggestions!=null){
			listAdapter.setSuggestions(dbh.getSuggestions(this));
			listAdapter.notifyDataSetChanged();
		}
		else{
			suggestions = dbh.getSuggestions(this);
			listAdapter = new suggestionArrayAdapter(this,this.getSuggestionsValuesForAdapter(),suggestions);
			ListView listview = (ListView) findViewById(R.id.suggestion_listview);
			listview.setAdapter(listAdapter); 
		}	
		tabs.setCurrentTab(1);
	}
	

	public ArrayList<String> getSuggestionsValuesForAdapter()
	{	
		suggestions = dbh.getSuggestions(this);
		
		if(suggestions!=null){
		    for (int i = 0; i < suggestions.length; ++i) {
		    	this.suggestionsValues.add(suggestions[i].getDescription());
		    }
		    return suggestionsValues;
		}
		else return null;
	}
	
	public void checkStatus(){
		
		//Retorna true si hay conexion a internet y logra actualizar las sugerencias
		RequestHandler requestHandler = new RequestHandler(requestQueue);
		current_user = new User(this);
		
		requestHandler.getSuggestions(current_user, this, this.dbh, new Callable<Void>(){
			public Void call(){
				 upDateSuggestions();
				 return null;
			}
		});
		
	}
	
	public void addListenerOnListView(){
		
		ListView listview = (ListView) findViewById(R.id.suggestion_listview);
		listview.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				newIntentSuggestionDetails(position);
			    }
			});
	}
	
	public void newIntentSuggestionDetails(int position){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Respuesta:");
		String answer;
		try{
			answer= this.listAdapter.getSuggestions()[position].getAnswer();
		}
		catch (Exception e) {
			answer ="null";
		}

		if(answer.compareTo("null")!=0){
			builder.setMessage(answer);
		}
		else builder.setMessage(R.string.suggestion_no_response);
	
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public void upDateSuggestions(){
		
		suggestions = dbh.getSuggestions(this);	
		
		//seteamos el list view de las propias sugerencias
		ListView listview = (ListView) findViewById(R.id.suggestion_listview);
		
		if(suggestions!=null){
		    listAdapter = new suggestionArrayAdapter(this,this.getSuggestionsValuesForAdapter(),suggestions);
		    listview.setAdapter(listAdapter); 
		}
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

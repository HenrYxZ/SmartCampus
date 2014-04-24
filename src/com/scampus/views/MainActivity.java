package com.scampus.views;



import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.android.gms.maps.model.PolygonOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.scampus.especial1.BannerPager;
import com.scampus.especial1.R;
import com.scampus.tools.Map;
import com.scampus.tools.PoisSQLiteHelper;
import com.scampus.tools.RequestHandler;
import com.scampus.tools.User;
import com.scampus.tools.MenuHelper;


public class MainActivity extends FragmentActivity {

	private static final int SPLASH = 0;
	private static final int SELECTION = 1;
	//Esta constante se agrega para el fragmento de Settings
	private static final int SETTINGS = 2;
	//
	private static final int FRAGMENT_COUNT = SETTINGS +1;
	//Arreglo de fragmentos
	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
	//flag para indicar una activity visible
	private boolean isResumed = false;
	//esta variable es para hacer un listener que escuacha cuando hay un cambio en el estado de la sesion de FB
	private UiLifecycleHelper uiHelper;
	//se usa para gatillar el UserSettingsFragment
	private MenuItem settings;
	private MenuItem logout;
	//estos son para el scroll del banner
	private BannerPager banner;
	private RequestQueue requestQueue; //volley para mandar requests al servidor
	private User current_user;
	SlidingMenu smenu;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private boolean asked_once = false;
	private boolean first_time = true; //primera vez que hace login en la app.
	Context context;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		printKey(); //con este metodo se imprime la clave que se usa para facebook

		context= this;
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		//para manejar los tres fragmentos se usa FragmentManager

		current_user = new User(this);
		requestQueue = Volley.newRequestQueue(this);
		requestQueue.cancelAll("VOLLEY");

		RequestHandler requestHandler = new RequestHandler(requestQueue);


		FragmentManager fm = getSupportFragmentManager();
		fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);
		fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);
		fragments[SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);

		FragmentTransaction transaction = fm.beginTransaction();
		for(int i = 0; i < fragments.length; i++) {
			transaction.hide(fragments[i]);
		}
		transaction.commit();

		banner = (BannerPager)findViewById(R.id.mainBanner);

		if(current_user.hasApiToken(this)){
			Log.e("API TOKEN", current_user.getApiToken());
			requestHandler.requestMapInfo(current_user, this);
			requestHandler.requestClaimsCategories(current_user,this);
			banner.onCreate();
		}

		this.setBannerListenner();

	}


	private void printKey() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					"com.scampus.especial1", 
					PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.i("FACEBOOK", Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {

		} catch (NoSuchAlgorithmException e) {

		}
	}


	private void requestPublishPermissions() {
		Session session = Session.getActiveSession();
		if (session != null && !asked_once){

			// Check for publish permissions    
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				Session.NewPermissionsRequest newPermissionsRequest = new Session
						.NewPermissionsRequest(this, PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);

			}
			asked_once = true;
		}
	}


	private void goToInitialSetting() {
		current_user.retrieveUser(this);
		if((current_user.hasApiToken(this) && !current_user.hasUniversity()) ||  (current_user.hasApiToken(this) && !current_user.hasCampus())){

			Intent i = new Intent(this, initSetActivity.class);
			startActivity(i);
			//finish();
		}



	}


	public void setBannerListenner() {

		banner.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {


			}

			@Override
			public void onPageSelected(int pos) {

				banner.killTimer();//esto es para que las imagenes no salten al estar moviendolas con el dedo

				View v = findViewById(R.id.bannerImage);
				v.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						//creamos el intent para enviar a la vista de publicidad
						Intent i = new Intent(MainActivity.this,publicityActivity.class);
						Bundle b = new Bundle();
						int pos = banner.getCurrentItem();
						String type = banner.getElements()[pos].getType().toString();
						b.putString("image_source", banner.getElements()[pos].getUrl()); //pasamos parametros para la nueva actividad
						b.putString("image_type", type); //pasamos el tipo TODO:agarrar el tipo en
						b.putString("image_name", banner.getElements()[pos].getName());
						if(type.equalsIgnoreCase("event")){
							int o = banner.getElements()[pos].getID();
							b.putInt("event_id",o);
						}
						else
							b.putInt("event_id", -1);
						i.putExtras(b);
						startActivity(i);							
					}
				});

				banner.recreateTimer();

			}

		});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	//este metodo hace override para preparar como se muestra el menu de opciones
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// solo agrega el menu cuando el fragmento de SELECTION esta visible
		if (fragments[SELECTION].isVisible()) {
			if (menu.size() ==1) {	  
				Log.i("SESSION", "Se agrego boton settings");
				logout = menu.add(R.string.logout);

				this.getActionBar().setDisplayHomeAsUpEnabled(false);		

			}
			return true;
		} else {
			menu.clear();
			settings = null;
		}
		return false;
	}
	//este metodo se lanza cuando se apreta el menu de opciones de android
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {	
		switch (item.getItemId()) {
		case android.R.id.home:
			smenu.toggle();

		}	

		if(item.equals(logout)){
			//Buscamos la session del usuario actual.
			Session session = Session.getActiveSession();
			//agregamos este log para probar que pasa con las token y como se comportan
			Log.i("SESSION", session.getAccessToken());
			//Serramos la sesion del usuario
			session.closeAndClearTokenInformation();
			current_user.cleanUniversity(this);
			current_user = new User(this);

			return true;
		}
		else
			return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();//FACEBOOK
		banner.recreateTimer();//timer del banner
		isResumed = true;//banner
		current_user = new User(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		this.banner.killTimer();
		//metodo del UiHelper
		uiHelper.onPause();
		isResumed = false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
		banner.erase();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}
	//este metodo es para cuando los fragments esten recien instanciados
	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		Session session = Session.getActiveSession();


		if (session != null && session.isOpened()) {
			// Si la sesion esta abierta,
			// tratamos de abrir selection fragment: Este fragment es donde se muestra el menu de la apliacion y el banner
			showFragment(SELECTION, false);
			smenu= (SlidingMenu) new MenuHelper().create(this, 1,null);



		} else {
			// otherwise present the splash screen
			// and ask the person to login.
			showFragment(SPLASH, false);

		}
	}
	public void onClick_logout(View v)
	{
		smenu.toggle();
		current_user.cleanUniversity(this);
		this.getActionBar().setDisplayHomeAsUpEnabled(false);
		Log.i("SESSION", "Se disparo el evento onOptionsItemSelected...");
		//Buscamos la session del usuario actual.
		Session session = Session.getActiveSession();
		//agregamos este log para probar que pasa con las token y como se comportan
		Log.i("SESSION", session.getAccessToken());
		//Serramos la sesion del usuario
		session.closeAndClearTokenInformation();


	}
	//metodo del boton de reciclar
	public void onClick_recycle(View v)
	{
		if (smenu.isMenuShowing()) 
			smenu.showContent(true);
		Intent i = new Intent(this, com.abhi.barcode.fragment.QrCode.class);

		startActivity(i);
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
	}
	//metodo del boton de perfil o cuenta del usuario
	public void onClick_account(View v)
	{
		if (smenu.isMenuShowing()) 
			smenu.showContent(true);

		Intent i = new Intent(this, accountActivity.class);
		startActivity(i);
	}
	//metodo del boton de denuncias
	public void onClick_claims(View v)
	{
		if (smenu.isMenuShowing()) 
			smenu.showContent(true);

		PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context, "DBPois", null,1);;
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
	}
	//este metodo se llama cuando hay algun cambio en la sesion de facebook para manejar los problemas
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		// Solo hace cambios si la actividad esta visible
		current_user = new User(this);
		if (isResumed) {
			FragmentManager manager = getSupportFragmentManager();
			// Get the number of entries in the back stack
			int backStackSize = manager.getBackStackEntryCount();
			// Clear the back stack
			for (int i = 0; i < backStackSize; i++) {
				manager.popBackStack();
			}
			if (state.isOpened()) {

				//VALIDAMOS CON EL SERVIDOR QUE EL USUARIO QUE INICIA SESION ES VALIDO
				Log.i("SESSION", "Logged in...");
				// TODO: Agregar progress dialog
				this.validateUser(session,this);

				//pedimos los permisos para publicar en facebook
				requestPublishPermissions();

				if(current_user.hasUniversity() && current_user.hasCampus()){
					// Mostramos el menu princiapl de la aplicacion (SELECTION fragment)
					showFragment(SELECTION, false);
				}
				else if(!current_user.hasCampus()){

					this.goToInitialSetting();
				}

				//TODO: ver si hay reautorizaciones de permisos de usuarios para mandar la historia
				//a facebook nuevamente				

			} else if (state.isClosed()) {
				// Si la sesion esta cerrada:
				// Mostramos el fragment para iniciar sesion con facebook (SPLASH fragment)
				Log.i("SESSION", "Logged out...");
				showFragment(SPLASH, false);
			}


		}
	}

	//Este metodo muestra un fragment (que es una parte de una vista)
	private void showFragment(int fragmentIndex, boolean addToBackStack) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < fragments.length; i++) {
			if (i == fragmentIndex) {
				transaction.show(fragments[i]); 
			} else {
				transaction.hide(fragments[i]);
			}
		}
		if (addToBackStack) {
			transaction.addToBackStack(null);
		}
		transaction.commit();
	}
	//este es un listenter que avisa cuando hay un cambio de estado en la sesion
	//este metodo hace overide del metodo call()

	private Session.StatusCallback callback = 

			new Session.StatusCallback() {
		@Override
		public void call(Session session, 
				SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
	private void validateUser(Session session, final Context context) {
		JSONObject object = new JSONObject();
		try {
			object.put("access_token", session.getAccessToken());
		} catch (JSONException e1) {

			e1.printStackTrace();
		}

		String url = "http://especial1.ing.puc.cl/mobile_users/login";

		JsonObjectRequest jr = new JsonObjectRequest(com.android.volley.Request.Method.POST,url,object,new com.android.volley.Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				Log.i("Volley",response.toString());
				//				if(response.get("authenticated") == "true")
				//				validatedUser = true;
				//Como la respuesta es efectiva y se ha validado el usuario, creamos al usuario.

				try {
					if(current_user == null && response.getString("authenticated")=="true"){
						current_user= new User();
						current_user.setApiToken(response.getString("api_token"));
						current_user.saveUser(context);
						Log.i("Volley","Se creo un nuevo usuario");
					}
					else
					{
						current_user.setApiToken(response.getString("api_token"));
						current_user.saveUser(context);
						Log.i("Volley","Se reutilizo el usuario");
						Log.e("APITOKEN",current_user.getApiToken());

					}
					goToInitialSetting();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		},new com.android.volley.Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("VOLLEY","");
			}
		});
		requestQueue.add(jr);


	}

	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}
}
class UpdateTimeTask extends TimerTask  {
	private int[] elements;
	private ViewPager vp;

	public UpdateTimeTask(ViewPager vp, int[] elements){
		this.vp = vp;
		this.elements = elements;
	}
	public void run() {
		//Code for the viewPager to change view
		int elementsCount = elements.length;
		int current = vp.getCurrentItem();
		int next;
		if(current == elementsCount) next = 0;
		else next = current+1;
		vp.setCurrentItem(next, true);


	}


}




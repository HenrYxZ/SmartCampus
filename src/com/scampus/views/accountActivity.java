package com.scampus.views;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.scampus.especial1.MySimpleArrayAdapter;
import com.scampus.especial1.R;
import com.scampus.tools.MenuHelper;
import com.scampus.tools.RequestHandler;
import com.scampus.tools.User;


public class accountActivity extends Activity {
	private int vidrios;
	private int papel;
	private int plastico;
	private int latas;
	private int pilas;
	private int lastWeek;
	private int lastMonth;
	private int total;
	private RequestQueue requestQueue;
	private User current_user;
	SlidingMenu menu;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);//LE SACA EL TITULO A LA APP
		setContentView(R.layout.activity_account);
		
		menu= (SlidingMenu) new MenuHelper().create(this, 1, null);
		 
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
		requestHandler.requestUserInfo(current_user, this, new Callable<Void>(){
			public Void call(){
				return setUserInfo();
			}
		});
		


	}
	private Void setUserInfo() {
		
		SharedPreferences settings = this.getSharedPreferences("user_info", 0);

		//recuperamos los atributos del usuario
		settings.getString("api_token", "");
		
		vidrios = settings.getInt("Vidrios",0);
		papel =  settings.getInt("Papel",0);
		plastico =  settings.getInt("Plastico",0);
		latas =  settings.getInt("Latas",0);
		pilas =  settings.getInt("Pilas",0);
		lastWeek =  settings.getInt("LastWeek",0);
		lastMonth =  settings.getInt("LastMonth",0);
		total =  settings.getInt("Total",0);
		
		TextView name = (TextView)findViewById(R.id.userInfoName);
		name.setText(current_user.getFirstName());
		TextView lastName = (TextView)findViewById(R.id.userInfoLastName);
		lastName.setText(current_user.getLastName());
		TextView week = (TextView)findViewById(R.id.userInfoLastWeek);
		week.setText("Reciclaste "+lastWeek+" veces");
		TextView month = (TextView)findViewById(R.id.userInfoLastMonth);
		month.setText("Reciclaste "+lastMonth+" veces");
		TextView all = (TextView)findViewById(R.id.userInfoAllTimes);
		all.setText("Desde que usas Smart Campus has reciclado "+total+" veces");
		TextView glass = (TextView)findViewById(R.id.userInfoGlass);
		glass.setText(vidrios+" botellas");
		TextView paper = (TextView)findViewById(R.id.userInfoPaper);
		paper.setText(papel+" veces en contenedores de papel");
		TextView latas = (TextView)findViewById(R.id.userInfoCan);
		latas.setText(vidrios+" latas");
		TextView batery = (TextView)findViewById(R.id.userInfoBatery);
		batery.setText(pilas+" pilas");
		TextView plastic = (TextView)findViewById(R.id.userInfoPlastic);
		plastic.setText(plastico+" elementos de plástico");
		
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
	//Al apretar home(logo), abrir o cerrar el menu lateral	
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
		case android.R.id.home:
				menu.toggle();
				break;
			}
			return true;

		}
}
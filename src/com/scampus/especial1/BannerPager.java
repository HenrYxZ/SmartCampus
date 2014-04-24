package com.scampus.especial1;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.scampus.tools.BannerElement;
import com.scampus.tools.DBHelper;
import com.scampus.tools.RequestHandler;
import com.scampus.views.MainActivity;
import com.scampus.tools.User;


public class BannerPager extends ViewPager{

	private BannerElement[] elements;
	private Timer timer;
	private DBHelper dbh;
	boolean swipe = false;
	private MyPagerAdapter adapter;
	private RequestQueue requestQueue;
	private User current_user;
	private RequestHandler requestHandler;
	private Context context;




	public BannerPager(Context context, AttributeSet attrs) {
		super(context,attrs);
		this.context = context;
		//usamos dbh para sacar datos de la base de datos
		dbh = new DBHelper(context);
		

		//iniciamos el banner el banner se inicia una vez que los elementos del banner han llegado.
		requestQueue = Volley.newRequestQueue(context);
		requestHandler = new RequestHandler(requestQueue);
		current_user = new User(context);

		//nota>El timer se crea en el on create y on resume de la actividad main.


	}
	public void onCreate(){
		if(current_user.hasApiToken(context))
		requestHandler.requestBanner(current_user, context, new Callable<Void>(){
			public Void call(){
				return initializeBanner();
			}
		});
	}

	private Void initializeBanner() {
		
		//sacamos de la base de datos lo elementos a mostrar en el banner
		elements = dbh.getBannerElements();
		adapter = new MyPagerAdapter(elements, context);		    
		this.setAdapter(adapter);
		this.setCurrentItem(0);
		return null;
	}
	
	public void erase(){
		this.elements = null;
		dbh.dropBannerTable(this.context);
	}

	public BannerElement[] getElements(){
		return this.elements;

	}
	public void recreateTimer(){
		timer = new Timer();	
		final Handler handler = new Handler();
		final MyRunnable Update = new MyRunnable(this);


		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				handler.post(Update);
			}
		}, 10000, 10000);
	}
	public void killTimer(){
		this.timer.cancel();
		
	}


}

//handler para el timer que cambia las paginas
class MyRunnable implements Runnable {
	private BannerPager banner;
	public MyRunnable(BannerPager banner) {
		this.banner = banner;
	}

	public void run() {
		
		if(banner.getElements() != null){
			int currentPage = banner .getCurrentItem();
			int next = 0;
			if (currentPage != banner.getElements().length-1) {
				next = currentPage+1;
			}		
			banner.setCurrentItem(next, true); 
		}


	}
}

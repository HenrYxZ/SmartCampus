package com.scampus.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.Session;
import com.google.android.gms.maps.GoogleMap;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.scampus.uc.R;

public class MenuHelper {
	PoisSQLiteHelper sesdbh;
	SQLiteDatabase db;
	Context context;

	public SlidingMenu create(final Context context, int a, final GoogleMap map)
	{

		this.context=context;

		((Activity) context).getActionBar().setDisplayHomeAsUpEnabled(true);
		final SlidingMenu menu = new SlidingMenu(context);
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity((Activity) context, SlidingMenu.SLIDING_CONTENT);
		if (a==1)
		{
			menu.setMenu(R.layout.menu);
		}

		if (a==2)
		{
			int chkId = 1001;
		
			
			LinearLayout ll=new LinearLayout(context);
			ll.setOrientation(1);
			TextView tv = new TextView(context);
			tv.setText("FILTROS");
			tv.setPadding(10, 10, 0, 0);

			ll.addView(tv);
			menu.setMenu(ll);
			sesdbh = new PoisSQLiteHelper(context, "DBPois", null,1);
			db = sesdbh.getWritableDatabase();
			Cursor c = db.rawQuery(" SELECT DISTINCT name_cat FROM Categories", null);
			CheckBox[] ch=  new CheckBox[c.getCount()+2];
			ch[0] = new CheckBox(context);
			ch[0].setId(chkId);
			ch[0].setChecked(false);
			ch[0].setText("All");
			ll.addView(ch[0]);

			int i= 1;
			if (c.moveToFirst()) {

				//Recorremos el cursor hasta que no haya más registros
				do {

					ch[i] = new CheckBox(context);
					ch[i].setId(chkId+i);
					ch[i].setChecked(true);
					ch[i].setText(c.getString(0));
					ll.addView(ch[i]);
					i++;



				} while(c.moveToNext());

				ch[i] = new CheckBox(context);
				ch[i].setId(chkId+i);
				ch[i].setChecked(true);
				ch[i].setText("Puntos de reciclaje");
				ll.addView(ch[i]);

				
				
				final Button bmaptype = new Button(context);
				bmaptype.setText("Cambiar a Satelite");
				bmaptype.setBackgroundResource(R.drawable.button_menu);
				bmaptype.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				bmaptype.setOnClickListener(new OnClickListener() {
					public void onClick(View v)
					{
						int type= map.getMapType();
						if(type==1)
						{
							bmaptype.setText("Cambiar a Normal");
							map.setMapType(2);
						}
						if (type==2)
						{
							bmaptype.setText("Cambiar a Satelite");
							map.setMapType(1);
						}


					} 
				});
				ll.addView(bmaptype);
				
				
				Button button1 = new Button(context);
				button1.setText("Inicio");
				button1.setBackgroundResource(R.drawable.button_menu);
				button1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				button1.setOnClickListener(new OnClickListener() {
					public void onClick(View v)
					{
						Intent resultIntent = new Intent();
						((Activity) context).setResult(Activity.RESULT_OK, resultIntent);
						((Activity) context).finish();


					} 
				});
				ll.addView(button1);

				Button button2 = new Button(context);
				button2.setText("Logout");
				button2.setBackgroundResource(R.drawable.button_menu);
				button2.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				button2.setOnClickListener(new OnClickListener() {
					public void onClick(View v)
					{
						Intent resultIntent = new Intent();
						((Activity) context).setResult(Activity.RESULT_OK, resultIntent);
						((Activity) context).finish();
						menu.toggle();
						((Activity) context).getActionBar().setDisplayHomeAsUpEnabled(false);
						Log.i("SESSION", "Se disparo el evento onOptionsItemSelected...");
						//Buscamos la session del usuario actual.
						Session session = Session.getActiveSession();
						//agregamos este log para probar que pasa con las token y como se comportan
						Log.i("SESSION", session.getAccessToken());
						//Serramos la sesion del usuario
						session.closeAndClearTokenInformation();



					} 
				});
				ll.addView(button2);

				




			}
			db.close();



		}

		return menu;

	}


}

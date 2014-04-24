package com.scampus.tools;

import java.util.concurrent.Callable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.GoogleMap;



public class RequestHandler {
	//esta clase se encarga de manejar los post y get entre la aplicacion android y el backend.
	private RequestQueue requestQueue;
	public  GoogleMap map;
	public RequestHandler(RequestQueue requestQueue){
		this.requestQueue = requestQueue;
	}


	public void requestUniversities(User current_user,final Context context, final Callable<Void> callback) {

		String url = "http://especial1.ing.puc.cl/api/get_universities/"+current_user.getApiToken();

		final ProgressDialog progress = ProgressDialog.show(context, "Cargando universidades",
				null, true);

		JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray> () {
			@Override
			public void onResponse(JSONArray response) {
				try {
					for (int i = 0; i < response.length(); i++) {
						JSONObject jsonObject = response.getJSONObject(i);//cada universidad representa un objeto
						//guardamos las universidades existentes en la base de datos
						University u = new University();
						u.setAcronym(jsonObject.getString("acronym"));
						u.setID(jsonObject.getInt("id"));
						u.setName(jsonObject.getString("name"));
						u.saveUniversity(context); 	                
					}

					if(callback != null)
						callback.call();

				} catch (JSONException e) {
					e.printStackTrace();

				} catch (Exception e) {
					
					e.printStackTrace();
				}
				finally{
					progress.dismiss();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("REQUEST", error.getMessage());
			}
		});

		requestQueue.add(req);

	}

	public void requestUniversitiesAndCampus(User current_user,final Context context) {
		///api/get_universitites_and_campuses/:api_token
		String url = "http://especial1.ing.puc.cl/api/get_universities_and_campuses/"+current_user.getApiToken();

		JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray> () {
			@Override
			public void onResponse(JSONArray response) {

				try {
					for (int i = 0; i < response.length(); i++) {
						JSONObject jsonObject = response.getJSONObject(i);//cada universidad representa un objeto
						//guardamos las universidades existentes en la base de datos

					}


				} catch (JSONException e) {
					e.printStackTrace();

				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("REQUEST", error.getMessage());
			}
		});

		requestQueue.add(req);

	}

	
	public void requestBanner(User current_user,final Context context, final Callable<Void> callback) {


		String url = "http://especial1.ing.puc.cl/api/load_banner/"+current_user.getUniversity().getID()+"/"+current_user.getApiToken();

		
		final ProgressDialog progress = new ProgressDialog(context);
		progress.setTitle("Cargando tus datos");
		progress.setMessage("Pidiendo fotos para el banner...");
		progress.setCancelable(true);
		progress.setProgressStyle(progress.STYLE_HORIZONTAL);
		progress.setProgress(0);
		progress.setMax(100);
		progress.show();
		
		JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray> () {


		    @Override
		    public void onResponse(JSONArray response) {
		    	Log.i("BANNER",response.toString());
		        try {
		                JSONObject jsonObject = response.getJSONObject(0);		                
		                JSONArray surveys = jsonObject.getJSONArray("surveys");
		                JSONArray reports = jsonObject.getJSONArray("reports");
		                JSONArray events = jsonObject.getJSONArray("events");
		                JSONArray advertises = jsonObject.getJSONArray("advertises");
		                //ENCUESTAS
		                for (int i = 0; i < surveys.length(); i++) {		                	
			                JSONObject survey = surveys.getJSONObject(i);//pasamos las surveys a un objeto
			                BannerElement b = new BannerElement(survey.getInt("id"),survey.getString("name"),survey.getString("photo"),survey.getBoolean("active"),BannerElement.Type.survey);//int id,String name, String url, boolean active, Type type
			                b.saveElement(context);
		                }
		                //NOTICIAS
		                for (int i = 0; i < reports.length(); i++) {
			                JSONObject report = reports.getJSONObject(i);//pasamos las surveys a un objeto
			                BannerElement b = new BannerElement(report.getInt("id"),report.getString("name"),report.getString("image_report"),report.getBoolean("active"),BannerElement.Type.report);//int id,String name, String url, boolean active, Type type
			                Log.i("EVENT",report.toString());
			                b.saveElement(context);
		                }
//		                //EVENTOS
		                for (int i = 0; i < events.length(); i++) {
			                JSONObject event = events.getJSONObject(i);//pasamos las surveys a un objeto
			                BannerElement b = new BannerElement(event.getInt("id"),event.getString("name"),event.getString("image"),true,BannerElement.Type.event);//int id,String name, String url, boolean active, Type type
			                b.saveElement(context);
			                
		                }
		                //PUBLICIDAD
		                for (int i = 0; i < advertises.length(); i++) {
			                JSONObject advertise = advertises.getJSONObject(i);//pasamos las surveys a un objeto
			                BannerElement b = new BannerElement(advertise.getInt("id"),advertise.getString("name"),advertise.getString("image_advertise"),advertise.getBoolean("active"),BannerElement.Type.advertise);//int id,String name, String url, boolean active, Type type
			                b.saveElement(context);
			                Log.i("EVENT",advertise.toString());

			                
		                }
		                //llamamos al metodo que se encarga de inicializar el banner
		                if(callback != null)
			    			callback.call();
		                
		                
		        } catch (JSONException e) {
		            e.printStackTrace();
		          
		        } catch (Exception e) {
				
					e.printStackTrace();
				}
		        finally{
		        	progress.dismiss();
		        }
		    }
		}, new Response.ErrorListener() {
		    @Override
		    public void onErrorResponse(VolleyError error) {
		    	progress.setMessage("Hubo un error al pedir las imagenes del banner");
		    	progress.dismiss();
		        Log.i("REQUEST", "");
		    }
		});
		progress.incrementProgressBy(50);
		progress.setMessage("Esperando respuesta del servidor");
		requestQueue.add(req);

	}
	public void requestUserInfo(User current_user,final Context context, final Callable<Void> callback) {
		
		String url = "http://especial1.ing.puc.cl/api/my_recycle_info/"+current_user.getUniversity().getID()+"/"+current_user.getApiToken();
		Log.i("REQUEST",url);
		JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray> () {
		    @Override
		    public void onResponse(JSONArray response) {
		    	Log.i("USER",""+response.length());
		 		
		 		final ProgressDialog progress = ProgressDialog.show(context, "Cargando tu información",
		 				  null, true);

		        try {
		            
		                JSONObject jsonObject = response.getJSONObject(0);//cada universidad representa un objeto
		                //guardamos las universidades existentes en la base de datos
		                Log.i("USER",jsonObject.toString());  
		                JSONArray byTypesA = jsonObject.getJSONArray("info_by_types");
		                JSONObject byTypes = byTypesA.getJSONObject(0);
		                int vidrios = byTypes.getInt("Vidrios");
		                int papel = byTypes.getInt("Papel");
		                int plastico = byTypes.getInt("Plastico");
		                int latas = byTypes.getInt("Latas");
		                int pilas = byTypes.getInt("Pilas");
		                int lastWeek = jsonObject.getInt("last_week");
		                int lastMonth = jsonObject.getInt("last_month");
		                int total = jsonObject.getInt("total");
		                
		                SharedPreferences settings = context.getSharedPreferences("user_info", 0);
		        		SharedPreferences.Editor editor = settings.edit();


		                //ponemos las variables para ser leidas en la actividad del usuario

		        		editor.putInt("Vidrios",vidrios ); //pasamos parametros para la nueva actividad
		        		editor.putInt("Papel",papel );
		        		editor.putInt("Plastico",plastico );
		        		editor.putInt("Latas",latas );
		        		editor.putInt("Pilas",pilas );
		        		editor.putInt("LastWeek",lastWeek );
		        		editor.putInt("LastMonth",lastMonth );
		        		editor.putInt("Total",total );
			    		
			    		editor.commit();
		   
		    	
		    		if(callback != null)
		    			callback.call();
		            
		        } catch (JSONException e) {
		            e.printStackTrace();
		            Log.i("REQUEST", "estoy aqui");
		        } catch (Exception e) {
					e.printStackTrace();
				}
		        finally{
		            progress.dismiss();
		        }
		    }

		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("REQUEST", error.getMessage());
			}
		});

		requestQueue.add(req);

	}
	
public void requestGameStatus(final User current_user,final Context context, final Callable<Void> callback) {
		
		String url = "http://especial1.ing.puc.cl/api/load_game/"+current_user.getApiToken()+"/"+current_user.getAccesToken();
		Log.i("GAME",url);
		JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray> () {
		    @Override
		    public void onResponse(JSONArray response) {
		    	Log.i("GAME",""+response.length());
		 		
		 		final ProgressDialog progress = ProgressDialog.show(context, "Cargando los datos del juego",
		 				  null, true);
		 		Log.i("GAME",response.toString());  
		        try {
		            
		                JSONObject jsonObject = response.getJSONObject(0);//cada universidad representa un objeto
		                //guardamos las universidades existentes en la base de datos
		                Log.i("GAME",jsonObject.toString());  
		                //jsonObject.getInt("score")
		                JSONArray ranking = jsonObject.getJSONArray("ranking");
		                current_user.setGameInfo(0, jsonObject.getInt("my_total_position"), ranking);
		                current_user.saveUser(context);
		                
		                SharedPreferences settings = context.getSharedPreferences("user_info", 0);
		        		SharedPreferences.Editor editor = settings.edit();
		        		if(ranking.length()>0){
		        			editor.putInt("friends_count", ranking.length());
			                for(int j = 0;j<ranking.length();j++){
			                	JSONObject friend = ranking.getJSONObject(j);
			                	editor.putString("name_"+j, friend.getString("name")); //pasamos parametros para la nueva actividad
				        		editor.putInt("ranking_"+j,friend.getInt("position") );
				        		editor.putInt("points_"+j,friend.getInt("score"));
				        		editor.putString("url_"+j, friend.getString("picture"));
			                }
		        		}
//		                
			    		editor.commit();
//		   
		    	
		    		if(callback != null)
		    			callback.call();
		            
		        } catch (JSONException e) {
		            e.printStackTrace();
		            Log.i("GAME",e.toString());  
		        } catch (Exception e) {
					e.printStackTrace();
					Log.i("GAME",e.toString());  
				}
		        finally{
		            progress.dismiss();
		        }
		    }

		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("GAME", error.getMessage());
			}
		});

		requestQueue.add(req);

	}

	public void requestCampus(User current_user,final Context context, final Callable<Void> callback) {

		String url = "http://especial1.ing.puc.cl/api/get_campuses/"+current_user.getApiToken();
		Log.i("REQUEST",url);
		JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray> () {
			@Override
			public void onResponse(JSONArray response) {
				Log.i("REQUEST",""+response.length());

				final ProgressDialog progress = ProgressDialog.show(context, "Cargando campuses",
						null, true);

				try {
					for (int i = 0; i < response.length(); i++) {
						JSONObject jsonObject = response.getJSONObject(i);//cada universidad representa un objeto
						//guardamos las universidades existentes en la base de datos
						Log.i("REQUEST",jsonObject.toString());
						Campus c = new Campus();
						c.setID(jsonObject.getInt("id"));
						c.setUniversityID(jsonObject.getInt("university_id"));
						c.setName(jsonObject.getString("name"));
						c.setPolygon(jsonObject.getString("encoded_polygon"));
						c.saveCampus(context);              
					}

					UniversitiesSQLiteHelper sesdbh = new UniversitiesSQLiteHelper(context, "universities", null,1);

					//Abre la base de datos
					SQLiteDatabase db = sesdbh.getWritableDatabase();

					Cursor c = db.rawQuery("SELECT * FROM universities", null);
					int cursorcount= c.getCount();
					Log.i("DB",""+cursorcount);

					if(callback != null)
						callback.call();

				} catch (JSONException e) {
					e.printStackTrace();
					Log.i("REQUEST", "estoy aqui");
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally{
					progress.dismiss();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("REQUEST", error.getMessage());
			}
		});

		requestQueue.add(req);

	}




	public void requestClaimsCategories(User current_user,final Context context) {

		String url = "http://especial1.ing.puc.cl/api/get_complaint_types/1/"+current_user.getApiToken();
		Log.i("REQUEST",url);

		JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray> () {
			@Override
			public void onResponse(JSONArray response) {
				Log.e("QWERTYUI", String.valueOf(response));
				PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context, "DBPois", null,1);
				SQLiteDatabase db = sesdbh.getWritableDatabase();

				try {
					JSONObject jsonObject1 = response.getJSONObject(0);
					JSONArray jsonarray = new JSONArray(jsonObject1.getString("complaint_types"));
					for (int i = 0; i < jsonarray.length(); i++) {
						JSONObject jsonObject = jsonarray.getJSONObject(i);

						int idaux = Integer.parseInt(jsonObject.getString("id"));
						//						int univeraux = Integer.parseInt(jsonObject.getString("id_university"));
						String nameaux = jsonObject.getString("name");
						

						try
						{
							db.execSQL("INSERT INTO Claim_types (id, name_cat, id_university) " +
									"VALUES ("+idaux+", '"+nameaux+"', 1)");
						}
						catch(Exception e)
						{
							Log.e("PORSSDB","ID ya existe");
						}

					}
					db.close();


				} catch (JSONException e) {
					e.printStackTrace();
					
				} catch (Exception e) {
					Log.e("QWERTYUIO","ID existe");
				}

			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("REQUEST", "");
			}
		});

		requestQueue.add(req);

	}





	

	public void requestMapInfo(User current_user,final Context context) {


		String url = "http://especial1.ing.puc.cl/api/load_campus_points/1/"+current_user.getApiToken();
		Log.i("REQUEST",url);
		JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray> () {
			@Override
			public void onResponse(JSONArray response) {

				PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context, "DBPois", null,1);;
				SQLiteDatabase db = sesdbh.getWritableDatabase();;

				Log.i("REQUEST","LARGO "+response.length());
				try {

					for (int i = 0; i < response.length(); i++) {
						JSONObject jsonObject = response.getJSONObject(i);

						JSONArray poisarray = new JSONArray(jsonObject.getString("pois"));
						for (int j = 0; j < poisarray.length(); j++)
						{
							JSONObject jsonObjectpois = poisarray.getJSONObject(j);
							Log.i("POI REQUEST",jsonObjectpois.getString("name"));
							int idaux = Integer.parseInt(jsonObjectpois.getString("id"));
							String nameaux = jsonObjectpois.getString("name").toUpperCase();
							Double lataux = jsonObjectpois.getDouble("latitude");
							Double lngaux = jsonObjectpois.getDouble("longitude");	
							String descaux = jsonObjectpois.getString("description");
//							String type = jsonObjectpois.getString("campus_id");

							try
							{
								db.execSQL("INSERT INTO Pois (id, name, description, type, campus, lat, lng) " +
										"VALUES ("+idaux+", '"+nameaux+"', '"+descaux+"', 'bla', 'campus', "+lataux+", "+lngaux+")");

								JSONArray categories = new JSONArray(jsonObjectpois.getString("categories_s"));

								for (int k = 0; k < categories.length(); k++)
								{

									JSONObject jsonObjectcat = categories.getJSONObject(k);
									
									String cataux=jsonObjectcat.getString("name");
									

									db.execSQL("INSERT INTO Categories (id_poi, name_cat) " +
											"VALUES ("+idaux+", '"+cataux+"')");
									

								}
							}
							catch(Exception e)
							{
								Log.e("POISDB","ID ya existe");
							}


						}
						JSONArray porsarray = new JSONArray(jsonObject.getString("pors"));
						for (int j = 0; j < porsarray.length(); j++)
						{
							JSONObject jsonObjectpors = porsarray.getJSONObject(j);

							int idaux = Integer.parseInt(jsonObjectpors.getString("id"));
							Double lataux = Double.parseDouble(jsonObjectpors.getString("latitude"));
							Double lngaux = Double.parseDouble(jsonObjectpors.getString("longitude"));
							String descaux = "Punto de reciclaje";

							try
							{
								db.execSQL("INSERT INTO Pors (id, description, campus, lat, lng) " +
										"VALUES ("+idaux+", '"+descaux+"', 'campus', "+lataux+", "+lngaux+")");
								
								JSONArray categories = new JSONArray(jsonObjectpors.getString("types_of_dumps"));

								for (int k = 0; k < categories.length(); k++)
								{

									JSONObject jsonObjectcat = categories.getJSONObject(k);
									
									String typeaux=jsonObjectcat.getString("type_s");
									

									db.execSQL("INSERT INTO Dump_types (id_por, name_type) " +
											"VALUES ("+idaux+", '"+typeaux+"')");
									

								}
							}
							catch(Exception e)
							{
								Log.e("PORSSDB","ID ya existe");
							}

						}
						JSONArray buildingsarray = new JSONArray(jsonObject.getString("buildings"));
						for (int j = 0; j < buildingsarray.length(); j++)
						{
							JSONObject jsonObjectbuildings = buildingsarray.getJSONObject(j);
							Log.i("Building REQUEST",jsonObjectbuildings.getString("name"));
							int idaux = Integer.parseInt(jsonObjectbuildings.getString("id"));
							String auxname =jsonObjectbuildings.getString("name");
							String auxpoly = jsonObjectbuildings.getString("encoded_polygon");

							new Building(context, idaux,auxname,"","", auxpoly).saveBuilding(context); 
						}
						JSONArray eventsarray = new JSONArray(jsonObject.getString("events"));
						for (int j = 0; j < eventsarray.length(); j++)
						{
							JSONObject jsonObjectevents = eventsarray.getJSONObject(j);

							int idaux = Integer.parseInt(jsonObjectevents.getString("id"));
							String nameaux = jsonObjectevents.getString("name").toUpperCase();
							Double lataux = Double.parseDouble(jsonObjectevents.getString("latitude"));
							Double lngaux = Double.parseDouble(jsonObjectevents.getString("longitude"));
							//			String type = var1.getString("campus_id");

							try
							{
								db.execSQL("INSERT INTO Events (id, name, description, type, campus, lat, lng) " +
										"VALUES ("+idaux+", '"+nameaux+"', 'descripcion', 'event', 'campus', "+lataux+", "+lngaux+")");

							}
							catch(Exception e)
							{
								Log.e("EVENTSDB","ID ya existe");
							}
						}

					}


				} catch (JSONException e) {
					e.printStackTrace();
					Log.i("REQUEST", "estoy aqui");
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("REQUEST", "");
			}
		});

		requestQueue.add(req);
	}
	
	
		private boolean checkConnectivity(Context context){
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		  	NetworkInfo ni = cm.getActiveNetworkInfo();
		  	if (ni == null) {
		  		
		  		String waitMessage = "Tu conexión a internet está lenta, revisa que estés conectado a una red Wifi o 3G";
		          Toast.makeText(context, 
		               waitMessage,
		               Toast.LENGTH_LONG).show();	 
		          return false;
		  	}
		  	return true;
		}
	
}

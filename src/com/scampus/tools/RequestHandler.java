package com.scampus.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.*;
import com.scampus.uc.R;
import com.scampus.uc.connectionChecker;
import com.scampus.views.markerDetailsActivity;
import com.scampus.views.publicityActivity;

public class RequestHandler {
	// esta clase se encarga de manejar los post y get entre la aplicacion
	// android y el backend.
	
	// Ver la posibilidad de cambiar esto de Volley a http://loopj.com/android-async-http/
	private RequestQueue requestQueue;
	public GoogleMap map;
	private AsyncHttpClient asyncClient;
	private static final long INITIAL_ALARM_DELAY = 30 * 1000L;
	private boolean success;

	public RequestHandler(RequestQueue requestQueue) {
		this.requestQueue = requestQueue;
		this.asyncClient = new AsyncHttpClient();
	}

	public void requestUniversities(User current_user, final Context context,
			final Callable<Void> callback) {

		String url = context.getString(R.string.web_server_url)
				+ "/api/get_universities/" + current_user.getApiToken();

		final ProgressDialog progress = ProgressDialog.show(context,
				"Cargando universidades", null, true);

		JsonArrayRequest req = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						try {
							for (int i = 0; i < response.length(); i++) {
								JSONObject jsonObject = response
										.getJSONObject(i);// cada universidad
															// representa un
															// objeto
								// guardamos las universidades existentes en la
								// base de datos
								University u = new University();
								u.setAcronym(jsonObject.getString("acronym"));
								u.setID(jsonObject.getInt("id"));
								u.setName(jsonObject.getString("name"));
								u.saveUniversity(context);
							}

							if (callback != null)
								callback.call();

						} catch (JSONException e) {
							e.printStackTrace();

						} catch (Exception e) {

							e.printStackTrace();
						} finally {
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

	public void requestUniversitiesAndCampus(User current_user,
			final Context context) {
		// /api/get_universitites_and_campuses/:api_token
		String url = context.getString(R.string.web_server_url)
				+ "/api/get_universities_and_campuses/"
				+ current_user.getApiToken();

		JsonArrayRequest req = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {

						try {
							for (int i = 0; i < response.length(); i++) {
								JSONObject jsonObject = response
										.getJSONObject(i);// cada universidad
															// representa un
															// objeto
								// guardamos las universidades existentes en la
								// base de datos

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

	public void requestBanner(User current_user, final Context context,
			final Callable<Void> callback) {

		String url = context.getString(R.string.web_server_url)
				+ "/api/load_banner/" + current_user.getUniversity().getID()
				+ "/" + current_user.getApiToken();
		
		Log.i("Resquet banner url",url);

		final ProgressDialog progress = new ProgressDialog(context);
		progress.setTitle("Cargando tus datos");
		progress.setMessage("Pidiendo fotos para el banner...");
		progress.setCancelable(true);
		progress.setProgressStyle(progress.STYLE_HORIZONTAL);
		progress.setProgress(0);
		progress.setMax(100);
		progress.show();

		JsonArrayRequest req = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {

					@Override
					public void onResponse(JSONArray response) {
						Log.i("BANNER", response.toString());
						try {
							JSONObject jsonObject = response.getJSONObject(0);
							JSONArray surveys = jsonObject
									.getJSONArray("surveys");
							JSONArray reports = jsonObject
									.getJSONArray("reports");
							JSONArray events = jsonObject
									.getJSONArray("events");
							JSONArray advertises = jsonObject
									.getJSONArray("advertises");
							
							//Borramos para actualizar
							//Debemos checkear si se puede conectar primero.
							DBHelper dbh = new DBHelper(context);
							dbh.dropBannerTable(context);
							
							// ENCUESTAS
							for (int i = 0; i < surveys.length(); i++) {
								JSONObject survey = surveys.getJSONObject(i);// pasamos
																				// las
																				// surveys
																				// a
																				// un
																				// objeto
								BannerElement b = new BannerElement(survey
										.getInt("id"),
										survey.getString("name"),
										survey.getString("cloud_image_url"), 
										survey.getInt("status"),
										BannerElement.Type.survey);// int
																	// id,String
																	// name,
																	// String
																	// url,
																	// boolean
																	// active,
																	// Type type
								b.link = "";
								b.saveElement(context);
							}
							// NOTICIAS
							for (int i = 0; i < reports.length(); i++) {
								JSONObject report = reports.getJSONObject(i);// pasamos
																				// las
																				// surveys
																				// a
																				// un
																				// objeto
								BannerElement b = new BannerElement(report
										.getInt("id"),
										report.getString("name"), 
										report.getString("cloud_image_url"),
										report.getBoolean("active"),
										BannerElement.Type.report);// int
																	// id,String
																	// name,
																	// String
																	// url,
																	// boolean
																	// active,
																	// Type type
								b.link = report.getString("link");
								b.saveElement(context);
							}
							// //EVENTOS
							for (int i = 0; i < events.length(); i++) {
								JSONObject event = events.getJSONObject(i);// pasamos
																			// las
																			// surveys
																			// a
																			// un
																			// objeto
								BannerElement b = new BannerElement(event.getInt("id"), 
										event.getString("name"),
										event.getString("cloud_image_url"), 
										true,//event.getBoolean("active"),
										BannerElement.Type.event);// int
																	// id,String
																	// name,
																	// String
																	// url,
																	// boolean
																	// active,
																	// Type type
								b.link = "";
								b.saveElement(context);

							}
							// PUBLICIDAD
							for (int i = 0; i < advertises.length(); i++) {
								JSONObject advertise = advertises
										.getJSONObject(i);// pasamos las surveys
															// a un objeto
								BannerElement b = new BannerElement(advertise.getInt("id"), 
										advertise.getString("name"), 
										advertise.getString("cloud_image_url"),
										advertise.getBoolean("active"),
										BannerElement.Type.advertise);// int
																		// id,String
																		// name,
																		// String
																		// url,
																		// boolean
																		// active,
																		// Type
																		// type
								b.link = advertise.getString("link");
								b.saveElement(context);
								Log.i("ADVERTISE", advertise.toString());
							}
							// llamamos al metodo que se encarga de inicializar
							// el banner
							if (callback != null)
								callback.call();

						} catch (JSONException e) {
							e.printStackTrace();

						} catch (Exception e) {

							e.printStackTrace();
						} finally {
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

	public void requestUserRecycleInfo(User current_user, final Context context,
			final Callable<Void> callback) {

		String url = context.getString(R.string.web_server_url)
				+ "/api/my_recycle_info/"
				+ current_user.getUniversity().getID() + "/"
				+ current_user.getApiToken();
		Log.i("REQUEST", url);
		JsonArrayRequest req = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						Log.i("USER", "" + response.length());

						final ProgressDialog progress = ProgressDialog.show(
								context, "Cargando tu información", null, true);

						try {

							JSONObject jsonObject = response.getJSONObject(0);
							
							Log.i("USER", jsonObject.toString());
							JSONArray byTypesA = jsonObject
									.getJSONArray("info_by_types");
							JSONObject byTypes = byTypesA.getJSONObject(0);
							
							//Abrimos sharedpreferences
							SharedPreferences settings = context
									.getSharedPreferences("user_info", 0);
							SharedPreferences.Editor editor = settings.edit();
							
							//Creamos el arreglo para guardar los tipos de reciclaje
							Set<String> set = new HashSet<String>();
							
							Iterator<String> iter = byTypes.keys();
						    while (iter.hasNext()) {
						        String key = iter.next();
						        try {
						            Object value = byTypes.get(key);
						            
						            //Agregamos la key y valor: Ej: Vidrio, 2
						            editor.putString(key, value.toString()); 
						            
						            //Agregamos solo el key al arreglo set
						            set.add(key);
						            
						        } catch (JSONException e) {
						            // Something went wrong!
						        }
						    }
						    
						    editor.putStringSet("Reciclajes", set);
							
							int lastWeek = jsonObject.getInt("last_week");
							int lastMonth = jsonObject.getInt("last_month");
							int total = jsonObject.getInt("total");

							// ponemos las variables para ser leidas en la
							// actividad del usuario
							
							editor.putInt("LastWeek", lastWeek);
							editor.putInt("LastMonth", lastMonth);
							editor.putInt("Total", total);

							editor.commit();

							if (callback != null)
								callback.call();

						} catch (JSONException e) {
							e.printStackTrace();
							Log.i("REQUEST", "estoy aqui");
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
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
	
	public void requestGameStatus(final User current_user,
			final Context context, final Callable<Void> callback) {

		String url = context.getString(R.string.web_server_url)
				+ "/api/load_game/" + current_user.getApiToken() + "/"
				+ current_user.getAccesToken();
		Log.i("GAME", url);
		JsonArrayRequest req = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						Log.i("GAME", "" + response.length());

						final ProgressDialog progress = ProgressDialog.show(
								context, "Cargando los datos del juego", null,
								true);
						Log.i("GAME", response.toString());
						try {

							JSONObject jsonObject = response.getJSONObject(0);
							
							Log.i("GAME", jsonObject.toString());
							// jsonObject.getInt("score")
							JSONArray ranking = jsonObject
									.getJSONArray("ranking");
							current_user.setGameInfo(0,
									jsonObject.getInt("my_total_position"),
									ranking);
							current_user.saveUser(context);

							SharedPreferences settings = context
									.getSharedPreferences("user_info", 0);
							SharedPreferences.Editor editor = settings.edit();
							if (ranking.length() > 0) {
								editor.putInt("friends_count", ranking.length());
								for (int j = 0; j < ranking.length(); j++) {
									JSONObject friend = ranking
											.getJSONObject(j);
									editor.putString("name_" + j,
											friend.getString("name")); // pasamos
																		// parametros
																		// para
																		// la
																		// nueva
																		// actividad
									editor.putInt("ranking_" + j,
											friend.getInt("position"));
									editor.putInt("points_" + j,
											friend.getInt("score"));
									editor.putString("url_" + j,
											friend.getString("picture"));
								}
							}
							//
							editor.commit();
							//

							if (callback != null)
								callback.call();

						} catch (JSONException e) {
							e.printStackTrace();
							Log.i("GAME", e.toString());
						} catch (Exception e) {
							e.printStackTrace();
							Log.i("GAME", e.toString());
						} finally {
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

	public void requestCampus(User current_user, final Context context,
			final Callable<Void> callback) {

		String url = context.getString(R.string.web_server_url)
				+ "/api/get_campuses/" + current_user.getApiToken();
		Log.i("REQUEST", url);
		JsonArrayRequest req = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						Log.i("REQUEST", "" + response.length());

						final ProgressDialog progress = ProgressDialog.show(
								context, "Cargando campuses", null, true);

						try {
							for (int i = 0; i < response.length(); i++) {
								JSONObject jsonObject = response
										.getJSONObject(i);// cada universidad
															// representa un
															// objeto
								// guardamos las universidades existentes en la
								// base de datos
								Log.i("REQUEST", jsonObject.toString());
								Campus c = new Campus();
								c.setID(jsonObject.getInt("id"));
								c.setUniversityID(jsonObject
										.getInt("university_id"));
								c.setName(jsonObject.getString("name"));
								c.setPolygon(jsonObject
										.getString("encoded_polygon"));
								c.saveCampus(context);
							}

							UniversitiesSQLiteHelper sesdbh = new UniversitiesSQLiteHelper(
									context, "universities", null, 1);

							// Abre la base de datos
							SQLiteDatabase db = sesdbh.getWritableDatabase();

							Cursor c = db.rawQuery(
									"SELECT * FROM universities", null);
							int cursorcount = c.getCount();
							Log.i("DB", "" + cursorcount);

							if (callback != null)
								callback.call();

						} catch (JSONException e) {
							e.printStackTrace();
							Log.i("REQUEST", "estoy aqui");
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
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

	public void requestClaimsCategories(User current_user, final Context context) {

		String url = context.getString(R.string.web_server_url)
				+ "/api/get_complaint_types/1/" + current_user.getApiToken();
		Log.i("REQUEST", url);

		JsonArrayRequest req = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						Log.e("QWERTYUI", String.valueOf(response));
						PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context,
								"DBPois", null, 1);
						SQLiteDatabase db = sesdbh.getWritableDatabase();

						try {
							JSONObject jsonObject1 = response.getJSONObject(0);
							JSONArray jsonarray = new JSONArray(jsonObject1
									.getString("complaint_types"));
							for (int i = 0; i < jsonarray.length(); i++) {
								JSONObject jsonObject = jsonarray
										.getJSONObject(i);

								int idaux = Integer.parseInt(jsonObject
										.getString("id"));
								// int univeraux =
								// Integer.parseInt(jsonObject.getString("id_university"));
								String nameaux = jsonObject.getString("name");

								try {
									db.execSQL("INSERT INTO Claim_types (id, name_cat, id_university) "
											+ "VALUES ("
											+ idaux
											+ ", '"
											+ nameaux + "', 1)");
								} catch (Exception e) {
									Log.e("PORSSDB", "ID ya existe");
								}

							}
							db.close();

						} catch (JSONException e) {
							e.printStackTrace();

						} catch (Exception e) {
							Log.e("QWERTYUIO", "ID existe");
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

	public void requestMapInfo(User current_user, final Context context) {

		if (current_user.getCampus() == null)
			return;
		
		String url = context.getString(R.string.web_server_url)
				+ "/api/load_campus_points/" + current_user.getCampus().getID()
				+ "/" + current_user.getApiToken();
		Log.i("REQUEST", url);
		JsonArrayRequest req = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {

						PoisSQLiteHelper sesdbh = new PoisSQLiteHelper(context,
								"DBPois", null, 1);
						SQLiteDatabase db = sesdbh.getWritableDatabase();

						Log.i("REQUEST", "LARGO " + response.length());
						try {

							for (int i = 0; i < response.length(); i++) {
								JSONObject jsonObject = response
										.getJSONObject(i);

								JSONArray poisarray = new JSONArray(jsonObject
										.getString("pois"));
								for (int j = 0; j < poisarray.length(); j++) {
									JSONObject jsonObjectpois = poisarray
											.getJSONObject(j);
									Log.i("POI REQUEST",
											jsonObjectpois.getString("name"));
									int idaux = Integer.parseInt(jsonObjectpois
											.getString("id"));
									String nameaux = jsonObjectpois.getString(
											"name").toUpperCase();
									Double lataux = jsonObjectpois
											.getDouble("latitude");
									Double lngaux = jsonObjectpois
											.getDouble("longitude");
									String descaux = jsonObjectpois
											.getString("description");
									JSONArray resources = jsonObjectpois
											.getJSONArray("resources");
									setPlaceResources(resources, idaux, "pois",
											db);
									// String type =
									// jsonObjectpois.getString("campus_id");

									try {
										db.execSQL("INSERT INTO Pois (id, name, description, type, campus, lat, lng) "
												+ "VALUES ("
												+ idaux
												+ ", '"
												+ nameaux
												+ "', '"
												+ descaux
												+ "', 'bla', 'campus', "
												+ lataux + ", " + lngaux + ")");

										JSONArray categories = new JSONArray(
												jsonObjectpois
														.getString("categories_s"));

										for (int k = 0; k < categories.length(); k++) {

											JSONObject jsonObjectcat = categories
													.getJSONObject(k);

											String cataux = jsonObjectcat
													.getString("name");

											db.execSQL("INSERT INTO Categories (id_poi, name_cat) "
													+ "VALUES ("
													+ idaux
													+ ", '" + cataux + "')");

										}
									} catch (Exception e) {
										Log.e("POISDB", "ID ya existe");
									}

								}
								JSONArray porsarray = new JSONArray(jsonObject
										.getString("pors"));
								for (int j = 0; j < porsarray.length(); j++) {
									JSONObject jsonObjectpors = porsarray
											.getJSONObject(j);

									int idaux = Integer.parseInt(jsonObjectpors
											.getString("id"));
									Double lataux = Double
											.parseDouble(jsonObjectpors
													.getString("latitude"));
									Double lngaux = Double
											.parseDouble(jsonObjectpors
													.getString("longitude"));
									String descaux = "Punto de reciclaje";
									JSONArray resources = jsonObjectpors
											.getJSONArray("resources");
									setPlaceResources(resources, idaux, "pors",
											db);

									try {
										db.execSQL("INSERT INTO Pors (id, description, campus, lat, lng) "
												+ "VALUES ("
												+ idaux
												+ ", '"
												+ descaux
												+ "', 'campus', "
												+ lataux + ", " + lngaux + ")");

										JSONArray categories = new JSONArray(
												jsonObjectpors
														.getString("types_of_dumps"));

										for (int k = 0; k < categories.length(); k++) {

											JSONObject jsonObjectcat = categories
													.getJSONObject(k);

											String typeaux = jsonObjectcat
													.getString("type_s");

											db.execSQL("INSERT INTO Dump_types (id_por, name_type) "
													+ "VALUES ("
													+ idaux
													+ ", '" + typeaux + "')");

										}
									} catch (Exception e) {
										Log.e("PORSSDB", "ID ya existe");
									}

								}
								JSONArray buildingsarray = new JSONArray(
										jsonObject.getString("buildings"));
								for (int j = 0; j < buildingsarray.length(); j++) {
									JSONObject jsonObjectbuildings = buildingsarray
											.getJSONObject(j);
									Log.i("Building REQUEST",
											jsonObjectbuildings
													.getString("name"));
									int idaux = Integer
											.parseInt(jsonObjectbuildings
													.getString("id"));
									String auxname = jsonObjectbuildings
											.getString("name");
									String auxpoly = jsonObjectbuildings
											.getString("encoded_polygon");
									JSONArray resources = jsonObjectbuildings
											.getJSONArray("resources");
									setPlaceResources(resources, idaux,
											"building", db);
									
									// Agregado por HenrY 24/4/14
									double auxcenterx = jsonObjectbuildings.getDouble("center_longitude");
									double auxcentery = jsonObjectbuildings.getDouble("center_latitude");
									Point p = new Point(auxcenterx, auxcentery);

									new Building(context, idaux,auxname,"","", auxpoly, p).saveBuilding(context); 
								}
								JSONArray eventsarray = new JSONArray(
										jsonObject.getString("events"));
								for (int j = 0; j < eventsarray.length(); j++) {
									JSONObject jsonObjectevents = eventsarray
											.getJSONObject(j);

									int idaux = Integer
											.parseInt(jsonObjectevents
													.getString("id"));
									String nameaux = jsonObjectevents
											.getString("name");
									String descaux = jsonObjectevents
											.getString("description");
									Double lataux = Double
											.parseDouble(jsonObjectevents
													.getString("latitude"));
									Double lngaux = Double
											.parseDouble(jsonObjectevents
													.getString("longitude"));
									String startaux = jsonObjectevents
											.getString("start_date");
									String endaux = jsonObjectevents
											.getString("end_date");
									JSONArray resources = jsonObjectevents
											.getJSONArray("resources");
									
									// Generar objeto Evento y guardarlo en la DB
									LatLng posaux = new LatLng(lataux, lngaux);
									Event event = new Event(idaux, nameaux, descaux, posaux, startaux,
											endaux);
									event.save(db);
									setPlaceResources(resources, idaux, "event", db);
									//Por ahora solo el primer evento tiene alarma
									/*if (j==0)
										setEventAlarm(context, event);*/
								}

							}

						} catch (JSONException e) {
							e.printStackTrace();
							Log.i("REQUEST", "estoy aqui");
						}
						
						db.close();
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.i("REQUEST", "");
					}
				});

		requestQueue.add(req);
	}

	private void setEventAlarm(Context context, Event event) {
		// Seteo de alarma
		AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
		Intent NotificationReceiverIntent = new Intent(context, 
				markerDetailsActivity.class);
		DBHelper dbHelper = new DBHelper(context);
		PlaceDetails place = dbHelper.getEventByName(context,
				event.getName());
		NotificationReceiverIntent.putExtra("placeTag", place);
		PendingIntent NotificationReceiverPendingIntent = PendingIntent.getBroadcast(
				context, 0, NotificationReceiverIntent, 0);
		Log.i("ALARMA", "SETEADA!");
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+INITIAL_ALARM_DELAY, 
				NotificationReceiverPendingIntent);
	}
	
	protected void setPlaceResources(JSONArray resources, int place_id,
			String place_type, SQLiteDatabase db) {

		for (int i = 0; i < resources.length(); i++) {
			try {
				JSONObject jsonLink = resources.getJSONObject(i);
				int id = jsonLink.getInt("id");
				String name = jsonLink.getString("name");
				String source = jsonLink.getString("source");
				String url = jsonLink.getString("url");
				String type = jsonLink.getString("tipo");

				Link link = new Link(id, name, url, type, source);
				link.setPlace(place_id, place_type);
				link.saveLink(db);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private boolean checkConnectivity(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {

			String waitMessage = "Tu conexión a internet está lenta, revisa que estés conectado a una red Wifi o 3G";
			Toast.makeText(context, waitMessage, Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	public void editCampus(User current_user, final Context context,
			final Callable<Void> callback) {

		String url = context.getString(R.string.web_server_url)
				+ "/api/edit_campus/" + current_user.getApiToken() + "/"
				+ current_user.getCampus().getID();
		Log.i("REQUEST", url);

		final ProgressDialog progress = ProgressDialog.show(context,
				"Actualizando sus datos", null, true);

		JsonArrayRequest req = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						try {
							for (int i = 0; i < response.length(); i++) {
								JSONObject jsonObject = response
										.getJSONObject(i);// cada universidad
															// representa un
															// objeto
								// guardamos las universidades existentes en la
								// base de datos
								Log.i("REQUEST", jsonObject.toString());
								/*
								 * University u = new University();
								 * u.setAcronym(
								 * jsonObject.getString("acronym"));
								 * u.setID(jsonObject.getInt("id"));
								 * u.setName(jsonObject.getString("name"));
								 * u.saveUniversity(context);
								 */
							}

							if (callback != null)
								callback.call();

						} catch (JSONException e) {
							e.printStackTrace();

						} catch (Exception e) {

							e.printStackTrace();
						} finally {
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

	
	public String sendSuggestion(User current_user, final Context context, String suggestion){

		connectionChecker connectionChecker = new connectionChecker(context);
		if(connectionChecker.checkConnectivity()){
		
			if (android.os.Build.VERSION.SDK_INT > 9) {
			        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			        StrictMode.setThreadPolicy(policy);
			    }
			 
			final ProgressDialog progress = ProgressDialog.show(context,
						"Enviando su sugerencia", null, true);
			 
			String url = context.getString(R.string.web_server_url)+"/api/send_suggestion/"
					+ current_user.getApiToken();
			
			Log.i("REQUEST", url);
			
			String id = "";
			
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient();
		        HttpPost httpPost = new HttpPost(url);
		        
		        //Agregamos la data.
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("description", suggestion));
		        UrlEncodedFormEntity a = new UrlEncodedFormEntity(nameValuePairs,"utf-8");
		        httpPost.setEntity(a);
		        
		        //ejecutamos http post request.
		        @SuppressWarnings("rawtypes")
		        ResponseHandler responseHandler = new BasicResponseHandler();
		        @SuppressWarnings("unchecked")
		        String response = httpclient.execute(httpPost, responseHandler);
		        JSONArray jsonResponse = new JSONArray(response);
		        JSONObject body = jsonResponse.getJSONObject(0);
		        JSONArray sugg = body.getJSONArray("suggestion");
		        JSONObject sugg2 = sugg.getJSONObject(0);
		        id = sugg2.getString("id");
		        
		        Log.i("response_suggestion",id.toString());
	
		    } catch (UnsupportedEncodingException e) {
		        e.printStackTrace();
		    } catch (ClientProtocolException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				progress.dismiss();
			}
			
		    return id;
		}
		
		else return "-1";

		//final ProgressDialog progress = ProgressDialog.show(context,
				//"Enviando su sugerencia", null, true);
		
	}
	
	
	public void getSuggestions(User current_user, final Context context, DBHelper dbh, final Callable<Void> callback) {
		
		connectionChecker connectionChecker = new connectionChecker(context);
		if(connectionChecker.checkConnectivity()){
		
			String url = context.getString(R.string.web_server_url)
					+ "/api/get_suggestions/" + current_user.getApiToken();
			Log.i("REQUEST", url);
	
			final ProgressDialog progress = ProgressDialog.show(context,
					"Cargando sus respuestas.", null, true);
	
			JsonArrayRequest req = new JsonArrayRequest(url,
					new Response.Listener<JSONArray>() {
						@Override
						public void onResponse(JSONArray response) {
							try {
								for (int i = 0; i < response.length(); i++) {
									JSONObject jsonObject = response
											.getJSONObject(i);
									JSONArray array = jsonObject.getJSONArray("mu_suggs");
									Log.i("1",array.toString());
									//vaciamos la tabla sugerencias para luego volver a llenarla.
									
									DBHelper dbh = new DBHelper(context);
									dbh.deleteSuggestions();
									
									
										for (int j = 0; j < array.length(); j++) {
											
											JSONObject sugg = array.getJSONObject(j);
									        String status = sugg.getString("status");
									        
									        if(status.equals("answered")){
									        	Suggestion s = new Suggestion(sugg.getString("id"), sugg.getString("description"),1,sugg.getString("answer"));
									        	Log.i("answer",sugg.getString("answer"));
									        	s.upDate(context);
									        }
									        else{
									        	Suggestion s = new Suggestion(sugg.getString("id"), sugg.getString("description"),0,sugg.getString("answer"));
									        	s.upDate(context);
									        }
										}
									
									Log.i("REQUEST-getSuggestions", jsonObject.toString());
									
								}
								
								if (callback != null)
									callback.call();
	
							} catch (JSONException e) {
								e.printStackTrace();
	
							} catch (Exception e) {
	
								e.printStackTrace();
							} finally {
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
		
		//Si no hay conexión de todas formas llama al call back para usar las sugerencias de la base de datos.
		else if (callback != null){
			try {
				callback.call();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}
	
	public JSONArray requestSurvey(String url) {
		InputStream is = null;
		JSONArray jArray = null;
		String json = "";
		
		// Making HTTP request
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			//HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line +"\n" );
			}
			is.close();
			json = sb.toString();
			
			Log.d("JSON Answer", json);
			
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try {
			jArray = new JSONArray(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jArray;
	
	}
	
	public void sendSurvey(User current_user, JSONObject js, final Context context,
			final Callable<Void> callback) {

		String url = context.getString(R.string.web_server_url)+"/api/respond_survey/"
				+ current_user.getApiToken();
		
		Log.i("REQUEST", url);

		final ProgressDialog progress = ProgressDialog.show(context,
				"Enviando su respuesta", null, true);
		
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
	        HttpPost httpPost = new HttpPost(url);
	        httpPost.setEntity(new StringEntity(js.toString()));
	        httpPost.setHeader("Accept", "application/json");
	        httpPost.setHeader("Content-type", "application/json");

	        @SuppressWarnings("rawtypes")
	        ResponseHandler responseHandler = new BasicResponseHandler();
	        @SuppressWarnings("unchecked")
	        String response = httpclient.execute(httpPost, responseHandler);
	        JSONObject jsonResponse = new JSONObject(response);
	        
	        //String serverResponse = jsonResponse.getString("success");
	        
	        Log.i("response_encuesta",jsonResponse.toString());
	        
	        if (callback != null)
				try {
					callback.call();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	    } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    } catch (ClientProtocolException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (JSONException e) {
			e.printStackTrace();
		}
		finally {
				progress.dismiss();
		}
	    return;
		
	}
	
	public void sendNativeLogin(final User current_user, final Context context,String password, final Callable<Void> callback) {
		String url = context.getString(R.string.web_server_url)+"/mobile_users/login";
		Log.i("REQUEST", url);
		
		JSONObject body = new JSONObject();
		try {
			body.put("access_token", current_user.getAccessToken());
			body.put("provider", "native");
		} catch (JSONException e){
			e.getStackTrace();
		}
		Log.i("Request Body", body.toString());
		JsonObjectRequest request = new JsonObjectRequest (Request.Method.POST,
				url, body, new Response.Listener<JSONObject>() {
					@Override
					public void onResponse (JSONObject response) {
						Log.d("Response", response.toString());
						try {
							if (response.getBoolean("authenticated")) {
								String api_token = response.getString("api_token");
								current_user.setApiToken(api_token);
								current_user.saveUser(context);
							}
						} catch (JSONException e) {
							Log.e("JSON ERROR", e.getMessage());
						}
						if (callback != null)
							try {
								callback.call();
							} catch (Exception e) {
								e.printStackTrace();
							}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						error.getStackTrace();				
					}
				});
		requestQueue.add(request);	
	}
	
	public void retrieveNativeUser(final User current_user, String password,
			final Context context, final Callable<Void> callable) {
		String url = context.getString(R.string.web_server_url)+"/api/login/v1/login";
		Log.i("REQUEST", url);

		JSONObject body = new JSONObject();
		try {
			body.put("email", current_user.getEmail());
			body.put("password", password);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.i("REQUEST Body", body.toString());
		JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,
				body, new Response.Listener<JSONObject>() {
					
					@Override
					public void onResponse(JSONObject response) {
						Log.d("Response", response.toString());
						JSONArray arr;
						JSONObject jsonUser;
						try {
							// API Manda un array con 1 user, esto no debería ser así
							if (response.getString("status").equalsIgnoreCase("Error")){
								if (callable != null) {
					            	try {
										callable.call();
									} catch (Exception e) {
										e.printStackTrace();
									}
					            }
							} else {
								arr = response.getJSONArray("user");
								jsonUser = arr.getJSONObject(0);
								Log.d("JSON User", jsonUser.toString());
								current_user.setByJson(jsonUser);
								current_user.setProvider("native");
					            current_user.saveUser(context);
					            if (callable != null) {
					            	try {
										callable.call();
									} catch (Exception e) {
										e.printStackTrace();
									}
					            }
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}	
					}
					
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d("Error.Response", error.getMessage());
					}
					
				});
		requestQueue.add(request);
	}
	
	public boolean sendRegisterAsync(final Context context, final User current_user, Uri mProfilePhotoUri) {
		success = false;
		String url = context.getString(R.string.web_server_url)+"/api/login/v1/signin";
		Log.i("REQUEST", url);
		// Manejo de respuesta de API en JSON
		
		ResponseHandlerInterface responseHandler = new JsonHttpResponseHandler() {
			@Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				try {
                	String status = response.getString("status");
                	if (status.equalsIgnoreCase("Error")) {
                		// ERROR al registar usuario
                		Log.i("Register", response.getString("error"));
                		current_user.cleanUser(context);
                		Toast.makeText(context, "Lo lamentamos, no se pudo crear el"+
                		"usuario, \n vuelve a intentarlo.", Toast.LENGTH_LONG).show();
                	} else {
                		// Usuario registrado con ÉXITO
                		Toast.makeText(context, "Felicidades, fuiste registrado!",
                				Toast.LENGTH_LONG).show();
                		Log.i("Register", response.toString());
                		JSONObject jsonUser = response.getJSONObject("user");
                		String photoUrl = jsonUser.getString("cloud_image_url");
                		String accessToken = jsonUser.getString("access_token");
                		Log.i("Register Photo", photoUrl);
                		current_user.setProfilePhotoUrl(photoUrl);
                		current_user.setAccessToken(accessToken);
                		current_user.saveUser(context);
                		success = true;
                	}
                } catch (JSONException e){
                	e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
            		JSONObject errorResponse) {
                if (errorResponse != null) {
                    Log.e("Register", errorResponse.toString());
                }
                current_user.cleanUser(context);
            }
		};
		
		// Parametros del request
		RequestParams params = new RequestParams();
		params.put("user[email]", current_user.getEmail());
		params.put("user[first_name]", current_user.getFirstName());
		params.put("user[last_name]", current_user.getLastName());
		params.put("user[password]", current_user.getPassword());
		params.put("user[password_confirmation]", current_user.getPassword());
		params.put("user[gender]", current_user.getSex());
		Calendar cal = current_user.getBirthday();
		String birthday = cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH)+1) + "/" +
				cal.get(Calendar.DAY_OF_MONTH);
		params.put("user[birthday]", birthday);
		// Obtener imagen
		String[] projection = { MediaStore.Images.Media.DATA}; 
		Cursor cursor = context.getContentResolver().query(
				mProfilePhotoUri,
                projection, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(projection[0]);
        String picturePath = cursor.getString(columnIndex);
		Log.i("Img Path", picturePath);
		try {
			params.put("user[avatar]", new File(picturePath), "image/jpg");
		} catch (FileNotFoundException e){
			//Log.e("Register", e.getMessage());
			e.getStackTrace();
		}
		// Se envia el POST!
		asyncClient.post(url, params, responseHandler);
		return success;
	}

	public void requestUserCampusInfo(final User current_user,
			final Context context) {
		String url = context.getString(R.string.web_server_url) + "/api/mobile_user/" +
				current_user.getApiToken();
		Log.i("REQUEST", url);
		JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, 
			new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
					try {
						if (!response.getBoolean("error")) {
							JSONObject user = response.getJSONObject("mobile_user");
							int campusId = user.getInt("campus_id");
							DBHelper dbHelper = new DBHelper(context);
							current_user.setCampus(dbHelper.getCampusById(campusId));
							current_user.saveUser(context);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					error.getStackTrace();		
				}
			}
		);
		requestQueue.add(request);
	}
}

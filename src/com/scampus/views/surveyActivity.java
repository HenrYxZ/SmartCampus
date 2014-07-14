package com.scampus.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.support.v4.util.LruCache;
import android.util.Log;
//import android.content.DialogInterface.OnClickListener;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.scampus.uc.R;
import com.scampus.tools.DBHelper;
import com.scampus.tools.Map;
import com.scampus.tools.RequestHandler;
import com.scampus.tools.User;
import com.scampus.uc.DiskBitmapCache;


public class surveyActivity extends Activity {	
	
	Button buttonEnviar;
	JSONObject s;
	HashMap<CheckBox,String> cbs; 
	HashMap<RadioGroup,String> rgs;
	HashMap<Spinner,String> spns;
	String surveyId;
	private User current_user;
	
	private RequestQueue requestQueue;
	
	String src;
	String name;
	String type;
	private ImageLoader imageLoader;
	int surveyID;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {	
		
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.survey_layout);
	    
	    requestQueue = Volley.newRequestQueue(this);
	    current_user = new User(this);
	    
	    cbs = new HashMap<CheckBox,String>();
		rgs = new HashMap<RadioGroup,String>();
		spns =  new HashMap<Spinner,String>();

	    TextView survey_name = (TextView)findViewById(R.id.survey_name);
	    
	    Intent intent = getIntent();
	    surveyID = intent.getIntExtra("survey_ID", 0);
	    String APIToken = intent.getStringExtra("API_token");
	    
	  //Cargamos todo para la imagen
	  	src = intent.getStringExtra("image_source");
	  	name = intent.getStringExtra("image_name");
	  	type = intent.getStringExtra("image_type");
	  	imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
		    private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
		    public void putBitmap(String url, Bitmap bitmap) {
		        mCache.put(url, bitmap);
		    }
		    public Bitmap getBitmap(String url) {
		        return mCache.get(url);
		    }
		});
	  	
	  	
	    String url = this.getString(R.string.web_server_url)
				+ "/api/load_survey/"+surveyID+"/"+APIToken;
	    Log.i("Request",url);
	    
	    if (android.os.Build.VERSION.SDK_INT > 9) {
	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy);
	    }
	    	    
	    RequestQueue rq = Volley.newRequestQueue(this);
	    RequestHandler rh = new RequestHandler(rq);
	    
	    //rh.saveSurvey(url, this);
	    JSONArray survey = rh.requestSurvey(url);
	    s = null;
	    try {
			s = survey.getJSONObject(0);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	    
	    try {
	    	JSONObject info = s.getJSONObject("survey");
	    	JSONArray questions = s.getJSONArray("questions");
	    	
			survey_name.setText(info.getString("name"));
			this.surveyId = info.getString("id");
			
			int nQuestions = questions.length();
			LinearLayout ll = (LinearLayout) this.findViewById(R.id.qs);
			
			//Le asiganmos la imagen.
			ImageView lv = (ImageView) this.findViewById(R.id.surveyImage);
			imageLoader.get(this.src, ImageLoader.getImageListener(lv, R.drawable.transparent, R.drawable.loadingerror));
			
			//questions layout
			LinearLayout.LayoutParams qslayoutParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			
			//density para margin entre checkbox, radiobutton y texto
			final float scale = this.getResources().getDisplayMetrics().density;
			
			qslayoutParams.setMargins(15, 0, 15, 15);
				
			for (int i = 0; i < nQuestions; i++) {
				JSONObject q = questions.getJSONObject(i);
				String type = q.getString("question_type");
				if (type.equals("Seleccion Multiple")) {
					TextView text = new TextView(this);
					String aux = q.getString("question_text");
					text.setText(aux);
					text.setTextAppearance(this, android.R.style.TextAppearance_Large);
					
					ll.addView(text, qslayoutParams);					
					JSONArray opt = q.getJSONArray("options");
					
					for (int j = 0; j < opt.length(); j++) {
						CheckBox cb = new CheckBox(getApplicationContext());
						cb.setTextColor(Color.BLACK);
		                cb.setText(opt.getString(j));
		                cb.setButtonDrawable(R.drawable.checkbox_survey);
		                
		                //margin entre checkbox y texto
		                cb.setPadding(cb.getPaddingLeft() + (int)(10.0f * scale + 0.5f),
		                		cb.getPaddingTop(),
		                		cb.getPaddingRight(),
		                		cb.getPaddingBottom());
		                
		                LinearLayout.LayoutParams layoutParamsCb = new LinearLayout.LayoutParams(
		    			LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		                layoutParamsCb.setMargins(15, 0, 15, 15);
		    					
		                String id1 = "1"+i+j;
						cb.setId(Integer.parseInt(id1));
						Log.i("check_box_id", String.valueOf(cb.getId()));
		                ll.addView(cb, layoutParamsCb);		
		                cbs.put(cb,q.getString("id"));
					}		
					
					//Separador
					TextView separador = new TextView(this);
					separador.setHeight(1);
					separador.setBackgroundColor(Color.DKGRAY);
					ll.addView(separador);
				}
				else if (type.equals("Seleccion Unica")) {
					TextView text = new TextView(this);
					String aux = q.getString("question_text");
					text.setText(aux);
					text.setTextAppearance(this, android.R.style.TextAppearance_Large);
					ll.addView(text,qslayoutParams);					
					
					JSONArray opt = q.getJSONArray("options");
					RadioGroup rg = new RadioGroup(this);
					rg.setOrientation(RadioGroup.VERTICAL);
					
					LinearLayout.LayoutParams layoutParamsRg = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
							layoutParamsRg.setMargins(15, 0, 15, 15);
					
					for (int j = 0; j < opt.length(); j++) {						
						RadioButton rb = new RadioButton(getApplicationContext());
						rb.setTextColor(Color.BLACK);
		                rb.setText(opt.getString(j));
		                rb.setButtonDrawable(R.drawable.radiobutton_survey);
		                
		                rb.setPadding(rb.getPaddingLeft() + (int)(10.0f * scale + 0.5f),
		                		rb.getPaddingTop(),
		                		rb.getPaddingRight(),
		                		rb.getPaddingBottom());
				                
		                String id2 = "2"+i+j;
						rb.setId(Integer.parseInt(id2));
						Log.i("radio id", String.valueOf(rb.getId()));
		                rg.addView(rb,layoutParamsRg);	
					}
					
					
					
					ll.addView(rg,layoutParamsRg);
					this.rgs.put(rg, q.getString("id"));
					
					//Separador
					TextView separador = new TextView(this);
					separador.setHeight(1);
					separador.setBackgroundColor(Color.DKGRAY);
					ll.addView(separador);
				}
				else {
					TextView text = new TextView(this);
					String aux = q.getString("question_text");
					text.setText(aux);
					text.setTextAppearance(this, android.R.style.TextAppearance_Large);
					ll.addView(text,qslayoutParams);					
					
					JSONArray opt = q.getJSONArray("options");
					
					ArrayList<String> arr = new ArrayList<String>(opt.length());
					
					for (int j = 0; j < opt.length(); j++) {						
						arr.add(opt.getString(j));					
					}
					
					Spinner spinner = new Spinner(this);					
					ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
					        android.R.layout.simple_spinner_item,
					            arr);
					spinner.setAdapter(spinnerArrayAdapter);
					
					LinearLayout.LayoutParams layoutParamsSpinner = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					layoutParamsSpinner.setMargins(15, 0, 15, 15);
						        
					String id3 = "3"+i;
					spinner.setId(Integer.parseInt(id3));
					Log.i("Spinner_id", String.valueOf(spinner.getId()));
					ll.addView(spinner,layoutParamsSpinner);
					this.spns.put(spinner, q.getString("id"));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    /*Button b = new Button(this);
	    b.setText("Enviar respuestas");
	    LayoutParams bParam = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    b.setLayoutParams(bParam);
	    addListenerOnButton(b);*/
	    addListenerOnButton();
	    
	}
	
	public void addListenerOnButton() {
		 
		buttonEnviar = (Button) findViewById(R.id.buttonEnviar);		
		buttonEnviar.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View v) {
				sendResponse();
			}
		});
	}
	
	private void sendResponse() {
		
		Log.i("clicl","click!");
		JSONObject response = new JSONObject();
		JSONObject responseFinal = new JSONObject();
		JSONArray questions = new JSONArray();
		
		
		JSONObject radioButtons = new JSONObject();
		
		if(!cbs.isEmpty()){
			
			String LastQuestionId = "";
			
			//Dejamos solo las ID una vez.
			Collection<String> questionsId = cbs.values();
			ArrayList<String> newQuestionsId=new ArrayList<String>();
			String lastId = "";
			for (int i = 0; i < questionsId.size(); i++){
					lastId=questionsId.toArray()[i].toString();
					if(!newQuestionsId.contains(lastId))
					newQuestionsId.add(lastId);
				
			}
			Log.i("questionsId",newQuestionsId.toString());

			JSONArray respuestas = new JSONArray();
				for (int i = 0; i < newQuestionsId.size(); i++){
					for(Entry<CheckBox, String> entry : cbs.entrySet()){
						
						CheckBox cb = (CheckBox)entry.getKey();
						if(newQuestionsId.toArray()[i].equals(entry.getValue()) && cb.isChecked()){
							respuestas.put(cb.getText());
						}
							
					}
					JSONObject checkboxes = new JSONObject();
					try {
						checkboxes.put("answers", respuestas);
						checkboxes.put("id", questionsId.toArray()[i]);
						questions.put(checkboxes);
						respuestas = new JSONArray();
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			 
		}
		
		//Agregamos radiobuttons al JSON questions
		if(!rgs.isEmpty()){
			for(Entry<RadioGroup, String> entry : this.rgs.entrySet()){
				
				
				RadioGroup radioGroup = (RadioGroup) entry.getKey();
				RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
				//String respuesta = radioButton.getText().toString();
				JSONArray respuestas = new JSONArray();
				
				//Checkeamos que haya respuesta
				if(radioButton !=null){
				respuestas.put(radioButton.getText().toString());
				}
						
				else{
					String succesMessage = "Debes responder todas las preguntas";
                    Toast.makeText(getApplicationContext(), 
                         succesMessage,
                         Toast.LENGTH_LONG).show();
					return;
				}
				String questionId = (String) entry.getValue();
				JSONObject radioButtonsAnswer = new JSONObject();
				
				try {
					radioButtonsAnswer.put("answers", respuestas);
					radioButtonsAnswer.put("id", questionId);
					questions.put(radioButtonsAnswer);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
		
		//Agregamos spinner a JSON questions
		if(!spns.isEmpty()){
			for(Entry<Spinner, String> entry : this.spns.entrySet()){
				
				
				Spinner spinner = (Spinner) entry.getKey();
				JSONArray respuestas = new JSONArray();
				respuestas.put(spinner.getSelectedItem().toString());
						
				String questionId = (String) entry.getValue();
				JSONObject spinnerAnswer = new JSONObject();
				
				try {
					spinnerAnswer.put("answers", respuestas);
					spinnerAnswer.put("id", questionId);
					questions.put(spinnerAnswer);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
		
		
		//Formamos el JSON FINAL.
		try {
			response.put("id", this.surveyId);
			response.put("questions", questions);
			responseFinal.put("response", response);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Log.i("JSONcheckboxes",checkboxes.toString());
		Log.i("JSONresponsefinal",responseFinal.toString());
		
		//Enviamos
		RequestHandler requestHandler = new RequestHandler(requestQueue);
		requestHandler.sendSurvey(current_user, responseFinal, this, new Callable<Void>(){
			public Void call(){
				deleteSurvey();
				return null;
			}
		});
		
		
		finish();
	}
	
	private void deleteSurvey(){
		
		//Damos el mensaje que la encuesta fue recibida.
		Toast.makeText(getApplicationContext(), 
                "Hemos recibido sus respuestas",
                Toast.LENGTH_LONG).show();
		
		//DBHelper dbh = new DBHelper(this);
		//dbh.deleteSurvey(surveyID);
		
	}
	
	
}

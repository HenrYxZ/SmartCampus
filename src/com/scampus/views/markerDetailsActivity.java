package com.scampus.views;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.util.LruCache;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.format.Time;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.RatingBar;
import android.util.*;
import android.widget.Toast;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.plus.Plus;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.scampus.uc.R;
import com.scampus.tools.DBHelper;
import com.scampus.tools.DateParser;
import com.scampus.tools.Link;
import com.scampus.tools.MenuHelper;
import com.scampus.tools.PlaceDetails;
import com.scampus.tools.RequestHandler;




//SMARTGRID
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class markerDetailsActivity extends Activity {

	TextView nameView;
	TextView descriptionView;
	LinearLayout links;
	RequestHandler requestHandler;
	RequestQueue requestQueue;
	ImageLoader imageLoader;
	LinearLayout imagesContainer;
	DBHelper dbHelper;
	LinearLayout smartgridDashboard;
	PlaceDetails place;

	LinearLayout markerdetails_mainLayout;

	static TextView startView;
	static TextView endView;

	
	LinkedList<Link> imageUrls;

	SlidingMenu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_marker_details);

		requestQueue = Volley.newRequestQueue(this);
		// Esto permite bajar imagenes mas facilmente para mas info
		// http://cypressnorth.com/mobile-application-development/
		// setting-android-google-volley-imageloader-networkimageview/
		imageLoader = new ImageLoader(requestQueue,
				new ImageLoader.ImageCache() {
					private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(
							10);

					public void putBitmap(String url, Bitmap bitmap) {
						mCache.put(url, bitmap);
					}

					public Bitmap getBitmap(String url) {
						return mCache.get(url);
					}
				});
		dbHelper = new DBHelper(this);
		menu = (SlidingMenu) new MenuHelper().create(this, 1, null);

		place = getIntent().getParcelableExtra("placeTag");
		
		markerdetails_mainLayout = (LinearLayout) findViewById(R.id.markerdetails_mainLayout);
		nameView = (TextView) findViewById(R.id.name);
		descriptionView = (TextView) findViewById(R.id.description);
		links = (LinearLayout) findViewById(R.id.links);

		//imagesContainer = (LinearLayout) findViewById(R.id.images);
		smartgridDashboard = (LinearLayout) findViewById(R.id.smartgrid_dashboard);

		startView = (TextView) findViewById(R.id.startDateView);
		endView = (TextView) findViewById(R.id.endDateView);

		// TODO Manejar mejor los eventos con rating y fecha
		Log.i("DETAILS", place.getType());
		LinearLayout datesContainer = (LinearLayout) findViewById(R.id.datesLayout);
		if (place.getType().equals("event")) {

			//setDates();

			String[] dates = setDates();
			startView.setText(dates[0]);
			endView.setText(dates[1]);
			

			//setRating();
		}
		else {
			datesContainer.removeAllViews();
			
			//markerdetails_mainLayout.removeView(datesContainer);
			//datesContainer.setVisibility(View.INVISIBLE);
			//datesContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		}
		
		if (place.getType().equals("building")) {
			descriptionView.setVisibility(View.INVISIBLE);
			smartgridDashboard.setVisibility(View.INVISIBLE);
		}
		
		/*
		 * RatingBar ratingBar; if(place.getType().equals("event")) { ratingBar
		 * = (RatingBar) findViewById(R.id.ratingBar);
		 * 
		 * // Cambio en el rating ratingBar.setOnRatingBarChangeListener(new
		 * RatingBar.OnRatingBarChangeListener() {
		 * 
		 * @Override public void onRatingChanged(RatingBar ratingBar, float
		 * rating, boolean fromUser) { sendRating();
		 * Toast.makeText(getApplicationContext
		 * (),getString(R.string.rating_changed) + String.valueOf(rating),
		 * Toast.LENGTH_LONG).show(); ratingBar.setIsIndicator(true); } }); //
		 * Se agrega el ratingBar en la segunda posicion
		 * mainLayout.addView(ratingBar); }
		 */

		// Set the Text
		nameView.setText(place.getName());
		descriptionView.setText(place.getDescription());

		// Set Links
		// String links = getLinks(place);
		Link[] links = dbHelper.getLinksFromPlace(this, place.getId(),
				place.getType());
		// Si no tiene links no muestra nada
		if (links.length == 0) {
			this.links.setVisibility(View.INVISIBLE);
			TextView labelLink = (TextView) findViewById(R.id.linksLabel);
			labelLink.setVisibility(View.INVISIBLE);
		} else
			setLinksIntoList(links);
		// Si no tiene descripción no muestra ese textview
		if(place.getDescription().equals(""))
			descriptionView.setVisibility(View.INVISIBLE);;

		// TODO Debería ponerse solo una como el frontis
		imageUrls = dbHelper.getImagesUrlsFromPlace(this,
				place.getId(), place.getType());
			
		//Obtenemos las imagenes.
		this.getImages(imageUrls);
		
		//Obtenemos la información de Smartgrid
		//this.getSmartgridInfo();
		
	}

	private String[] setDates() {
		// TODO obtener fechas de la base de datos dandoles el nombre del evento
		String[] dates = dbHelper.getEventDates(place.getId());
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
		
		DateParser dateParser = new DateParser();
		String[] answers = new String[2];
		answers[0] = sdf.format(dateParser.parse(dates[0]).getTime());
		answers[1] = sdf.format(dateParser.parse(dates[1]).getTime());
		return answers;
	}

	private void setLinksIntoList(Link[] links) {
		// Muestra la lista de links en un list view
		for (Link link : links) {
			// TextView linkView = (TextView)findViewById(R.id.linkView);
			TextView linkView = new TextView(this);
			LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutparams.setMargins(0, 5, 0, 0);
			linkView.setLayoutParams(layoutparams);
			
			linkView.setPadding(10, 10, 10, 10);
			 
			
			linkView.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.external_link_icon, 0, 0, 0);
			 
			linkView.setCompoundDrawablePadding(20);
			//linkView.setAutoLinkMask(Linkify.ALL);
			String text = "<a href=" + link.getUrl() + ">" + link.getName()
					+ "</a>";
			
			Spannable spannedText = Spannable.Factory.getInstance().newSpannable(
					Html.fromHtml(text));
			Spannable processedText = removeUnderlines(spannedText);
			
			linkView.setText(processedText);
			linkView.setMovementMethod(LinkMovementMethod.getInstance());
			linkView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
			linkView.setLinkTextColor(Color.parseColor("#6f6f6f"));
			
			/*if(link.getType().equalsIgnoreCase("youtube")){
				linkView.setBackgroundColor(Color.parseColor("#AA0b8db3"));
			}
			else if (link.getType().equalsIgnoreCase("word")){
				linkView.setBackgroundColor(Color.parseColor("#AA8650ac"));
			}*/
					
			Log.i("tipo link",link.getType());
			
			TextView separator = new TextView(this);
			separator.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, 1));
			separator.setBackgroundColor(Color.DKGRAY);
			separator.setPadding(10, 0, 10, 0);
			this.links.addView(linkView);
			//this.links.addView(separator);
		}
	}
	
	public static Spannable removeUnderlines(Spannable p_Text) {  
        URLSpan[] spans = p_Text.getSpans(0, p_Text.length(), URLSpan.class);  
        for (URLSpan span : spans) {  
             int start = p_Text.getSpanStart(span);  
             int end = p_Text.getSpanEnd(span);  
             p_Text.removeSpan(span);  
             span = new URLSpanNoUnderline(span.getURL());  
             p_Text.setSpan(span, start, end, 0);  
        }  
        return p_Text;  
   }  
	
	// Al apretar home(logo), abrir o cerrar el menu lateral
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			menu.toggle();
			break;
		}
		return true;

	}
	
	private void getImages(LinkedList<Link> imageUrls) {

		
		//Lo hacemos en async task para cargar las imagenes.
    	
    	//AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
    	class MyAsyncTask extends AsyncTask<Void, Void, String> {
    		
    		public ArrayList<Bitmap> images;
    		public LinkedList<Link> imageUrls;
    		
           
            protected String doInBackground(Void... params) {
            	
            	if (android.os.Build.VERSION.SDK_INT > 9) {
        	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	        StrictMode.setThreadPolicy(policy);
        	    }
            	
            	images = new ArrayList<Bitmap>();
            	
            	for (Link imgUrl : imageUrls) {
            		
        			Bitmap bitmap;
        			try {
        				bitmap = BitmapFactory.decodeStream((InputStream)new URL(imgUrl.getUrl()).getContent());
        				images.add(bitmap);
        			} catch (MalformedURLException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			} catch (IOException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
        			
        		}
                
                return null;
            }
            
            protected void onPreExecute() {
            	setProgressBarIndeterminateVisibility(true);
            }

            
            protected void onPostExecute(String result) {
            	setProgressBarIndeterminateVisibility(false);
                setImages(images);
            }

        };
        
        MyAsyncTask task = new MyAsyncTask();
        task.imageUrls = imageUrls;
        task.execute();

	}
	
	private void getSmartgridInfo() {

		
		//Lo hacemos en async task para cargar la info de smartgrid
    	class MyAsyncTask extends AsyncTask<Void, Void, String> {
           
    		String smartGridHtml;
    		
            protected String doInBackground(Void... params) {
            	
            	if (android.os.Build.VERSION.SDK_INT > 9) {
        	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	        StrictMode.setThreadPolicy(policy);
        	    }
            	
            	Document doc;
        		String url = "http://smartgrid.uc.cl/DATABASE/EK000001_MET001/index.php";
        		try {
        			doc = Jsoup.connect(url).get();
        		} catch (IOException e) {
        			doc = new Document(null);
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		
        		if(doc!=null){
        			Element semaforo = doc.select("svg").first();
        			smartGridHtml = semaforo.toString();
        		}
                return null;
            }
            
            protected void onPostExecute(String result) {
            	setSmartGridInfo(smartGridHtml);
            }

        };
        
        MyAsyncTask task = new MyAsyncTask();
        task.execute();

	}
	
	public void setImages(ArrayList<Bitmap> images){
		
		//-------------------FANCYCOVER----------------------------------------
				FancyCoverFlow fancyCoverFlow = (FancyCoverFlow) findViewById(R.id.fancyCoverFlow);
		        //fancyCoverFlow.setReflectionEnabled(true);
		        //fancyCoverFlow.setReflectionRatio(0.3f);
		        //fancyCoverFlow.setReflectionGap(0);
		        ViewGroupExampleAdapter ViewGroupE= new ViewGroupExampleAdapter(images,this.imageUrls);
		        fancyCoverFlow.setAdapter(ViewGroupE);
	}
	
public void setSmartGridInfo(String html){
		
		WebView newWebView = new WebView(this);
		newWebView.loadData(html, "text/html", "UTF-8");
		this.smartgridDashboard.addView(newWebView);
		
	}
	

	/*private void showImages(LinkedList<Link> imageUrls) {
		// TODO Muestra las imagenes iterando en la lista
		for (Link imgUrl : imageUrls) {
			NetworkImageView image = new NetworkImageView(this);
			image.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT));
			image.setScaleType(ScaleType.CENTER_CROP);
			image.setImageUrl(imgUrl.getUrl(), imageLoader);
			imagesContainer.addView(image);
		}

	}*/

	private String getLinks(PlaceDetails place) {
		// TODO Poner links desde un request a API
		String links = "Links:\n\n"
				+ " http://www.youtube.com/watch?v=UlTxrG37wrI \n\n"
				+ " http://goo.gl/RZb9d1";
		return links;
	}

	public void sendRating() {
		// TODO mandar rating por API al server
	}

	public static class URLSpanNoUnderline extends URLSpan {
		public URLSpanNoUnderline(String url) {
			super(url);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			super.updateDrawState(ds);
			ds.setUnderlineText(false);
		}
	}
	
	
	
	//-------------------FANCYCOVER-------------------------------------------
	
	private static class ViewGroupExampleAdapter extends FancyCoverFlowAdapter {

        // =============================================================================
        // Private members
        // =============================================================================

        private ArrayList<Bitmap> images;
        private ArrayList<String> titulos;
        LinkedList<Link> links;

        // =============================================================================
        // Supertype overrides
        // =============================================================================
        
        
        public ViewGroupExampleAdapter(ArrayList<Bitmap> images, LinkedList<Link> links){
        	this.images = images;
        	this.links = links;
        }
        
        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Bitmap getItem(int i) {
            return images.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getCoverFlowItem(int i, View reuseableView, ViewGroup viewGroup) {
            CustomViewGroup customViewGroup = null;

            if (reuseableView != null) {
                customViewGroup = (CustomViewGroup) reuseableView;
            } else {
                customViewGroup = new CustomViewGroup(viewGroup.getContext());
                customViewGroup.setLayoutParams(new FancyCoverFlow.LayoutParams(1000, 1000));
            }
           
            customViewGroup.getImageView().setImageBitmap(this.getItem(i));
            customViewGroup.getTextView().setText(this.links.get(i).getName());
            
            return customViewGroup;
        }
        
    }

    private static class CustomViewGroup extends LinearLayout {

        // =============================================================================
        // Child views
        // =============================================================================
    	
    	private TextView textView;
        private ImageView imageView;

        // =============================================================================
        // Constructor
        // =============================================================================

        private CustomViewGroup(Context context) {
            super(context);

            this.setOrientation(VERTICAL);
            this.setWeightSum(5);

            this.imageView = new ImageView(context);
            this.textView = new TextView(context);
            

            //LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            LayoutParams layoutParams = new LayoutParams(1000,900);   
            LayoutParams layoutParams2 = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);   
            
            this.imageView.setLayoutParams(layoutParams);
            this.imageView.setScaleType(ScaleType.CENTER_INSIDE);
            this.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            this.imageView.setAdjustViewBounds(true);			
            
            this.textView.setLayoutParams(layoutParams2);
            this.textView.setGravity(Gravity.CENTER);
            this.textView.setTypeface(Typeface.DEFAULT_BOLD);
            this.textView.setTextColor(Color.DKGRAY);
            this.textView.setPadding(0, 20, 0, 0);
            this.textView.setTextSize(20);
            
            
            
            this.addView(this.textView);
            this.addView(this.imageView);
            
        }

        // =============================================================================
        // Getters
        // =============================================================================

        private ImageView getImageView() {
            return imageView;
        }
        private TextView getTextView() {
            return textView;
        }
    }
    
}

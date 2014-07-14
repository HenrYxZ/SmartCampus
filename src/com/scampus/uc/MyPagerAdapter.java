package com.scampus.uc;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.scampus.uc.R;
import com.scampus.tools.BannerElement;


public class MyPagerAdapter extends PagerAdapter {
	private ImageLoader imageLoader;
	private RequestQueue requestQueue; //volley
	private Context context;
	private BannerElement[] elements;
	
    public int getCount() {
        return elements.length;
    }
    public MyPagerAdapter(BannerElement[] elements, Context ctx){
    	this.context = ctx;
    	this.elements = elements;
    
    	requestQueue = Volley.newRequestQueue(context);
     	imageLoader = new ImageLoader(requestQueue, new DiskBitmapCache(context.getCacheDir()));
     	
     	
    }
    //este metodo recibe la vista para mostrar
    public Object instantiateItem(View collection, int position) {
        LayoutInflater inflater = (LayoutInflater) collection.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        View view = inflater.inflate(R.layout.baner, null);
        ImageView image = (ImageView)view.findViewById(R.id.bannerImage);
        
	  	imageLoader.get(elements[position].getUrl(context), ImageLoader.getImageListener(image, R.drawable.transparent, R.drawable.baner1));
	  	
	  	TextView bannerTittle = (TextView)view.findViewById(R.id.bannerTextTittle);
	  	bannerTittle.setText(elements[position].getName());
	  	
	  	String bannerElementType = elements[position].getType().toString();
	  	
	  	if(bannerElementType.equalsIgnoreCase("report"))
	  		bannerTittle.setBackgroundColor(Color.parseColor("#AA0b8db3"));
	  	else if(bannerElementType.equalsIgnoreCase("event"))
	  		bannerTittle.setBackgroundColor(Color.parseColor("#AA8650ac"));
	  	else if(bannerElementType.equalsIgnoreCase("advertise"))
	  		bannerTittle.setBackgroundColor(Color.parseColor("#AA206d33"));
	  	else if(bannerElementType.equalsIgnoreCase("survey"))
	  		bannerTittle.setBackgroundColor(Color.parseColor("#AAff0800"));	

        ((ViewPager) collection).addView(view, 0);
        return view;
    }
    @Override
    public void destroyItem(View arg0, int arg1, Object arg2) {
        ((ViewPager) arg0).removeView((View) arg2);
    }
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);
    }
    @Override
    public Parcelable saveState() {
        return null;
    }
	
    
}



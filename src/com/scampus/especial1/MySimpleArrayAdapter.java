package com.scampus.especial1;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.scampus.especial1.R;

public class MySimpleArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] names;
	private final String[] urls;
	private final int[] positions;
	private final int[] points;
	private ImageLoader imageLoader;
	private RequestQueue requestQueue; //volley



	public MySimpleArrayAdapter(Context context, String[] names, String[] urls, int[] positions,int[]points) {
		super(context, R.layout.rowlayout, names);
		this.context = context;
		this.names = names;
		this.urls = urls;
		this.positions = positions;
		this.points = points;

		requestQueue = Volley.newRequestQueue(context);
		imageLoader = new ImageLoader(requestQueue, new DiskBitmapCache(context.getCacheDir()));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.rowlayout, parent, false);

		TextView textView = (TextView) rowView.findViewById(R.id.label);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		textView.setText(names[position]);

		TextView texto = (TextView) rowView.findViewById(R.id.secondLine);
		texto.setText("Posición "+positions[position]+" con "+points[position]+" puntos");

		

		imageLoader.get(urls[position], ImageLoader.getImageListener(imageView, R.drawable.transparent, R.drawable.transparent));
		// Change the icon for Windows and iPhone
		imageView.setImageResource(R.drawable.camara_72);

		return rowView;
	}
} 
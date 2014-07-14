package com.scampus.tools;

import java.util.ArrayList;

import com.scampus.uc.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class suggestionArrayAdapter extends ArrayAdapter<String> {
  private final Context context;
  private final ArrayList<String> values;
  private Suggestion[] suggestions;

  public suggestionArrayAdapter(Context context, ArrayList<String> values, Suggestion[] suggestions) {
    super(context,  R.layout.suggestion_list_item, values);
    this.context = context;
    this.values = values;
    this.suggestions=suggestions;
    
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.suggestion_list_item, parent, false);
    TextView textView = (TextView) rowView.findViewById(R.id.rowTextView);
    ImageView imageView = (ImageView) rowView.findViewById(R.id.suggestionStatus);
    textView.setText(values.get(position));
    // Change the icon for Windows and iPhone
    Suggestion s = this.suggestions[position];
    if (s.getStatus()==0 && s!=null){    
      imageView.setImageResource(R.drawable.ticket_no);
    } 
    else imageView.setImageResource(R.drawable.ticket_ok);
    return rowView;
  }
  
  public void setSuggestions(Suggestion[] suggestions){
	  
	  this.suggestions = suggestions;
	  return;
  }
  
public Suggestion[] getSuggestions(){
	  
	  return this.suggestions;
  }
} 
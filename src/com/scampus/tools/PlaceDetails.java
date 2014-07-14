package com.scampus.tools;

import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;

public class PlaceDetails implements Parcelable {

	int id;
	String name;
	String description;
	String type;
	double latitude;
	double longitude;

	public PlaceDetails(int id, String name, String descript, String type,
			LatLng position) {
		this.id = id;
		this.name = name;
		this.description = descript;
		this.type = type;
		this.latitude = position.latitude;
		this.longitude = position.longitude;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public String getType() {
		return this.type;
	}

	public double getLatitude() {
		return this.latitude;
	}

	public double getLongitude() {
		return this.longitude;
	}

	// parcel part
	public PlaceDetails(Parcel in) {
		String[] data = new String[6];

		in.readStringArray(data);
		this.id = Integer.parseInt(data[0]);
		this.name = data[1];
		this.description = data[2];
		this.type = data[3];
		this.latitude = Double.parseDouble(data[4]);
		this.longitude = Double.parseDouble(data[5]);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeStringArray(new String[] { Integer.toString(this.id),
				this.name, this.description, this.type,
				Double.toString(this.latitude), Double.toString(this.longitude) });
	}

	public static final Parcelable.Creator<PlaceDetails> CREATOR = new Parcelable.Creator<PlaceDetails>() {

		@Override
		public PlaceDetails createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new PlaceDetails(source); // using parcelable constructor
		}

		@Override
		public PlaceDetails[] newArray(int size) {
			// TODO Auto-generated method stub
			return new PlaceDetails[size];
		}
	};
}

package com.hichip.thecamhi.widget.stickygridview;

import android.os.Parcel;
import android.os.Parcelable;

public class GridItem implements Parcelable {
	private String path;
	private String time;
	private int section;

	public GridItem(String path, String time) {
		super();
		this.path = path;
		this.time = time;
	}
	
	public GridItem(Parcel source) {
		this.path=source.readString();
		this.time=source.readString();
		this.section=source.readInt();
				
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	public int getSection() {
		return section;
	}

	public void setSection(int section) {
		this.section = section;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(path);
		dest.writeString(time);
		dest.writeInt(section);
		
	}
	  public static final Creator<GridItem> CREATOR = new Creator<GridItem>() {
		    @Override public GridItem createFromParcel(Parcel source) {
		      return new GridItem(source);
		    }

		    @Override public GridItem[] newArray(int size) {
		      return new GridItem[size];
		    }
		  };
}

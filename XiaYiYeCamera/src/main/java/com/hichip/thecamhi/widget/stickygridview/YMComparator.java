package com.hichip.thecamhi.widget.stickygridview;

import java.util.Comparator;

public class YMComparator implements Comparator<GridItem> {

	@Override
	public int compare(GridItem o1, GridItem o2) {
		int b =o1.getTime().compareTo(o2.getTime());
		if(b>0){
			return -1;
		}else if (b==0) {
			return 0;
		}else {
			return 1;
		}
			
	}

}

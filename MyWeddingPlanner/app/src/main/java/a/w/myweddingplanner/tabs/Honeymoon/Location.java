package a.w.myweddingplanner.tabs.Honeymoon;

import java.util.ArrayList;
import java.util.List;

class Location {
	boolean isOpen, isSelected;
	String id, title;
	int cost;
	List<Place> places;

	Location(String i, String t, boolean s, int c){
		isSelected = s;
		isOpen = false;
		id = i;
		places = new ArrayList<>();
		title = t;
		cost = c;
	}
}

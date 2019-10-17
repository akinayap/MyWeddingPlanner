package a.w.myweddingplanner.tabs.Packages;

import java.util.ArrayList;
import java.util.List;

class Package {
	boolean isOpen, isSelected;
	String id;
	String title;
	List<Item> items;

	Package(String i, String t, boolean s){
		isSelected = s;
		isOpen = false;
		id = i;
		items = new ArrayList<>();
		title = t;
	}

	int getCost(){
		int totalCost = 0;
		for(Item i : items){
			totalCost += i.cost;
		}

		return totalCost;
	}
}

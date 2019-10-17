package a.w.myweddingplanner.tabs.Budget;

import java.util.ArrayList;
import java.util.List;

public class Category {
	boolean isOpen;
	String title;
	List<BudgetItem> items;

	Category(String t){
		isOpen = false;
		items = new ArrayList<>();
		title = t;
	}

	int getCost(){
		int totalCost = 0;
		for(BudgetItem i : items){
			if(i.selected)
				totalCost += i.cost;
		}

		return totalCost;
	}
}

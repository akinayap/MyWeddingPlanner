package a.w.myweddingplanner.tabs.Budget;

class BudgetItem {
	String id, title, link;
	int cost;
	boolean selected, isOpen;

	BudgetItem(String i, String t, String l, int c, boolean s){
		id = i;
		title = t;
		link = l;
		cost = c;
		selected = s;
		isOpen = false;
	}

	void modify(String t, String l, int c, boolean s) {
		title = t;
		link = l;
		cost = c;
		selected = s;
	}
}

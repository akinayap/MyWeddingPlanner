package a.w.myweddingplanner.tabs.Honeymoon;

class Place {
	String title, image, link;
	double latitude, longitude;
	boolean isOpen;

	Place(String t, String i, String l, double lat, double lng){
		title = t;
		image = i;
		link = l;
		latitude = lat;
		longitude = lng;

		isOpen = false;
	}
}

package a.w.myweddingplanner.tabs.Honeymoon;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import a.w.myweddingplanner.R;

import static android.content.ContentValues.TAG;

public class HoneymoonFrag  extends Fragment implements OnMapReadyCallback {
	private GoogleMap mMap;
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private List<Location> locList = new ArrayList<>();
	private HoneymoonAdapter adapter;
	ConstraintLayout mapView;

	public View onCreateView(@NonNull LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.frag_honeymoon, container, false);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
						.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		return root;
	}


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mapView = view.findViewById(R.id.map_view);
		ConstraintLayout listView = view.findViewById(R.id.list_view);

		mapView.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);

		/* Map View */
		Button goMap = view.findViewById(R.id.go_map_view);
		Button goList = view.findViewById(R.id.go_list_view);
		goMap.setOnClickListener(v->{
			mapView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		});
		goList.setOnClickListener(v->{
			mapView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		});

		ImageButton searchBtn = view.findViewById(R.id.search_btn);
		searchBtn.setOnClickListener(v->{
			EditText et = view.findViewById(R.id.et_location);
			String location = et.getText().toString();
			List<Address> addressList = null;

			Geocoder geocoder = new Geocoder(getContext());
			try {
				addressList = geocoder.getFromLocationName(location,10);

			} catch (IOException e) {
				e.printStackTrace();
			}

			final Dialog dialog = new Dialog(getContext());
			dialog.setContentView(R.layout.address_list_layout);
			TextView tv = dialog.findViewById(R.id.searched_query);
			tv.setText(location);
			for(int i = 0; i < addressList.size(); ++i){
				LinearLayout ll = dialog.findViewById(R.id.list_of_places);
				View newView = LayoutInflater.from(getContext()).inflate(R.layout.address_item, ll, false);

				TextView tvAddress = newView.findViewById(R.id.tv_address);
				TextView tvCountry = newView.findViewById(R.id.tv_country);
				TextView tvGeoLoc = newView.findViewById(R.id.tv_geoloc);

				if(addressList.get(i).getAddressLine(0).equals("")){
					tvAddress.setVisibility(View.GONE);
				}

				if(addressList.get(i).getCountryName() != null && addressList.get(i).getCountryName().equals("")){
					tvCountry.setVisibility(View.GONE);
				}

				tvAddress.setText(addressList.get(i).getAddressLine(0));
				tvCountry.setText(addressList.get(i).getCountryName());
				tvGeoLoc.setText(addressList.get(i).getLatitude() + ", " + addressList.get(0).getLongitude());

				newView.setOnClickListener(nvc->{
					TextView a = nvc.findViewById(R.id.tv_address);
					TextView c = nvc.findViewById(R.id.tv_country);
					TextView l = nvc.findViewById(R.id.tv_geoloc);

					final Dialog addDialog = new Dialog(getContext());
					addDialog.setContentView(R.layout.add_place);

					Button cancelBtn = addDialog.findViewById(R.id.cancel_btn);
					Button okBtn = addDialog.findViewById(R.id.ok_btn);

					EditText etCountry = addDialog.findViewById(R.id.et_loc_country);
					EditText etLocation = addDialog.findViewById(R.id.et_loc_name);
					EditText etLink = addDialog.findViewById(R.id.et_loc_link);
					EditText etImage = addDialog.findViewById(R.id.et_loc_image);

					etLocation.setText(a.getText());
					etCountry.setText(c.getText());

					cancelBtn.setOnClickListener(cv-> {
						addDialog.dismiss();
					});
					okBtn.setOnClickListener(ov->{
						String[] separated = l.getText().toString().split(",");
						Place p = new Place(etLocation.getText().toString(), etImage.getText().toString(), etLink.getText().toString(), Double.parseDouble(separated[0]), Double.parseDouble(separated[1].substring(1)));

						// DO THIS
						int k;
						for(k = 0; k < locList.size(); ++k){
							if(etCountry.getText().toString().equals(locList.get(k).title)){
								locList.get(k).places.add(p);
								final int id = k;

								db.collection("Locations")
												.whereEqualTo("Title", etCountry.getText().toString())
												.get()
												.addOnCompleteListener(task -> {
													if (task.isSuccessful()) {
														for (QueryDocumentSnapshot document : task.getResult()) {
															Map<String, Object> newPlacesOfInterests = new HashMap<>();

															for(Place place : locList.get(id).places){
																Map<String, Object> newPlaceDetails = new HashMap<>();
																newPlaceDetails.put("Image", place.image);
																newPlaceDetails.put("Link", place.link);
																GeoPoint newLatLng = new GeoPoint(place.latitude, place.longitude);
																newPlaceDetails.put("Location", newLatLng);
																newPlacesOfInterests.put(place.title, newPlaceDetails);
															}
															db.collection("Locations").document(document.getId()).update("Places Of Interests", newPlacesOfInterests);
														}
													} else {
														Log.w(TAG, "Error getting documents.", task.getException());
													}
												});
								break;
							}
						}
						if(k == locList.size()){
							Map<String, Object> newPkg = new HashMap<>();
							newPkg.put("Cost", 2000);
							newPkg.put("Title", etCountry.getText().toString());
							newPkg.put("Selected", false);

							Map<String, Object> newPlace = new HashMap<>();
							Map<String, Object> newPlaceDetails = new HashMap<>();
							newPlaceDetails.put("Image", etImage.getText().toString());
							newPlaceDetails.put("Link", etLink.getText().toString());
							GeoPoint newLatLng = new GeoPoint(Double.parseDouble(separated[0]), Double.parseDouble(separated[1].substring(1)));
							newPlaceDetails.put("Location", newLatLng);
							newPlace.put(etLocation.getText().toString(), newPlaceDetails);
							newPkg.put("Places Of Interests", newPlace);

							db.collection("Locations")
											.add(newPkg)
											.addOnSuccessListener(documentReference -> Log.e("Success", documentReference.getId()))
											.addOnFailureListener(e -> Log.e("Exception", e.toString()));
						}
						// Add to firebase
						LatLng latLng = new LatLng(Double.parseDouble(separated[0]), Double.parseDouble(separated[1].substring(1)));
						mMap.addMarker(new MarkerOptions().position(latLng).title(location));
						mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

						addDialog.dismiss();
						dialog.dismiss();
					});

					addDialog.show();
				});

				ll.addView(newView);
			}

			dialog.show();
		});

		/* List View */
		RecyclerView rv = view.findViewById(R.id.rv_locations);
		adapter = new HoneymoonAdapter(locList);
		rv.setAdapter(adapter);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
		rv.setLayoutManager(linearLayoutManager);
	}


	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		ConstraintLayout selectedLocation = mapView.findViewById(R.id.selected_location);
		mMap.setOnMapClickListener(v->selectedLocation.setVisibility(View.GONE));

		db.collection("Locations").orderBy("Title").addSnapshotListener((snapshots, e) -> {
			if (e != null) {
				System.err.println("Listen failed:" + e);
				return;
			}
			for (DocumentChange dc : snapshots.getDocumentChanges()) {
				switch (dc.getType()) {
					case MODIFIED:
						for(int i = 0; i < locList.size(); ++i){
							if(dc.getDocument().get("Title").toString().equals(locList.get(i).title)){
								locList.get(i).places.clear();
								Map<String, Map<String, Object>> placesOfInterests = (Map<String, Map<String, Object>>) dc.getDocument().get("Places Of Interests");
								Object[] poi = placesOfInterests.keySet().toArray();
								for (Object value : poi) {
									String image = placesOfInterests.get(value).get(placesOfInterests.get(value).keySet().toArray()[0].toString()).toString();
									String link = placesOfInterests.get(value).get(placesOfInterests.get(value).keySet().toArray()[1].toString()).toString();
									GeoPoint plc = (GeoPoint) placesOfInterests.get(value).get(placesOfInterests.get(value).keySet().toArray()[2].toString());

									Place p = new Place(value.toString(), image, link, plc.getLatitude(), plc.getLongitude());
									locList.get(i).places.add(p);
									LatLng latLng = new LatLng(p.latitude, p.longitude);
									mMap.addMarker(new MarkerOptions().position(latLng).title(p.title).snippet(p.image));
									mMap.setOnMarkerClickListener(marker -> {
										selectedLocation.setVisibility(View.VISIBLE);
										TextView name = mapView.findViewById(R.id.loc_name);
										ImageView img = mapView.findViewById(R.id.loc_img);

										name.setText(marker.getTitle());
										new DownloadImageTask(img).execute(marker.getSnippet());
										return false;
									});
								}
								adapter.notifyDataSetChanged();
								break;
							}
						}

/*						for(int i = 0; i < locList.size(); ++i){
							if(locList.get(i).id.equals(dc.getDocument().getId())){
								loc = locList.get(i);
								break;
							}
						}
						if(loc != null){
							loc.places.clear();
							Log.e("Modify data", dc.getDocument().get("Places Of Interests").toString());
							Map<String, Map<String, Object>> placesOfInterests = (Map<String, Map<String, Object>>) dc.getDocument().get("Places Of Interests");
							Object[] poi = placesOfInterests.keySet().toArray();
							for(int i = 0 ; i < poi.length; ++i){
								String image = placesOfInterests.get(poi[i]).keySet().toArray()[0].toString();
								String link = placesOfInterests.get(poi[i]).keySet().toArray()[1].toString();
								Object plc = placesOfInterests.get(poi[i]).keySet().toArray()[2];
								Log.e("Loc", plc.toString());
								//String location = placesOfInterests.get(poi[0]).keySet().toArray()[2].toString();
								loc.places.add(new Place(poi[i].toString(), image, link, 0.0, 0.0));
							}
						}*/
						break;
					case ADDED:
						String title = dc.getDocument().get("Title").toString();
						Location loc = new Location(dc.getDocument().getId(), title, (boolean) dc.getDocument().get("Selected"), Integer.parseInt(dc.getDocument().get("Cost").toString()));
						Map<String, Map<String, Object>> placesOfInterests = (Map<String, Map<String, Object>>) dc.getDocument().get("Places Of Interests");
						Object[] poi = placesOfInterests.keySet().toArray();
						for (Object value : poi) {
							String image = placesOfInterests.get(value).get(placesOfInterests.get(value).keySet().toArray()[0].toString()).toString();
							String link = placesOfInterests.get(value).get(placesOfInterests.get(value).keySet().toArray()[1].toString()).toString();
							GeoPoint plc = (GeoPoint) placesOfInterests.get(value).get(placesOfInterests.get(value).keySet().toArray()[2].toString());

							Place p = new Place(value.toString(), image, link, plc.getLatitude(), plc.getLongitude());
							loc.places.add(p);
							LatLng latLng = new LatLng(p.latitude, p.longitude);
							mMap.addMarker(new MarkerOptions().position(latLng).title(p.title).snippet(p.image));
							mMap.setOnMarkerClickListener(marker -> {
								selectedLocation.setVisibility(View.VISIBLE);
								TextView name = mapView.findViewById(R.id.loc_name);
								ImageView img = mapView.findViewById(R.id.loc_img);

								name.setText(marker.getTitle());
								new DownloadImageTask(img).execute(marker.getSnippet());
								return false;
							});
						}
						locList.add(loc);
						adapter.notifyDataSetChanged();
						break;
					case REMOVED:
						locList.removeIf(o -> o.id.equals(dc.getDocument().getId()));
						adapter.notifyDataSetChanged();
						break;
					default:
						break;
				}
			}
		});
	}


	private class DownloadImageTask  extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;
		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap bmp = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				bmp = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return bmp;
		}
		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
		}
	}
}
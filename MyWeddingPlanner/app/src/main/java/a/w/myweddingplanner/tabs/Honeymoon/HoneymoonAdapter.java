package a.w.myweddingplanner.tabs.Honeymoon;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import a.w.myweddingplanner.R;

public class HoneymoonAdapter  extends RecyclerView.Adapter<HoneymoonAdapter.ViewHolder> {

	private List<Location> locations;
	private Context mContext;

	public HoneymoonAdapter(List<Location> loc) {
		locations = loc;
	}

	@NonNull
	@Override
	public HoneymoonAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		mContext = parent.getContext();
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.honeymoon_item, parent, false);
		return new HoneymoonAdapter.ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final HoneymoonAdapter.ViewHolder holder, final int position) {
		Location loc = locations.get(position);

		holder.tv.setText(loc.title);
		holder.tvTotal.setText(Integer.toString(loc.cost));

		holder.ll.setVisibility(loc.isOpen ? View.VISIBLE : View.GONE);

		holder.ll.removeAllViews();
		for(Place i : loc.places){
			View v = LayoutInflater.from(mContext).inflate(R.layout.honeymoon_details_item, holder.ll, false);
			TextView title = v.findViewById(R.id.detail_title);
			ImageView image = v.findViewById(R.id.detail_img);
			TextView link = v.findViewById(R.id.detail_link);

			if(!i.image.equals(""))
				new DownloadImageTask(image).execute(i.image);

			link.setText(i.link);

			ConstraintLayout cl = v.findViewById(R.id.more_details);
			cl.setVisibility(i.isOpen ? View.VISIBLE : View.GONE);

			v.setOnClickListener(c->{
				i.isOpen = !i.isOpen;
				cl.setVisibility(i.isOpen ? View.VISIBLE : View.GONE);
			});

			v.setOnLongClickListener(view -> {
				final Dialog addDialog = new Dialog(mContext);
				addDialog.setContentView(R.layout.add_place);

				Button cancelBtn = addDialog.findViewById(R.id.cancel_btn);
				Button okBtn = addDialog.findViewById(R.id.ok_btn);

				TextView tvCountry = addDialog.findViewById(R.id.tv_loc_country);
				EditText etCountry = addDialog.findViewById(R.id.et_loc_country);
				EditText etLocation = addDialog.findViewById(R.id.et_loc_name);
				EditText etLink = addDialog.findViewById(R.id.et_loc_link);
				EditText etImage = addDialog.findViewById(R.id.et_loc_image);

				etLocation.setText(i.title);
				tvCountry.setVisibility(View.GONE);
				etCountry.setVisibility(View.GONE);
				etLink.setText(i.link);
				etImage.setText(i.image);

				cancelBtn.setOnClickListener(cv-> {
					addDialog.dismiss();
				});
				okBtn.setOnClickListener(ov->{
					i.modify(etLocation.getText().toString(), etImage.getText().toString(), etLink.getText().toString());

					FirebaseFirestore db = FirebaseFirestore.getInstance();
					db.collection("Locations")
									.whereEqualTo("Title", loc.title)
									.get()
									.addOnCompleteListener(task -> {
										if (task.isSuccessful()) {
											for (QueryDocumentSnapshot document : task.getResult()) {
												Map<String, Object> newPlacesOfInterests = new HashMap<>();

												for(Place place : loc.places){
													Log.e("Place", place.title);
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
											Log.w("Error", "Error getting documents.", task.getException());
										}
									});

					addDialog.dismiss();
				});

				addDialog.show();
				return false;
			});

			title.setText(i.title);
			holder.ll.addView(v);
		}

		holder.cl.setOnClickListener(v -> {
			loc.isOpen = !loc.isOpen;
			holder.ll.setVisibility(loc.isOpen ? View.VISIBLE : View.GONE);
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

	@Override
	public int getItemCount() {
		return locations.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder{
		ConstraintLayout cl;
		LinearLayout ll;
		TextView tv, tvTotal;
		ViewHolder(View itemView) {
			super(itemView);
			cl = itemView.findViewById(R.id.expand);
			ll = itemView.findViewById(R.id.loc_items);
			tv = itemView.findViewById(R.id.loc_title);
			tvTotal = itemView.findViewById(R.id.loc_total);
		}
	}
}
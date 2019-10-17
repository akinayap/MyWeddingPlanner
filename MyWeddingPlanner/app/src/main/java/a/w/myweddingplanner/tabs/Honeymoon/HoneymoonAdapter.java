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
package a.w.myweddingplanner.tabs.Packages;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import a.w.myweddingplanner.R;

class PackagesAdapter extends RecyclerView.Adapter<PackagesAdapter.ViewHolder> {

	private List<Package> packagesList;
	private Context mContext;

	public PackagesAdapter(List<Package> pkgs) {
		packagesList = pkgs;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		mContext = parent.getContext();
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.packages_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
		Package pkg = packagesList.get(position);
		holder.tv.setText(pkg.title);
		holder.tvTotal.setText(Integer.toString(pkg.getCost()));

		holder.cb.setChecked(pkg.isSelected);
		holder.cb.setOnCheckedChangeListener((compoundButton, b) -> {
			pkg.isSelected = b;
			FirebaseFirestore db = FirebaseFirestore.getInstance();
			db.collection("Packages")
							.document(pkg.id)
							.update("Selected", pkg.isSelected);
		});

		holder.ll.setVisibility(pkg.isOpen ? View.VISIBLE : View.GONE);
		holder.ll.removeAllViews();
		for(Item i : pkg.items){
			View v = LayoutInflater.from(mContext).inflate(R.layout.package_detail_item, holder.ll, false);
			TextView title = v.findViewById(R.id.detail_title);
			TextView cost = v.findViewById(R.id.detail_cost);
			title.setText(i.title);
			cost.setText(Integer.toString(i.cost));
			holder.ll.addView(v);
		}

		holder.cl.setOnClickListener(v -> {
			holder.ll.setVisibility(pkg.isOpen ? View.VISIBLE : View.GONE);
			pkg.isOpen = !pkg.isOpen;
		});
		holder.cl.setOnLongClickListener(view -> {
			final Dialog dialog = new Dialog(mContext);
			dialog.setContentView(R.layout.add_item_layout);
			EditText shopName = dialog.findViewById(R.id.et_shop_name);
			shopName.setText(pkg.title);

			for(int i = 0; i < pkg.items.size(); ++i){
				addField(dialog, i+1, pkg.items.get(i).title, pkg.items.get(i).cost);
			}
			final int[] itemCount = {pkg.items.size()};

			ImageButton delBtn = dialog.findViewById(R.id.delete_btn);
			delBtn.setVisibility(View.VISIBLE);
			delBtn.setOnClickListener(v->{
				FirebaseFirestore db = FirebaseFirestore.getInstance();
				db.collection("Packages").document(pkg.id).delete();
				packagesList.remove(pkg);
				dialog.dismiss();
			});

			Button cancelBtn = dialog.findViewById(R.id.cancel_btn);
			Button addFieldBtn = dialog.findViewById(R.id.add_field_btn);
			Button okBtn = dialog.findViewById(R.id.ok_btn);

			cancelBtn.setOnClickListener(cv->dialog.dismiss());
			addFieldBtn.setOnClickListener(av->{
				++itemCount[0];
				LinearLayout ll = dialog.findViewById(R.id.all_items);
				View newView = LayoutInflater.from(mContext).inflate(R.layout.add_package_item, ll, false);
				TextView tv = newView.findViewById(R.id.item_id);
				tv.setText("Item " + itemCount[0]);
				ImageButton btn = newView.findViewById(R.id.del_btn);
				btn.setOnClickListener(bv->{ll.removeView(newView);});
				ll.addView(newView);
			});
			okBtn.setOnClickListener(ov->{
				FirebaseFirestore db = FirebaseFirestore.getInstance();
				db.collection("Packages").document(pkg.id).delete();
				packagesList.remove(pkg);
				notifyDataSetChanged();

				Map<String, Object> newPkg = new HashMap<>();
				newPkg.put("Title", shopName.getText().toString());

				LinearLayout ll = dialog.findViewById(R.id.all_items);
				for(int i = 0; i < ll.getChildCount(); ++i){
					View child = ll.getChildAt(i);
					EditText item = child.findViewById(R.id.et_item);
					EditText cost = child.findViewById(R.id.et_cost);

					newPkg.put(item.getText().toString(), Integer.parseInt(cost.getText().toString()));
				}

				db.collection("Packages")
								.add(newPkg)
								.addOnSuccessListener(documentReference -> Log.e("Success", documentReference.getId()))
								.addOnFailureListener(e -> Log.e("Exception", e.toString()));
				dialog.dismiss();
			});

			dialog.show();
			return false;
		});
	}

	private void addField(Dialog dialog, int id, String title, int cost){
		LinearLayout ll = dialog.findViewById(R.id.all_items);
		View newView = LayoutInflater.from(mContext).inflate(R.layout.add_package_item, ll, false);
		TextView tv = newView.findViewById(R.id.item_id);
		tv.setText("Item " + id);
		ImageButton btn = newView.findViewById(R.id.del_btn);
		btn.setOnClickListener(bv->{ll.removeView(newView);});

		EditText title_et = newView.findViewById(R.id.et_item);
		title_et.setText(title);
		EditText cost_et = newView.findViewById(R.id.et_cost);
		cost_et.setText(Integer.toString(cost));

		ll.addView(newView);
	}

	@Override
	public int getItemCount() {
		return packagesList.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder{
		ConstraintLayout cl;
		LinearLayout ll;
		TextView tv, tvTotal;
		CheckBox cb;
		//Button btn;;

		ViewHolder(View itemView) {
			super(itemView);
			cl = itemView.findViewById(R.id.expand);
			ll = itemView.findViewById(R.id.shop_items);
			cb = itemView.findViewById(R.id.select_box);
			/*rv = itemView.findViewById(R.id.rv_details);*/
			tv = itemView.findViewById(R.id.shop_title);
			tvTotal = itemView.findViewById(R.id.shop_total);
			//btn = itemView.findViewById(R.id.add_new_item);
		}
	}
}
package a.w.myweddingplanner.tabs.Budget;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import a.w.myweddingplanner.R;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

	private List<Category> categories;
	private Context mContext;

	public BudgetAdapter(List<Category> cat) {
		categories = cat;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		mContext = parent.getContext();
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.budget_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
		Category cat = categories.get(position);

		holder.tv.setText(cat.title);
		holder.tvTotal.setText(Integer.toString(cat.getCost()));
		holder.ll.setVisibility(cat.isOpen ? View.VISIBLE : View.GONE);

		holder.ll.removeAllViews();
		for(BudgetItem i : cat.items){
			View v = LayoutInflater.from(mContext).inflate(R.layout.budget_detail_item, holder.ll, false);
			TextView title = v.findViewById(R.id.detail_title);
			TextView cost = v.findViewById(R.id.detail_cost);
			TextView link = v.findViewById(R.id.detail_link);
			CheckBox cb = v.findViewById(R.id.select_box);
			cb.setChecked(i.selected);
			cb.setOnCheckedChangeListener((compoundButton, b) -> {
				i.selected = b;
				holder.tvTotal.setText(Integer.toString(cat.getCost()));

				FirebaseFirestore db = FirebaseFirestore.getInstance();
				db.collection(cat.title)
								.document(i.id)
								.update("Selected", i.selected);

			});

			ConstraintLayout cl = v.findViewById(R.id.more_details);
			cl.setVisibility(i.isOpen ? View.VISIBLE : View.GONE);

			v.setOnClickListener(c->{
				i.isOpen = !i.isOpen;
				cl.setVisibility(i.isOpen ? View.VISIBLE : View.GONE);
			});

			link.setText(i.link);
			title.setText(i.title);
			cost.setText(Integer.toString(i.cost));
			holder.ll.addView(v);
		}

		holder.cl.setOnClickListener(v -> {
			cat.isOpen = !cat.isOpen;
			holder.ll.setVisibility(cat.isOpen ? View.VISIBLE : View.GONE);
		});

		holder.cl.setOnLongClickListener(view -> {
			final Dialog dialog = new Dialog(mContext);
			dialog.setContentView(R.layout.add_budget_item_layout);
			Spinner spinner = dialog.findViewById(R.id.spinner_cat);
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, BudgetFrag.cat_names);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(dataAdapter);
			spinner.setSelection(BudgetFrag.cat_names.indexOf(cat.title));

			for(int i = 0; i < cat.items.size(); ++i){
				addField(dialog, i+1, cat.items.get(i).title, cat.items.get(i).cost, cat.items.get(i).link);
			}
			final int[] itemCount = {cat.items.size()};

			Button cancelBtn = dialog.findViewById(R.id.cancel_btn);
			Button addFieldBtn = dialog.findViewById(R.id.add_field_btn);
			Button okBtn = dialog.findViewById(R.id.ok_btn);

			cancelBtn.setOnClickListener(cv->dialog.dismiss());
			addFieldBtn.setOnClickListener(av->{
				++itemCount[0];
				LinearLayout ll = dialog.findViewById(R.id.all_items);
				View newView = LayoutInflater.from(mContext).inflate(R.layout.add_budget_item, ll, false);
				TextView tv = newView.findViewById(R.id.item_id);
				tv.setText("Item " + itemCount[0]);
				ImageButton btn = newView.findViewById(R.id.del_btn);
				btn.setOnClickListener(bv->{ll.removeView(newView);});
				ll.addView(newView);
			});
			okBtn.setOnClickListener(ov->{
				Spinner s = dialog.findViewById(R.id.spinner_cat);


				FirebaseFirestore db = FirebaseFirestore.getInstance();
				for(BudgetItem i : cat.items){
					db.collection(s.getSelectedItem().toString()).document(i.id).delete();
				}
				LinearLayout ll = dialog.findViewById(R.id.all_items);
				for(int i = 0; i < ll.getChildCount(); ++i) {
					View child = ll.getChildAt(i);
					EditText item = child.findViewById(R.id.et_item);
					EditText cost = child.findViewById(R.id.et_cost);
					EditText link = child.findViewById(R.id.et_link);
					if (!(item.getText().toString().equals("")) && !(cost.getText().toString().equals("")) && !(link.getText().toString().equals(""))) {
						Map<String, Object> newItem = new HashMap<>();
						newItem.put("Title", item.getText().toString());
						newItem.put("Selected", false);
						newItem.put("Link", link.getText().toString());
						newItem.put("Cost", Integer.parseInt(cost.getText().toString()));
						db.collection(s.getSelectedItem().toString())
										.add(newItem)
										.addOnSuccessListener(documentReference -> Log.e("Success", documentReference.getId()))
										.addOnFailureListener(e -> Log.e("Exception", e.toString()));

					}
				}
				dialog.dismiss();
			});

			dialog.show();
			return false;
		});
	}

	private void addField(Dialog dialog, int i, String title, int cost, String link){
		LinearLayout ll = dialog.findViewById(R.id.all_items);
		View newView = LayoutInflater.from(mContext).inflate(R.layout.add_budget_item, ll, false);
		TextView tv = newView.findViewById(R.id.item_id);
		tv.setText("Item " + i);
		ImageButton btn = newView.findViewById(R.id.del_btn);
		btn.setOnClickListener(bv->{ll.removeView(newView);});

		EditText title_et = newView.findViewById(R.id.et_item);
		title_et.setText(title);
		EditText cost_et = newView.findViewById(R.id.et_cost);
		cost_et.setText(Integer.toString(cost));
		EditText link_et = newView.findViewById(R.id.et_link);
		link_et.setText(link);

		ll.addView(newView);
	}

	@Override
	public int getItemCount() {
		return categories.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder{
		ConstraintLayout cl;
		LinearLayout ll;
		TextView tv, tvTotal;
		ViewHolder(View itemView) {
			super(itemView);
			cl = itemView.findViewById(R.id.expand);
			ll = itemView.findViewById(R.id.cat_items);
			tv = itemView.findViewById(R.id.cat_name);
			tvTotal = itemView.findViewById(R.id.cat_total);
		}
	}
}
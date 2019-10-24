package a.w.myweddingplanner.tabs.Budget;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import a.w.myweddingplanner.R;

public class BudgetFrag extends Fragment {
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private List<Category> categories = new ArrayList<>();
	public static List<String> cat_names = Arrays.asList("Decorations", "Invitations", "Venue", "Miscellaneous");
	private BudgetAdapter adapter;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.frag_budget, container, false);
		for(String catName : cat_names){
			Category cat = new Category(catName);
			db.collection(catName).addSnapshotListener((snapshots, e) -> {
				if (e != null) {
					System.err.println("Listen failed:" + e);
					return;
				}
				for (DocumentChange dc : snapshots.getDocumentChanges()) {
					switch (dc.getType()) {
						case MODIFIED:
							BudgetItem item = null;
							for(int i = 0; i < cat.items.size(); ++i){
								if(cat.items.get(i).id.equals(dc.getDocument().getId())){
									item = cat.items.get(i);
									break;
								}
							}
							if(item != null){
								String title = dc.getDocument().get("Title").toString();
								String link = dc.getDocument().get("Link").toString();
								int cost = Integer.parseInt(dc.getDocument().get("Cost").toString());
								boolean selected = (boolean) dc.getDocument().get("Selected");
								item.modify(title, link, cost, selected);
								adapter.notifyDataSetChanged();
							}
							break;
						case ADDED:
							String title = dc.getDocument().get("Title").toString();
							String link = dc.getDocument().get("Link").toString();
							int cost = Integer.parseInt(dc.getDocument().get("Cost").toString());
							boolean selected = (boolean) dc.getDocument().get("Selected");
							BudgetItem bi = new BudgetItem(dc.getDocument().getId(), title, link, cost, selected);
							cat.items.add(bi);
							adapter.notifyDataSetChanged();
							break;
						case REMOVED:
							cat.items.removeIf(o -> o.id.equals(dc.getDocument().getId()));
							adapter.notifyDataSetChanged();
							break;
						default:
							break;
					}
				}
			});
			categories.add(cat);
		}
		return root;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Init add btn
		Button addBtn = view.findViewById(R.id.add_btn);
		addBtn.setOnClickListener(v->{
			final Dialog dialog = new Dialog(getContext());
			final int[] itemCount = {0};
			dialog.setContentView(R.layout.add_budget_item_layout);

			Spinner spinner = dialog.findViewById(R.id.spinner_cat);
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, cat_names);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(dataAdapter);

			Button cancelBtn = dialog.findViewById(R.id.cancel_btn);
			Button addFieldBtn = dialog.findViewById(R.id.add_field_btn);
			Button okBtn = dialog.findViewById(R.id.ok_btn);

			cancelBtn.setOnClickListener(cv->dialog.dismiss());
			addFieldBtn.setOnClickListener(av->{
				++itemCount[0];
				LinearLayout ll = dialog.findViewById(R.id.all_items);
				View newView = LayoutInflater.from(getContext()).inflate(R.layout.add_budget_item, ll, false);
				TextView tv = newView.findViewById(R.id.item_id);
				tv.setText("Item " + itemCount[0]);
				ImageButton btn = newView.findViewById(R.id.del_btn);
				btn.setOnClickListener(bv->{ll.removeView(newView);});
				ll.addView(newView);
			});
			okBtn.setOnClickListener(ov->{
				Spinner s = dialog.findViewById(R.id.spinner_cat);

				LinearLayout ll = dialog.findViewById(R.id.all_items);
				for(int i = 0; i < ll.getChildCount(); ++i){
					View child = ll.getChildAt(i);
					EditText item = child.findViewById(R.id.et_item);
					EditText cost = child.findViewById(R.id.et_cost);
					EditText link = child.findViewById(R.id.et_link);
					if(!(item.getText().toString().equals("")) && !(cost.getText().toString().equals(""))&& !(link.getText().toString().equals("")))
					{
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
		});

		// Init rv
		RecyclerView rv = view.findViewById(R.id.rv_categories);
		adapter = new BudgetAdapter(categories);
		rv.setAdapter(adapter);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
		rv.setLayoutManager(linearLayoutManager);
	}

}
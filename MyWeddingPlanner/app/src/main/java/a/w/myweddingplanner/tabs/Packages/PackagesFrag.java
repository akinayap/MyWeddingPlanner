package a.w.myweddingplanner.tabs.Packages;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import a.w.myweddingplanner.R;

public class PackagesFrag extends Fragment {
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private List<Package> packageList = new ArrayList<>();
	private PackagesAdapter adapter;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.frag_packages, container, false);

		db.collection("Packages").orderBy("Title").addSnapshotListener((snapshots, e) -> {
			if (e != null) {
				System.err.println("Listen failed:" + e);
				return;
			}
			for (DocumentChange dc : snapshots.getDocumentChanges()) {
				Package pkg = null;
				switch (dc.getType()) {
					case MODIFIED:
						for(int i = 0; i < packageList.size(); ++i){
							if(packageList.get(i).id.equals(dc.getDocument().getId())){
								pkg = packageList.get(i);
								break;
							}
						}
						if(pkg != null){
							pkg.items.clear();
							for(String data : dc.getDocument().getData().keySet()){
								if(!data.equals("Title") && !data.equals("Selected")){
									pkg.items.add(new Item(data, Integer.parseInt(dc.getDocument().get(data).toString())));
								}
							}
							adapter.notifyDataSetChanged();
						}
						break;
					case ADDED:
						String title = dc.getDocument().get("Title").toString();
						pkg = new Package(dc.getDocument().getId(), title, (boolean) dc.getDocument().get("Selected"));
						for(String data : dc.getDocument().getData().keySet()){
							if(!data.equals("Title") && !data.equals("Selected")){
								pkg.items.add(new Item(data, Integer.parseInt(dc.getDocument().get(data).toString())));
							}
						}
						packageList.add(pkg);
						adapter.notifyDataSetChanged();
						break;
					case REMOVED:
						packageList.removeIf(o -> o.id.equals(dc.getDocument().getId()));
						adapter.notifyDataSetChanged();
						break;
					default:
						break;
				}
			}
		});

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
			dialog.setContentView(R.layout.add_item_layout);

			ImageButton delBtn = dialog.findViewById(R.id.delete_btn);
			delBtn.setVisibility(View.VISIBLE);

			Button cancelBtn = dialog.findViewById(R.id.cancel_btn);
			Button addFieldBtn = dialog.findViewById(R.id.add_field_btn);
			Button okBtn = dialog.findViewById(R.id.ok_btn);

			cancelBtn.setOnClickListener(cv->dialog.dismiss());
			addFieldBtn.setOnClickListener(av->{
				++itemCount[0];
				LinearLayout ll = dialog.findViewById(R.id.all_items);
				View newView = LayoutInflater.from(getContext()).inflate(R.layout.add_package_item, ll, false);
				TextView tv = newView.findViewById(R.id.item_id);
				tv.setText("Item " + itemCount[0]);
				ImageButton btn = newView.findViewById(R.id.del_btn);
				btn.setOnClickListener(bv->{ll.removeView(newView);});
				ll.addView(newView);

			});
			okBtn.setOnClickListener(ov->{
				Map<String, Object> newPkg = new HashMap<>();
				EditText shopName = dialog.findViewById(R.id.et_shop_name);
				newPkg.put("Title", shopName.getText().toString());

				LinearLayout ll = dialog.findViewById(R.id.all_items);
				for(int i = 0; i < ll.getChildCount(); ++i){
					View child = ll.getChildAt(i);
					EditText item = child.findViewById(R.id.et_item);
					EditText cost = child.findViewById(R.id.et_cost);
					if(!(item.getText().toString().equals("")) && !(cost.getText().toString().equals("")))
						newPkg.put(item.getText().toString(), Integer.parseInt(cost.getText().toString()));
				}

				db.collection("Packages")
								.add(newPkg)
								.addOnSuccessListener(documentReference -> Log.e("Success", documentReference.getId()))
								.addOnFailureListener(e -> Log.e("Exception", e.toString()));

				dialog.dismiss();
			});

			dialog.show();
		});

		// Init rv
		RecyclerView rv = view.findViewById(R.id.rv_packages);
		adapter = new PackagesAdapter(packageList);
		rv.setAdapter(adapter);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
		rv.setLayoutManager(linearLayoutManager);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.e("Pause", "Do saving here");
	}
}

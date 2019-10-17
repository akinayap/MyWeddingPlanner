package a.w.myweddingplanner.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import a.w.myweddingplanner.R;

public class GuestlistFrag  extends Fragment {
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.frag_guestlist, container, false);
		final TextView textView = root.findViewById(R.id.text_guestlist);
		textView.setText("Guest List");
		return root;
	}
}
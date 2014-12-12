package supr.rachl.ftc.defcon2014;

import supr.rachl.ftc.defcon2014.dummy.DummyContent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//Class Description:
//Fragment showing campaign progress
//Allows user to cancel campaign

public class ItemDetailFragment extends Fragment {
	private static final String DEBUG_TAG = "sr_defcon_ItemDetailFragment";
	
	Handler handler; 
	
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private DummyContent.DummyItem mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false); 

		String validatedNumbers;
		validatedNumbers = workPrefString("read", "validatedNumbers", "");
	
		// Show the validated numbers as text in a TextView.
		if (mItem != null) {
			((TextView) rootView.findViewById(R.id.item_detail)).setText(validatedNumbers);
		}
		
		return rootView;
	}
	
	public String workPrefString(String type, String pref, String value) {
		String thisPref;
		if (type == "read") {
			final SharedPreferences mPref = this.getActivity().getApplicationContext().getSharedPreferences(pref,
					Context.MODE_PRIVATE);
			thisPref = mPref.getString(pref, value);
			return thisPref;
		} else if (type == "write") {
			final SharedPreferences mPref = this.getActivity().getApplicationContext().getSharedPreferences(pref,
					Context.MODE_PRIVATE);
			thisPref = mPref.getString(pref, "");
			mPref.edit().putString(pref, value).commit();
			return value;
		} else
			return "error";
	}
}

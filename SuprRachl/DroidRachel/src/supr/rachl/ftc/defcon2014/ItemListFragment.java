package supr.rachl.ftc.defcon2014;

import supr.rachl.ftc.defcon2014.dummy.DummyContent;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

//Class Description:
//Fragment showing application option menu

public class ItemListFragment extends ListFragment {

	ListAdapter myListAdapter;

	private static final String DEBUG_TAG = "sr_defcon_ItemListFragment";

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO: replace with a real list adapter.

		myListAdapter = new ArrayAdapter<DummyContent.DummyItem>(
				getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1,
				DummyContent.ITEMS);

		setListAdapter(myListAdapter);

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		if (DummyContent.ITEMS.get(position).content.equalsIgnoreCase("Run & Report")) {

			String targetNumbers = "";
			String campaignType = "";
			String campaignScript = "";

			targetNumbers = workPrefString("read", "targetNumbers", "");
			campaignType = workPrefString("read", "campaignType", "");
			campaignScript = workPrefString("read", "campaignScript", "");

			if ((targetNumbers.length() == 0) || (campaignType.length() == 0) || (campaignScript.length() == 0)) {
				String checkingInput = "Missing targets or campaign data.";
				Toast.makeText(this.getActivity().getApplicationContext(),checkingInput.toString(), Toast.LENGTH_LONG).show();		
			}
			else {
				//Check targets
				Boolean targetok = false;
				String checkingTargets = "Prepaing campaign ... Please wait.";
				Toast.makeText(this.getActivity().getApplicationContext(),checkingTargets.toString(), Toast.LENGTH_LONG).show();						
				String validatedNumbers = workPrefString("read", "validatedNumbers", "");
				if (validatedNumbers.length() > 0) {
						targetok = true;
				}
				if (targetok) {
					//Check targets
					Boolean campaignok = false;
					//Hard coded campaign (robocall) for demo, no networking to cc-Server
					campaignok = true;
					if (campaignok) {
						mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
					}
					else {
						String failCampaign = "Campaign error.";
						Toast.makeText(this.getActivity().getApplicationContext(),failCampaign.toString(), Toast.LENGTH_LONG).show();		
					}
				}
				else {
					String failTargets = "No valid target numbers found.";
					Toast.makeText(this.getActivity().getApplicationContext(),failTargets.toString(), Toast.LENGTH_LONG).show();		    			
				}    		
			}
		}
		else if (DummyContent.ITEMS.get(position).content.equalsIgnoreCase("Fetch Targets")) {
			// Notify if targets are fetched
			//Log.i(DEBUG_TAG , "Fetching targets");
			DialogFetchTargets dialogtargets = new DialogFetchTargets(getActivity());
			dialogtargets.show();
		}
		else if (DummyContent.ITEMS.get(position).content.equalsIgnoreCase("Fetch Campaign")) {
			// Notify if campaign is fetched
			//Log.i(DEBUG_TAG , "Fetching campaign");
			DialogFetchCampaign dialogcampaign = new DialogFetchCampaign(getActivity());
			dialogcampaign.show();
		}        
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}


	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(activateOnItemClick
				? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
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



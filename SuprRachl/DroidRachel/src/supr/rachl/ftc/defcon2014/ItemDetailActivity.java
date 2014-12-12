package supr.rachl.ftc.defcon2014;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

//Class Description:
//Shows campaign progress
//Allows user to cancel campaign

public class ItemDetailActivity extends FragmentActivity {

	private static final String DEBUG_TAG = "sr_defcon_ItemDetailActivity";
	private PendingIntent mAlarmSender;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ItemDetailFragment.ARG_ITEM_ID,
					getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
			ItemDetailFragment fragment = new ItemDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
			.add(R.id.item_detail_container, fragment)
			.commit();
		}

		// If not yet running create an IntentSender that will launch our campaign service, to be scheduled
		// with the alarm manager.
		mAlarmSender = PendingIntent.getService(this, 0, new Intent(
				this, CampaignService.class), 0);
		startCampaignService();
		
		DialogCampaignRun dialogrun = new DialogCampaignRun(this);
		dialogrun.show();	
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			cancelCampaignService();
			String cancellingCampaign = "Campaign cancelled.";
			Toast.makeText(this,cancellingCampaign.toString(), Toast.LENGTH_SHORT).show();
			
			NavUtils.navigateUpTo(this, new Intent(this, ItemListActivity.class));
			
			finish();

			return true;
			
		}
		return super.onOptionsItemSelected(item);
	}

	public void startCampaignService() {
		
		//Log.i(DEBUG_TAG , "Starting Campaign service");
		//register a new alarm service
		long firstTime;
		firstTime = SystemClock.elapsedRealtime() + 15000;
		AlarmManager alarms = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarms.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				firstTime, 1000*60*5, mAlarmSender);
		// triggers campaign to new number every 5 minutes

	}

	public void cancelCampaignService() {
		//Log.i(DEBUG_TAG , "Cancelling active campaign services");
		//cancelling alarm sender
		AlarmManager alarms = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarms.cancel(mAlarmSender);

	}    


	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();					
	}

}

package supr.rachl.ftc.defcon2014;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

//Class Description:
//Notifies user if campaign assets have been fetched
//Structures and stores the campaign details for later use

public class DialogFetchCampaign extends Dialog implements OnClickListener {
	final String DEBUG_TAG = "sr_defcon_DialogFetchTarget";
	Button okButton;
	String numbers = "";

	public DialogFetchCampaign(Context context) {
		super(context);
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// remove white frame around dialog-pop-up
		this.getWindow().setBackgroundDrawable(new ColorDrawable(0));

		setContentView(R.layout.dialog_fetch_campaign);
		okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(this);
		
		//Hard coded campaign (robocall) for demo, no networking to cc-Server
		String campaign = "Robocall 'Welcome to Bank of America. We regret to inform you that due to supsicious activity on your account your debit card has been temporarily loked. In order to unlock we need to verify your idendity by entering some of your information on file. Please enter your 16 digit account number as it appears on your Visa debit card.'";
		Toast.makeText(getContext(),"Campaign: " + campaign.toString(), Toast.LENGTH_LONG).show();			
		//saving campaign details
		String campaignType = "robocall";
		String campaignScript = "Welcome to Bank of America. We regret to inform you that due to supsicious activity on your account your debit card has been temporarily loked. In order to unlock we need to verify your idendity by entering some of your information on file. Please enter your 16 digit account number as it appears on your Visa debit card.";
		workPrefString("write", "campaignType", campaignType);
		workPrefString("write", "campaignScript", campaignScript);
		
	}

	public void onClick(View v) {
		if (v == okButton) {
			dismiss();
		}
	}
	
	public String workPrefString(String type, String pref, String value) {
		String thisPref;
		if (type == "read") {
			final SharedPreferences mPref = getContext().getSharedPreferences(pref,
					Context.MODE_PRIVATE);
			thisPref = mPref.getString(pref, value);
			return thisPref;
		} else if (type == "write") {
			final SharedPreferences mPref = getContext().getSharedPreferences(pref,
					Context.MODE_PRIVATE);
			thisPref = mPref.getString(pref, "");
			mPref.edit().putString(pref, value).commit();
			return value;
		} else
			return "error";
	}

}	



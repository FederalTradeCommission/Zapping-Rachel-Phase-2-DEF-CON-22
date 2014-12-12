package supr.rachl.ftc.defcon2014;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

//Class Description:
//Notifies user of start of campaign

public class DialogCampaignRun extends Dialog implements OnClickListener {
	final String DEBUG_TAG = "sr_defcon_DialogFetchTarget";
	Button okButton;

	public DialogCampaignRun(Context context) {
		super(context);
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// remove white frame around dialog-pop-up
		this.getWindow().setBackgroundDrawable(new ColorDrawable(0));

		setContentView(R.layout.dialog_campaign_run);
		okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(this);
				
	}

	public void onClick(View v) {
		if (v == okButton) {
			dismiss();
			//show cancel button in Campaign View
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



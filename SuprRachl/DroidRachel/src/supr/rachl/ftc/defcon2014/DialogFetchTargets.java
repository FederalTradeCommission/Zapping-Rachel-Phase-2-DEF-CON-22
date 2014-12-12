package supr.rachl.ftc.defcon2014;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

//Class Description:
//Notifies user if campaign targets (numbers) have been fetched
//Validates numbers to avoid honey pot detection using 2xAPI
//Structures and stores numbers for later use


public class DialogFetchTargets extends Dialog implements OnClickListener {
	final String DEBUG_TAG = "sr_defcon_DialogFetchTarget";
	Button okButton;
	
	private checkAPIs m_checkAPIs;

	public DialogFetchTargets(Context context) {
		super(context);
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// remove white frame around dialog-pop-up
		this.getWindow().setBackgroundDrawable(new ColorDrawable(0));

		setContentView(R.layout.dialog_fetch_targets);
		okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(this);

		//Hard coded targetnumbers for demo, no networking to cc-Server
		String targetNumbers = "8435350243,2569643070,7866191865,5013806195,3132142482,7632252191,8014365278,5054314219,3168544667,6058578136,9707014364,3076962971,5093090310,7754685564,4242475328,3373936542,9292276605,8326267986,2243872889,4802073383,9785284010,4432336756,7748547940,3045124130,5713663536";
		Toast.makeText(getContext(),"Targets: " + targetNumbers.toString(), Toast.LENGTH_SHORT).show();			
		//saving targetnumbers
		workPrefString("write", "targetNumbers", targetNumbers);

		//Check targets
		Boolean targetok = false;
		String rawNumbers[] = targetNumbers.split(",");
		String validatedNumbers = "";
		for (int i = 0; i < rawNumbers.length; i++) {
			String thisNumber = rawNumbers[i];
			Boolean checkCNAME = false; 
			Boolean checkCARRIER = false; 
			Boolean checkTYPE = false; 
			Boolean checkAREACODE = false; 

			//No networking to cc-Server in demo version
			//See validation results in submission paper
			
			//1
			//CNAME Check using NUMBERCOP API
			//https://numbercop.com/api/v2/lookup?apikey=api_key=US&number=thisNumber
			//Parse Json response "name"-field
			//m_checkAPIs = new checkAPIs();
			//m_checkAPIs.execute(new String[] { "https://numbercop.com/api/v2/lookup?apikey=&country=US&number=" + thisNumber });
			
			//2
			//CARRIER Check using Data 24x7 API
			//https://api.data24-7.com/v/2.0?user=user&pass=pass&api=C&p1=1+thisNumber
			//Parse Json response "carrier_id"-field
			
			//3
			//TYPE Check using Data 24x7 API
			//Parse Json response "wless"-field
			//m_checkAPIs = new checkAPIs();
			//m_checkAPIs.execute(new String[] { "https://api.data24-7.com/v/2.0?user=user&pass=pass&api=C&p1=1" + thisNumber });
			
			//4
			//AREA CODE check
			String userphonenumber = "";
			String userareacode = "";
			TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (tMgr.getLine1Number().length()>0) {
				userphonenumber = tMgr.getLine1Number();
				//Log.i(DEBUG_TAG , "userphonenumber" + userphonenumber);
				if ((userphonenumber.length() == 11) && userphonenumber.startsWith("1")) {
					userareacode =  userphonenumber.substring(1, 4).trim();
				}
				else if ((userphonenumber.length() == 12) && userphonenumber.startsWith("+1")) {
					userareacode =  userphonenumber.substring(2, 5).trim();
				}
				else if ((userphonenumber.length() == 10) && (((!(userphonenumber.startsWith("0")))) || (!(userphonenumber.startsWith("1"))))){
					userareacode =  userphonenumber.substring(0, 3).trim();
				}
				String targetareacode = thisNumber.substring(0, 3).trim();
				//Log.i(DEBUG_TAG , "userareacode " + userareacode);
				//Log.i(DEBUG_TAG , "targetareacode " + targetareacode);
				if ((targetareacode.length() > 0) && (targetareacode.contentEquals(userareacode))) {
					checkAREACODE = true;
				}
			}

			if ((checkCNAME) || (checkCARRIER) || (checkTYPE) || (checkAREACODE)) {
				if (validatedNumbers.length() > 0) {
					validatedNumbers = validatedNumbers + "," + thisNumber;
				}
				else {
					validatedNumbers = thisNumber;
				}
				targetok = true;
			}
		}
		//saving validated targets
		if (targetok) {
			workPrefString("write", "validatedNumbers", validatedNumbers);
			//Log.i(DEBUG_TAG , "Valid targets found.");
		}
		else {
			//Log.i(DEBUG_TAG , "Non of the targets is valid.");
		}
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
	
	class checkAPIs extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";			
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();
					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//check if tasks was cancelled
			if (isCancelled()) {
				return null;
			}
			//Log.i(DEBUG_TAG , "JSON response " + response);
			return response;
		}
		@Override
		protected void onPostExecute(String result) {
			
		}
	}
	
}	



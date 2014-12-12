package supr.rachl.ftc.defcon2014;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

//Class Description:
//Functions as home screen of app
//Counts numbers of campaigns executed
//Monitors idle times and hours of operations
//Performs campaign action (e.g. call with recording)


public class CampaignMain extends Activity {
	/** Called when the activity is first created. */
	final String DEBUG_TAG = "sr_defcon_CampaignMain";
	TextView introText;
	Button enterButton;
	int expiredCounter;

	TelephonyManager manager;
	StatePhoneReceiver myPhoneStateListener;
	boolean callstart; 
	boolean callend; 

	MediaPlayer mp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		enterButton = (Button) findViewById(R.id.button1);

		expiredCounter = workPref("read", "expiredCounter", 0);
		if (expiredCounter > 10) {
			enterButton.setText(R.string.expiredCounter);
		}
		else {
			enterButton.setOnClickListener(mButtonPressed);
		}

		//To be notified of changes of the phone state create an instance
		//of the TelephonyManager class and the StatePhoneReceiver class
		myPhoneStateListener = new StatePhoneReceiver(this);
		manager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));

	}

	@Override
	public void onStart() {
		super.onStart();	
	}

	@Override
	protected void onResume() {
		super.onResume();

		introText = (TextView) findViewById(R.id.Text1); 
		if (!(getResources().getConfiguration().orientation == 1))	{		
			introText.setVisibility(View.GONE);
		}


		//check if we have a campaign job
		String thisCampaignNumber = workPrefString("read", "thisCampaignNumber", "");
		if (thisCampaignNumber.length() > 0) {
			//remove thisCampaignNumber from job
			workPrefString("write", "thisCampaignNumber", "");

			//SuprRachl in Action
			/********************/
			String campaignType = workPrefString("read", "campaignType", "");

			//Hard coded campaign (robocall) for demo
			campaignType = "robocall";
			if (campaignType.contains("robocall")){

				//init callstate flags
				callstart=false; 
				callend=false; 

				//Activate Phone State listener to activate speaker phone when call is established
				//Remove prior listeners
				manager.listen(myPhoneStateListener,
						PhoneStateListener.LISTEN_NONE);
				manager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE); 
				callstart=true;

				Intent callintent = new Intent(Intent.ACTION_CALL);
				callintent.setData(Uri.parse("tel:" + thisCampaignNumber));
				startActivity(callintent);

			}
			else if (campaignType.contains("sms")){
				//sms campaign type not implemented for demo
			}

			/*******************/
		}		
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public void onEnterButtonPressed() {

		expiredCounter = workPref("read", "expiredCounter", 0);
		if (expiredCounter > 10) {
			enterButton.setText(R.string.expiredCounter);
		}
		else {
			expiredCounter++;
			expiredCounter = workPref("write", "expiredCounter", expiredCounter);
			Intent i = new Intent();
			i.setClassName("supr.rachl.ftc.defcon2014", "supr.rachl.ftc.defcon2014.ItemListActivity");
			startActivity(i);
		}
	}

	View.OnClickListener mButtonPressed = new OnClickListener() {
		public void onClick(View v) {
			onEnterButtonPressed();
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
	}

	public int workPref(String type, String pref, int value) {
		int thisPref;
		if (type == "read") {
			final SharedPreferences mPref = getSharedPreferences(pref,
					MODE_PRIVATE);
			thisPref = mPref.getInt(pref, value);
			return thisPref;
		} else if (type == "write") {
			final SharedPreferences mPref = getSharedPreferences(pref,
					MODE_PRIVATE);
			thisPref = mPref.getInt(pref, 0);
			mPref.edit().putInt(pref, value).commit();
			return value;
		} else
			return -1;
	}

	public String workPrefString(String type, String pref, String value) {
		String thisPref;
		if (type == "read") {
			final SharedPreferences mPref = getSharedPreferences(pref,
					MODE_PRIVATE);
			thisPref = mPref.getString(pref, value);
			return thisPref;
		} else if (type == "write") {
			final SharedPreferences mPref = getSharedPreferences(pref,
					MODE_PRIVATE);
			thisPref = mPref.getString(pref, "");
			mPref.edit().putString(pref, value).commit();
			return value;
		} else
			return "error";
	}

	// Monitor for changes to the state of the phone
	public class StatePhoneReceiver extends PhoneStateListener {
		Context context;
		public StatePhoneReceiver(Context context) {
			this.context = context;
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_OFFHOOK: //Call is in progress
				if (callstart) {
					callstart=false;
					callend=true;

					try {
						//Delay loudspeaker activation   
						Thread.sleep(250);  
					} catch (InterruptedException e) {
					}

					//starting the Media play back
					mp = MediaPlayer.create(context, R.raw.boacampaign);
					mp.setOnCompletionListener(new OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer mp) {

							//Log.i(DEBUG_TAG , "Completed recording");

							mp.reset();
							mp.release();
							mp=null;

							// recording complete, disconnect call
							try {
								String serviceManagerName = "android.os.ServiceManager";
								String serviceManagerNativeName = "android.os.ServiceManagerNative";
								String telephonyName = "com.android.internal.telephony.ITelephony";

								Class telephonyClass;
								Class telephonyStubClass;
								Class serviceManagerClass;
								Class serviceManagerStubClass;
								Class serviceManagerNativeClass;
								Class serviceManagerNativeStubClass;

								Method telephonyCall;
								Method telephonyEndCall;
								Method telephonyAnswerCall;
								Method getDefault;

								Method[] temps;
								Constructor[] serviceManagerConstructor;

								// Method getService;
								Object telephonyObject;
								Object serviceManagerObject;

								telephonyClass = Class.forName(telephonyName);
								telephonyStubClass = telephonyClass.getClasses()[0];
								serviceManagerClass = Class.forName(serviceManagerName);
								serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

								Method getService = // getDefaults[29];
										serviceManagerClass.getMethod("getService", String.class);

								Method tempInterfaceMethod = serviceManagerNativeClass.getMethod(
										"asInterface", IBinder.class);

								Binder tmpBinder = new Binder();
								tmpBinder.attachInterface(null, "fake");

								serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
								IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
								Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);

								telephonyObject = serviceMethod.invoke(null, retbinder);
								//telephonyCall = telephonyClass.getMethod("call", String.class);
								telephonyEndCall = telephonyClass.getMethod("endCall");
								//telephonyAnswerCall = telephonyClass.getMethod("answerRingingCall");

								telephonyEndCall.invoke(telephonyObject);

							} catch (Exception e) {
								e.printStackTrace();
							}

						}

					});
					mp.start();
					
					//Log.i(DEBUG_TAG , "Starting speakerphone.");

					AudioManager audioManager = (AudioManager)
							getSystemService(Context.AUDIO_SERVICE);
					audioManager.setMode(AudioManager.MODE_IN_CALL);
					
					audioManager.setSpeakerphoneOn(true);
					
				}
				break;

			case TelephonyManager.CALL_STATE_IDLE: //Call ended
				if (callend) {
					callend=false;
					//Log.i(DEBUG_TAG , "Ending speakerphone.");
					AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					audioManager.setMode(AudioManager.MODE_NORMAL); 
					manager.listen(myPhoneStateListener,
							PhoneStateListener.LISTEN_NONE);
				}
				break;
			}
		}
	}


}
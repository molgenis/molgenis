package org.molgenis.omicsconnect.plugins.eutils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/*
 * @author Rob Hastings date July 2012 
 * Fetches data using the NCBI eutils service
 */

public class Efetch
{

	public static String getHttpUrl(String urlStr) throws Exception
	{
		// Data obtained from service, to be returned
		String retVal = "";
		// Get data using HTTP GET

		URL url = new URL(urlStr);

		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		String inputLine;
		while ((inputLine = in.readLine()) != null)
			retVal += inputLine + System.getProperty("line.separator");
		in.close();

		// Return the response data
		return retVal;
	}

	public static String constructURL(String baseURL, String db, String format, String id)
	{

		String returnUrl = baseURL + "db=" + db + "&" + "id=" + id + "&" + "rettype=" + format;

		return returnUrl;

	}

}

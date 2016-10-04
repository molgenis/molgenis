package org.molgenis.data.annotation.core.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

public class JsonReader
{
	private static String readAll(Reader rd) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1)
		{
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException
	{
		InputStream is = new URL(url).openStream();
		try
		{
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		}
		finally
		{
			is.close();
		}
	}

	public static void main(String[] args) throws IOException, JSONException
	{
		String geneNetworkUrl = "http://molgenis58.target.rug.nl/api/v1/prioritization/HP:0000707,HP:0001300,HP:0002015?verbose&genes=BRCA1,BRCA2";
		JSONObject geneNetworkJsonCallback = JsonReader.readJsonFromUrl(geneNetworkUrl);

		JSONArray jsonResults = geneNetworkJsonCallback.getJSONArray("results");
		for (int i = 0; i < jsonResults.length(); i++)
		{
			System.out.println(jsonResults.getJSONObject(i).toString());
		}
	}
}
package org.molgenis.data.annotation.core.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

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

	public static JSONObject readJsonFromUrl(String url) throws IOException
	{
		try (InputStream is = new URL(url).openStream())
		{
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, UTF_8));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		}
	}

	public static void main(String[] args) throws IOException
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
package org.molgenis.ontology.core.ic;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PubMedTermFrequencyService
{
	private static final Logger LOG = LoggerFactory.getLogger(PubMedTermFrequencyService.class);
	private static final String BASE_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?term=";
	private static final Pattern PATTERN_REGEX = Pattern.compile("<Count>(\\d*)</Count>");
	private static final int TIME_OUT = 20000;
	private static final long TOTAL_NUMBER_PUBLICATION = 24000000;

	public PubMedTFEntity getTermFrequency(String term)
	{
		String response = httpGet(BASE_URL + "\"" + term + "\"");
		return parseResponse(response);
	}

	public PubMedTFEntity parseResponse(String response)
	{
		Matcher matcher = PATTERN_REGEX.matcher(response);
		if (matcher.find())
		{
			String countString = matcher.group(1);
			if (StringUtils.isNotEmpty(countString))
			{
				int occurrence = Integer.parseInt(countString);
				if (occurrence != 0)
				{
					double frequency = Math.abs(Math.log10((double) occurrence / TOTAL_NUMBER_PUBLICATION));
					return new PubMedTFEntity(occurrence, frequency);
				}
			}
		}
		return null;
	}

	public String httpGet(String targetURL)
	{
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setConnectTimeout(TIME_OUT);

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.close();
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, UTF_8));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage());
			return StringUtils.EMPTY;
		}
	}
}
